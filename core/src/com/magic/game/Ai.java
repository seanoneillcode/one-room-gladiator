package com.magic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Ai {
    Entity entity;
    float speed = Gladiator.PLAYER_SPEED;

    State state = State.MOVING_PLAYER;
    float attackTimer;
    float attackTimeMax = 0.8f;
    float attackHitStartTime = 0.6f;
    float attackHitEndTime = 0.4f;
    float weaponSize = 20f;
    float attackRange = 40f;
    float pickupTimer;
    float maxPickupTime = 1.0f;

    Rectangle hitBox;

    public Ai(Entity entity) {
        this.entity = entity;
    }

    public void update(Entity player, Gladiator gladiator) {
        if (state == State.DEAD) {
            return;
        }
        attackTimer = attackTimer - Gdx.graphics.getDeltaTime();

        if (entity.state == Entity.State.STUNNED) {
            state = State.STUNNED;
        } else {
            if (state == State.STUNNED) {
                if (entity.state == Entity.State.NORMAL) {
                    state = State.PICKINGUP;
                    pickupTimer = maxPickupTime;
                }
            } else {
                if (state == State.PICKINGUP) {
                    pickupTimer = pickupTimer - Gdx.graphics.getDeltaTime();
                    if (pickupTimer < 0) {
                        state = State.MOVING_PLAYER;
                    }
                } else {
                    if (player.getPos().dst(entity.getPos()) < attackRange) {
                        state = State.ATTACKING;
                    } else {
                        if (attackTimer < 0) {
                            state = State.MOVING_PLAYER;
                        }
                    }
                }
            }
        }
        if (state == State.MOVING_PLAYER) {
            Vector2 dir = player.getPos().cpy().sub(entity.getPos()).nor().scl(speed);
            entity.body.applyLinearImpulse(dir, entity.getPos(), true);
        }
        if (state == State.ATTACKING) {
            if (attackTimer < 0) {
                attackTimer = attackTimeMax;
                Vector2 dir = player.getPos().cpy().sub(entity.getPos()).nor().scl((weaponSize));
                hitBox = new Rectangle(entity.getPos().x + dir.x, entity.getPos().y + dir.y, weaponSize, weaponSize);
            } else {
                if (attackTimer > attackHitEndTime && attackTimer < attackHitStartTime) {
                    handleHitting(player);
                    gladiator.drawRect(hitBox);
                }
            }
        }
        if (entity.health <= 0) {
            state = State.DEAD;
        }
        entity.update();
    }

    private void handleHitting(Entity player) {
        Rectangle playerBox = new Rectangle(player.getPos().x, player.getPos().y, Gladiator.ENTITY_RADIUS*2, Gladiator.ENTITY_RADIUS*2);
        if (hitBox.overlaps(playerBox)) {
            if (player.takeDamage(1)) {
                Vector2 dir = player.getPos().cpy().sub(entity.getPos()).nor().scl((50.0f));
                player.body.applyForceToCenter(dir, true);
            }
        }
    }

    public enum State {
        MOVING_PLAYER,
        ATTACKING,
        DEAD,
        STUNNED,
        PICKINGUP
    }
}
