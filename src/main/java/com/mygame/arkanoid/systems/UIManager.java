package com.mygame.arkanoid.systems;

// File: UIManager.java (trong package com.mygame.arkanoid.systems)
import com.mygame.arkanoid.core.GameManager;
import java.awt.Graphics2D;

public class UIManager {
    private GameManager gameManager;
    private HeartUI heartUI; // Sử dụng lớp HeartsUI bạn đã tạo
    private ScoreUI scoreUI;
    // Sau này có thể thêm ScoreUI, TimerUI...
    // private ScoreUI scoreUI;

    public UIManager(GameManager gm) {
        this.gameManager = gm;
        this.heartUI = new HeartUI(gm); // Truyền GameManager vào để HeartsUI lấy dữ liệu
        this.scoreUI = new ScoreUI(gm);
    }

    public void update() {
        // Cập nhật logic cho các thành phần UI (ví dụ: animation)
        heartUI.update();
        scoreUI.update();
    }

    public void draw(Graphics2D g2d) {
        // Vẽ tất cả các thành phần UI
        heartUI.draw(g2d);
        scoreUI.draw(g2d);
    }
}