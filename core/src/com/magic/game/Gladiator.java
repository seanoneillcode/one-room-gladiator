package com.magic.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;

import java.util.*;

public class Gladiator extends ApplicationAdapter {

    public static final float ENTITY_DAMPING = 60.0f;
    public static final float ENTITY_RADIUS = 6;
    public static final float ENTITY_DENSITY = 1f;
    public static final float PLAYER_SPEED = 30.0f;
    public static final float BOX_TO_WORLD = 10f;
    public static final float WORLD_TO_BOX = 0.1f;
    public static final float MAX_ENTITY_SPEED = 8.0f;
    public static final float ATTACK_FORCE = 40f;
    public static final float PICKUP_RADIUS = 3f;
    public static final float DARK_SCREEN_TIMER = 2.0f;
    float buttonCooldown = 0.2f;

    final float VIRTUAL_HEIGHT = 180f;
    OrthographicCamera cam;
    Box2DDebugRenderer debugRenderer;
	SpriteBatch batch;
    World world;
    float screenWidth, screenHeight;

    boolean showDebug = false;
    float debugCoolDown;

	PlayerEntityImpl player;
	Texture background, hitboxImage, backgroundNight, message, finishMessage, sleepMessage, talkMessage;
    Sprite darkScreen;

	List<Entity> ents;
    List<Ai> ais;
    List<Entity> pickups;

    Rectangle hitbox;
    Vector2 hitBoxOffset = new Vector2();
    Vector2 hitBoxSize = new Vector2(20, 20);
    private float elapsedTime = 0;
    MetaGame metaGame;
    Body shopWall, talkWall, bunkWall1, bunkWall2, bunkWall3;

    Sound fastDanceMusic, loseSound, sliceSound, darkSlowMusic;
    Vector2 shopPos, sleepPos, talkPos;

    boolean isVictory;
    float buttonTimer;
    float darkScreenTimer = 0f;
    float darkScreenOpacity = 0f;
    boolean fadeDirectionOut = true;
    MetaGame.GameState nextState;
    Map<String, Float> playerParams;
    List<Ai> hitSquad;
    int hitSquadSize = 0;

	@Override
	public void create () {
        world = new World(new Vector2(), false);
        world.setContactListener(new CollisionListener());
        cam = new OrthographicCamera();
        debugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();
        metaGame = new MetaGame();
        hitSquad = new ArrayList<Ai>();

		background = new Texture("background.png");
        backgroundNight = new Texture("background-night.png");
        darkScreen = new Sprite(new Texture("dark-screen.png"));
        hitboxImage = new Texture("hitbox.png");
        finishMessage = new Texture("finish-message.png");
        sleepMessage = new Texture("sleep-message.png");
        talkMessage = new Texture("talk-message.png");

        buildWall(new Vector2(12,146), new Vector2(20, 260)); // left
        buildWall(new Vector2(666,146), new Vector2(20, 260)); // right
        buildWall(new Vector2(337,10), new Vector2(630, 20)); // bottom
        buildWall(new Vector2(337,278), new Vector2(630, 20)); // top
        shopWall = buildWall(new Vector2(555, 250), new Vector2(150, 30));
        talkWall = buildWall(new Vector2(84, 100), new Vector2(28, 40));
        bunkWall1 = buildWall(new Vector2(374, 260), new Vector2(78, 50));
        bunkWall2 = buildWall(new Vector2(280, 284), new Vector2(78, 50));
        bunkWall3 = buildWall(new Vector2(194, 250), new Vector2(78, 50));
        shopPos = new Vector2(500, 220);
        sleepPos = new Vector2(280, 240);
        talkPos = new Vector2(90, 80);

        hitBoxOffset = new Vector2();
        hitBoxSize = new Vector2(20, 20);

        sliceSound = Gdx.audio.newSound(Gdx.files.internal("slice-sound-sml.ogg"));
        loseSound = Gdx.audio.newSound(Gdx.files.internal("lose-sound-sml.ogg"));
        fastDanceMusic = Gdx.audio.newSound(Gdx.files.internal("dance-dance.ogg"));
        darkSlowMusic = Gdx.audio.newSound(Gdx.files.internal("synth-runner.ogg"));
        //fastDanceMusic.loop(0.2f);
        playerParams = new HashMap<String, Float>();
        playerParams.put("maxSpeed", 5.0f);
        playerParams.put("maxHealth", 8.0f);
        playerParams.put("damage", 1.0f);
        playerParams.put("souls", 0f);
        playerParams.put("team", 0f);

        resetGame();
	}

    public void resetGame() {
        cleanGameArea();
        hitSquadSize = 2;
        int[] wave = metaGame.getWave();
        addWave(wave);
    }

    public void cleanGameArea() {
        if (ents != null) {
            for (Entity ent : ents) {
                ent.dispose();
                if (ent.getBody() != null) {
                    world.destroyBody(ent.getBody());
                    ent.destroyBody();

                }
            }
        }
        if (ais != null) {
            for (Ai ai : ais) {
                ai.dispose();
            }
        }
        ents = new ArrayList<Entity>();
        ais = new ArrayList<Ai>();
        pickups = new ArrayList<Entity>();
        player = PlayerEntity(new Vector2(300, 120), (player == null ? playerParams : player.params), javafx.scene.paint.Color.color(1.0, 0.0, 0.0));
        player.getSprite().setColor(1.0f, 0.1f, 0.1f, 1.0f);
        ents.add(player);
        SoundPlayer.setPlayer(player);
        elapsedTime = 0;
        isVictory = false;
    }

    private void addWave(int[] teams) {
        if (teams.length == 1) {
            for (int i = 1; i < teams[0]; i++) {
                addEnemy(i, MathUtils.random(360f));
            }
        } else {
            float startColor = 0;
            float gap = 360.0f / teams.length;
            for (int i = 0; i < teams.length; i++) {
                for (int j = 0; j < teams[i]; j++) {
                    addEnemy(i, startColor);
                }
                startColor = startColor + MathUtils.random(gap / 2.0f, gap);
            }
        }
    }

    private void addEnemy(int team, float hue) {
        Map<String, Float> params = new HashMap<String, Float>();
        params.put("maxSpeed", 4.0f);
        params.put("maxHealth", 6.0f);
        params.put("damage", 1.0f);
        params.put("souls", 0f);
        params.put("team", (float)team);

        javafx.scene.paint.Color color = javafx.scene.paint.Color.hsb(hue, 1.0f, 1.0f);
        PlayerEntityImpl ent = PlayerEntity(getRandomPosition(), params, color);
        ent.getSprite().setColor((float)color.getRed(), (float)color.getGreen(), (float)color.getBlue(), 1.0f);
        ents.add(ent);
        ais.add(new Ai(ent));
    }

    private Vector2 getRandomPosition() {
        float xpos = MathUtils.random(40, 580);
        float ypos = MathUtils.random(40, 240);
        return new Vector2(xpos, ypos);
    }

    public void resize (int width, int height) {
        screenWidth = VIRTUAL_HEIGHT * width / (float)height;
        screenHeight = VIRTUAL_HEIGHT;
        cam.setToOrtho(false, (VIRTUAL_HEIGHT * width / (float)height), (VIRTUAL_HEIGHT));
        batch.setProjectionMatrix(cam.combined);
    }

	public PlayerEntityImpl PlayerEntity(Vector2 pos, Map<String, Float> params, javafx.scene.paint.Color color) {
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
        return new PlayerEntityImpl(pos, body, params, color);
    }

    public Entity buildPickup(Vector2 pos) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(pos.x * WORLD_TO_BOX, pos.y * WORLD_TO_BOX);
        Body body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setLinearDamping(ENTITY_DAMPING * WORLD_TO_BOX);
        CircleShape shape = new CircleShape();
        shape.setRadius(PICKUP_RADIUS * WORLD_TO_BOX);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = ENTITY_DENSITY * WORLD_TO_BOX;
        fixtureDef.friction = 0;
        Fixture fixture = body.createFixture(fixtureDef);
        shape.dispose();
        return new PickupEntity(pos, body);
    }

    public Body buildWall(Vector2 pos, Vector2 size) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(pos.x * WORLD_TO_BOX, pos.y * WORLD_TO_BOX);
        Body body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(size.x * 0.5f * WORLD_TO_BOX, size.y * 0.5f * WORLD_TO_BOX);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 100f;
        fixtureDef.friction = 0;
        Fixture fixture = body.createFixture(fixtureDef);
        shape.dispose();
        return body;
    }

    public void resetPlayerPosition() {
        player.getBody().setTransform(new Vector2(300 * WORLD_TO_BOX, 120 * WORLD_TO_BOX), player.getBody().getAngle());
    }

	@Override
	public void render () {
        if (metaGame.gameState == MetaGame.GameState.PLAYAGAIN) {
            metaGame.playAgain();
            resetGame();
        }
        metaGame.update(this);
        buttonTimer = buttonTimer - Gdx.graphics.getDeltaTime();
        handleInput();
        if (!metaGame.isRenderingGame()) {
            world.step(Gdx.graphics.getDeltaTime(), 6, 2);
            cam.position.set(player.getPos().x, player.getPos().y, 0);
            cam.update();
            batch.setProjectionMatrix(cam.combined);
            batch.begin();
            metaGame.render(batch, player.getPos().sub(screenWidth * 0.5f, screenHeight * 0.5f));
            handleNext();
            batch.end();
            return;
        }

        if (!metaGame.isPaused()) {
            world.step(Gdx.graphics.getDeltaTime(), 6, 2);
            cam.position.set(player.getPos().x, player.getPos().y, 0);
            cam.update();
            batch.setProjectionMatrix(cam.combined);
        }

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
        if (metaGame.gameState == MetaGame.GameState.NIGHT) {
            batch.draw(backgroundNight, 0, 0);
            shopWall.setActive(true);
            talkWall.setActive(true);
            bunkWall1.setActive(true);
            bunkWall2.setActive(true);
            bunkWall3.setActive(true);
        } else {
            batch.draw(background, 0, 0);
            shopWall.setActive(false);
            talkWall.setActive(false);
            bunkWall1.setActive(false);
            bunkWall2.setActive(false);
            bunkWall3.setActive(false);
        }
        Collections.sort(ents, new Comparator<Entity>() {
            @Override
            public int compare(Entity o1, Entity o2) {
                if (o1.getState() == PlayerState.DEAD) {
                    return -1;
                }
                if (o2.getState() == PlayerState.DEAD) {
                    return 1;
                }
                return (int)(o2.getPos().y - o1.getPos().y);
            }
        });
		for (Entity ent : ents) {
			ent.getSprite().draw(batch);
            if (ent.getHealth() > 0) {
                ent.draw(batch);
            }
            if (showDebug && ent.getBody() != null) {
                Rectangle entHit = getEntityHitBox(ent);
                Color color = Color.BLUE;
                if (ent instanceof PlayerEntityImpl && ((PlayerEntityImpl)ent).damageTimer > 0) {
                    color = Color.RED;
                }
                batch.setColor(color);
                batch.draw(hitboxImage, entHit.x, entHit.y, entHit.width, entHit.height);
            }
		}
        batch.setColor(Color.WHITE);
        if (!metaGame.isPaused()) {
            if (player.getHealth() < 1 && metaGame.gameState == MetaGame.GameState.GAMEPLAY && nextState == null) {
                loseSound.play();
                darkScreenTimer = DARK_SCREEN_TIMER;
                fadeDirectionOut = true;
                nextState = MetaGame.GameState.LOSE;
            }
            handleUpdate();
            if (showDebug && player.isAttackHurting()) {
                batch.draw(hitboxImage, hitbox.getX(), hitbox.getY(), hitbox.getWidth(), hitbox.height);
            }

            elapsedTime += Gdx.graphics.getDeltaTime();
        }
        if (metaGame.gameState == MetaGame.GameState.VICTORY) {
            batch.draw(finishMessage, player.getPos().x - 50, player.getPos().y  + 70);
        }
        if (metaGame.gameState == MetaGame.GameState.NIGHT) {
            if (player.getPos().dst2(shopPos) < 400) {
                batch.draw(talkMessage, player.getPos().x - 50, player.getPos().y  + 70);
            }
            if (player.getPos().dst2(sleepPos) < 1600) {
                batch.draw(sleepMessage, player.getPos().x - 50, player.getPos().y  + 70);
            }
            if (player.getPos().dst2(talkPos) < 900) {
                batch.draw(talkMessage, player.getPos().x - 50, player.getPos().y  + 70);
            }
        }
        handleNext();
		batch.end();

        if (metaGame.gameState == MetaGame.GameState.COUNTDOWN || metaGame.isSelectScreen()) {
            for (Entity ent : ents) {
                ent.update(elapsedTime);
            }
            cam.position.set(player.getPos().x, player.getPos().y, 0);
            cam.update();
            batch.setProjectionMatrix(cam.combined);
            batch.begin();
            metaGame.render(batch, player.getPos().sub(screenWidth * 0.5f, screenHeight * 0.5f));
            batch.end();
        }

        if (showDebug) {
            debugRenderer.render(world, cam.combined.cpy().scl(BOX_TO_WORLD));
        }
	}

	@Override
    public void dispose() {
        background.dispose();
        backgroundNight.dispose();
        hitboxImage.dispose();
        finishMessage.dispose();
        sleepMessage.dispose();
        talkMessage.dispose();

        sliceSound.dispose();
        loseSound.dispose();
        fastDanceMusic.dispose();
        darkSlowMusic.dispose();

        metaGame.dispose();
    }

	private void handleNext() {
        if (nextState != null) {
            darkScreenTimer = darkScreenTimer - Gdx.graphics.getDeltaTime();
            if (!fadeDirectionOut) {
                darkScreenOpacity = darkScreenTimer / DARK_SCREEN_TIMER;
            } else {
                darkScreenOpacity = MathUtils.clamp(1.0f - (darkScreenTimer / DARK_SCREEN_TIMER), 0, 1.0f);
            }
            cam.position.set(player.getPos().x, player.getPos().y, 0);
            cam.update();
            batch.setProjectionMatrix(cam.combined);
            Vector2 pos = player.getPos().cpy().sub(screenWidth * 0.5f, screenHeight * 0.5f);
            darkScreen.setPosition(pos.x, pos.y);
            darkScreen.draw(batch, darkScreenOpacity);
            if (darkScreenTimer < 0) {
                resetPlayerPosition();
                if (nextState == MetaGame.GameState.NIGHT) {
                    cleanGameArea();
                }
                if (nextState == MetaGame.GameState.EXIT) {
                    Gdx.app.exit();
                }
                metaGame.setState(nextState);
                nextState = null;
            }
        }
    }

    public void drawRect(Rectangle rect) {
        if (showDebug) {
            batch.draw(hitboxImage, rect.x, rect.y, rect.width, rect.height);
        }
    }

	public static Rectangle getEntityHitBox(Entity ent) {
        Vector2 offset = new Vector2(ENTITY_RADIUS, ENTITY_RADIUS);
        return new Rectangle(ent.getPos().x - offset.x, ent.getPos().y - offset.y, ENTITY_RADIUS * 2, ENTITY_RADIUS * 2);
    }

	public void handleUpdate() {

        // player hit
        hitbox = new Rectangle(
                player.getPos().x + hitBoxOffset.x,
                player.getPos().y + hitBoxOffset.y,
                hitBoxSize.x, hitBoxSize.y);
        if (player.isAttackHurting()) {
            for (Entity ent : ents) {
                if (ent != player && ent.getHealth() > 0) {
                    if (Intersector.overlaps(getEntityHitBox(ent), hitbox)) {
                        if (ent.takeDamage(player.params.get("damage").intValue()) ) {
                            Vector2 dir = ent.getPos().cpy().sub(player.getPos()).nor().scl(ATTACK_FORCE);
                            ent.getBody().applyForceToCenter(dir, true);
                            break;
                        }
                    }
                }
            }
        }

        boolean aliveAi = false;
        for (Ai ai : ais) {
            ai.update(this, elapsedTime, ents);
            if (ai.playerEntity.getState() != PlayerState.DEAD && ai.playerEntity.getTeam() != player.getTeam()) {
                aliveAi = true;
                if (hitSquad.size() < hitSquadSize) {
                    hitSquad.add(ai);
                }
            }
        }
        Iterator<Ai> ais = hitSquad.iterator();
        while (ais.hasNext()) {
            Ai ai = ais.next();
            ai.target = player;
        }
        if (!aliveAi && (metaGame.gameState == MetaGame.GameState.GAMEPLAY)) {
            metaGame.setState(MetaGame.GameState.VICTORY);
        }
        if (metaGame.gameState != MetaGame.GameState.VICTORY) {
            player.setIsVictory(false);
        } else {
            player.setIsVictory(true);
        }
        player.update(elapsedTime);

        for (Entity ent : pickups) {
            ent.update(elapsedTime);
        }

        List<Entity> newEnts = new ArrayList<Entity>();
        // remove dead ents
        Iterator<Entity> entIter = ents.iterator();
        while(entIter.hasNext()) {
            Entity ent = entIter.next();
            if (ent.getState() == PlayerState.DEAD && ent.getBody() != null && ent != player) {
                if (!(ent instanceof PickupEntity)) {
                    Entity newEnt = buildPickup(ent.getPos());
                    newEnts.add(newEnt);
                    pickups.add(newEnt);
                    world.destroyBody(ent.getBody());
                    ent.destroyBody();
                } else {
                    world.destroyBody(ent.getBody());
                    ent.destroyBody();
                    entIter.remove();
                }

            }
        }
        ents.addAll(newEnts);
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

        boolean leftArrow = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightArrow = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean upArrow = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downArrow = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean attackButton = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean useButton = Gdx.input.isKeyPressed(Input.Keys.E);

        float delta = Gdx.graphics.getDeltaTime();
        float playerMove = PLAYER_SPEED * delta;
        if (player.getState() == PlayerState.STUNNED) {
            playerMove = 0;
            upArrow = false;
            downArrow = false;
            leftArrow = false;
            rightArrow = false;
        }
        Vector2 playerPos = player.getPos();

        if (metaGame.gameState == MetaGame.GameState.GAMEPLAY || metaGame.gameState == MetaGame.GameState.NIGHT
                || metaGame.gameState == MetaGame.GameState.VICTORY) {
            if (player.getState() == PlayerState.IDLE || player.getState() == PlayerState.MOVING) {
                if (leftArrow) {
                    player.getBody().applyLinearImpulse(-playerMove, 0, playerPos.x,playerPos.y, true);
                    player.setIsRight(false);
                    player.setState(PlayerState.MOVING);
                }
                if (rightArrow) {
                    player.getBody().applyLinearImpulse(playerMove, 0, playerPos.x,playerPos.y, true);
                    player.setIsRight(true);
                    player.setState(PlayerState.MOVING);
                }
                if (upArrow) {
                    player.getBody().applyLinearImpulse(0, playerMove, playerPos.x,playerPos.y, true);
                    player.setState(PlayerState.MOVING);
                }
                if (downArrow) {
                    player.getBody().applyLinearImpulse(0, -playerMove, playerPos.x,playerPos.y, true);
                    player.setState(PlayerState.MOVING);
                }
            }

            if (!downArrow && !upArrow && !leftArrow && !rightArrow && (player.getState() != PlayerState.STUNNED)) {
                player.getBody().setLinearVelocity(0, 0);
                if (player.getState() == PlayerState.MOVING) {
                    player.setState(PlayerState.IDLE);
                }
            }
            if (attackButton) {
                boolean isAttackingAllowed = metaGame.isRenderingGame() && metaGame.gameState != MetaGame.GameState.NIGHT;
                if (player.attackCooldown < 0 && isAttackingAllowed) {
                    sliceSound.play(SoundPlayer.getSfxVolume(player.getPos()), MathUtils.random(0.5f, 2.0f), 0 );
                    player.setState(PlayerState.ATTACKING);
                    hitBoxOffset = new Vector2(-ENTITY_RADIUS, -ENTITY_RADIUS);
                    hitBoxSize = new Vector2(10, 10);
                    if (player.isRight) {
                        hitBoxOffset.x = 1.4f * ENTITY_RADIUS;
                    } else {
                        hitBoxOffset.x = -3.4f * ENTITY_RADIUS;
                    }
                }
            }
        }

        if (buttonTimer < 0 && useButton && metaGame.gameState == MetaGame.GameState.NIGHT && nextState == null) {
            if (playerPos.dst2(shopPos) < 400) {
                buttonTimer = buttonCooldown;
                metaGame.setState(MetaGame.GameState.SHOP);
            }
            if (playerPos.dst2(sleepPos) < 1600) {
                buttonTimer = buttonCooldown;
                darkScreenTimer = DARK_SCREEN_TIMER;
                fadeDirectionOut = true;
                metaGame.resetDay();
                nextState = MetaGame.GameState.PROGRESS;
                darkSlowMusic.stop();
            }
            if (playerPos.dst2(talkPos) < 900) {
                buttonTimer = buttonCooldown;
                metaGame.pickRandomAdvice();
                metaGame.setState(MetaGame.GameState.ADVICE);
            }
        }
        if (buttonTimer < 0 && useButton && nextState == null) {
            buttonTimer = buttonCooldown;
            metaGame.updateGamestate(this);
        }
        if (buttonTimer < 0 && (upArrow || downArrow) && nextState == null) {
            buttonTimer = buttonCooldown;
            metaGame.moveCursor(upArrow);
        }
    }
}
