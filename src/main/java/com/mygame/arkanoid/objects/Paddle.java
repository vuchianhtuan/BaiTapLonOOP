package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.config.GameConstants;

/**
 * Lớp Paddle đại diện cho thanh điều khiển trong trò chơi Arkanoid.
 */
public class Paddle extends MovableObject {
    private int speed = GameConstants.PADDLE_SPEED;
    private PowerUp currentPowerUp;
    private String imageName;
    private final int originalWidth;
    private boolean isSticky = false;

    /**
     * Kiểm tra xem paddle có tính năng dính bóng không.
     * @return true nếu paddle có tính năng dính bóng, false nếu không.
     */
    public boolean isSticky() {
        return isSticky;
    }

    /**
     * Đặt tính năng dính bóng cho paddle.
     */
    public void setSticky(boolean sticky) {
        this.isSticky = sticky;
    }

    /**
     * Di chuyển paddle sang trái.
     */
    public void moveLeft() {
        x -= speed;
        if (x < 0) x = 0;
    }

    /**
     * Di chuyển paddle sang phải.
     */
    public void moveRight() {
        x += speed;
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        if (x + width > gameAreaWidth) x = gameAreaWidth - width;
    }

    /**
     * Khởi tạo một paddle tại vị trí (x, y) với kích thước và skin cụ thể.
     */
    public Paddle(int x, int y, int width, int height, String skinKey) {
        super(x, y, width, height);
        this.imageName = skinKey;
        this.originalWidth = width;
    }

    /**
     * Mở rộng chiều rộng paddle.
     */
    public void expand(int amount) {
        // Tăng chiều rộng và điều chỉnh lại vị trí x để nó mở rộng đều 2 bên
        this.x -= amount / 2;
        this.width += amount;
    }

    /**
     * Thu hẹp chiều rộng paddle về kích thước ban đầu.
     */
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