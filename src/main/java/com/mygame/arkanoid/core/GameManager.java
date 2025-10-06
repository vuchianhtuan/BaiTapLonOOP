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

import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.*;

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

    public void startGame() {
        loadAssets();

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
        paddle.update(inputHandler);
        ball.update(inputHandler, paddle);

        for (PowerUp p : powerUps) {
            p.update();
        }

        //thêm mới update ball
        if (ball.getY() > 600) {
            ball.setStuckToPaddle(true);
            ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
            ball.setY(paddle.getY() - ball.getHeight());
        }

        if(ball.checkCollision(paddle) && !ball.isStuckToPaddle()) {
            ball.bounceOff(paddle);
        }

        for (Brick brick : bricks) {
            if (!brick.isDestroyed() && ball.checkCollision(brick)) {
                brick.takeHit();
                score += 10;
                ball.bounceOff(brick);
                break;
            }
        }
    }

    public void loadAssets() {
        // Sử dụng AssetManager singleton để tải ảnh
        AssetManager.getInstance().loadImage("normalBrick", "/images/button_blue.png");
        AssetManager.getInstance().loadImage("ball", "/images/ball_red_large.png");
        AssetManager.getInstance().loadImage("paddle", "/images/button_yellow.png");
        AssetManager.getInstance().loadImage("expandPowerUp", "/images/hole_small_end.png");
    }

    public GameManager() {
        inputHandler = new InputHandler();
        bricks = new ArrayList<>();
        powerUps = new ArrayList<>();
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

