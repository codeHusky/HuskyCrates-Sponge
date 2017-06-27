package com.codehusky.huskyui;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskyui.components.inventory.Page;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import com.codehusky.huskyui.components.State;

import java.util.HashMap;

/**
 * HuskyUI is a system to make "GUIs" easy to make and interact with for developers. Specifically me.
 * This class actually doesn't do anything other than hold data for GUIs.
 */
public class HuskyUI {
    private HashMap<String,State> states = new HashMap<>();
    private String initalState;
    public HuskyUI(){

    }
    public void addDefaultState(State state){
        initalState = state.id;
        this.addState(state);
    }
    public void addState(State state){
        state.ui = this;
        states.put(state.id,state);
    }

    public State getState(String id){
        return states.get(id);
    }

    public void openState(Player player, String state){
        State ourState = states.get(state);
        if(ourState == null){
            player.sendMessage(Text.of(TextColors.RED, "A state that does not exist was attempted to be opened"));
            player.sendMessage(Text.of(TextColors.RED, "ID: " + state));
            return;
        }
        ourState.observer = player;
        if(ourState instanceof Page) {
            Page ourPage = (Page) ourState;
            player.openInventory(ourPage.generatePageView(), HuskyCrates.instance.genericCause);
        }else{
            player.closeInventory(HuskyCrates.instance.genericCause);
            player.sendMessage(Text.of(TextColors.RED, "An invalid or incomplete state was opened."));
            player.sendMessage(Text.of(TextColors.RED, "ID: " + state));
        }
    }
    public void launchForPlayer(Player player){
        this.openState(player,initalState);
    }
}
