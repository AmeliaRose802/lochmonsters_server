import SerializableDataClasses.Food
import SerializableDataClasses.Vector2
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


class TimeSync(){

    fun getTimeReply() : ByteBuffer {
        var buffer = ByteBuffer.allocate(10)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putChar('t');
        buffer.putLong(System.currentTimeMillis() -  Game.startTime)
        buffer.flip()
        return buffer;
    }
}