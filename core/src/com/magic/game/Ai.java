package com.magic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Ai {
    PlayerEntityImpl playerEntity;
    float speed = Gladiator.PLAYER_SPEED;

    State state = State.MOVING_PLAYER;
    float attackTimer;
    float attackTimeMax = 0.8f;
    float attackHitStartTime = 0.6f;
    float attackHitEndTime = 0.4f;
    float weaponSize = 10f;
    float attackRange = 32f;
    float pickupTimer;
    float maxPickupTime = 1.0f;

    Rectangle hitBox;
    Sound sliceSound, screamSound;
    boolean sliceSoundPlaying;

    public Ai(PlayerEntityImpl playerEntity) {
        this.playerEntity = playerEntity;
        sliceSound = Gdx.audio.newSound(Gdx.files.internal("slice-sound.wav"));
        screamSound = Gdx.audio.newSound(Gdx.files.internal("scream-sound.wav"));
        sliceSoundPlaying = false;
    }

    public void update(Entity target, Gladiator gladiator, float time) {
        attackTimer = attackTimer - Gdx.graphics.getDeltaTime();
        if (playerEntity.health <= 0) {
            if (state != State.DEAD) {
                //screamSound.play(0.9f, MathUtils.random(0.5f, 2.0f), 0);
            }
            state = State.DEAD;
            playerEntity.update(time);
            return;
        }

        if (playerEntity.state == PlayerEntityImpl.State.STUNNED) {
            state = State.STUNNED;
        } else {
            if (state == State.STUNNED) {
                if (playerEntity.state == PlayerEntityImpl.State.NORMAL) {
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
                    if (target == null){
                        state = State.IDLE;
                    } else {
                        if (target instanceof PlayerEntityImpl && target.getPos().dst(playerEntity.getPos()) < attackRange) {
                            state = State.ATTACKING;
                        } else {
                            if (attackTimer < 0) {
                                state = State.MOVING_PLAYER;
                            }
                        }
                    }
                }
            }
        }
        if (target == null){
            state = State.IDLE;
        }
        if (state == State.MOVING_PLAYER) {
            Vector2 dir = target.getPos().cpy().sub(playerEntity.getPos()).nor().scl(speed);
            playerEntity.body.applyLinearImpulse(dir, playerEntity.getPos(), true);
            playerEntity.setIsRunning(true);
            playerEntity.setIsRight(dir.x > 0);
        }

        if (state == State.ATTACKING) {
            playerEntity.setIsRunning(false);
            if (attackTimer < 0) {
                attackTimer = attackTimeMax;
                Vector2 dir = target.getPos().cpy().sub(playerEntity.getPos()).nor().scl((weaponSize));
                playerEntity.setIsRight(dir.x > 0);
            } else {
                if (attackTimer > attackHitEndTime && attackTimer < attackHitStartTime) {
                    handleHitting(target);
                    playerEntity.setIsAttacking(true);
                    gladiator.drawRect(hitBox);
                }
            }
        } else {
            playerEntity.setIsAttacking(false);
            sliceSoundPlaying = false;
        }

        playerEntity.update(time);
    }

    private void handleHitting(Entity target) {
        if (!sliceSoundPlaying) {
            //sliceSound.play(0.6f, MathUtils.random(0.5f, 2.0f), 0);
            sliceSoundPlaying = true;
        }
        Rectangle playerBox = new Rectangle(target.getPos().x, target.getPos().y, Gladiator.ENTITY_RADIUS*2, Gladiator.ENTITY_RADIUS*2);
        Vector2 dir = target.getPos().cpy().sub(playerEntity.getPos()).nor().scl((weaponSize));
        hitBox = new Rectangle(playerEntity.getPos().x + dir.x, playerEntity.getPos().y + dir.y, weaponSize, weaponSize);
        if (hitBox.overlaps(playerBox)) {
            if (target.takeDamage(1)) {
                Vector2 force = target.getPos().cpy().sub(playerEntity.getPos()).nor().scl((Gladiator.ATTACK_FORCE / 2));
                target.getBody().applyForceToCenter(force, true);
            }
        }
    }

    public enum State {
        MOVING_PLAYER,
        ATTACKING,
        DEAD,
        STUNNED,
        PICKINGUP,
        IDLE
    }
}
