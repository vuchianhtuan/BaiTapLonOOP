package com.mygame.arkanoid.effects;

import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.Color;
import java.awt.Graphics;

public class Shard {
    private double x, y; // Vị trí (dùng double để tính toán chính xác hơn)
    private double dx, dy; // Vận tốc
    private final Color color;
    private int lifetime; // Tuổi thọ theo frame
    private final int size = 4; // Kích thước của mảnh vụn

    private static final double GRAVITY = 0.2; // Gia tốc trọng trường
    private static final double DRAG = 0.98; // Lực cản không khí

    public Shard(int startX, int startY, Color c) {
        this.x = startX;
        this.y = startY;
        this.color = c;
        this.lifetime = 30;

        // 1. Thiết lập góc ngẫu nhiên (vẫn giữ 0 đến 360 độ)
        double angle = Math.random() * 2 * Math.PI;
        double speed = 2.0 + Math.random() * 3.0;

        // 2. TÍNH VẬN TỐC NGANG (DX) - Vẫn có thể sang trái hoặc phải
        this.dx = speed * Math.cos(angle);

        // 3. TÍNH VẬN TỐC DỌC (DY) - BẮT BUỘC HƯỚNG XUỐNG (sử dụng Math.abs)
        // Math.sin(angle) có thể âm (hướng lên) hoặc dương (hướng xuống).
        // Math.abs() đảm bảo dy luôn dương (hướng xuống) hoặc bằng 0.
        this.dy = Math.abs(speed * Math.sin(angle));

        // Tùy chọn: Thêm một lực đẩy xuống ban đầu để rơi nhanh hơn
        this.dy += 1.0;
    }
    public void update() {
        // Áp dụng trọng lực
        dy += GRAVITY;

        // Áp dụng lực cản/ma sát
        dx *= DRAG;
        dy *= DRAG;

        // Cập nhật vị trí
        x += dx;
        y += dy;

        // Giảm tuổi thọ
        lifetime--;
    }

    public boolean isAlive() {
        return lifetime > 0;
    }

    public void render(Graphics g, ScalingManager sm) {
        // Chuyển từ hệ tọa độ game sang hệ tọa độ màn hình
        int screenX = sm.scaleX((int) x);
        int screenY = sm.scaleY((int) y);
        int scaledSize = sm.scaleWidth(size);

        g.setColor(color);
        // Vẽ mảnh vụn là một hình vuông nhỏ
        g.fillRect(screenX, screenY, scaledSize, scaledSize);
    }
}