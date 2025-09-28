package com.mygame.arkanoid.core;
import com.mygame.arkanoid.engine.Renderer;

// Đa luồng: GameLoop chạy update + render song song với GUI
public class GameLoop extends Thread {
    private volatile boolean running = true;
    private GameManager gameManager;
    private Renderer renderer;

    public GameLoop(GameManager gameManager, Renderer renderer) {
        this.gameManager = gameManager;
        this.renderer = renderer;
    }

    @Override
    public void run() {}

    public void stopLoop() {
        running = false;
    }
}