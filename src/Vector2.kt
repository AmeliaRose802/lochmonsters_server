import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Vector2(var x: Float, var y: Float){
    fun getByteBuffer() : ByteBuffer{
        val buffer : ByteBuffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.flip();
        return buffer;
    }
}