package com.magic.game;

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

    public Entity (Texture texture, Vector2 pos, Body body) {
        this.sprite = new Sprite(texture);
        this.sprite.setPosition(pos.x, pos.y);
        this.body = body;
        this.health = 1;
    }

    public void update() {

        if (this.body != null ) {
            Vector2 newPos = body.getPosition().cpy().scl(BOX_TO_WORLD);
            Vector2 offset = new Vector2(sprite.getWidth(), sprite.getHeight()).scl(0.5f);
            this.sprite.setPosition(newPos.x - offset.x, newPos.y - offset.y);
            Vector2 limitVel = body.getLinearVelocity();
            float speed = limitVel.len();
            if (speed > MAX_ENTITY_SPEED) {
                body.setLinearVelocity(limitVel.nor().scl(MAX_ENTITY_SPEED));
            }
        }
    }

    public Vector2 getPos() {
        return body.getPosition().cpy().scl(BOX_TO_WORLD);
    }
}
