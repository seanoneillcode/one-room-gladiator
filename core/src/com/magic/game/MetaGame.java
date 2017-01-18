package com.magic.game;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
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
    Texture currentTexture, controlsTex, ideaTex, winTex, loseTex, selectTex, shopTex, restTokenTex,
            shopExitButton, shopBuyButton, adviceTex, playerTokenTex, eventTokenTex, chevronAcrossTex,
            playerIconTex;
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
    private String adviceText = "my advice is to set variables correctly";
    private Vector2 backgroundPos, currentBackgroundPos;

    int[] sleepWave = new int[]{};

    int[] mediumDiffsmallSize = new int[] {2, 4};
    int[] mediumDiffMediumSize = new int[] {3, 5, 7};
    int[] mediumDiffLargeSize = new int[] {0, 4, 4, 4, 8, 10};
    int[] mediumDiffExLargeSize = new int[] {0, 8, 8, 8, 16, 20};

    int[] hardDiffsmallSize = new int[] {2, 5};
    int[] hardDiffmediumSize = new int[] {2, 4, 9};
    int[] hardDiffLargeSize = new int[] {1, 3, 4, 4, 9, 12};
    int[] hardDiffExLargeSize = new int[] {2, 6, 8, 8, 18, 24};

    int[] mediumMelee = new int [] {80};
    int[] largeMelee = new int [] {120};
    int[] exlargeMelee = new int [] {160};
    int[] everyone = new int [] {200};

    int[][] levelWaves = new int[][] {
            mediumDiffsmallSize,
            mediumMelee
//            mediumMelee, mediumDiffsmallSize, sleepWave,
//            mediumDiffMediumSize, mediumDiffLargeSize, sleepWave,
//            largeMelee, hardDiffsmallSize, sleepWave,
//            hardDiffmediumSize, hardDiffLargeSize, sleepWave,
//            exlargeMelee, hardDiffExLargeSize
    };

    int currentWaveIndex;

    MetaGame () {
        gameState = GameState.CONTROLS;
        controlsTex = new Texture("controls.png");
        ideaTex = new Texture("idea.png");
        winTex = new Texture("victory-screen.png");
        loseTex = new Texture("you-lose.png");
        selectTex = new Texture("select-box.png");
        shopTex = new Texture("shop-screen.png");
        shopExitButton = new Texture("exit-button.png");
        shopBuyButton = new Texture("buy-button.png");
        adviceTex = new Texture("talk-bad-screen.png");
        playerTokenTex = new Texture("player-token.png");
        eventTokenTex = new Texture("event-token.png");
        playerIconTex = new Texture("players-icon.png");
        restTokenTex = new Texture("rest-event-token.png");
        chevronAcrossTex = new Texture("chevron-across.png");
        TextureRegion[][] idleRegions = TextureRegion.split(new Texture("countdown.png"), 40, 40);
        countdownAnim = new Animation(1f, idleRegions[0]);
        elapsedTime = 0;
        isPlayAgainSelected = true;
        FileHandle handle = Gdx.files.getFileHandle("test-font.ttf", Files.FileType.Internal);
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(handle);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 14;
        parameter.kerning = false;
        font = generator.generateFont(parameter);
        font.setColor(143f / 255f, 184f / 255f, 196f / 255f, 1f);
        playerSouls = 0;
        backgroundPos = new Vector2();
        currentBackgroundPos = new Vector2();
        currentWaveIndex = 0;
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

    public int[] getWave() {
        return levelWaves[currentWaveIndex];
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
        if (gameState == GameState.ADVICE) {
            currentTexture = adviceTex;
        }
        if (gameState == GameState.PROGRESS) {
            currentTexture = null;
        }
        this.playerSouls = game.player.getSouls();
    }

    public void playAgain() {
        elapsedTime = 0;
        gameState = GameState.COUNTDOWN;
        currentWaveIndex++;
    }

    public void render(SpriteBatch batch, Vector2 pos) {
        if (currentTexture != null) {
            batch.draw(currentTexture, pos.x, pos.y);
            if (gameState == GameState.LOSE) {
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
            if (gameState == GameState.ADVICE) {
                font.drawWrapped(batch, adviceText, pos.x + 10, pos.y + 70, 220);
            }
        } else {
            if (gameState == GameState.COUNTDOWN) {
                elapsedTime = elapsedTime + Gdx.graphics.getDeltaTime();
                batch.draw(countdownAnim.getKeyFrame(elapsedTime, false), pos.x + 109, pos.y + 65);
                if (countdownAnim.isAnimationFinished(elapsedTime)) {
                    gameState = GameState.GAMEPLAY;
                }
            }
            if (gameState == GameState.PROGRESS) {
                Gdx.gl.glClearColor( 0, 0, 0, 1 );
                Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

                if (levelWaves[currentWaveIndex].length == 0) {
                    batch.draw(restTokenTex, currentBackgroundPos.x + pos.x + 50, pos.y + 50);
                    batch.draw(chevronAcrossTex, currentBackgroundPos.x + pos.x + 130, pos.y + 80);

                } else {
                    batch.draw(eventTokenTex, currentBackgroundPos.x + pos.x + 50, pos.y + 50);
                    batch.draw(eventTokenTex, currentBackgroundPos.x + pos.x + 200, pos.y + 50);
                    batch.draw(chevronAcrossTex, currentBackgroundPos.x + pos.x + 130, pos.y + 80);
                    batch.draw(playerTokenTex, pos.x + 92, pos.y + 75);
                    batch.draw(playerIconTex, currentBackgroundPos.x + pos.x + 62, pos.y + 100);
                    font.draw(batch, "x " + getNumPeople(levelWaves[currentWaveIndex]), currentBackgroundPos.x + pos.x + 75, pos.y + 110);
                    font.draw(batch, "" + levelWaves[currentWaveIndex].length + " teams", currentBackgroundPos.x + pos.x + 60, pos.y + 70);
                }

                if (currentWaveIndex + 1 < levelWaves.length) {

                    if (levelWaves[currentWaveIndex + 1].length == 0) {
                        batch.draw(restTokenTex, currentBackgroundPos.x + pos.x + 200, pos.y + 50);
                    } else {
                        batch.draw(eventTokenTex, currentBackgroundPos.x + pos.x + 200, pos.y + 50);
                        font.draw(batch, "x " + getNumPeople(levelWaves[currentWaveIndex + 1]), currentBackgroundPos.x + pos.x + 75 + 150, pos.y + 110);
                        font.draw(batch, "" + levelWaves[currentWaveIndex + 1].length + " teams", currentBackgroundPos.x + pos.x + 60 + 150, pos.y + 70);
                        batch.draw(playerIconTex, currentBackgroundPos.x + pos.x + 62 + 150, pos.y + 100);
                    }

                } else {
                    // TODO draw victory token
                    font.draw(batch, "VICTORY", currentBackgroundPos.x + pos.x + 55 + 150, pos.y + 90);
                }
                batch.draw(playerTokenTex, pos.x + 92, pos.y + 75);

                if (currentBackgroundPos.x > backgroundPos.x) {
                    currentBackgroundPos.x = currentBackgroundPos.x - (60.0f * Gdx.graphics.getDeltaTime());
                }
            }
        }
    }

    private int getNumPeople(int[] cur) {
        int res = 0;
        for (int i = 0; i < cur.length; i++) {
            res = res + cur[i];
        }
        return res;
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

    public void pickRandomAdvice() {
        String[] advices = new String[] {
                "Run away to stay alive in the short term",
                "Saw those little glowing things when you wasted someone?\nThose were bio chips",
                "If you don't upgrade your self you'll get crushed later,\nby someone like me!",
                "You might want to just end it now, it doesn't get better",
                "What are you in this for?\nGlory ? Crime? The killing?",
                "A round is either a free for all or team based",
                "If you want to survive longer, target the bigger teams",
                "Keep moving, fodder"
        };
        adviceText = advices[MathUtils.random(advices.length - 1)];
    }

    public void updateGamestate(Gladiator game) {
        if (game.nextState != null) {
            return;
        }
        if (gameState == GameState.CONTROLS) {
            game.fastDanceMusic.loop(SoundPlayer.getMusicVolume());
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
                currentWaveIndex = -1;
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
                if (game.player.getSouls() > 4) {
                    game.player.setSouls(game.player.getSouls() - 5);
                    Float maxHealth = game.player.params.get("maxHealth");
                    game.player.params.put("maxHealth", maxHealth + 0.4f);
                }
            }
            if (shopSelectIndex == 2) {
                if (game.player.getSouls() > 9) {
                    game.player.setSouls(game.player.getSouls() - 10);
                    Float damage = game.player.params.get("damage");
                    game.player.params.put("damage", damage + 0.4f);
                }
            }
            if (shopSelectIndex == 3) {
                if (game.player.getSouls() > 2) {
                    game.player.setSouls(game.player.getSouls() - 3);
                    Float maxSpeed = game.player.params.get("maxSpeed");
                    game.player.params.put("maxSpeed", maxSpeed + 0.4f);
                }
            }
            return;
        }
        if (gameState == GameState.VICTORY) {
            backgroundPos = new Vector2(-131, 0);
            currentBackgroundPos = new Vector2();
            game.nextState = GameState.PROGRESS;
            game.darkScreenTimer = game.DARK_SCREEN_TIMER;
            game.fadeDirectionOut = true;
            game.fastDanceMusic.stop();
            return;
        }
        if (gameState == GameState.ADVICE) {
            gameState = GameState.NIGHT;
            return;
        }
        if (gameState == GameState.PROGRESS) {
            if (currentWaveIndex < (levelWaves.length - 1)) {
                if (levelWaves[currentWaveIndex + 1].length == 0) {
                    game.nextState = GameState.NIGHT;
                    game.darkSlowMusic.loop(SoundPlayer.getMusicVolume());
                } else {
                    game.fastDanceMusic.loop(SoundPlayer.getMusicVolume());
                    game.nextState = GameState.PLAYAGAIN;
                }
            } else {
                game.nextState = GameState.WIN;
            }
            game.darkScreenTimer = game.DARK_SCREEN_TIMER;
            game.fadeDirectionOut = true;
        }
        if (gameState == GameState.WIN) {
            currentWaveIndex = -1;
            game.nextState = GameState.EXIT;
            game.darkScreenTimer = game.DARK_SCREEN_TIMER;
            game.fadeDirectionOut = true;
        }
    }

    public void resetDay() {
        currentWaveIndex++;
        backgroundPos = new Vector2(-131, 0);
        currentBackgroundPos = new Vector2();
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
        NIGHT,
        ADVICE,
        PROGRESS,
        EXIT
    }

    public void dispose() {
        controlsTex.dispose();
        ideaTex.dispose();
        winTex.dispose();
        loseTex.dispose();
        selectTex.dispose();
        shopTex.dispose();
        restTokenTex.dispose();
        shopExitButton.dispose();
        shopBuyButton.dispose();
        adviceTex.dispose();
        playerTokenTex.dispose();
        eventTokenTex.dispose();
        chevronAcrossTex.dispose();
        playerIconTex.dispose();
    }
}
