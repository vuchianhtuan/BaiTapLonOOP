package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.objects.GameObject;
import java.awt.Graphics;
/*
public abstract class Brick extends GameObject {
    protected int hitPoints;
    protected String type;

    public abstract void takeHit();
    public abstract boolean isDestroyed();
}
 */

public abstract class Brick extends GameObject {
    protected int hitPoints;
    protected String type;
    protected String imageName;

    public Brick(int x, int y, int width, int height, int hitPoints, String type, String imageName) {
        super(x, y, width, height); // Gọi constructor của lớp cha GameObject
        this.hitPoints = hitPoints;
        this.type = type;
        this.imageName = imageName;
    }

    public void takeHit() {
        if (this.hitPoints > 0) {
            this.hitPoints--;
        }
    }

    public boolean isDestroyed() {
        return this.hitPoints <= 0;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public String getType() {
        return type;
    }

    @Override
    public abstract void update();

    @Override
    public abstract void render(Graphics g);
}