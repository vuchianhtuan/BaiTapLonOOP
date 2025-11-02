package com.mygame.arkanoid.ui.screens;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.ui.controls.BackButton;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

// QUẢN LÝ ĐIỂM SỐ VÀ THỜI GIAN CHƠI
public class ScoreManager {
    private final GameManager gameManager;
    private InputHandler inputHandler;

    private int highScore; // Điểm cao nhất MỘT LẦN CHƠI
    private long fastestTime = Long.MAX_VALUE; // Thời gian nhanh nhất HOÀN THÀNH GAME

    private List<Integer> topScores = new ArrayList<>(6); // Top 5 điểm
    private List<Long> topTimes = new ArrayList<>(6); // Top 5 thời gian (WIN)
    private Map<Integer, Integer> perLevelHighScores;
    private Map<Integer, Long> perLevelFastestTimes;
    private int totalLevels = 3;

    private Image BackgroundImage;
    private BackButton backButton;

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

        saveScoresToFile();
    }


    public int getHighScore() { return highScore; }
    public long getFastestTime() { return fastestTime; }
    public List<Integer> getTopScores() { return new ArrayList<>(topScores); }
    public List<Long> getTopTimes() { return new ArrayList<>(topTimes); }

    /**
     * Hàm này sẽ được gọi BÊN TRONG GameManager.updateGame() khi ở state "HIGH_SCORES".
     */
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

    /**
     * Lấy điểm cao nhất của một màn chơi cụ thể.
     * @param levelIndex
     * @return
     */
    public int getBestScoreForLevel(int levelIndex) {
        return perLevelHighScores.getOrDefault(levelIndex, 0);
    }
    public long getFastestTimeForLevel(int levelIndex) {
        return perLevelFastestTimes.getOrDefault(levelIndex, Long.MAX_VALUE);
    }

    /**
     * Định dạng thời gian từ milliseconds sang định dạng mm:ss.SSS.
     * @param millis
     * @return
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

    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g;

        // Bật khử răng cưa
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Vẽ nền
        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage,
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT),
                    null);
        } else { // Vẽ nền đen dự phòng nếu ảnh lỗi
            g.setColor(Color.BLACK);
            g.fillRect(
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        // --- THÊM: Màu nền cho panel chữ ---
        Color panelColor = new Color(50, 50, 50, 200); // Màu xám đậm, bán trong suốt (alpha=200)
        int panelArc = 20; // Độ bo tròn góc panel
        int panelPadding = 15; // Khoảng cách từ chữ đến mép panel (logic)
        // --- KẾT THÚC THÊM ---

        // 2. Chuẩn bị Font
        Font titleFont = new Font("Arial", Font.BOLD, 48);
        Font recordFont = new Font("Arial", Font.BOLD, 28);
        Font listFont = new Font("Arial", Font.PLAIN, 24);

        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));
        Font scaledRecordFont = recordFont.deriveFont((float)(recordFont.getSize() * sm.getScale()));
        Font scaledListFont = listFont.deriveFont((float)(listFont.getSize() * sm.getScale()));

        // Lưu composite mặc định
        Composite defaultComposite = g2d.getComposite();

        // 3. Vẽ Tiêu đề và Panel nền
        g2d.setFont(scaledTitleFont);
        g2d.setColor(Color.WHITE);
        String title = "HIGH SCORES";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        int titleHeight = fmTitle.getHeight();
        int titleAscent = fmTitle.getAscent();
        int titleLogicX = (sm.NATIVE_WIDTH - titleWidth) / 2;
        int titleLogicY = 80;

        // Vẽ panel nền cho tiêu đề
        int titlePanelX = titleLogicX - panelPadding;
        int titlePanelY = titleLogicY - titleAscent - panelPadding; // Căn Y dựa vào ascent
        int titlePanelW = titleWidth + 2 * panelPadding;
        int titlePanelH = titleHeight + 2 * panelPadding;
        g2d.setColor(panelColor);
        g2d.fillRoundRect(sm.scaleX(titlePanelX), sm.scaleY(titlePanelY),
                sm.scaleWidth(titlePanelW), sm.scaleHeight(titlePanelH),
                sm.scaleWidth(panelArc), sm.scaleHeight(panelArc));
        // Vẽ chữ tiêu đề
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, sm.scaleX(titleLogicX), sm.scaleY(titleLogicY));


        // 4. Vẽ Kỷ lục và Panel nền
        g2d.setFont(scaledRecordFont);
        FontMetrics fmRecord = g2d.getFontMetrics();
        int recordHeight = fmRecord.getHeight();
        int recordAscent = fmRecord.getAscent();

        String bestScoreText = "Best Score: " + highScore;
        int bestScoreWidth = fmRecord.stringWidth(bestScoreText);
        int bestScoreLogicX = (sm.NATIVE_WIDTH - bestScoreWidth) / 2;
        int bestScoreLogicY = 160; // Dịch xuống

        String fastestTimeText = "Fastest Win: " + formatTime(fastestTime);
        int fastestTimeWidth = fmRecord.stringWidth(fastestTimeText);
        int fastestTimeLogicX = (sm.NATIVE_WIDTH - fastestTimeWidth) / 2;
        int fastestTimeLogicY = bestScoreLogicY + recordHeight + sm.scaleHeight(10); // Dưới dòng điểm

        // Tính kích thước panel chung cho 2 dòng kỷ lục
        int recordPanelW = Math.max(bestScoreWidth, fastestTimeWidth) + 2 * panelPadding;
        int recordPanelH = (recordHeight + sm.scaleHeight(10)) * 2 + 2 * panelPadding; // 2 dòng + padding
        int recordPanelX = (sm.NATIVE_WIDTH - recordPanelW) / 2;
        int recordPanelY = bestScoreLogicY - recordAscent - panelPadding;

        // Vẽ panel nền kỷ lục
        g2d.setColor(panelColor);
        g2d.fillRoundRect(sm.scaleX(recordPanelX), sm.scaleY(recordPanelY),
                sm.scaleWidth(recordPanelW), sm.scaleHeight(recordPanelH),
                sm.scaleWidth(panelArc), sm.scaleHeight(panelArc));
        // Vẽ chữ kỷ lục
        g2d.setColor(Color.WHITE);
        g2d.drawString(bestScoreText, sm.scaleX(bestScoreLogicX), sm.scaleY(bestScoreLogicY));
        g2d.drawString(fastestTimeText, sm.scaleX(fastestTimeLogicX), sm.scaleY(fastestTimeLogicY));


        // 5. Vẽ 2 cột danh sách và Panel nền
        g2d.setFont(scaledListFont);
        FontMetrics fmList = g2d.getFontMetrics();
        int listStartY = 280; // Dịch danh sách xuống
        int listTitleY = listStartY - fmList.getHeight() - sm.scaleHeight(5); // Vị trí tiêu đề cột
        int lineHeight = fmList.getHeight() + sm.scaleHeight(10);
        int listPanelH = lineHeight * 6 + 2 * panelPadding; // Panel cao đủ cho tiêu đề + 5 dòng + padding
        int listPanelArc = 15; // Bo tròn ít hơn

        // --- Cột Top Scores ---
        int scoreColXLogic = sm.NATIVE_WIDTH / 4; // Tọa độ logic X cột điểm
        String scoreTitle = "Top 5 Scores";
        int scoreTitleWidth = fmList.stringWidth(scoreTitle);
        // Tính kích thước panel cột điểm
        int scorePanelW = scoreTitleWidth + 4 * panelPadding; // Rộng hơn 1 chút
        int scorePanelX = scoreColXLogic - scorePanelW / 2;
        int scorePanelY = listTitleY - fmList.getAscent() - panelPadding;
        // Vẽ panel nền cột điểm
        g2d.setColor(panelColor);
        g2d.fillRoundRect(sm.scaleX(scorePanelX), sm.scaleY(scorePanelY),
                sm.scaleWidth(scorePanelW), sm.scaleHeight(listPanelH),
                sm.scaleWidth(listPanelArc), sm.scaleHeight(listPanelArc));
        // Vẽ tiêu đề cột điểm
        g2d.setColor(Color.ORANGE); // Màu khác cho tiêu đề cột
        g2d.drawString(scoreTitle, sm.scaleX(scoreColXLogic) - scoreTitleWidth / 2, sm.scaleY(listTitleY));
        // Vẽ danh sách điểm
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) { // Luôn vẽ 5 dòng
            String entryText;
            if (i < topScores.size()) {
                entryText = String.format("%d. %d", i + 1, topScores.get(i));
            } else {
                entryText = String.format("%d. ---", i + 1);
            }
            int entryWidth = fmList.stringWidth(entryText);
            g2d.drawString(entryText, sm.scaleX(scoreColXLogic) - entryWidth / 2, sm.scaleY(listStartY + i * lineHeight));
        }

        // --- Cột Top Times ---
        int timeColXLogic = sm.NATIVE_WIDTH * 3 / 4; // Tọa độ logic X cột thời gian
        String timeTitle = "Top 5 Times (Win)";
        int timeTitleWidth = fmList.stringWidth(timeTitle);
        // Tính kích thước panel cột thời gian
        int timePanelW = timeTitleWidth + 4 * panelPadding; // Rộng hơn 1 chút
        int timePanelX = timeColXLogic - timePanelW / 2;
        int timePanelY = listTitleY - fmList.getAscent() - panelPadding; // Cùng Y với panel điểm
        // Vẽ panel nền cột thời gian
        g2d.setColor(panelColor);
        g2d.fillRoundRect(sm.scaleX(timePanelX), sm.scaleY(timePanelY),
                sm.scaleWidth(timePanelW), sm.scaleHeight(listPanelH),
                sm.scaleWidth(listPanelArc), sm.scaleHeight(listPanelArc));
        // Vẽ tiêu đề cột thời gian
        g2d.setColor(Color.CYAN); // Màu khác cho tiêu đề cột
        g2d.drawString(timeTitle, sm.scaleX(timeColXLogic) - timeTitleWidth / 2, sm.scaleY(listTitleY));
        // Vẽ danh sách thời gian
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) { // Luôn vẽ 5 dòng
            String entryText;
            if (i < topTimes.size()) {
                entryText = String.format("%d. %s", i + 1, formatTime(topTimes.get(i)));
            } else {
                entryText = String.format("%d. --:--.---", i + 1);
            }
            int entryWidth = fmList.stringWidth(entryText);
            g2d.drawString(entryText, sm.scaleX(timeColXLogic) - entryWidth / 2, sm.scaleY(listStartY + i * lineHeight));
        }

        // 6. Vẽ nút Back
        backButton.draw(g, sm);

        // Reset composite về mặc định (quan trọng nếu dùng alpha)
        g2d.setComposite(defaultComposite);
    }

    /**
     * Xác định vị trí file lưu điểm dựa trên hệ thống.
     * @return Path đến file điểm hoặc null nếu không tìm được vị trí phù hợp.
     */
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
                return currentDir.resolve("scores.properties");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi truy cập thư mục hiện tại: " + e.getMessage());
        }

        // Nếu tất cả thất bại
        System.err.println("Không tìm thấy vị trí phù hợp để lưu file điểm số.");
        return null; // Trả về null nếu không tìm được vị trí
    }

    /**
     * Đảm bảo file điểm tồn tại, nếu không thì tạo mới với giá trị mặc định.
     * @throws IOException nếu không thể tạo file.
     */
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

    /**
     * Phương thức tải điểm từ file.
     */
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

    /**
     * Khởi tạo điểm số mặc định trong bộ nhớ khi file bị lỗi.
     */
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

    /**
     * Phương thức lưu điểm vào file.
     */
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