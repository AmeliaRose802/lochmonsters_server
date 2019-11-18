import java.io.*
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.*
import kotlin.random.Random
import jdk.nashorn.internal.objects.ArrayBufferView.buffer
import java.net.DatagramPacket




fun main() {
    val s = Server();
    s.init()
    s.listen()
}

class Server() {
    val portNum = 5555

    //Data classes
    data class Vector2(var x: Float, var y: Float);
    data class Color(val r: Short, val b: Short, val g: Short);
    data class Snake(val name: String, val color: Color, var length : Short, var pos: Vector2, var dir: Vector2);
    data class Client(val snake : Snake, val tcpSocket: SocketChannel?, var udpAddress: InetSocketAddress?);

    //Currently Connected clients (eg monsters in the game)
    val clients = mutableMapOf<Int, Client>()

    //TODO: Assign uneque ID's instead of indexing by name
    var nextID: Int = 1;

    //Initalize the sockets
    // val udpSocket = DatagramSocket(portNum);
    //val tcpServerSocket = ServerSocket(portNum);

    val selector = Selector.open()
    val serverSocketChannel = ServerSocketChannel.open()
    val udpSocketChannel = DatagramChannel.open()
    var buffer = ByteBuffer.allocate(256)

    fun init() {
        val host = InetAddress.getByName("localhost")

        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.bind(InetSocketAddress(host, portNum))
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)

        udpSocketChannel.configureBlocking(false)
        udpSocketChannel.bind(InetSocketAddress(host, portNum))
        udpSocketChannel.register(selector, SelectionKey.OP_READ)
        val key: SelectionKey? = null
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        clients.put(0, Client(Snake("Test", Color(255,17,19), 3, Vector2(0.5f, 1.5f), Vector2(0.0f, 0.0f)), null, null))
        clients.put(1, Client(Snake("bob", Color(8,255,3), 5, Vector2(7.0f, 4.0f), Vector2(1.0f, 0.5f)), null, null))
        nextID++
    }

    fun listen() {
        println("Server is listening...")

        while (true) {
            try {
                selector.select()
                val selectedKeys = selector.selectedKeys()
                val iter = selectedKeys.iterator()
                while (iter.hasNext()) {

                    val key = iter.next()

                    if (key.isAcceptable) {
                        println("New connection avable");
                        val client : SocketChannel = serverSocketChannel.accept()
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    }
                    if (key.isReadable) {

                        if (key.channel() is SocketChannel) {
                            getTCPPacket(key.channel() as SocketChannel, key)
                        } else if (key.channel() is DatagramChannel) {
                            getUDPPackets(key.channel() as DatagramChannel);
                        }
                        buffer.clear();
                    }

                    iter.remove()
                }
            } catch (e: IOException) {
                println("Error: $e");
            }
        }
    }


    fun getUDPPackets(socket: DatagramChannel) {
        val address = socket.receive(buffer) as InetSocketAddress

        buffer.position(0);

        val type = buffer.getChar();
        val id = buffer.getInt();
        if (clients[id] == null) {
            println("Unknown Client");
            return;
        }
        else{
            clients[id]!!.udpAddress = address;
        }


        when (type) {
            'p' -> {
                handlePosUpdate(id, buffer);
            }
        }


    }
    fun getTCPPacket(socket: SocketChannel, key: SelectionKey) {
        socket.read(buffer);
        buffer.position(0);
        val type = buffer.getChar();


        when (type) {
            'c' -> {
                println("Connection request")
                val id = AddClient(buffer, socket)

                sendConnectReply(socket, id); //Probley should be checking if write will block but oh well
                sendAllOtherPlayerData(socket, id); //TODO: Ideally this would only send closest clients
            }
        }
    }


    fun handlePosUpdate(id: Int, data: ByteBuffer) {

        clients[id]!!.snake.pos.x = data.float;
        clients[id]!!.snake.pos?.y = data.float;

        clients[id]!!.snake.dir?.x = data.float;
        clients[id]!!.snake.dir?.y = data.float;

        println(clients[id]!!.snake);

        sendPositionUpdate(id);

    }

    //Add a new player to the game
    fun AddClient(buffer: ByteBuffer, socket: SocketChannel): Int {
        val r = buffer.short
        val g = buffer.short
        val b = buffer.short

        val name: ByteArray = ByteArray(32);

        buffer.get(name);

        val playerName = String(name).trim();

        val currentID = nextID;
        //Add the client with the socket linked to it. Don't set IP and port yet, thouse are for UDP
        clients.put(currentID, Client(Snake(playerName, Color(r, g, b),3, Vector2(0f, 0f), Vector2(0f, 0f)), socket, null))

        nextID++;

        return currentID;
    }

    fun sendConnectReply(clientSocket: SelectableChannel, id: Int) {
        val reply: ByteArray = ByteArray(14)

        val x: Int = Random.nextInt(0, 100); //Eventually this will pick a spot not near other snakes
        val y: Int = Random.nextInt(0, 100);


        reply[0] = 'c'.toByte()
        reply[2] = id.toByte()
        reply[6] = x.toByte()
        reply[10] = y.toByte()


        val c = clientSocket as SocketChannel;
        c.write(ByteBuffer.wrap(reply))
    }


    fun sendAllOtherPlayerData(clientSocket: SelectableChannel, myID : Int){
        println("Attempting to send other player data");
        val s = clientSocket as SocketChannel
        val c = ByteBuffer.allocate(4 + (60 * (clients.size -1)));
        c.order(ByteOrder.LITTLE_ENDIAN)

        c.putChar('o')

        c.putShort((clients.size - 1).toShort());

        for ((id, client) in clients) {
            //Only send data for other clients
            if(id != myID) {

                c.putInt(id) //Add ID
                c.put(client.snake.name.padEnd(32).toByteArray()) //Name
                c.putShort(client.snake.length) //Length of snake
                c.putShort(client.snake.color.r) //R of color
                c.putShort(client.snake.color.g) //R of color
                c.putShort(client.snake.color.b) //R of color
                c.putFloat(client.snake.pos.x)
                c.putFloat(client.snake.pos.y)
                c.putFloat(client.snake.dir.x)
                c.putFloat(client.snake.dir.y)

            }
        }


        s.write(ByteBuffer.wrap(c.array())); //I have no god damm idea why this works!


        //s.write(c);

    }

    //Sends position update for client specified by id to all clients (including origional sender)
    fun sendPositionUpdate(id : Int){
        val c = ByteBuffer.allocate(22);
        c.order(ByteOrder.LITTLE_ENDIAN)

        c.putChar('u')

        c.putInt(id)
        c.putFloat(clients[id]!!.snake.pos.x)
        c.putFloat(clients[id]!!.snake.pos.y)
        c.putFloat(clients[id]!!.snake.dir.x)
        c.putFloat(clients[id]!!.snake.dir.y)

        for((id, client) in clients){
            if(client.udpAddress != null){
                udpSocketChannel.send(ByteBuffer.wrap(c.array()),client.udpAddress);
            }

            //s.write(ByteBuffer.wrap(c.array()));
        }

    }
}
