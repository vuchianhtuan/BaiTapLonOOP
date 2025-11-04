package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.Graphics;
import java.awt.Graphics2D;      // <-- THÊM
import java.awt.AlphaComposite; // <-- THÊM
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.Math;          // <-- THÊM

/**
 * Lớp đại diện cho vật phẩm tăng sức mạnh mở rộng thanh đỡ (paddle).
 * Khi người chơi thu thập vật phẩm này, chiều rộng của thanh đỡ sẽ được
 * mở rộng trong một khoảng thời gian nhất định.
 */
public class ExpandPaddlePowerUp extends PowerUp {
    private String imageName;
    private static final int EXPAND_AMOUNT = 50;

    private float fadeTimer = 0;
    private float currentAlpha = 1.0f;
    private static final float FADE_SPEED = 0.03f; // Tốc độ mờ/rõ (điều chỉnh để mượt hơn)

    @Override public void update() {
        this.y += fallSpeed;

        this.fadeTimer += FADE_SPEED;

        // Math.abs(sin(x)) dao động từ 0 (mờ nhất) đến 1 (rõ nhất)
        this.currentAlpha = Math.abs((float)Math.sin(this.fadeTimer));
    }

    public ExpandPaddlePowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "expand", 600);
        this.imageName = "expandPowerUp";
    }

    /**
     * ÁP DỤNG HIỆU ỨNG: Mở rộng chiều rộng paddle.
     */
    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.getPaddle().resetWidth();
        gameManager.getPaddle().expand(EXPAND_AMOUNT);
    }

    @Override
    public void removeEffect(GameManager gameManager) {
        gameManager.getPaddle().resetWidth();
    }

    @Override
    public void render(Graphics g, ScalingManager sm) {
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.currentAlpha));

        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);

        int scaledX = sm.scaleX(this.x);
        int scaledY = sm.scaleY(this.y);
        int scaledWidth = sm.scaleWidth(this.width);
        int scaledHeight = sm.scaleHeight(this.height);

        if (img != null) {
            g2d.drawImage(img, scaledX, scaledY, scaledWidth, scaledHeight, null);
        } else {
            g2d.setColor(Color.GREEN);
            g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
        }

        g2d.dispose(); // Khôi phục trạng thái Graphics
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}