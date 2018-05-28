package com.codehusky.huskycrates.crate.virtual;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.virtual.effects.Effect;
import com.codehusky.huskycrates.exception.ConfigError;
import com.codehusky.huskycrates.exception.ConfigParseError;
import com.codehusky.huskycrates.exception.RewardDeliveryError;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Slot {
    private Item displayItem;

    private List<Reward> rewards = new ArrayList<>();
    private List<List<Reward>> rewardGroups = new ArrayList<>();

    private Integer chance;

    private Boolean pickRandom; // if winning results in a mystery selection from rewards.

    private Integer pickSize; // default 1

    private Boolean pickUnique; // if winnings have to be unique or if they can be repeated in one pick

    public Slot(ConfigurationNode node, Crate holder){
        this.displayItem = new Item(node.getNode("displayItem"));


        for(ConfigurationNode rNode : node.getNode("rewards").getChildrenList()){
            if(rNode.hasListChildren()){
                ArrayList<Reward> rewardGroup = new ArrayList<>();
                for(ConfigurationNode rgNode : rNode.getChildrenList()){
                    rewardGroup.add(new Reward(rgNode,node.getNode("displayItem"),holder));
                }
                this.rewardGroups.add(rewardGroup);
            }else {
                this.rewards.add(new Reward(rNode,node.getNode("displayItem"),holder));
            }
        }

        if(this.rewards.size() == 0){
            HuskyCrates.instance.logger.warn("Slot has no rewards @ " + ConfigError.readablePath(node.getNode("rewards").getPath()));
        }

        if(node.getNode("chance").isVirtual()){
            throw new ConfigParseError("Chance not specified in reward.",node.getNode("chance").getPath());
        }
        this.chance = node.getNode("chance").getInt();


        this.pickRandom = node.getNode("pickRandom").getBoolean(false);

        if(this.pickRandom){
            this.pickSize = node.getNode("pickSize").getInt(1);

            if(this.pickSize > this.rewards.size()){
                throw new ConfigParseError("pickSize is bigger than the amount of rewards.",node.getNode("pickSize").getPath());
            }

            this.pickUnique = node.getNode("pickUnique").getBoolean(true);
        }
    }

    public boolean rewardPlayer(Player player, Location<World> crateLocation){
        List<Object> theseRewards = new ArrayList<>(rewards);
        theseRewards.addAll(rewardGroups);
        if(this.pickRandom){
            ArrayList<Object> availRewards = new ArrayList<>(rewards);
            availRewards.addAll(rewardGroups);
            ArrayList<Object> selectedRewards = new ArrayList<>();

            for(int i = 0; i < this.pickSize; i++){
                Object selected = availRewards.get(new Random().nextInt(availRewards.size()));
                selectedRewards.add(selected);

                if(this.pickUnique) availRewards.remove(selected);
            }
            theseRewards = selectedRewards;
        }
        try {
            for (Object reward : theseRewards) {
                if(reward instanceof Reward) {
                    ((Reward)reward).actOnReward(player, crateLocation);
                }else if(reward instanceof List){
                    for(Reward rr : (List<Reward>)reward){
                        rr.actOnReward(player,crateLocation);
                    }
                }
            }
        }catch(RewardDeliveryError e){
            e.printStackTrace();
            player.sendMessage(Text.of(TextColors.RED,"A fatal error has occurred while trying to deliver your reward. Please contact server administration."));
            return false;
        }
        return true;
    }

    public Integer getChance() {
        return chance;
    }

    public Item getDisplayItem() {
        return displayItem;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public class Reward {
        private RewardType rewardType;

        private String rewardString; // can be a message or a command. :3

        private Item rewardItem;

        private Effect effect;
        private boolean effectOnPlayer = false;

        private Integer keyCount = 1;

        private Reward(ConfigurationNode node, ConfigurationNode displayItemNode, Crate holder){
            try {
                this.rewardType = RewardType.valueOf(node.getNode("type").getString("").toUpperCase());
            }catch(IllegalArgumentException e){
                throw new ConfigParseError("Invalid reward type or no reward type specified.",node.getNode("type").getPath());
            }

            if(this.rewardType == RewardType.USERCOMMAND || this.rewardType == RewardType.SERVERCOMMAND || this.rewardType == RewardType.SERVERMESSAGE || this.rewardType == RewardType.USERMESSAGE || this.rewardType == RewardType.KEY){
                rewardString = node.getNode("data").getString();
                if(rewardString == null){
                    throw new ConfigParseError("No data specified for reward.",node.getNode("data").getPath());
                }
                if(this.rewardType == RewardType.KEY){
                    if(!HuskyCrates.registry.isKey(rewardString) && !holder.hasLocalKey() && !holder.getLocalKey().getId().equals(rewardString)){
                        throw new ConfigParseError("Invalid key ID!",node.getNode("data").getPath());
                    }else{
                        this.keyCount = node.getNode("keyCount").getInt(1);
                    }
                }
            }else if(this.rewardType == RewardType.ITEM){
                if(node.getNode("item").isVirtual()){
                    rewardItem = new Item(displayItemNode);
                }else {
                    rewardItem = new Item(node.getNode("item"));
                }
            }else if(this.rewardType == RewardType.EFFECT){
                effect = new Effect(node.getNode("effect"));
                effectOnPlayer = node.getNode("effectOnPlayer").getBoolean(false);
            }
        }

        private String replaceCommand(Player player, Location<World> crateLocation){
            return rewardString
                    .replace("%p",player.getName())
                    .replace("%P",player.getUniqueId().toString())

                    .replace("%cxi",crateLocation.getBlockX() + "")
                    .replace("%cyi",crateLocation.getBlockY() + "")
                    .replace("%czi",crateLocation.getBlockZ() + "")
                    .replace("%cxd",crateLocation.getX() + "")
                    .replace("%cyd",crateLocation.getY() + "")
                    .replace("%czd",crateLocation.getZ() + "")

                    .replace("%pxi",player.getLocation().getBlockX() + "")
                    .replace("%pyi",player.getLocation().getBlockY() + "")
                    .replace("%pzi",player.getLocation().getBlockZ() + "")
                    .replace("%pxd",player.getLocation().getX() + "")
                    .replace("%pyd",player.getLocation().getY() + "")
                    .replace("%pzd",player.getLocation().getZ() + "");
        }

        public void actOnReward(Player player, Location<World> crateLocation) {
            if(rewardType == RewardType.ITEM){

                InventoryTransactionResult result = Util.getHotbarFirst(player.getInventory()).offer(rewardItem.toItemStack());
                if(result.getType() != InventoryTransactionResult.Type.SUCCESS){
                    throw new RewardDeliveryError("Failed to deliver item to " + player.getName() + " from reward.");
                }

            }else if(rewardType == RewardType.SERVERMESSAGE){
                Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(replaceCommand(player,crateLocation)));

            }else if(rewardType == RewardType.USERMESSAGE){
                player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(replaceCommand(player, crateLocation)));

            }else if(rewardType == RewardType.SERVERCOMMAND){
                Sponge.getCommandManager().process(Sponge.getServer().getConsole(),replaceCommand(player, crateLocation));

            }else if(rewardType == RewardType.USERCOMMAND){
                Sponge.getCommandManager().process(player,replaceCommand(player, crateLocation));

            }else if(rewardType == RewardType.EFFECT){
                HuskyCrates.registry.runEffect(effect,(effectOnPlayer)? player.getLocation() : crateLocation );
            }else if(rewardType == RewardType.KEY){
                if(HuskyCrates.registry.getKey(rewardString) == null){
                    throw new RewardDeliveryError("Failed to deliver key to " + player.getName() + ": \"" + rewardString + "\" is not a valid key id.");
                }
                //BUG!!!
                InventoryTransactionResult result = Util.getHotbarFirst(
                        player.getInventory())
                        .offer(
                                HuskyCrates.registry.getKey(rewardString)
                                        .getKeyItemStack(this.keyCount)
                        );
                if(result.getType() != InventoryTransactionResult.Type.SUCCESS){
                    throw new RewardDeliveryError("Failed to deliver key to " + player.getName() + " from reward.");
                }
            } else {
                throw new RewardDeliveryError("Failed to deliver reward to " + player.getName() + " due to invalid reward type. If you see this, contact the developer immediately.");
            }
        }


        public RewardType getRewardType() {
            return rewardType;
        }
    }
    public enum RewardType {
        USERCOMMAND,
        SERVERCOMMAND,

        USERMESSAGE,
        SERVERMESSAGE,

        ITEM,

        EFFECT,

        KEY
    }
}
