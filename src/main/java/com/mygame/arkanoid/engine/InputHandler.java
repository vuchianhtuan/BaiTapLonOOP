package com.mygame.arkanoid.engine;

import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.event.*;


/**
 * Xử lý tất cả các sự kiện đầu vào từ bàn phím và chuột.
 */
public class InputHandler implements KeyListener, MouseMotionListener, MouseListener {
    private final boolean[] keys = new boolean[256];
    private int mouseX, mouseY;
    private boolean mouseClicked = false;
    private boolean isCurrentlyPressed = false;

    /**
     * Khởi tạo InputHandler với trạng thái ban đầu.
     */
    public InputHandler() {
        this.mouseX = 0;
        this.mouseY = 0;
    }

    /**
     * Kiểm tra xem phím có đang được nhấn không.
     * @return true nếu phím đang được nhấn, false nếu không.
     */
    public boolean isKeyDown(int keyCode) {
        if (keyCode >= 0 && keyCode < keys.length) {
            return keys[keyCode];
        }
        return false;
    }

    public int getMouseX() { return mouseX; }

    public int getMouseY() { return mouseY; }

    /**
     * Kiểm tra xem chuột có vừa được click không.
     * @return true nếu chuột vừa được click, false nếu không.
     */
    public boolean isMouseClicked() {
        if (mouseClicked) {
            mouseClicked = false; // Reset lại ngay sau khi kiểm tra
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra xem chuột có đang được nhấn không.
     * @return true nếu chuột đang được nhấn, false nếu không.
     */
    public boolean isMousePressed() {
        return isCurrentlyPressed;
    }

    /**
     * Xử lý sự kiện khi một phím được nhấn.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = true; // Đánh dấu là đang nhấn
        }
    }

    /**
     * Xử lý sự kiện khi một phím được thả.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = false; // Đánh dấu là đã thả
        }
    }

    /**
     * Xử lý sự kiện khi chuột di chuyển.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX(); // Cập nhật vị trí chuột
        this.mouseY = e.getY();
    }

    /**
     * Xử lý sự kiện khi chuột được nhấn.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // Đánh dấu là chuột vừa được click khi nhấn xuống
        if (e.getButton() == MouseEvent.BUTTON1) { // Chỉ xử lý chuột trái
            this.mouseClicked = true;
            this.isCurrentlyPressed = true;
        }
    }

    /**
     * Lấy tọa độ chuột ảo (đã qua scaling).
     * @return Tọa độ X và Y của chuột đã được unscale.
     */
    public int getVirtualMouseX() {
        return ScalingManager.getInstance().unscaleX(this.mouseX);
    }
    public int getVirtualMouseY() {
        return ScalingManager.getInstance().unscaleY(this.mouseY);
    }


    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) {}

    /**
     * Xử lý sự kiện khi chuột được thả.
     * @param e the event to be processed
     */
    @Override public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.isCurrentlyPressed = false; // Đánh dấu đã thả chuột
        }
    }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}