package pw.codehusky.huskygui.components.page;

import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskygui.components.State;

import java.util.HashMap;

/**
 * An inventory view state. Traditional crate gui view and such.
 * Generally, when making guis custom, overriding the page class is a good idea.
 */
public class Page extends State {
    private HashMap<Integer,Element> elements;
    private Integer slotCount;
    private InventoryArchetype archetype;
    public Inventory generatePageView(){
        Inventory ourInventory = Inventory.builder()
                .of(archetype)
                .listener(InteractInventoryEvent.class, evt ->{
                    if(!(evt instanceof InteractInventoryEvent.Open) && !(evt instanceof  InteractInventoryEvent.Close)){
                        evt.setCancelled(true);
                        //clickable
                    }
                })
                .property(InventoryTitle.PROPERTY_NAME,InventoryTitle.of(Text.of("AA BB", TextColors.RED," CC DD")))
                .build(HuskyCrates.instance);
        int slotNum = 0;
        for(Inventory slot : ourInventory.slots()){
            if(elements.containsKey(slotNum)){
                slot.set(elements.get(slotNum).getDisplayItem());
            }
            slotNum++;
        }
        return ourInventory;
    }
}
