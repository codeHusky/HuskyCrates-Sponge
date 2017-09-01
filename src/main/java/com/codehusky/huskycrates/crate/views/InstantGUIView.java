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
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.concurrent.TimeUnit;

public class InstantGUIView extends CrateView {
	private CrateReward holder = null;

	public InstantGUIView(Player viewer, VirtualCrate virtualCrate) {
		super(viewer, InventoryArchetypes.DISPENSER, virtualCrate.displayName);

		vc = virtualCrate;
		items = vc.getItemSet();

		if (virtualCrate.scrambleRewards) {
			scrambleRewards();
		}

		startUpdateTask(); // Start the update task that calls updateTick

		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			updater.cancel();
			viewer.closeInventory(plugin.genericCause);
			handleReward(holder);
			viewer.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, viewer.getLocation().getPosition(), 1);
		}).delay(3, TimeUnit.SECONDS).submit(HuskyCrates.instance);
	}

	private int tickCount = 0;

	@Override
	protected void updateView(int state) {
		int slotNum = 0;
		for (Inventory e : inventory.slots()) {
			double speed = 3;
			double confettiSpeed = 2;
			if (slotNum != 4) {
				if (tickCount == 0 || Math.round(tickCount / confettiSpeed) > Math.round((tickCount - 1) / confettiSpeed)) {
					e.set(confetti());
				} else {
					e.set(e.peek().get());
				}
			} else if (holder == null) {
				try {
					int i = itemIndexSelected();
					e.set(((CrateReward) items.get(i)[1]).getDisplayItem());
					holder = (CrateReward) items.get(i)[1];
					viewer.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH, viewer.getLocation().getPosition(), 1);
				} catch (RandomItemSelectionFailureException e1) {
					plugin.logger.error("Random Item Selection failed in Roulette Crate View: " + vc.displayName);
				}
			} else {
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
		g.offer(Keys.DISPLAY_NAME, Text.of("HuskyCrates"));
		return g;
	}

	public void updateTick() {
		updateView(0);
		tickCount++;
	}
}
