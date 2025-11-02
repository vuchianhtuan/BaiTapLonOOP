package com.mygame.arkanoid.ui.controls;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.GameObject;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.image.BufferedImage;

// THANH TRACK CỦA THANH TRƯỢT
public class Track extends GameObject {
    String imageName;

    public Track(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.imageName = "track";
    }

    @Override
    public void update() {
        // Cập nhật logic của Track nếu cần thiết

    }

    @Override
    public void render(Graphics g, ScalingManager sm) {

        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}
