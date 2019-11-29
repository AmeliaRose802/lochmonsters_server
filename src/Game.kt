import Networking.Server
import SerializableDataClasses.Vector2
import java.nio.ByteBuffer


//This is a singleton
object Game {

    //Private
    private const val PORT_NUM = 5555;
    private var nextID: Int = 0;

    //Public
    var startTime: Long = 0;
    val fieldSize = Vector2(25f, 25f);
    val server: Server = Server(PORT_NUM);
    var snakeManager = SnakeManager()
    var foodManager = FoodManager()

    fun loop() {
        startTime = System.currentTimeMillis();
        val gameRunning = true;

        println("Game is running...")

        while (gameRunning) {
            server.update()
            snakeManager.update();
        }
    }

    fun hasSnake(id: Int): Boolean {
        return snakeManager.snakes[id] != null;
    }

    fun getNextID(): Int {
        nextID++;
        return nextID;
    }

}