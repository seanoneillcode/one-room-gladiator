package com.magic.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Gladiator extends ApplicationAdapter {

    public static final float ENTITY_DAMPING = 30.0f;
    public static final float ENTITY_RADIUS = 12;
    public static final float ENTITY_DENSITY = 0.2f;
    public static final float PLAYER_SPEED = 20.0f;
    public static final float BOX_TO_WORLD = 10f;
    public static final float WORLD_TO_BOX = 0.1f;
    public static final float MAX_ENTITY_SPEED = 10.0f;
    public static final float ATTACK_COOLDOWN = 0.2f;

    final float VIRTUAL_HEIGHT = 480f;
    OrthographicCamera cam;
    Box2DDebugRenderer debugRenderer;
	SpriteBatch batch;
    World world;
    float screenWidth, screenHeight;

    boolean showDebug;
    float debugCoolDown;

	Entity player, enemy;
	Texture background, hitboxImage;

	List<Entity> ents;


    float attackCooldown = 0;
    boolean isAttacking = false;
    Rectangle hitbox;
    Vector2 hitBoxOffset = new Vector2();
    Vector2 hitBoxSize = new Vector2(20, 20);


	@Override
	public void create () {
        world = new World(new Vector2(), false);
        cam = new OrthographicCamera();
        debugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();

		background = new Texture("background.png");
        hitboxImage = new Texture("hitbox.png");

		ents = new ArrayList<Entity>();
		player = buildEntity(new Texture("player.png"), new Vector2(40, 40));
        ents.add(player);
        enemy = buildEntity(new Texture("enemy.png"), new Vector2(80, 100));
        ents.add(enemy);
        enemy = buildEntity(new Texture("enemy.png"), new Vector2(180, 200));
        ents.add(enemy);
        enemy = buildEntity(new Texture("enemy.png"), new Vector2(200, 250));
        ents.add(enemy);

        hitBoxOffset = new Vector2();
        hitBoxSize = new Vector2(20, 20);
	}

    public void resize (int width, int height) {
        screenWidth = VIRTUAL_HEIGHT * width / (float)height;
        screenHeight = VIRTUAL_HEIGHT;
        cam.setToOrtho(false, (VIRTUAL_HEIGHT * width / (float)height), (VIRTUAL_HEIGHT));
        batch.setProjectionMatrix(cam.combined);
    }

	public Entity buildEntity(Texture tex, Vector2 pos) {
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
        return new Entity(tex, pos, body);
    }

	@Override
	public void render () {
        handleInput();
        handleUpdate();
        world.step(Gdx.graphics.getDeltaTime(), 6, 2);
        cam.position.set(player.sprite.getX(), player.sprite.getY(), 0);
        cam.update();
        batch.setProjectionMatrix(cam.combined);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(background, 0, 0);
		for (Entity ent : ents) {
            ent.update();
			ent.sprite.draw(batch);

            if (showDebug) {
                Rectangle entHit = getEntityHitBox(ent);
                batch.draw(hitboxImage, entHit.x, entHit.y, entHit.width, entHit.height);
            }
		}
        if (showDebug && attackCooldown > 0) {
            batch.draw(hitboxImage, hitbox.getX(), hitbox.getY(), hitbox.getWidth(), hitbox.height);
        }

		batch.end();
        if (showDebug) {
            debugRenderer.render(world, cam.combined.cpy().scl(BOX_TO_WORLD));
        }
	}

	public Rectangle getEntityHitBox(Entity ent) {
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
                if (ent != player) {
                    if (Intersector.overlaps(getEntityHitBox(ent), hitbox)) {
                        ent.health = 0;
                    }
                }
            }
        }

        // remove dead ents
        Iterator<Entity> entIter = ents.iterator();
        while(entIter.hasNext()) {
            Entity ent = entIter.next();
            if (ent.health <= 0) {
                world.destroyBody(ent.body);
                entIter.remove();
            }
        }
    }

	public void handleInput() {
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
        Vector2 playerPos = player.getPos();
        if (leftArrow) {
            player.body.applyLinearImpulse(-playerMove, 0, playerPos.x,playerPos.y, true);
        }
        if (rightArrow) {
            player.body.applyLinearImpulse(playerMove, 0, playerPos.x,playerPos.y, true);
        }
        if (upArrow) {
            player.body.applyLinearImpulse(0, playerMove, playerPos.x,playerPos.y, true);
        }
        if (downArrow) {
            player.body.applyLinearImpulse(0, -playerMove, playerPos.x,playerPos.y, true);
        }


        attackCooldown = attackCooldown - delta;
        if (attackButton) {
            if (attackCooldown < 0) {
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

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        debugCoolDown = debugCoolDown - Gdx.graphics.getDeltaTime();
        if (debugCoolDown < 0 && Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            debugCoolDown = 0.2f;
            showDebug = !showDebug;
        }
    }
}
