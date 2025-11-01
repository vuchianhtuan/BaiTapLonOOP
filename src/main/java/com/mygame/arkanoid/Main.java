package com.mygame.arkanoid;

import com.mygame.arkanoid.core.*;
import com.mygame.arkanoid.systems.helper.ScalingManager;
import com.mygame.arkanoid.systems.save.SaveSystem;
import com.mygame.arkanoid.systems.save.SaveData;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static JFrame window;

    public static void main(String[] args) {
        window = new JFrame("Arkanoid");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);

        GameManager gameManager = GameManager.getInstance();

        SaveData data = SaveSystem.load();
        if (data != null && data.isCanContinue()) {
            gameManager.restoreFromSave(data);
        }

        GamePanel gamePanel = new GamePanel(gameManager);
        GameLoop gameLoop = new GameLoop(gameManager, gamePanel);

        window.add(gamePanel);

        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = gamePanel.getWidth();
                int newHeight = gamePanel.getHeight();

                ScalingManager.getInstance().update(newWidth, newHeight);
            }
        });

        // SAVE KHI ĐÓNG CỬA SỔ
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (gameManager.canContinue()) {
                    SaveSystem.save(SaveSystem.capture(gameManager));
                }
                System.exit(0);
            }
        });

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        ScalingManager.getInstance().update(gamePanel.getWidth(), gamePanel.getHeight());
        gameLoop.start();
    }
}

