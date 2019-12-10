package Networking

import ClientNotConnected
import Game
import OutOfDatePacket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.DatagramChannel

class UDPHandler(val server: Server) {

    data class UDPClientData(val id: Int, var udpAddress: InetSocketAddress?, var mostRecentUDPUpdate: Long);
    var udpClients = mutableMapOf<Int, UDPClientData>();
    private var buffer: ByteBuffer = ByteBuffer.allocate(512);

    init {
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }

    /*
    Get a UDP packet and check if it is valid. If not discard it. If so handle it
     */
    fun getUDPPackets(socket: DatagramChannel) {

        val address = socket.receive(buffer) as InetSocketAddress
        buffer.flip()

        val type = buffer.char
        val id = buffer.int
        val timestamp = buffer.long

        try {
            if (!Game.hasSnake(id)) {
                throw ClientNotConnected("Game has never heard of this snake");
            }

            //IF the UDP client is not regestered, regester it
            if (udpClients.get(id) == null) {
                udpClients.put(id, UDPClientData(id, address, timestamp))
            }

            //Now that I've checked that its not null, assigning it to a varible so I don't have to assert not null every time
            val udpClient = udpClients[id]!!
            udpClient.udpAddress = address

            //Make sure it is up to date
            if (udpClient.mostRecentUDPUpdate > timestamp) {
                throw OutOfDatePacket("UDP Packet older then most recent message for snake");
            }

            udpClient.mostRecentUDPUpdate = timestamp

            //It is a valid packet
            when (type) {
                'p' -> {
                    handlePosUpdate(id, buffer, timestamp);
                }
            }

        } catch (e: ClientNotConnected) {
            println(e)
            sendTerminationMessage(address)
        } catch (e: OutOfDatePacket) {
        }

        buffer.clear()

    }

    public fun closeConnection(id : Int){
        sendTerminationMessage(udpClients[id]!!.udpAddress!!)
        udpClients.remove(id);
        Game.server.tcpHandler.notifyOthersClientLeft(id);
        Game.snakeManager.snakes.remove(id);
    }

    private fun sendTerminationMessage(client: InetSocketAddress) {
        println("Sending termination message on UDP");
        var termination = ByteBuffer.allocate(2)
        termination.order(ByteOrder.LITTLE_ENDIAN)
        termination.putChar('b');
        termination.flip();
        server.udpSocketChannel.send(termination, client);
    }

    fun terminateAll(){
        udpClients.forEach{
            sendTerminationMessage(it.value.udpAddress!!)
        }
    }

    private fun handlePosUpdate(id: Int, data: ByteBuffer, timestamp: Long) {
        Game.snakeManager!!.snakes[id]?.updatePosition(data, timestamp);
        sendPositionUpdate(id);
    }

    //Sends position update for client specified by id to all clients (including origional sender)
    private fun sendPositionUpdate(id: Int) {
        val posData = Game.snakeManager!!.snakes[id]!!.getPositionByteBuffer();
        broadcastToOthers(posData, id)
    }

    private fun broadcastToOthers(message: ByteBuffer, id: Int) {
        udpClients.filter { it.key != id }.forEach {
            server.udpSocketChannel.send(message, it.value.udpAddress);
        }
    }
}