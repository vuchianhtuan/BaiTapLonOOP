package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Lớp MovingBrick đại diện cho một viên gạch di chuyển ngang trong trò chơi Arkanoid.
 */
public class MovingBrick extends Brick {
    private int speedX;
    private int originalX;
    private int moveRange;

    public MovingBrick(int x, int y, int width, int height, int speedX, int moveRange) {
        super(x, y, width, height, 2, "moving", "normalBrick");
        this.speedX = speedX;
        this.originalX = x;
        this.moveRange = moveRange;
    }

    // cập nhật vị trí sau mỗi loop
    @Override public void update() {}

    /**
     * Cập nhật vị trí của gạch di chuyển và xử lý va chạm.
     * @param brickList
     */
    public void update(List<Brick> brickList) {
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        this.x += this.speedX;

        // Kiểm tra nếu chạm tường bên trái hoặc bên phải, đổi hướng
        if (this.x <= 0 || this.x + this.width >= gameAreaWidth) {
            this.speedX = -this.speedX;
            this.x += this.speedX; // Điều chỉnh vị trí sau khi đổi hướng
        }

        // Kiểm tra nếu vượt quá phạm vi di chuyển, đổi hướng
        if (Math.abs(this.x - this.originalX) >= this.moveRange) {
            this.speedX = -this.speedX;
            this.x += this.speedX; // Điều chỉnh vị trí sau khi đổi hướng
        }

        // Kiểm tra va chạm với các viên gạch khác và đảo chiều nếu cần
        for (Brick other : brickList) {
            if (other != this && this.x < other.getX() + other.getWidth() && this.x + this.width > other.getX() && this.y == other.getY()){
                this.speedX = -this.speedX;
                this.x += this.speedX; // Điều chỉnh vị trí sau khi đổi hướng
                break;
            }
        }
    }

    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        }
    }
}
