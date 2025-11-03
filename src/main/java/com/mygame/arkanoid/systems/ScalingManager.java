package com.mygame.arkanoid.systems;

/**
 * Quản lý việc co giãn (scaling) và căn chỉnh (letterboxing)
 * của toàn bộ cửa sổ game.
 * <p>
 * Lớp này hoạt động theo mẫu Singleton và chịu trách nhiệm tính toán
 * tỷ lệ ({@code scale}) và độ dời ({@code offsetX}, {@code offsetY})
 * để đảm bảo rằng kích thước logic ({@code NATIVE_WIDTH}, {@code NATIVE_HEIGHT})
 * luôn vừa vặn bên trong cửa sổ vật lý (window) mà không bị méo hình
 * (duy trì tỷ lệ khung hình - aspect ratio).
 * <p>
 * <b>Quy trình hoạt động:</b>
 * <ol>
 * <li>{@link com.mygame.arkanoid.core.GamePanel} gọi {@link #update(int, int)}
 * mỗi frame, cung cấp kích thước cửa sổ hiện tại.</li>
 * <li>{@code update} tính toán giá trị {@code scale}, {@code offsetX},
 * {@code offsetY} mới.</li>
 * <li>Tất cả các phương thức {@code render} trong game
 * sử dụng {@link #scaleX}, {@link #scaleY}, {@link #scaleWidth},
 * {@link #scaleHeight} để chuyển đổi tọa độ logic (ví dụ: x=100)
 * sang tọa độ màn hình (ví dụ: x=150) trước khi vẽ.</li>
 * <li>{@link com.mygame.arkanoid.engine.InputHandler} sử dụng
 * {@link #unscaleX} và {@link #unscaleY} để chuyển đổi
 * tọa độ chuột (màn hình) về tọa độ logic (game).</li>
 * </ol>
 */
public class ScalingManager {
    private static ScalingManager instance;

    // Kích thước logic gốc (native resolution) của toàn bộ game
    public final int NATIVE_WIDTH = 1120;
    public final int NATIVE_HEIGHT = 720;
    /** Kích thước logic của khu vực chơi game (không tính Sidebar UI). */
    public final int GAME_AREA_WIDTH = 960;

    // --- Biến lưu trữ kết quả tính toán ---
    /** Tỷ lệ co giãn chung (ví dụ: 1.5, 2.0). */
    private double scale = 1.0;
    /** Độ dời (offset) theo trục X để căn giữa (letterbox). */
    private int offsetX = 0;
    /** Độ dời (offset) theo trục Y để căn giữa (letterbox). */
    private int offsetY = 0;

    /** Constructor private để thực thi Singleton. */
    private ScalingManager() {
    }

    /**
     * Lấy thể hiện (instance) duy nhất của ScalingManager (Singleton).
     * <p>
     * <b>Lưu ý:</b> Phương thức này là {@code synchronized} để đảm bảo
     * an toàn luồng (thread-safety) trong trường hợp có nhiều luồng
     * cố gắng khởi tạo nó cùng lúc (mặc dù trong game Swing
     * điều này hiếm khi xảy ra).
     */
    public static synchronized ScalingManager getInstance() {
        if (instance == null) {
            instance = new ScalingManager();
        }
        return instance;
    }

    /**
     * Cập nhật các hệ số co giãn (scale, offsetX, offsetY)
     * dựa trên kích thước cửa sổ vật lý hiện tại.
     * <p>
     * Logic này tính toán tỷ lệ co giãn (scale) nhỏ nhất theo cả hai
     * trục (X và Y) để đảm bảo toàn bộ kích thước gốc (NATIVE)
     * luôn nằm vừa vặn bên trong cửa sổ. Sau đó, nó tính toán
     * {@code offsetX} và {@code offsetY} để căn giữa (letterbox)
     * khu vực game nếu cửa sổ không có cùng tỷ lệ khung hình.
     *
     * @param currentWindowWidth Chiều rộng vật lý hiện tại của cửa sổ (pixel).
     * @param currentWindowHeight Chiều cao vật lý hiện tại của cửa sổ (pixel).
     */
    public void update(int currentWindowWidth, int currentWindowHeight) {
        // 1. Tính tỷ lệ scale riêng cho từng trục
        double scaleX = (double) currentWindowWidth / NATIVE_WIDTH;
        double scaleY = (double) currentWindowHeight / NATIVE_HEIGHT;

        // 2. Chọn tỷ lệ nhỏ nhất (để vừa vặn, không bị cắt xén)
        this.scale = Math.min(scaleX, scaleY);

        // 3. Tính kích thước render thực tế trên màn hình
        int renderWidth = (int) (NATIVE_WIDTH * this.scale);
        int renderHeight = (int) (NATIVE_HEIGHT * this.scale);

        // 4. Tính toán offset để căn giữa (tạo viền đen "letterbox")
        this.offsetX = (currentWindowWidth - renderWidth) / 2;
        this.offsetY = (currentWindowHeight - renderHeight) / 2;
    }

    /**
     * Chuyển đổi một tọa độ X <b>logic (game)</b>
     * sang tọa độ X <b>vật lý (màn hình)</b> để vẽ.
     * <p>
     * Công thức: {@code (logicX * scale) + offsetX}
     *
     * @param logicX Tọa độ X trong thế giới game (ví dụ: 100).
     * @return Tọa độ X tương ứng trên màn hình (ví dụ: 150).
     */
    public int scaleX(int logicX) {
        return (int) (logicX * scale) + offsetX;
    }

    /**
     * Chuyển đổi một tọa độ Y <b>logic (game)</b>
     * sang tọa độ Y <b>vật lý (màn hình)</b> để vẽ.
     * <p>
     * Công thức: {@code (logicY * scale) + offsetY}
     *
     * @param logicY Tọa độ Y trong thế giới game (ví dụ: 200).
     * @return Tọa độ Y tương ứng trên màn hình (ví dụ: 300).
     */
    public int scaleY(int logicY) {
        return (int) (logicY * scale) + offsetY;
    }

    /**
     * Chuyển đổi một chiều rộng <b>logic (game)</b>
     * sang chiều rộng <b>vật lý (màn hình)</b>.
     * (Không cộng offset vì đây là kích thước, không phải vị trí).
     *
     * @param logicWidth Chiều rộng logic (ví dụ: 40).
     * @return Chiều rộng tương ứng trên màn hình (ví dụ: 60).
     */
    public int scaleWidth(int logicWidth) {
        return (int) (logicWidth * scale);
    }

    /**
     * Chuyển đổi một chiều cao <b>logic (game)</b>
     * sang chiều cao <b>vật lý (màn hình)</b>.
     *
     * @param logicHeight Chiều cao logic (ví dụ: 20).
     * @return Chiều cao tương ứng trên màn hình (ví dụ: 30).
     */
    public int scaleHeight(int logicHeight) {
        return (int) (logicHeight * scale);
    }

    // --- CÁC HÀM "UN-SCALE" (Vẫn cần thiết cho InputHandler) ---

    /**
     * Chuyển đổi (ngược) một tọa độ X <b>vật lý (màn hình)</b>
     * (ví dụ: từ chuột) sang tọa độ X <b>logic (game)</b>.
     * <p>
     * Công thức: {@code (screenX - offsetX) / scale}
     *
     * @param screenX Tọa độ X trên màn hình (ví dụ: 150).
     * @return Tọa độ X tương ứng trong thế giới game (ví dụ: 100).
     */
    public int unscaleX(int screenX) {
        if (scale == 0) return 0; // Tránh lỗi chia cho 0
        return (int) ((screenX - offsetX) / scale);
    }

    /**
     * Chuyển đổi (ngược) một tọa độ Y <b>vật lý (màn hình)</b>
     * (ví dụ: từ chuột) sang tọa độ Y <b>logic (game)</b>.
     * <p>
     * Công thức: {@code (screenY - offsetY) / scale}
     *
     * @param screenY Tọa độ Y trên màn hình (ví dụ: 300).
     * @return Tọa độ Y tương ứng trong thế giới game (ví dụ: 200).
     */
    public int unscaleY(int screenY) {
        if (scale == 0) return 0; // Tránh lỗi chia cho 0
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