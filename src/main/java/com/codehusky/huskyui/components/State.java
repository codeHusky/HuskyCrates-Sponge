package com.codehusky.huskyui.components;

import com.codehusky.huskyui.HuskyUI;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Created by lokio on 6/25/2017.
 */
public class State {
    public String id;
    public boolean hasParent = false;
    public String parentState;
    public HuskyUI ui = null;
    public Player observer = null;
    public State(){
        this.id="null";
    }
    public State(String id){this.id = id;}
    public void setParent(String parent){
        parentState = parent;
        hasParent = true;
    }
}
