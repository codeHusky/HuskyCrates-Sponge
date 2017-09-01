package com.codehusky.huskycrates.crate.views;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.CrateCommandSource;
import com.codehusky.huskycrates.crate.VirtualCrate;
import com.codehusky.huskycrates.crate.config.CrateReward;
import com.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;
import com.codehusky.huskycrates.lang.LangData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by lokio on 12/29/2016.
 */
public abstract class CrateView {
	HuskyCrates plugin;
	VirtualCrate vc;
	Inventory inventory;
	Player viewer;
	Task updater;
	ArrayList<Object[]> items;

	public CrateView(Player viewer, InventoryArchetype type, String displayName) {
		this.plugin = HuskyCrates.instance;
		this.viewer = viewer;

		if (type != null) {
			buildInventory(type, displayName);
			updateView(0);
		}
	}

	private void buildInventory(InventoryArchetype type, String displayName) {
		inventory = Inventory.builder()
				.of(type)
				.listener(InteractInventoryEvent.class, event -> {
					if (!(event instanceof InteractInventoryEvent.Open || event instanceof InteractInventoryEvent.Close)) {
						event.setCancelled(true);
					}
				})
				.property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(displayName)))
				.build(plugin);
	}

	public void startUpdateTask() {
		updater = Sponge.getScheduler().createTaskBuilder().execute(this::updateTick).intervalTicks(1).submit(plugin);
	}

	public void openInventory() {
		if (!viewer.isViewingInventory()) {
			viewer.openInventory(inventory, plugin.genericCause);
		}
	}

	protected void updateView(int state) {

	}

	protected abstract void updateTick();

	public void scrambleRewards() {
		ArrayList<Object[]> scrambled = new ArrayList<>();
		ArrayList<Object[]> toSift = (ArrayList<Object[]>) items.clone();
		while (toSift.size() > 0) {
			int pick;
			if (toSift.size() == 1) pick = 0;
			else pick = new Random().nextInt(toSift.size() - 1);
			scrambled.add(toSift.get(pick));
			toSift.remove(pick);
		}
		items = scrambled;
	}

	public int itemIndexSelected() throws RandomItemSelectionFailureException {
		double random = new Random().nextFloat() * vc.getMaxProb();
		double cummProb = 0;
		for (int i = 0; i < items.size(); i++) {
			cummProb += ((double) items.get(i)[0]);
			if (random <= cummProb) {
				return i;
			}
		}
		throw new RandomItemSelectionFailureException();
	}

	public void handleReward(CrateReward giveToPlayer) {
		for (Object reward : giveToPlayer.getRewards()) {
			if (reward instanceof String) {
				Sponge.getCommandManager().process(new CrateCommandSource(), reward.toString().replace("%p", viewer.getName()));
			} else {
				viewer.getInventory().offer(((ItemStack) reward).copy());
			}
		}
		boolean mult = false;
		LangData thisData = giveToPlayer.getLangData();
		if (!giveToPlayer.treatAsSingle() && giveToPlayer.getRewards().size() == 1 && giveToPlayer.getRewards().get(0) instanceof ItemStack) {
			if (((ItemStack) giveToPlayer.getRewards().get(0)).getQuantity() > 1) {
				viewer.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
						thisData.formatter(thisData.rewardMessage, ((ItemStack) giveToPlayer.getRewards().get(0)).getQuantity() + "", viewer, vc, giveToPlayer, null, null)
				));
				if (giveToPlayer.shouldAnnounce()) {
					Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(
							thisData.formatter(thisData.rewardAnnounceMessage, ((ItemStack) giveToPlayer.getRewards().get(0)).getQuantity() + "", viewer, vc, giveToPlayer, null, null)
					));
				}
				mult = true;
			}
		}
		if (!mult) {
			String[] vowels = {"a", "e", "i", "o", "u"};
			if (Arrays.asList(vowels).contains(giveToPlayer.getRewardName().substring(0, 1).toLowerCase())) {
				viewer.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
						thisData.formatter(thisData.rewardMessage, "an", viewer, vc, giveToPlayer, null, null)
				));
				if (giveToPlayer.shouldAnnounce()) {
					Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(
							thisData.formatter(thisData.rewardAnnounceMessage, "an", viewer, vc, giveToPlayer, null, null)
					));
				}
			} else {
				viewer.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
						thisData.formatter(thisData.rewardMessage, "a", viewer, vc, giveToPlayer, null, null)
				));
				if (giveToPlayer.shouldAnnounce()) {
					Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(
							thisData.formatter(thisData.rewardAnnounceMessage, "a", viewer, vc, giveToPlayer, null, null)
					));
				}
			}
		}
	}

	public Inventory getInventory() {
		return inventory;
	}
}
