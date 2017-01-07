package com.magic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class MetaGame {


    public GameState gameState;
    Texture currentTexture, controlsTex, ideaTex, winTex, loseTex, selectTex, shopTex,
            shopExitButton, shopBuyButton;
    Animation countdownAnim;
    float elapsedTime;
    boolean isPlayAgainSelected;
    int shopSelectIndex = 0;
    Vector2[] buttonPositions = new Vector2[] {
            new Vector2(181, 5),
            new Vector2(181, 26),
            new Vector2(181, 44),
            new Vector2(181, 62)
    };
    private BitmapFont font;
    private int playerSouls;

    MetaGame () {
        gameState = GameState.GAMEPLAY;
        controlsTex = new Texture("controls.png");
        ideaTex = new Texture("idea.png");
        winTex = new Texture("you-win.png");
        loseTex = new Texture("you-lose.png");
        selectTex = new Texture("select-box.png");
        shopTex = new Texture("shop-screen.png");
        shopExitButton = new Texture("exit-button.png");
        shopBuyButton = new Texture("buy-button.png");
        TextureRegion[][] idleRegions = TextureRegion.split(new Texture("countdown.png"), 40, 40);
        countdownAnim = new Animation(1f, idleRegions[0]);
        elapsedTime = 0;
        isPlayAgainSelected = true;
        FileHandle handle = Gdx.files.internal("MavenPro-regular.ttf");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(handle);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 14;
        parameter.kerning = false;
        font = generator.generateFont(parameter);
        playerSouls = 0;
    }

    public void moveCursor(boolean up) {
        if (gameState == GameState.LOSE || gameState == GameState.WIN) {
            isPlayAgainSelected = !isPlayAgainSelected;
        }
        if (gameState == GameState.SHOP) {
            if (up) {
                shopSelectIndex = shopSelectIndex + 1;
            } else {
                shopSelectIndex = shopSelectIndex - 1;
            }
            shopSelectIndex = MathUtils.clamp(shopSelectIndex, 0, 3);
        }
    }

    public void update(Gladiator game) {
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
        if (gameState == GameState.SHOP) {
            currentTexture = shopTex;
        }
        if (gameState == GameState.NIGHT) {
            currentTexture = null;
        }
        if (gameState == GameState.COUNTDOWN) {
            currentTexture = null;
        }
        if (gameState == GameState.VICTORY) {
            currentTexture = null;
        }
        this.playerSouls = game.player.souls;
    }

    public void playAgain() {
        elapsedTime = 0;
        gameState = GameState.COUNTDOWN;
    }

    public void render(SpriteBatch batch, Vector2 pos) {
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
            if (gameState == GameState.SHOP) {
                Vector2 selectPos = buttonPositions[shopSelectIndex];
                if (shopSelectIndex == 0) {
                    batch.draw(shopExitButton, pos.x + selectPos.x, pos.y + selectPos.y);
                } else {
                    batch.draw(shopBuyButton, pos.x + selectPos.x, pos.y + selectPos.y);
                }

                font.draw(batch, String.valueOf(playerSouls), pos.x + 220, pos.y + 122);
            }
        } else {
            if (gameState == GameState.COUNTDOWN) {
                elapsedTime = elapsedTime + Gdx.graphics.getDeltaTime();
                batch.draw(countdownAnim.getKeyFrame(elapsedTime, false), pos.x + 109, pos.y + 65);
                if (countdownAnim.isAnimationFinished(elapsedTime)) {
                    gameState = GameState.GAMEPLAY;
                }
            }
        }
        batch.end();
    }

    public boolean isSelectScreen() {
        return gameState == GameState.LOSE || gameState == GameState.WIN;
    }

    public boolean isRenderingGame() {
        return gameState == GameState.GAMEPLAY || gameState == GameState.COUNTDOWN || gameState == GameState.NIGHT
                || isSelectScreen() || gameState == GameState.VICTORY || gameState == GameState.SCREEN_TRANSITION;
    }

    public boolean isPaused() {
        return !(gameState == GameState.GAMEPLAY || gameState == GameState.NIGHT || gameState == GameState.VICTORY);
    }

    public void setState(GameState state) {
        this.gameState = state;
    }

    public void updateGamestate(Gladiator game) {
        if (gameState == GameState.CONTROLS) {
            gameState = GameState.IDEA;
            return;
        }
        if (gameState == GameState.IDEA) {
            game.resetPlayerPosition();
            gameState = GameState.COUNTDOWN;
            return;
        }
        if (gameState == GameState.LOSE) {
            if (!isPlayAgainSelected) {
                game.nextState = GameState.PLAYAGAIN;
                game.darkScreenTimer = game.DARK_SCREEN_TIMER;
                game.fadeDirectionOut = true;
            } else {
                Gdx.app.exit();
            }
            return;
        }
        if (gameState == GameState.SHOP) {
            if (shopSelectIndex == 0) {
                gameState = GameState.NIGHT;
            }
            if (shopSelectIndex == 1) {
                if (game.player.souls > 0) {
                    game.player.souls = game.player.souls - 1;
                    Float maxHealth = game.player.params.get("maxHealth");
                    game.player.params.put("maxHealth", maxHealth + 1.0f);
                }
            }
            if (shopSelectIndex == 2) {
                if (game.player.souls > 0) {
                    game.player.souls = game.player.souls - 1;
                    Float damage = game.player.params.get("damage");
                    game.player.params.put("damage", damage + 1.0f);
                }
            }
            if (shopSelectIndex == 3) {
                if (game.player.souls > 0) {
                    game.player.souls = game.player.souls - 1;
                    Float maxSpeed = game.player.params.get("maxSpeed");
                    game.player.params.put("maxSpeed", maxSpeed + 1.0f);
                }
            }
            return;
        }
        if (gameState == GameState.VICTORY) {
            game.nextState = GameState.NIGHT;
            game.darkScreenTimer = game.DARK_SCREEN_TIMER;
            game.fadeDirectionOut = true;
            return;
        }
    }



    public enum GameState {
        IDEA,
        CONTROLS,
        COUNTDOWN,
        GAMEPLAY,
        LOSE,
        WIN,
        VICTORY,
        SCREEN_TRANSITION,
        PLAYAGAIN,
        SHOP,
        NIGHT

    }
}
