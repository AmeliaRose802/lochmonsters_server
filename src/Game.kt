
val portNum = 5555;

//This is a singleton
object Game{

    //Public varibles
    var startTime : Long = 0;


    //Private varibles
    val snakes = mutableMapOf<Int, Snake>();
    private var nextID: Int = 0;

    private val server : Server = Server(portNum);

    init{
        println("Creating the game")
    }

    fun loop(){
        startTime = System.currentTimeMillis();
        val gameRunning = true;

        println("Game is running...")

        while(gameRunning){
            server.update()
        }
    }

    fun hasSnake(id : Int) : Boolean{
        return snakes[id] != null;
    }

    fun getNextID() : Int{
        nextID++;
        return nextID;
    }
}