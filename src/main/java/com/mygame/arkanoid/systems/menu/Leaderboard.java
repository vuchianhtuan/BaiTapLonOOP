package com.mygame.arkanoid.systems.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {
    private List<Integer> scores;

    public Leaderboard() {
        scores = new ArrayList<>(6);
    }
    public void addScore(int score) {
        scores.add(score);
        scores.sort(Comparator.reverseOrder());
        scores.remove(5); // Giữ lại 5 điểm cao nhất
    }
    public List<Integer> getTopScores() { return scores; }

}

