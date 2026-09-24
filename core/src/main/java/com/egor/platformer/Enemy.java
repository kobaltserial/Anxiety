package com.egor.platformer;

import com.badlogic.gdx.math.Rectangle;

/**
 * Patrolling enemy that walks back and forth on a platform.
 * Flips direction when its edge would leave the platform or hit a wall.
 */
public class Enemy {

    public static final float SIZE = 40f;

    private float x;
    private float y;
    private final float platformMinX;
    private final float platformMaxX;
    private float direction;
    private boolean alive = true;

    public Enemy(float x, float y, float platformMinX, float platformMaxX, float direction) {
        this.x = x;
        this.y = y;
        this.platformMinX = platformMinX;
        this.platformMaxX = platformMaxX;
        this.direction = direction;
    }

    public void update(float delta, float speed) {
        x += direction * speed * delta;

        // Turn around at the platform edges so the enemy never walks off.
        if (x <= platformMinX) {
            x = platformMinX;
            direction = 1f;
        } else if (x + SIZE >= platformMaxX) {
            x = platformMaxX - SIZE;
            direction = -1f;
        }
    }

    public Rectangle bounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }
}
