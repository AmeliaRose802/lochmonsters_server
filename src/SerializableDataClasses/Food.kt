package SerializableDataClasses

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Food(val id : Int, var position: Vector2) {

    fun getByteBuffer() : ByteBuffer{
        var foodData = ByteBuffer.allocate(12);
        foodData.order(ByteOrder.LITTLE_ENDIAN)

        foodData.putInt(id)
        foodData.put(position.getByteBuffer())

        foodData.flip()
        return foodData;
    }

    companion object{
        const val BUFFER_LENGTH = 12;
    }

}