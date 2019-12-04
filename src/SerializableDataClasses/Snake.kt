package SerializableDataClasses

import Game
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Snake(var id: Int = -1, var name: String = "NONE", var color: Color = Color(
    0,
    0,
    0
), var length : Short = 3, var pos: Vector2 = Vector2(
    0f,
    0f
), var dir: Vector2 = Vector2(
    0f,
    0f
)
){

    private val speed = 4f;
    private var predictedPos = pos
    private var posLastSet = System.currentTimeMillis()

    companion object{
        const val BUFFER_LENGTH = 60
    }

    fun buildFromByteBuffer(buffer: ByteBuffer){
       val r = buffer.short
       val g = buffer.short
       val b = buffer.short

       color = Color(r, g, b)

       val nameBuffer: ByteArray = ByteArray(32)

       buffer.get(nameBuffer)

       name = String(nameBuffer).trim()
       id = Game.getNextID()
   }

    fun getSnakeDataByteBuffer() : ByteBuffer{
        var snakeData = ByteBuffer.allocate(Snake.BUFFER_LENGTH)
        snakeData.order(ByteOrder.LITTLE_ENDIAN)

        snakeData.putInt(id) //Add ID
        snakeData.put(name.padEnd(32).toByteArray()) //Name
        snakeData.putShort(length) //Length of snake
        snakeData.put(color.getByteBuffer())
        snakeData.put(predictedPos.getByteBuffer())
        snakeData.put(dir.getByteBuffer())
        snakeData.flip()
        return snakeData;
    }

    fun getPositionByteBuffer() : ByteBuffer{
        val positionUpdate = ByteBuffer.allocate(30)
        positionUpdate.order(ByteOrder.LITTLE_ENDIAN)

        positionUpdate.putChar('u')

        positionUpdate.putInt(id)
        positionUpdate.putLong(System.currentTimeMillis() - Game.startTime)
        positionUpdate.put(predictedPos.getByteBuffer())

        positionUpdate.put(dir.getByteBuffer())

        positionUpdate.flip()

        return positionUpdate;
    }

    fun updatePosition(posUpdate : ByteBuffer, timestamp : Long){
        val posX = posUpdate.float
        val posY = posUpdate.float

        val dirX = posUpdate.float
        val dirY = posUpdate.float

        //Direction vector must be less then one so verify that it is correct before updating any values.
        if(dirX > 1 && dirY > 1 ){
            return;
        }

        val positionAtPacketSendTime = Vector2(posX, posY)
        val velocity = Vector2(dirX, dirY) * speed
        val gameTime = System.currentTimeMillis() - Game.startTime
        val elapsedTime = (gameTime - timestamp)/1000f

        val predictedCurrentPosition = constrainToBounds( positionAtPacketSendTime + (velocity * elapsedTime))

        pos = predictedCurrentPosition;
        predictedPos = pos;
        posLastSet = System.currentTimeMillis()

        dir = Vector2(dirX, dirY);
    }

    fun update(){
        val velocity = dir * speed;
        val deltaTime = System.currentTimeMillis() -  posLastSet
        predictedPos = constrainToBounds(pos + (velocity * (deltaTime/1000f))); //Update the predicted position each frame by linerally moving it in the same direction
    }

    fun getPredictedPositionAtTime(time: Long) : Vector2 {
        val velocity = dir * speed;
        val gameTime = time - posLastSet;
        val predicted = (velocity * (time/1000f));

        return predicted;
    }

    fun constrainToBounds(value : Vector2) : Vector2 {
        return Vector2(
            value.x.coerceIn(-Game.fieldSize.x, Game.fieldSize.x),
            value.y.coerceIn(-Game.fieldSize.y, Game.fieldSize.y)
        )
    }

    fun addSegment(){
        length++
    }
}