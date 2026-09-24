package com.egor.platformer;

import com.badlogic.gdx.math.Rectangle;

/**
 * Zone that stores the player's respawn position once touched.
 * Stays activated until the level is reloaded.
 */
public class Checkpoint {

    private final Rectangle bounds;
    private final float respawnX;
    private final float respawnY;
    private boolean activated = false;

    public Checkpoint(float x, float y, float width, float height, float respawnX, float respawnY) {
        this.bounds = new Rectangle(x, y, width, height);
        this.respawnX = respawnX;
        this.respawnY = respawnY;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isActivated() {
        return activated;
    }

    public void activate() {
        activated = true;
    }

    public float getRespawnX() {
        return respawnX;
    }

    public float getRespawnY() {
        return respawnY;
    }
}
