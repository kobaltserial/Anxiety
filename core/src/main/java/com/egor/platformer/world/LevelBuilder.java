package com.egor.platformer.world;

import com.egor.platformer.entities.Checkpoint;
import com.egor.platformer.entities.Enemy;
import com.egor.platformer.entities.Platform;

/**
 * Builds the hardcoded levels. Kept separate from GameScreen so the level
 * data can later be moved to JSON without touching the screen code.
 */
public class LevelBuilder {

    public static final float DEFAULT_WORLD_WIDTH = 10240f;
    public static final float DEFAULT_WORLD_HEIGHT = 1440f;

    private LevelBuilder() {
    }

    public static Level buildLevel1() {
        Level level = new Level(DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT, 300f, 300f);

        buildZone1Tutorial(level);
        buildZone2FirstPit(level);
        buildZone3Ascent(level);
        buildZone4Cave(level);
        buildZone5DangerousDescent(level);
        buildZone6Finale(level);
        buildEnemies(level);
        buildCheckpoints(level);

        return level;
    }

    // Zone 1 (X 0..1700): tutorial. Flat ground, one step up, no pits.
    private static void buildZone1Tutorial(Level level) {
        level.platforms.add(new Platform(0, 100, 1700, 100));
        level.platforms.add(new Platform(400, 400, 200, 30));
        level.platforms.add(new Platform(900, 600, 200, 30));
    }

    // Zone 2 (X 1700..3400): first pit with a platform in the middle.
    private static void buildZone2FirstPit(Level level) {
        level.platforms.add(new Platform(1700, 100, 300, 100));
        level.platforms.add(new Platform(2300, 250, 150, 30));
        level.platforms.add(new Platform(2700, 400, 200, 30));
        level.platforms.add(new Platform(3100, 100, 300, 100));
    }

    // Zone 3 (X 3400..5120): vertical ascent through stacked ledges.
    private static void buildZone3Ascent(Level level) {
        level.platforms.add(new Platform(3400, 100, 250, 100));
        level.platforms.add(new Platform(3700, 300, 150, 30));
        level.platforms.add(new Platform(3950, 500, 150, 30));
        level.platforms.add(new Platform(4200, 700, 150, 30));
        level.platforms.add(new Platform(4450, 900, 150, 30));
        level.platforms.add(new Platform(4700, 1100, 420, 30));
    }

    // Zone 4 (X 5120..6800): cave with a low ceiling.
    private static void buildZone4Cave(Level level) {
        level.platforms.add(new Platform(5120, 1100, 1680, 30));
        level.platforms.add(new Platform(5120, 100, 400, 100));
        level.platforms.add(new Platform(5600, 350, 300, 30));
        level.platforms.add(new Platform(6000, 100, 300, 100));
        level.platforms.add(new Platform(6400, 350, 300, 30));
        level.platforms.add(new Platform(6700, 100, 100, 100));
    }

    // Zone 5 (X 6800..8500): dangerous descent over narrow platforms.
    private static void buildZone5DangerousDescent(Level level) {
        level.platforms.add(new Platform(6800, 100, 200, 100));
        level.platforms.add(new Platform(7200, 400, 120, 30));
        level.platforms.add(new Platform(7500, 250, 120, 30));
        level.platforms.add(new Platform(7800, 500, 120, 30));
        level.platforms.add(new Platform(8100, 100, 400, 100));
    }

    // Zone 6 (X 8500..10240): finale. Wide room with final enemies.
    private static void buildZone6Finale(Level level) {
        level.platforms.add(new Platform(8500, 100, 1740, 100));
        level.platforms.add(new Platform(8800, 400, 300, 30));
        level.platforms.add(new Platform(9300, 600, 300, 30));
        level.platforms.add(new Platform(9800, 400, 300, 30));
    }

    private static void buildEnemies(Level level) {
        level.enemies.add(new Enemy(1300, 200, 1100, 1600, 1f));
        level.enemies.add(new Enemy(1750, 200, 1700, 2000, -1f));
        level.enemies.add(new Enemy(3480, 200, 3400, 3650, 1f));
        level.enemies.add(new Enemy(6100, 200, 6000, 6300, -1f));
        level.enemies.add(new Enemy(8200, 200, 8100, 8400, 1f));
        level.enemies.add(new Enemy(8900, 500, 8800, 9100, -1f));
    }

    private static void buildCheckpoints(Level level) {
        level.checkpoints.add(new Checkpoint(1750, 200, 60, 150, 1800, 220));
        level.checkpoints.add(new Checkpoint(3450, 200, 60, 150, 3500, 220));
        level.checkpoints.add(new Checkpoint(6850, 200, 60, 150, 6900, 220));
    }
}
