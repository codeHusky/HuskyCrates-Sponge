package pw.codehusky.huskycrates.crate.config;

public class CrateReward<T> {
    private T reward;
    private String rewardName;
    private boolean treatAsSingle;
    public CrateReward(T reward, String name, boolean treatAsSingle){
        this.reward = reward;
        this.rewardName = name;
        this.treatAsSingle = treatAsSingle;
    }
    public T getReward() {
        return reward;
    }
    public String getRewardName() {
        return rewardName;
    }
    public boolean treatAsSingle() {
        return treatAsSingle;
    }
}
