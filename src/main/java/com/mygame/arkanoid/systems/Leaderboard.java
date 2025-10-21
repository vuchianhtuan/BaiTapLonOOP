package com.mygame.arkanoid.systems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

