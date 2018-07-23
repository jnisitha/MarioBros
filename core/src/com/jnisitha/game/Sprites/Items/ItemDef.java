package com.jnisitha.game.Sprites.Items;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by jnisi on 2016-11-02.
 */

public class ItemDef {
    public Vector2 position;
    public Class<?> type; //we dont know the type of this class.

    public ItemDef(Vector2 position, Class<?> type){
        this.position = position;
        this.type = type;
    }
}
