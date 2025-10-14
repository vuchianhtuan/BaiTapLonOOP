package com.mygame.arkanoid;

import com.mygame.arkanoid.core.*;
import com.mygame.arkanoid.systems.UIManager;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Arkanoid");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GameManager gameManager = new GameManager();
        UIManager uiManager = new UIManager(gameManager);

        //gameManager.startGame();
        GamePanel gamePanel = new GamePanel(gameManager, uiManager);
        GameLoop gameLoop = new GameLoop(gameManager, gamePanel, uiManager);

        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);


        gameLoop.start();
    }
}

