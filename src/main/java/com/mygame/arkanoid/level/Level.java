package com.mygame.arkanoid.level;

import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.PowerUpType;

import static com.mygame.arkanoid.objects.bricks.BrickFactory.create;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Đại diện cho một màn chơi (Level) duy nhất.
 * <p>
 * Lớp này chịu trách nhiệm tải và lưu trữ cấu hình của màn chơi từ một tệp
 * văn bản (trong resources). Cấu hình này bao gồm bố cục gạch (layout),
 * tỷ lệ rớt power-up, và thông tin chủ đề (theme) như nhạc nền, hình nền.
 */
public class Level {

    // Danh sách gạch và cấu hình màn chơi
    private final List<Brick> bricks;
    private final List<Brick> bossBricks;
    private String levelType = "normal";
    private final Map<PowerUpType, Double> powerUpConfig;
    private final Random random = new Random();
    private double laserShooterSpawnRate = 0.0;
    private String themeMusic = ""; // Tên file nhạc nền
    private String themeAssetPrefix = ""; // Tiền tố tài nguyên (ví dụ "ice_")
    private String themeBackground = "";

    // Các hằng số để căn chỉnh layout
    private static final int BRICK_WIDTH = 45;      // Cũ: 40
    private static final int BRICK_HEIGHT = 20;     // Cũ: 20
    private static final int PADDING_X = 4;         // Cũ: 10
    private static final int PADDING_Y = 4;         // Cũ: 10
    private static final int START_OFFSET_X = 40;   // Cũ: 50
    private static final int START_OFFSET_Y = 30;

    /**
     * Khởi tạo một level mới bằng cách tải dữ liệu từ một tệp tài nguyên (resource file) cụ thể.
     *
     * @param filePath Đường dẫn đến tệp level trong resources (ví dụ: "/levels/level1.txt").
     * @throws IllegalArgumentException Nếu không tìm thấy tệp.
     * @throws RuntimeException Nếu có lỗi nghiêm trọng khi đọc/phân tích tệp.
     */
    public Level(String filePath) {
        this.bricks = new ArrayList<>();
        this.bossBricks = new ArrayList<>();
        this.powerUpConfig = new HashMap<>();
        loadLevelFromFile(filePath);
    }

    /**
     * Tải và phân tích tệp định nghĩa level từ đường dẫn resource.
     * <p>
     * Cấu trúc file được chia làm 2 phần, phân tách bởi dòng "---":
     * <ol>
     * <li><b>Header:</b> Chứa các cặp "key: value" để cấu hình level (ví dụ: powerups, theme_music).</li>
     * <li><b>Layout:</b> Một bản đồ ASCII (ký tự) đại diện cho vị trí các loại gạch.</li>
     * </ol>
     *
     * @param filePath Đường dẫn tài nguyên nội bộ đến tệp level.
     */
    private void loadLevelFromFile(String filePath) {
        // Đọc file từ resources
        try (InputStream is = Level.class.getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Không thể tìm thấy file level: " + filePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                List<String> lines = reader.lines().collect(Collectors.toList());
                int layoutStartIndex = 0;

                // 1. Đọc Header để lấy cấu hình
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.equals("---")) {
                        layoutStartIndex = i + 1; // Đánh dấu vị trí bắt đầu của layout
                        break;
                    }
                    parseHeaderLine(line); // Phân tích dòng cấu hình
                }

                // 2. Đọc Layout gạch
                for (int i = layoutStartIndex; i < lines.size(); i++) {
                    String line = lines.get(i);
                    int row = i - layoutStartIndex; // Tính toán hàng (row)
                    for (int col = 0; col < line.length(); col++) {
                        char brickType = line.charAt(col);
                        // Tính toán tọa độ X, Y dựa trên hằng số layout
                        int x = START_OFFSET_X + col * (BRICK_WIDTH + PADDING_X);
                        int y = START_OFFSET_Y + row * (BRICK_HEIGHT + PADDING_Y);

                        // Sử dụng Factory để tạo đối tượng Brick từ ký tự
                        Brick brick = create(brickType, x, y);
                        if (brick != null) {
                            // Phân loại gạch thường và gạch của trùm (boss)
                            if (isBossBrickType(brickType)) {
                                bossBricks.add(brick);
                            } else {
                                bricks.add(brick);
                            }
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // lỗi file không tồn tại hoặc format sai
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file level: " + filePath, e);
        }
    }

    /**
     * Kiểm tra xem một ký tự có đại diện cho gạch của trùm (boss) hay không.
     * @param type Ký tự loại gạch.
     * @return true nếu là gạch trùm, false nếu không.
     */
    private boolean isBossBrickType(char type) {
        return type == 'B' || type == 'T' || type == 'C';
    }

    /**
     * Phân tích một dòng trong phần header (ví dụ: "key: value").
     * <p>
     * Hỗ trợ bỏ qua các comment: Bất kỳ văn bản nào sau dấu thăng ({@code #})
     * trên cùng một dòng sẽ bị loại bỏ trước khi phân tích giá trị (value).
     *
     * @param line Dòng văn bản thô từ phần header của tệp level.
     */
    private void parseHeaderLine(String line) {
        String[] parts = line.split(":", 2); // Chia thành 2 phần: key và value
        if (parts.length < 2) return; // Bỏ qua nếu không phải định dạng "key: value"

        String key = parts[0].trim().toLowerCase();
        String rawValue = parts[1]; // Lấy giá trị thô, ví dụ: " 0.02  # 2%..."

        // BƯỚC QUAN TRỌNG: TÁCH BỎ COMMENT (phần sau dấu #)
        String value;
        if (rawValue.contains("#")) {
            // Lấy phần tử đầu tiên sau khi cắt bằng "#", và xóa khoảng trắng
            value = rawValue.split("#")[0].trim();
        } else {
            // Nếu không có comment, chỉ cần xóa khoảng trắng
            value = rawValue.trim();
        }
        // Giờ đây, 'value' đã sạch sẽ, ví dụ: "0.02" hoặc "EXPAND=0.1,SLOW_BALL=0.05"

        // Bắt đầu phân tích các giá trị đã được làm sạch
        if ("type".equals(key)) {
            this.levelType = value.toLowerCase();

        } else if ("powerups".equals(key)) {
            // Phân tích định dạng "POWERUP_TYPE=rate,POWERUP_TYPE=rate,..."
            String[] powerUpDefs = value.split(",");
            for (String def : powerUpDefs) {
                String[] pv = def.split("="); // Tách "POWERUP_TYPE=rate"
                if (pv.length == 2) {
                    try {
                        PowerUpType type = PowerUpType.valueOf(pv[0].trim().toUpperCase());
                        double rate = Double.parseDouble(pv[1].trim());
                        powerUpConfig.put(type, rate); // Lưu cấu hình power-up
                    } catch (IllegalArgumentException e) {
                        System.err.println("Lỗi định dạng powerup không xác định: " + pv[0]);
                    } catch (Exception e) {
                        System.err.println("Lỗi định dạng tỷ lệ powerup: " + def);
                    }
                }
            }
        } else if ("lasershooter_spawn_rate".equals(key)) {
            // Phân tích tỷ lệ spawn lasershooter
            try {
                // Bây giờ 'value' chỉ là "0.02" nên sẽ parse thành công
                this.laserShooterSpawnRate = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // Lỗi này vẫn có thể xảy ra nếu người dùng gõ "abc" thay vì "0.02"
                System.err.println("Lỗi định dạng tỷ lệ lasershooter: " + value);
            }
        } else if ("theme_music".equals(key)) {
            this.themeMusic = value;
        } else if ("theme_assets".equals(key)) {
            this.themeAssetPrefix = value;
        } else if ("theme_background".equals(key)) {
            this.themeBackground = value;
        }
    }

    public String getThemeBackground() {
        return themeBackground;
    }

    public String getThemeMusic() {
        return themeMusic;
    }

    public String getThemeAssetPrefix() {
        return themeAssetPrefix;
    }

    public double getLaserShooterSpawnRate() {
        return laserShooterSpawnRate;
    }

    /**
     * Quyết định ngẫu nhiên một loại PowerUp để thả ra (spawn).
     * <p>
     * Sử dụng kỹ thuật "cumulative roll" (cộng dồn tỷ lệ).
     * Lấy một số ngẫu nhiên (0.0 đến 1.0) và so sánh với tổng
     * tỷ lệ tích lũy của các power-up đã được định nghĩa trong header.
     *
     * @return Một {@link PowerUpType} ngẫu nhiên, hoặc {@code null} nếu
     * không có power-up nào được cấu hình hoặc không trúng (roll > tổng tỷ lệ).
     */
    public PowerUpType getRandomPowerUpType() {
        if (powerUpConfig.isEmpty()) return null;

        double roll = random.nextDouble(); // Giá trị ngẫu nhiên từ 0.0 đến < 1.0
        double cumulativeRate = 0.0; // Tỷ lệ tích lũy

        // Duyệt qua các loại PowerUp và tỷ lệ của chúng
        for (Map.Entry<PowerUpType, Double> entry : powerUpConfig.entrySet()) {
            cumulativeRate += entry.getValue();
            if (roll < cumulativeRate) {
                // Nếu số ngẫu nhiên rơi vào "vùng" tỷ lệ của power-up này, chọn nó
                return entry.getKey();
            }
        }
        return null; // Trả về null nếu không trúng (ví dụ: tổng tỷ lệ < 1.0)
    }

    public List<Brick> getBossBricks() {
        return bossBricks;
    }

    public boolean isBossLevel() {
        return "boss".equals(levelType);
    }

    /**
     * Tính toán và trả về một hình chữ nhật (Rectangle) bao quanh
     * tất cả các gạch của trùm (boss bricks) đã được định nghĩa trong layout.
     * <p>
     * Được dùng để xác định kích thước và vị trí ban đầu của thực thể Boss
     * khi khởi tạo.
     *
     * @return Một {@link java.awt.Rectangle} bao quanh các gạch của trùm,
     * hoặc (0,0,0,0) nếu không có gạch trùm.
     */
    public java.awt.Rectangle getBossInitialBounds() {
        if (bossBricks.isEmpty()) {
            return new java.awt.Rectangle(0, 0, 0, 0);
        }
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        // Tìm các giá trị X, Y nhỏ nhất và lớn nhất
        for (Brick brick : bossBricks) {
            minX = Math.min(minX, brick.getX());
            minY = Math.min(minY, brick.getY());
            maxX = Math.max(maxX, brick.getX() + brick.getWidth());
            maxY = Math.max(maxY, brick.getY() + brick.getHeight());
        }
        // Trả về hình chữ nhật từ các điểm cực trị
        return new java.awt.Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public List<Brick> getBricks() {
        return bricks;
    }

    public boolean isCompleted() {
        // Level hoàn thành khi không còn gạch (thường)
        return bricks.isEmpty();
    }
}