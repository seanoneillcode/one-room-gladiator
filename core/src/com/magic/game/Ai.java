package com.magic.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class Ai {
    PlayerEntityImpl playerEntity;
//    float speed = Gladiator.PLAYER_SPEED;

    State state = State.MOVING_PLAYER;
    float attackTimer;
    float attackTimeMax = 0.8f;
    float attackHitStartTime = 0.6f;
    float attackHitEndTime = 0.4f;
    float weaponSize = 12f;
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

    public Entity getNearestEntity(List<Entity> ents) {
        float dist = 0;
        Entity found = null;
        Entity self = this.playerEntity;
        for (Entity other : ents) {
            if (other == self || other.getHealth() < 1) {
                continue;
            }
            if (other instanceof PlayerEntityImpl && !isTarget(other)) {
                continue;
            }
            float thisDist = other.getPos().dst2(self.getPos());
            if (thisDist < dist || found == null) {
                found = other;
                dist = thisDist;
            }
        }
        return found;
    }

    public void update(Gladiator gladiator, float time, List<Entity> ents) {
        attackTimer = attackTimer - Gdx.graphics.getDeltaTime();
        Entity target = getNearestEntity(ents);
        if (playerEntity.health <= 0) {
            if (state != State.DEAD) {
                screamSound.play(0.8f, MathUtils.random(0.5f, 2.0f), 0);
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
                        if (isTarget(target) && target.getPos().dst(playerEntity.getPos()) < attackRange) {
                            if (Math.abs(target.getPos().y - playerEntity.getPos().y) < 12) {
                                state = State.ATTACKING;
                            } else {
                                state = State.MOVING_PLAYER;
                            }
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
            float speed = playerEntity.params.get("maxSpeed");
            Vector2 targetPos = target.getPos().cpy();
            if (target instanceof PlayerEntityImpl) {
                if (targetPos.x < playerEntity.getPos().x) {
                    targetPos.x = targetPos.x + (2 * Gladiator.ENTITY_RADIUS);
                } else {
                    targetPos.x = targetPos.x - (2 * Gladiator.ENTITY_RADIUS);
                }
            }
            Vector2 dir = targetPos.sub(playerEntity.getPos()).nor().scl(speed);
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

    private boolean isTarget(Entity target) {
        return target instanceof PlayerEntityImpl && ((PlayerEntityImpl) target).getTeam() != playerEntity.getTeam();
    }

    private void handleHitting(Entity target) {
        if (!sliceSoundPlaying) {
            sliceSound.play(0.4f, MathUtils.random(0.5f, 2.0f), 0);
            sliceSoundPlaying = true;
        }
        Rectangle targetBox = new Rectangle(target.getPos().x - Gladiator.ENTITY_RADIUS,
                target.getPos().y - Gladiator.ENTITY_RADIUS, Gladiator.ENTITY_RADIUS*2, Gladiator.ENTITY_RADIUS*2);
//        Vector2 dir = target.getPos().cpy().sub(playerEntity.getPos()).nor().scl((weaponSize));
//        hitBox = new Rectangle(playerEntity.getPos().x + dir.x, playerEntity.getPos().y + dir.y, weaponSize, weaponSize);
        Vector2 offset = new Vector2(weaponSize * 1.3f, 0);
        if (target.getPos().x < playerEntity.getPos().x) {
            offset.x = offset.x * -1f;
        }
        hitBox = new Rectangle(playerEntity.getPos().x + offset.x - Gladiator.ENTITY_RADIUS,
                playerEntity.getPos().y + offset.y - Gladiator.ENTITY_RADIUS, weaponSize, weaponSize);
        if (hitBox.overlaps(targetBox)) {
            if (target.takeDamage(playerEntity.params.get("damage").intValue())) {
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
