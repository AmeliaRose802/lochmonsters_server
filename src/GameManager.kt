
class GameManager(){
    var snakeManager =  SnakeManager()
    var foodManager = FoodManager()

    fun update(){
        snakeManager.update();
        foodManager.update();
    }
}