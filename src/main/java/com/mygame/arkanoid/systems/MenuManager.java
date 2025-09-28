package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.engine.InputHandler;

public class MenuManager {

    public enum MenuState { MAIN_MENU, OPTIONS, HIGH_SCORE, IN_GAME }
    private MenuState currentState;
    private Renderer renderer;
    private InputHandler inputHandler;

    public MenuManager() {}

    public void update() {}
    public void render() {}
    public MenuState getCurrentState() {
        return null;
    }

    private void handleMainMenuInput() {}
    private void handleOptionsInput() {}
    private void handleHighScoreInput() {}
}
