import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import kotlin.random.Random

class TCPHandler(val server: Server){
    var tcpClients = mutableMapOf<Int, SocketChannel>()
    var buffer = ByteBuffer.allocate(512);

    init {
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }

    fun getTCPPacket(socket: SocketChannel) {
        socket.read(buffer);
        buffer.flip()


        try{
            val type = buffer.getChar();

            println("TCP Message of type $type receved")

            when (type) {
                'c' -> {
                    println("Connection request")
                    val id = AddClient(buffer, socket)
                    sendConnectReply(socket, id);
                    sendAllOtherPlayerData(socket, id); //TODO: Ideally this would only send closest clients
                }
            }

            buffer.clear()
        }
        catch( e : Exception){
            println("ERROR: $e")
        }


    }

    /*
    Find the ID assocated with a given socket
     */
    fun getChannelID(socketChannel : SocketChannel) : Int{
        return tcpClients.filter { it.value == socketChannel }.keys.first()
    }

    //Add a new player to the game
    private fun AddClient(buffer: ByteBuffer, socket: SocketChannel) : Int{

        val s = Snake();
        s.buildFromByteBuffer(buffer);
        Game.snakes[s.id] = s;

        tcpClients[s.id] = socket;

        return s.id;
    }


    private fun sendConnectReply(clientSocket: SocketChannel, id: Int) {
        val reply: ByteBuffer = ByteBuffer.allocate(22)
        reply.order(ByteOrder.LITTLE_ENDIAN)
        val x: Int = Random.nextInt(0, 25); //Eventually this will pick a spot not near other snakes
        val y: Int = Random.nextInt(0, 25);

        reply.putChar('c');
        reply.putInt(id)
        reply.putInt(x)
        reply.putInt(y)
        reply.putLong(System.currentTimeMillis() - Game.startTime);

        reply.flip();
        clientSocket.write(reply)
    }


    private fun sendAllOtherPlayerData(clientSocket: SocketChannel, myID : Int){

        val otherPlayersData = ByteBuffer.allocate(4 + (60 * (Game.snakes.size -1)));
        otherPlayersData.order(ByteOrder.LITTLE_ENDIAN)

        otherPlayersData.putChar('o')

        otherPlayersData.putShort((Game.snakes.size - 1).toShort());


        for (snake in Game.snakes.values) {
            //Only send data for other clients
            if(snake.id != myID) {
                otherPlayersData.put(snake.getSnakeDataByteBuffer());
            }
        }

        otherPlayersData.flip();

        clientSocket.write(otherPlayersData);
    }




}