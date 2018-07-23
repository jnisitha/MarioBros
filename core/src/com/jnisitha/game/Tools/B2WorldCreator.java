package com.jnisitha.game.Tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.jnisitha.game.MarioBros;
import com.jnisitha.game.Screens.PlayScreen;
import com.jnisitha.game.Sprites.Brick;
import com.jnisitha.game.Sprites.Coin;
import com.jnisitha.game.Sprites.Enemies.Enemy;
import com.jnisitha.game.Sprites.Enemies.Goomba;
import com.jnisitha.game.Sprites.Enemies.Turtle;

/**
 * Created by jnisi on 2016-10-31.
 */

public class B2WorldCreator {

    private Array<Goomba> goombas;
    private  Array<Turtle> turtles;
    private Array<Enemy> enemies;

    public B2WorldCreator(PlayScreen screen){

        World world = screen.getWorld();
        TiledMap map = screen.getMap();

        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        //this takes care of the ground bodies/fixtures.
        for (MapObject object: map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)){//this gets the graphics layers from Tiled level1.tmx that was created. layering starts from 0.
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth()/2)/ MarioBros.PPM, (rect.getY() + rect.getHeight()/2)/MarioBros.PPM);
            //Dynamic Bodies = the player and other things that are affected by gravity and forces and what not.
            //static bodies = dont move. can move them forcefully by programming.
            //kinematic = these are not affected by forces. BuT you can move these by applying constant velocities. Pendulums and moving platforms.

            body = world.createBody(bdef);

            shape.setAsBox((rect.getWidth()/2)/MarioBros.PPM, (rect.getHeight()/2)/MarioBros.PPM);
            fdef.shape = shape;
            body.createFixture(fdef);
        }

        //create pipe bodies/fixtures
        for (MapObject object: map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)){//this gets the graphics layers from Tiled level1.tmx that was created. layering starts from 0.
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth()/2)/MarioBros.PPM, (rect.getY() + rect.getHeight()/2)/MarioBros.PPM);

            body = world.createBody(bdef);

            shape.setAsBox((rect.getWidth()/2)/MarioBros.PPM, (rect.getHeight()/2)/MarioBros.PPM);
            fdef.shape = shape;
            fdef.filter.categoryBits = MarioBros.OBJECT_BIT;
            body.createFixture(fdef);
        }

        //create brick bodies/fixtures
        for (MapObject object: map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)){//this gets the graphics layers from Tiled level1.tmx that was created. layering starts from 0.

            new Brick(screen, object);
        }

        //create coin bodies/fixtures
        for (MapObject object: map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)){//this gets the graphics layers from Tiled level1.tmx that was created. layering starts from 0.

            new Coin(screen, object);
        }

        //create the goombas
        goombas = new Array<Goomba>();
        for(MapObject object: map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)){
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            goombas.add(new Goomba(screen, rect.getX()/MarioBros.PPM, rect.getY()/MarioBros.PPM));
        }

        //create the turtles.
        turtles =  new Array<Turtle>();
        for (MapObject object: map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)){
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            turtles.add(new Turtle(screen, rect.getX()/MarioBros.PPM, rect.getY()/MarioBros.PPM));
        }
    }

    public Array<Goomba> getGoombas() {
        return goombas;
    }
    public Array<Enemy> getEnemies(){
        enemies = new Array<Enemy>();
        enemies.addAll(goombas);
        enemies.addAll(turtles);
        return enemies;
    }
}
