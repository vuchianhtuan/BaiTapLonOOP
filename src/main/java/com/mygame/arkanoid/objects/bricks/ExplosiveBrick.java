package com.mygame.arkanoid.objects.bricks;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ExplosiveBrick extends Brick {
    public ExplosiveBrick(int x, int y, int width, int height) {
        super(x, y, width, height, 1, "Explosive", "explosiveBrick");
    }

    //@Override public void takeHit() {}
    //@Override public boolean isDestroyed() { return false; }
    @Override public void update() {}
    @Override public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(this.x, this.y, this.width, this.height);
            g.setColor(Color.BLACK);
            g.drawRect(this.x, this.y, this.width, this.height);
        }
    }
}

