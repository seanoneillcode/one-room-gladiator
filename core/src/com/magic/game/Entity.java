package com.magic.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public interface Entity {
    void setHealth(int health);
    int getHealth();
    boolean takeDamage(int damage);
    Sprite getSprite();
    Vector2 getPos();
    void draw(SpriteBatch batch);
    Body getBody();
    void update(float elapsedTime);
    void destroyBody();
    PlayerState getState();
    void dispose();
}
