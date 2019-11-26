import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.TimerTask
import kotlin.random.Random

class FoodManager : TimerTask() {
    var nextID = 0;
    val food = mutableMapOf<Int, Vector2>();

    override fun run() {
        val foodPos = Vector2.randInRange(Game.fieldSize)
        food[nextID] = foodPos;
        sendFoodPos(nextID);

        nextID++;
    }

    fun sendFoodPos(id: Int){
        println("Sending food position")
        var snakeData = ByteBuffer.allocate(14);
        snakeData.order(ByteOrder.LITTLE_ENDIAN)
        snakeData.putChar('f')
        snakeData.putInt(id);
        snakeData.put(food[nextID]!!.getByteBuffer())

        snakeData.flip()
        Game.server.tcpHandler.broadcastToOthers(snakeData);
    }

    fun foodEaten(message : ByteBuffer){
        val foodID = message.getInt();
        val snakeID = message.getInt();
        println("Food: $foodID eaten by snake: $snakeID")

        if(food.containsKey(foodID) && Game.gameManager.snakeManager.snakes.containsKey(snakeID)){
            food.remove(foodID);
            Game.gameManager.snakeManager.snakes[snakeID]!!.addSegment()
        }
        else{
            println("Got message about food or snake that does not exist")
            println(food[foodID]);
            println(Game.gameManager.snakeManager.snakes[snakeID])
            //TODO: Tell client that they did not actually collect the food they thought they did
        }
    }

    fun sendFoodEaten(foodID : Int, snakeID : Int){
        var snakeData = ByteBuffer.allocate(14);
        snakeData.order(ByteOrder.LITTLE_ENDIAN)
        snakeData.putChar('e')
        snakeData.putInt(foodID)
        snakeData.putInt(snakeID)

        snakeData.flip()
        Game.server.tcpHandler.broadcastToOthers(snakeData, snakeID)
    }

    //It might need to update stuff in the future, I don't know
    fun update(){}

}