package com.magic.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class Gladiator extends ApplicationAdapter {
	SpriteBatch batch;
	Texture player, enemy;
	Texture background;

	List<Entity> ents;

	@Override
	public void create () {
		batch = new SpriteBatch();

		background = new Texture("background.png");

		ents = new ArrayList<Entity>();
		player = new Texture("player.png");
		ents.add(new Entity(player, new Vector2(20, 20), null));
		enemy = new Texture("enemy.png");
		ents.add(new Entity(enemy, new Vector2(400, 400), null));

	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(background, 0, 0);
		for (Entity ent : ents) {
			ent.sprite.draw(batch);
		}
		batch.end();
	}
}
