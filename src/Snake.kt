import java.nio.ByteBuffer
import java.nio.ByteOrder




data class Snake(var id: Int = -1, var name: String = "NONE", var color: Color = Color(0,0,0), var length : Short = 0, var pos: Vector2 = Vector2(0f,0f), var dir: Vector2 = Vector2(0f, 0f)){

   fun buildFromByteBuffer(buffer: ByteBuffer){
       val r = buffer.short
       val g = buffer.short
       val b = buffer.short

       color = Color(r, g, b);

       val nameBuffer: ByteArray = ByteArray(32);

       buffer.get(nameBuffer);

       name = String(nameBuffer).trim();
       id = Game.getNextID();

   }
    fun getSnakeDataByteBuffer() : ByteBuffer{
        var snakeData = ByteBuffer.allocate(60);
        snakeData.order(ByteOrder.LITTLE_ENDIAN)

        snakeData.putInt(id) //Add ID
        snakeData.put(name.padEnd(32).toByteArray()) //Name
        snakeData.putShort(length) //Length of snake
        snakeData.put(color.getByteBuffer())
        snakeData.put(pos.getByteBuffer())
        snakeData.put(dir.getByteBuffer())
        snakeData.flip()
        return snakeData;
    }

    fun getPositionByteBuffer() : ByteBuffer{
        val positionUpdate = ByteBuffer.allocate(30);
        positionUpdate.order(ByteOrder.LITTLE_ENDIAN)

        positionUpdate.putChar('u')

        positionUpdate.putInt(id)
        positionUpdate.putLong(System.currentTimeMillis() - Game.startTime)
        positionUpdate.put(pos.getByteBuffer())
        positionUpdate.put(dir.getByteBuffer())

        positionUpdate.flip()

        return positionUpdate;
    }

    fun updatePosition(posUpdate : ByteBuffer){
        pos.x = posUpdate.float;
        pos.y = posUpdate.float;

        dir.x = posUpdate.float;
        dir.y = posUpdate.float;
    }
}