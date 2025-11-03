package com.mygame.arkanoid.ui.controls;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Lớp BackButton đại diện cho một nút "Quay lại" trong giao diện người dùng.
 */
public class BackButton {
    private Rectangle bounds;
    private String imageName = "Back";

    public BackButton(int x, int y, int width, int height) {
        bounds = new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(bounds.x), sm.scaleY(bounds.y),
                    sm.scaleWidth(bounds.width), sm.scaleHeight(bounds.height), null);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(sm.scaleX(10), sm.scaleY(10),
                    sm.scaleWidth(40), sm.scaleHeight(40));
        }
    }

    /**
     * KIỂM TRA NÚT CÓ ĐƯỢC NHẤN HAY KHÔNG.
     * @param mx tọa độ x của con trỏ chuột.
     * @param my tọa độ x, y của con trỏ chuột.
     * @return true nếu tọa độ (mx, my) nằm trong vùng nút, ngược lại false.
     */
    public boolean contains(int mx, int my) {
        return bounds.contains(mx, my);
    }
}
