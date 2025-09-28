import java.util.List;

// ------------------- LỚP CƠ SỞ -------------------
abstract class GameObject {
    protected int x, y;
    protected int width, height;

    public abstract void update();
    public abstract void render();
}

abstract class MovableObject extends GameObject {
    protected int dx, dy;
    public abstract void move();
}

// ------------------- PADDLE & BALL -------------------
class Paddle extends MovableObject {
    private int speed;
    private PowerUp currentPowerUp;

    public void moveLeft() {}
    public void moveRight() {}
    public void applyPowerUp(PowerUp powerUp) {}

    @Override public void move() {}
    @Override public void update() {}
    @Override public void render() {}
}

class Ball extends MovableObject {
    private int speed;
    private int directionX, directionY;
    private boolean stuckToPaddle = true;

    public void bounceOff(GameObject other) {}
    public boolean checkCollision(GameObject other) { return false; }
    public boolean isStuckToPaddle() { return stuckToPaddle; }

    @Override public void move() {}
    @Override public void update() {}
    @Override public void render() {}
}

// ------------------- BRICKS -------------------
abstract class Brick extends GameObject {
    protected int hitPoints;
    protected String type;

    public abstract void takeHit();
    public abstract boolean isDestroyed();
}

class NormalBrick extends Brick {
    @Override public void takeHit() {}
    @Override public boolean isDestroyed() { return false; }
    @Override public void update() {}
    @Override public void render() {}
}

class StrongBrick extends Brick {
    @Override public void takeHit() {}
    @Override public boolean isDestroyed() { return false; }
    @Override public void update() {}
    @Override public void render() {}
}

// Brick mới: nổ (phá gạch xung quanh)
class ExplosiveBrick extends Brick {
    @Override public void takeHit() {}
    @Override public boolean isDestroyed() { return false; }
    @Override public void update() {}
    @Override public void render() {}
}

// Brick mới: di chuyển
class MovingBrick extends Brick {
    @Override public void takeHit() {}
    @Override public boolean isDestroyed() { return false; }
    @Override public void update() {}
    @Override public void render() {}
}

// ------------------- POWERUPS -------------------
abstract class PowerUp extends GameObject {
    protected String type;
    protected int duration;

    public abstract void applyEffect(Paddle paddle);
    public abstract void removeEffect(Paddle paddle);
}

class ExpandPaddlePowerUp extends PowerUp {
    @Override public void applyEffect(Paddle paddle) {}
    @Override public void removeEffect(Paddle paddle) {}
    @Override public void update() {}
    @Override public void render() {}
}

class FastBallPowerUp extends PowerUp {
    @Override public void applyEffect(Paddle paddle) {}
    @Override public void removeEffect(Paddle paddle) {}
    @Override public void update() {}
    @Override public void render() {}
}

class ExtraLifePowerUp extends PowerUp {
    @Override public void applyEffect(Paddle paddle) {}
    @Override public void removeEffect(Paddle paddle) {}
    @Override public void update() {}
    @Override public void render() {}
}

class MultiBallPowerUp extends PowerUp {
    @Override public void applyEffect(Paddle paddle) {}
    @Override public void removeEffect(Paddle paddle) {}
    @Override public void update() {}
    @Override public void render() {}
}

class SlowBallPowerUp extends PowerUp {
    @Override public void applyEffect(Paddle paddle) {}
    @Override public void removeEffect(Paddle paddle) {}
    @Override public void update() {}
    @Override public void render() {}
}

class StickyPaddlePowerUp extends PowerUp {
    @Override public void applyEffect(Paddle paddle) {}
    @Override public void removeEffect(Paddle paddle) {}
    @Override public void update() {}
    @Override public void render() {}
}

// ------------------- GAME CORE -------------------
class GameManager {
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private int score;
    private int lives;
    private String gameState;

    private ScoreManager scoreManager;
    private LevelManager levelManager;
    private Renderer renderer;
    private SoundManager soundManager;
    private InputHandler inputHandler;

    public void startGame() {
        try {
            gameState = "RUNNING";
            // khởi tạo tài nguyên
        } catch (Exception e) {
            ErrorHandler.log("Lỗi khi bắt đầu game: " + e.getMessage());
        }
    }

    public void updateGame() {
        try {
            // cập nhật trạng thái game
        } catch (Exception e) {
            ErrorHandler.log("Lỗi update game: " + e.getMessage());
        }
    }

    public void handleInput() {}
    public void checkCollisions() {}
    public void gameOver() {}
}

// ------------------- ENGINE SUPPORT -------------------
class Renderer {
    private AssetManager assetManager;
    public void draw(GameObject obj) {}
}

class SoundManager {
    public void playSound(String soundName) {}
    public void playBackgroundMusic(String musicName) {}
    public void stopBackgroundMusic() {}
}

class InputHandler {
    public void handleKeyboardInput() {}
    public void handleMouseInput() {}
}

class AssetManager {
    public void loadImage(String path) {}
    public Object getImage(String name) { return null; }
}

// ------------------- SCORE & SAVE -------------------
class ScoreManager {
    private int currentScore;
    private int highScore;
    private Leaderboard leaderboard;

    public void addScore(int points) {}
    public int getCurrentScore() { return currentScore; }
    public int getHighScore() { return highScore; }
    public void resetScore() {}
}

class Leaderboard {
    private List<Integer> scores;
    public void addScore(int score) {}
    public List<Integer> getTopScores() { return scores; }
}

class SaveManager {
    public void saveGame(GameManager state) {}
    public GameManager loadGame() { return null; }
}

// ------------------- LEVEL -------------------
class Level {
    private int levelNumber;
    private List<Brick> bricks;

    public void loadLevel() {
        try {
            // load data
        } catch (Exception e) {
            ErrorHandler.log("Lỗi load level: " + e.getMessage());
        }
    }

    public boolean isCompleted() { return false; }
    public List<Brick> getBricks() { return bricks; }
}

class LevelManager {
    private List<Level> levels;
    private int currentLevelIndex;

    public void loadLevels() {}
    public Level getCurrentLevel() { return null; }
    public void nextLevel() {}
    public boolean hasMoreLevels() { return false; }
}

// ------------------- EFFECTS -------------------
abstract class Effect {
    protected int duration;
    public abstract void update();
    public abstract void render();
    public boolean isFinished() { return false; }
}

class ParticleEffect extends Effect {
    private int x, y;
    private int particleCount;

    @Override public void update() {}
    @Override public void render() {}
}

// ------------------- CẢI TIẾN -------------------
// Đa luồng: GameLoop chạy update + render song song với GUI
class GameLoop extends Thread {
    private volatile boolean running = true;
    private GameManager gameManager;
    private Renderer renderer;

    public GameLoop(GameManager gameManager, Renderer renderer) {
        this.gameManager = gameManager;
        this.renderer = renderer;
    }

    @Override
    public void run() {
        while (running) {
            try {
                gameManager.updateGame();
                // renderer.drawAll(gameManager) -> có thể thêm method trong Renderer
                Thread.sleep(16); // ~60 FPS
            } catch (Exception e) {
                ErrorHandler.log("Lỗi trong GameLoop: " + e.getMessage());
            }
        }
    }

    public void stopLoop() {
        running = false;
    }
}

// Xử lý lỗi tập trung
class ErrorHandler {
    public static void log(String msg) {
        System.err.println("[ERROR] " + msg);
    }
}
