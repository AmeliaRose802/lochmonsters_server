import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Snake(var id: Int = -1, var name: String = "NONE", var color: Color = Color(0,0,0), var length : Short = 3, var pos: Vector2 = Vector2(0f,0f), var dir: Vector2 = Vector2(0f, 0f)){

    val speed = 4f;
    var predictedPos = pos;
    var posLastSet = System.currentTimeMillis();

    fun buildFromByteBuffer(buffer: ByteBuffer){
       val r = buffer.short
       val g = buffer.short
       val b = buffer.short

       color = Color(r, g, b);

       val nameBuffer: ByteArray = ByteArray(32);

       buffer.get(nameBuffer);

       name = String(nameBuffer).trim();
       id = Game.getNextID();

       //This keeps getting updated to reflect where server thinks the snake is going


   }
    fun getSnakeDataByteBuffer() : ByteBuffer{
        var snakeData = ByteBuffer.allocate(60);
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
        val positionUpdate = ByteBuffer.allocate(30);
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
        val posX = posUpdate.float;
        val posY = posUpdate.float;

        val dirX = posUpdate.float;
        val dirY = posUpdate.float;

        //Direction vector must be less then one so verify that it is correct before updating any values.
        if(dirX > 1 && dirY > 1 ){
            return;
        }



        //val positionAtPacketSendTime = getPredictedPositionAtTime(timestamp)
        val positionAtPacketSendTime = Vector2(posX, posY)
        val velocity = Vector2(dirX, dirY) * speed;
        val gameTime = System.currentTimeMillis() - Game.startTime;
        val elapsedTime = gameTime - timestamp;


        var predictedCurrentPosition = constrainToBounds( positionAtPacketSendTime + (velocity * (elapsedTime/1000f)));


        if((positionAtPacketSendTime - predictedCurrentPosition).getMag() > 5 ){
            println("Overly diffrent prediction diff: "+ (positionAtPacketSendTime - predictedCurrentPosition).getMag())
            println("$name real:      $positionAtPacketSendTime Dir x: $dirX y: $dirY")
            println("$name predicted: $predictedCurrentPosition")
            println("Game time: "+((System.currentTimeMillis() - Game.startTime)/1000f)+". Time stamp: "+timestamp/1000f+". Elapsed Time: " + (((System.currentTimeMillis() - Game.startTime) - timestamp)/1000f))
        }



        //println("$name: Real: $positionAtPacketSendTime Expected: $predictedCurrentPosition. Elapsed time: $elapsedTime. Velocity $velocity")
        //It does not make sense to lerp to this since it is not being displayed to the users. Clients can do their own lerping

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

    fun getPredictedPositionAtTime(time: Long) : Vector2{
        val velocity = dir * speed;
        val gameTime = time - posLastSet;
        val predicted = (velocity * (time/1000f));

        return predicted;
    }

    fun constrainToBounds(value : Vector2) : Vector2{
        return Vector2(value.x.coerceIn(-Game.fieldSize.x, Game.fieldSize.x), value.y.coerceIn(-Game.fieldSize.y, Game.fieldSize.y));
        //return value;
    }

    fun addSegment(){
        length++;
    }
}