package com.mygame.arkanoid.level;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý việc tải và chuyển đổi giữa các màn chơi (levels) trong game.
 * Lớp này giữ một danh sách các đường dẫn tệp level và theo dõi
 * level hiện tại đang được chơi.
 */
public class LevelManager {
    /** Danh sách các đường dẫn (paths) tới các file định nghĩa level. */
    private List<String> levelFiles;
    /** Chỉ số (index) của level hiện tại trong danh sách `levelFiles`. */
    private int currentLevelIndex;
    /** Đối tượng Level hiện tại đã được tải vào bộ nhớ. */
    private Level currentLevel;

    /**
     * Khởi tạo LevelManager.
     * `currentLevelIndex` được đặt là -1 để lần gọi {@link #loadNextLevel()}
     * đầu tiên sẽ tăng chỉ số lên 0 và tải level đầu tiên.
     */
    public LevelManager() {
        levelFiles = new ArrayList<>();
        currentLevelIndex = -1; // Bắt đầu từ -1
    }

    /**
     * Tải (hardcode) danh sách đường dẫn đến các tệp level.
     * Thứ tự các level được thêm vào đây quyết định thứ tự chơi trong game.
     */
    public void loadLevels() {
        levelFiles.add("/levels/Level1.txt");
        levelFiles.add("/levels/Level2.txt");
        levelFiles.add("/levels/Level_Boss.txt");
        // Thêm các file level khác nếu có
    }

    /**
     * Tải trực tiếp một màn chơi theo chỉ số (index) cung cấp.
     * Cập nhật {@code currentLevelIndex} và {@code currentLevel} nếu thành công.
     *
     * @param index Chỉ số của màn (0, 1, 2...)
     * @return true nếu tải thành công, false nếu chỉ số không hợp lệ hoặc có lỗi tải.
     */
    public boolean loadSpecificLevel(int index) {
        if (index >= 0 && index < levelFiles.size()) {
            currentLevelIndex = index; // Cập nhật chỉ số hiện tại
            String levelFile = levelFiles.get(currentLevelIndex);

            // Thử tải level từ file
            try {
                currentLevel = new Level(levelFile); // Tạo đối tượng Level mới
                return true;
            } catch (IllegalArgumentException e) {
                // Lỗi này thường xảy ra nếu getResourceAsStream không tìm thấy file
                System.err.println("Không thể tải level (không hợp lệ): " + e.getMessage());
            } catch (RuntimeException e) {
                // Lỗi này thường xảy ra nếu file bị lỗi cú pháp
                System.err.println("Lỗi khi tải level: " + e.getMessage());
                e.printStackTrace();
            }

            currentLevel = null; // Đảm bảo level là null nếu tải thất bại
            return false;
        }
        // Lỗi nếu chỉ số nằm ngoài phạm vi
        System.err.println("Lỗi: Chỉ số level không hợp lệ: " + index);
        currentLevel = null; // Đặt level hiện tại là null nếu lỗi
        return false; // Chỉ số không hợp lệ
    }

    /**
     * Đặt màn chơi hiện tại và tải nó.
     * Đây là một phương thức tiện ích (wrapper) cho {@link #loadSpecificLevel(int)}.
     *
     * @param index Chỉ số của màn muốn đặt (0, 1, 2...)
     */
    public void setCurrentLevel(int index) {
        // Không cần trừ 1 nữa, gọi trực tiếp loadSpecificLevel
        loadSpecificLevel(index);
    }

    /**
     * Tải level tiếp theo trong danh sách.
     * Tăng {@code currentLevelIndex} lên 1, sau đó tải level tại chỉ số mới đó.
     *
     * @return true nếu còn level và tải thành công, false nếu hết level hoặc tải lỗi.
     */
    public boolean loadNextLevel() {
        currentLevelIndex++; // Tăng chỉ số để lấy level tiếp theo
        if (hasMoreLevels()) {
            // Nếu chỉ số mới vẫn hợp lệ
            String nextLevelFile = levelFiles.get(currentLevelIndex);
            try {
                currentLevel = new Level(nextLevelFile);
                return true;
            } catch (RuntimeException e) {
                // Xử lý lỗi nếu file level tiếp theo bị hỏng
                System.err.println("Không thể tải level tiếp theo: " + e.getMessage());
                e.printStackTrace();
                currentLevel = null;
            }
        }
        // Nếu không còn level (chỉ số vượt quá kích thước danh sách)
        currentLevel = null; // Đặt là null khi hết level
        return false; // Không còn level nào
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public boolean hasMoreLevels() {
        return currentLevelIndex < levelFiles.size();
    }

    /**
     * Đặt lại (reset) trình quản lý level.
     * Đặt {@code currentLevelIndex} về -1 để lần gọi {@link #loadNextLevel()}
     * tiếp theo sẽ bắt đầu lại từ level 0.
     */
    public void reset() {
        currentLevelIndex = -1; // Reset để loadNextLevel bắt đầu từ 0
        currentLevel = null;
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }
}