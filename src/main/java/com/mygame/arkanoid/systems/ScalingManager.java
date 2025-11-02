package com.mygame.arkanoid.systems;

// HỆ THỐNG QUẢN LÝ LOGIC CO GIÃN (SCALING) CỬA SỔ
public class ScalingManager {
    private static ScalingManager instance;

    // Kích thước logic gốc CỦA TOÀN BỘ CỬA SỔ
    public final int NATIVE_WIDTH = 1120;
    public final int NATIVE_HEIGHT = 720;
    // Kích thước logic CỦA KHU VỰC CHƠI GAME
    public final int GAME_AREA_WIDTH = 960;

    // Biến cho logic co giãn (scaling) mới
    private double scale = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    private ScalingManager() {
    }

    /**
     * Lấy thể hiện đơn (singleton) của ScalingManager.
     */
    public static synchronized ScalingManager getInstance() {
        if (instance == null) {
            instance = new ScalingManager();
        }
        return instance;
    }

    /**
     * Cập nhật hệ số scale dựa trên kích thước cửa sổ hiện tại.
     * Logic này giữ đúng tỷ lệ khung hình (aspect ratio).
     */
    public void update(int currentWindowWidth, int currentWindowHeight) {
        double scaleX = (double) currentWindowWidth / NATIVE_WIDTH;
        double scaleY = (double) currentWindowHeight / NATIVE_HEIGHT;
        this.scale = Math.min(scaleX, scaleY);

        int renderWidth = (int) (NATIVE_WIDTH * this.scale);
        int renderHeight = (int) (NATIVE_HEIGHT * this.scale);

        this.offsetX = (currentWindowWidth - renderWidth) / 2;
        this.offsetY = (currentWindowHeight - renderHeight) / 2;
    }

    // --- CÁC HÀM CŨ VỚI LOGIC MỚI ---

    /**
     * Chuyển tọa độ X logic sang tọa độ X màn hình
     * (GIỮ NGUYÊN TÊN HÀM CŨ)
     */
    public int scaleX(int logicX) {
        return (int) (logicX * scale) + offsetX;
    }

    /**
     * Chuyển tọa độ Y logic sang tọa độ Y màn hình
     * (GIỮ NGUYÊN TÊN HÀM CŨ)
     */
    public int scaleY(int logicY) {
        return (int) (logicY * scale) + offsetY;
    }

    /**
     * Chuyển chiều rộng logic sang chiều rộng màn hình
     * (GIỮ NGUYÊN TÊN HÀM CŨ)
     */
    public int scaleWidth(int logicWidth) {
        return (int) (logicWidth * scale);
    }

    /**
     * Chuyển chiều cao logic sang chiều cao màn hình
     * (GIỮ NGUYÊN TÊN HÀM CŨ)
     */
    public int scaleHeight(int logicHeight) {
        return (int) (logicHeight * scale);
    }

    // --- CÁC HÀM "UN-SCALE" (Vẫn cần thiết cho InputHandler) ---

    /** Chuyển tọa độ X màn hình (ví dụ: chuột) sang X logic */
    public int unscaleX(int screenX) {
        if (scale == 0) return 0;
        return (int) ((screenX - offsetX) / scale);
    }

    /** Chuyển tọa độ Y màn hình (ví dụ: chuột) sang Y logic */
    public int unscaleY(int screenY) {
        if (scale == 0) return 0;
        return (int) ((screenY - offsetY) / scale);
    }


    public double getScale() {
        return scale;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }
}
