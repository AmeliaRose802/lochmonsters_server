import SerializableDataClasses.Food
import SerializableDataClasses.Vector2
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

//TODO: A lot of this feels like really gross leaky incapsulation with how it's derectly calling stuff on TCP manager, but that could just be paranoia
class FoodManager : TimerTask() {

    private var nextID = 0;
    private val MAX_FOOD = 200;
    private val FOOD_INTERVAL: Long = 2000;

    val food = mutableMapOf<Int, Food>();

    val timer = Timer()

    init {

        timer.schedule(this, 0, FOOD_INTERVAL)
    }

    //Spawn food at a regular interval until the maximum amount of food is reached
    override fun run() {
        if (food.size < MAX_FOOD) {
            food[nextID] = Food(nextID, Vector2.randInRange(Game.fieldSize - 1.0f));
            sendFoodPos(nextID);
            nextID++;
        }
    }

    private fun sendFoodPos(id: Int) {

        var snakeData = ByteBuffer.allocate(14);
        snakeData.order(ByteOrder.LITTLE_ENDIAN)
        snakeData.putChar('f')
        snakeData.put(food[id]!!.getByteBuffer())
        snakeData.flip()
        Game.server!!.tcpHandler.broadcast(snakeData);
    }

    fun foodEaten(message: ByteBuffer) {
        val foodID = message.int;
        val snakeID = message.int;

        if (food.containsKey(foodID) && Game.snakeManager?.snakes!!.containsKey(snakeID)) {
            food.remove(foodID);
            Game.snakeManager!!.snakes[snakeID]!!.addSegment()
            sendFoodEaten(foodID, snakeID);
        }
    }

    private fun sendFoodEaten(foodID: Int, snakeID: Int) {
        var snakeData = ByteBuffer.allocate(14);
        snakeData.order(ByteOrder.LITTLE_ENDIAN)
        snakeData.putChar('a')
        snakeData.putInt(foodID)
        snakeData.putInt(snakeID)

        snakeData.flip()
        Game.server.tcpHandler.broadcast(snakeData)
    }

    fun getAllFoodBuffer(): ByteBuffer {
        val numFood = food.size;
        var foodList = ByteBuffer.allocate(2 + Food.BUFFER_LENGTH * numFood);
        foodList.order(ByteOrder.LITTLE_ENDIAN)

        foodList.putShort(numFood.toShort());

        food.forEach {
            foodList.put(it.value.getByteBuffer());
        }

        foodList.flip()
        return foodList;
    }
}