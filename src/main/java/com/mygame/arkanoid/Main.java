package com.mygame.arkanoid;

import com.mygame.arkanoid.core.*;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.systems.UIManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main {
    public static JFrame window;

    public static void main(String[] args) {
        window = new JFrame("Arkanoid");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setResizable(true);

        GameManager gameManager = new GameManager();
        UIManager uiManager = new UIManager(gameManager);
        GamePanel gamePanel = new GamePanel(gameManager, uiManager);
        GameLoop gameLoop = new GameLoop(gameManager, gamePanel, uiManager);



        window.add(gamePanel);

        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = gamePanel.getWidth();
                int newHeight = gamePanel.getHeight();

                ScalingManager.getInstance().update(newWidth, newHeight);
            }
        });

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        ScalingManager.getInstance().update(gamePanel.getWidth(), gamePanel.getHeight());

        gameLoop.start();
    }
}

