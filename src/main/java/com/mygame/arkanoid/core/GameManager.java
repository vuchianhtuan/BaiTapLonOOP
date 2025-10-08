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

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                bricks.add(new NormalBrick(j * 70 + 50, i * 30 + 50, 60, 20));
            }
        }

        powerUps.add(new ExpandPaddlePowerUp(200, 10, 30, 30));
    }

    public void updateGame() {
        if ("PLAYING".equals(gameState)) {
            paddle.update(inputHandler);
            ball.update(inputHandler, paddle);

            for (PowerUp p : powerUps) {
                p.update();
            }

            if(ball.checkCollision(paddle) && !ball.isStuckToPaddle()) {
                ball.bounceOff(paddle);
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
                        brickIterator.remove(); // Xóa gạch hiện tại khỏi danh sách bricks
                    }

                    break; // Thoát khỏi vòng lặp để bóng không va chạm nhiều gạch trong 1 frame
                }
            }
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

