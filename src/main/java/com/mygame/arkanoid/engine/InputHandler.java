package com.mygame.arkanoid.engine;

import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.event.*;

public class InputHandler implements KeyListener, MouseMotionListener, MouseListener {
    private final boolean[] keys = new boolean[256];
    private int mouseX, mouseY;
    private boolean mouseClicked = false;
    private boolean isCurrentlyPressed = false;

    public InputHandler() {
        this.mouseX = 0;
        this.mouseY = 0;
    }

    public boolean isKeyDown(int keyCode) {
        if (keyCode >= 0 && keyCode < keys.length) {
            return keys[keyCode];
        }
        return false;
    }

    public int getMouseX() { return mouseX; }

    public int getMouseY() { return mouseY; }

    public boolean isMouseClicked() {
        if (mouseClicked) {
            mouseClicked = false; // Reset lại ngay sau khi kiểm tra
            return true;
        }
        return false;
    }

    public boolean isMousePressed() {
        return isCurrentlyPressed;
    }

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
        this.mouseY = e.getY();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Đánh dấu là chuột vừa được click khi nhấn xuống
        if (e.getButton() == MouseEvent.BUTTON1) { // Chỉ xử lý chuột trái
            this.mouseClicked = true;
            this.isCurrentlyPressed = true;
        }
    }

    public int getVirtualMouseX() {
        return ScalingManager.getInstance().unscaleX(this.mouseX);
    }

    public int getVirtualMouseY() {
        return ScalingManager.getInstance().unscaleY(this.mouseY);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.isCurrentlyPressed = false; // Đánh dấu đã thả chuột
        }
    }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}