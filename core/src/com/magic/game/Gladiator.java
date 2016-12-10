package com.magic.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.List;

public class Gladiator extends ApplicationAdapter {

    public static final float ENTITY_DAMPING = 30.0f;
    public static final float ENTITY_RADIUS = 12.0f;
    public static final float ENTITY_DENSITY = 0.2f;
    public static final float PLAYER_SPEED = 20.0f;
    public static final float BOX_TO_WORLD = 10f;
    public static final float WORLD_TO_BOX = 0.1f;
    public static final float MAX_ENTITY_SPEED = 10.0f;

    final float VIRTUAL_HEIGHT = 480f;
    OrthographicCamera cam;
    Matrix4 debugMatrix;
    Box2DDebugRenderer debugRenderer;
	SpriteBatch batch;
    World world;
    float screenWidth, screenHeight;

    boolean showDebug;
    float debugCoolDown;

	Entity player, enemy;
	Texture background;

	List<Entity> ents;

	@Override
	public void create () {
        world = new World(new Vector2(), false);
        cam = new OrthographicCamera();
        debugMatrix = new Matrix4(cam.combined);
        debugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();

		background = new Texture("background.png");

		ents = new ArrayList<Entity>();
		player = buildEntity(new Texture("player.png"), new Vector2(40, 40));
        enemy = buildEntity(new Texture("enemy.png"), new Vector2(80, 200));
        ents.add(player);
        ents.add(enemy);
	}

    public void resize (int width, int height) {
        screenWidth = VIRTUAL_HEIGHT * width / (float)height;
        screenHeight = VIRTUAL_HEIGHT;
        cam.setToOrtho(false, (VIRTUAL_HEIGHT * width / (float)height), (VIRTUAL_HEIGHT));
        batch.setProjectionMatrix(cam.combined);
        Matrix4 debugScaled = cam.combined.cpy().setToScaling(
                cam.combined.getScaleX() * BOX_TO_WORLD,
                cam.combined.getScaleY() * BOX_TO_WORLD,
                cam.combined.getScaleZ() * BOX_TO_WORLD);
        debugScaled.translate(-28, -24, 0);
        debugMatrix = new Matrix4(debugScaled);
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
        world.step(Gdx.graphics.getDeltaTime(), 6, 2);
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(background, 0, 0);
		for (Entity ent : ents) {
            ent.update();
			ent.sprite.draw(batch);
		}
		batch.end();
        if (showDebug) {
            debugRenderer.render(world, debugMatrix);
        }
	}

	public void handleInput() {
        boolean leftArrow = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightArrow = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean upArrow = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downArrow = Gdx.input.isKeyPressed(Input.Keys.DOWN);

        float delta = Gdx.graphics.getDeltaTime();
        float playerMove = PLAYER_SPEED * delta;
        Vector2 playerPos = player.body.getPosition();
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

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        debugCoolDown = debugCoolDown - Gdx.graphics.getDeltaTime();
        if (debugCoolDown < 0 && Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            debugCoolDown = 0.2f;
            showDebug = !showDebug;
        }
    }
}
