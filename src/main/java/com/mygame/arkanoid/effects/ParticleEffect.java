package com.mygame.arkanoid.effects;
import com.mygame.arkanoid.objects.Paddle;

import java.awt.Graphics;
public class ParticleEffect extends Effect {
    private int x, y;
    private int particleCount;

    public ParticleEffect(Paddle target, int durationInTicks) {
        super(target, durationInTicks);
    }

    @Override public void update() {}

    @Override
    public void apply() {

    }

    @Override
    public void remove() {

    }

    @Override
    public void render(Graphics g) {}
}
