package com.magic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import static com.magic.game.Gladiator.BOX_TO_WORLD;
import static com.magic.game.Gladiator.MAX_ENTITY_SPEED;

public class Entity {
    Sprite sprite;
    Body body;
    int health;
    float damageCooldown = 2.0f;
    float stunTime = 1.4f;
    float damageTimer;
    public State state;

    public Entity (Texture texture, Vector2 pos, Body body) {
        this.sprite = new Sprite(texture);
        this.sprite.setPosition(pos.x, pos.y);
        this.body = body;
        this.health = 2;
        state = State.NORMAL;
    }

    public void update() {
        if (this.body != null ) {
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
        if (damageTimer < stunTime) {
            state = State.NORMAL;
        }
        if (damageTimer >= 0) {
            damageTimer = damageTimer - Gdx.graphics.getDeltaTime();
        }
    }

    public boolean takeDamage(int amount) {
        if (damageTimer < 0) {
            state = State.STUNNED;
            health = health - amount;
            damageTimer = damageCooldown;
            System.out.println("entity takes damage, health = " + health);
            return true;
        }
        return false;
    }

    public Vector2 getPos() {
        return body.getPosition().cpy().scl(BOX_TO_WORLD);
    }

    public enum State {
        STUNNED,
        NORMAL
    }
}
