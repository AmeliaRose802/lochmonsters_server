import SerializableDataClasses.Snake
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SnakeManager() {

    val snakes = mutableMapOf<Int, Snake>();

    fun update() {
        for (snake in snakes.values) {
            snake.update();
        }
    }

    fun getAllSnakesBuffer(id: Int) : ByteBuffer{

        var snakesList = ByteBuffer.allocate(2 + Snake.BUFFER_LENGTH * snakes.size);
        snakesList.order(ByteOrder.LITTLE_ENDIAN)

        snakesList.putShort((snakes.size - 1).toShort())

        for (snake in Game.snakeManager.snakes.values) {
            //Only send data for other clients
            if (snake.id != id) {
                snakesList.put(snake.getSnakeDataByteBuffer())
            }
        }

        snakesList.flip()
        return snakesList;
    }
}