package com.magic.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.MotorJoint;
import com.badlogic.gdx.physics.box2d.joints.MotorJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import java.util.*;

public class Gladiator extends ApplicationAdapter {

    public static final float ENTITY_DAMPING = 20.0f;
    public static final float ENTITY_RADIUS = 6;
    public static final float ENTITY_DENSITY = 0.4f;
    public static final float PLAYER_SPEED = 10.0f;
    public static final float BOX_TO_WORLD = 10f;
    public static final float WORLD_TO_BOX = 0.1f;
    public static final float MAX_ENTITY_SPEED = 4.0f;
    public static final float ATTACK_COOLDOWN = 0.2f;
    public static final float ATTACK_FORCE = 40f;

    final float VIRTUAL_HEIGHT = 180f;
    OrthographicCamera cam;
    Box2DDebugRenderer debugRenderer;
	SpriteBatch batch;
    World world;
    float screenWidth, screenHeight;

    boolean showDebug = true;
    float debugCoolDown;

	Entity player;
	Texture background, hitboxImage;

	List<Entity> ents;
    List<Ai> ais;

    float attackCooldown = 0;
    Rectangle hitbox;
    Vector2 hitBoxOffset = new Vector2();
    Vector2 hitBoxSize = new Vector2(20, 20);
    private float elapsedTime = 0;

	@Override
	public void create () {
        world = new World(new Vector2(), false);
        cam = new OrthographicCamera();
        debugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();

		background = new Texture("background.png");
        hitboxImage = new Texture("hitbox.png");

		ents = new ArrayList<Entity>();
        ais = new ArrayList<Ai>();
		player = buildEntity(new Vector2(80, 80));
        player.health = 4;
        ents.add(player);

        addWave(5);

        hitBoxOffset = new Vector2();
        hitBoxSize = new Vector2(20, 20);
	}

    private void addWave(int size) {
        for (int i = 0; i < size; i++) {
            addEnemy();
        }
    }

    private void addEnemy() {
        Entity ent = buildEntity(getRandomPosition());
        ents.add(ent);
        ais.add(new Ai(ent));
    }

    private Vector2 getRandomPosition() {
        float xpos = MathUtils.random(0, 640);
        float ypos = MathUtils.random(0, 480);
        return new Vector2(xpos, ypos);
    }

    public void resize (int width, int height) {
        screenWidth = VIRTUAL_HEIGHT * width / (float)height;
        screenHeight = VIRTUAL_HEIGHT;
        cam.setToOrtho(false, (VIRTUAL_HEIGHT * width / (float)height), (VIRTUAL_HEIGHT));
        batch.setProjectionMatrix(cam.combined);
    }

	public Entity buildEntity(Vector2 pos) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(pos.x * WORLD_TO_BOX, pos.y * WORLD_TO_BOX);
        Body body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setLinearDamping(ENTITY_DAMPING * WORLD_TO_BOX);
        CircleShape shape = new CircleShape();
        shape.setRadius(ENTITY_RADIUS * WORLD_TO_BOX);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = ENTITY_DENSITY * WORLD_TO_BOX;
        fixtureDef.friction = 0;
        Fixture fixture = body.createFixture(fixtureDef);
        shape.dispose();
        return new Entity(pos, body);
    }

	@Override
	public void render () {
        handleInput();

        world.step(Gdx.graphics.getDeltaTime(), 6, 2);
        cam.position.set(player.sprite.getX(), player.sprite.getY(), 0);
        cam.update();
        batch.setProjectionMatrix(cam.combined);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(background, 0, 0);
        Collections.sort(ents, new Comparator<Entity>() {
            @Override
            public int compare(Entity o1, Entity o2) {
                return (int)(o2.getPos().y - o1.getPos().y);
            }
        });
		for (Entity ent : ents) {
			ent.sprite.draw(batch);

            if (showDebug && ent.body != null) {
                Rectangle entHit = getEntityHitBox(ent);
                batch.draw(hitboxImage, entHit.x, entHit.y, entHit.width, entHit.height);
            }
		}
        handleUpdate();
        if (showDebug && attackCooldown > 0) {
            batch.draw(hitboxImage, hitbox.getX(), hitbox.getY(), hitbox.getWidth(), hitbox.height);
        }
        elapsedTime += Gdx.graphics.getDeltaTime();

		batch.end();
        if (showDebug) {
            debugRenderer.render(world, cam.combined.cpy().scl(BOX_TO_WORLD));
        }
	}

    public void drawRect(Rectangle rect) {
        batch.draw(hitboxImage, rect.x, rect.y, rect.width, rect.height);
    }

	public static Rectangle getEntityHitBox(Entity ent) {
        Vector2 offset = new Vector2(ENTITY_RADIUS * 0.5f, ENTITY_RADIUS * 0.5f);
        return new Rectangle(ent.getPos().x - offset.x, ent.getPos().y - offset.y, ENTITY_RADIUS, ENTITY_RADIUS);
    }

	public void handleUpdate() {

        // player hit
        if (attackCooldown > 0) {
            hitbox = new Rectangle(
                    player.getPos().x + hitBoxOffset.x,
                    player.getPos().y + hitBoxOffset.y,
                    hitBoxSize.x, hitBoxSize.y);
            for (Entity ent : ents) {
                if (ent != player && ent.health > 0) {
                    if (Intersector.overlaps(getEntityHitBox(ent), hitbox)) {
                        if (ent.takeDamage(1) ) {
                            Vector2 dir = ent.getPos().cpy().sub(player.getPos()).nor().scl(ATTACK_FORCE);
                            ent.body.applyForceToCenter(dir, true);
                        }
                    }
                }
            }
        }

        for (Ai ai : ais) {
            ai.update(player, this, elapsedTime);
        }
        player.update(elapsedTime);

        // remove dead ents
        Iterator<Entity> entIter = ents.iterator();
        while(entIter.hasNext()) {
            Entity ent = entIter.next();
            if (ent.health <= 0 && ent.body != null && ent != player) {
                world.destroyBody(ent.body);
                ent.body = null;
            }
        }
    }

	public void handleInput() {

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        debugCoolDown = debugCoolDown - Gdx.graphics.getDeltaTime();
        if (debugCoolDown < 0 && Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            debugCoolDown = 0.2f;
            showDebug = !showDebug;
        }
        if (player.health < 1) {
            return;
        }

        boolean leftArrow = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightArrow = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean upArrow = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downArrow = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean hitLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean hitRight = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean hitUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean hitDown = Gdx.input.isKeyPressed(Input.Keys.S);
        boolean attackButton = hitDown || hitLeft || hitUp || hitRight;

        float delta = Gdx.graphics.getDeltaTime();
        float playerMove = PLAYER_SPEED * delta;
        if (player.state == Entity.State.STUNNED) {
            playerMove = 0;
        }
        Vector2 playerPos = player.getPos();
        attackCooldown = attackCooldown - delta;
        player.setIsRunning(leftArrow || rightArrow || upArrow || downArrow);
        if (leftArrow && !player.isAttacking) {
            player.body.applyLinearImpulse(-playerMove, 0, playerPos.x,playerPos.y, true);
            player.setIsRight(false);
        }
        if (rightArrow && !player.isAttacking) {
            player.body.applyLinearImpulse(playerMove, 0, playerPos.x,playerPos.y, true);
            player.setIsRight(true);
        }
        if (upArrow && !player.isAttacking) {
            player.body.applyLinearImpulse(0, playerMove, playerPos.x,playerPos.y, true);
        }
        if (downArrow && !player.isAttacking) {
            player.body.applyLinearImpulse(0, -playerMove, playerPos.x,playerPos.y, true);
        }
        if (attackButton) {
            if (attackCooldown < 0) {
                player.setIsAttacking(true);
                attackCooldown = ATTACK_COOLDOWN;
                hitBoxOffset = new Vector2(-ENTITY_RADIUS, -ENTITY_RADIUS);
                hitBoxSize = new Vector2(20, 20);
                Vector2 hitBoxSizeHalf = hitBoxSize.cpy().scl(0.5f);
                float doubleRadius = 2 * ENTITY_RADIUS;
                if (hitDown) {
                    hitBoxOffset.y = hitBoxOffset.y - (doubleRadius + hitBoxSizeHalf.y);
                }
                if (hitUp) {
                    hitBoxOffset.y = hitBoxOffset.y + (doubleRadius + hitBoxSizeHalf.y);
                }
                if (hitRight) {
                    hitBoxOffset.x = hitBoxOffset.x + (doubleRadius + hitBoxSizeHalf.x);
                }
                if (hitLeft) {
                    hitBoxOffset.x = hitBoxOffset.x - (doubleRadius + hitBoxSizeHalf.x);
                }
            }
        }
        if (attackCooldown < 0) {
            player.setIsAttacking(false);
        }


    }
}
