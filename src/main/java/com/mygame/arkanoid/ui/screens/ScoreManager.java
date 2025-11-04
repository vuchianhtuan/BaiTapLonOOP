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

/**
 * Quản lý logic, hiển thị (render) và lưu trữ (persistence)
 * tất cả các loại điểm số và thành tích của trò chơi.
 * <p>
 * Trách nhiệm bao gồm:
 * <ul>
 * <li>Theo dõi điểm cao nhất (High Score) và thời gian thắng nhanh nhất (Fastest Win).</li>
 * <li>Duy trì Top 5 điểm và Top 5 thời gian thắng.</li>
 * <li>Theo dõi điểm/thời gian tốt nhất cho từng màn chơi (level) riêng lẻ.</li>
 * <li>Xử lý việc lưu ({@link #saveScoresToFile}) và tải ({@link #loadScoresFromFile})
 * toàn bộ dữ liệu này vào một tệp {@code .properties} an toàn
 * trong thư mục người dùng.</li>
 * <li>Cung cấp giao diện (UI) để hiển thị các điểm số này ({@link #render}).</li>
 * <li>Xử lý input (nhấn nút Back) trong màn hình điểm số ({@link #update}).</li>
 * </ul>
 */
public class ScoreManager {
    private final GameManager gameManager;
    private InputHandler inputHandler;

    // --- Biến lưu trữ điểm (trong bộ nhớ) ---
    private int highScore; // Điểm cao nhất MỘT LẦN CHƠI
    private long fastestTime = Long.MAX_VALUE; // Thời gian nhanh nhất HOÀN THÀNH GAME

    private List<Integer> topScores = new ArrayList<>(6); // Top 5 điểm
    private List<Long> topTimes = new ArrayList<>(6); // Top 5 thời gian (WIN)
    private Map<Integer, Integer> perLevelHighScores;
    private Map<Integer, Long> perLevelFastestTimes;
    private int totalLevels = 3; // Tổng số level, dùng để lặp khi lưu/tải

    // --- Biến UI ---
    private Image BackgroundImage;
    private BackButton backButton;

    // --- Biến hệ thống file (File System) ---
    /** Đường dẫn (Path) động đến tệp lưu điểm (ví dụ: ".../.arkanoidGame/scores.properties"). */
    private static final Path SCORE_FILE = resolveResourceBackedScoreFile();
    /** Đường dẫn (Path) đến thư mục chứa tệp điểm. */
    private static final Path SCORE_DIR = (SCORE_FILE != null) ? SCORE_FILE.getParent() : null;

    /**
     * Khởi tạo trình quản lý điểm số.
     * <p>
     * Tải (load) điểm số đã lưu từ tệp tin ngay lập tức.
     * Khởi tạo nút "Back" cho UI.
     *
     * @param gameManager  Tham chiếu đến GameManager (để chuyển trạng thái).
     * @param inputHandler Tham chiếu đến InputHandler (để kiểm tra click chuột).
     */
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
        backButton = new BackButton(10, 10, 40, 40); // Tọa độ logic (native)
    }

    /**
     * Gửi (submit) kết quả của một phiên (session) chơi
     * (khi Game Over hoặc Game Win).
     * <p>
     * So sánh kết quả mới với các kỷ lục (record) hiện tại
     * (High Score, Fastest Time, Top 5) và cập nhật nếu cần.
     * Tự động gọi {@link #saveScoresToFile()} nếu có thay đổi.
     *
     * @param finalScore Điểm cuối cùng của lượt chơi.
     * @param totalTime  Tổng thời gian chơi của lượt đó (milliseconds).
     * @param didWin     {@code true} nếu người chơi thắng game
     * (chỉ khi thắng mới xét Fastest Time).
     */
    public synchronized void submitSessionResult(int finalScore, long totalTime, boolean didWin) {
        // (Biến 'changed' không còn cần thiết vì saveScoresToFile()
        // được gọi một lần ở cuối)

        // 1. Cập nhật High Score (Điểm cao nhất mọi thời đại)
        if (finalScore > highScore) {
            highScore = finalScore;
        }

        // 2. Thêm điểm vào Top 5 Scores
        if (finalScore > 0) {
            topScores.add(finalScore);
            topScores.sort(Comparator.reverseOrder()); // Sắp xếp giảm dần
            while (topScores.size() > 5) { // Giữ lại 5 điểm cao nhất
                topScores.remove(5);
            }
        }

        // 3. Chỉ cập nhật thời gian nếu người chơi THẮNG và có thời gian hợp lệ
        if (didWin && totalTime > 0) {
            // Cập nhật Fastest Time (Thời gian nhanh nhất mọi thời đại)
            if (totalTime < fastestTime) {
                fastestTime = totalTime;
            }

            // Thêm thời gian vào Top 5 Times
            topTimes.add(totalTime);
            topTimes.sort(Comparator.naturalOrder()); // Sắp xếp tăng dần
            while (topTimes.size() > 5) { // Giữ lại 5 thời gian nhanh nhất
                topTimes.remove(5);
            }
        }

        // 4. Lưu tất cả thay đổi vào file
        saveScoresToFile();
    }

    public int getHighScore() { return highScore; }
    public long getFastestTime() { return fastestTime; }
    public List<Integer> getTopScores() { return new ArrayList<>(topScores); }
    public List<Long> getTopTimes() { return new ArrayList<>(topTimes); }

    /**
     * Cập nhật logic của màn hình High Scores
     * (được gọi mỗi frame bởi GameManager
     * khi {@code gameState == "HIGH_SCORES"}).
     * <p>
     * Chỉ kiểm tra xem người dùng có click vào nút "Back" hay không.
     */
    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();
        if (inputHandler.isMouseClicked() && backButton.contains(virtualMouseX, virtualMouseY)) {
            gameManager.setGameState("MENU"); // Quay về Menu
        }
    }

    /**
     * Gửi (submit) kết quả của một màn chơi (level) riêng lẻ
     * (khi vừa hoàn thành level).
     * <p>
     * So sánh điểm/thời gian của màn này với kỷ lục (record)
     * của chính màn đó.
     * Tự động gọi {@link #saveScoresToFile()} nếu có kỷ lục mới.
     *
     * @param levelIndex Chỉ số (index) của màn vừa hoàn thành.
     * @param levelScore Điểm số đạt được trong màn đó.
     * @param levelTime  Thời gian hoàn thành màn đó (milliseconds).
     */
    public synchronized void submitLevelResult(int levelIndex, int levelScore, long levelTime) {
        if (levelIndex < 0 || levelIndex >= totalLevels) return; // Bỏ qua nếu index không hợp lệ
        boolean changed = false;

        // Cập nhật điểm cao nhất của màn
        int currentBestScore = perLevelHighScores.getOrDefault(levelIndex, 0);
        if (levelScore > currentBestScore) {
            perLevelHighScores.put(levelIndex, levelScore);
            changed = true;
        }

        // Cập nhật thời gian nhanh nhất của màn (chỉ khi thời gian > 0)
        long currentBestTime = perLevelFastestTimes.getOrDefault(levelIndex, Long.MAX_VALUE);
        if (levelTime < currentBestTime && levelTime > 0) {
            perLevelFastestTimes.put(levelIndex, levelTime);
            changed = true;
        }

        // Chỉ lưu vào file nếu có thay đổi
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
     * Định dạng thời gian từ mili-giây sang chuỗi "mm:ss.SSS"
     * (phút:giây.mili-giây).
     *
     * @param millis Tổng số mili-giây.
     * @return Chuỗi đã định dạng, hoặc "--:--.---" nếu giá trị không hợp lệ.
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

    /**
     * Vẽ (render) toàn bộ màn hình High Scores.
     * <p>
     * Chịu trách nhiệm vẽ nền, các panel (bo góc, bán trong suốt)
     * và tất cả văn bản (Tiêu đề, Kỷ lục, Top 5) đã được
     * co giãn (scale) và căn chỉnh (align).
     *
     * @param g Đối tượng Graphics (sẽ được cast sang Graphics2D).
     */
    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g;

        // Bật khử răng cưa (anti-aliasing)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Vẽ nền
        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage,
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT),
                    null);
        } else { // Fallback: nền đen
            g.setColor(Color.BLACK);
            g.fillRect(
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        // --- Các thiết lập chung cho Panel UI ---
        Color panelColor = new Color(50, 50, 50, 200); // Xám đậm, bán trong suốt
        int panelArc = 20; // Độ bo tròn góc (logic)
        int panelPadding = 15; // Padding (logic)

        // 2. Chuẩn bị Fonts (gốc, chưa scale)
        Font titleFont = new Font("Arial", Font.BOLD, 48);
        Font recordFont = new Font("Arial", Font.BOLD, 28);
        Font listFont = new Font("Arial", Font.PLAIN, 24);

        // Lấy các phiên bản font đã scale
        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));
        Font scaledRecordFont = recordFont.deriveFont((float)(recordFont.getSize() * sm.getScale()));
        Font scaledListFont = listFont.deriveFont((float)(listFont.getSize() * sm.getScale()));

        Composite defaultComposite = g2d.getComposite(); // Lưu lại composite mặc định

        // --- 3. Vẽ Tiêu đề "HIGH SCORES" và Panel nền của nó ---
        g2d.setFont(scaledTitleFont);
        g2d.setColor(Color.WHITE);
        String title = "HIGH SCORES";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        int titleHeight = fmTitle.getHeight();
        int titleAscent = fmTitle.getAscent();
        int titleLogicX = (sm.NATIVE_WIDTH - titleWidth) / 2; // Căn giữa logic
        int titleLogicY = 80;

        // Tính toán panel nền cho tiêu đề
        int titlePanelX = titleLogicX - panelPadding;
        int titlePanelY = titleLogicY - titleAscent - panelPadding; // Căn Y dựa vào ascent
        int titlePanelW = titleWidth + 2 * panelPadding;
        int titlePanelH = titleHeight + 2 * panelPadding;
        // Vẽ panel nền (bo góc)
        g2d.setColor(panelColor);
        g2d.fillRoundRect(sm.scaleX(titlePanelX), sm.scaleY(titlePanelY),
                sm.scaleWidth(titlePanelW), sm.scaleHeight(titlePanelH),
                sm.scaleWidth(panelArc), sm.scaleHeight(panelArc));
        // Vẽ chữ tiêu đề
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, sm.scaleX(titleLogicX), sm.scaleY(titleLogicY));


        // --- 4. Vẽ Kỷ lục (Best Score / Fastest Win) và Panel nền ---
        g2d.setFont(scaledRecordFont);
        FontMetrics fmRecord = g2d.getFontMetrics();
        int recordHeight = fmRecord.getHeight();
        int recordAscent = fmRecord.getAscent();

        // Chuẩn bị văn bản kỷ lục
        String bestScoreText = "Best Score: " + highScore;
        int bestScoreWidth = fmRecord.stringWidth(bestScoreText);
        int bestScoreLogicX = (sm.NATIVE_WIDTH - bestScoreWidth) / 2; // Căn giữa
        int bestScoreLogicY = 160;

        String fastestTimeText = "Fastest Win: " + formatTime(fastestTime);
        int fastestTimeWidth = fmRecord.stringWidth(fastestTimeText);
        int fastestTimeLogicX = (sm.NATIVE_WIDTH - fastestTimeWidth) / 2; // Căn giữa
        int fastestTimeLogicY = bestScoreLogicY + recordHeight + sm.scaleHeight(10); // Dưới dòng điểm

        // Tính kích thước panel chung cho 2 dòng kỷ lục
        int recordPanelW = Math.max(bestScoreWidth, fastestTimeWidth) + 2 * panelPadding;
        int recordPanelH = (recordHeight + sm.scaleHeight(10)) * 2 + 2 * panelPadding;
        int recordPanelX = (sm.NATIVE_WIDTH - recordPanelW) / 2; // Căn giữa
        int recordPanelY = bestScoreLogicY - recordAscent - panelPadding; // Căn Y

        // Vẽ panel nền kỷ lục
        g2d.setColor(panelColor);
        g2d.fillRoundRect(sm.scaleX(recordPanelX), sm.scaleY(recordPanelY),
                sm.scaleWidth(recordPanelW), sm.scaleHeight(recordPanelH),
                sm.scaleWidth(panelArc), sm.scaleHeight(panelArc));
        // Vẽ chữ kỷ lục
        g2d.setColor(Color.WHITE);
        g2d.drawString(bestScoreText, sm.scaleX(bestScoreLogicX), sm.scaleY(bestScoreLogicY));
        g2d.drawString(fastestTimeText, sm.scaleX(fastestTimeLogicX), sm.scaleY(fastestTimeLogicY));


        // --- 5. Vẽ 2 cột danh sách (Top 5) và Panel nền ---
        g2d.setFont(scaledListFont);
        FontMetrics fmList = g2d.getFontMetrics();
        int listStartY = 280; // Vị trí Y (logic) bắt đầu của dòng 1
        int listTitleY = listStartY - fmList.getHeight() - sm.scaleHeight(5); // Vị trí tiêu đề cột
        int lineHeight = fmList.getHeight() + sm.scaleHeight(10); // Khoảng cách (logic) giữa các dòng
        int listPanelH = lineHeight * 6 + 2 * panelPadding; // Panel cao đủ cho tiêu đề + 5 dòng
        int listPanelArc = 15; // Bo tròn ít hơn

        // --- Cột Top Scores ---
        int scoreColXLogic = sm.NATIVE_WIDTH / 4; // Tọa độ X (logic) cột điểm (1/4 màn hình)
        String scoreTitle = "Top 5 Scores";
        int scoreTitleWidth = fmList.stringWidth(scoreTitle);
        // Tính kích thước panel
        int scorePanelW = scoreTitleWidth + 4 * panelPadding;
        int scorePanelX = scoreColXLogic - scorePanelW / 2; // Căn giữa
        int scorePanelY = listTitleY - fmList.getAscent() - panelPadding; // Căn Y
        // Vẽ panel nền
        g2d.setColor(panelColor);
        g2d.fillRoundRect(sm.scaleX(scorePanelX), sm.scaleY(scorePanelY),
                sm.scaleWidth(scorePanelW), sm.scaleHeight(listPanelH),
                sm.scaleWidth(listPanelArc), sm.scaleHeight(listPanelArc));
        // Vẽ tiêu đề cột
        g2d.setColor(Color.ORANGE);
        g2d.drawString(scoreTitle, sm.scaleX(scoreColXLogic) - scoreTitleWidth / 2, sm.scaleY(listTitleY));
        // Vẽ 5 dòng điểm
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) { // Luôn vẽ 5 dòng
            String entryText;
            if (i < topScores.size()) {
                entryText = String.format("%d. %d", i + 1, topScores.get(i));
            } else {
                entryText = String.format("%d. ---", i + 1); // Dòng trống
            }
            int entryWidth = fmList.stringWidth(entryText);
            g2d.drawString(entryText, sm.scaleX(scoreColXLogic) - entryWidth / 2, sm.scaleY(listStartY + i * lineHeight));
        }

        // --- Cột Top Times ---
        int timeColXLogic = sm.NATIVE_WIDTH * 3 / 4; // Tọa độ X (logic) cột thời gian (3/4 màn hình)
        String timeTitle = "Top 5 Times (Win)";
        int timeTitleWidth = fmList.stringWidth(timeTitle);
        // Tính kích thước panel
        int timePanelW = timeTitleWidth + 4 * panelPadding;
        int timePanelX = timeColXLogic - timePanelW / 2; // Căn giữa
        int timePanelY = listTitleY - fmList.getAscent() - panelPadding; // Cùng Y
        // Vẽ panel nền
        g2d.setColor(panelColor);
        g2d.fillRoundRect(sm.scaleX(timePanelX), sm.scaleY(timePanelY),
                sm.scaleWidth(timePanelW), sm.scaleHeight(listPanelH),
                sm.scaleWidth(listPanelArc), sm.scaleHeight(listPanelArc));
        // Vẽ tiêu đề cột
        g2d.setColor(Color.CYAN);
        g2d.drawString(timeTitle, sm.scaleX(timeColXLogic) - timeTitleWidth / 2, sm.scaleY(listTitleY));
        // Vẽ 5 dòng thời gian
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) {
            String entryText;
            if (i < topTimes.size()) {
                entryText = String.format("%d. %s", i + 1, formatTime(topTimes.get(i)));
            } else {
                entryText = String.format("%d. --:--.---", i + 1); // Dòng trống
            }
            int entryWidth = fmList.stringWidth(entryText);
            g2d.drawString(entryText, sm.scaleX(timeColXLogic) - entryWidth / 2, sm.scaleY(listStartY + i * lineHeight));
        }

        // 6. Vẽ nút Back
        backButton.draw(g, sm);

        // Reset composite về mặc định
        g2d.setComposite(defaultComposite);
    }

    /**
     * Xác định (resolve) đường dẫn (Path) tin cậy để lưu tệp điểm.
     * <p>
     * Thử các vị trí theo thứ tự ưu tiên:
     * 1. Thư mục người dùng ({@code user.home}/.arkanoidGame) - Ưu tiên hàng đầu, ổn định.
     * 2. Thư mục làm việc hiện tại ({@code user.dir}) - Dùng cho môi trường phát triển (dev).
     * <p>
     * Sẽ tự động kiểm tra quyền ghi và tạo thư mục nếu cần.
     *
     * @return Path đến tệp {@code scores.properties}, hoặc {@code null}
     * nếu không tìm được vị trí nào có thể ghi.
     */
    private static Path resolveResourceBackedScoreFile() {
        // Ưu tiên 1: Thư mục người dùng (ổn định nhất)
        try {
            Path homeDir = Paths.get(System.getProperty("user.home"), ".arkanoidGame");
            // Kiểm tra hoặc tạo thư mục
            if (Files.notExists(homeDir)) {
                try {
                    Files.createDirectories(homeDir);
                } catch (IOException e) {
                    System.err.println("Không thể tạo thư mục lưu điểm trong thư mục người dùng: " + homeDir + " | " + e.getMessage());
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
            if (Files.isDirectory(currentDir) && Files.isWritable(currentDir)) {
                return currentDir.resolve("scores.properties");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi truy cập thư mục hiện tại: " + e.getMessage());
        }

        // Nếu tất cả thất bại
        System.err.println("Không tìm thấy vị trí phù hợp để lưu file điểm số.");
        return null;
    }

    /**
     * Đảm bảo tệp {@code scores.properties} tồn tại.
     * <p>
     * Nếu tệp chưa tồn tại, hàm này sẽ tạo mới nó với các giá trị mặc định
     * (ví dụ: highScore=0, fastestTime=MAX_VALUE).
     *
     * @throws IOException nếu không thể tạo thư mục hoặc tệp.
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
            // Đặt giá trị mặc định
            p.setProperty("highScore", "0");
            p.setProperty("fastestTime", String.valueOf(Long.MAX_VALUE));
            p.setProperty("topScores", "");
            p.setProperty("topTimes", "");
            for (int i = 0; i < totalLevels; i++) {
                p.setProperty("level." + i + ".score", "0");
                p.setProperty("level." + i + ".time", String.valueOf(Long.MAX_VALUE));
            }
            // Ghi tệp mặc định
            try (OutputStream os = Files.newOutputStream(
                    SCORE_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                p.store(os, "Arkanoid scores");
            }
        }
    }

    /**
     * Tải (load) dữ liệu điểm từ tệp {@code scores.properties}
     * (đã được xác định bởi {@code SCORE_FILE}) vào bộ nhớ.
     * <p>
     * Sử dụng {@link java.util.Properties} để đọc tệp.
     * Có xử lý lỗi (ví dụ: {@code NumberFormatException}) nếu dữ liệu
     * trong tệp bị hỏng, và sẽ gọi {@link #initializeDefaultScores()}
     * nếu có lỗi nghiêm trọng.
     */
    private void loadScoresFromFile() {
        if (SCORE_FILE == null) {
            System.err.println("Không thể tải điểm do đường dẫn file không hợp lệ.");
            initializeDefaultScores(); // Khởi tạo giá trị mặc định trong bộ nhớ
            return;
        }
        try {
            ensureFileExists(); // Đảm bảo file tồn tại (hoặc tạo mới)
            Properties p = new Properties();
            try (InputStream is = Files.newInputStream(SCORE_FILE, StandardOpenOption.READ)) {
                p.load(is);
            }

            // Tải HighScore tổng
            try { highScore = Integer.parseInt(p.getProperty("highScore", "0").trim()); }
            catch (NumberFormatException ignored) { highScore = 0; }

            // Tải FastestTime tổng
            try { fastestTime = Long.parseLong(p.getProperty("fastestTime", String.valueOf(Long.MAX_VALUE)).trim()); }
            catch (NumberFormatException ignored) { fastestTime = Long.MAX_VALUE; }

            // Tải Top 5 Scores (dạng "100,50,10")
            topScores.clear();
            String ts = p.getProperty("topScores", "").trim();
            if (!ts.isEmpty()) {
                for (String s : ts.split(",")) {
                    try { topScores.add(Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {}
                }
                topScores.sort(Comparator.reverseOrder());
                while (topScores.size() > 5) { topScores.remove(5); }
            }

            // Tải Top 5 Times (dạng "12345,67890")
            topTimes.clear();
            String tt = p.getProperty("topTimes", "").trim();
            if (!tt.isEmpty()) {
                for (String s : tt.split(",")) {
                    try { topTimes.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
                }
                topTimes.sort(Comparator.naturalOrder());
                while (topTimes.size() > 5) { topTimes.remove(5); }
            }

            // Tải điểm/thời gian từng màn (dạng "level.0.score=100")
            perLevelHighScores.clear();
            perLevelFastestTimes.clear();
            for (int i = 0; i < totalLevels; i++) {
                try {
                    int score = Integer.parseInt(p.getProperty("level." + i + ".score", "0"));
                    perLevelHighScores.put(i, score);
                    long time = Long.parseLong(p.getProperty("level." + i + ".time", String.valueOf(Long.MAX_VALUE)));
                    perLevelFastestTimes.put(i, time);
                } catch(NumberFormatException ignored) {
                    // Đặt giá trị mặc định nếu có lỗi
                    perLevelHighScores.put(i, 0);
                    perLevelFastestTimes.put(i, Long.MAX_VALUE);
                }
            }

        } catch (IOException e) {
            System.err.println("Lỗi khi tải điểm từ file: " + SCORE_FILE + " | " + e.getMessage());
            initializeDefaultScores(); // Khởi tạo mặc định nếu đọc file lỗi
        }
    }

    /**
     * Khởi tạo điểm số mặc định (trong bộ nhớ)
     * trong trường hợp tệp tin bị lỗi hoặc không thể truy cập.
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
     * Lưu (save) dữ liệu điểm số hiện tại (trong bộ nhớ)
     * vào tệp {@code scores.properties}.
     * <p>
     * Chuyển đổi các danh sách (List) Top 5 thành chuỗi (String)
     * được phân tách bằng dấu phẩy (comma-separated)
     * (ví dụ: "100,50,10") để lưu trữ trong tệp {@link Properties}.
     */
    private void saveScoresToFile() {
        if (SCORE_FILE == null) {
            System.err.println("Không thể lưu điểm do đường dẫn file không hợp lệ.");
            return;
        }
        try {
            Properties p = new Properties();

            // Lưu HighScore và FastestTime tổng
            p.setProperty("highScore", String.valueOf(highScore));
            p.setProperty("fastestTime", String.valueOf(fastestTime));

            // Lưu Top 5 Scores (chuyển List<Integer> thành "100,50,10")
            String ts = topScores.stream().limit(5).map(String::valueOf).collect(Collectors.joining(","));
            p.setProperty("topScores", ts);

            // Lưu Top 5 Times (chuyển List<Long> thành "12345,67890")
            String tt = topTimes.stream().limit(5).map(String::valueOf).collect(Collectors.joining(","));
            p.setProperty("topTimes", tt);

            // Lưu điểm/thời gian từng màn
            for (int i = 0; i < totalLevels; i++) {
                p.setProperty("level." + i + ".score", String.valueOf(perLevelHighScores.getOrDefault(i, 0)));
                p.setProperty("level." + i + ".time", String.valueOf(perLevelFastestTimes.getOrDefault(i, Long.MAX_VALUE)));
            }

            // Ghi vào file (tạo mới, ghi đè nội dung cũ)
            try (OutputStream os = Files.newOutputStream(
                    SCORE_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                p.store(os, "Arkanoid scores");
            }
        } catch (IOException e) {
            System.err.println("Lỗi nghiêm trọng khi lưu điểm vào file: " + SCORE_FILE + " | " + e.getMessage());
        }
    }
}