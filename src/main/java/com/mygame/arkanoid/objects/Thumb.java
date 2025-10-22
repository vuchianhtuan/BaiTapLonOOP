package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.SoundManager;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class Thumb extends GameObject {
    public static int Tx = 0;
    private String imageName;

    public Thumb(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.imageName = "thumb";
        Tx = 660;
    }

    @Override
    public void update() {
        // Không sử dụng phương thức này
    }
    public void update(InputHandler inputHandler, SoundManager soundManager, Track Track) {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();
        // Cập nhật vị trí của Thumb dựa trên đầu vào của người dùng
        if (inputHandler.isMousePressed() && virtualMouseY >= this.y && virtualMouseY <= this.y + this.height) {
            int mouseX = inputHandler.getVirtualMouseX();
            // Giới hạn vị trí của Thumb trong phạm vi của Track
            int trackStartX = Track.getX();
            int trackEndX = Track.getX() + Track.getWidth() - this.getWidth();
            if (mouseX < trackStartX) {
                this.setX(trackStartX);
                this.Tx = trackStartX;
            } else if (mouseX > trackEndX) {
                this.setX(trackEndX);
                this.Tx = trackEndX;
            } else {
                this.setX(mouseX);
                this.Tx = mouseX;
            }
            // Cập nhật âm lượng dựa trên vị trí của Thumb
            float volume = (float) (this.getX() - trackStartX) / (trackEndX - trackStartX);
            soundManager.setVolume(volume);
        }
    }

    @Override
    public void render(Graphics g, ScalingManager sm) {

        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.Tx), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(sm.scaleX(this.Tx), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}
