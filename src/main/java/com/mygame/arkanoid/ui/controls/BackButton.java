package com.mygame.arkanoid.ui.controls;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Lớp đại diện cho một nút "Quay lại" (BackButton) chung trong giao diện người dùng (UI).
 * <p>
 * Lớp này đóng gói vị trí và kích thước (dưới dạng {@link Rectangle})
 * trong tọa độ logic (thế giới game), logic để vẽ ({@link #draw})
 * và logic để kiểm tra va chạm với chuột ({@link #contains}).
 */
public class BackButton {
    /**
     * Vùng (bounds) hình chữ nhật của nút, lưu trữ bằng tọa độ logic (thế giới game).
     */
    private Rectangle bounds;
    private String imageName = "Back";

    /**
     * Khởi tạo nút "Quay lại" với vị trí và kích thước logic.
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public BackButton(int x, int y, int width, int height) {
        bounds = new Rectangle(x, y, width, height);
    }

    /**
     * Vẽ (render) nút "Quay lại" lên màn hình.
     * <p>
     * Lấy hình ảnh (asset) "Back" từ {@link AssetManager} và vẽ nó
     * tại vị trí {@code bounds} đã được co giãn (scale)
     * bởi {@link ScalingManager}.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    public void draw(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(imageName);
        if (img != null) {
            // Vẽ ảnh đã được co giãn (scale)
            g.drawImage(img,
                    sm.scaleX(bounds.x), sm.scaleY(bounds.y),
                    sm.scaleWidth(bounds.width), sm.scaleHeight(bounds.height), null);
        } else {
            // Fallback: Vẽ hình chữ nhật màu xanh nếu ảnh bị lỗi
            g.setColor(Color.BLUE);
            g.fillRect(sm.scaleX(10), sm.scaleY(10),
                    sm.scaleWidth(40), sm.scaleHeight(40));
        }
    }

    /**
     * Kiểm tra xem một điểm (thường là tọa độ chuột) có nằm
     * bên trong vùng {@code bounds} (logic) của nút hay không.
     * <p>
     * <b>Quan trọng:</b> Tọa độ (mx, my) được truyền vào
     * <b>phải</b> là tọa độ logic (ảo), đã được
     * {@link com.mygame.arkanoid.engine.InputHandler}
     * chuyển đổi (unscale) từ tọa độ màn hình.
     *
     * @param mx Tọa độ X (logic) của con trỏ chuột.
     * @param my Tọa độ Y (logic) của con trỏ chuột.
     * @return true nếu tọa độ (mx, my) nằm trong vùng {@code bounds} của nút.
     */
    public boolean contains(int mx, int my) {
        return bounds.contains(mx, my);
    }
}