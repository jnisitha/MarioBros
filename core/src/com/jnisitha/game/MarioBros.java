package com.jnisitha.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.jnisitha.game.Screens.PlayScreen;

public class MarioBros extends Game {
	public SpriteBatch batch;
    public static final int V_WIDTH = 400;//virtual width and height.
    public static final int V_HEIGHT = 208;

	public static final float PPM = 100; //this is pixels per meter inorder to scale everything.

	public static final short NOTHING_BIT = 0;
	public static final short GROUND_BIT = 1;//every fixture created has this bit.
	public static final short MARIO_BIT = 2;//it is easier to use OR on powers of two
	public static final short BRICK_BIT = 4;
	public static final short COIN_BIT = 8;//static means it belongs to the class not the instance of the class. final means unchangeable. However you can always initialize a final in the constructor.
	public static final short DESTROYED_BIT = 16;
	public static final short OBJECT_BIT = 32;
	public static final short ENEMY_BIT = 64;
	public static final short ENEMY_HEAD_BIT = 128;
	public static final short ITEM_BIT = 256;
	public static final short MARIO_HEAD_BIT = 512;


	//Assetmanager
	public static AssetManager manager;
	@Override
	public void create () {
		batch = new SpriteBatch();
		manager = new AssetManager();
		manager.load("audio/music/mario_music.ogg", Music.class);
		manager.load("audio/sounds/bump.wav", Sound.class);
		manager.load("audio/sounds/coin.wav", Sound.class);
		manager.load("audio/sounds/breakblock.wav", Sound.class);
		manager.load("audio/sounds/powerup_spawn.wav", Sound.class);
		manager.load("audio/sounds/powerup.wav", Sound.class);
		manager.load("audio/sounds/stomp.wav", Sound.class);
		manager.load("audio/sounds/mariodie.wav", Sound.class);
		manager.load("audio/sounds/powerdown.wav", Sound.class);

		manager.finishLoading();//doing synchronous loading here.

		setScreen(new PlayScreen(this));
	}

	@Override
	public void render () {
		super.render();//delegates the render method to the screen active at the time.
		//manager.update(); for asynchronous loading
	}
	
	@Override
	public void dispose () {
		super.dispose();
		manager.dispose();
		batch.dispose();
	}
}

//using assetManager in a static way can cause issues, especially on android. instead pass around assetmanager to those classes that need it.
