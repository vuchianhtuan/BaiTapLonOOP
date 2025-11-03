package com.mygame.arkanoid;

import com.mygame.arkanoid.core.*;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.save.SaveSystem;
import com.mygame.arkanoid.save.SaveData;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Lớp `Main` là điểm khởi đầu (entry point) của ứng dụng trò chơi Arkanoid.
 * <p>
 * Trách nhiệm chính của lớp này là thiết lập cửa sổ ({@link JFrame}),
 * khởi tạo các thành phần cốt lõi ({@link GameManager}, {@link GamePanel},
 * {@link GameLoop}), và bắt đầu vòng lặp trò chơi.
 * <p>
 * Nó cũng xử lý các sự kiện vòng đời quan trọng của cửa sổ:
 * <ul>
 * <li>Tự động tải (load) dữ liệu đã lưu khi khởi động.</li>
 * <li>Cập nhật tỷ lệ co giãn ({@link ScalingManager}) khi người dùng
 * thay đổi kích thước cửa sổ.</li>
 * <li>Tự động lưu (save) trạng thái game khi người dùng đóng cửa sổ
 * (nếu game đang ở trạng thái có thể tiếp tục).</li>
 * </ul>
 */
public class Main {
    /**
     * Cửa sổ chính ({@link JFrame}) của toàn bộ ứng dụng trò chơi.
     * Được khai báo là `static` để có thể truy cập từ bất kỳ đâu (nếu cần),
     * mặc dù điều này thường không được khuyến khích bằng việc truyền tham chiếu.
     */
    public static JFrame window;

    /**
     * Phương thức chính (entry point) của ứng dụng Java.
     *
     * @param args Các tham số dòng lệnh (không được sử dụng trong game này).
     */
    public static void main(String[] args) {

        // --- 1. Thiết lập Cửa sổ (Window) ---
        window = new JFrame("Arkanoid");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Thoát khi nhấn 'X'
        window.setResizable(true); // Cho phép thay đổi kích thước

        // --- 2. Khởi tạo GameManager và Tải dữ liệu đã lưu (nếu có) ---
        /**
         * Lấy instance của GameManager (Singleton),
         * đây là "bộ não" trung tâm của game.
         */
        GameManager gameManager = GameManager.getInstance();

        /**
         * Thử tải (load) dữ liệu từ tệp save (ví dụ: savegame.bin).
         */
        SaveData data = SaveSystem.load();
        if (data != null && data.isCanContinue()) {
            // Nếu có file save hợp lệ, khôi phục (restore) trạng thái game
            gameManager.restoreFromSave(data);
        }

        // --- 3. Khởi tạo các thành phần cốt lõi của game ---
        /**
         * Khởi tạo Giao diện (Panel) để vẽ (render) game.
         * Đây là "bức tranh" (canvas) của game.
         */
        GamePanel gamePanel = new GamePanel(gameManager);
        /**
         * Khởi tạo Vòng lặp game (GameLoop) trên một luồng (Thread) riêng biệt.
         * Nó sẽ gọi `gameManager.update()` và `gamePanel.repaint()` liên tục.
         */
        GameLoop gameLoop = new GameLoop(gameManager, gamePanel);

        window.add(gamePanel); // Thêm "bức tranh" vào cửa sổ

        // --- 4. Thiết lập Listener (Trình nghe) sự kiện thay đổi kích thước ---
        /**
         * Bất cứ khi nào cửa sổ bị người dùng thay đổi kích thước,
         * chúng ta cần gọi `ScalingManager.update()`
         * để tính toán lại tỷ lệ co giãn (scale) và
         * độ dời (offset) cho việc "letterboxing" (viền đen).
         */
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = gamePanel.getWidth();
                int newHeight = gamePanel.getHeight();

                // Cập nhật ScalingManager với kích thước mới
                ScalingManager.getInstance().update(newWidth, newHeight);
            }
        });

        // --- 5. Thiết lập Listener (Trình nghe) sự kiện Đóng Cửa sổ ---
        /**
         * Xử lý việc tự động lưu game khi người dùng nhấn nút 'X'
         * trên cửa sổ (trước khi ứng dụng thoát).
         */
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                /**
                 * Chỉ tự động lưu game nếu người chơi đang ở trạng thái
                 * có thể "Tiếp tục" (canContinue).
                 * (Ví dụ: đang chơi hoặc tạm dừng,
                 * không phải ở màn hình GAME OVER hoặc GAME WIN).
                 */
                if (gameManager.canContinue()) {
                    // "Chụp" (capture) trạng thái hiện tại của GameManager
                    // và lưu (save) nó vào tệp.
                    SaveSystem.save(SaveSystem.capture(gameManager));
                }
                System.exit(0); // Đảm bảo ứng dụng thoát
            }
        });

        // --- 6. Hoàn tất thiết lập và Hiển thị Cửa sổ ---
        /**
         * `pack()`: Tự động điều chỉnh kích thước cửa sổ
         * vừa vặn với kích thước mong muốn (preferred size)
         * của các component bên trong nó (ở đây là GamePanel).
         */
        window.pack();
        window.setLocationRelativeTo(null); // Căn giữa cửa sổ trên màn hình
        window.setVisible(true); // Hiển thị cửa sổ

        // --- 7. Bắt đầu Game ---
        /**
         * Cập nhật tỷ lệ scale lần đầu tiên ngay sau khi
         * `pack()` và `setVisible()` được gọi.
         * Điều này đảm bảo kích thước ban đầu là chính xác.
         */
        ScalingManager.getInstance().update(gamePanel.getWidth(), gamePanel.getHeight());

        /**
         * Bắt đầu luồng (Thread) của vòng lặp game.
         * Logic game (update/render) bắt đầu chạy từ đây.
         */
        gameLoop.start();
    }
}