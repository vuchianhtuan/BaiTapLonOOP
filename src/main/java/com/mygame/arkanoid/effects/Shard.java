package com.mygame.arkanoid.effects;

import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Đại diện cho một mảnh vỡ (Shard) vật lý.
 * Đây là một hạt (particle) đơn giản được dùng trong hiệu ứng
 * gạch vỡ (shatter), tuân theo các quy tắc vật lý cơ bản
 * như trọng lực và lực cản không khí.
 */
public class Shard {

    private double x, y; // Vị trí (dùng double để tính toán chính xác hơn)
    private double dx, dy; // Vận tốc (delta x, delta y)
    private final Color color;
    private int lifetime; // Tuổi thọ (tính bằng số frame) trước khi tự hủy
    private final int size = 4; // Kích thước gốc của mảnh vụn (pixel)

    /** Gia tốc trọng trường, kéo mảnh vụn đi xuống mỗi frame. */
    private static final double GRAVITY = 0.2;
    /** Lực cản không khí (ma sát), làm chậm mảnh vụn theo thời gian. */
    private static final double DRAG = 0.98;

    /**
     * Khởi tạo một mảnh vụn mới tại vị trí bắt đầu với một màu sắc cụ thể.
     * Mảnh vụn sẽ ngay lập tức được "bắn" ra theo một hướng ngẫu nhiên
     * nhưng **luôn có xu hướng đi xuống**.
     *
     * @param startX Vị trí X ban đầu (tọa độ game).
     * @param startY Vị trí Y ban đầu (tọa độ game).
     * @param c Màu sắc của mảnh vụn.
     */
    public Shard(int startX, int startY, Color c) {
        this.x = startX;
        this.y = startY;
        this.color = c;
        this.lifetime = 30; // Mảnh vụn sẽ tồn tại trong 30 frame (khoảng 0.5 giây)

        // 1. Chọn một góc ngẫu nhiên (từ 0 đến 360 độ, tính bằng radian)
        double angle = Math.random() * 2 * Math.PI;
        // Chọn một tốc độ ban đầu ngẫu nhiên
        double speed = 2.0 + Math.random() * 3.0;

        // 2. Tính vận tốc ngang (dx): Có thể âm (trái) hoặc dương (phải)
        this.dx = speed * Math.cos(angle);

        // 3. Tính vận tốc dọc (dy): BẮT BUỘC phải dương (hướng xuống)
        // Math.sin(angle) có thể âm (lên) hoặc dương (xuống).
        // Dùng Math.abs() để đảm bảo kết quả luôn dương (luôn đi xuống).
        this.dy = Math.abs(speed * Math.sin(angle));

        // Thêm một lực đẩy nhẹ ban đầu xuống dưới để trông tự nhiên hơn
        this.dy += 1.0;
    }

    /**
     * Cập nhật trạng thái vật lý của mảnh vụn cho mỗi frame.
     * Áp dụng trọng lực và lực cản không khí, sau đó cập nhật vị trí.
     */
    public void update() {
        // Áp dụng trọng lực (tăng vận tốc dy)
        dy += GRAVITY;

        // Áp dụng lực cản (giảm cả hai vận tốc)
        dx *= DRAG;
        dy *= DRAG;

        // Cập nhật vị trí dựa trên vận tốc mới
        x += dx;
        y += dy;

        // Giảm thời gian tồn tại
        lifetime--;
    }

    /**
     * Kiểm tra xem mảnh vụn còn "sống" hay không.
     * @return {@code true} nếu tuổi thọ (lifetime) vẫn còn > 0,
     * {@code false} nếu mảnh vụn đã hết hạn và cần bị xóa.
     */
    public boolean isAlive() {
        return lifetime > 0;
    }

    /**
     * Vẽ mảnh vụn lên màn hình.
     * Phương thức này chuyển đổi tọa độ logic (game) sang tọa độ
     * vật lý (màn hình) bằng ScalingManager.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager) để tính toán tọa độ.
     */
    public void render(Graphics g, ScalingManager sm) {
        // Chuyển từ hệ tọa độ game (ví dụ: x=100) sang hệ tọa độ màn hình (ví dụ: x=150)
        int screenX = sm.scaleX((int) x);
        int screenY = sm.scaleY((int) y);
        // Co giãn kích thước của mảnh vụn
        int scaledSize = sm.scaleWidth(size);

        g.setColor(color);
        // Vẽ mảnh vụn là một hình vuông nhỏ
        g.fillRect(screenX, screenY, scaledSize, scaledSize);
    }
}