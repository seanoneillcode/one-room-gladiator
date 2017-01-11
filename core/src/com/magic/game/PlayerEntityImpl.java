package com.magic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.sun.media.jfxmedia.events.PlayerStateEvent;

import java.util.HashMap;
import java.util.Map;

import static com.magic.game.Gladiator.BOX_TO_WORLD;
import static com.magic.game.Gladiator.MAX_ENTITY_SPEED;

import javafx.scene.paint.Color;

public class PlayerEntityImpl implements Entity {

    public static final float ATTACK_COOLDOWN = 0.6f;

    Sprite sprite;
    Body body;
    int health;
    float damageCooldown = 0.8f;
    float stunTime = 0.4f;
    float damageTimer;
    private PlayerState state;
    boolean isRight, isRunning;
    AnimState animState;
    Animation idle, run, slow, hurt, die, att, victory;
    int imageWidth = 43;
    int imageHeight = 44;
    float slowCooldown = 0.14f;
    float slowTimer;
    float dieTimer, attTimer;
    Vector2 lastPos;
    Texture shadow;
    Sound thumpSound;
    boolean isVictory;
    int team;
    Color color;
    float attackCooldown = 0;

    Map<String, Float> params;

    public PlayerEntityImpl(Vector2 pos, Body body, Map<String, Float> params, Color color) {
        animState = AnimState.IDLE;
        this.sprite = new Sprite();
        sprite.setSize(imageWidth, imageHeight);
        sprite.setOrigin(imageWidth / 2, imageHeight);

        shadow = new Texture("player-shadow.png");
        TextureRegion[][] idleRegions = TextureRegion.split(new Texture("player-idle.png"), imageWidth, imageHeight);
        idle = new Animation(1/2f, idleRegions[0]);
        TextureRegion[][] runRegions = TextureRegion.split(new Texture("player-run.png"), imageWidth, imageHeight);
        run = new Animation(1/10f, runRegions[0]);
        TextureRegion[][] slowRegions = TextureRegion.split(new Texture("player-slow.png"), imageWidth, imageHeight);
        slow = new Animation(0.8f, slowRegions[0]);
        TextureRegion[][] hurtRegions = TextureRegion.split(new Texture("player-hurt.png"), imageWidth, imageHeight);
        hurt = new Animation(1/2f, hurtRegions[0]);
        TextureRegion[][] victoryRegions = TextureRegion.split(new Texture("victory-dance.png"), imageWidth, imageHeight);
        victory = new Animation(0.5f, victoryRegions[0]);
        TextureRegion[][] dieRegions = TextureRegion.split(new Texture("player-die.png"), imageWidth, imageHeight);
        die = new Animation(1/12f, dieRegions[0]);
        TextureRegion[][] attRegions = TextureRegion.split(new Texture("player-attack.png"), imageWidth, imageHeight);
        att = new Animation(1/30f, attRegions[0]);
        setAnimation(0);
        this.color = color;
        thumpSound = Gdx.audio.newSound(Gdx.files.internal("thump-sound.wav"));

        Vector2 loc = body.getPosition().cpy().scl(BOX_TO_WORLD);
        this.sprite.setPosition(loc.x, loc.y);
        this.body = body;
        this.health = params.get("maxHealth").intValue();
        isRight = true;
        isRunning = false;
        isVictory = false;
        dieTimer = 0;
        attTimer = 0;
        lastPos = pos.cpy();
        body.setUserData(this);
        this.params = new HashMap<String, Float>(params);
        this.team = params.get("team").intValue();
        state = PlayerState.IDLE;
        attackCooldown = 0;
    }

    public int getTeam() {
        return team;
    }

    public void addSoul() {
        this.params.put("souls", params.get("souls") + 1);
    }

    public PlayerState getState() {
        return state;
    }

    public void setIsRight(boolean isRight) {
        this.isRight = isRight;
    }

    public void setIsVictory(boolean isVictory) {
        this.isVictory = isVictory;
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
            loop = false;
        }
        if (animState == AnimState.VICTORY) {
            choice = victory;
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

    public void setState(PlayerState state) {
        if (this.state == PlayerState.MOVING && state != PlayerState.MOVING) {
            slowTimer = slowCooldown;
        }
        if (state == PlayerState.ATTACKING) {
            attackCooldown = ATTACK_COOLDOWN;
        }
        this.state = state;
    }

    public int getSouls() {
        return params.get("souls").intValue();
    }

    public void setSouls(int value) {
        params.put("souls", Integer.valueOf(value).floatValue());
    }

    public boolean isAttackHurting() {
        return attackCooldown > (ATTACK_COOLDOWN / 2f);
    }

    public void update(float time) {
        attackCooldown = attackCooldown - Gdx.graphics.getDeltaTime();
        if (attackCooldown < 0 && state == PlayerState.ATTACKING) {
            state = PlayerState.IDLE;
        }
        if (this.body != null) {
            Vector2 newPos = body.getPosition().cpy().scl(BOX_TO_WORLD);
            Vector2 offset = new Vector2(sprite.getWidth(), sprite.getHeight()).scl(0.5f);
            this.sprite.setPosition(newPos.x - offset.x, newPos.y - offset.y);
            Vector2 limitVel = body.getLinearVelocity();
            float speed = limitVel.len();
            float maxSpeed = params.get("maxSpeed");
            if (speed > maxSpeed ) {
                if (state == PlayerState.STUNNED) {
                    body.setLinearVelocity(limitVel.nor().scl(MAX_ENTITY_SPEED * 3));
                } else {
                    body.setLinearVelocity(limitVel.nor().scl(maxSpeed));
                }
            }
        }

        if (state == PlayerState.ATTACKING) {
            animState = AnimState.ATT;
        } else {
            attTimer = 0;
        }
        if (state == PlayerState.IDLE) {
            animState = AnimState.IDLE;
        }
        if (state == PlayerState.MOVING) {
            animState = AnimState.RUN;
        }
        if (damageTimer >= 0) {
            damageTimer = damageTimer - Gdx.graphics.getDeltaTime();
            animState = AnimState.HURT;
            if (damageTimer < stunTime) {
                animState = AnimState.SLOW;
            }
            state = PlayerState.STUNNED;
        } else {
            if (state == PlayerState.STUNNED) {
                state = PlayerState.IDLE;
            }
        }
        if (slowTimer >= 0) {
            animState = AnimState.SLOW;
            slowTimer = slowTimer - Gdx.graphics.getDeltaTime();
        }
        if (!isRunning && isVictory) {
            animState = AnimState.VICTORY;
        }
        if (health <= 0) {
            animState = AnimState.DIE;
            state = PlayerState.DEAD;
        }
        setAnimation(time);
        int maxHealth = params.get("maxHealth").intValue();
        float value = MathUtils.clamp(((float) health / (float) maxHealth) * 1.2f, 0f, 1.0f);
        Color midColor = color.deriveColor(0, 1.0f, value, 1.0f);
        getSprite().setColor((float)midColor.getRed(), (float)midColor.getGreen(), (float)midColor.getBlue(), 1.0f);
    }

    @Override
    public void destroyBody() {
        this.body = null;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public int getHealth() {
        return this.health;
    }

    public boolean takeDamage(int amount) {
        if (damageTimer < 0) {
            thumpSound.play(0.6f, MathUtils.random(0.5f, 2.0f), 0);
            health = health - amount;
            damageTimer = damageCooldown;
            return true;
        }
        return false;
    }

    @Override
    public Sprite getSprite() {
        return sprite;
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

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(this.shadow, this.sprite.getX(), this.sprite.getY());
    }

    @Override
    public Body getBody() {
        return body;
    }

    public enum AnimState {
        IDLE,
        RUN,
        SLOW,
        HURT,
        DIE,
        ATT,
        VICTORY
    }
}
