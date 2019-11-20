import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.DatagramChannel
import java.nio.channels.SocketChannel

class UDPHandler(val server: Server) {

    data class UDPClientData(val id: Int, var udpAddress: InetSocketAddress?, var mostRecentUDPUpdate: Long);

    var udpClients = mutableMapOf<Int, UDPClientData>();

    var buffer = ByteBuffer.allocate(512);

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
            if(udpClients.get(id) == null){
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
                    handlePosUpdate(id, buffer);
                }
            }

        } catch (e: ClientNotConnected ) {
            println(e)
            sendTerminationMessage(address)
        }
        catch(e: OutOfDatePacket){
            println(e);
        }

        buffer.clear()

    }

    fun sendTerminationMessage(client: InetSocketAddress) {
        //TODO
    }

    fun handlePosUpdate(id: Int, data: ByteBuffer) {
        Game.snakes[id]?.updatePosition(data);
        sendPositionUpdate(id);
    }


    //Sends position update for client specified by id to all clients (including origional sender)
    fun sendPositionUpdate(id: Int) {
        val posData = Game.snakes[id]?.getPositionByteBuffer();
        //udpClients.filterKeys { it != id }.forEach{println(it)}
        for (client in udpClients.values) {
            if(client.id != id){
                println("Sending to: " + client.udpAddress)
                server.udpSocketChannel.send(posData, client.udpAddress);
            }

        }

    }
}