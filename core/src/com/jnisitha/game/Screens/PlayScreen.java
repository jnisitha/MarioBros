package com.jnisitha.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.jnisitha.game.MarioBros;
import com.jnisitha.game.Scenes.Hud;
import com.jnisitha.game.Sprites.Enemies.Enemy;
import com.jnisitha.game.Sprites.Items.Item;
import com.jnisitha.game.Sprites.Items.ItemDef;
import com.jnisitha.game.Sprites.Items.Mushroom;
import com.jnisitha.game.Sprites.Mario;
import com.jnisitha.game.Tools.B2WorldCreator;
import com.jnisitha.game.Tools.WorldContactListener;

import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by jnisi on 2016-10-25.
 */

public class PlayScreen implements Screen{
    //Reference to the game to set Screens
    private MarioBros game;
    private TextureAtlas atlas;

    //playscreen variables.
    private OrthographicCamera gameCamera;
    private Viewport gamePort;
    private Hud hud;

    //sprite
    private Mario player;

    //Music
    private Music music;

    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;


    //Tiled map variables
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    //Box2d variables
    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    public PlayScreen(MarioBros game){

        atlas = new TextureAtlas("Mario_and_Enemies.pack");//we can use libgdx asset manager instead. here we are not using many and its not that intensive. so this is used instead.


        this.game = game;
        gameCamera = new OrthographicCamera();
        gamePort = new FitViewport(MarioBros.V_WIDTH/MarioBros.PPM, MarioBros.V_HEIGHT/MarioBros.PPM, gameCamera);
        //creating the hud for scores/timers/level info

        hud = new Hud(game.batch);

        //load the map and set renderer
        mapLoader = new TmxMapLoader();
        map = mapLoader.load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1/MarioBros.PPM);


        //gamecamera is by default focused at 0,0. we want only the positive quadrant.
        gameCamera.position.set(gamePort.getWorldWidth()/2, gamePort.getWorldHeight()/2, 0);

        world = new World(new Vector2(0, -10), true);//first is gravity second allows objects to sleep in the world saving calculation time.
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        //create sprites
        player = new Mario(this);


        music = MarioBros.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.play();

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();

        //goombas = new Goomba(this, 5.64f, .16f);//these are kinda messed up but should work fine once the positions are imported from tiled.

        world.setContactListener(new WorldContactListener());
    }

    public void spawnItem(ItemDef idef){
        itemsToSpawn.add(idef);
    }

    public void handleSpawningItems(){
        if(!itemsToSpawn.isEmpty()){
            ItemDef idef = itemsToSpawn.poll();
            if(idef.type == Mushroom.class){
                items.add (new Mushroom(this, idef.position.x, idef.position.y));
            }
        }
    }

    public void handleInput(float deltaTime){
        if (player.currentState != Mario.State.DEAD) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {//just pressed means no holding it down.
                player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true); //first the Vector, then where the vector is applied, if the body is woken up from this force/impulse.
            }
            if ((Gdx.input.isKeyPressed(Input.Keys.RIGHT)) && player.b2body.getLinearVelocity().x <= 2) {
                player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
            }

            if ((Gdx.input.isKeyPressed(Input.Keys.LEFT)) && player.b2body.getLinearVelocity().x >= -2) {
                player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
            }
        }


    }

    public void update(float deltaTime){
        handleInput(deltaTime);
        handleSpawningItems();

        //takes  a step in the physics simulation. how fast our game is updated.
        world.step(1/60f, 6, 2);

        player.update(deltaTime);
        for (Enemy enemy: creator.getEnemies()){
            enemy.update(deltaTime);
            if (enemy.getX() < player.getX() + 250/MarioBros.PPM){
                enemy.b2body.setActive(true);
            }
        }

        for(Item item : items){
            item.update(deltaTime);
        }
        hud.update(deltaTime);

        if (player.currentState != Mario.State.DEAD){
            gameCamera.position.x = player.b2body.getPosition().x;
        }

        gameCamera.update();
        renderer.setView(gameCamera); //only render what the gameCamera can see.
    }

    public TiledMap getMap(){
        return map;
    }

    public World getWorld(){
        return world;
    }

    public TextureAtlas getAtlas(){
        return atlas;
    }

    public boolean gameOver(){
        if (player.currentState == Mario.State.DEAD && player.getStateTimer() > 3){
            return true;
        }
        return false;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);//sets color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);//clears screen

        //render the game map.
        renderer.render();

        //render our Box2DDebugLines.
        b2dr.render(world, gameCamera.combined);


        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();
        player.draw(game.batch);
        for (Enemy enemy: creator.getEnemies()){
            enemy.draw(game.batch);
        }
        for(Item item: items){
            item.draw(game.batch);
        }
        game.batch.end();

        //set batch to now draw what the Hud Camera sees.
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if(gameOver()){
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }


    @Override
    public void resize(int width, int height) {//when we change the size of the screen (on desktop) the view needs to be adjusted.
        gamePort.update(width, height);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }
}
