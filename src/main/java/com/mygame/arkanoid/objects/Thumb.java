package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.engine.AssetManager;
// Xóa các import không cần thiết (InputHandler, SoundManager)
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Rectangle; // <-- THÊM IMPORT NÀY

public class Thumb extends GameObject {

    // Sửa cảnh báo "may be final" bằng cách thêm 'final'
    private final String imageName;

    // <-- THÊM BIẾN 'bounds' VÀO ĐÂY
    private Rectangle bounds;

    public Thumb(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.imageName = "thumb";

        // <-- KHỞI TẠO 'bounds' KHI TẠO THUMB
        this.bounds = new Rectangle(x, y, width, height);
    }

    @Override
    public void update() {
        // SettingManager sẽ điều khiển Thumb
    }

    // --- BỔ SUNG: Hàm setX() ---
    // SettingManager cần hàm này để ra lệnh cho Thumb di chuyển
    public void setX(int x) {
        this.x = x; // Cập nhật tọa độ x (thừa hưởng từ GameObject)

        // Giờ đây this.bounds đã tồn tại và sẽ được cập nhật
        if (this.bounds != null) {
            this.bounds.x = x;
        }
    }

    // --- BỔ SUNG: Hàm getBounds() ---
    // SettingManager cần hàm này để kiểm tra xem chuột có click vào Thumb không
    public Rectangle getBounds() {
        return this.bounds; // Giờ đây this.bounds đã tồn tại
    }


    @Override
    public void render(Graphics g, ScalingManager sm) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);

        // Vẽ bằng this.x (thay vì Tx cũ)
        if (img != null) {
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }
}