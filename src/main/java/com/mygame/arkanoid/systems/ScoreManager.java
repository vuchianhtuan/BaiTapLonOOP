package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.objects.BackButton;

import java.awt.*;
// Cần các import này cho xử lý file
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException; // Thêm import này
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*; // Import đầy đủ java.util
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ScoreManager {
    private final GameManager gameManager;
    private InputHandler inputHandler;

    // --- Dữ liệu lưu trữ ---
    private int highScore; // Điểm cao nhất MỘT LẦN CHƠI
    private long fastestTime = Long.MAX_VALUE; // Thời gian nhanh nhất HOÀN THÀNH GAME

    private List<Integer> topScores = new ArrayList<>(6); // Top 5 điểm
    private List<Long> topTimes = new ArrayList<>(6); // Top 5 thời gian (WIN)

    // Dữ liệu theo từng màn
    private Map<Integer, Integer> perLevelHighScores;
    private Map<Integer, Long> perLevelFastestTimes;
    private int totalLevels = 3;

    private Image BackgroundImage;
    private BackButton backButton;

    // Biến tạm cho session hiện tại (sẽ bị xóa vì không cần)
    // private int currentSessionScore = 0;
    // private long currentSessionTime = 0;


    private static final Path SCORE_FILE = resolveResourceBackedScoreFile();
    private static final Path SCORE_DIR = (SCORE_FILE != null) ? SCORE_FILE.getParent() : null; // Kiểm tra null

    public ScoreManager(GameManager gameManager, InputHandler inputHandler) {
        this.highScore = 0;
        this.gameManager = gameManager;
        this.inputHandler = inputHandler;
        BackgroundImage = AssetManager.getInstance().getImage("scoreBackground");

        this.topScores = new ArrayList<>(6);
        this.topTimes = new ArrayList<>(6);
        this.perLevelHighScores = new HashMap<>();
        this.perLevelFastestTimes = new HashMap<>();

        loadScoresFromFile(); // Tải dữ liệu đã lưu
        backButton = new BackButton(10, 10, 40, 40);
    }

    /**
     * Xử lý kết quả khi kết thúc một lượt chơi (Game Over hoặc Game Win).
     * @param finalScore Điểm cuối cùng của lượt chơi.
     * @param totalTime Tổng thời gian chơi của lượt đó (milliseconds).
     * @param didWin true nếu người chơi thắng game, false nếu thua.
     */
    public synchronized void submitSessionResult(int finalScore, long totalTime, boolean didWin) {
        boolean changed = false;

        // 1. Cập nhật High Score (Điểm cao nhất mọi thời đại)
        if (finalScore > highScore) {
            highScore = finalScore;
            changed = true;
        }

        // 2. Thêm điểm vào Top 5 Scores
        if (finalScore > 0) {
            topScores.add(finalScore);
            topScores.sort(Comparator.reverseOrder());
            while (topScores.size() > 5) { // Dùng while để xóa nhiều nếu cần
                topScores.remove(5);
            }
            // Không cần set changed = true ở đây, sẽ check ở cuối
        }


        // 3. Chỉ cập nhật thời gian nếu người chơi THẮNG và có thời gian hợp lệ
        if (didWin && totalTime > 0) {
            // Cập nhật Fastest Time (Thời gian nhanh nhất mọi thời đại)
            if (totalTime < fastestTime) {
                fastestTime = totalTime;
                changed = true;
            }

            // Thêm thời gian vào Top 5 Times
            topTimes.add(totalTime);
            topTimes.sort(Comparator.naturalOrder());
            while (topTimes.size() > 5) { // Dùng while
                topTimes.remove(5);
            }
            // Không cần set changed = true ở đây
        }

        // Chỉ lưu nếu có thay đổi thực sự trong kỷ lục hoặc danh sách top
        // (Kiểm tra xem điểm/thời gian mới có thực sự vào top 5 không)
        // Cách đơn giản là luôn lưu khi gọi hàm này
        saveScoresToFile();
    }


    public int getHighScore() { return highScore; }
    public long getFastestTime() { return fastestTime; }
    public List<Integer> getTopScores() { return new ArrayList<>(topScores); }
    public List<Long> getTopTimes() { return new ArrayList<>(topTimes); }


    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();
        if (inputHandler.isMouseClicked() && backButton.contains(virtualMouseX, virtualMouseY)) {
            gameManager.setGameState("MENU");
        }
    }

    /**
     * Gửi kết quả của một màn chơi (level) để lưu lại
     */
    public synchronized void submitLevelResult(int levelIndex, int levelScore, long levelTime) {
        if (levelIndex < 0 || levelIndex >= totalLevels) return;
        boolean changed = false;
        int currentBestScore = perLevelHighScores.getOrDefault(levelIndex, 0);
        if (levelScore > currentBestScore) {
            perLevelHighScores.put(levelIndex, levelScore);
            changed = true;
        }
        long currentBestTime = perLevelFastestTimes.getOrDefault(levelIndex, Long.MAX_VALUE);
        if (levelTime < currentBestTime && levelTime > 0) {
            perLevelFastestTimes.put(levelIndex, levelTime);
            changed = true;
        }
        if (changed) {
            saveScoresToFile();
        }
    }

    public int getBestScoreForLevel(int levelIndex) {
        return perLevelHighScores.getOrDefault(levelIndex, 0);
    }
    public long getFastestTimeForLevel(int levelIndex) {
        return perLevelFastestTimes.getOrDefault(levelIndex, Long.MAX_VALUE);
    }

    /**
     * Helper function to format milliseconds into MM:SS.sss
     */
    private String formatTime(long millis) {
        if (millis == Long.MAX_VALUE || millis <= 0) {
            return "--:--.---";
        }
        long totalSeconds = millis / 1000;
        long milliseconds = millis % 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
    }


    // --- HÀM RENDER ĐƯỢC THIẾT KẾ LẠI ---
    // KHÔNG CÓ @Override ở đây
    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Vẽ nền
        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage,
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT),
                    null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        // 2. Chuẩn bị Font
        Font titleFont = new Font("Arial", Font.BOLD, 48);
        Font recordFont = new Font("Arial", Font.BOLD, 28);
        Font listFont = new Font("Arial", Font.PLAIN, 24);

        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));
        Font scaledRecordFont = recordFont.deriveFont((float)(recordFont.getSize() * sm.getScale()));
        Font scaledListFont = listFont.deriveFont((float)(listFont.getSize() * sm.getScale()));

        g2d.setColor(Color.WHITE);

        // 3. Vẽ Tiêu đề
        g2d.setFont(scaledTitleFont);
        String title = "HIGH SCORES";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g2d.drawString(title, sm.scaleX((sm.NATIVE_WIDTH - titleWidth) / 2), sm.scaleY(80));

        // 4. Vẽ Kỷ lục
        g2d.setFont(scaledRecordFont);
        FontMetrics fmRecord = g2d.getFontMetrics();

        String bestScoreText = "Best Score: " + highScore;
        int bestScoreWidth = fmRecord.stringWidth(bestScoreText);
        g2d.drawString(bestScoreText, sm.scaleX((sm.NATIVE_WIDTH - bestScoreWidth) / 2), sm.scaleY(140));

        String fastestTimeText = "Fastest Win: " + formatTime(fastestTime);
        int fastestTimeWidth = fmRecord.stringWidth(fastestTimeText);
        g2d.drawString(fastestTimeText, sm.scaleX((sm.NATIVE_WIDTH - fastestTimeWidth) / 2), sm.scaleY(180));

        // 5. Vẽ 2 cột: Top 5 Scores và Top 5 Times
        g2d.setFont(scaledListFont);
        FontMetrics fmList = g2d.getFontMetrics();
        int listStartY = 260;
        int lineHeight = fmList.getHeight() + sm.scaleHeight(10); // Khoảng cách dòng

        // --- Cột Top Scores ---
        int scoreColXLogic = sm.NATIVE_WIDTH / 4; // Tọa độ logic X cột điểm
        String scoreTitle = "Top 5 Scores";
        int scoreTitleWidth = fmList.stringWidth(scoreTitle);
        g2d.drawString(scoreTitle, sm.scaleX(scoreColXLogic) - scoreTitleWidth / 2, sm.scaleY(listStartY - lineHeight)); // Tiêu đề cột
        for (int i = 0; i < topScores.size(); i++) {
            String entryText = String.format("%d. %d", i + 1, topScores.get(i));
            int entryWidth = fmList.stringWidth(entryText);
            g2d.drawString(entryText, sm.scaleX(scoreColXLogic) - entryWidth / 2, sm.scaleY(listStartY + i * lineHeight));
        }
        // Vẽ thêm dòng trống nếu ít hơn 5 điểm
        for (int i = topScores.size(); i < 5; i++) {
            String entryText = String.format("%d. ---", i + 1);
            int entryWidth = fmList.stringWidth(entryText);
            g2d.drawString(entryText, sm.scaleX(scoreColXLogic) - entryWidth / 2, sm.scaleY(listStartY + i * lineHeight));
        }

        // --- Cột Top Times ---
        int timeColXLogic = sm.NATIVE_WIDTH * 3 / 4; // Tọa độ logic X cột thời gian
        String timeTitle = "Top 5 Times (Win)";
        int timeTitleWidth = fmList.stringWidth(timeTitle);
        g2d.drawString(timeTitle, sm.scaleX(timeColXLogic) - timeTitleWidth / 2, sm.scaleY(listStartY - lineHeight)); // Tiêu đề cột
        for (int i = 0; i < topTimes.size(); i++) {
            String entryText = String.format("%d. %s", i + 1, formatTime(topTimes.get(i)));
            int entryWidth = fmList.stringWidth(entryText);
            g2d.drawString(entryText, sm.scaleX(timeColXLogic) - entryWidth / 2, sm.scaleY(listStartY + i * lineHeight));
        }
        // Vẽ thêm dòng trống nếu ít hơn 5 thời gian
        for (int i = topTimes.size(); i < 5; i++) {
            String entryText = String.format("%d. --:--.---", i + 1);
            int entryWidth = fmList.stringWidth(entryText);
            g2d.drawString(entryText, sm.scaleX(timeColXLogic) - entryWidth / 2, sm.scaleY(listStartY + i * lineHeight));
        }


        // 6. Vẽ nút Back
        backButton.draw(g, sm);
    }


    // --- resolveResourceBackedScoreFile() ĐÃ SỬA ---
    private static Path resolveResourceBackedScoreFile() {
        // Ưu tiên 1: Thư mục người dùng (ổn định nhất)
        try {
            Path homeDir = Paths.get(System.getProperty("user.home"), ".arkanoidGame"); // Đặt tên thư mục rõ ràng hơn
            // Kiểm tra hoặc tạo thư mục
            if (Files.notExists(homeDir)) {
                try {
                    Files.createDirectories(homeDir);
                } catch (IOException e) {
                    System.err.println("Không thể tạo thư mục lưu điểm trong thư mục người dùng: " + homeDir + " | " + e.getMessage());
                    // Chuyển sang thử vị trí khác nếu không tạo được
                }
            }
            // Kiểm tra quyền ghi
            if (Files.isDirectory(homeDir) && Files.isWritable(homeDir)) {
                return homeDir.resolve("scores.properties");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi truy cập thư mục người dùng: " + e.getMessage());
        }


        // Ưu tiên 2: Thư mục làm việc hiện tại (cho phát triển)
        try {
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            // Chỉ dùng nếu có thể ghi
            if (Files.isDirectory(currentDir) && Files.isWritable(currentDir)) {
                // Có thể tạo thư mục con 'data' nếu muốn
                // Path dataDir = currentDir.resolve("data");
                // if (Files.notExists(dataDir)) Files.createDirectories(dataDir);
                // if (Files.isDirectory(dataDir) && Files.isWritable(dataDir)) {
                //     return dataDir.resolve("scores.properties");
                // }
                // Hoặc lưu trực tiếp vào thư mục hiện tại:
                return currentDir.resolve("scores.properties");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi truy cập thư mục hiện tại: " + e.getMessage());
        }


        // Ưu tiên 3: Vị trí classpath (thường chỉ đọc được khi đóng gói)
        // Bỏ qua việc ghi vào đây vì thường không khả thi
        /*
        try {
            URL url = ScoreManager.class.getClassLoader().getResource("");
            if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
                Path classesDir = Paths.get(url.toURI());
                // Rất hiếm khi thư mục classes có thể ghi được sau khi build
                if (Files.isDirectory(classesDir) && Files.isWritable(classesDir)) {
                    // return classesDir.resolve("scores.properties"); // Không nên dùng
                }
            }
        } catch (URISyntaxException | SecurityException e) { // Bắt các exception cụ thể
            System.err.println("Lỗi khi truy cập classpath: " + e.getMessage());
        }
        */

        // Nếu tất cả thất bại
        System.err.println("Không tìm thấy vị trí phù hợp để lưu file điểm số.");
        return null; // Trả về null nếu không tìm được vị trí
    }

    // --- ensureFileExists() ĐÃ SỬA ---
    private void ensureFileExists() throws IOException {
        if (SCORE_FILE == null || SCORE_DIR == null) {
            throw new IOException("Không thể xác định đường dẫn lưu điểm.");
        }
        // Kiểm tra và tạo thư mục cha nếu cần
        if (Files.notExists(SCORE_DIR)) {
            Files.createDirectories(SCORE_DIR);
        }
        // Kiểm tra và tạo file nếu cần
        if (Files.notExists(SCORE_FILE)) {
            Properties p = new Properties();
            p.setProperty("highScore", "0");
            p.setProperty("fastestTime", String.valueOf(Long.MAX_VALUE));
            p.setProperty("topScores", "");
            p.setProperty("topTimes", "");
            for (int i = 0; i < totalLevels; i++) {
                p.setProperty("level." + i + ".score", "0");
                p.setProperty("level." + i + ".time", String.valueOf(Long.MAX_VALUE));
            }
            try (OutputStream os = Files.newOutputStream(
                    SCORE_FILE,
                    StandardOpenOption.CREATE, // Chỉ tạo nếu chưa có
                    StandardOpenOption.WRITE)) {
                p.store(os, "Arkanoid scores");
            }
        }
    }

    // --- loadScoresFromFile() ĐÃ SỬA ---
    private void loadScoresFromFile() {
        if (SCORE_FILE == null) {
            System.err.println("Không thể tải điểm do đường dẫn file không hợp lệ.");
            initializeDefaultScores(); // Khởi tạo giá trị mặc định trong bộ nhớ
            return;
        }
        try {
            ensureFileExists(); // Đảm bảo file tồn tại
            Properties p = new Properties();
            try (InputStream is = Files.newInputStream(SCORE_FILE, StandardOpenOption.READ)) {
                p.load(is);
            }

            // Load HighScore tổng
            try { highScore = Integer.parseInt(p.getProperty("highScore", "0").trim()); }
            catch (NumberFormatException ignored) { highScore = 0; }

            // Load FastestTime tổng
            try { fastestTime = Long.parseLong(p.getProperty("fastestTime", String.valueOf(Long.MAX_VALUE)).trim()); }
            catch (NumberFormatException ignored) { fastestTime = Long.MAX_VALUE; }

            // Load Top 5 Scores
            topScores.clear();
            String ts = p.getProperty("topScores", "").trim();
            if (!ts.isEmpty()) {
                for (String s : ts.split(",")) {
                    try { topScores.add(Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {}
                }
                topScores.sort(Comparator.reverseOrder());
                while (topScores.size() > 5) { topScores.remove(5); }
            }

            // Load Top 5 Times
            topTimes.clear();
            String tt = p.getProperty("topTimes", "").trim();
            if (!tt.isEmpty()) {
                for (String s : tt.split(",")) {
                    try { topTimes.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
                }
                topTimes.sort(Comparator.naturalOrder());
                while (topTimes.size() > 5) { topTimes.remove(5); }
            }

            // Load điểm/thời gian từng màn
            perLevelHighScores.clear();
            perLevelFastestTimes.clear();
            for (int i = 0; i < totalLevels; i++) {
                try {
                    int score = Integer.parseInt(p.getProperty("level." + i + ".score", "0"));
                    perLevelHighScores.put(i, score);
                    long time = Long.parseLong(p.getProperty("level." + i + ".time", String.valueOf(Long.MAX_VALUE)));
                    perLevelFastestTimes.put(i, time);
                } catch(NumberFormatException ignored) {
                    perLevelHighScores.put(i, 0);
                    perLevelFastestTimes.put(i, Long.MAX_VALUE);
                }
            }

        } catch (IOException e) {
            System.err.println("Lỗi khi tải điểm từ file: " + SCORE_FILE + " | " + e.getMessage());
            initializeDefaultScores(); // Khởi tạo mặc định nếu đọc file lỗi
        }
    }

    // --- THÊM HÀM MỚI: Khởi tạo điểm mặc định ---
    private void initializeDefaultScores() {
        highScore = 0;
        fastestTime = Long.MAX_VALUE;
        topScores.clear();
        topTimes.clear();
        perLevelHighScores.clear();
        perLevelFastestTimes.clear();
        for (int i = 0; i < totalLevels; i++) {
            perLevelHighScores.put(i, 0);
            perLevelFastestTimes.put(i, Long.MAX_VALUE);
        }
        System.out.println("Đã khởi tạo điểm số mặc định do lỗi file.");
    }


    // --- saveScoresToFile() ĐÃ SỬA ---
    private void saveScoresToFile() {
        if (SCORE_FILE == null) {
            System.err.println("Không thể lưu điểm do đường dẫn file không hợp lệ.");
            return;
        }
        try {
            // Không cần ensureFileExists() nữa vì nó được gọi trong load và submit
            Properties p = new Properties();

            // Lưu HighScore và FastestTime tổng
            p.setProperty("highScore", String.valueOf(highScore));
            p.setProperty("fastestTime", String.valueOf(fastestTime));

            // Lưu Top 5 Scores
            String ts = topScores.stream().limit(5).map(String::valueOf).collect(Collectors.joining(","));
            p.setProperty("topScores", ts);

            // Lưu Top 5 Times
            String tt = topTimes.stream().limit(5).map(String::valueOf).collect(Collectors.joining(","));
            p.setProperty("topTimes", tt);

            // Lưu điểm/thời gian từng màn
            for (int i = 0; i < totalLevels; i++) {
                p.setProperty("level." + i + ".score", String.valueOf(perLevelHighScores.getOrDefault(i, 0)));
                p.setProperty("level." + i + ".time", String.valueOf(perLevelFastestTimes.getOrDefault(i, Long.MAX_VALUE)));
            }

            // Ghi vào file
            try (OutputStream os = Files.newOutputStream(
                    SCORE_FILE,
                    StandardOpenOption.CREATE, // Tạo nếu chưa có
                    StandardOpenOption.TRUNCATE_EXISTING, // Ghi đè nội dung cũ
                    StandardOpenOption.WRITE)) {
                p.store(os, "Arkanoid scores");
                // System.out.println("Đã lưu điểm vào: " + SCORE_FILE); // Debug
            }
        } catch (IOException e) {
            System.err.println("Lỗi nghiêm trọng khi lưu điểm vào file: " + SCORE_FILE + " | " + e.getMessage());
            // Có thể hiển thị thông báo lỗi cho người dùng ở đây
        }
    }
}