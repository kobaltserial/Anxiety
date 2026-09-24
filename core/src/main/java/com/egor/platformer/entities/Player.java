package com.egor.platformer.entities;

import com.badlogic.gdx.math.Rectangle;

/**
 * Player character. Holds state (position, velocity, hp, action flags) and
 * exposes logic for input, physics, damage and rendering.
 */
public class Player {

    public static final float SIZE = 50f;
    public static final int MAX_HP = 100;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private boolean onGround;
    private boolean facingRight = true;

    private int hp = MAX_HP;
    private float invulnerableTimer;

    private boolean dashing;
    private float dashTimer;
    private float dashCooldownTimer;
    private float dashDirection = 1f;

    private boolean attacking;
    private float attackTimer;
    private float attackCooldownTimer;
    private float attackDirection = 1f;
    private boolean attackHitApplied;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
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

    public float getCenterX() {
        return x + SIZE / 2f;
    }

    public float getCenterY() {
        return y + SIZE / 2f;
    }

    public int getHp() {
        return hp;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public boolean isDashing() {
        return dashing;
    }

    public float getAttackTimer() {
        return attackTimer;
    }

    public float getAttackDirection() {
        return attackDirection;
    }

    public float getInvulnerableTimer() {
        return invulnerableTimer;
    }
    public void updatePosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
