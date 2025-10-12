package com.mygame.arkanoid.objects.bricks;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class StrongBrick extends Brick {

    @Override public void update() {
        if (hitPoints == 2) {
            this.imageName = "strongBrick1";
        }
        if (hitPoints == 1) {
            this.imageName = "strongBrick2";
        }

    }
    @Override public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        }

    }

    public StrongBrick(int x, int y, int width, int height) {
        super(x, y, width, height, 3, "StrongBrick", "strongBrick");
    }

}
