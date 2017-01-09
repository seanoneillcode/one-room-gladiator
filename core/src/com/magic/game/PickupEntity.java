package com.magic.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import javafx.scene.paint.Color;

import static com.magic.game.Gladiator.BOX_TO_WORLD;
import static com.magic.game.Gladiator.MAX_ENTITY_SPEED;

public class PickupEntity implements Entity {

    Sprite sprite;
    int imageWidth = 9;
    int imageHeight = 9;
    int health;
    Body body;
    Vector2 lastPos;
    Animation idle;
    PlayerState state;

    public PickupEntity(Vector2 pos, Body body) {
        Vector2 loc = body.getPosition().cpy().scl(BOX_TO_WORLD);
        TextureRegion[][] idleRegions = TextureRegion.split(new Texture("coin.png"), imageWidth, imageHeight);
        idle = new Animation(1/2f, idleRegions[0]);
        this.sprite = new Sprite();
        this.sprite.setPosition(loc.x, loc.y);
        sprite.setSize(imageWidth, imageHeight);
        Color color = Color.hsb(MathUtils.random(360.0f), MathUtils.random(0.8f, 1.0f), MathUtils.random(0.2f, 1.0f));

        sprite.setColor((float)color.getRed(), (float)color.getGreen(), (float)color.getBlue(), 1.0f);
        sprite.setRegion(idle.getKeyFrame(0, true));
        this.body = body;
        this.health = 1;
        this.lastPos = pos.cpy();
        body.setUserData(this);
        state = PlayerState.IDLE;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public boolean takeDamage(int damage) {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return sprite;
    }

    public PlayerState getState() {
        return state;
    }

    @Override
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
//        batch.draw(texture, getPos().x, getPos().y);
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public void update(float elapsedTime) {
        if (this.body != null) {
            Vector2 newPos = body.getPosition().cpy().scl(BOX_TO_WORLD);
            Vector2 offset = new Vector2(3, 8);//new Vector2(sprite.getWidth(), sprite.getHeight()).scl(0.5f);
            this.sprite.setPosition(newPos.x - offset.x, newPos.y - offset.y);
            Vector2 limitVel = body.getLinearVelocity();
            float speed = limitVel.len();
            if (speed > MAX_ENTITY_SPEED ) {
                body.setLinearVelocity(limitVel.nor().scl(MAX_ENTITY_SPEED));
            }
        }
        sprite.setRegion(idle.getKeyFrame(elapsedTime, true));
        if (health <= 0) {
            state = PlayerState.DEAD;
        }
    }

    @Override
    public void destroyBody() {
        this.body = null;
    }
}
