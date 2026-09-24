package com.egor.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
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

    // Dash tuning. Duration is short so the movement feels like an impulse,
    // not like a long slide.
    private static final float DASH_SPEED = 900f;
    private static final float DASH_DURATION = 0.15f;
    private static final float DASH_COOLDOWN = 0.4f;

    private static final float DEATH_Y = -100f;

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

    private final List<Platform> platforms = new ArrayList<>();
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
        updateCamera();
        draw();
    }

    private void handleInput(float delta) {
        // Dash triggers before regular movement so its velocity overrides input.
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {
            tryStartDash();
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

        // isKeyJustPressed so the jump triggers once per press, not every frame.
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
    }

    private void applyPhysics(float delta) {
        if (dashCooldownTimer > 0f) {
            dashCooldownTimer -= delta;
        }

        if (isDashing) {
            // During a dash gravity is ignored and velocity is fixed,
            // so the movement stays snappy and predictable.
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

        // Collisions are resolved one axis at a time. Moving both at once
        // causes the player to snag on platform edges or clip through on high speeds.
        playerX += velocityX * delta;
        resolveX();

        playerY += velocityY * delta;
        resolveY();

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

            // A dash that hits a wall ends immediately. Without this the player
            // would keep grinding against the wall for the rest of the dash.
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

    private void respawn() {
        playerX = 300f;
        playerY = 300f;
        velocityX = 0f;
        velocityY = 0f;
        isOnGround = false;
        isDashing = false;
        dashCooldownTimer = 0f;
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

        // While dashing the knight fades slightly, so the movement reads as fast
        // without adding particles yet.
        if (isDashing) {
            shapeRenderer.setColor(1f, 1f, 1f, 0.5f);
        }

        drawPlayer();
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

        float nailX = facingRight ? centerX + 12f : centerX - 20f;
        shapeRenderer.setColor(nailR, nailG, nailB, alpha);
        shapeRenderer.rect(nailX, bottomY + 15f, 8f, 25f);

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
