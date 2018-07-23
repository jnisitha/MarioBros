package com.jnisitha.game.Sprites.Enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.async.ThreadUtils;
import com.jnisitha.game.MarioBros;
import com.jnisitha.game.Screens.PlayScreen;
import com.jnisitha.game.Sprites.Mario;

/**
 * Created by jnisi on 2016-11-03.
 */

public class Turtle extends Enemy {
    public enum State {WALKING, STANDING_SHELL, MOVING_SHELL, DEAD}

    public State currentState;
    public State previousState;

    public static final int KICK_LEFT_SPEED = -2;
    public static final int KICK_RIGHT_SPEED = 2;

    private float stateTime;
    private Animation walkAnimation;
    private Array<TextureRegion> frames;
    private TextureRegion shell;
    private float deadRotationDegrees;
    private boolean setToDestroy;
    private boolean destroyed;

    public Turtle(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<TextureRegion>();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 0, 0, 16, 24));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 16, 0, 16, 24));
        shell = new TextureRegion(screen.getAtlas().findRegion("turtle"), 64, 0, 16, 24);

        walkAnimation = new Animation(0.2f, frames);
        currentState = previousState = State.WALKING;
        deadRotationDegrees = 0;

        setBounds(getX(), getY(), 16/ MarioBros.PPM, 16/ MarioBros.PPM);

    }

    @Override
    protected void defineEnemy() {//this is the same as for the Goomba class. should be refactored to minimize copypasta.
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
        fdef.restitution = 1.5f; //half the bouncyness
        fdef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        fdef.filter.maskBits = MarioBros.MARIO_BIT;

        b2body.createFixture(fdef).setUserData(this);//setUserData is done so that the collision class can access this shit.

    }

    public TextureRegion getFrame(float deltaTime){
        TextureRegion region;
        switch (currentState){
            case MOVING_SHELL:
            case STANDING_SHELL:
                region = shell;
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTime, true);
                break;
        }

        if(velocity.x > 0 && region.isFlipX() == false){
            region.flip(true, false);
        }
        if (velocity.x < 0 && region.isFlipX() == true){
            region.flip(true, false);
        }
        stateTime = currentState == previousState ? stateTime + deltaTime: 0; //These are inline if statements basically.

        previousState = currentState;
        return region;
    }

    @Override
    public void hitOnHead(Mario mario) {

        if (currentState != State.STANDING_SHELL){
            //switch to shell
            //switch currentstate
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        } else {
            kick(mario.getX() <= this.getX() ? KICK_RIGHT_SPEED : KICK_LEFT_SPEED);
        }

    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Turtle){
            if (((Turtle) enemy).currentState == State.MOVING_SHELL && currentState != State.MOVING_SHELL){
                killed();
            }else if (currentState == State.MOVING_SHELL && ((Turtle) enemy).currentState == State.WALKING){
                return;
            }else {
                reverseVelocity(true, false);
            }
        }else if (currentState != State.MOVING_SHELL){
            reverseVelocity(true, false);
        }
    }

    public void kick(int speed){
        velocity.x = speed;
        currentState = State.MOVING_SHELL;
    }

    public State getCurrentState(){
        return currentState;
    }

    @Override
    public void reverseVelocity(boolean x, boolean y) {
        super.reverseVelocity(x, y);
    }

    @Override
    public void update(float deltaTime) {
        setRegion(getFrame(deltaTime));
        if (currentState == State.STANDING_SHELL && stateTime > 5){
            currentState = State.WALKING;
            velocity.x = 1;
        }

        setPosition(b2body.getPosition().x - getWidth()/2, b2body.getPosition().y - 8/MarioBros.PPM);

        if (currentState == State.DEAD){
            deadRotationDegrees += 3;
            rotate(deadRotationDegrees);
            if (stateTime > 5 && !destroyed){
                world.destroyBody(b2body);
                destroyed = true;
            }
        }
        b2body.setLinearVelocity(velocity);

    }

    public void killed(){
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = MarioBros.NOTHING_BIT;

        for (Fixture fixture: b2body.getFixtureList()){
            fixture.setFilterData(filter);
        }

        b2body.applyLinearImpulse(new Vector2(0, 5f), b2body.getWorldCenter(), true);
    }

    //shit method to stop a little bug
    public void draw(Batch batch){
        if(!destroyed){
            super.draw(batch);
        }
    }
}
