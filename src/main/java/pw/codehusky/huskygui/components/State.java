package pw.codehusky.huskygui.components;

/**
 * Created by lokio on 6/25/2017.
 */
public class State {
    public String id;
    public boolean hasParent = false;
    public String parentState;
    public State(){
        this.id="null";
    }
    public State(String id){this.id = id;}
    public void setParent(String parent){
        parentState = parent;
        hasParent = true;
    }
}
