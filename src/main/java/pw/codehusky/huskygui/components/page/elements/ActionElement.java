package pw.codehusky.huskygui.components.page.elements;

import pw.codehusky.huskygui.components.Action;
import pw.codehusky.huskygui.components.page.Element;

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
