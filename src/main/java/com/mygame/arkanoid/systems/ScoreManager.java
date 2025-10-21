package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.objects.BackButton;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ScoreManager {
    private final GameManager gameManager;
    private InputHandler inputHandler;
    private int currentScore;
    private int highScore;
    private List<Integer> Leaderboard = new ArrayList<>(11);
    private Image BackgroundImage;
    private BackButton backButton;

    private static final Path SCORE_FILE = resolveResourceBackedScoreFile();
    private static final Path SCORE_DIR = SCORE_FILE.getParent();

    public ScoreManager(GameManager gameManager, InputHandler inputHandler) {
        this.currentScore = 0;
        this.highScore = 0;
        this.gameManager = gameManager;
        this.inputHandler = inputHandler;
        BackgroundImage = AssetManager.getInstance().getImage("scoreBackground");
        loadScoresFromFile();
        backButton = new BackButton(10, 10, 40, 40);
    }

    public void addScore(int points) {
        currentScore += points;
        if (currentScore > highScore) {
            highScore = currentScore;
            saveScoresToFile();
        }
    }

    public synchronized void submitScore(int finalScore) {
        if (finalScore <= 0) return;

        if (finalScore > highScore) {
            highScore = finalScore;
        }
        Leaderboard.add(finalScore);
        Leaderboard.sort(Comparator.reverseOrder());
        if (Leaderboard.size() > 10) {
            Leaderboard = new ArrayList<>(Leaderboard.subList(0, 10));
        }
        saveScoresToFile();
    }

    public int getCurrentScore() { return currentScore; }
    public int getHighScore() { return highScore; }
    public List<Integer> getLeaderboard() { return new ArrayList<>(Leaderboard); }

    public void resetScore() {
        currentScore = 0;
        saveScoresToFile();
    }

    public void addToLeaderboard(int score) {
        Leaderboard.add(score);
        Leaderboard.sort(Comparator.reverseOrder());
        if (Leaderboard.size() > 5) {
            Leaderboard = new ArrayList<>(Leaderboard.subList(0, 5)); // keep top 5
        }
        saveScoresToFile();
    }


    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

        if (inputHandler.isMouseClicked() && backButton.contains(virtualMouseX, virtualMouseY)) {
            gameManager.setGameState("MENU");
        }
    }

    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();

        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage, 0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        // Tiêu đề
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(Color.WHITE);
        g.drawString("HIGH SCORES", sm.scaleX(430), sm.scaleY(60));

        // Hiển thị High Score
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("High Score: " + highScore, sm.scaleX(430), sm.scaleY(100));

        // Danh sách Top Scores
        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < Leaderboard.size(); i++) {
            int score = Leaderboard.get(i);
            String entryText = String.format("Top %d: %d", i + 1, score);
            int logicX = 430;
            int logicY = 140 + i * 36;
            g.drawString(entryText, sm.scaleX(logicX), sm.scaleY(logicY));
        }

        // Nút Back
        backButton.draw(g, sm);
    }

    // --- Persistence helpers ---

    // Tạo đường dẫn tới file điểm số ưu tiên các thư mục phát triển nếu có thể ghi được
    private static Path resolveResourceBackedScoreFile() {
        // 1) Đường dẫn phát triển: `<project>/src/main/resources`
        Path devResources = Paths.get(System.getProperty("user.dir"), "src", "main", "resources");
        if (Files.isDirectory(devResources) && Files.isWritable(devResources)) {
            return devResources.resolve("scores.properties");
        }

        // 2) Đường dẫn runtime: thư mục classes của ứng dụng
        try {
            URL url = ScoreManager.class.getClassLoader().getResource("");
            if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
                Path classesDir = Paths.get(url.toURI());
                if (Files.isDirectory(classesDir) && Files.isWritable(classesDir)) {
                    return classesDir.resolve("scores.properties");
                }
            }
        } catch (Exception ignored) {
            // ignore and fallback
        }

        // 3) Đường dẫn người dùng: thư mục home của người dùng
        Path homeDir = Paths.get(System.getProperty("user.home"), ".arkanoid");
        return homeDir.resolve("scores.properties");
    }

    // Đảm bảo file điểm số tồn tại, nếu không thì tạo mới với điểm số mặc định
    private void ensureFileExists() throws IOException {
        if (Files.notExists(SCORE_DIR)) {
            Files.createDirectories(SCORE_DIR);
        }
        if (Files.notExists(SCORE_FILE)) {
            Properties p = new Properties();
            p.setProperty("highScore", "0");
            p.setProperty("leaderboard", "");
            try (OutputStream os = Files.newOutputStream(
                    SCORE_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                p.store(os, "Arkanoid scores");
            }
        }
    }

    // Tải điểm số từ file
    private void loadScoresFromFile() {
        try {
            ensureFileExists();
            Properties p = new Properties();
            try (InputStream is = Files.newInputStream(SCORE_FILE, StandardOpenOption.READ)) {
                p.load(is);
            }

            String hs = p.getProperty("highScore", "0").trim();
            try {
                highScore = Integer.parseInt(hs);
            } catch (NumberFormatException ignored) {
                highScore = 0;
            }

            Leaderboard.clear();
            String lb = p.getProperty("leaderboard", "").trim();
            if (!lb.isEmpty()) {
                for (String s : lb.split(",")) {
                    try {
                        Leaderboard.add(Integer.parseInt(s.trim()));
                    } catch (NumberFormatException ignored) {
                        // skip bad entries
                    }
                }
                Leaderboard.sort(Comparator.reverseOrder());
                if (Leaderboard.size() > 5) {
                    Leaderboard = new ArrayList<>(Leaderboard.subList(0, 5));
                }
            }
        } catch (IOException e) {
            highScore = Math.max(highScore, 0);
            if (Leaderboard == null) Leaderboard = new ArrayList<>(5);
        }
    }

    // Lưu điểm số vào file
    private void saveScoresToFile() {
        try {
            ensureFileExists();
            Properties p = new Properties();
            p.setProperty("highScore", String.valueOf(highScore));
            String lb = Leaderboard.stream()
                    .sorted(Comparator.reverseOrder())
                    .limit(5)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            p.setProperty("leaderboard", lb);
            try (OutputStream os = Files.newOutputStream(
                    SCORE_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                p.store(os, "Arkanoid scores");
            }
        } catch (IOException ignored) {
            // ignore write errors to avoid crashing the game
        }
    }
}
