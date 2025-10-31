package com.mygame.arkanoid.objects.bricks;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.helper.ScalingManager;

public class NormalBrick extends Brick {
    @Override public void update() {}
    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        }
    }

    public NormalBrick(int x, int y, int width, int height) {
        // Gạch thường có 1 máu, loại "normal"
        super(x, y, width, height, 1, "normal", "normalBrick");
    }
}