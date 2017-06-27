package com.codehusky.huskyui.components.inventory.elements;

import com.codehusky.huskyui.components.Action;
import com.codehusky.huskyui.components.inventory.Element;

/**
 * Created by lokio on 6/25/2017.
 */
public class ActionElement extends Element {
    private Action action;
    public void runAction(String currentPage){
        action.runAction(currentPage);
    }
    public void setAction(Action action){
        this.action = action;
    }
    public Action getAction() {
        return action;
    }
}
