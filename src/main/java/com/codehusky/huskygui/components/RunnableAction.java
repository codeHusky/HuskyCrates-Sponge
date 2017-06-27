package com.codehusky.huskygui.components;

import org.spongepowered.api.entity.living.player.Player;
import com.codehusky.huskygui.HuskyGUI;

/**
 * Created by lokio on 6/26/2017.
 */
public class RunnableAction extends Action {

    private GUIRunnable runnable;

    public RunnableAction(HuskyGUI gui, Player observer, boolean isCloseAction, boolean isBackAction, String goalState) {
        super(gui, observer, isCloseAction, isBackAction, goalState);
    }

    public void setRunnable(GUIRunnable runnable){
        this.runnable = runnable;
    }
    @Override
    public void runAction(String currentState){
        runnable.run(this);
    }
}
