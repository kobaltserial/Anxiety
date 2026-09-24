package com.egor.platformer;

import com.badlogic.gdx.math.Rectangle;

/**
 * Static rectangular obstacle the player can stand on, bump into or block against.
 */
public class Platform {

    private final Rectangle bounds;

    public Platform(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
