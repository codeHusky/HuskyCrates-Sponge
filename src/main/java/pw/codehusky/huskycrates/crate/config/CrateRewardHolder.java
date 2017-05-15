package pw.codehusky.huskycrates.crate.config;

import org.spongepowered.api.item.inventory.ItemStack;

public class CrateRewardHolder {
    private ItemStack displayItem;
    private CrateReward reward;
    private boolean dispRewardSimilar;
    private double chance;
    public CrateRewardHolder(ItemStack disp, CrateReward reward, double chance, boolean dispRewardSimilar) {
        this.reward = reward;
        this.displayItem = disp;
        this.chance = chance;
        this.dispRewardSimilar = dispRewardSimilar;
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

    public boolean isDispRewardSimilar() {
        return dispRewardSimilar;
    }
}
