import java.io.*
import java.net.*
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SelectionKey
import java.net.InetSocketAddress
import sun.nio.ch.IOUtil.configureBlocking
import java.nio.channels.ServerSocketChannel
import java.net.InetAddress
import java.nio.channels.Selector
import jdk.nashorn.internal.objects.ArrayBufferView.buffer
import java.nio.channels.SocketChannel


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
    data class Client(val name: String, val color: Color, var pos: Vector2, var dir: Vector2);

    //Currently Connected clients (eg monsters in the game)
    val clients = mutableMapOf<String, Client>()

    //TODO: Assign uneque ID's instead of indexing by name
    var nextID: Int = 0;

    //Initalize the sockets
    val udpSocket = DatagramSocket(portNum);
    //val tcpServerSocket = ServerSocket(portNum);

    val selector = Selector.open()
    val serverSocketChannel = ServerSocketChannel.open()
    var buffer = ByteBuffer.allocate(256)

    fun main(args: Array<String>) {


        //Until accepting clients actually works adding one for testing pourpuses.


        /*
        try {
            val ss = ServerSocket(portNum)
            val s = ss.accept()//establishes connection
            println("Established Connection");
            val dis = DataInputStream(s.getInputStream())
            //val bis = BufferedInputStream(s.getInputStream())

            val type = dis.read().toChar();
            println("type= $type")
            val length = messageLengths[type]!!;


            //Yes, I will be figuring out something more elegent later
            if(type === 'c'){
                AddClient(dis);
                SendConnectReply(s);
            }

            ss.close()
        } catch (e: Exception) {
            println(e)
        }
        */

    }

    fun init(){
        val host = InetAddress.getByName("localhost")

        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.bind(InetSocketAddress(host, portNum))
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
        val key: SelectionKey? = null
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }

    fun listen() {
        println("Server is listening...")
        //clients.put("AAAA", Client("AAAA", Color(0, 0, 0), Vector2(0f, 0f), Vector2(0f, 0f)))
        //getUDPPackets();

        while(true){
            selector.select()
            val selectedKeys = selector.selectedKeys()
            val iter = selectedKeys.iterator()
            while (iter.hasNext()) {

                val key = iter.next()

                if (key.isAcceptable) {
                    println("New connection avable");
                    val client = serverSocketChannel.accept()
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    println("Regestered new connection")
                }
                if(key.isReadable){
                    println("Data to read");

                    val socket : SocketChannel = key.channel() as SocketChannel

                    val dis = DataInputStream(socket.socket().getInputStream())
                    socket.read(buffer);
                    buffer.position(0);
                    val type = buffer.getChar();
                    println(String(buffer.array(), 0, buffer.array().size))

                    println("type= $type")

                    when(type){
                        'c' -> AddClient(buffer);
                    }
                }

                iter.remove()
            }
        }
    }

    fun getUDPPackets() {
        while (true) {
            val buf = ByteArray(256)

            var packet = DatagramPacket(buf, buf.size)
            udpSocket.receive(packet)

            val address = packet.address
            val port = packet.port
            packet = DatagramPacket(buf, buf.size, address, port)

            val dataBuffer = ByteBuffer.wrap(packet.data);
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN);

            //First byte of every packet indicates type
            val type = dataBuffer.char;


            println("First bit  $type");

            when (type) {
                'p' -> handlePosUpdate(dataBuffer);
                else -> {
                    println("Unknown packet type")
                }
            }
        }
    }


    fun handlePosUpdate(data: ByteBuffer) {
        val nameLength = data.short.toInt();
        val nameBuffer = ByteArray(nameLength);
        data.get(nameBuffer)

        val name = String(nameBuffer);

        println(name);


        if (clients[name] == null) {
            println("Unknown Client");
            return;
        }

        clients[name]!!.pos.x = data.float;
        clients[name]!!.pos?.y = data.float;

        clients[name]!!.dir?.x = data.float;
        clients[name]!!.dir?.y = data.float;

        println(clients[name]);
    }

    //Add a new player to the game
    fun AddClient(buffer: ByteBuffer): String {
        val r = buffer.short
        val g = buffer.short
        val b = buffer.short
        println("The color is $r $g $b");

        val name: ByteArray = ByteArray(32);

        buffer.get(name);

        val playerName = String(name).trim();
        println(playerName);

        val currentID = nextID;
        clients.put(playerName, Client(playerName, Color(r, g, b), Vector2(0f, 0f), Vector2(0f, 0f)))
        println(clients[playerName]);

        nextID++;

        return playerName;
    }

    fun SendConnectReply(clientSocket: Socket) {
        val dos = DataOutputStream(clientSocket.getOutputStream())
        dos.writeChar('c'.toInt())
        dos.writeInt(10);
        dos.writeInt(10);
        println(dos.size());
        dos.flush()
        println("Data sent");
    }
}
