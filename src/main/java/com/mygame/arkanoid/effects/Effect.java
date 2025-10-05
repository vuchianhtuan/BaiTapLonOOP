package com.mygame.arkanoid.effects;

import com.mygame.arkanoid.objects.*;

import java.awt.*;

/*
public abstract class Effect {
    protected int duration;
    public abstract void update();
    public abstract void render();
    public boolean isFinished() { return false; }
}
*/

/**
 * Quản lý logic và thời gian của một hiệu ứng đang hoạt động.
 * Được tạo ra khi một PowerUp được thu thập.
 */
public abstract class Effect {
    protected int duration; // Thời gian còn lại (tính bằng tick/frame)
    protected Paddle target; // Đối tượng chịu tác động

    public Effect(Paddle target, int durationInTicks) {
        this.target = target;
        this.duration = durationInTicks;
    }

    /**
     * Cập nhật bộ đếm thời gian.
     * GameManager sẽ gọi phương thức này mỗi frame.
     */
    public void update() {
        if (duration > 0) {
            duration--;
        }
    }

    /**
     * Kiểm tra xem hiệu ứng đã hết hạn chưa.
     * @return true nếu thời gian đã hết.
     */
    public boolean isFinished() {
        return duration <= 0;
    }

    /**
     * Áp dụng hiệu ứng lên mục tiêu.
     * Được gọi một lần ngay khi hiệu ứng được tạo ra.
     */
    public abstract void apply();

    /**
     * Gỡ bỏ hiệu ứng khỏi mục tiêu.
     * Được gọi một lần khi hiệu ứng kết thúc.
     */
    public abstract void remove();

    public abstract void render(Graphics g);

    // Phương thức render có thể dùng để vẽ các chỉ báo đặc biệt,
    // ví dụ như làm cho paddle nhấp nháy.
    // @Override public abstract void render();
}