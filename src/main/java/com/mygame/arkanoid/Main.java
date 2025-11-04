package com.mygame.arkanoid;

import com.mygame.arkanoid.core.*;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.save.SaveSystem;
import com.mygame.arkanoid.save.SaveData;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Lớp Main là điểm khởi đầu của trò chơi.
 */
public class Main {
    public static JFrame window;

    public static void main(String[] args) {
        // Khởi tạo cửa sổ trò chơi
        window = new JFrame("Arkanoid");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);

        /**
         * KHỞI TẠO HỆ THỐNG QUẢN LÝ TRÒ CHƠI VÀ TẢI DỮ LIỆU LƯU.
         */
        GameManager gameManager = GameManager.getInstance();

        SaveData data = SaveSystem.load();
        if (data != null && data.isCanContinue()) {
            gameManager.restoreFromSave(data);
        }

        /**
         * KHỞI TẠO GIAO DIỆN VÀ VÒNG LẶP TRÒ CHƠI.
         */
        GamePanel gamePanel = new GamePanel(gameManager);
        GameLoop gameLoop = new GameLoop(gameManager, gamePanel);

        window.add(gamePanel);
        /**
         * THIẾT LẬP SỰ KIỆN THAY ĐỔI KÍCH THƯỚC CỬA SỔ ĐỂ CẬP NHẬT TỶ LỆ SCALE.
         */

        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = gamePanel.getWidth();
                int newHeight = gamePanel.getHeight();

                ScalingManager.getInstance().update(newWidth, newHeight);
            }
        });

        /**
         * THIẾT LẬP SỰ KIỆN KHI ĐÓNG CỬA SỔ ĐỂ TỰ ĐỘNG LƯU TRẠNG THÁI TRÒ CHƠI.
         */
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (gameManager.canContinue()) {
                    SaveSystem.save(SaveSystem.capture(gameManager));
                }
                System.exit(0);
            }
        });

        /**
         * HIỂN THỊ CỬA SỔ VÀ BẮT ĐẦU VÒNG LẶP TRÒ CHƠI.
         */
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        /**
         * CẬP NHẬT TỶ LỆ SCALE LẦN ĐẦU TIÊN VÀ BẮT ĐẦU VÒNG LẶP TRÒ CHƠI.
         */
        ScalingManager.getInstance().update(gamePanel.getWidth(), gamePanel.getHeight());
        gameLoop.start();
    }
}

