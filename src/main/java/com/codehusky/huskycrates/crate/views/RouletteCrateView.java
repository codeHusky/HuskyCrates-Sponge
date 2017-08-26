package com.codehusky.huskycrates.crate.views;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.VirtualCrate;
import com.codehusky.huskycrates.crate.config.CrateReward;
import com.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by lokio on 12/29/2016.
 */
public class RouletteCrateView extends CrateView {
	private boolean stopped = false;
	private CrateReward holder;
	private boolean firedEnd = false;
	private boolean outOfTime = false;

	public RouletteCrateView(HuskyCrates plugin, Player viewer, VirtualCrate virtualCrate) {
		super(viewer, null, null);
		// Tell the super class not to build an inventory (we have to create the inventory and update the state ourselves)

		this.plugin = plugin;
		vc = virtualCrate;
		items = vc.getItemSet();

		if (virtualCrate.scrambleRewards) {
			scrambleRewards();
		}

		inventory = Inventory.builder()
				.of(InventoryArchetypes.DISPENSER)
				.listener(InteractInventoryEvent.class, evt -> {
					if (!(evt instanceof InteractInventoryEvent.Open) && !(evt instanceof InteractInventoryEvent.Close)) {
						evt.setCancelled(true);
						if (!stopped && evt instanceof ClickInventoryEvent) {
							viewer.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH, viewer.getLocation().getPosition(), 1);
						}
						stopped = true;
					}
				})
				.property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName)))
				.build(plugin);

		updateView(0);
		startUpdateTask(); // Start the update task, which calls updateTick
	}

	private int tickCount = 0;

	private void updateView(int state) {
		int secRemain = (10 - Math.round(tickCount / 20));
		if (secRemain < 0)
			stopped = true;
		int slotNum = 0;
		for (Inventory e : inventory.slots()) {
			double speed = 3;
			double confettiSpeed = 2;
			if (slotNum != 4) {
				if (stopped) {
					if (tickCount == 0 || Math.round(tickCount / confettiSpeed) > Math.round((tickCount - 1) / confettiSpeed)) {
						e.set(confetti());
					} else {
						e.set(e.peek().get());
					}
				} else {
					ItemStack border;
					if (Math.floor(slotNum / 3) != 1) {
						border = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR, DyeColors.BLACK).build();
					} else {
						border = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR, DyeColors.GRAY).build();
					}
					border.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET, "HuskyCrates"));
					ArrayList<Text> itemLore = new ArrayList<>();
					itemLore.add(Text.of(TextColors.DARK_GRAY, "Click anywhere to stop!"));
					itemLore.add(Text.of(TextColors.DARK_GRAY, "Seconds remaining: " + secRemain));
					border.offer(Keys.ITEM_LORE, itemLore);
					e.set(border);
				}
			} else if (!stopped && (tickCount == 0 || Math.round(tickCount / speed) > Math.round((tickCount - 1) / speed))) {
				try {
					int i = itemIndexSelected();
					e.set(((CrateReward) items.get(i)[1]).getDisplayItem());
					holder = (CrateReward) items.get(i)[1];
					viewer.playSound(SoundTypes.UI_BUTTON_CLICK, viewer.getLocation().getPosition(), 0.25);
				} catch (RandomItemSelectionFailureException e1) {
					plugin.logger.error("Random Item Selection failed in Roulette Crate View: " + vc.displayName);
				}

				//e.set(((CrateRewardHolder)items.get(Math.round(tickCount/2) % items.size())[1]).getDisplayItem());
			} else {
				if (stopped && !firedEnd) {
					if (secRemain < 0) {
						outOfTime = true;
						viewer.playSound(SoundTypes.BLOCK_GLASS_BREAK, viewer.getLocation().getPosition(), 1);
					}
					Sponge.getScheduler().createTaskBuilder().execute(task -> {
						updater.cancel();
						viewer.closeInventory(plugin.genericCause);
						handleReward(holder);
						viewer.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, viewer.getLocation().getPosition(), 1);
					}).delay(3, TimeUnit.SECONDS).submit(HuskyCrates.instance);
					firedEnd = true;
				}
				e.set(e.peek().get());
			}
			slotNum++;
		}
	}

	private ItemStack confetti() {
		DyeColor[] colors = {DyeColors.BLUE, DyeColors.CYAN, DyeColors.LIME, DyeColors.LIGHT_BLUE, DyeColors.MAGENTA, DyeColors.ORANGE, DyeColors.PINK, DyeColors.PURPLE, DyeColors.RED, DyeColors.YELLOW};
		ItemStack g = ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.add(Keys.DYE_COLOR, colors[(int) Math.floor(Math.random() * colors.length)])
				.build();
		if (!outOfTime) {
			g.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET, "Your prize awaits..."));
		} else {
			g.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET, TextColors.RED, "Ran out of time!"));
		}
		return g;
	}

	public void updateTick() {
		updateView(0);
		tickCount++;
	}
}
