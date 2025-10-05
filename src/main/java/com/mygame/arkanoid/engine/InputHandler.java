package com.mygame.arkanoid.engine;

/*
public class InputHandler {
    public void handleKeyboardInput() {}
    public void handleMouseInput() {}
}
*/

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Xử lý tất cả đầu vào từ bàn phím và chuột.
 * Lớp này cần được đăng ký với GamePanel (ví dụ: panel.addKeyListener(this)) để nhận sự kiện.
 */
public class InputHandler implements KeyListener, MouseMotionListener {

    private final boolean[] keys = new boolean[256]; // Đủ để chứa hầu hết các mã phím
    private int mouseX;

    public InputHandler() {
        this.mouseX = 0;
    }

    // --- CÁC PHƯƠNG THỨC CÔNG KHAI ĐỂ GAME MANAGER TRUY VẤN ---

    /**
     * Kiểm tra xem một phím có đang được nhấn hay không.
     * @param keyCode Mã phím, ví dụ: KeyEvent.VK_SPACE
     * @return true nếu phím đang được nhấn, ngược lại false.
     */
    public boolean isKeyDown(int keyCode) {
        if (keyCode >= 0 && keyCode < keys.length) {
            return keys[keyCode];
        }
        return false;
    }

    /**
     * Lấy vị trí X hiện tại của con trỏ chuột.
     * @return Tọa độ X của chuột.
     */
    public int getMouseX() {
        return mouseX;
    }

    // --- CÁC PHƯƠNG THỨC ĐƯỢC GỌI TỰ ĐỘNG BỞI HỆ THỐNG SWING/AWT ---

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = true; // Đánh dấu là đang nhấn
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = false; // Đánh dấu là đã thả
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX(); // Cập nhật vị trí chuột
    }

    // Các phương thức không dùng đến nhưng bắt buộc phải có
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
}