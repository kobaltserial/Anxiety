package com.egor.platformer.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

/**
 * Player character. Owns its own state, input, physics and combat timers.
 * The game loop calls {@link #update(float, List)} every frame and reads
 * the resulting position and flags through getters.
 */
public class Player {

    public static final float SIZE = 50f;
    public static final int MAX_HP = 100;

    private static final float MOVE_SPEED = 300f;
    private static final float GRAVITY = -1500f;
    private static final float JUMP_FORCE = 700f;

    private static final float DASH_SPEED = 900f;
    private static final float DASH_DURATION = 0.15f;
    private static final float DASH_COOLDOWN = 0.4f;

    private static final float ATTACK_WINDUP = 0.08f;
    private static final float ATTACK_STRIKE = 0.12f;
    private static final float ATTACK_TOTAL = ATTACK_WINDUP + ATTACK_STRIKE;
    private static final float ATTACK_COOLDOWN = 0.35f;

    private static final float ATTACK_WIDTH = 55f;
    private static final float ATTACK_HEIGHT = 50f;
    private static final float INVULNERABLE_TIME = 1.2f;
    private static final float DEATH_Y = -100f;

    private static final float ANXIETY_MAX = 100f;
    private static final float ANXIETY_START = 50f;

    // Per-second rate while the player is moving.
    private static final float ANXIETY_GAIN_MOVE = 5f;

    // Per-second loss while standing still. Must be larger than the gain,
// otherwise the player could idle forever without dying.
    private static final float ANXIETY_LOSS_IDLE = 10f;

    // Instant bumps added on specific actions.
    private static final float ANXIETY_GAIN_JUMP = 10f;
    private static final float ANXIETY_GAIN_DASH = 15f;
    private static final float ANXIETY_GAIN_ATTACK = 10f;

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private boolean onGround;
    private boolean facingRight = true;

    private int hp = MAX_HP;
    private float invulnerableTimer;

    private float anxiety = ANXIETY_START;
    private boolean anxietyDeath;

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

    // Set to true by the world when the player took a fatal hit or fell out of bounds.
    // The screen reads this flag and respawns the player.
    private boolean pendingRespawn;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // ------------------------------------------------------------------
    // Public queries
    // ------------------------------------------------------------------

    public Rectangle bounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public Rectangle attackBounds() {
        float centerY = y + SIZE / 2f;
        float ax = attackDirection > 0 ? x + SIZE : x - ATTACK_WIDTH;
        float ay = centerY - ATTACK_HEIGHT / 2f;
        return new Rectangle(ax, ay, ATTACK_WIDTH, ATTACK_HEIGHT);
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

    public float getAnxiety() {
        return anxiety;
    }

    public boolean isAnxietyDeath() {
        return anxietyDeath;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isAttackHitApplied() {
        return attackHitApplied;
    }

    public boolean isStrikeActive() {
        return attacking && attackTimer <= ATTACK_STRIKE;
    }

    public boolean isPendingRespawn() {
        return pendingRespawn;
    }

    public boolean wasOnGroundLastFrame() {
        return wasOnGroundLastFrame;
    }

    public boolean justLanded() {
        return onGround && !wasOnGroundLastFrame;
    }

    public float getVelocityX() {
        return velocityX;
    }
    public boolean isDashJustStarted() {
        return dashJustStarted;
    }

    public void clearDashJustStarted() {
        dashJustStarted = false;
    }

    // ------------------------------------------------------------------
    // Mutators used by the world
    // ------------------------------------------------------------------

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
        dashing = false;
        attacking = false;
        dashCooldownTimer = 0f;
        attackCooldownTimer = 0f;
        invulnerableTimer = 0f;
        hp = MAX_HP;
        anxiety = ANXIETY_START;
        anxietyDeath = false;
        pendingRespawn = false;
    }

    // ------------------------------------------------------------------
    // Frame update
    // ------------------------------------------------------------------

    /**
     * Handles input, timers, physics and collision for the current frame.
     * Called once per frame by the game screen.
     */
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && onGround && !dashing) {
            velocityY = JUMP_FORCE;
            onGround = false;
            anxiety = Math.min(ANXIETY_MAX, anxiety + ANXIETY_GAIN_JUMP);
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
        attackCooldownTimer = ATTACK_COOLDOWN;
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

        boolean moving = Math.abs(velocityX) > 1f || !onGround || dashing;

        if (moving) {
            anxiety += ANXIETY_GAIN_MOVE * delta;
        } else {
            anxiety -= ANXIETY_LOSS_IDLE * delta;
        }

        anxiety = MathUtils.clamp(anxiety, 0f, ANXIETY_MAX);

        if (anxiety <= 0f) {
            anxietyDeath = true;
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

    /**
     * Draws the player using the given shape renderer.
     * The caller is responsible for begin()/end() around the call is NOT
     * needed: this method manages its own ShapeRenderer.begin/end block.
     */
    public void draw(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer) {
        float centerX = getCenterX();
        float bottomY = y;

        // While invulnerable, blink so the player can read the damage state.
        float alpha = 1f;
        if (dashing) alpha = 0.6f;
        if (invulnerableTimer > 0f) {
            alpha = 0.4f + 0.6f * Math.abs(com.badlogic.gdx.math.MathUtils.sin(invulnerableTimer * 20f));
        }

        float cloakR = 0.15f, cloakG = 0.15f, cloakB = 0.20f;
        float maskR = 0.85f, maskG = 0.85f, maskB = 0.80f;
        float hornR = 0.65f, hornG = 0.65f, hornB = 0.60f;
        float nailR = 0.75f, nailG = 0.75f, nailB = 0.70f;

        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(cloakR, cloakG, cloakB, alpha);
        shapeRenderer.rect(centerX - 15f, bottomY, 30f, 40f);

        drawSword(shapeRenderer, centerX, bottomY, alpha, nailR, nailG, nailB);

        shapeRenderer.setColor(maskR, maskG, maskB, alpha);
        shapeRenderer.circle(centerX, bottomY + 48f, 14f);

        shapeRenderer.setColor(hornR, hornG, hornB, alpha);
        shapeRenderer.triangle(
            centerX - 12f, bottomY + 55f,
            centerX - 4f, bottomY + 55f,
            centerX - 10f, bottomY + 72f
        );
        shapeRenderer.triangle(
            centerX + 4f, bottomY + 55f,
            centerX + 12f, bottomY + 55f,
            centerX + 10f, bottomY + 72f
        );
        shapeRenderer.end();
    }

    /**
     * Draws the nail in one of three states: idle (at the side), windup
     * (raised above the head) or strike (slammed forward and down).
     */
    private void drawSword(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer,
                           float centerX, float bottomY, float alpha,
                           float r, float g, float b) {
        shapeRenderer.setColor(r, g, b, alpha);

        float handX = facingRight ? centerX + 12f : centerX - 12f;
        float handY = bottomY + 22f;

        if (!attacking) {
            float x = facingRight ? handX : handX - 8f;
            shapeRenderer.rect(x, handY - 4f, 8f, 25f);
            return;
        }

        boolean windup = attackTimer > ATTACK_STRIKE;

        if (windup) {
            float x = handX - 4f;
            shapeRenderer.rect(x, bottomY + 40f, 8f, 40f);
        } else {
            float reach = 40f;
            float x = facingRight ? handX : handX - reach;
            float y = bottomY + 5f;
            shapeRenderer.rect(x, y, reach, 10f);
        }
    }
}
