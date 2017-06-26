package pw.codehusky.huskygui;

import org.spongepowered.api.entity.living.player.Player;
import pw.codehusky.huskygui.components.State;

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
}
