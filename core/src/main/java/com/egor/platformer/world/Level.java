package com.egor.platformer.world;

import com.egor.platformer.entities.Checkpoint;
import com.egor.platformer.entities.Enemy;
import com.egor.platformer.entities.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for one level: platforms, enemies, checkpoints and world bounds.
 * Does not own any rendering or update logic; the game screen drives it.
 */
public class Level {

    public final List<Platform> platforms = new ArrayList<>();
    public final List<Enemy> enemies = new ArrayList<>();
    public final List<Checkpoint> checkpoints = new ArrayList<>();

    public final float worldWidth;
    public final float worldHeight;
    public final float spawnX;
    public final float spawnY;

    public Level(float worldWidth, float worldHeight, float spawnX, float spawnY) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }
}
