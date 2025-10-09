package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Ball;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.systems.LevelManager;
import com.mygame.arkanoid.systems.ScoreManager;
import com.mygame.arkanoid.objects.Paddle;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.engine.SoundManager;
import com.mygame.arkanoid.util.ErrorHandler;
import com.mygame.arkanoid.systems.MenuManager;

import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class GameManager {
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private PowerUp activePowerUp;
    private int score;
    private int lives;
    private String gameState;

    private ScoreManager scoreManager;
    private LevelManager levelManager;
    private Renderer renderer;
    private SoundManager soundManager;
    private InputHandler inputHandler;
    private MenuManager menuManager;

    public void startGame() {
        paddle = new Paddle(350, 550, 100, 20);
        ball = new Ball(390, 530, 15, 15);
        bricks.clear();
        powerUps.clear();

        // Hủy bỏ hiệu ứng power-up cũ khi bắt đầu game mới
        if (activePowerUp != null) {
            activePowerUp.removeEffect(paddle);
            activePowerUp = null;
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                if (i == 2 && j == 5) {
                    bricks.add(new ExplosiveBrick(j * 70 + 50, i * 30 + 50, 60, 20));
                } else {
                    bricks.add(new NormalBrick(j * 70 + 50, i * 30 + 50, 60, 20));
                }
            }
        }
    }

    // Thêm phương thức này vào cuối lớp GameManager
    private void explode(Brick sourceBrick, double radius) {
        // Lấy tọa độ trung tâm của viên gạch nổ
        int sourceCenterX = sourceBrick.getX() + sourceBrick.getWidth() / 2;
        int sourceCenterY = sourceBrick.getY() + sourceBrick.getHeight() / 2;

        // Tạo một danh sách để chứa những viên gạch sẽ bị phá hủy bởi vụ nổ
        List<Brick> bricksToDestroy = new ArrayList<>();

        // Duyệt qua tất cả các viên gạch còn lại
        for (Brick otherBrick : bricks) {
            // Bỏ qua chính viên gạch vừa nổ
            if (otherBrick == sourceBrick) {
                continue;
            }

            int otherCenterX = otherBrick.getX() + otherBrick.getWidth() / 2;
            int otherCenterY = otherBrick.getY() + otherBrick.getHeight() / 2;

            // Tính khoảng cách giữa hai tâm gạch
            double distance = Math.sqrt(Math.pow(sourceCenterX - otherCenterX, 2) + Math.pow(sourceCenterY - otherCenterY, 2));

            // Nếu gạch khác nằm trong bán kính vụ nổ, thêm nó vào danh sách cần phá hủy
            if (distance <= radius) {
                bricksToDestroy.add(otherBrick);
            }
        }

        // Phá hủy và cộng điểm cho những viên gạch trong danh sách
        for (Brick brick : bricksToDestroy) {
            // Đảm bảo không xử lý lặp lại nếu một viên gạch đã bị phá hủy
            if (!brick.isDestroyed()) {
                // Giảm máu của gạch về 0 để chắc chắn nó bị phá hủy
                while(!brick.isDestroyed()) {
                    brick.takeHit();
                }
                score += 10; // Cộng điểm
            }
        }
    }

    public void updateGame() {
        if ("PLAYING".equals(gameState)) {
            paddle.update(inputHandler);
            ball.update(inputHandler, paddle);

            // 1. CẬP NHẬT POWER-UP ĐANG RƠI VÀ KIỂM TRA VA CHẠM
            Iterator<PowerUp> powerUpIterator = powerUps.iterator();
            while (powerUpIterator.hasNext()) {
                PowerUp p = powerUpIterator.next();
                p.update(); // Cho power-up rơi xuống

                // Nếu paddle hứng được power-up
                if (paddle.getBounds().intersects(p.getBounds())) {
                    // Hủy hiệu ứng cũ trước khi áp dụng hiệu ứng mới
                    if (activePowerUp != null) {
                        activePowerUp.removeEffect(paddle);
                    }

                    activePowerUp = p; // Gán power-up mới
                    activePowerUp.applyEffect(paddle); // Áp dụng hiệu ứng
                    powerUpIterator.remove(); // Xóa khỏi danh sách đang rơi
                }
                // Nếu power-up rơi ra ngoài màn hình
                else if (p.getY() > 600) { // 600 là chiều cao màn hình
                    powerUpIterator.remove();
                }
            }

            // 2. QUẢN LÝ THỜI GIAN POWER-UP ĐANG CÓ HIỆU LỰC
            if (activePowerUp != null) {
                activePowerUp.tick(); // Đếm ngược
                if (activePowerUp.isExpired()) {
                    activePowerUp.removeEffect(paddle); // Hủy hiệu ứng
                    activePowerUp = null; // Xóa power-up
                }
            }

            if(ball.checkCollision(paddle) && !ball.isStuckToPaddle()) {
                if (paddle.isSticky()) {
                    ball.stickToPaddle(paddle);
                } else {
                    ball.bounceOff(paddle);
                }
            }

            Iterator<Brick> brickIterator = bricks.iterator();
            while (brickIterator.hasNext()) {
                Brick brick = brickIterator.next();

                // Chỉ kiểm tra va chạm với những viên gạch chưa bị phá hủy
                if (!brick.isDestroyed() && ball.checkCollision(brick)) {
                    brick.takeHit(); // Gạch nhận sát thương
                    score += 10;
                    ball.bounceOff(brick);

                    // Kiểm tra ngay sau khi nhận sát thương, nếu gạch bị phá hủy thì xóa nó
                    if (brick.isDestroyed()) {
                        double rand = Math.random();
                        if (rand < 0.15) { // 15% cơ hội ra Expand Paddle
                            powerUps.add(new ExpandPaddlePowerUp(brick.getX(), brick.getY(), 30, 30));
                        } else if (rand < 0.3) { // 15% cơ hội ra Sticky Paddle
                            powerUps.add(new StickyPaddlePowerUp(brick.getX(), brick.getY(), 30, 30));
                        }

                        // (Nếu có logic cũ cho explosive brick thì giữ nguyên)
                        if (brick instanceof ExplosiveBrick) {
                            explode(brick, 100.0);
                        }
                    }
                    break; // Thoát khỏi vòng lặp để bóng không va chạm nhiều gạch trong 1 frame
                }
            }
            bricks.removeIf(brick -> brick.isDestroyed());
        } else if ("MENU".equals(gameState)) {
            menuManager.update();
        }
    }

    public void loadAssets() {
        AssetManager.getInstance().loadImage("normalBrick", "/images/button_blue.png");
        AssetManager.getInstance().loadImage("ball", "/images/ball_red_large.png");
        AssetManager.getInstance().loadImage("paddle", "/images/button_yellow.png");
        AssetManager.getInstance().loadImage("expandPowerUp", "/images/hole_small_end.png");
        AssetManager.getInstance().loadImage("menuBackground", "/images/backGroundMenu.png");
        AssetManager.getInstance().loadImage("explosiveBrick", "/images/button_grey.png");
        AssetManager.getInstance().loadImage("stickyPowerUp", "/images/star.png");
    }

    public GameManager() {
        inputHandler = new InputHandler();
        bricks = new ArrayList<>();
        powerUps = new ArrayList<>();
        this.soundManager = new SoundManager();
        loadAssets();
        menuManager = new MenuManager(this, inputHandler);
        // Đặt trạng thái ban đầu của game là MENU
        this.gameState = "MENU";
        soundManager.playBackgroundMusic("RegressiveTrip_Release.wav");
    }

    public MenuManager getMenuManager() { return menuManager; }
    public String getGameState() { return gameState; }
    public void setGameState(String state) {
        if (this.gameState != null && this.gameState.equals(state)) {
            return;
        }
        this.gameState = state; // Cập nhật trạng thái mới
        // Dựa vào trạng thái mới để đổi nhạc
        if ("PLAYING".equals(state)) {
            // Nếu chuyển sang màn hình chơi, bật nhạc game
            soundManager.playBackgroundMusic("DiracSea.wav");
        } else if ("MENU".equals(state)) {
            soundManager.playBackgroundMusic("RegressiveTrip_Release.wav");
        } else {
            // Nếu là các trạng thái khác (GAME_OVER,...) thì dừng nhạc
            soundManager.stopBackgroundMusic();
        }
    }

    public void handleInput() {}
    public void checkCollisions() {}
    public void gameOver() {}
    public Paddle getPaddle() { return paddle; }
    public Ball getBall() { return ball; }
    public List<Brick> getBricks() { return bricks; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public InputHandler getInputHandler() { return inputHandler; }
}

