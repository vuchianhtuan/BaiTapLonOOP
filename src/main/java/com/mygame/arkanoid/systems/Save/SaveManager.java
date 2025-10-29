package com.mygame.arkanoid.systems.Save;

import com.mygame.arkanoid.core.GameManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Lưu/khôi phục tiến trình và thiết lập vào thư mục người dùng:
 * %USERPROFILE%\.arkanoid\savegame.bin và settings.bin
 */
public class SaveManager {

    private final Path saveDir = Paths.get(System.getProperty("user.home"), ".arkanoid");
    private final Path saveFile = saveDir.resolve("savegame.bin");
    private final Path settingsFile = saveDir.resolve("settings.bin");

    public SaveManager() {
        try {
            Files.createDirectories(saveDir);
        } catch (IOException ignored) {}
    }

    public void saveGame(GameManager gm) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(saveFile))) {
            SaveData data = new SaveData();
            int levelIndex = (gm.getLevelManager() != null) ? gm.getLevelManager().getCurrentLevelIndex() : 0;

            data.setLevelIndex(levelIndex);
            data.setLives(gm.getLives());
            data.setScore(gm.getScore());
            data.setPlaytimeMillis(gm.getPlaytimeMillis());
            data.setCurrentLevelPlaytimeMillis(gm.getCurrentLevelPlaytimeMillis());

            String state = gm.getGameState();
            boolean canContinue = gm.canContinue();
            // Nếu đang ở GAME_OVER/GAME_WIN thì không cho continue.
            if ("GAME_OVER".equals(state) || "GAME_WIN".equals(state)) {
                canContinue = false;
            }
            data.setCanContinue(canContinue);
            data.setSavedAtEpochMillis(System.currentTimeMillis());

            oos.writeObject(data);
        } catch (IOException e) {
            // Ghi log đơn giản, tránh làm vỡ game loop
            System.err.println("SaveManager.saveGame failed: " + e.getMessage());
        }
    }

    /**
     * Khởi tạo một GameManager mới và áp trạng thái từ save nếu hợp lệ.
     * Trả về null nếu không có save hoặc không thể continue.
     */
    public GameManager loadGame() {
        if (!Files.exists(saveFile)) return null;

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(saveFile))) {
            Object obj = ois.readObject();
            if (!(obj instanceof SaveData)) return null;

            SaveData data = (SaveData) obj;
            if (!data.isCanContinue()) return null;

            GameManager gm = GameManager.getInstance();
            // Áp trạng thái về MENU + bật continue, nạp đúng level
            gm.restoreFromSave(data);
            return gm;

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("SaveManager.loadGame failed: " + e.getMessage());
            return null;
        }
    }

    // --- Settings ---

    public void saveSettings(SettingsData settings) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(settingsFile))) {
            oos.writeObject(settings);
        } catch (IOException e) {
            System.err.println("SaveManager.saveSettings failed: " + e.getMessage());
        }
    }

    public SettingsData loadSettings() {
        if (!Files.exists(settingsFile)) return new SettingsData();
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(settingsFile))) {
            Object obj = ois.readObject();
            return (obj instanceof SettingsData) ? (SettingsData) obj : new SettingsData();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("SaveManager.loadSettings failed: " + e.getMessage());
            return new SettingsData();
        }
    }
}
