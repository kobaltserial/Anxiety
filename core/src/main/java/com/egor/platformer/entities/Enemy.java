package com.egor.platformer.entities;

import com.badlogic.gdx.math.Rectangle;

/**
 * Patrolling enemy that walks back and forth on a platform.
 * Has HP and can be damaged by the player's melee attack.
 */
public class Enemy {

    public static final float SIZE = 160f;
    public static final int MAX_HP = 100;

    private float x;
    private float y;
    private final float platformMinX;
    private final float platformMaxX;
    private float direction;
    private int hp = MAX_HP;

    public Enemy(float x, float y, float platformMinX, float platformMaxX, float direction) {
        this.x = x;
        this.y = y;
        this.platformMinX = platformMinX;
        this.platformMaxX = platformMaxX;
        this.direction = direction;
    }

    public void update(float delta, float speed) {
        x += direction * speed * delta;

        if (x <= platformMinX) {
            x = platformMinX;
            direction = 1f;
        } else if (x + SIZE >= platformMaxX) {
            x = platformMaxX - SIZE;
            direction = -1f;
        }
    }

    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    public boolean isDead() {
        return hp <= 0;
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

    public int getHp() {
        return hp;
    }
}
