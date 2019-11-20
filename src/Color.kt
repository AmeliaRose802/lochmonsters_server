import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Color(val r: Short, val g: Short, val b: Short){
    fun getByteBuffer() : ByteBuffer{
        val buffer : ByteBuffer = ByteBuffer.allocate(6);
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(r);
        buffer.putShort(g);
        buffer.putShort(b);
        buffer.flip();
        return buffer;
    }

}