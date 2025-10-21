package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.PowerUpType;
import com.mygame.arkanoid.util.ErrorHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Level {
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
    private static final int BRICK_WIDTH = 42;      // Cũ: 40
    private static final int BRICK_HEIGHT = 26;     // Cũ: 20
    private static final int PADDING_X = 4;         // Cũ: 10
    private static final int PADDING_Y = 4;         // Cũ: 10
    private static final int START_OFFSET_X = 80;   // Cũ: 50
    private static final int START_OFFSET_Y = 50;

    public Level(String filePath) {
        this.bricks = new ArrayList<>();
        this.bossBricks = new ArrayList<>();
        this.powerUpConfig = new HashMap<>();
        loadLevelFromFile(filePath);
    }

    private void loadLevelFromFile(String filePath) {
        try (InputStream is = Level.class.getResourceAsStream(filePath)) {
            if (is == null) {
                ErrorHandler.log("Không thể tìm thấy file level: " + filePath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                List<String> lines = reader.lines().collect(Collectors.toList());
                int layoutStartIndex = 0;

                // 1. Đọc Header để lấy cấu hình
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.equals("---")) {
                        layoutStartIndex = i + 1;
                        break;
                    }
                    parseHeaderLine(line);
                }

                // 2. Đọc Layout gạch
                for (int i = layoutStartIndex; i < lines.size(); i++) {
                    String line = lines.get(i);
                    int row = i - layoutStartIndex;
                    for (int col = 0; col < line.length(); col++) {
                        char brickType = line.charAt(col);
                        int x = START_OFFSET_X + col * (BRICK_WIDTH + PADDING_X);
                        int y = START_OFFSET_Y + row * (BRICK_HEIGHT + PADDING_Y);

                        Brick brick = createBrick(brickType, x, y);
                        if (brick != null) {
                            if (isBossBrickType(brickType)) {
                                bossBricks.add(brick);
                            } else {
                                bricks.add(brick);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorHandler.log("Lỗi khi đọc file level: " + filePath + " | " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isBossBrickType(char type) {
        return type == 'B' || type == 'T' || type == 'C';
    }

    private void parseHeaderLine(String line) {
        String[] parts = line.split(":", 2);
        if (parts.length < 2) return;

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
            String[] powerUpDefs = value.split(",");
            for (String def : powerUpDefs) {
                String[] pv = def.split("=");
                if (pv.length == 2) {
                    try {
                        PowerUpType type = PowerUpType.valueOf(pv[0].trim().toUpperCase());
                        double rate = Double.parseDouble(pv[1].trim());
                        powerUpConfig.put(type, rate);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Lỗi định dạng powerup không xác định: " + pv[0]);
                    } catch (Exception e) {
                        System.err.println("Lỗi định dạng tỷ lệ powerup: " + def);
                    }
                }
            }
        } else if ("lasershooter_spawn_rate".equals(key)) { // Dùng else if
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
        } else if ("theme_background".equals(key)) { // <-- THÊM LOGIC MỚI NÀY
            this.themeBackground = value;
        }
    }

    private Brick createBrick(char type, int x, int y) {
        switch (type) {
            case '1': return new NormalBrick(x, y, BRICK_WIDTH, BRICK_HEIGHT);
            case '2': return new StrongBrick(x, y, BRICK_WIDTH, BRICK_HEIGHT, 2);
            case 'L': return new LaserShooterBrick(x, y, BRICK_WIDTH, BRICK_HEIGHT, 3);
            case 'E': return new ExplosiveBrick(x, y, BRICK_WIDTH, BRICK_HEIGHT);
            case 'M': return new MovingBrick(x, y, BRICK_WIDTH, BRICK_HEIGHT, 2, 200);
            case 'B': return new NormalBrick(x, y, BRICK_WIDTH, BRICK_HEIGHT);
            case 'T': return new StrongBrick(x, y, BRICK_WIDTH, BRICK_HEIGHT, 3); // Máu 3
            case 'C': return new LaserShooterBrick(x, y, BRICK_WIDTH, BRICK_HEIGHT, 5);
            case '_': case '0': default: return null;
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
     * Trả về một loại PowerUp ngẫu nhiên dựa trên tỷ lệ đã cấu hình.
     */
    public PowerUpType getRandomPowerUpType() {
        if (powerUpConfig.isEmpty()) return null;

        double roll = random.nextDouble();
        double cumulativeRate = 0.0;

        for (Map.Entry<PowerUpType, Double> entry : powerUpConfig.entrySet()) {
            cumulativeRate += entry.getValue();
            if (roll < cumulativeRate) {
                return entry.getKey();
            }
        }
        return null;
    }

    public List<Brick> getBossBricks() {
        return bossBricks;
    }

    public boolean isBossLevel() {
        return "boss".equals(levelType);
    }

    public java.awt.Rectangle getBossInitialBounds() {
        if (bossBricks.isEmpty()) {
            return new java.awt.Rectangle(0, 0, 0, 0);
        }
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (Brick brick : bossBricks) {
            minX = Math.min(minX, brick.getX());
            minY = Math.min(minY, brick.getY());
            maxX = Math.max(maxX, brick.getX() + brick.getWidth());
            maxY = Math.max(maxY, brick.getY() + brick.getHeight());
        }
        return new java.awt.Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public List<Brick> getBricks() {
        return bricks;
    }

    public boolean isCompleted() {
        return bricks.isEmpty();
    }
}
