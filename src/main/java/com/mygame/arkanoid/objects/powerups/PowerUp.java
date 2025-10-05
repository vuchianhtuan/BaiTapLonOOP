package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.objects.GameObject;
import com.mygame.arkanoid.objects.Paddle;
import java.awt.Graphics;
/*
public abstract class PowerUp extends GameObject {
    protected String type;
    protected int duration;

    public abstract void applyEffect(Paddle paddle);
    public abstract void removeEffect(Paddle paddle);
}
*/

public abstract class PowerUp extends GameObject {
    protected String type;
    protected int duration; // Thời gian hiệu lực (tính bằng tick/frame)
    protected int fallSpeed = 2; // Tốc độ rơi

    public PowerUp(int x, int y, int width, int height, String type, int duration) {
        super(x, y, width, height);
        this.type = type;
        this.duration = duration;
    }

    @Override
    public void update() {
        this.y += fallSpeed;
    }

    @Override
    public abstract void render(Graphics g);

    /**
     * Áp dụng hiệu ứng lên Paddle. Sẽ được implement bởi lớp con.
     */
    public abstract void applyEffect(Paddle paddle);

    /**
     * Gỡ bỏ hiệu ứng khỏi Paddle. Sẽ được implement bởi lớp con.
     */
    public abstract void removeEffect(Paddle paddle);
}
