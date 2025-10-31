package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import com.mygame.arkanoid.systems.helper.ScalingManager;

public class Paddle extends MovableObject {
    private int speed = 15;
    private PowerUp currentPowerUp;
    private String imageName;
    private final int originalWidth;
    private boolean isSticky = false;

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean sticky) {
        this.isSticky = sticky;
    }

    public void moveLeft() {
        x -= speed;
        if (x < 0) x = 0;
    }
    public void moveRight() {
        x += speed;
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        if (x + width > gameAreaWidth) x = gameAreaWidth - width;
    }
    public void applyPowerUp(PowerUp powerUp) {}

    public Paddle(int x, int y, int width, int height, String skinKey) {
        super(x, y, width, height);
        this.imageName = skinKey;
        this.originalWidth = width;
    }

    public void expand(int amount) {
        // Tăng chiều rộng và điều chỉnh lại vị trí x để nó mở rộng đều 2 bên
        this.x -= amount / 2;
        this.width += amount;
    }

    public void resetWidth() {
        // Điều chỉnh lại vị trí x trước khi thu hẹp
        this.x += (this.width - this.originalWidth) / 2;
        this.width = this.originalWidth;
    }

    // Cập nhật vị trí paddle dựa trên InputHandler
    public void update(InputHandler inputHandler) {
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
    @Override public void render(Graphics g, ScalingManager sm) {

        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}