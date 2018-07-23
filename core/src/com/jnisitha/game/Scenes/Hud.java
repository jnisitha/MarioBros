package com.jnisitha.game.Scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.jnisitha.game.MarioBros;

/**
 * Created by jnisi on 2016-10-25.
 */

public class Hud implements Disposable{
    public Stage stage;
    private Viewport viewport;//new camera and viewport for the hud since the hud needs to stay stationary.

    private Integer worldTimer;//Integer is an object whereas int is a primitive type in java.
    private  float timeCount;
    private static Integer score;

    private Label countdownLabel;
    private static Label scoreLabel;
    private Label timeLabel;
    private Label levelLabel;
    private Label worldLabel;
    private Label marioLabel;

    public Hud(SpriteBatch sb){
        worldTimer = 300;
        timeCount = 0;
        score = 0;

        viewport = new FitViewport(MarioBros.V_WIDTH, MarioBros.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);//a stage can be thought of as an empty box. we need to create a table which organizes the layout of the stage.

        Table table = new Table();
        table.top();
        table.setFillParent(true);//the table is now the size of our stage.

        countdownLabel = new Label(String.format("%03d", worldTimer), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        scoreLabel = new Label(String.format("%06d", score), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        timeLabel = new Label("TIME", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        levelLabel = new Label("1-1", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        worldLabel = new Label("WORLD", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        marioLabel = new Label("MARIO", new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        table.add(marioLabel).expandX().padTop(10);
        table.add(worldLabel).expandX().padTop(10);
        table.add(timeLabel).expandX().padTop(10);
        table.row();
        table.add(scoreLabel).expandX();
        table.add(levelLabel).expandX();
        table.add(countdownLabel).expandX();

        stage.addActor(table);

    }

    @Override
    public void dispose(){
        stage.dispose();
    }

    public void update(float deltaTime){
        timeCount += deltaTime;
        if(timeCount>=1){
            worldTimer --;
            countdownLabel.setText(String.format("%03d", worldTimer));
            timeCount = 0;
        }
    }

    public static void addScore(int value){//this is considered bad practice. Overall STATIC is not considered to be good OOP practice. this messes with inheritance concepts and multithreaded programming. read up.
        //this can be avoided by passing the hud.
        score += value;
        scoreLabel.setText(String.format("%06d", score));
    }

    //END NOTES
    //Static variables are bad, unless they represent "singleton" of some kind, and if they do, then in OOP it's better to create actual singleton class (especially if you can use enum singleton).
    // Among other things, static variables make multi-threaded programming difficult, and can do that even in single-threaded programming, where you "unexpectedly" need two instances of class with static fields.

    //On the other hand static methods are generally just fine, as long as they do not access any static data, but operate only on their arguments.
    // Of course, if you notice you have static void MyStaticUtils.operateOnFoo(Foo foo), then it's much better to have non-static void Foo.operate() method.
    // But sometimes you don't have the luxury of adding methods to an existing class, and must operate on instances returned by existing methods, and then static utility methods are definitely good choice.


}
