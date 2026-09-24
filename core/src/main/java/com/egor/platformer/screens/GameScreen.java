package com.egor.platformer.screens;

import com.badlogic.gdx.Game;
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
import com.egor.platformer.entities.Checkpoint;
import com.egor.platformer.entities.Enemy;
import com.egor.platformer.entities.Particle;
import com.egor.platformer.entities.Platform;
import com.egor.platformer.entities.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main gameplay screen. Owns the level, enemies, particles, camera and HUD,
 * and drives the player each frame.
 */
public class GameScreen implements Screen {

    private final Game game;

    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    private static final float WORLD_WIDTH = 5120f;
    private static final float WORLD_HEIGHT = 720f;

    private static final int ENEMY_DAMAGE = 50;
    private static final int PLAYER_DAMAGE = 20;
    private static final float ENEMY_SPEED = 80f;

    private static final float RUN_DUST_INTERVAL = 0.08f;

    private static final float HP_BAR_X = 20f;
    private static final float HP_BAR_Y = 660f;
    private static final float HP_BAR_WIDTH = 60f;
    private static final float HP_BAR_HEIGHT = 30f;
    private static final float HP_BAR_GAP = 8f;
    private static final int HP_BARS_COUNT = 5;

    private static final float ENEMY_HP_BAR_WIDTH = 50f;
    private static final float ENEMY_HP_BAR_HEIGHT = 6f;

    private float respawnX = 300f;
    private float respawnY = 300f;

    private float runDustTimer = 0f;
    private boolean wasOnGroundLastFrame = false;

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Checkpoint> checkpoints = new ArrayList<>();

    private Player player;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private OrthographicCamera hudCamera;

    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        hudCamera.update();
        player = new Player(300f, 300f);
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

        enemies.add(new Enemy(700, 350, 800, 980, 1f));
        enemies.add(new Enemy(1650, 300, 1600, 1800, -1f));
        enemies.add(new Enemy(3050, 450, 3000, 3180, 1f));
        enemies.add(new Enemy(4050, 300, 4000, 4150, -1f));

        checkpoints.add(new Checkpoint(1000, 100, 40, 100, 1000, 150));
        checkpoints.add(new Checkpoint(2400, 100, 40, 100, 2400, 150));
        checkpoints.add(new Checkpoint(3800, 100, 40, 100, 3800, 150));
    }

    @Override
    public void render(float delta) {
        handleScreenInput();
        player.update(delta, platforms);

        if (player.isDashJustStarted()) {
            emitDashTrail();
            player.clearDashJustStarted();
        }

        if (player.isPendingRespawn()) {
            respawnPlayer();
        }

        updateEnemies(delta);
        checkCombat();
        checkCheckpoints();
        emitMovementParticles(delta);
        updateParticles(delta);
        updateCamera();
        draw();
    }

    private void handleScreenInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, this));
        }
    }

    private void respawnPlayer() {
        player.respawn(respawnX, respawnY);
        particles.clear();
    }

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            enemy.update(delta, ENEMY_SPEED);
        }
        enemies.removeIf(Enemy::isDead);
    }

    private void checkCombat() {
        Rectangle playerBounds = player.bounds();

        if (player.isStrikeActive() && !player.isAttackHitApplied()) {
            Rectangle attack = player.attackBounds();

            for (Enemy enemy : enemies) {
                if (attack.overlaps(enemy.bounds())) {
                    enemy.takeDamage(ENEMY_DAMAGE);
                    emitAttackHit(enemy);
                    player.markAttackHitApplied();
                    break;
                }
            }
        }

        if (player.getInvulnerableTimer() > 0f) return;

        for (Enemy enemy : enemies) {
            if (playerBounds.overlaps(enemy.bounds())) {
                player.takeDamage(PLAYER_DAMAGE);
                return;
            }
        }
    }

    private void checkCheckpoints() {
        Rectangle playerBounds = player.bounds();

        for (Checkpoint checkpoint : checkpoints) {
            if (checkpoint.isActivated()) continue;

            if (playerBounds.overlaps(checkpoint.getBounds())) {
                checkpoint.activate();
                respawnX = checkpoint.getRespawnX();
                respawnY = checkpoint.getRespawnY();
            }
        }
    }

    private void emitMovementParticles(float delta) {
        if (player.justLanded()) {
            emitLandingDust();
        }

        if (player.isOnGround() && Math.abs(player.getVelocityX()) > 1f && !player.isDashing()) {
            runDustTimer -= delta;
            if (runDustTimer <= 0f) {
                runDustTimer = RUN_DUST_INTERVAL;
                emitRunDust();
            }
        } else {
            runDustTimer = 0f;
        }

        if (player.isDashing() && player.wasOnGroundLastFrame() && !wasOnGroundLastFrame) {
            // no-op; placeholder for future dash-on-ground particles
        }

        wasOnGroundLastFrame = player.isOnGround();
    }

    private void emitLandingDust() {
        float centerX = player.getCenterX();
        float baseY = player.getY();

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
        float centerX = player.getCenterX();
        float baseY = player.getY();

        float dirX = -Math.signum(player.getVelocityX());
        particles.add(new Particle(
            centerX - dirX * 15f, baseY,
            dirX * MathUtils.random(30f, 70f), MathUtils.random(10f, 40f),
            MathUtils.random(3f, 5f),
            MathUtils.random(0.2f, 0.3f),
            0.6f, 0.6f, 0.55f
        ));
    }

    private void emitDashTrail() {
        float centerX = player.getCenterX();
        float centerY = player.getCenterY();
        float dashDir = player.isFacingRight() ? 1f : -1f;

        for (int i = 0; i < 15; i++) {
            float t = i / 14f;
            float offsetX = -dashDir * (10f + t * 45f);
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

    private void emitAttackHit(Enemy enemy) {
        float cx = enemy.getX() + Enemy.SIZE / 2f;
        float cy = enemy.getY() + Enemy.SIZE / 2f;

        for (int i = 0; i < 8; i++) {
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(60f, 160f);

            particles.add(new Particle(
                cx, cy,
                MathUtils.cos(angle) * speed,
                MathUtils.sin(angle) * speed,
                MathUtils.random(2f, 4f),
                MathUtils.random(0.15f, 0.3f),
                1f, 0.9f, 0.6f
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

    private void updateCamera() {
        float targetX = player.getCenterX();
        float targetY = player.getCenterY();

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
        drawPlatforms();
        drawCheckpoints();
        drawEnemies();
        drawParticles();
        drawPlayer();
        drawEnemyHpBars();

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        drawPlayerHud();
    }

    private void drawPlatforms() {
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.25f, 0.2f, 1f);
        for (Platform platform : platforms) {
            Rectangle bounds = platform.getBounds();
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        shapeRenderer.end();
    }

    private void drawCheckpoints() {
        shapeRenderer.begin(ShapeType.Filled);
        for (Checkpoint checkpoint : checkpoints) {
            Rectangle bounds = checkpoint.getBounds();
            if (checkpoint.isActivated()) {
                shapeRenderer.setColor(0.3f, 0.9f, 0.4f, 0.7f);
            } else {
                shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.4f);
            }
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        shapeRenderer.end();
    }

    private void drawEnemies() {
        shapeRenderer.begin(ShapeType.Filled);
        for (Enemy enemy : enemies) {
            shapeRenderer.setColor(0.85f, 0.25f, 0.25f, 1f);
            shapeRenderer.rect(enemy.getX(), enemy.getY(), Enemy.SIZE, Enemy.SIZE);

            float eyeY = enemy.getY() + Enemy.SIZE - 12f;
            float eyeOffsetX = 8f;
            float eyeCenterX = enemy.getX() + Enemy.SIZE / 2f;
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.circle(eyeCenterX - eyeOffsetX, eyeY, 3f);
            shapeRenderer.circle(eyeCenterX + eyeOffsetX, eyeY, 3f);
        }
        shapeRenderer.end();
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
        float centerX = player.getCenterX();
        float bottomY = player.getY();

        float alpha = 1f;
        if (player.isDashing()) alpha = 0.6f;
        if (player.getInvulnerableTimer() > 0f) {
            alpha = 0.4f + 0.6f * Math.abs(MathUtils.sin(player.getInvulnerableTimer() * 20f));
        }

        float cloakR = 0.15f, cloakG = 0.15f, cloakB = 0.20f;
        float maskR = 0.85f, maskG = 0.85f, maskB = 0.80f;
        float hornR = 0.65f, hornG = 0.65f, hornB = 0.60f;
        float nailR = 0.75f, nailG = 0.75f, nailB = 0.70f;

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(cloakR, cloakG, cloakB, alpha);
        shapeRenderer.rect(centerX - 15f, bottomY, 30f, 40f);

        drawSword(centerX, bottomY, alpha, nailR, nailG, nailB);

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

    private void drawSword(float centerX, float bottomY, float alpha, float r, float g, float b) {
        shapeRenderer.setColor(r, g, b, alpha);

        boolean facingRight = player.isFacingRight();
        float handX = facingRight ? centerX + 12f : centerX - 12f;
        float handY = bottomY + 22f;

        if (!player.isAttacking()) {
            float x = facingRight ? handX : handX - 8f;
            shapeRenderer.rect(x, handY - 4f, 8f, 25f);
            return;
        }

        boolean windup = player.getAttackTimer() > 0.12f;

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

    private void drawEnemyHpBars() {
        shapeRenderer.begin(ShapeType.Filled);
        for (Enemy enemy : enemies) {
            float barX = enemy.getX() + Enemy.SIZE / 2f - ENEMY_HP_BAR_WIDTH / 2f;
            float barY = enemy.getY() + Enemy.SIZE + 8f;

            shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.9f);
            shapeRenderer.rect(barX - 1f, barY - 1f, ENEMY_HP_BAR_WIDTH + 2f, ENEMY_HP_BAR_HEIGHT + 2f);

            float fraction = enemy.getHp() / (float) Enemy.MAX_HP;
            shapeRenderer.setColor(0.9f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(barX, barY, ENEMY_HP_BAR_WIDTH * fraction, ENEMY_HP_BAR_HEIGHT);
        }
        shapeRenderer.end();
    }

    private void drawPlayerHud() {
        shapeRenderer.begin(ShapeType.Filled);

        int barsToFill = (int) Math.ceil(player.getHp() / 20f);

        for (int i = 0; i < HP_BARS_COUNT; i++) {
            float x = HP_BAR_X + i * (HP_BAR_WIDTH + HP_BAR_GAP);

            shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.8f);
            shapeRenderer.rect(x - 2f, HP_BAR_Y - 2f, HP_BAR_WIDTH + 4f, HP_BAR_HEIGHT + 4f);

            if (i < barsToFill) {
                shapeRenderer.setColor(0.9f, 0.2f, 0.2f, 1f);
            } else {
                shapeRenderer.setColor(0.3f, 0.1f, 0.1f, 0.6f);
            }
            shapeRenderer.rect(x, HP_BAR_Y, HP_BAR_WIDTH, HP_BAR_HEIGHT);
        }

        float numberX = HP_BAR_X + HP_BARS_COUNT * (HP_BAR_WIDTH + HP_BAR_GAP) + 12f;
        drawNumber(player.getHp(), numberX, HP_BAR_Y + HP_BAR_HEIGHT / 2f, 12f);

        shapeRenderer.end();
    }

    private void drawNumber(int value, float x, float centerY, float digitWidth) {
        String digits = Integer.toString(value);
        float digitHeight = digitWidth * 1.8f;
        float thickness = Math.max(2f, digitWidth * 0.18f);
        float spacing = digitWidth * 0.5f;

        float cursorX = x;

        for (int i = 0; i < digits.length(); i++) {
            int digit = digits.charAt(i) - '0';
            drawDigit(digit, cursorX, centerY, digitWidth, digitHeight, thickness);
            cursorX += digitWidth + spacing;
        }
    }

    private void drawDigit(int digit, float x, float centerY, float w, float h, float t) {
        boolean[] segments = SEVEN_SEGMENT[digit];

        float top = centerY + h / 2f;
        float bottom = centerY - h / 2f;
        float mid = centerY;

        shapeRenderer.setColor(0.95f, 0.95f, 0.9f, 1f);

        if (segments[0]) shapeRenderer.rect(x, top - t, w, t);
        if (segments[1]) shapeRenderer.rect(x, mid, t, h / 2f);
        if (segments[2]) shapeRenderer.rect(x + w - t, mid, t, h / 2f);
        if (segments[3]) shapeRenderer.rect(x, mid - t / 2f, w, t);
        if (segments[4]) shapeRenderer.rect(x, bottom, t, h / 2f);
        if (segments[5]) shapeRenderer.rect(x + w - t, bottom, t, h / 2f);
        if (segments[6]) shapeRenderer.rect(x, bottom, w, t);
    }

    private static final boolean[][] SEVEN_SEGMENT = {
        { true,  true,  true,  false, true,  true,  true  },
        { false, false, true,  false, false, true,  false },
        { true,  false, true,  true,  true,  false, true  },
        { true,  false, true,  true,  false, true,  true  },
        { false, true,  true,  true,  false, true,  false },
        { true,  true,  false, true,  false, true,  true  },
        { true,  true,  false, true,  true,  true,  true  },
        { true,  false, true,  false, false, true,  false },
        { true,  true,  true,  true,  true,  true,  true  },
        { true,  true,  true,  true,  false, true,  true  },
    };

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, false);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
