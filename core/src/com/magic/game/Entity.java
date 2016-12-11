package com.magic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import javafx.scene.paint.Color;

import java.util.List;

import static com.magic.game.Gladiator.BOX_TO_WORLD;
import static com.magic.game.Gladiator.MAX_ENTITY_SPEED;

public class Entity {
    Sprite sprite;
    Body body;
    int health;
    float damageCooldown = 0.8f;
    float stunTime = 0.4f;
    float damageTimer;
    public State state;
    boolean isRight, isRunning;
    AnimState animState;
    Animation idle, run, slow, hurt, die, att;
    int imageWidth = 43;
    int imageHeight = 44;
    float slowCooldown = 0.6f;
    float slowTimer;
    float dieTimer, attTimer;
    public boolean isAttacking;
    Vector2 lastPos;
    Texture shadow;
    Sound thumpSound;

    public Entity (Vector2 pos, Body body) {
        animState = AnimState.IDLE;
        this.sprite = new Sprite();
        sprite.setSize(imageWidth, imageHeight);
        sprite.setOrigin(imageWidth / 2, imageHeight);
        Color color = Color.hsb(MathUtils.random(360.0f), MathUtils.random(0.8f, 1.0f), MathUtils.random(0.2f, 1.0f));

        sprite.setColor((float)color.getRed(), (float)color.getGreen(), (float)color.getBlue(), 1.0f);

        shadow = new Texture("player-shadow.png");
        TextureRegion[][] idleRegions = TextureRegion.split(new Texture("player-idle.png"), imageWidth, imageHeight);
        idle = new Animation(1/2f, idleRegions[0]);
        TextureRegion[][] runRegions = TextureRegion.split(new Texture("player-run.png"), imageWidth, imageHeight);
        run = new Animation(1/10f, runRegions[0]);
        TextureRegion[][] slowRegions = TextureRegion.split(new Texture("player-slow.png"), imageWidth, imageHeight);
        slow = new Animation(1/2f, slowRegions[0]);
        TextureRegion[][] hurtRegions = TextureRegion.split(new Texture("player-hurt.png"), imageWidth, imageHeight);
        hurt = new Animation(1/2f, hurtRegions[0]);
        TextureRegion[][] dieRegions = TextureRegion.split(new Texture("player-die.png"), imageWidth, imageHeight);
        die = new Animation(1/12f, dieRegions[0]);
        TextureRegion[][] attRegions = TextureRegion.split(new Texture("player-attack.png"), imageWidth, imageHeight);
        att = new Animation(1/30f, attRegions[0]);
        setAnimation(0);

        thumpSound = Gdx.audio.newSound(Gdx.files.internal("thump-sound.wav"));

        Vector2 loc = body.getPosition().cpy().scl(BOX_TO_WORLD);
        this.sprite.setPosition(loc.x, loc.y);
        this.body = body;
        this.health = 2;
        state = State.NORMAL;
        isRight = true;
        isRunning = false;
        isAttacking = false;
        dieTimer = 0;
        attTimer = 0;
        lastPos = pos.cpy();
    }

    public void setIsAttacking(boolean isAttacking) {
        this.isAttacking = isAttacking;
        if (!isAttacking) {
            attTimer = 0;
        }
    }

    public void setIsRight(boolean isRight) {
        this.isRight = isRight;
    }

    public void setAnimation(float time) {
        Animation choice = idle;
        boolean loop = true;
        float actualTime = time;
        if (animState == AnimState.SLOW) {
            choice = slow;
        }
        if (animState == AnimState.RUN) {
            choice = run;
        }
        if (animState == AnimState.HURT) {
            choice = hurt;
        }
        if (animState == AnimState.ATT) {
            choice = att;
            loop = false;
            actualTime = attTimer;
            attTimer = attTimer + Gdx.graphics.getDeltaTime();
        }
        if (animState == AnimState.DIE) {
            choice = die;
            loop = false;
            actualTime = dieTimer;
            dieTimer = dieTimer + Gdx.graphics.getDeltaTime();
        }
        TextureRegion region = choice.getKeyFrame(actualTime, loop);
        if (isRight && region.isFlipX()) {
            region.flip(true, false);
        }
        if (!isRight && !region.isFlipX()) {
            region.flip(true, false);
        }
        sprite.setRegion(region);
    }

    public void setIsRunning(boolean isRunning) {
        if (isRunning) {
            animState = AnimState.RUN;
            this.isRunning = true;
        } else {
            if (this.isRunning) {
                slowTimer = slowCooldown;
            }
            if (slowTimer > 0) {
                animState = AnimState.SLOW;
            } else {
                animState = AnimState.IDLE;
            }
            this.isRunning = false;
        }
    }

    public void update(float time) {
        if (this.body != null) {
            Vector2 newPos = body.getPosition().cpy().scl(BOX_TO_WORLD);
            Vector2 offset = new Vector2(sprite.getWidth(), sprite.getHeight()).scl(0.5f);
            this.sprite.setPosition(newPos.x - offset.x, newPos.y - offset.y);
            Vector2 limitVel = body.getLinearVelocity();
            float speed = limitVel.len();
            if (speed > MAX_ENTITY_SPEED ) {
                if (state == State.STUNNED) {
                    body.setLinearVelocity(limitVel.nor().scl(MAX_ENTITY_SPEED * 3));
                } else {
                    body.setLinearVelocity(limitVel.nor().scl(MAX_ENTITY_SPEED));
                }
            }
        }
        if (isAttacking) {
            animState = AnimState.ATT;
        }
        if (damageTimer >= 0) {
            damageTimer = damageTimer - Gdx.graphics.getDeltaTime();
            animState = AnimState.HURT;
        }
        if (damageTimer < stunTime) {
            state = State.NORMAL;
            if (damageTimer > 0) {
                animState = AnimState.SLOW;
            }
        }
        slowTimer = slowTimer - Gdx.graphics.getDeltaTime();
        if (health <= 0) {
            animState = AnimState.DIE;
        }
        setAnimation(time);
    }

    public boolean takeDamage(int amount) {
        if (damageTimer < 0) {
            thumpSound.play(0.8f, MathUtils.random(0.5f, 2.0f), 0);
            state = State.STUNNED;
            health = health - amount;
            damageTimer = damageCooldown;
            return true;
        }
        return false;
    }

    public Vector2 getPos() {
        Vector2 pos;
        if (body != null) {
            pos = body.getPosition().cpy().scl(BOX_TO_WORLD);
            lastPos = pos.cpy();
        } else {
            pos = lastPos;
        }
        return pos;
    }

    public enum AnimState {
        IDLE,
        RUN,
        SLOW,
        HURT,
        DIE,
        ATT
    }

    public enum State {
        STUNNED,
        NORMAL
    }
}
