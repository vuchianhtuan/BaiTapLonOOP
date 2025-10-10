package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.objects.GameObject;
import com.mygame.arkanoid.core.GameManager;
import java.awt.Graphics;

public abstract class PowerUp extends GameObject {
    protected String type;
    protected int duration; // Thời gian hiệu lực (tính bằng tick/frame)
    protected int fallSpeed = 2; // Tốc độ rơi

    public PowerUp(int x, int y, int width, int height, String type, int duration) {
        super(x, y, width, height);
        this.type = type;
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void tick() {
        if (duration > 0) {
            duration--;
        }
    }

    // Thêm phương thức để kiểm tra hết hạn
    public boolean isExpired() {
        return duration <= 0;
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
    public abstract void applyEffect(GameManager gameManager);

    /**
     * Gỡ bỏ hiệu ứng khỏi Paddle. Sẽ được implement bởi lớp con.
     */
    public abstract void removeEffect(GameManager gameManager);
}
