package com.jnisitha.game.Sprites;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.jnisitha.game.MarioBros;
import com.jnisitha.game.Screens.PlayScreen;
import com.jnisitha.game.Sprites.Enemies.Enemy;
import com.jnisitha.game.Sprites.Enemies.Turtle;

import sun.net.www.protocol.mailto.MailToURLConnection;

/**
 * Created by jnisi on 2016-10-30.
 */

public class Mario extends Sprite {
    public enum State{ FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD};//enumeration
    public State currentState;
    public State previousState;

    private float stateTimer;

    private World world;
    public Body b2body;

    private TextureRegion bigMarioStand;
    private TextureRegion marioStand;
    public TextureRegion marioJump;
    private TextureRegion bigMarioJump;// theres only one texture for this. so theres no need to animate.
    private TextureRegion marioDead;
    private Animation bigMarioRun;
    private Animation growMario;
    public Animation marioRun;

    private boolean runningRight;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;
    private boolean marioIsBig;
    private boolean runGrowAnimation;
    private boolean marioisDead;


    public Mario(PlayScreen screen){
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;
        timeToDefineBigMario = false;

        Array<TextureRegion> frames = new Array<TextureRegion>();

        //creating the run animation.
        for (int i = 1; i<4;i++){
            frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i*16, 0, 16, 16));
        }
        marioRun = new Animation(0.1f, frames);
        frames.clear();//no longer need this. setup jump animation with this.

        //for big mario
        for (int i = 1; i<4;i++){
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), i*16, 0, 16, 32));
        }
        bigMarioRun = new Animation(0.1f, frames);
        frames.clear();

        //Mario Grow Animation
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        growMario = new Animation(0.2f, frames);


        //creating the jump animation This is just one frame so its just changing the texture really no animation
        marioJump = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 80,0,16,16);
        bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 80,0,16,32);

        //create standing textures
        marioStand = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 0, 0, 16, 16);//the standing little mario texture starts from 0,0 and ends at 16,16.
        bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0,0,16,32);

        //mario is DEAD texure
        marioDead = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 96,0,16,16);

        setBounds(0,0, 16/MarioBros.PPM, 16/MarioBros.PPM);
        defineMario();
        setRegion(marioStand);
    }

    public void defineMario(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(32/ MarioBros.PPM,32/MarioBros.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6/ MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;//Every fixture in Box2d has a filter. a category filter and a mask filter. category is what the fixture is (Brick, coin...).
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;//mask bit represents what this fixture can collide with. since the same fdef is used for the 'body' and the 'head' setting it only once works.

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        //create sensor for head
        EdgeShape head = new EdgeShape();//line between two points really. hence two vector2 s are given below.
        head.set(new Vector2(-2/MarioBros.PPM, 6/MarioBros.PPM), new Vector2(2/MarioBros.PPM, 6/MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;//this means it doesnt collide with anything in the world. it is just there for us to query.

        b2body.createFixture(fdef).setUserData(this);//this will uniquely identify this fixture as "head"
    }

    public void defineBigMario(){
        Vector2 currentPosition = b2body.getPosition();
        world.destroyBody(b2body);//this destroys the old body of mario.

        BodyDef bdef = new BodyDef();
        bdef.position.set(currentPosition.add(0, 10/MarioBros.PPM));//to account for marios height change.
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6/ MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        //for the lower part of the body the existing circle is moved and resized as necessary.
        shape.setPosition(new Vector2(0, -14/MarioBros.PPM));
        b2body.createFixture(fdef).setUserData(this);

        //create sensor for head
        EdgeShape head = new EdgeShape();//line between two points really. hence two vector2 s are given below.
        head.set(new Vector2(-2/MarioBros.PPM, 6/MarioBros.PPM), new Vector2(2/MarioBros.PPM, 6/MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;//this means it doesnt collide with anything in the world. it is just there for us to query.

        b2body.createFixture(fdef).setUserData(this);//this will uniquely identify this fixture as "head"

        timeToDefineBigMario = false;
    }

    //YOU CANNOT ADD OR REMOVE BODIES DURING THE PHYSICS STEPS IN BOX2D. So flags are setup and stuff is taken care of during the update cycle.
    public void update(float deltaTime){
        if (marioIsBig) {
            setPosition(b2body.getPosition().x - getWidth()/2, b2body.getPosition().y - getHeight()/2 - 6/MarioBros.PPM); //connecting the body to the sprite. why is this in the update section?
        }else {
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2); //connecting the body to the sprite. why is this in the update section?
        }
        if (timeToDefineBigMario){
            defineBigMario();
        }

        if (timeToRedefineMario){
            redifineMario();
        }
        setRegion(getFrame(deltaTime));

    }

    public void redifineMario(){
        Vector2 currentPosition = b2body.getPosition();
        world.destroyBody(b2body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(currentPosition);
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6/ MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;//Every fixture in Box2d has a filter. a category filter and a mask filter. category is what the fixture is (Brick, coin...).
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;//mask bit represents what this fixture can collide with. since the same fdef is used for the 'body' and the 'head' setting it only once works.

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        //create sensor for head
        EdgeShape head = new EdgeShape();//line between two points really. hence two vector2 s are given below.
        head.set(new Vector2(-2/MarioBros.PPM, 6/MarioBros.PPM), new Vector2(2/MarioBros.PPM, 6/MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;//this means it doesnt collide with anything in the world. it is just there for us to query.

        b2body.createFixture(fdef).setUserData(this);//this will uniquely identify this fixture as "head"

        timeToRedefineMario = false;

    }

    //This method will return the appropriate frame we need to display as the sprites texture region.
    public TextureRegion getFrame(float deltaTime){
        currentState = getState();

        TextureRegion region;
        switch(currentState){
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = growMario.getKeyFrame(stateTimer);
                if(growMario.isAnimationFinished(stateTimer)){
                    runGrowAnimation = false;
                }
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump : marioJump;
                break;
            case RUNNING:
                region = marioIsBig ? bigMarioRun.getKeyFrame(stateTimer,true):marioRun.getKeyFrame(stateTimer, true); //stateTimer decides what frame gets pulled from the animation.
                break;
            case FALLING:
            case STANDING:
            default:
                region = marioIsBig ? bigMarioStand : marioStand;
                break;
        }

        if(!marioisDead) {
            if ((b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
                region.flip(true, false);
                runningRight = false;
            } else if ((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
                region.flip(true, false);
                runningRight = true;
            }
        }


        stateTimer = currentState == previousState ? stateTimer + deltaTime: 0;
        previousState = currentState;
        return region;


    }

    public void grow(){
        runGrowAnimation = true;
        marioIsBig = true;
        timeToDefineBigMario = true;
        setBounds(getX(), getY(), getWidth(), getHeight() * 2);//setting the bounds for big mario
        MarioBros.manager.get("audio/sounds/powerup.wav", Sound.class).play();
    }

    public State getState(){

        if(marioisDead){
            return State.DEAD;
        }else if (runGrowAnimation){
             return State.GROWING;
        }else if(b2body.getLinearVelocity().y>0 || (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING)){
            return State.JUMPING;
        } else if(b2body.getLinearVelocity().y<0){
            return State.FALLING;
        }else if(b2body.getLinearVelocity().x != 0){
            return State.RUNNING;
        }else {
            return State.STANDING;
        }
    }

    public boolean isBig(){
        return marioIsBig;
    }

    public boolean isDead(){
        return marioisDead;
    }

    public float getStateTimer(){
        return stateTimer;
    }

    public void touchedEnemy(Enemy enemy){
        if( enemy instanceof Turtle && ((Turtle) enemy).getCurrentState() == Turtle.State.STANDING_SHELL){
            ((Turtle) enemy).kick(this.getX() <= enemy.getX() ? Turtle.KICK_RIGHT_SPEED : Turtle.KICK_LEFT_SPEED);
        }else {
            if (marioIsBig) {
                marioIsBig = false;
                timeToRedefineMario = true;
                setBounds(getX(), getY(), getWidth(), getHeight() / 2);
                MarioBros.manager.get("audio/sounds/powerdown.wav", Sound.class).play();
            } else {
                MarioBros.manager.get("audio/music/mario_music.ogg", Music.class).stop();
                MarioBros.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
                marioisDead = true;
                Filter filter = new Filter();
                filter.maskBits = MarioBros.NOTHING_BIT;

                //sets all the filters of mario to NOthING_BIT so that he wont collide with anything.
                for (Fixture fixture : b2body.getFixtureList()) {
                    fixture.setFilterData(filter);
                }
                b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
            }
        }
    }
}
