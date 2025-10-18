package com.mygame.arkanoid.systems;

public class ScalingManager {
    private static ScalingManager instance;

    // Đây là độ phân giải "ảo" mà toàn bộ logic game của bạn dựa trên
    public final int NATIVE_WIDTH = 1280;
    public final int NATIVE_HEIGHT = 720;

    private double scaleX = 1.0;
    private double scaleY = 1.0;

    private ScalingManager() {
        // Private constructor for Singleton pattern
    }

    public static synchronized ScalingManager getInstance() {
        if (instance == null) {
            instance = new ScalingManager();
        }
        return instance;
    }

    /**
     * Cập nhật hệ số scale dựa trên kích thước cửa sổ hiện tại.
     */
    public void update(int currentWidth, int currentHeight) {
        this.scaleX = (double) currentWidth / NATIVE_WIDTH;
        this.scaleY = (double) currentHeight / NATIVE_HEIGHT;
    }

    // Các phương thức để scale tọa độ và kích thước khi vẽ
    public int scaleX(int x) { return (int) (x * scaleX); }
    public int scaleY(int y) { return (int) (y * scaleY); }
    public int scaleWidth(int width) { return (int) (width * scaleX); }
    public int scaleHeight(int height) { return (int) (height * scaleY); }

    // Các phương thức để "un-scale" tọa độ chuột từ màn hình thật về màn hình ảo
    public int unscaleX(int screenX) { return (int) (screenX / scaleX); }
    public int unscaleY(int screenY) { return (int) (screenY / scaleY); }
}
