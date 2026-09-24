package com.egor.platformer.world;

import com.egor.platformer.entities.Checkpoint;
import com.egor.platformer.entities.Enemy;
import com.egor.platformer.entities.Platform;

/**
 * Builds the hardcoded levels. Proportions tuned to match Hollow Knight:
 * ground 200 tall, ledges 300x60, checkpoints 80x200.
 */
public class LevelBuilder {

    public static final float DEFAULT_WORLD_WIDTH = 12000f;
    public static final float DEFAULT_WORLD_HEIGHT = 2000f;

    private LevelBuilder() {
    }

    public static Level buildLevel1() {
        Level level = new Level(DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT, 300f, 900f);

        buildZone1Tutorial(level);
        buildZone2FirstPit(level);
        buildZone3Ascent(level);
        buildZone4Cave(level);
        buildZone5Finale(level);
        buildEnemies(level);
        buildCheckpoints(level);

        return level;
    }

    // Zone 1 (X 0..2200): flat ground with a couple of ledges to learn jumps.
    private static void buildZone1Tutorial(Level level) {
        level.platforms.add(new Platform(0, 400, 2200, 200));
        level.platforms.add(new Platform(500, 800, 300, 60));
        level.platforms.add(new Platform(1200, 1100, 300, 60));
    }

    // Zone 2 (X 2200..4500): first pit with a platform in the middle.
    private static void buildZone2FirstPit(Level level) {
        level.platforms.add(new Platform(2200, 400, 500, 200));
        level.platforms.add(new Platform(2900, 600, 300, 60));
        level.platforms.add(new Platform(3400, 900, 300, 60));
        level.platforms.add(new Platform(4000, 400, 500, 200));
    }

    // Zone 3 (X 4500..7000): vertical ascent through stacked ledges.
    private static void buildZone3Ascent(Level level) {
        level.platforms.add(new Platform(4500, 400, 400, 200));
        level.platforms.add(new Platform(4900, 700, 300, 60));
        level.platforms.add(new Platform(5250, 1000, 300, 60));
        level.platforms.add(new Platform(5600, 1300, 300, 60));
        level.platforms.add(new Platform(5950, 1600, 300, 60));
        level.platforms.add(new Platform(6300, 1400, 700, 60));
    }

    // Zone 4 (X 7000..9500): cave with a low ceiling.
    private static void buildZone4Cave(Level level) {
        level.platforms.add(new Platform(7000, 1700, 2500, 100));
        level.platforms.add(new Platform(7000, 400, 600, 200));
        level.platforms.add(new Platform(7800, 800, 400, 60));
        level.platforms.add(new Platform(8400, 400, 500, 200));
        level.platforms.add(new Platform(9100, 800, 400, 60));
    }

    // Zone 5 (X 9500..12000): finale. Wide ground with a small arena.
    private static void buildZone5Finale(Level level) {
        level.platforms.add(new Platform(9500, 400, 2500, 200));
        level.platforms.add(new Platform(10000, 800, 300, 60));
        level.platforms.add(new Platform(10800, 1100, 300, 60));
        level.platforms.add(new Platform(11500, 800, 300, 60));
    }

    private static void buildEnemies(Level level) {
        level.enemies.add(new Enemy(1400, 600, 1300, 1600, 1f));
        level.enemies.add(new Enemy(2800, 600, 2200, 2700, -1f));
        level.enemies.add(new Enemy(4700, 600, 4500, 4900, 1f));
        level.enemies.add(new Enemy(8600, 600, 8400, 8900, -1f));
        level.enemies.add(new Enemy(10400, 600, 9500, 11000, 1f));
    }

    private static void buildCheckpoints(Level level) {
        level.checkpoints.add(new Checkpoint(2600, 600, 80, 200, 2650, 700));
        level.checkpoints.add(new Checkpoint(7300, 600, 80, 200, 7350, 700));
    }
}
