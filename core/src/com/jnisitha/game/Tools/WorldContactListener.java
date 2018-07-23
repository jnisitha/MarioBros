package com.jnisitha.game.Tools;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.jnisitha.game.MarioBros;
import com.jnisitha.game.Sprites.Enemies.Enemy;
import com.jnisitha.game.Sprites.InteractiveTileObject;
import com.jnisitha.game.Sprites.Items.Item;
import com.jnisitha.game.Sprites.Mario;

import org.omg.CORBA.OBJECT_NOT_EXIST;

/**
 * Created by jnisi on 2016-10-31.
 */

//contact listener gets called when two fixture collide.
public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
       Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;//the collision definition using the ORing of bits.

        switch (cDef){
            //MARIO COLLISIONS ---------------------------------------
            //If mario jumps on an enemy
            case MarioBros.ENEMY_HEAD_BIT | MarioBros.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ENEMY_HEAD_BIT){
                    ((Enemy)fixA.getUserData()).hitOnHead((Mario) fixB.getUserData());
                }else {
                    ((Enemy)fixB.getUserData()).hitOnHead((Mario) fixA.getUserData());
                }
                break;
            //Mario head butts a coin or a brick
            case MarioBros.MARIO_HEAD_BIT | MarioBros.BRICK_BIT:
            case MarioBros.MARIO_HEAD_BIT | MarioBros.COIN_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.MARIO_HEAD_BIT){
                    ((InteractiveTileObject) fixB.getUserData()).onHeadHit((Mario) fixA.getUserData());
                }else{
                    ((InteractiveTileObject) fixA.getUserData()).onHeadHit((Mario) fixB.getUserData());
                }
                break;
            //Mario colliding with the items
            case MarioBros.ITEM_BIT | MarioBros.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT){
                    ((Item)fixA.getUserData()).use((Mario) fixB.getUserData());
                }else{
                    ((Item)fixB.getUserData()).use((Mario) fixA.getUserData());
                }
                break;
            //Enemies colliding with mario
            case MarioBros.ENEMY_BIT | MarioBros.MARIO_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.MARIO_BIT){
                    ((Mario)fixA.getUserData()).touchedEnemy((Enemy) fixB.getUserData());
                }else {
                    ((Mario)fixB.getUserData()).touchedEnemy((Enemy) fixA.getUserData());
                }
                break;

            //OTHER COLLISIONS -----------------------------------------------------
            //Enemies colliding with pipes.
            case MarioBros.ENEMY_BIT | MarioBros.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ENEMY_BIT){
                    ((Enemy)fixA.getUserData()).reverseVelocity(true, false);
                }else {
                    ((Enemy)fixB.getUserData()).reverseVelocity(true, false);
                }
                break;

            //Enemies colliding with Enemies
            case MarioBros.ENEMY_BIT | MarioBros.ENEMY_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ENEMY_BIT) {
                    ((Enemy) fixA.getUserData()).onEnemyHit((Enemy) fixB.getUserData());
                    ((Enemy) fixB.getUserData()).onEnemyHit((Enemy) fixA.getUserData());
                }
                break;
            //Items colliding with pipes.
            case MarioBros.ITEM_BIT | MarioBros.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT){
                    ((Item)fixA.getUserData()).reverseVelocity(true, false);
                }else{
                    ((Item)fixB.getUserData()).reverseVelocity(true, false);
                }
                break;


        }

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {//once something has collided you can change the characteristics of that collision.

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) { //Things like what angles the fixtures went at. results of the collision

    }
}
