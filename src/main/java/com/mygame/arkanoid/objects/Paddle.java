package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent; // Thêm import cho KeyEvent

public class Paddle extends MovableObject {
    private int speed = 10;
    private PowerUp currentPowerUp;
    private String imageName;

    public void moveLeft() {
        x -= speed;
        if (x < 0) x = 0;
    }
    public void moveRight() {
        x += speed;
        if (x + width >800) x = 800 - width;
        //Thiếu khai báo biến screenSize, để mặc định 800x600
    }
    public void applyPowerUp(PowerUp powerUp) {}

    public Paddle(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.imageName = "paddle";
    }

    // Cập nhật vị trí paddle dựa trên InputHandler
    public void update(InputHandler inputHandler) {
        //this.x = inputHandler.getMouseX() - this.width / 2;

        // thay đổi vị trí paddle dựa trên phím bấm
        boolean left = inputHandler.isKeyDown(KeyEvent.VK_LEFT) ||
                       inputHandler.isKeyDown(KeyEvent.VK_A);
        boolean right = inputHandler.isKeyDown(KeyEvent.VK_RIGHT) ||
                        inputHandler.isKeyDown(KeyEvent.VK_D);
        if (left) {
            moveLeft();
        } else if (right) {
            moveRight();
        }
    }

    @Override public void move() {}
    @Override public void update() {}
    @Override public void render(Graphics g) {

        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(this.x, this.y, this.width, this.height);
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}