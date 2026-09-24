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

    // Virtual resolution of the camera. 16:9, so it fits any modern monitor.
    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    // Size of the whole level in world units. Wider than the viewport, so the camera can scroll.
    private static final float WORLD_WIDTH = 5120f;
    private static final float WORLD_HEIGHT = 720f;

    private static final float PLAYER_SIZE = 50f;
    private static final float MOVE_SPEED = 300f;
    private static final float GRAVITY = -1500f;
    private static final float JUMP_FORCE = 700f;

    // Player dies below this line, so falling off the world is not an infinite fall.
    private static final float DEATH_Y = -100f;

    private float playerX = 300f;
    private float playerY = 300f;
    private float velocityX = 0f;
    private float velocityY = 0f;
    private boolean isOnGround = false;

    private final List<Platform> platforms = new ArrayList<>();
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        // FitViewport keeps the aspect ratio on any window size and letterboxes if needed.
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        buildLevel();
    }

    private void buildLevel() {
        platforms.add(new Platform(0, 50, WORLD_WIDTH, 50));      // ground spanning the whole world
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
        velocityX = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocityX = -MOVE_SPEED;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocityX = MOVE_SPEED;
        }

        // isKeyJustPressed so the jump triggers once per press, not every frame.
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
            velocityY = JUMP_FORCE;
            isOnGround = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void applyPhysics(float delta) {
        velocityY += GRAVITY * delta;

        // Collisions are resolved one axis at a time. Moving both at once
        // causes the player to snag on platform edges or clip through on high speeds.
        playerX += velocityX * delta;
        resolveX();

        playerY += velocityY * delta;
        resolveY();

        // Safety net: if the player somehow falls out of the world, respawn.
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
    }

    private void updateCamera() {
        // Center the camera on the player, but clamp so the view never goes outside the world.
        float targetX = playerX + PLAYER_SIZE / 2f;
        float targetY = playerY + PLAYER_SIZE / 2f;

        // The visible half-size depends on the viewport aspect ratio.
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

        // Set the viewport before drawing so everything is rendered in world coordinates.
        viewport.apply();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeType.Filled);

        shapeRenderer.setColor(0.3f, 0.25f, 0.2f, 1f);
        for (Platform platform : platforms) {
            Rectangle bounds = platform.getBounds();
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        shapeRenderer.setColor(0.2f, 0.9f, 0.3f, 1f);
        shapeRenderer.rect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        // The viewport must be updated on resize, otherwise the camera keeps the old aspect ratio.
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
