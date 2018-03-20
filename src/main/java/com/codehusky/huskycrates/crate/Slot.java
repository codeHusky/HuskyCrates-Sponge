package com.codehusky.huskycrates.crate;

import com.codehusky.huskycrates.exceptions.ConfigParseError;
import com.codehusky.huskycrates.exceptions.RewardDeliveryError;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;

import java.util.List;

public class Slot {
    private Item displayItem;

    private List<Reward> rewards;

    private Boolean pickRandom; // if winning results in a mystery selection from rewards.

    private Integer pickSize; // default 1

    private Boolean pickUnique; // if winnings have to be unique or if they can be repeated in one pick

    public Slot(CommentedConfigurationNode node){
        this.displayItem = new Item(node.getNode("displayItem"));

        for(CommentedConfigurationNode rNode : node.getNode("rewards").getChildrenList()){
            this.rewards.add(new Reward(rNode));
        }

        this.pickRandom = node.getNode("pickRandom").getBoolean(false);

        if(this.pickRandom){
            this.pickSize = node.getNode("pickSize").getInt(1);
            this.pickUnique = node.getNode("pickUnique").getBoolean(true);
        }
    }

    public boolean rewardPlayer(Player player){
        if(this.pickRandom){

        }else{
            for(Reward reward : rewards){
                reward
            }
        }
    }

    class Reward {
        private RewardType rewardType;

        private String rewardString; // can be a message or a command. :3

        private Item rewardItem;

        private Reward(CommentedConfigurationNode node){
            try {
                this.rewardType = RewardType.valueOf(node.getNode("type").getString("").toUpperCase());
            }catch(IllegalArgumentException e){
                throw new ConfigParseError("Invalid reward type or no reward type specified.",node.getNode("type").getPath());
            }

            if(this.rewardType == RewardType.COMMAND || this.rewardType == RewardType.SERVERMESSAGE || this.rewardType == RewardType.USERMESSAGE){
                rewardString = node.getNode("data").getString();
                if(rewardString == null){
                    throw new ConfigParseError("No data specified for reward.",node.getNode("data").getPath());
                }
            }else if(this.rewardType == RewardType.ITEM){
                rewardItem = new Item(node.getNode("item"));
            }
        }

        public void actOnReward(Player player) {
            if(rewardType == RewardType.ITEM){
                InventoryTransactionResult result = player.getInventory().offer(rewardItem.toItemStack());
                if(result.getType() != InventoryTransactionResult.Type.SUCCESS){
                    throw new RewardDeliveryError("Failed to deliver item to " + player.getName() + " from reward.");
                }
            }
        }


        public RewardType getRewardType() {
            return rewardType;
        }
    }
    enum RewardType {
        COMMAND,
        USERMESSAGE,
        SERVERMESSAGE,
        ITEM
    }
}
