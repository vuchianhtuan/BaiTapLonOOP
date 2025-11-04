package com.mygame.arkanoid.save;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.LaserShooterBrick;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Hệ thống lưu trữ trạng thái game.
 */
public final class SaveSystem {
    private SaveSystem() {}

    /**
     * Lấy đường dẫn thư mục lưu trữ trạng thái game.
     */
    public static Path getSaveDir() {
        String home = System.getProperty("user.home");
        return Paths.get(home, ".arkanoid");
    }

    /**
     * Lấy đường dẫn tập tin lưu trữ trạng thái game.
     */
    public static Path getSaveFile() { return getSaveDir().resolve("savegame.bin"); }

    /**
     * Lưu trạng thái game vào tập tin.
     */
    public static void save(SaveData data) {
        try {
            Files.createDirectories(getSaveDir()); // Tạo thư mục nếu chưa tồn tại
            // Ghi dữ liệu vào tập tin
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(getSaveFile()))) {
                oos.writeObject(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tải trạng thái game từ tập tin.
     */
    public static SaveData load() {
        Path f = getSaveFile();
        if (!Files.exists(f)) return null;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(f))) {
            Object o = ois.readObject();
            return (o instanceof SaveData) ? (SaveData) o : null;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Xóa tập tin lưu trạng thái game.
     */
    public static void deleteSave() {
        try { Files.deleteIfExists(getSaveFile()); }
        catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Chụp trạng thái hiện tại của game để lưu.
     */
    public static SaveData capture(GameManager gm) {
        SaveData d = new SaveData();
        d.setLevelIndex(gm.getLevelManager() != null ? gm.getLevelManager().getCurrentLevelIndex() : 0); // màn hiện tại
        d.setScore(gm.getScore()); // điểm hiện tại
        d.setLives(gm.getLives()); // số mạng hiện tại
        d.setPlaytimeMillis(gm.getPlaytimeMillis()); // thời gian chơi hiện tại
        d.setCurrentLevelPlaytimeMillis(gm.getCurrentLevelPlaytimeMillis()); // thời gian
        d.setCanContinue(true); // cho phép tiếp tục
        d.setSavedAtEpochMillis(System.currentTimeMillis()); // thời gian lưu

        // danh sách ID các viên gạch còn sống
        Set<Integer> alive = new LinkedHashSet<>();
        for (Brick b : gm.getBricks()) alive.add(b.getId()); // thêm gạch thường
        for (LaserShooterBrick s : gm.getLaserShooters()) alive.add(s.getId()); // thêm gạch bắn laser
        if (gm.getBoss() != null) {
            for (Brick b : gm.getBoss().getBricks()) alive.add(b.getId());
        }
        d.setAliveBrickIds(new ArrayList<>(alive));

        return d;
    }
}
