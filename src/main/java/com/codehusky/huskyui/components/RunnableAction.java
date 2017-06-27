package com.codehusky.huskyui.components;

import org.spongepowered.api.entity.living.player.Player;
import com.codehusky.huskyui.HuskyUI;

/**
 * Created by lokio on 6/26/2017.
 */
public class RunnableAction extends Action {

    private UIRunnable runnable;

    public RunnableAction(HuskyUI gui, Player observer, boolean isCloseAction, boolean isBackAction, String goalState) {
        super(gui, observer, isCloseAction, isBackAction, goalState);
    }

    public void setRunnable(UIRunnable runnable){
        this.runnable = runnable;
    }
    @Override
    public void runAction(String currentState){
        runnable.run(this);
    }
}
