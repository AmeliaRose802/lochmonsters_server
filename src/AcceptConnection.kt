import java.io.*
import java.net.*
import com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil.close
import java.lang.reflect.Array.getLength
import java.net.DatagramPacket
import java.net.InetAddress
import java.nio.ByteBuffer


const val portNum = 5555

//Client and server exchange messages of set types each of which has a set format and length
val messageLengths = mapOf('c' to 39);

data class Vector2(val x : Float, val y : Float);

data class Color(val r : Short, val b: Short, val g: Short);

data class Client(val name: String, val color: Color, val pos : Vector2, val dir : Vector2);

val clients = mutableMapOf<String, Client>()

var nextID : Int = 0;

fun main(args: Array<String>) {
    println("Server is listening...")
    getUDPPackets();


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


fun getUDPPackets() {
    val socket = DatagramSocket(portNum);
    val buf = ByteArray(256)
    var running = true

    while (running) {
        var packet = DatagramPacket(buf, buf.size)
        socket.receive(packet)


        val address = packet.address
        val port = packet.port
        packet = DatagramPacket(buf, buf.size, address, port)

        //First byte of every packet indicates type
        val type = String(packet.data, 0, 1).toCharArray()[0];

        println("First bit " + type);
        if(type == 'p'){
            ProcessPosUpdate(packet.data);
        }


        val received = String(packet.data, 0, packet.length)
        println(received)

        if (received == "end") {
            running = false
            continue
        }
        socket.send(packet)
    }
    socket.close()
}

fun ProcessPosUpdate(data : ByteArray){

    val name = String(data, 1, 32).trim()

    val buffer = ByteBuffer.wrap(data)
    val xPos = buffer.getInt()
    val yPos = buffer.getDouble()
    println("name $name  x: $xPos y: $yPos");
}
//Add a new player to the game
fun AddClient(dis : DataInputStream) : String{
    val r = dis.readShort();
    val g = dis.readShort();
    val b = dis.readShort();
    println("The color is $r $g $b");

    val name : ByteArray = ByteArray(32);

    dis.read(name)

    val playerName = String(name).trim();
    println(playerName);

    val currentID = nextID;
    clients.put(playerName, Client(playerName, Color(r, g, b), Vector2(0f,0f), Vector2(0f,0f)))
    println(clients[playerName]);

    nextID++;

    return playerName;
}

fun SendConnectReply(clientSocket : Socket){
    val dos = DataOutputStream(clientSocket.getOutputStream())
    dos.writeChar('c'.toInt())
    dos.writeInt(10);
    dos.writeInt(10);
    println(dos.size());
    dos.flush()
    println("Data sent");
}