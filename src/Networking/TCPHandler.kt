package Networking

import Game
import SerializableDataClasses.Snake
import SerializableDataClasses.Vector2
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SocketChannel

class TCPHandler(val server: Server) {

    private var tcpClients = mutableMapOf<Int, SocketChannel>()
    private var buffer = ByteBuffer.allocate(512)

    init {
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }

    fun getPacket(socket: SocketChannel) {

        val amountRead: Int = socket.read(buffer)

        if (amountRead <= 0) {
            closeConnection(socket)
            return
        }

        buffer.flip()

        try {
            when (buffer.char) {
                'a' -> {
                    Game.foodManager.foodEaten(buffer);
                }
                'c' -> {
                    val id = addClient(buffer, socket)
                    sendConnectReply(socket, id)
                    sendNewClientData(id)
                }
                'e' -> {
                    closeConnection(socket)
                }
                't' -> {
                    socket.write(Game.timeSync.getTimeReply())
                }
            }
        } catch (e: Exception) {
            println("ERROR: $e")
        }
        buffer.clear()
    }


    private fun closeConnection(socket: SocketChannel) {
        tcpClients.filter { it.value == socket }.keys.forEach {
            Game.snakeManager.snakes.remove(it)
            server.udpHandler.udpClients.remove(it);
            tcpClients.remove(it);
        }

        socket.close()
    }

    //Add a new player to the game
    private fun addClient(buffer: ByteBuffer, socket: SocketChannel): Int {
        val newSnake = Snake()
        newSnake.buildFromByteBuffer(buffer)
        Game.snakeManager.snakes[newSnake.id] = newSnake

        tcpClients[newSnake.id] = socket

        return newSnake.id
    }

    private fun sendConnectReply(clientSocket: SocketChannel, id: Int) {
        val gameState = getGameState(id)

        val reply: ByteBuffer = ByteBuffer.allocate(22 + gameState.capacity())
        reply.order(ByteOrder.LITTLE_ENDIAN)

        val pos = Vector2.randInRange(Game.fieldSize)

        reply.putChar('c')
        reply.putInt(id)
        reply.put(pos.getByteBuffer())
        reply.putLong(System.currentTimeMillis() - Game.startTime)
        reply.put(gameState);

        reply.flip()
        clientSocket.write(reply)
    }


    private fun getGameState(id: Int): ByteBuffer {

        val snakeBuffer = Game.snakeManager.getAllSnakesBuffer(id);
        val foodBuffer = Game.foodManager.getAllFoodBuffer();

        val currentGameState = ByteBuffer.allocate(snakeBuffer.capacity() + foodBuffer.capacity())
        currentGameState.order(ByteOrder.LITTLE_ENDIAN)

        currentGameState.put(snakeBuffer)
        currentGameState.put(foodBuffer)

        currentGameState.flip()

        return currentGameState;
    }


    private fun sendNewClientData(id: Int) {
        val newPlayer = ByteBuffer.allocate(2 + Snake.BUFFER_LENGTH)

        newPlayer.order(ByteOrder.LITTLE_ENDIAN)

        newPlayer.putChar('n')
        newPlayer.put(Game.snakeManager.snakes[id]!!.getSnakeDataByteBuffer())
        newPlayer.flip();

        broadcast(newPlayer, id)
    }

    /*
    Send a message to all clients other then the one specified by the id passed in
     */
    fun broadcast(message: ByteBuffer, id: Int) {

        tcpClients.filter { it.key != id }.forEach {
            it.value.write((message))
            message.flip()
        }
    }

    fun broadcast(message: ByteBuffer) {
        tcpClients.forEach {
            it.value.write((message))
            message.flip()
        }
    }

}