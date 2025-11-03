package com.mygame.arkanoid.save;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.LaserShooterBrick;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Lớp tiện ích (utility class) tĩnh, chịu trách nhiệm xử lý
 * việc lưu (save), tải (load), và xóa (delete) trạng thái trò chơi.
 * <p>
 * Lớp này sử dụng cơ chế Tuần tự hóa (Serialization) của Java
 * để ghi và đọc đối tượng {@link SaveData} vào/từ một tệp tin nhị phân.
 * <p>
 * Nó cũng cung cấp phương thức {@link #capture(GameManager)}
 * để "chụp ảnh" (snapshot) trạng thái hiện tại của {@link GameManager}
 * và chuyển đổi nó thành một đối tượng {@link SaveData} "sạch".
 * <p>
 * Lớp này là {@code final} và có constructor {@code private}
 * để ngăn chặn việc tạo đối tượng.
 */
public final class SaveSystem {

    /** Constructor private để ngăn chặn việc tạo đối tượng (instantiation). */
    private SaveSystem() {}

    /**
     * Lấy đường dẫn (Path) đến thư mục lưu trữ trạng thái game.
     * <p>
     * Thư mục này thường là {@code /home/username/.arkanoid}
     * (trên Linux/macOS) hoặc
     * {@code C:\Users\.username\.arkanoid} (trên Windows).
     *
     * @return Một đối tượng {@link Path} trỏ đến thư mục lưu trữ.
     */
    public static Path getSaveDir() {
        String home = System.getProperty("user.home"); // Lấy thư mục home của người dùng
        return Paths.get(home, ".arkanoid"); // Trả về đường dẫn ".arkanoid" bên trong home
    }

    /**
     * Lấy đường dẫn (Path) đầy đủ đến tệp tin lưu trữ trạng thái game.
     *
     * @return Một đối tượng {@link Path} trỏ đến tệp (ví dụ: ".../.arkanoid/savegame.bin").
     */
    public static Path getSaveFile() { return getSaveDir().resolve("savegame.bin"); }

    /**
     * Lưu (tuần tự hóa) một đối tượng {@link SaveData} vào tệp tin.
     * <p>
     * Sẽ tự động tạo thư mục lưu trữ nếu nó chưa tồn tại.
     * Ghi đè (overwrite) tệp save cũ nếu đã tồn tại.
     *
     * @param data Đối tượng {@link SaveData} chứa trạng thái game cần lưu.
     */
    public static void save(SaveData data) {
        try {
            Files.createDirectories(getSaveDir()); // Tạo thư mục nếu chưa tồn tại
            // Sử dụng try-with-resources để đảm bảo ObjectOutputStream được đóng
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(getSaveFile()))) {
                oos.writeObject(data); // Tuần tự hóa và ghi đối tượng ra tệp
            }
        } catch (IOException e) {
            // Xử lý lỗi nếu không thể ghi tệp (ví dụ: không có quyền ghi)
            e.printStackTrace();
        }
    }

    /**
     * Tải (giải tuần tự hóa) trạng thái game từ tệp tin.
     *
     * @return Một đối tượng {@link SaveData} nếu tải thành công,
     * hoặc {@code null} nếu tệp không tồn tại hoặc có lỗi
     * (ví dụ: tệp bị hỏng, phiên bản không tương thích - {@code InvalidClassException}).
     */
    public static SaveData load() {
        Path f = getSaveFile();
        if (!Files.exists(f)) return null; // Trả về null nếu không tìm thấy tệp

        // Sử dụng try-with-resources để đảm bảo ObjectInputStream được đóng
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(f))) {
            Object o = ois.readObject(); // Đọc đối tượng từ tệp
            // Kiểm tra an toàn: đảm bảo đối tượng đọc được đúng là SaveData
            return (o instanceof SaveData) ? (SaveData) o : null;
        } catch (IOException | ClassNotFoundException e) {
            // Xử lý lỗi (ví dụ: tệp hỏng, ClassNotFound, InvalidClassException)
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Xóa tệp tin lưu trạng thái game (nếu tồn tại).
     * Thường được gọi khi người chơi hoàn thành game (thắng/thua)
     * hoặc chọn "New Game" và ghi đè save cũ.
     */
    public static void deleteSave() {
        try { Files.deleteIfExists(getSaveFile()); }
        catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * "Chụp ảnh" (snapshot) trạng thái hiện tại của {@link GameManager}
     * và chuyển đổi nó thành một đối tượng {@link SaveData} "sạch".
     *
     * @param gm Đối tượng {@link GameManager} đang chạy.
     * @return Một đối tượng {@link SaveData} chứa các thông tin cần thiết.
     */
    public static SaveData capture(GameManager gm) {
        SaveData d = new SaveData();

        // --- Chụp trạng thái người chơi và tiến trình ---
        d.setLevelIndex(gm.getLevelManager() != null ? gm.getLevelManager().getCurrentLevelIndex() : 0); // màn hiện tại
        d.setScore(gm.getScore()); // điểm hiện tại
        d.setLives(gm.getLives()); // số mạng hiện tại
        d.setPlaytimeMillis(gm.getPlaytimeMillis()); // tổng thời gian chơi
        d.setCurrentLevelPlaytimeMillis(gm.getCurrentLevelPlaytimeMillis()); // thời gian trong màn
        d.setCanContinue(true); // Đánh dấu là có thể "Tiếp tục"
        d.setSavedAtEpochMillis(System.currentTimeMillis()); // Dấu thời gian

        // --- Chụp trạng thái gạch ---
        // (Đây là phần quan trọng nhất để khôi phục màn chơi)
        // Sử dụng Set để đảm bảo không có ID trùng lặp
        Set<Integer> alive = new LinkedHashSet<>();
        // Lấy ID của tất cả gạch thường còn sống
        for (Brick b : gm.getBricks()) alive.add(b.getId());
        // Lấy ID của tất cả gạch bắn laser còn sống
        for (LaserShooterBrick s : gm.getLaserShooters()) alive.add(s.getId());
        // Lấy ID của tất cả gạch (của Boss) còn sống
        if (gm.getBoss() != null) {
            for (Brick b : gm.getBoss().getBricks()) alive.add(b.getId());
        }
        // Chuyển Set thành List để lưu vào SaveData
        d.setAliveBrickIds(new ArrayList<>(alive));

        return d;
    }
}