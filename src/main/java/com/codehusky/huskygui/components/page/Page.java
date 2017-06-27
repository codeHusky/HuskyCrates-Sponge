package com.codehusky.huskygui.components.page;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskygui.components.State;
import com.codehusky.huskygui.components.page.elements.ActionElement;

import java.util.HashMap;

/**
 * An inventory view state. Traditional crate gui view and such.
 * Generally, when making guis custom, overriding the page class is a good idea.
 */
public class Page extends State {
    private HashMap<Integer,Element> elements = new HashMap<>();
    private InventoryDimension inventoryDimension = InventoryDimension.of(9,1);
    public boolean fillEmptyWithItem = false;
    public ItemStack empty = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR, DyeColors.BLACK).add(Keys.DISPLAY_NAME,Text.EMPTY).build();
    public Page(String id){
        super(id);
    }
    public Page(String id, InventoryDimension invD){
        super(id);
        this.setInventoryDimension(invD);
    }
    public void setInventoryDimension(InventoryDimension invD){
        inventoryDimension = invD;
    }
    public void putElement(int slotnum, Element element){
        elements.put(slotnum, element);
    }
    public Inventory generatePageView(){
        Inventory ourInventory = Inventory.builder()
                .property(InventoryDimension.PROPERTY_NAME,inventoryDimension)
                .listener(InteractInventoryEvent.class, evt ->{
                    if(!(evt instanceof InteractInventoryEvent.Open) && !(evt instanceof  InteractInventoryEvent.Close)){
                        evt.setCancelled(true);
                        //clickable
                        if(evt.getCursorTransaction().getDefault().getType() != ItemTypes.AIR) {
                            if(evt.getCursorTransaction().getDefault().toContainer().get(DataQuery.of("UnsafeData","slotnum")).isPresent()) {
                                int slotnum = (int) evt.getCursorTransaction().getDefault().toContainer().get(DataQuery.of("UnsafeData", "slotnum")).get();
                                if (elements.get(slotnum) instanceof ActionElement) {
                                    ((ActionElement) elements.get(slotnum)).runAction(id);
                                }
                            }
                        }
                    }
                })
                .property(InventoryTitle.PROPERTY_NAME,InventoryTitle.of(Text.of(TextColors.RED,"HUSKYGUI ",TextColors.YELLOW,id)))
                .build(HuskyCrates.instance);

        int slotNum = 0;
        for(Inventory slot : ourInventory.slots()){
            if(elements.containsKey(slotNum)){
                ItemStack orig = elements.get(slotNum).getDisplayItem();
                ItemStack modifiedStack = ItemStack.builder().fromContainer(orig.toContainer().set(DataQuery.of("UnsafeData","slotnum"),slotNum)).build();
                slot.set(modifiedStack);
            }else if(fillEmptyWithItem){
                slot.set(empty);
            }
            slotNum++;
        }
        return ourInventory;
    }
}
