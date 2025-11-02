package com.mygame.arkanoid.save;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.LaserShooterBrick;

import java.io.*;
import java.nio.file.*;
import java.util.*;

// HỆ THỐNG QUẢN LÝ LƯU TRẠNG THÁI GAME
public final class SaveSystem {
    private SaveSystem() {}

    /**
     * LẤY THƯ MỤC LƯU TRỮ TRONG THƯ MỤC NGƯỜI DÙNG
     * @return
     */
    public static Path getSaveDir() {
        String home = System.getProperty("user.home");
        return Paths.get(home, ".arkanoid");
    }

    /**
     * LẤY ĐƯỜNG DẪN TẬP TIN LƯU TRẠNG THÁI GAME
     * @return
     */
    public static Path getSaveFile() { return getSaveDir().resolve("savegame.bin"); }

    /**
     * LƯU TRẠNG THÁI GAME VÀO TẬP TIN
     * @param data DỮ LIỆU LƯU TRẠNG THÁI GAME
     */
    public static void save(SaveData data) {
        try {
            Files.createDirectories(getSaveDir());
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(getSaveFile()))) {
                oos.writeObject(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * TẢI TRẠNG THÁI GAME TỪ TẬP TIN
     * @return DỮ LIỆU LƯU TRẠNG THÁI GAME HOẶC NULL NẾU KHÔNG TÌM THẤY
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
     * XÓA TẬP TIN LƯU TRẠNG THÁI GAME NẾU TỒN TẠI TRÁNH LỖI KHI TẢI
     */
    public static void deleteSave() {
        try { Files.deleteIfExists(getSaveFile()); }
        catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * CHỤP ẢNH TRẠNG THÁI HIỆN TẠI CỦA GAME
     * @param gm
     * @return
     */
    public static SaveData capture(GameManager gm) {
        SaveData d = new SaveData();
        d.setLevelIndex(gm.getLevelManager() != null ? gm.getLevelManager().getCurrentLevelIndex() : 0);
        d.setScore(gm.getScore());
        d.setLives(gm.getLives());
        d.setPlaytimeMillis(gm.getPlaytimeMillis());
        d.setCurrentLevelPlaytimeMillis(gm.getCurrentLevelPlaytimeMillis());
        d.setCanContinue(true);
        d.setSavedAtEpochMillis(System.currentTimeMillis());

        Set<Integer> alive = new LinkedHashSet<>();
        for (Brick b : gm.getBricks()) alive.add(b.getId());
        for (LaserShooterBrick s : gm.getLaserShooters()) alive.add(s.getId());
        if (gm.getBoss() != null) {
            for (Brick b : gm.getBoss().getBricks()) alive.add(b.getId());
        }
        d.setAliveBrickIds(new ArrayList<>(alive));

        return d;
    }
}
