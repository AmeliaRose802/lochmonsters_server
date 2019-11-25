import java.sql.Time

val portNum = 5555;
val MS_PER_FRAME = 2;


//This is a singleton
object Game{
    //Public varibles
    var startTime : Long = 0;
    var gameManager : GameManager = GameManager()
    val fieldSize = Vector2(25f, 25f);

    //Private varibles
    private var nextID: Int = 0;
    private val server : Server = Server(portNum);

    init{
        println("Creating the game")
    }

    fun loop(){
        startTime = System.currentTimeMillis();
        val gameRunning = true;

        println("Game is running...")
        println("Start time is "+ startTime);

        while(gameRunning){
            val startFrame = System.currentTimeMillis();

            server.update()
            gameManager.update()

            val amountToWait = MS_PER_FRAME -  (System.currentTimeMillis() - startFrame);

            if(amountToWait > 0){
                //Thread.sleep(amountToWait);
                //TODO: EIther put server in seprate thread or work out some way to avoid out of date packets
            }
        }
    }

    fun hasSnake(id : Int) : Boolean{
        return gameManager.snakeManager.snakes[id] != null;
    }

    fun getNextID() : Int{
        nextID++;
        return nextID;
    }
}