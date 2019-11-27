import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.TimerTask
import kotlin.random.Random

class FoodManager : TimerTask() {
    var nextID = 0;
    val MAX_FOOD = 300;
    val food = mutableMapOf<Int, Vector2>();

    override fun run() {
        //If the amount of food got too ridiculus it could cause glitches
        if(food.size < MAX_FOOD){
            val foodPos = Vector2.randInRange(Game.fieldSize)
            food[nextID] = foodPos;
            sendFoodPos(nextID);

            nextID++;
        }
    }

    fun sendFoodPos(id: Int){
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

        if(food.containsKey(foodID) && Game.gameManager.snakeManager.snakes.containsKey(snakeID)){
            food.remove(foodID);
            Game.gameManager.snakeManager.snakes[snakeID]!!.addSegment()
            sendFoodEaten(foodID, snakeID);
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
        snakeData.putChar('a')
        snakeData.putInt(foodID)
        snakeData.putInt(snakeID)

        snakeData.flip()
        Game.server.tcpHandler.broadcastToOthers(snakeData, snakeID)

    }

    fun getAllFoodBuffer() : ByteBuffer{
        val numFood = food.size;
        var foodList = ByteBuffer.allocate(2 + 12 * numFood);
        foodList.order(ByteOrder.LITTLE_ENDIAN)

        foodList.putShort(numFood.toShort());

        food.forEach{
            foodList.putInt(it.key)
            foodList.put(it.value.getByteBuffer())
        }

        foodList.flip()
        return foodList;
    }

    //It might need to update stuff in the future, I don't know
    fun update(){}

}