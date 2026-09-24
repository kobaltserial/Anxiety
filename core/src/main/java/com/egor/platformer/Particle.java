package com.egor.platformer;

import com.badlogic.gdx.math.MathUtils;

/**
 * Short-lived visual effect used for dust, dash trails and impact feedback.
 */
public class Particle {

    public float x;
    public float y;
    public float velocityX;
    public float velocityY;
    public float size;
    public float life;
    public float maxLife;

    public float r;
    public float g;
    public float b;

    public Particle(float x, float y, float velocityX, float velocityY,
                    float size, float life,
                    float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.size = size;
        this.life = life;
        this.maxLife = life;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /** Advances the particle by delta seconds. */
    public void update(float delta) {
        x += velocityX * delta;
        y += velocityY * delta;
        velocityY += -300f * delta; // light gravity, so dust arcs and falls
        life -= delta;
    }

    public boolean isDead() {
        return life <= 0f;
    }

    /** 0.0 when dead, 1.0 when fresh. Used to fade the particle out. */
    public float alpha() {
        return MathUtils.clamp(life / maxLife, 0f, 1f);
    }
}
