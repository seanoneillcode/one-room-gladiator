package com.magic.game;

import com.badlogic.gdx.physics.box2d.*;

public class CollisionListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Body a = contact.getFixtureA().getBody();
        Body b = contact.getFixtureB().getBody();
        a.getUserData();
        if(a.getUserData() instanceof PickupEntity && b.getUserData() instanceof PlayerEntityImpl) {
            PlayerEntityImpl playerEntity = (PlayerEntityImpl) b.getUserData();
            PickupEntity pickupEntity = (PickupEntity) a.getUserData();
            pickupEntity.setHealth(-1);
            playerEntity.addSoul();
            return;
        }
        if(b.getUserData() instanceof PickupEntity && a.getUserData() instanceof PlayerEntityImpl) {
            PlayerEntityImpl playerEntity = (PlayerEntityImpl) a.getUserData();
            PickupEntity pickupEntity = (PickupEntity) b.getUserData();
            pickupEntity.setHealth(-1);
            playerEntity.addSoul();
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
