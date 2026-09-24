package com.egor.platformer.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

/**
 * Player character. Owns its own state, input, physics and combat timers.
 * Sizes are scaled 4x so the sprite reads at a comfortable size on a
 * 2560x1440 viewport.
 */
public class Player {

    public static final float SIZE = 200f;
    public static final int MAX_HP = 100;

    private static final float MOVE_SPEED = 1200f;
    private static final float GRAVITY = -6000f;
    private static final float JUMP_FORCE = 2800f;
    private static final float AIR_JUMP_FORCE = 2400f;
    private static final int MAX_AIR_JUMPS = 1;

    private static final float DASH_SPEED = 3600f;
    private static final float DASH_DURATION = 0.15f;
    private static final float DASH_COOLDOWN = 0.4f;

    private static final float ATTACK_WINDUP = 0.08f;
    private static final float ATTACK_STRIKE = 0.12f;
    private static final float ATTACK_TOTAL = ATTACK_WINDUP + ATTACK_STRIKE;
    private static final float ATTACK_COOLDOWN = 0.35f;
    private static final float ATTACK_WIDTH = 220f;
    private static final float ATTACK_HEIGHT = 200f;

    private static final float INVULNERABLE_TIME = 1.2f;
    private static final float DEATH_Y = -400f;

    private static final float ANXIETY_MAX = 100f;
    private static final float ANXIETY_START = 50f;
    private static final float ANXIETY_GAIN_MOVE = 0.625f;
    private static final float ANXIETY_LOSS_IDLE = 5f;
    private static final float ANXIETY_GAIN_JUMP = 1.25f;
    private static final float ANXIETY_GAIN_DASH = 2f;
    private static final float ANXIETY_GAIN_ATTACK = 1.25f;

    private static final float PEAK_DURATION = 15f;
    private static final float OVERLOAD_DPS = 10f;
    private static final float IDLE_THRESHOLD = 3f;

    private static final float GLOW_COLOR_R = 1f;
    private static final float GLOW_COLOR_G = 0.80f;
    private static final float GLOW_COLOR_B = 0.35f;
    private static final float GLOW_ALPHA_MAX = 0.55f;
    private static final float GLOW_OUTER_RADIUS = 72f;
    private static final float GLOW_INNER_RADIUS = 40f;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private boolean onGround;
    private boolean facingRight = true;
    private int airJumpsLeft = 1;

    private float hp = MAX_HP;
    private float invulnerableTimer;

    private float anxiety = ANXIETY_START;
    private boolean anxietyDeath;
    private float peakTimer;
    private boolean overloadActive;
    private float idleTimer;

    private boolean dashing;
    private float dashTimer;
    private float dashCooldownTimer;
    private float dashDirection = 1f;
    private boolean dashJustStarted;

    private boolean attacking;
    private float attackTimer;
    private float attackCooldownTimer;
    private float attackDirection = 1f;
    private boolean attackHitApplied;

    private boolean wasOnGroundLastFrame;
    private boolean pendingRespawn;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle bounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public Rectangle attackBounds() {
        float centerY = y + SIZE / 2f;
        float ax = attackDirection > 0 ? x + SIZE : x - ATTACK_WIDTH;
        float ay = centerY - ATTACK_HEIGHT / 2f;
        return new Rectangle(ax, ay, ATTACK_WIDTH, ATTACK_HEIGHT);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getCenterX() { return x + SIZE / 2f; }
    public float getCenterY() { return y + SIZE / 2f; }
    public int getHp() { return (int) hp; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isAttacking() { return attacking; }
    public boolean isDashing() { return dashing; }
    public float getAttackTimer() { return attackTimer; }
    public float getAttackDirection() { return attackDirection; }
    public float getInvulnerableTimer() { return invulnerableTimer; }
    public float getAnxiety() { return anxiety; }
    public boolean isAnxietyDeath() { return anxietyDeath; }
    public float getPeakTimer() { return peakTimer; }
    public boolean isPeakActive() { return peakTimer > 0f && anxiety >= ANXIETY_MAX; }

    public float getDamageMultiplier() {
        if (isPeakActive()) return 3.0f;
        if (anxiety < 30f) return 1.0f;
        if (anxiety < 50f) return 1.2f;
        if (anxiety < 70f) return 1.5f;
        if (anxiety < 90f) return 2.0f;
        return 2.5f;
    }

    public float getAttackSpeedMultiplier() {
        if (isPeakActive()) return 2.5f;
        if (anxiety < 30f) return 1.0f;
        if (anxiety < 50f) return 1.1f;
        if (anxiety < 70f) return 1.25f;
        if (anxiety < 90f) return 1.5f;
        return 2.0f;
    }

    public boolean isOnGround() { return onGround; }
    public boolean isAttackHitApplied() { return attackHitApplied; }
    public boolean isStrikeActive() { return attacking && attackTimer <= ATTACK_STRIKE; }
    public boolean isPendingRespawn() { return pendingRespawn; }
    public boolean wasOnGroundLastFrame() { return wasOnGroundLastFrame; }
    public boolean justLanded() { return onGround && !wasOnGroundLastFrame; }
    public float getVelocityX() { return velocityX; }
    public boolean isDashJustStarted() { return dashJustStarted; }

    public void clearDashJustStarted() {
        dashJustStarted = false;
    }

    public void markAttackHitApplied() {
        attackHitApplied = true;
    }

    public void takeDamage(int amount) {
        if (invulnerableTimer > 0f) return;

        hp -= amount;
        invulnerableTimer = INVULNERABLE_TIME;

        if (hp <= 0) {
            hp = 0;
            pendingRespawn = true;
        }
    }

    public void clearPendingRespawn() {
        pendingRespawn = false;
    }

    public void respawn(float respawnX, float respawnY) {
        x = respawnX;
        y = respawnY;
        velocityX = 0f;
        velocityY = 0f;
        onGround = false;
        airJumpsLeft = MAX_AIR_JUMPS;
        dashing = false;
        attacking = false;
        dashCooldownTimer = 0f;
        attackCooldownTimer = 0f;
        invulnerableTimer = 0f;
        hp = MAX_HP;
        anxiety = ANXIETY_START;
        anxietyDeath = false;
        peakTimer = 0f;
        overloadActive = false;
        idleTimer = 0f;
        pendingRespawn = false;
    }

    public void update(float delta, List<Platform> platforms) {
        handleInput(delta);
        updateTimers(delta);
        updateAnxiety(delta);
        applyPhysics(delta, platforms);
        checkOutOfBounds();
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {
            tryStartDash();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            tryStartAttack();
        }

        if (!dashing) {
            velocityX = 0f;

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                velocityX = -MOVE_SPEED;
                facingRight = false;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                velocityX = MOVE_SPEED;
                facingRight = true;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !dashing) {
            if (onGround) {
                velocityY = JUMP_FORCE;
                onGround = false;
                anxiety = Math.min(ANXIETY_MAX, anxiety + ANXIETY_GAIN_JUMP);
            } else if (airJumpsLeft > 0) {
                velocityY = AIR_JUMP_FORCE;
                airJumpsLeft--;
                anxiety = Math.min(ANXIETY_MAX, anxiety + ANXIETY_GAIN_JUMP);
            }
        }
    }

    private void tryStartDash() {
        if (dashing) return;
        if (dashCooldownTimer > 0f) return;

        dashing = true;
        dashTimer = DASH_DURATION;
        dashCooldownTimer = DASH_COOLDOWN;
        dashDirection = facingRight ? 1f : -1f;
        dashJustStarted = true;
        anxiety = Math.min(ANXIETY_MAX, anxiety + ANXIETY_GAIN_DASH);
    }

    private void tryStartAttack() {
        if (attacking) return;
        if (attackCooldownTimer > 0f) return;

        attacking = true;
        attackTimer = ATTACK_TOTAL;
        attackCooldownTimer = ATTACK_COOLDOWN / getAttackSpeedMultiplier();
        attackDirection = facingRight ? 1f : -1f;
        attackHitApplied = false;
        anxiety = Math.min(ANXIETY_MAX, anxiety + ANXIETY_GAIN_ATTACK);
    }

    private void updateTimers(float delta) {
        if (dashCooldownTimer > 0f) dashCooldownTimer -= delta;
        if (attackCooldownTimer > 0f) attackCooldownTimer -= delta;
        if (invulnerableTimer > 0f) invulnerableTimer -= delta;

        if (attacking) {
            attackTimer -= delta;
            if (attackTimer <= 0f) {
                attacking = false;
            }
        }
    }

    private void updateAnxiety(float delta) {
        if (anxietyDeath) return;

        boolean moving = Math.abs(velocityX) > 1f || dashing;

        if (moving) {
            anxiety += ANXIETY_GAIN_MOVE * delta;
            idleTimer = 0f;
        } else {
            idleTimer += delta;
            boolean atPeak = anxiety >= ANXIETY_MAX;

            if (!atPeak || idleTimer >= IDLE_THRESHOLD) {
                anxiety -= ANXIETY_LOSS_IDLE * delta;
            }
        }

        anxiety = MathUtils.clamp(anxiety, 0f, ANXIETY_MAX);

        if (anxiety <= 0f) {
            anxietyDeath = true;
        }

        updatePeak(delta);
    }

    private void updatePeak(float delta) {
        if (anxiety >= ANXIETY_MAX && peakTimer <= 0f && !overloadActive) {
            peakTimer = PEAK_DURATION;
            overloadActive = true;
        }

        if (peakTimer > 0f) {
            peakTimer -= delta;
            if (peakTimer < 0f) {
                peakTimer = 0f;
            }
            return;
        }

        if (overloadActive && anxiety >= ANXIETY_MAX) {
            hp -= OVERLOAD_DPS * delta;
            if (hp <= 0f) {
                hp = 0;
                pendingRespawn = true;
            }
        } else if (anxiety < ANXIETY_MAX) {
            overloadActive = false;
        }
    }

    private void applyPhysics(float delta, List<Platform> platforms) {
        wasOnGroundLastFrame = onGround;

        if (dashing) {
            dashTimer -= delta;
            velocityX = DASH_SPEED * dashDirection;
            velocityY = 0f;
            if (dashTimer <= 0f) {
                dashing = false;
                velocityX = 0f;
            }
        } else {
            velocityY += GRAVITY * delta;
        }

        x += velocityX * delta;
        resolveX(platforms);

        y += velocityY * delta;
        resolveY(platforms);
    }

    private void resolveX(List<Platform> platforms) {
        Rectangle player = bounds();

        for (Platform platform : platforms) {
            Rectangle pb = platform.getBounds();
            if (!player.overlaps(pb)) continue;

            if (velocityX > 0) {
                x = pb.x - SIZE;
            } else if (velocityX < 0) {
                x = pb.x + pb.width;
            }
            player.setPosition(x, y);

            if (dashing) {
                dashing = false;
                velocityX = 0f;
            }
        }
    }

    private void resolveY(List<Platform> platforms) {
        Rectangle player = bounds();
        onGround = false;

        for (Platform platform : platforms) {
            Rectangle pb = platform.getBounds();
            if (!player.overlaps(pb)) continue;

            if (velocityY <= 0) {
                y = pb.y + pb.height;
                onGround = true;
                airJumpsLeft = MAX_AIR_JUMPS;
            } else {
                y = pb.y - SIZE;
            }
            velocityY = 0f;
            player.setPosition(x, y);
        }
    }

    private void checkOutOfBounds() {
        if (y < DEATH_Y) {
            pendingRespawn = true;
        }
        if (anxietyDeath) {
            pendingRespawn = true;
        }
    }

    public void draw(ShapeRenderer shapeRenderer) {
        float centerX = getCenterX();
        float bottomY = y;

        float alpha = 1f;
        if (dashing) alpha = 0.6f;
        if (invulnerableTimer > 0f) {
            alpha = 0.4f + 0.6f * Math.abs(MathUtils.sin(invulnerableTimer * 20f));
        }

        float anxietyRatio = anxiety / ANXIETY_MAX;

        float skinR = 0.62f, skinG = 0.48f, skinB = 0.38f;
        float cloakR = 0.10f, cloakG = 0.10f, cloakB = 0.13f;
        float cloakTrimR = 0.28f, cloakTrimG = 0.20f, cloakTrimB = 0.14f;
        float leatherR = 0.22f, leatherG = 0.16f, leatherB = 0.12f;
        float gloveR = 0.14f, gloveG = 0.11f, gloveB = 0.09f;
        float hairR = 0.05f, hairG = 0.05f, hairB = 0.06f;

        float coreR = GLOW_COLOR_R, coreG = GLOW_COLOR_G, coreB = GLOW_COLOR_B;
        float glowAlpha = anxietyRatio * GLOW_ALPHA_MAX;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (!attacking) {
            drawSheathedSword(shapeRenderer, centerX, bottomY, alpha);
        }

        // Cloak (short tattered cape behind the torso).
        shapeRenderer.setColor(cloakR, cloakG, cloakB, alpha);
        shapeRenderer.triangle(
            centerX - 88f, bottomY + 48f,
            centerX + 88f, bottomY + 48f,
            centerX + 64f, bottomY + 208f
        );
        shapeRenderer.triangle(
            centerX - 88f, bottomY + 48f,
            centerX + 64f, bottomY + 208f,
            centerX - 64f, bottomY + 208f
        );
        shapeRenderer.triangle(
            centerX - 88f, bottomY + 48f,
            centerX - 56f, bottomY - 16f,
            centerX - 24f, bottomY + 32f
        );
        shapeRenderer.triangle(
            centerX + 88f, bottomY + 48f,
            centerX + 56f, bottomY - 16f,
            centerX + 24f, bottomY + 32f
        );

        shapeRenderer.setColor(cloakTrimR, cloakTrimG, cloakTrimB, alpha);
        shapeRenderer.rect(centerX - 68f, bottomY + 200f, 136f, 12f);

        // Legs.
        shapeRenderer.setColor(leatherR, leatherG, leatherB, alpha);
        shapeRenderer.rect(centerX - 52f, bottomY, 40f, 88f);
        shapeRenderer.rect(centerX + 12f, bottomY, 40f, 88f);

        // Torso.
        shapeRenderer.setColor(leatherR, leatherG, leatherB, alpha);
        shapeRenderer.rect(centerX - 60f, bottomY + 80f, 120f, 112f);

        // Chest strap.
        shapeRenderer.setColor(leatherR * 0.7f, leatherG * 0.7f, leatherB * 0.7f, alpha);
        shapeRenderer.rect(centerX - 56f, bottomY + 136f, 112f, 16f);

        // Heart glow.
        if (glowAlpha > 0.01f) {
            float pulse = 1f + 0.15f * MathUtils.sin(anxiety * 0.4f);
            shapeRenderer.setColor(coreR, coreG, coreB, glowAlpha * 0.35f);
            shapeRenderer.circle(centerX, bottomY + 136f, 48f * pulse);
            shapeRenderer.setColor(coreR, coreG, coreB, glowAlpha);
            shapeRenderer.circle(centerX, bottomY + 136f, 20f * pulse);
        }

        // Shoulders.
        shapeRenderer.setColor(leatherR, leatherG, leatherB, alpha);
        shapeRenderer.rect(centerX - 80f, bottomY + 168f, 48f, 40f);
        shapeRenderer.rect(centerX + 32f, bottomY + 168f, 48f, 40f);

        // Arms.
        shapeRenderer.setColor(skinR, skinG, skinB, alpha);
        float armY = bottomY + 88f;
        if (facingRight) {
            shapeRenderer.rect(centerX + 56f, armY, 32f, 80f);
            shapeRenderer.rect(centerX - 88f, armY + 16f, 32f, 64f);
        } else {
            shapeRenderer.rect(centerX - 88f, armY, 32f, 80f);
            shapeRenderer.rect(centerX + 56f, armY + 16f, 32f, 64f);
        }

        // Gloves.
        shapeRenderer.setColor(gloveR, gloveG, gloveB, alpha);
        if (facingRight) {
            shapeRenderer.rect(centerX + 56f, armY - 16f, 36f, 40f);
            shapeRenderer.rect(centerX - 88f, armY, 36f, 40f);
        } else {
            shapeRenderer.rect(centerX - 92f, armY - 16f, 36f, 40f);
            shapeRenderer.rect(centerX + 52f, armY, 36f, 40f);
        }

        // Head.
        float headY = bottomY + 208f;
        shapeRenderer.setColor(skinR, skinG, skinB, alpha);
        shapeRenderer.rect(centerX - 36f, headY, 72f, 64f);

        // Hair and beard.
        shapeRenderer.setColor(hairR, hairG, hairB, alpha);
        shapeRenderer.rect(centerX - 40f, headY + 48f, 80f, 24f);
        shapeRenderer.rect(centerX - 40f, headY - 8f, 80f, 20f);

        // Eyes.
        shapeRenderer.setColor(0.9f, 0.75f, 0.35f, alpha);
        if (facingRight) {
            shapeRenderer.rect(centerX + 4f, headY + 24f, 12f, 12f);
        } else {
            shapeRenderer.rect(centerX - 16f, headY + 24f, 12f, 12f);
        }

        if (attacking) {
            drawAttackSword(shapeRenderer, centerX, bottomY, alpha);
        }

        shapeRenderer.end();
    }

    private void drawSheathedSword(ShapeRenderer shapeRenderer, float centerX, float bottomY, float alpha) {
        shapeRenderer.setColor(0.18f, 0.12f, 0.08f, alpha);
        float hx = centerX - 56f;
        float hy = bottomY + 240f;
        shapeRenderer.rect(hx, hy, 16f, 56f);
        shapeRenderer.setColor(0.65f, 0.45f, 0.20f, alpha);
        shapeRenderer.rect(hx - 4f, hy + 56f, 24f, 12f);

        shapeRenderer.setColor(0.55f, 0.58f, 0.62f, alpha);
        shapeRenderer.rect(centerX - 48f, bottomY + 200f, 112f, 16f);
        shapeRenderer.setColor(0.75f, 0.78f, 0.82f, alpha);
        shapeRenderer.rect(centerX - 48f, bottomY + 208f, 112f, 4f);

        shapeRenderer.setColor(0.55f, 0.38f, 0.16f, alpha);
        shapeRenderer.rect(centerX - 52f, bottomY + 196f, 20f, 20f);
    }

    private void drawAttackSword(ShapeRenderer shapeRenderer,
                                 float centerX, float bottomY, float alpha) {
        float shoulderX = facingRight ? centerX + 16f : centerX - 16f;
        float shoulderY = bottomY + 184f;

        boolean windup = attackTimer > ATTACK_STRIKE;

        if (windup) {
            shapeRenderer.setColor(0.60f, 0.63f, 0.68f, alpha);
            shapeRenderer.rect(shoulderX - 12f, shoulderY + 48f, 24f, 160f);
            shapeRenderer.setColor(0.80f, 0.82f, 0.86f, alpha);
            shapeRenderer.rect(shoulderX - 8f, shoulderY + 48f, 8f, 160f);
        } else {
            float reach = 240f;
            float bladeY = bottomY + 16f;
            float bladeX = facingRight ? centerX + 24f : centerX - 24f - reach;
            shapeRenderer.setColor(0.62f, 0.65f, 0.70f, alpha);
            shapeRenderer.rect(bladeX, bladeY, reach, 24f);
            shapeRenderer.setColor(0.82f, 0.85f, 0.88f, alpha);
            shapeRenderer.rect(bladeX, bladeY + 20f, reach, 8f);
        }
    }
}
