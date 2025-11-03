package com.mygame.arkanoid.objects.bricks;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Lớp StrongBrick đại diện cho một viên gạch cứng trong trò chơi Arkanoid.
 */
public class StrongBrick extends Brick {

    @Override public void update() {
        if (hitPoints == 2) {
            this.imageName = "strongBrick1";
        } else if (hitPoints == 1) {
            this.imageName = "strongBrick2";
        } else if (hitPoints >= 3) {
            this.imageName = "strongBrick";
        }
    }

    /**
     * Lấy tên hình ảnh ban đầu dựa trên số máu.
     * @param health số máu ban đầu của gạch.
     * @return tên hình ảnh ban đầu.
     */
    private static String getInitialImageName(int health) {
        if (health == 2) {
            return "strongBrick1"; // Gạch 2 HP bắt đầu với ảnh 1
        }
        if (health == 1) {
            return "strongBrick2"; // Gạch 1 HP bắt đầu với ảnh 2
        }
        // Mặc định (cho 3 HP hoặc nhiều hơn) là ảnh "strongBrick"
        return "strongBrick";
    }

    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        }
    }

    public StrongBrick(int x, int y, int width, int height, int health) {
        super(x, y, width, height, health, "StrongBrick", getInitialImageName(health));
    }

}
