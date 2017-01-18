package com.magic.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import javafx.scene.paint.Color;

public interface Entity {
    void setHealth(int health);
    int getHealth();
    boolean takeDamage(int damage);
    Vector2 getPos();
    void setColor(Color color);
    void draw(SpriteBatch batch);
    Body getBody();
    void update(float elapsedTime);
    void destroyBody();
    PlayerState getState();
    void dispose();
}
