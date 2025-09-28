package com.mygame.arkanoid;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Copy file này vào cùng cấp với class Main trong folder src để chạy thử game mẫu trên mạng để tham khảo.
 */
abstract class GameObject {
    protected int x, y;
    protected int width, height;

    public abstract void update();
    public abstract void render(Graphics g);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

abstract class MovableObject extends GameObject {
    protected int dx, dy;
    public abstract void move();
}

// ================== PADDLE ==================
class Paddle extends MovableObject {
    private int speed = 10;
    private int screenWidth;
    private boolean moveLeft, moveRight;

    public Paddle(int x, int y, int width, int height, int screenWidth) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.screenWidth = screenWidth;
    }

    public void setMoveLeft(boolean moveLeft) { this.moveLeft = moveLeft; }
    public void setMoveRight(boolean moveRight) { this.moveRight = moveRight; }

    @Override
    public void move() {
        if (moveLeft && x > 0) x -= speed;
        if (moveRight && x + width < screenWidth) x += speed;
    }

    @Override
    public void update() { move(); }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, width, height);
    }
}

// ================== BALL ==================
class Ball extends MovableObject {
    private int speed = 5;
    private int directionX = 1, directionY = -1;
    private int screenWidth, screenHeight;
    private boolean stuckToPaddle = true;

    public Ball(int x, int y, int size, int screenWidth, int screenHeight) {
        this.x = x;
        this.y = y;
        this.width = size;
        this.height = size;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void releaseFromPaddle() { stuckToPaddle = false; }
    public void stickToPaddle(Paddle paddle) {
        stuckToPaddle = true;
        this.x = paddle.x + paddle.width / 2 - width / 2;
        this.y = paddle.y - height;
    }

    public boolean checkCollision(GameObject other) {
        return getBounds().intersects(other.getBounds());
    }

    public void bounceX() { directionX *= -1; }
    public void bounceY() { directionY *= -1; }

    public int getScreenHeight() { return screenHeight; }
    public boolean isStuckToPaddle() { return stuckToPaddle; }

    @Override
    public void move() {
        if (!stuckToPaddle) {
            x += directionX * speed;
            y += directionY * speed;

            if (x <= 0 || x + width >= screenWidth) bounceX();
            if (y <= 0) bounceY();
        }
    }

    @Override
    public void update() { move(); }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(x, y, width, height);
    }
}

// ================== BRICKS ==================
abstract class Brick extends GameObject {
    protected int hitPoints;
    protected Color color;

    public abstract void takeHit();
    public boolean isDestroyed() { return hitPoints <= 0; }
}

class NormalBrick extends Brick {
    public NormalBrick(int x, int y, int width, int height) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
        this.hitPoints = 1;
        this.color = Color.ORANGE;
    }

    @Override
    public void takeHit() { hitPoints--; }
    @Override
    public void update() {}
    @Override
    public void render(Graphics g) {
        if (!isDestroyed()) {
            g.setColor(color);
            g.fillRect(x, y, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);
        }
    }
}

// ================== GAME MANAGER ==================
class GameManager extends JPanel implements KeyListener, ActionListener {
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks;
    private Timer timer;
    private int lives = 3;
    private int score = 0;
    private boolean running = true;

    public GameManager(int screenWidth, int screenHeight) {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);

        paddle = new Paddle(screenWidth / 2 - 50, screenHeight - 50, 100, 15, screenWidth);
        ball = new Ball(screenWidth / 2, screenHeight - 70, 15, screenWidth, screenHeight);

        bricks = new ArrayList<>();
        int rows = 5, cols = 10, brickWidth = 60, brickHeight = 20;
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                bricks.add(new NormalBrick(50 + j * (brickWidth + 5), 50 + i * (brickHeight + 5), brickWidth, brickHeight));

        addKeyListener(this);
        setFocusable(true);
        timer = new Timer(16, this);
        timer.start();
    }

    private void updateGame() {
        if (!running) return;

        paddle.update();
        ball.update();

        if (ball.y > ball.getScreenHeight()) {
            lives--;
            if (lives <= 0) {
                running = false;
                System.out.println("Game Over! Score: " + score);
            } else {
                ball.stickToPaddle(paddle);
            }
        }

        checkCollisions();
    }

    public void checkCollisions() {
        if (ball.checkCollision(paddle) && !ball.isStuckToPaddle()) {
            ball.bounceY();
        }

        for (Brick brick : bricks) {
            if (!brick.isDestroyed() && ball.checkCollision(brick)) {
                brick.takeHit();
                score += 10;
                ball.bounceY();
                break;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paddle.render(g);
        ball.render(g);
        for (Brick brick : bricks) brick.render(g);

        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Lives: " + lives, 10, 40);
    }

    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) paddle.setMoveLeft(true);
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) paddle.setMoveRight(true);
        if (e.getKeyCode() == KeyEvent.VK_SPACE) ball.releaseFromPaddle();
    }
    @Override public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) paddle.setMoveLeft(false);
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) paddle.setMoveRight(false);
    }
    @Override public void keyTyped(KeyEvent e) {}
}

// ================== MAIN ==================
public class sampleCase {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Arkanoid OOP");
        GameManager game = new GameManager(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setVisible(true);
    }
}