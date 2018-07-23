package com.jnisitha.game.Sprites.Enemies;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.jnisitha.game.Screens.PlayScreen;
import com.jnisitha.game.Sprites.Mario;

/**
 * Created by jnisi on 2016-11-02.
 */

public abstract class Enemy extends Sprite {

    protected World world;
    protected Screen screen;
    protected TextureAtlas atlas;
    public Body b2body;
    public Vector2 velocity;

    public Enemy(PlayScreen screen, float x, float y){
        this.world = screen.getWorld();
        this.atlas = screen.getAtlas();
        this.screen = screen;
        setPosition(x,y);
        defineEnemy();
        velocity = new Vector2(-1,-2);
        b2body.setActive(false);//sets the body to inactive until mario approaches.
    }

    protected abstract void defineEnemy();
    public abstract void hitOnHead(Mario mario);
    public abstract void onEnemyHit(Enemy enemy);

    public void reverseVelocity (boolean x, boolean y){
        if (x){ velocity.x = -velocity.x;}
        if (y){velocity.y = -velocity.y;}
    }

    public abstract void update(float deltaTime);
}
