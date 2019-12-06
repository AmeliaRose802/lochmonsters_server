import Networking.Server
import SerializableDataClasses.Vector2
import java.nio.ByteBuffer


//This is a singleton
object Game {

    //Private
    const val PORT_NUM = 5555;
    private var nextID: Int = 0;

    //Public
    var startTime: Long = 0;
    val fieldSize = Vector2(25f, 25f);
    var server: Server = Server(PORT_NUM);
    var snakeManager : SnakeManager = SnakeManager();
    var foodManager : FoodManager = FoodManager();
    var timeSync = TimeSync();

    var gameRunning = true;

    fun loop() {
        gameRunning = true;
        startTime = System.currentTimeMillis();

        println("Game is running...")

        while (gameRunning) {
            server.update()
            snakeManager.update();
        }

        server.closeServer()
        snakeManager.snakes.clear()
        foodManager.food.clear()
    }

    fun stop(){
        println("Stopping game server")
        gameRunning = false
        server.closeServer()
        snakeManager.snakes.clear()
        foodManager.food.clear()
        foodManager.timer.cancel()
    }

    fun hasSnake(id: Int): Boolean {
        return snakeManager.snakes[id] != null;
    }

    fun getNextID(): Int {
        nextID++;
        return nextID;
    }

}