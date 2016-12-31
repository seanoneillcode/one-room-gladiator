package com.magic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class MetaGame {

    float buttonCooldown = 0.2f;
    float buttonTimer = 0;

    public GameState gameState;
    Texture currentTexture, controlsTex, ideaTex, winTex, loseTex, selectTex;
    Animation countdownAnim;
    float elapsedTime;
    boolean isPlayAgainSelected;

    MetaGame () {
        gameState = GameState.GAMEPLAY;
        controlsTex = new Texture("controls.png");
        ideaTex = new Texture("idea.png");
        winTex = new Texture("you-win.png");
        loseTex = new Texture("you-lose.png");
        selectTex = new Texture("select-box.png");
        TextureRegion[][] idleRegions = TextureRegion.split(new Texture("countdown.png"), 40, 40);
        countdownAnim = new Animation(1f, idleRegions[0]);
        elapsedTime = 0;
        isPlayAgainSelected = true;
    }

    public void update() {
        buttonTimer = buttonTimer - Gdx.graphics.getDeltaTime();
        handleInput();
        if (gameState == GameState.CONTROLS) {
            currentTexture = controlsTex;
        }
        if (gameState == GameState.IDEA) {
            currentTexture = ideaTex;
        }
        if (gameState == GameState.WIN) {
            currentTexture = winTex;
        }
        if (gameState == GameState.LOSE) {
            currentTexture = loseTex;
        }
        if (gameState == GameState.COUNTDOWN) {
            currentTexture = null;
        }
    }

    public void playAgain() {
        elapsedTime = 0;
        gameState = GameState.COUNTDOWN;
    }

    public void render(SpriteBatch batch, Vector2 pos) {
        update();
        batch.begin();
        if (currentTexture != null) {
            batch.draw(currentTexture, pos.x, pos.y);
            if (isSelectScreen()) {
                Vector2 selectPos = pos.cpy().add(new Vector2(60, 16));
                if (!isPlayAgainSelected) {
                    selectPos = pos.cpy().add(new Vector2(60, 54));
                }
                batch.draw(selectTex, selectPos.x, selectPos.y);
            }
        } else {
            elapsedTime = elapsedTime + Gdx.graphics.getDeltaTime();
            batch.draw(countdownAnim.getKeyFrame(elapsedTime, false), pos.x + 109, pos.y + 65);
            if (countdownAnim.isAnimationFinished(elapsedTime)) {
                gameState = GameState.GAMEPLAY;
            }
        }
        batch.end();
    }

    public boolean isSelectScreen() {
        return gameState == GameState.LOSE || gameState == GameState.WIN;
    }

    public boolean isRenderingGame() {
        return gameState == GameState.GAMEPLAY || gameState == GameState.COUNTDOWN || isSelectScreen();
    }

    public boolean isPaused() {
        return gameState != GameState.GAMEPLAY;
    }

    public void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        boolean space = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        boolean enter = Gdx.input.isKeyPressed(Input.Keys.ENTER);
        boolean anyKey = space || enter;
        if (buttonTimer < 0 && anyKey) {
            buttonTimer = buttonCooldown;
            if (gameState == GameState.CONTROLS) {
                gameState = GameState.IDEA;
            } else {
                if (gameState == GameState.IDEA) {
                    gameState = GameState.COUNTDOWN;
                } else {
                    if (gameState == GameState.WIN || gameState == GameState.LOSE) {
                        if (!isPlayAgainSelected) {
                            gameState = GameState.PLAYAGAIN;
                        } else {
                            Gdx.app.exit();
                        }
                    }
                }
            }
        }
        boolean upArrow = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downArrow = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        if (buttonTimer < 0 && (upArrow || downArrow)) {
            buttonTimer = buttonCooldown;
            isPlayAgainSelected = !isPlayAgainSelected;
        }
    }

    public enum GameState {
        IDEA,
        CONTROLS,
        COUNTDOWN,
        GAMEPLAY,
        LOSE,
        WIN,
        PLAYAGAIN
    }
}
