package com.mygame.arkanoid;

import com.mygame.arkanoid.core.*;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Arkanoid");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        // Tạo và kết nối các thành phần
        GameManager gameManager = new GameManager();
        gameManager.startGame();

        GamePanel gamePanel = new GamePanel(gameManager);
        GameLoop gameLoop = new GameLoop(gameManager, gamePanel);

        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gameLoop.start();
    }
}
