package com.mygame.arkanoid.systems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {
    private List<Integer> scores;

    public Leaderboard() {
        scores = new ArrayList<>(10);
    }
    public void addScore(int score) {
        scores.add(score);
        scores.sort(Comparator.reverseOrder());
        if (scores.size() > 10) {
            scores = scores.subList(0, 10);
        }
    }
    public List<Integer> getTopScores() { return scores; }

}

