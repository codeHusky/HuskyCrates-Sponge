package pw.codehusky.huskycrates.crate.views;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;

/**
 * Created by lokio on 12/29/2016.
 */
public class NullCrateView extends CrateView {
    private HuskyCrates plugin;
    public NullCrateView(HuskyCrates plugin, Player runner, VirtualCrate virtualCrate){
        this.plugin = plugin;
    }
    @Override
    public Inventory getInventory() {
        Inventory woop = Inventory.builder()
                .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9,1))
                .property(InventoryTitle.PROPERTY_NAME,InventoryTitle.of(Text.of(TextColors.DARK_RED,"INVALID CRATE TYPE!")))
                .listener(InteractInventoryEvent.class, evt ->{
                    if(!(evt instanceof InteractInventoryEvent.Open) && !(evt instanceof  InteractInventoryEvent.Close)){
                        evt.setCancelled(true);
                    }
                    //System.out.println(evt.getClass());
                })
                .build(plugin);
        woop.offer(ItemStack.of(ItemTypes.BARRIER,256*2 + 64));
        for(Inventory e : woop.slots()){
            ItemStack b = e.peek().get();
            b.setQuantity(1);
            e.set(b);
        }
        return woop;
    }
}
