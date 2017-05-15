package pw.codehusky.huskycrates.crate.views;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;

public class NullCrateView implements CrateView {


    public NullCrateView(Player runner, VirtualCrate virtualCrate) {
    }

    @Override
    public Inventory getInventory() {
        Inventory woop = Inventory.builder()
                .property(InventoryDimension.PROPERTY_NAM, InventoryDimension.of(9, 1))
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(TextColors.DARK_RED, "INVALID CRATE TYPE!")))
                .listener(ClickInventoryEvent.class, evt -> evt.setCancelled(true))
                .build(HuskyCrates.instance);
        woop.offer(ItemStack.of(ItemTypes.BARRIER, 256 * 2 + 64));
        for (Inventory e : woop.slots()) {
            ItemStack b = e.peek().get();
            b.setQuantity(1);
            e.set(b);
        }
        return woop;
    }
}
