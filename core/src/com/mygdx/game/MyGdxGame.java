package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class MyGdxGame extends Game {
	public AssetManager assetManager;
	public Skin skin;

	@Override
	public void create() {
		assetManager = new AssetManager();
		assetManager.load("uiskin.atlas", TextureAtlas.class);
		assetManager.load("uiskin.json", Skin.class);
		assetManager.finishLoading();

		skin = assetManager.get("uiskin.json", Skin.class);

		setScreen(new MenuScreen(this));
	}
}
