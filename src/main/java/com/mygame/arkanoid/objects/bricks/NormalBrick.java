package com.mygame.arkanoid.objects.bricks;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import com.mygame.arkanoid.engine.AssetManager;

public class NormalBrick extends Brick {
    @Override public void update() {}
    @Override public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        }
    }

    public NormalBrick(int x, int y, int width, int height) {
        // Gạch thường có 1 máu, loại "normal"
        super(x, y, width, height, 1, "normal", "normalBrick");
    }
}