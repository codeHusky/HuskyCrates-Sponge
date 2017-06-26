package pw.codehusky.huskygui.components;

import org.spongepowered.api.entity.living.player.Player;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskygui.HuskyGUI;

/**
 * This is something that the State handler will use in the occurance that this is called to decide
 * where to move the current GUI Instance to.
 */
public class Action {
    public HuskyGUI gui;
    public Player observer;
    public String currentState;
    public boolean isCloseAction;
    public boolean isBackAction;
    public String goalState;
    public Action(HuskyGUI gui, Player observer, String currentState, boolean isCloseAction, boolean isBackAction, String goalState){
        this.gui = gui;
        this.observer = observer;
        this.currentState = currentState;
        this.isCloseAction = isCloseAction;
        this.isBackAction=isBackAction;
        this.goalState = goalState;
    }

    public void runAction(){
        //fired when action is activated
        if(isCloseAction)
            observer.closeInventory(HuskyCrates.instance.genericCause);
        if(isBackAction) {

        }else{
            //normal state change
            gui.
        }
    }
}
