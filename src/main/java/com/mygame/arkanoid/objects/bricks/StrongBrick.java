package com.mygame.arkanoid.objects.bricks;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;
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
    @Override public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        }
    }

    public StrongBrick(int x, int y, int width, int height, int health) {
        super(x, y, width, height, health, "StrongBrick", "strongBrick");
    }

}
