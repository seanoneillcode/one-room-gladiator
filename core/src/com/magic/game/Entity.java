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

    public Entity (Texture texture, Vector2 pos, Body body) {
        this.sprite = new Sprite(texture);
        this.sprite.setPosition(pos.x, pos.y);
        this.body = body;
    }

    public void update() {
        if (this.body != null) {
            this.sprite.setPosition(body.getPosition().x * BOX_TO_WORLD, body.getPosition().y * BOX_TO_WORLD);
            Vector2 limitVel = body.getLinearVelocity();
            float speed = limitVel.len();
            if (speed > MAX_ENTITY_SPEED) {
                body.setLinearVelocity(limitVel.nor().scl(MAX_ENTITY_SPEED));
            }
        }
    }
}
