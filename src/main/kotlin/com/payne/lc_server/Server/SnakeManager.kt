import SerializableDataClasses.Snake
import com.payne.lc_server.Server.TimeLimitedMap
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SnakeManager() {

    val snakes = mutableMapOf<Int, Snake>();
    val hits = TimeLimitedMap(500)
    val hitBy = TimeLimitedMap(500)


    fun update() {
        for (snake in snakes.values) {
            snake.update()
        }
        hits.update()
        hitBy.update()
    }

    fun handleCollision(message : ByteBuffer){
        val hitterID = message.int
        val hitID = message.int
        hits.add(hitterID, hitID)


        if(hitBy.get(hitID) == hitterID){
            println("$hitterID hit $hitID");
            println("Found collision match on hit");
            removeSnake(hitID);
        }
    }

    fun handleHitBy(message: ByteBuffer){
        val hitID = message.int;
        val hitterID = message.int;
        hitBy.add(hitterID, hitID);


        if(hits.get(hitterID) == hitID){
            println("$hitID was hit  by $hitterID");
            println("Found collion match on hit by")
            removeSnake(hitID);
        }
    }

    private fun removeSnake(id : Int){
        Game.server.tcpHandler.closeConnection(id);
        Game.server.udpHandler.closeConnection(id);
        snakes.remove(id);
    }

    fun getAllSnakesBuffer(id: Int) : ByteBuffer{

        var snakesList = ByteBuffer.allocate(2 + Snake.BUFFER_LENGTH * snakes.size);
        snakesList.order(ByteOrder.LITTLE_ENDIAN)

        snakesList.putShort((snakes.size - 1).toShort())

        for (snake in Game.snakeManager!!.snakes.values) {
            //Only send data for other clients
            if (snake.id != id) {
                snakesList.put(snake.getSnakeDataByteBuffer())
            }
        }

        snakesList.flip()
        return snakesList;
    }
}