import java.*
import java.io.*
import java.net.*
const val portNum = 5555

//Client and server exchange messages of set types each of which has a set format and length
val messageLengths = mapOf('c' to 33);

data class Color(val r : Int, val b: Int, val g: Int);


fun main(args: Array<String>) {
    println("Server is listening...")

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

            val r = dis.readShort();
            val g = dis.readShort();
            val b = dis.readShort();
            println("The color is $r $b $g");

            val name : ByteArray = ByteArray(40);

            println(dis.read(name, 13, length));

            /*
            var sholdRun = true;
            while(sholdRun){
                val data = dis.read();
                println(data.toChar());
                sholdRun = (data != -1);
            }*/
            //val c = Color(dis.read(),dis.read(),dis.read());
            //println("c: $c");
        }




        ss.close()
    } catch (e: Exception) {
        println(e)
    }
}
