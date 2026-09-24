package com.egor.platformer.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Pause overlay shown on top of the game screen.
 * Keeps a reference to the game screen so "Resume" can return to the exact same state.
 */
public class PauseScreen implements Screen {

    private static final float VIEWPORT_WIDTH = 1280f;
    private static final float VIEWPORT_HEIGHT = 720f;

    private static final String[] ITEMS = {"Resume", "Restart", "Main Menu"};

    private final Game game;
    private final GameScreen gameScreen;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private int selectedIndex = 0;

    public PauseScreen(Game game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(3f);
    }

    @Override
    public void render(float delta) {
        handleInput();
        draw();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex = (selectedIndex - 1 + ITEMS.length) % ITEMS.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex = (selectedIndex + 1) % ITEMS.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            confirm();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(gameScreen);
        }
    }

    private void confirm() {
        if (selectedIndex == 0) {
            game.setScreen(gameScreen);
        } else if (selectedIndex == 1) {
            game.setScreen(new GameScreen(game));
        } else {
            game.setScreen(new MenuScreen(game));
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(true);
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.12f, 0.12f, 0.18f, 1f);
        shapeRenderer.rect(340f, 180f, 600f, 380f);

        float itemHeight = 60f;
        float itemsStartY = 460f;
        float highlightY = itemsStartY - selectedIndex * itemHeight - 10f;
        shapeRenderer.setColor(0.3f, 0.5f, 0.9f, 0.35f);
        shapeRenderer.rect(380f, highlightY, 520f, itemHeight);
        shapeRenderer.end();

        batch.begin();
        font.getData().setScale(4f);
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "PAUSED", 490f, 640f);

        font.getData().setScale(2.5f);
        for (int i = 0; i < ITEMS.length; i++) {
            float y = itemsStartY - i * itemHeight + 30f;
            font.draw(batch, ITEMS[i], 420f, y);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
