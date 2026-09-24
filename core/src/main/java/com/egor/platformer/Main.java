package com.egor.platformer;

import com.badlogic.gdx.Game;

/**
 * Entry point of the game. Delegates control to the active {@link com.badlogic.gdx.Screen}.
 */
public class Main extends Game {

    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
