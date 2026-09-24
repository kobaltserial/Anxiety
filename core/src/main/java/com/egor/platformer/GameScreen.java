package com.egor.platformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/** Главный игровой экран. Здесь будет вся игровая логика. */
public class GameScreen implements Screen {

    // Наш первый игровой объект — квадрат
    private float playerX = 300f;   // координата X (влево/вправо)
    private float playerY = 200f;   // координата Y (вверх/вниз)
    private float playerSize = 50f; // размер квадрата
    private float speed = 300f;     // пикселей в секунду

    // Инструмент для рисования простых фигур (квадратов, линий, кругов)
    private ShapeRenderer shapeRenderer;

    @Override
    public void show() {
        // Вызывается один раз, когда экран становится активным
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        // Вызывается 60 раз в секунду. delta — время между кадрами в секундах.

        handleInput(delta);   // 1. обрабатываем нажатия клавиш
        update(delta);        // 2. обновляем состояние мира
        draw();               // 3. рисуем всё на экране
    }

    /** Читаем ввод с клавиатуры и двигаем квадрат. */
    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)    || Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerY += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)  || Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerY -= speed * delta;
        }

        // Выход по ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    /** Здесь позже будет физика, коллизии, ИИ врагов и т.д. */
    private void update(float delta) {
        // пока пусто — всё движение делается в handleInput
    }

    /** Очищаем экран и рисуем квадрат. */
    private void draw() {
        // 1. Заливаем экран тёмно-синим цветом (R, G, B, A — от 0 до 1)
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2. Готовим ShapeRenderer к рисованию залитых фигур
        shapeRenderer.begin(ShapeType.Filled);

        // 3. Красим в зелёный и рисуем квадрат
        shapeRenderer.setColor(0.2f, 0.9f, 0.3f, 1f);
        shapeRenderer.rect(playerX, playerY, playerSize, playerSize);

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // Освобождаем ресурсы, когда экран больше не нужен
        shapeRenderer.dispose();
    }
}
