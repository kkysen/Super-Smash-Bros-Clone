package com.github.kkysen.supersmashbros.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.kkysen.libgdx.util.ExtensionMethods;
import com.github.kkysen.libgdx.util.Loggable;
import com.github.kkysen.libgdx.util.Renderable;
import com.github.kkysen.supersmashbros.ai.AI;

import lombok.experimental.ExtensionMethod;

/**
 * The {@link World} class is the main class encompassing the whole game. It
 * contains a {@link #background} {@link Texture} and a single rectangular
 * {@link #platform}. It also has an {@link Array}&lt;{@link Player}&gt; for all
 * the {@link #players}, although I assume it will normally just be two
 * {@link #players}. When the {@link World} is rendered, it loops through all
 * the {@link #players}, updating them, checking if anyone won the game yet,
 * checking if anyone died by falling off, checking if they hit the
 * {@link #platform}, and then rendering the {@link #players} themselves.
 * 
 * @author Khyber Sen
 */
@ExtensionMethod(ExtensionMethods.class)
public class World implements Renderable, Disposable, Loggable {
    
    private final int width;
    private final int height;
    
    private final TextureRegion background;
    public final Rectangle bounds;
    public final Platform platform;
    
    public final float gravity = -500; // FIXME
    
    private final Array<Player> players;
    
    public boolean gameOver = false;
    
    public World(final int width, final int height, final Texture background, final Sprite platform,
            final Player... players) {
        this.width = width;
        this.height = height;
        this.background = new TextureRegion(background, 0, 0, width, height);
        bounds = new Rectangle(0, 0, width, height);
        platform.setSize(width * 0.25f, height * 0.1f);
        platform.setCenter(width * 0.5f, height * 0.25f);
        this.platform = new Platform(platform);
        this.players = new Array<>(players);
        final Rectangle platformBounds = this.platform.bounds;
        final float platformTop = platformBounds.maxY();
        final float platformLeft = platformBounds.x;
        final float platformRight = platformBounds.maxX();
        for (final Player player : players) {
            player.world = this;
            player.position.x = MathUtils.random(platformLeft, platformRight);
            player.position.y = platformTop + MathUtils.random(10f);
        }
    }
    
    @Override
    public String toString() {
        return "World";
    }
    
    private boolean someoneWon() {
        int numAlive = 0;
        for (final Player player : players) {
            if (player.lives > 0) {
                numAlive++;
            }
        }
        return numAlive == 1;
    }
    
    private void updateAndRenderPlayers(final Batch batch) {
        for (int i = 0; i < players.size; i++) {
            final Player player = players.removeIndex(i);
            log("updating " + player);
            if (player.isAI()) {
                ((AI) player.controller).makeDecisions(player, players);
            }
            player.update(players);
            
            // FIXME check this game logic
            if (!player.isCompletelyDead()) {
                players.add(player);
                players.swap(i, players.size - 1);
                if (!player.isAlive()) {
                    player.lives--;
                    player.reSpawn(batch);
                }
            } else {
                log(player + " has been killed");
                // already removed from array
                player.kill();
            }
            log(player + " rendered");
            player.render(batch);
        }
    }
    
    @Override
    public void render(final Batch batch) {
        log("rendering background and platform");
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        platform.render(batch);
        if (gameOver) {
            return;
        }
        log("rendering " + this + " with " + players.size + " players");
        if (players.size == 0) {
            log("nobody won");
            finishGame();
            return;
        }
        if (false/*someoneWon()*/) {
            players.sort((x, y) -> y.lives - x.lives);  //want greatest lives first
            log(players.get(0) + " has won");
            // TODO other stuff should be done here eventually
            finishGame();
            return;
        }
        updateAndRenderPlayers(batch);
    }
    
    private void finishGame() {
        error("      game over");
        gameOver = true;
    }
    
    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        //background.dispose();
    }
    
}
