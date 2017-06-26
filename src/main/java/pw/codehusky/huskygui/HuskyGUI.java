package pw.codehusky.huskygui;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.commands.Husky;
import pw.codehusky.huskygui.components.State;
import pw.codehusky.huskygui.components.page.Page;

import java.util.HashMap;

/**
 * HuskyGUI is a system to make "GUIs" easy to make and interact with for developers. Specifically me.
 * This class actually doesn't do anything other than hold data for GUIs.
 */
public class HuskyGUI {
    private HashMap<String,State> states = new HashMap<>();
    private String initalState;
    public HuskyGUI(){

    }
    public void addDefaultState(State state){
        initalState = state.id;
        this.addState(state);
    }
    public void addState(State state){
        states.put(state.id,state);
    }

    public State getState(String id){
        return states.get(id);
    }

    public void openState(Player player, String state){
        State ourState = states.get(state);
        if(ourState instanceof Page) {
            Page ourPage = (Page) ourState;
            player.openInventory(ourPage.generatePageView(), HuskyCrates.instance.genericCause);
        }else{
            player.closeInventory(HuskyCrates.instance.genericCause);
            if(ourState == null){
                player.sendMessage(Text.of(TextColors.RED, "A state that does not exist was attempted to be opened"));
                player.sendMessage(Text.of(TextColors.RED, "ID: " + state));
            }else {
                player.sendMessage(Text.of(TextColors.RED, "An invalid or incomplete state was opened."));
                player.sendMessage(Text.of(TextColors.RED, "ID: " + state));
            }
        }
    }
    public void launchForPlayer(Player player){
        this.openState(player,initalState);
    }
}
