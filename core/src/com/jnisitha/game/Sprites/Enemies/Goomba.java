package com.jnisitha.game.Sprites.Enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.jnisitha.game.MarioBros;
import com.jnisitha.game.Screens.PlayScreen;
import com.jnisitha.game.Sprites.Mario;

/**
 * Created by jnisi on 2016-11-02.
 */

public class Goomba extends com.jnisitha.game.Sprites.Enemies.Enemy {

    private float stateTime;
    private Animation walkAnimation;
    private Array<TextureRegion> frames;
    private boolean setToDestroy;
    private boolean destroyed;

    public Goomba(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<TextureRegion>();
        for (int i= 0; i<2; i++){
            frames.add( new TextureRegion(screen.getAtlas().findRegion("goomba"), i*16,0,16,16));
        }

        walkAnimation = new Animation(0.4f , frames);
        stateTime = 0;
        setToDestroy = false;
        destroyed = false;
        setBounds(getX(), getY(), 16/MarioBros.PPM, 16/MarioBros.PPM);//sets how big the enemy sprite is
    }

    public void update(float deltaTime){
        stateTime += deltaTime;
        if(setToDestroy && !destroyed){//here destroyed means texture removal from the game world.
            world.destroyBody(b2body);
            destroyed = true;
            setRegion(new TextureRegion(atlas.findRegion("goomba"), 32, 0, 16, 16));
            stateTime = 0;
        }else if (!destroyed){
            b2body.setLinearVelocity(velocity);
            setPosition(b2body.getPosition().x - getWidth()/2, b2body.getPosition().y - getHeight()/2);
            setRegion(walkAnimation.getKeyFrame(stateTime, true));//true loops the animation.
        }

    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(),getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6/MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.ENEMY_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.MARIO_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        //Create the head of the goomba for collision
        PolygonShape head = new PolygonShape();
        Vector2[] vertex = new Vector2[4];//create new vector 2 java array.
        vertex[0] = new Vector2(-5, 8).scl(1/MarioBros.PPM);
        vertex[1] = new Vector2(5, 8).scl(1/MarioBros.PPM);//scl scales the thing by the PPM
        vertex[2] = new Vector2(-3, 3).scl(1/MarioBros.PPM);
        vertex[3] = new Vector2(3, 3).scl(1/MarioBros.PPM);
        head.set(vertex);

        fdef.shape = head;
        fdef.restitution = 0.5f; //half the bouncyness
        fdef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        fdef.filter.maskBits = MarioBros.MARIO_BIT;

        b2body.createFixture(fdef).setUserData(this);//setUserData is done so that the collision class can access this shit.
    }

    public void draw(Batch batch){
        if(!destroyed || stateTime < 1){
            super.draw(batch);
        }
    }

    //Here one of the things we have to do is remove the box2d body so that the goomba no longer collides with things. However this is not doable here
    //since this method is getting called inside our contact listener which is inturn getting called through the world.step function in playscreen update. You cannot delete bodies mid simulation since
    //the second body would be left hanging.
    @Override
    public void hitOnHead(Mario mario) {
        setToDestroy = true;
        MarioBros.manager.get("audio/sounds/stomp.wav", Sound.class).play();
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Turtle && ((Turtle) enemy).currentState == Turtle.State.MOVING_SHELL){
            setToDestroy = true;
        }else {
            reverseVelocity(true, false);
        }
    }
}
