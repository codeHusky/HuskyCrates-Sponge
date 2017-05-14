package pw.codehusky.huskycrates.crate.config;

import org.spongepowered.api.item.inventory.ItemStack;

public class CrateRewardHolder {
    private ItemStack displayItem;
    private CrateReward reward;
    private double chance;
    public CrateRewardHolder(ItemStack disp, CrateReward reward, double chance) {
        this.reward = reward;
        this.displayItem = disp;
        this.chance = chance;
    }

    public CrateReward getReward() {
        return reward;
    }

    public double getChance() {
        return chance;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }
}
