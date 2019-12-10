import SerializableDataClasses.Snake
import com.payne.lc_server.Server.TimeLimitedMap
import tornadofx.osgi.impl.getBundleId
import java.lang.Float.max
import java.lang.Float.min
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random

class SnakeManager() {

    val snakes = mutableMapOf<Int, Snake>();
    val hits = TimeLimitedMap(500)
    val hitBy = TimeLimitedMap(500)

    val LENGTH_BOUNUS = .05f;

    fun update() {
        for (snake in snakes.values) {
            snake.update()
        }
        hits.update()
        hitBy.update()
    }

    fun handleHit(message : ByteBuffer){
        val hitterID = message.int
        val hitID = message.int



        if(hitBy.has(hitID) && hitBy.get(hitID) == hitterID){
            println("$hitterID hit $hitID");
            println("Found collision match on hit");
            handleCollision(hitterID, hitID);
        }
        else if(hits.has(hitID) && hits.get(hitID) == hitterID){
            println("Found collision that other also thought was hit");
            handleMutualHit(hitterID, hitID);
        }

        hits.add(hitterID, hitID)

    }

    fun handleHitBy(message: ByteBuffer){
        val hitID = message.int;
        val hitterID = message.int;



        if(hits.has(hitterID) && hits.get(hitterID) == hitID){
            println("$hitID was hit  by $hitterID");
            println("Found collion match on hit by")
            handleCollision(hitterID, hitID);
        }
        else if(hitBy.has(hitterID) && hitBy.get(hitterID) == hitID){
            println("Found collision that other also thought was a hit by");
            handleMutualHit(hitterID, hitID);
        }
        hitBy.add(hitterID, hitID);

    }

    /*
    Pick which snake is killed by collision.
    Random but weighted in favor of longer snakes and the hitter
     */
    private fun handleCollision(hitterID: Int, hitID: Int){
        val hitterLength = snakes[hitterID]!!.length
        val hitLength = snakes[hitID]!!.length

        //Needs to genrate a random number greater then this to win. 90% chance for hitter innitally
        var hitterWinThreshold : Double = .1;

        //If the snake hit was longer then make it harder for the hitter to win
        if(hitLength > hitterLength){
            hitterWinThreshold += min(LENGTH_BOUNUS * (hitLength - hitterLength), .9f);
        }

        println("To win random number must be greater then $hitterWinThreshold Hitter length $hitterLength hit length $hitLength")
        if(Random.nextFloat() > hitterWinThreshold){
            removeSnake(hitID)
        }
        else{
            removeSnake(hitterID)
        }
    }

    private fun handleMutualHit(id1 : Int, id2 : Int){

        var firstWins = .5;

        firstWins += min((snakes[id2]!!.length -  snakes[id1]!!.length) * LENGTH_BOUNUS, .9f);

        println("First wins threshold $firstWins")
        if(Random.nextFloat() > firstWins){
            println("removing second, snake $id2");
            removeSnake(id2)
        }
        else{
            println("removing first,  snake $id1");
            removeSnake(id1)
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