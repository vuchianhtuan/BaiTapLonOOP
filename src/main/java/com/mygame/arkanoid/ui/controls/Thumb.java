package com.mygame.arkanoid.ui.controls;

import com.mygame.arkanoid.engine.AssetManager;
// Xóa các import không cần thiết (InputHandler, SoundManager)
import com.mygame.arkanoid.objects.GameObject;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Rectangle; // <-- THÊM IMPORT NÀY

/**
 * Đại diện cho "tay cầm" (handle) hay "nút kéo" (Thumb)
 * của một thanh trượt (Slider) trong giao diện người dùng (UI).
 * <p>
 * Lớp này kế thừa từ {@link GameObject} và quản lý một đối tượng
 * {@link Rectangle} (bounds) nội bộ để theo dõi vị trí va chạm
 * (hit-testing) của nó một cách hiệu quả.
 */
public class Thumb extends GameObject {

    private final String imageName;
    /** Vùng va chạm (bounding box) nội bộ, được cập nhật bởi {@link #setX(int)}. */
    private Rectangle bounds;

    /**
     * Khởi tạo một Thumb (nút kéo) mới.
     *
     * @param x Vị trí X (logic) ban đầu.
     * @param y Vị trí Y (logic) ban đầu.
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public Thumb(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.imageName = "thumb";

        // <-- KHỞI TẠO 'bounds' KHI TẠO THUMB
        // Khởi tạo vùng bounds nội bộ để quản lý
        this.bounds = new Rectangle(x, y, width, height);
    }

    /**
     * (Bỏ trống) Phương thức cập nhật (update) logic.
     * Thumb là một đối tượng bị động (passive); vị trí của nó
     * được cập nhật từ bên ngoài (ví dụ: bởi một lớp Slider)
     * thông qua {@link #setX(int)}.
     */
    @Override
    public void update() {}

    /**
     * Cập nhật vị trí X (logic) của Thumb.
     * <p>
     * Ghi đè (override) phương thức {@code setX} của {@link GameObject}
     * để đảm bảo rằng cả {@code this.x} (dùng để vẽ) và
     * {@code this.bounds.x} (dùng để kiểm tra va chạm)
     * đều được đồng bộ hóa.
     *
     * @param x tọa độ x (logic) mới.
     */
    @Override // Ghi đè phương thức setX() của GameObject
    public void setX(int x) {
        this.x = x; // Cập nhật tọa độ x (thừa hưởng từ GameObject)

        // Đồng bộ hóa vị trí X của vùng bounds nội bộ
        if (this.bounds != null) {
            this.bounds.x = x;
        }
    }

    /**
     * Lấy vùng va chạm (bounding box) nội bộ của Thumb.
     * <p>
     * Ghi đè (override) phương thức {@code getBounds} của {@link GameObject}
     * để trả về đối tượng {@code Rectangle} nội bộ (đã được cache)
     * thay vì tạo một đối tượng mới mỗi lần gọi.
     * <p>
     * Được sử dụng để kiểm tra va chạm chuột (hit-testing).
     *
     * @return Đối tượng {@link Rectangle} nội bộ.
     */
    @Override // Ghi đè phương thức getBounds() của GameObject
    public Rectangle getBounds() {
        return this.bounds; // Trả về đối tượng bounds đã được quản lý
    }


    /**
     * Vẽ (render) Thumb lên màn hình.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override
    public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);

        // Vẽ bằng this.x (vị trí đã được cập nhật)
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            // Fallback: Vẽ hình chữ nhật màu xanh nếu ảnh bị lỗi
            g.setColor(Color.BLUE);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}