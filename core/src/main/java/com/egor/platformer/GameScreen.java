package com.egor.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main gameplay screen. Handles input, physics and rendering of the level.
 */
public class GameScreen implements Screen {

    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    private static final float WORLD_WIDTH = 5120f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float PLAYER_SIZE = 50f;
    private static final float MOVE_SPEED = 300f;
    private static final float GRAVITY = -1500f;
    private static final float JUMP_FORCE = 700f;

    private static final float DASH_SPEED = 900f;
    private static final float DASH_DURATION = 0.15f;
    private static final float DASH_COOLDOWN = 0.4f;

    private static final float ATTACK_DURATION = 0.15f;
    private static final float ATTACK_COOLDOWN = 0.35f;
    private static final float ATTACK_WIDTH = 55f;
    private static final float ATTACK_HEIGHT = 40f;

    private static final float DEATH_Y = -100f;
    private static final float RUN_DUST_INTERVAL = 0.08f;

    private float playerX = 300f;
    private float playerY = 300f;
    private float velocityX = 0f;
    private float velocityY = 0f;
    private boolean isOnGround = false;
    private boolean facingRight = true;

    private boolean isDashing = false;
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    private float dashDirection = 1f;

    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private float attackCooldownTimer = 0f;
    private float attackDirection = 1f;

    private boolean wasOnGroundLastFrame = false;
    private float runDustTimer = 0f;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        buildLevel();
    }

    private void buildLevel() {
        platforms.add(new Platform(0, 50, WORLD_WIDTH, 50));
        platforms.add(new Platform(400, 200, 150, 20));
        platforms.add(new Platform(800, 300, 180, 20));
        platforms.add(new Platform(1200, 400, 150, 20));
        platforms.add(new Platform(1600, 250, 200, 20));
        platforms.add(new Platform(2100, 350, 160, 20));
        platforms.add(new Platform(2600, 200, 150, 20));
        platforms.add(new Platform(3000, 400, 180, 20));
        platforms.add(new Platform(3500, 300, 200, 20));
        platforms.add(new Platform(4000, 250, 150, 20));
        platforms.add(new Platform(4500, 350, 180, 20));
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        applyPhysics(delta);
        updateParticles(delta);
        updateCamera();
        draw();
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {
            tryStartDash();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            tryStartAttack();
        }

        if (!isDashing) {
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround && !isDashing) {
            velocityY = JUMP_FORCE;
            isOnGround = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void tryStartDash() {
        if (isDashing) return;
        if (dashCooldownTimer > 0f) return;

        isDashing = true;
        dashTimer = DASH_DURATION;
        dashCooldownTimer = DASH_COOLDOWN;
        dashDirection = facingRight ? 1f : -1f;

        emitDashTrail();
    }

    private void tryStartAttack() {
        if (isAttacking) return;
        if (attackCooldownTimer > 0f) return;

        isAttacking = true;
        attackTimer = ATTACK_DURATION;
        attackCooldownTimer = ATTACK_COOLDOWN;
        attackDirection = facingRight ? 1f : -1f;

        emitAttackSpark();
    }

    private void applyPhysics(float delta) {
        if (dashCooldownTimer > 0f) dashCooldownTimer -= delta;
        if (attackCooldownTimer > 0f) attackCooldownTimer -= delta;

        if (isAttacking) {
            attackTimer -= delta;
            if (attackTimer <= 0f) {
                isAttacking = false;
            }
        }

        if (isDashing) {
            dashTimer -= delta;

            velocityX = DASH_SPEED * dashDirection;
            velocityY = 0f;

            if (dashTimer <= 0f) {
                isDashing = false;
                velocityX = 0f;
            }
        } else {
            velocityY += GRAVITY * delta;
        }

        playerX += velocityX * delta;
        resolveX();

        playerY += velocityY * delta;
        resolveY();

        emitMovementParticles(delta);

        if (playerY < DEATH_Y) {
            respawn();
        }
    }

    private void resolveX() {
        Rectangle player = playerBounds();

        for (Platform platform : platforms) {
            Rectangle bounds = platform.getBounds();
            if (!player.overlaps(bounds)) continue;

            if (velocityX > 0) {
                playerX = bounds.x - PLAYER_SIZE;
            } else if (velocityX < 0) {
                playerX = bounds.x + bounds.width;
            }
            player.setPosition(playerX, playerY);

            if (isDashing) {
                isDashing = false;
                velocityX = 0f;
            }
        }
    }

    private void resolveY() {
        Rectangle player = playerBounds();
        isOnGround = false;

        for (Platform platform : platforms) {
            Rectangle bounds = platform.getBounds();
            if (!player.overlaps(bounds)) continue;

            if (velocityY <= 0) {
                playerY = bounds.y + bounds.height;
                isOnGround = true;
            } else {
                playerY = bounds.y - PLAYER_SIZE;
            }
            velocityY = 0f;
            player.setPosition(playerX, playerY);
        }
    }

    private Rectangle playerBounds() {
        return new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
    }

    private Rectangle attackBounds() {
        float centerY = playerY + PLAYER_SIZE / 2f;
        float x = attackDirection > 0 ? playerX + PLAYER_SIZE : playerX - ATTACK_WIDTH;
        float y = centerY - ATTACK_HEIGHT / 2f;
        return new Rectangle(x, y, ATTACK_WIDTH, ATTACK_HEIGHT);
    }

    private void emitMovementParticles(float delta) {
        if (isOnGround && !wasOnGroundLastFrame) {
            emitLandingDust();
        }

        if (isOnGround && Math.abs(velocityX) > 1f && !isDashing) {
            runDustTimer -= delta;
            if (runDustTimer <= 0f) {
                runDustTimer = RUN_DUST_INTERVAL;
                emitRunDust();
            }
        } else {
            runDustTimer = 0f;
        }

        wasOnGroundLastFrame = isOnGround;
    }

    private void emitLandingDust() {
        float centerX = playerX + PLAYER_SIZE / 2f;
        float baseY = playerY;

        for (int i = 0; i < 6; i++) {
            float dirX = MathUtils.random(-1f, 1f);
            float speed = MathUtils.random(80f, 180f);
            particles.add(new Particle(
                centerX, baseY,
                dirX * speed, MathUtils.random(20f, 80f),
                MathUtils.random(4f, 7f),
                MathUtils.random(0.25f, 0.4f),
                0.75f, 0.75f, 0.70f
            ));
        }
    }

    private void emitRunDust() {
        float centerX = playerX + PLAYER_SIZE / 2f;
        float baseY = playerY;

        float dirX = -Math.signum(velocityX);
        particles.add(new Particle(
            centerX - dirX * 15f, baseY,
            dirX * MathUtils.random(30f, 70f), MathUtils.random(10f, 40f),
            MathUtils.random(3f, 5f),
            MathUtils.random(0.2f, 0.3f),
            0.6f, 0.6f, 0.55f
        ));
    }

    private void emitDashTrail() {
        float centerX = playerX + PLAYER_SIZE / 2f;
        float centerY = playerY + PLAYER_SIZE / 2f;

        for (int i = 0; i < 15; i++) {
            float t = i / 14f;
            float offsetX = -dashDirection * (10f + t * 45f);
            float offsetY = MathUtils.random(-18f, 18f);

            float size = MathUtils.random(3f, 6f) * (1f - t * 0.5f);
            float life = MathUtils.random(0.12f, 0.22f) * (1f - t * 0.4f);

            particles.add(new Particle(
                centerX + offsetX, centerY + offsetY,
                0f, MathUtils.random(-10f, 20f),
                size,
                life,
                0.85f, 0.85f, 0.80f
            ));
        }
    }

    private void emitAttackSpark() {
        float centerX = playerX + PLAYER_SIZE / 2f;
        float centerY = playerY + PLAYER_SIZE / 2f;

        float baseX = centerX + attackDirection * 25f;

        for (int i = 0; i < 6; i++) {
            float dirX = attackDirection * MathUtils.random(60f, 160f);
            float dirY = MathUtils.random(-60f, 60f);

            particles.add(new Particle(
                baseX, centerY + MathUtils.random(-15f, 15f),
                dirX, dirY,
                MathUtils.random(2f, 4f),
                MathUtils.random(0.1f, 0.2f),
                1f, 1f, 0.9f
            ));
        }
    }

    private void updateParticles(float delta) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update(delta);
            if (p.isDead()) {
                it.remove();
            }
        }
    }

    private void respawn() {
        playerX = 300f;
        playerY = 300f;
        velocityX = 0f;
        velocityY = 0f;
        isOnGround = false;
        isDashing = false;
        isAttacking = false;
        dashCooldownTimer = 0f;
        attackCooldownTimer = 0f;
        particles.clear();
    }

    private void updateCamera() {
        float targetX = playerX + PLAYER_SIZE / 2f;
        float targetY = playerY + PLAYER_SIZE / 2f;

        float visibleHalfWidth = camera.viewportWidth / 2f * camera.zoom;
        float visibleHalfHeight = camera.viewportHeight / 2f * camera.zoom;

        float clampedX = Math.max(visibleHalfWidth, Math.min(targetX, WORLD_WIDTH - visibleHalfWidth));
        float clampedY = Math.max(visibleHalfHeight, Math.min(targetY, WORLD_HEIGHT - visibleHalfHeight));

        camera.position.set(clampedX, clampedY, 0f);
        camera.update();
    }

    private void draw() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.25f, 0.2f, 1f);
        for (Platform platform : platforms) {
            Rectangle bounds = platform.getBounds();
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        shapeRenderer.end();

        drawParticles();
        drawPlayer();
    }

    private void drawParticles() {
        shapeRenderer.begin(ShapeType.Filled);
        for (Particle p : particles) {
            float a = p.alpha();
            shapeRenderer.setColor(p.r, p.g, p.b, a);
            shapeRenderer.rect(p.x - p.size / 2f, p.y - p.size / 2f, p.size, p.size);
        }
        shapeRenderer.end();
    }

    private void drawPlayer() {
        float centerX = playerX + PLAYER_SIZE / 2f;
        float bottomY = playerY;

        float alpha = isDashing ? 0.6f : 1f;

        float cloakR = 0.15f, cloakG = 0.15f, cloakB = 0.20f;
        float maskR = 0.85f, maskG = 0.85f, maskB = 0.80f;
        float hornR = 0.65f, hornG = 0.65f, hornB = 0.60f;
        float nailR = 0.75f, nailG = 0.75f, nailB = 0.70f;

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(cloakR, cloakG, cloakB, alpha);
        shapeRenderer.rect(centerX - 15f, bottomY, 30f, 40f);

        // Nail position shifts while attacking: it thrusts forward in the attack direction.
        float nailOffset = isAttacking ? 30f : 12f;
        float nailX = facingRight ? centerX + nailOffset : centerX - nailOffset - 8f;
        float nailWidth = isAttacking ? 35f : 8f;
        shapeRenderer.setColor(nailR, nailG, nailB, alpha);
        shapeRenderer.rect(nailX, bottomY + 15f, nailWidth, 8f);

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

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, false);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
