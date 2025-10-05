package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Paddle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class ExpandPaddlePowerUp extends PowerUp {
    //@Override public void applyEffect(Paddle paddle) {}
    //@Override public void removeEffect(Paddle paddle) {}
    private String imageName;
    @Override public void update() {
        this.y += fallSpeed;
    }
    //@Override public void render() {}

    public ExpandPaddlePowerUp(int x, int y, int width, int height) {
        super(x, y, width, height, "expand", 600);
        this.imageName = "expandPowerUp";
    }

    @Override
    public void applyEffect(Paddle paddle) {
        System.out.println("PowerUp: Mở rộng Paddle đã được áp dụng!");
    }

    @Override
    public void removeEffect(Paddle paddle) {
        System.out.println("PowerUp: Hiệu ứng mở rộng Paddle đã hết!");
    }

    // PowerUp kế thừa từ GameObject nên cũng cần render
    @Override
    public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(this.x, this.y, this.width, this.height);
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
