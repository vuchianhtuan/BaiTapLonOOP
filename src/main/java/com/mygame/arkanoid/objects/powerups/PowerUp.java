package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.objects.GameObject;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.Graphics;

/**
 * Lớp trừu tượng (abstract) cơ sở cho tất cả các vật phẩm tăng sức mạnh (PowerUp).
 * <p>
 * Lớp này định nghĩa các hành vi cốt lõi:
 * <ol>
 * <li>Là một {@link GameObject} có thể rơi xuống (thông qua {@link #update()}).</li>
 * <li>Có một thời gian hiệu lực ({@code duration}) sau khi được kích hoạt
 * (được đếm ngược bởi {@link #tick()}).</li>
 * <li>Định nghĩa các phương thức trừu tượng {@link #applyEffect} và
 * {@link #removeEffect} mà các lớp con phải triển khai (implement).</li>
 * </ol>
 */
public abstract class PowerUp extends GameObject {
    /** Mã định danh loại power-up (ví dụ: "expand", "fast_ball"). */
    protected String type;
    /**
     * Thời gian hiệu lực (tính bằng frame) CỦA HIỆU ỨNG
     * sau khi được kích hoạt (không phải thời gian vật phẩm rơi).
     */
    protected int duration;
    /** Tốc độ rơi (pixel/frame) của vật phẩm khi đang rơi. */
    protected int fallSpeed = 2;

    /**
     * Khởi tạo một PowerUp cơ bản.
     *
     * @param x Vị trí X (logic) ban đầu (thường là từ gạch vỡ).
     * @param y Vị trí Y (logic) ban đầu.
     * @param width Chiều rộng (logic) của vật phẩm.
     * @param height Chiều cao (logic) của vật phẩm.
     * @param type Mã định danh loại (ví dụ: "expand").
     * @param duration Thời gian hiệu lực (frame) của hiệu ứng sau khi kích hoạt.
     */
    public PowerUp(int x, int y, int width, int height, String type, int duration) {
        super(x, y, width, height);
        this.type = type;
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    /**
     * Cập nhật (đếm ngược) thời gian hiệu lực của hiệu ứng.
     * <p>
     * Phương thức này được gọi bởi {@code GameManager} mỗi frame
     * <b>SAU KHI</b> power-up đã được kích hoạt và đang có hiệu lực.
     * Nó không liên quan đến phương thức {@code update()} (dùng cho vật phẩm rơi).
     */
    public void tick() {
        if (duration > 0) {
            duration--;
        }
    }

    /**
     * Kiểm tra xem thời gian hiệu lực của hiệu ứng đã hết hay chưa.
     * @return true nếu {@code duration <= 0}, false nếu còn.
     */
    public boolean isExpired() {
        return duration <= 0;
    }

    /**
     * Cập nhật vị trí của vật phẩm (khi đang rơi).
     * Được gọi bởi {@code EntityManager} mỗi frame <b>TRƯỚC KHI</b> được thu thập.
     */
    @Override
    public void update() {
        this.y += fallSpeed;
    }

    /**
     * Vẽ (render) vật phẩm lên màn hình (khi đang rơi).
     * Phải được triển khai (implement) bởi lớp con.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override
    public abstract void render(Graphics g, ScalingManager sm);

    /**
     * Áp dụng hiệu ứng của PowerUp lên trò chơi.
     * <p>
     * Được gọi bởi {@code GameManager} khi paddle va chạm với vật phẩm này.
     *
     * @param gameManager Tham chiếu đến GameManager để thay đổi trạng thái (ví dụ: paddle, ball).
     */
    public abstract void applyEffect(GameManager gameManager);

    /**
     * Gỡ bỏ (hoàn tác) hiệu ứng của PowerUp.
     * <p>
     * Được gọi bởi {@code GameManager} khi {@link #isExpired()} trả về true.
     *
     * @param gameManager Tham chiếu đến GameManager để hoàn tác thay đổi.
     */
    public abstract void removeEffect(GameManager gameManager);
}