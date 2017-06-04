package pw.codehusky.huskycrates.crate.config;

import org.spongepowered.api.item.inventory.ItemStack;
import pw.codehusky.huskycrates.lang.SharedLangData;

public class CrateRewardHolder {
    private ItemStack displayItem;
    private CrateReward reward;
    private boolean dispRewardSimilar;
    private double chance;
    private SharedLangData langData;
    public CrateRewardHolder(ItemStack disp, CrateReward reward, double chance, boolean dispRewardSimilar,SharedLangData langData) {
        this.reward = reward;
        this.displayItem = disp;
        this.chance = chance;
        this.dispRewardSimilar = dispRewardSimilar;
        this.langData = langData;
    }

    public CrateReward getReward() {
        return reward;
    }

    public double getChance() {
        return chance;
    }

    public SharedLangData getLangData() {
        return langData;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public boolean isDispRewardSimilar() {
        return dispRewardSimilar;
    }
}
