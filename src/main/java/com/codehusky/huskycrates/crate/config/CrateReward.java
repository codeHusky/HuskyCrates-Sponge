package com.codehusky.huskycrates.crate.config;

import com.codehusky.huskycrates.lang.LangData;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;

public class CrateReward {
    private ItemStack displayItem;
    private ArrayList<Object> rewards;
    private double chance;
    private LangData langData;
    private String rewardName;
    private boolean shouldAnnounce;
    private boolean single;
    public CrateReward(ItemStack disp, ArrayList<Object> rewards, String rewardName, double chance, LangData langData, boolean shouldAnnounce, boolean single) {
        this.rewards = rewards;
        this.displayItem = disp;
        this.chance = chance;
        this.langData = langData;
        this.shouldAnnounce = shouldAnnounce;
        this.rewardName = rewardName;
        this.single = single;
    }

    public ArrayList<Object> getRewards() {
        return rewards;
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

    public String getRewardName() {
        return rewardName;
    }

    public boolean shouldAnnounce() {
        return shouldAnnounce;
    }
    public boolean treatAsSingle() {
        return single;
    }
}
