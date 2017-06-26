package pw.codehusky.huskygui;

import org.spongepowered.api.entity.living.player.Player;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskygui.components.State;
import pw.codehusky.huskygui.components.page.Page;

import java.util.HashMap;

/**
 * HuskyGUI is a system to make "GUIs" easy to make and interact with for developers. Specifically me.
 * This class actually doesn't do anything other than hold data for GUIs.
 */
public class HuskyGUI {
    private HashMap<String,State> states;
    private String initalState;
    public class Builder {
        public Builder(){

        }
    }

    public void openState(Player player, String state){
        State ourState = states.get(state);
        if(ourState instanceof Page) {
            Page ourPage = (Page) ourState;
            player.openInventory(ourPage.generatePageView(), HuskyCrates.instance.genericCause);
        }
    }
}
