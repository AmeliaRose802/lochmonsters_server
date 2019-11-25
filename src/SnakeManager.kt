

class SnakeManager(){


    val snakes = mutableMapOf<Int, Snake>();

    fun update(){
        for(snake in snakes.values){
            snake.update();
        }
    }
}