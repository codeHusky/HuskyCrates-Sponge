package pw.codehusky.huskycrates.crate.config;

import org.spongepowered.api.item.inventory.ItemStack;

public class CrateReward<T> {
    private T reward;
    private String rewardName;
    private boolean treatAsSingle;
    public CrateReward(T reward, String name, boolean treatAsSingle){
        this.reward = reward;
        if(reward instanceof String){
            String ret = reward.toString();
            if(ret.split("")[0].equals("/")){
                this.reward = (T) ret.substring(1);
            }
        }
        this.rewardName = name;
        this.treatAsSingle = treatAsSingle;
        //System.out.println(treatAsSingle);
    }
    public T getReward() {
        if(reward instanceof ItemStack)
            return (T)((ItemStack)reward).copy(); //lmao intellij chill out
        return reward;
    }
    public String getRewardName() {
        return rewardName;
    }
    public boolean treatAsSingle() {
        return treatAsSingle;
    }
}
