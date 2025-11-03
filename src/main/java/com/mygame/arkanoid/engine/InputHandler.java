package com.mygame.arkanoid.engine;

import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.event.*;


/**
 * Quản lý tập trung đầu vào (input) từ bàn phím và chuột.
 * <p>
 * Lớp này implements các interface listener của AWT (KeyListener, MouseListener)
 * để nhận sự kiện, sau đó lưu trạng thái của chúng.
 * Các lớp khác (như GameManager, Player) sẽ "hỏi" (poll) trạng thái từ lớp này.
 */
public class InputHandler implements KeyListener, MouseMotionListener, MouseListener {
    /** Mảng lưu trạng thái nhấn/thả của 256 mã phím. */
    private final boolean[] keys = new boolean[256];
    /** Tọa độ X, Y thô (raw) của chuột trên cửa sổ. */
    private int mouseX, mouseY;
    /**
     * Cờ "one-shot": chỉ true trong 1 frame ngay sau khi click,
     * sau đó bị reset ngay khi được gọi bởi isMouseClicked().
     */
    private boolean mouseClicked = false;
    /** Cờ "level-trigger": true SUỐT THỜI GIAN chuột đang được nhấn giữ. */
    private boolean isCurrentlyPressed = false;

    public InputHandler() {
        this.mouseX = 0;
        this.mouseY = 0;
    }

    /**
     * Kiểm tra xem một phím có đang được **nhấn giữ** hay không (level-triggered).
     * @param keyCode Mã phím (ví dụ: {@code KeyEvent.VK_SPACE}).
     * @return true nếu phím đang được giữ, false nếu không.
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
     * Kiểm tra xem một cú click chuột (nhấn xuống) có **vừa xảy ra** hay không.
     * <p>
     * Đây là kiểu kiểm tra "one-shot" (hoặc "edge-triggered").
     * Nó sẽ trả về {@code true} **chỉ một lần** cho mỗi cú click.
     * <p>
     * <b>Quan trọng:</b> Gọi hàm này sẽ "tiêu thụ" (consume) cú click,
     * tự động reset cờ về {@code false} cho đến khi có cú click tiếp theo.
     *
     * @return true nếu một cú click mới vừa xảy ra kể từ lần kiểm tra trước.
     */
    public boolean isMouseClicked() {
        if (mouseClicked) {
            mouseClicked = false; // Reset (tiêu thụ) cú click ngay sau khi kiểm tra
            return true;
        }
        return false;
    }

    /**
     * Kiểm tra xem chuột có đang được **nhấn giữ** hay không.
     * <p>
     * Đây là kiểu kiểm tra "level-triggered" (ngược lại với `isMouseClicked`).
     * Nó sẽ trả về {@code true} liên tục miễn là nút chuột trái còn được giữ.
     *
     * @return true nếu nút chuột trái đang được giữ.
     */
    public boolean isMousePressed() {
        return isCurrentlyPressed;
    }

    /**
     * Xử lý sự kiện khi một phím được nhấn.
     * @param e the event to be processed
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
     * @param e the event to be processed
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
     * @param e the event to be processed
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        // Cập nhật vị trí chuột (tọa độ thô của cửa sổ)
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    /**
     * Xử lý sự kiện khi chuột được nhấn.
     * @param e the event to be processed
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // Chỉ xử lý chuột trái
        if (e.getButton() == MouseEvent.BUTTON1) {
            // 1. Kích hoạt cờ "one-shot" (cho isMouseClicked)
            this.mouseClicked = true;
            // 2. Kích hoạt cờ "level-trigger" (cho isMousePressed)
            this.isCurrentlyPressed = true;
        }
    }

    /**
     * Lấy tọa độ X <b>ảo</b> của chuột (đã qua "unscaling").
     * <p>
     * Tọa độ này khớp với hệ tọa độ gốc của game (ví dụ: 1120x720),
     * bất kể kích thước cửa sổ vật lý là gì.
     *
     * @return Tọa độ X ảo trong thế giới game.
     */
    public int getVirtualMouseX() {
        return ScalingManager.getInstance().unscaleX(this.mouseX);
    }

    /**
     * Lấy tọa độ Y <b>ảo</b> của chuột (đã qua "unscaling").
     *
     * @return Tọa độ Y ảo trong thế giới game.
     */
    public int getVirtualMouseY() {
        return ScalingManager.getInstance().unscaleY(this.mouseY);
    }


    /** (Không sử dụng - chúng ta dùng polling qua keyPressed/keyReleased) */
    @Override public void keyTyped(KeyEvent e) {}

    /** Coi việc kéo thả chuột (drag) giống như di chuyển chuột (move) bình thường. */
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }

    /** (Không sử dụng - chúng ta dùng mousePressed để có phản hồi ngay lập tức) */
    @Override public void mouseClicked(MouseEvent e) {}

    /**
     * Xử lý sự kiện khi chuột được thả.
     * @param e the event to be processed
     */
    @Override public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.isCurrentlyPressed = false; // Tắt cờ "level-trigger"
        }
    }

    /** (Không sử dụng) */
    @Override public void mouseEntered(MouseEvent e) {}
    /** (Không sử dụng) */
    @Override public void mouseExited(MouseEvent e) {}
}