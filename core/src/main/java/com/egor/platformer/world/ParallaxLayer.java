package com.egor.platformer.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ParallaxLayer {

    private final Texture texture;
    private final float parallaxFactor;
    private final float worldWidth;
    private final float worldHeight;
    private final float worldY;

    public ParallaxLayer(Texture texture, float parallaxFactor,
                         float worldWidth, float worldHeight, float worldY) {
        this.texture = texture;
        this.parallaxFactor = parallaxFactor;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.worldY = worldY;
    }

    public void draw(SpriteBatch batch, OrthographicCamera camera) {
        float camX = camera.position.x;

        float viewLeft = camX - camera.viewportWidth / 2f * camera.zoom;

        // Shift makes this layer move parallaxFactor times the camera.
        float offsetX = camX * (1f - parallaxFactor);

        float startX = viewLeft + offsetX;
        startX = (float) Math.floor(startX / worldWidth) * worldWidth - worldWidth;

        float endX = viewLeft + camera.viewportWidth * camera.zoom + worldWidth;

        for (float x = startX; x < endX; x += worldWidth) {
            batch.draw(texture, x, worldY, worldWidth, worldHeight);
        }
    }

    public void dispose() {
        texture.dispose();
    }
}
