package pw.codehusky.huskycrates.crate.config;

import org.spongepowered.api.item.inventory.ItemStack;
import pw.codehusky.huskycrates.lang.LangData;

public class CrateRewardHolder {
    private ItemStack displayItem;
    private CrateReward reward;
    private boolean dispRewardSimilar;
    private double chance;
    private LangData langData;
    private boolean shouldAnnounce;
    public CrateRewardHolder(ItemStack disp, CrateReward reward, double chance, boolean dispRewardSimilar, LangData langData, boolean shouldAnnounce) {
        this.reward = reward;
        this.displayItem = disp;
        this.chance = chance;
        this.dispRewardSimilar = dispRewardSimilar;
        this.langData = langData;
        this.shouldAnnounce = shouldAnnounce;
    }

    public CrateReward getReward() {
        return reward;
    }

    public double getChance() {
        return chance;
    }

    public LangData getLangData() {
        return langData;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public boolean isDispRewardSimilar() {
        return dispRewardSimilar;
    }

    public boolean shouldAnnounce() {
        return shouldAnnounce;
    }
}
