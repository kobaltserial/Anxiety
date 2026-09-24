package com.egor.platformer.world;

import com.egor.platformer.entities.Checkpoint;
import com.egor.platformer.entities.Enemy;
import com.egor.platformer.entities.Platform;

/**
 * Builds the hardcoded levels. Coordinates are scaled 4x to match the
 * 2560x1440 viewport so everything reads at a comfortable size.
 */
public class LevelBuilder {

    public static final float DEFAULT_WORLD_WIDTH = 40960f;
    public static final float DEFAULT_WORLD_HEIGHT = 5760f;

    private LevelBuilder() {
    }

    public static Level buildLevel1() {
        Level level = new Level(DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT, 1200f, 1200f);

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

    // Zone 1 (X 0..6800): tutorial. Flat ground, one step up, no pits.
    private static void buildZone1Tutorial(Level level) {
        level.platforms.add(new Platform(0, 400, 6800, 400));
        level.platforms.add(new Platform(1600, 1600, 800, 120));
        level.platforms.add(new Platform(3600, 2400, 800, 120));
    }

    // Zone 2 (X 6800..13600): first pit with a platform in the middle.
    private static void buildZone2FirstPit(Level level) {
        level.platforms.add(new Platform(6800, 400, 1200, 400));
        level.platforms.add(new Platform(9200, 1000, 600, 120));
        level.platforms.add(new Platform(10800, 1600, 800, 120));
        level.platforms.add(new Platform(12400, 400, 1200, 400));
    }

    // Zone 3 (X 13600..20480): vertical ascent through stacked ledges.
    private static void buildZone3Ascent(Level level) {
        level.platforms.add(new Platform(13600, 400, 1000, 400));
        level.platforms.add(new Platform(14800, 1200, 600, 120));
        level.platforms.add(new Platform(15800, 2000, 600, 120));
        level.platforms.add(new Platform(16800, 2800, 600, 120));
        level.platforms.add(new Platform(17800, 3600, 600, 120));
        level.platforms.add(new Platform(18800, 4400, 1680, 120));
    }

    // Zone 4 (X 20480..27200): cave with a low ceiling.
    private static void buildZone4Cave(Level level) {
        level.platforms.add(new Platform(20480, 4400, 6720, 120));
        level.platforms.add(new Platform(20480, 400, 1600, 400));
        level.platforms.add(new Platform(22400, 1400, 1200, 120));
        level.platforms.add(new Platform(24000, 400, 1200, 400));
        level.platforms.add(new Platform(25600, 1400, 1200, 120));
        level.platforms.add(new Platform(26800, 400, 400, 400));
    }

    // Zone 5 (X 27200..34000): dangerous descent over narrow platforms.
    private static void buildZone5DangerousDescent(Level level) {
        level.platforms.add(new Platform(27200, 400, 800, 400));
        level.platforms.add(new Platform(28800, 1600, 480, 120));
        level.platforms.add(new Platform(30000, 1000, 480, 120));
        level.platforms.add(new Platform(31200, 2000, 480, 120));
        level.platforms.add(new Platform(32400, 400, 1600, 400));
    }

    // Zone 6 (X 34000..40960): finale. Wide room with final enemies.
    private static void buildZone6Finale(Level level) {
        level.platforms.add(new Platform(34000, 400, 6960, 400));
        level.platforms.add(new Platform(35200, 1600, 1200, 120));
        level.platforms.add(new Platform(37200, 2400, 1200, 120));
        level.platforms.add(new Platform(39200, 1600, 1200, 120));
    }

    private static void buildEnemies(Level level) {
        level.enemies.add(new Enemy(5200, 800, 4400, 6400, 1f));
        level.enemies.add(new Enemy(7000, 800, 6800, 8000, -1f));
        level.enemies.add(new Enemy(13920, 800, 13600, 14600, 1f));
        level.enemies.add(new Enemy(24400, 800, 24000, 25200, -1f));
        level.enemies.add(new Enemy(32800, 800, 32400, 33600, 1f));
        level.enemies.add(new Enemy(35600, 2000, 35200, 36400, -1f));
    }

    private static void buildCheckpoints(Level level) {
        level.checkpoints.add(new Checkpoint(7000, 800, 240, 600, 7200, 880));
        level.checkpoints.add(new Checkpoint(13800, 800, 240, 600, 14000, 880));
        level.checkpoints.add(new Checkpoint(27400, 800, 240, 600, 27600, 880));
    }
}
