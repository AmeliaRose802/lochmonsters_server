import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.print.attribute.standard.MediaSize

data class Vector2(var x: Float, var y: Float){
    fun getByteBuffer() : ByteBuffer{
        val buffer : ByteBuffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.flip();
        return buffer;
    }

    operator fun plus(other : Vector2): Vector2 {
        return Vector2(x + other.x, y + other.y);
    }

    operator fun minus(other : Vector2): Vector2 {
        return Vector2(x - other.x, y - other.y);
    }

    operator fun times(other : Float): Vector2 {
        return Vector2(x * other, y * other);
    }

    operator fun times(other : Vector2): Vector2 {
        return Vector2(x * other.x, y * other.y);
    }

    fun getMag() : Float{
        return Math.sqrt(Math.pow(x.toDouble(), 2.0) + Math.pow(y.toDouble(), 2.0)).toFloat();
    }
}