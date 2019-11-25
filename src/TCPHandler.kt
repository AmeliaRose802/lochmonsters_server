import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel
import kotlin.random.Random

class TCPHandler(val server: Server) {
    var tcpClients = mutableMapOf<Int, SocketChannel>()


    init {

    }

    fun getTCPPacket(socket: SocketChannel) {
        var buffer = ByteBuffer.allocate(512)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        val amountRead : Int = socket.read(buffer)

        if(amountRead <= 0){
            closeConnection(socket)
            return
        }

        buffer.flip()



        try {
            val type = buffer.getChar()

            when (type) {
                'c' -> {
                    val id = AddClient(buffer, socket)
                    sendConnectReply(socket, id)
                    sendAllOtherPlayerData(socket, id)
                    sendNewClientData(id)
                }
                'e' -> {
                    closeConnection(socket)
                }
            }


        } catch (e: Exception) {
            println("ERROR: $e")
        }
        buffer.clear()


    }


    fun closeConnection(socket: SocketChannel) {
        tcpClients.filter { it.value == socket }.keys.forEach {
            Game.gameManager.snakeManager.snakes.remove(it)
            server.udpHandler.udpClients.remove(it);
            tcpClients.remove(it);
        }

        socket.close()
        return;
    }


    /*
    Find the ID assocated with a given socket
     */
    fun getChannelID(socketChannel: SocketChannel): Int {
        return tcpClients.filter { it.value == socketChannel }.keys.first()
    }

    //Add a new player to the game
    private fun AddClient(buffer: ByteBuffer, socket: SocketChannel): Int {

        val s = Snake()
        s.buildFromByteBuffer(buffer)
        Game.gameManager.snakeManager.snakes[s.id] = s

        tcpClients[s.id] = socket

        return s.id
    }


    private fun sendConnectReply(clientSocket: SocketChannel, id: Int) {
        val reply: ByteBuffer = ByteBuffer.allocate(22)
        reply.order(ByteOrder.LITTLE_ENDIAN)
        val x: Int = Random.nextInt(0, Game.fieldSize.x.toInt()) //Eventually this will pick a spot not near other snakes
        val y: Int = Random.nextInt(0, Game.fieldSize.x.toInt())

        reply.putChar('c')
        reply.putInt(id)
        reply.putInt(x)
        reply.putInt(y)
        reply.putLong(System.currentTimeMillis() - Game.startTime)

        reply.flip()
        clientSocket.write(reply)
    }


    private fun sendAllOtherPlayerData(clientSocket: SocketChannel, id: Int) {

        val otherPlayersData = ByteBuffer.allocate(4 + (60 * (Game.gameManager.snakeManager.snakes.size - 1)))
        otherPlayersData.order(ByteOrder.LITTLE_ENDIAN)

        otherPlayersData.putChar('o')

        otherPlayersData.putShort((Game.gameManager.snakeManager.snakes.size - 1).toShort())


        for (snake in Game.gameManager.snakeManager.snakes.values) {
            //Only send data for other clients
            if (snake.id != id) {
                otherPlayersData.put(snake.getSnakeDataByteBuffer())
            }
        }

        otherPlayersData.flip()

        clientSocket.write(otherPlayersData)

    }

    private fun sendNewClientData(id: Int) {
        val newPlayer = ByteBuffer.allocate(62)


        newPlayer.order(ByteOrder.LITTLE_ENDIAN)

        newPlayer.putChar('n')
        newPlayer.put(Game.gameManager.snakeManager.snakes[id]!!.getSnakeDataByteBuffer())
        newPlayer.flip();

        broadcastToOthers(newPlayer, id)

    }

    /*
    Send a message to all clients other then the one specified by the id passed in
     */
    private fun broadcastToOthers(message: ByteBuffer, id: Int) {
        tcpClients.filter { it.key != id }.forEach {
            println("Sending new client data to "+ it.value.remoteAddress)
            it.value.write((message))
        }
    }

}