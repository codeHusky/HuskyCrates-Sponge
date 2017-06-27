package com.codehusky.huskyui.components.inventory;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskyui.components.inventory.elements.ActionElement;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;

/**
 * Created by lokio on 6/26/2017.
 */
public class AutoPage extends Page {
    private ArrayList<Element> autoElements = new ArrayList<>();
    public boolean centered = false;
    public AutoPage(String id) {
        super(id);
        fillEmptyWithItem = true;
    }

    public AutoPage(String id, InventoryDimension invD) {
        super(id, invD);
        fillEmptyWithItem = true;
    }

    @Override
    public void setInventoryDimension(InventoryDimension invD) {
        throw new NullPointerException();
    }

    @Override
    public void putElement(int slotnum, Element element) {
        throw new NullPointerException();
        //autoElements.add(element);
    }

    public void addElement(Element element){
        autoElements.add(element);
    }

    @Override
    public Inventory generatePageView() {
        /*
        int height = (int)Math.ceil(((double)HuskyCrates.instance.crateUtilities.getCrateTypes().size())/9d);
            System.out.println(height);
            cratePage.setInventoryDimension(InventoryDimension.of(9,height + 1));
         */
        int rowsToDisplay = (int)Math.ceil(((double) autoElements.size())/9d);
        inventoryDimension = InventoryDimension.of(9,( rowsToDisplay+ 1));
        Inventory ourInventory = Inventory.builder()
                .property(InventoryDimension.PROPERTY_NAME,inventoryDimension)
                .listener(InteractInventoryEvent.class, evt ->{
                    if(!(evt instanceof InteractInventoryEvent.Open) && !(evt instanceof  InteractInventoryEvent.Close)){
                        evt.setCancelled(true);
                        //clickable
                        if(evt.getCursorTransaction().getDefault().getType() != ItemTypes.AIR) {
                            if(evt.getCursorTransaction().getDefault().toContainer().get(DataQuery.of("UnsafeData","slotnum")).isPresent()) {
                                int slotnum = (int) evt.getCursorTransaction().getDefault().toContainer().get(DataQuery.of("UnsafeData", "slotnum")).get();
                                if(slotnum == -1){
                                    //back
                                    ui.openState(observer,parentState);
                                }else if (autoElements.get(slotnum) instanceof ActionElement) {
                                    ((ActionElement) autoElements.get(slotnum)).runAction(id);
                                }
                            }
                        }
                    }
                })
                .property(InventoryTitle.PROPERTY_NAME,InventoryTitle.of(title))
                .build(HuskyCrates.instance);
        int slotNum = 0;
        for(Inventory slot : ourInventory.slots()){
            if(autoElements.size() > slotNum){
                if(centered && (slotNum >= ((rowsToDisplay-1)*9))){
                    if(autoElements.size()%2 == 1){//odd
                        int width = autoElements.size()%9;
                    }
                }
                ItemStack orig = autoElements.get(slotNum).getDisplayItem();
                ItemStack modifiedStack = ItemStack.builder().fromContainer(orig.toContainer().set(DataQuery.of("UnsafeData","slotnum"),slotNum)).build();
                slot.set(modifiedStack);
            }else if(slotNum > (rowsToDisplay*9)-1){

                if(slotNum == rowsToDisplay*9+4){


                    ItemStack orig = ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET, TextColors.WHITE,"Back")).build();
                    ItemStack modifiedStack = ItemStack.builder().fromContainer(orig.toContainer().set(DataQuery.of("UnsafeData","slotnum"),-1)).build();
                    slot.set(modifiedStack);
                }else if(fillEmptyWithItem){
                    slot.set(empty);
                }
            }
            slotNum++;
        }
        return ourInventory;
    }
}
