package com.codehusky.huskycrates.crate.virtual.views;

import com.codehusky.huskycrates.crate.virtual.Item;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.item.ItemTypes;

public class ViewConfig {
    private Item borderItem;
    public ViewConfig(ConfigurationNode node){
        if(!node.getNode("borderItem").isVirtual())
            this.borderItem = new Item(node.getNode("borderItem"));
        else
            this.borderItem = new Item("&6HuskyCrates", ItemTypes.STAINED_GLASS_PANE,null,1,15,null,null,null);
    }

    public Item getBorderItem() {
        return borderItem;
    }
}
