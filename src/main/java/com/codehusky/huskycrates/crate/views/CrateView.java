package com.codehusky.huskycrates.crate.views;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.CrateCommandSource;
import com.codehusky.huskycrates.crate.VirtualCrate;
import com.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.serializer.TextSerializers;
import com.codehusky.huskycrates.crate.config.CrateReward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by lokio on 12/29/2016.
 */
public class CrateView {
    HuskyCrates plugin;
    VirtualCrate vc;
    Player ourplr;
    ArrayList<Object[]> items;
    public Inventory getInventory(){
        return null;
    }
    //empty class for organization and such
    public int itemIndexSelected() throws RandomItemSelectionFailureException {
        double random = new Random().nextFloat()*vc.getMaxProb();
        double cummProb = 0;
        for(int i = 0; i < items.size(); i++) {
            cummProb += ((double) items.get(i)[0]);
            if (random <= cummProb) {
                return i;
            }
        }
        throw new RandomItemSelectionFailureException();
    }
    public void handleReward(CrateReward giveToPlayer){
        for(Object reward : giveToPlayer.getRewards()) {
            //System.out.println(reward);
            if (reward instanceof String) {
                Sponge.getCommandManager().process(new CrateCommandSource(), reward.toString().replace("%p", ourplr.getName()));
            } else {
                //System.out.println(giveToPlayer.getReward().treatAsSingle());

                ourplr.getInventory().offer(((ItemStack) reward).copy());
            }
        }
        boolean mult = false;
        if (!giveToPlayer.treatAsSingle() && giveToPlayer.getRewards().size() == 1 && giveToPlayer.getRewards().get(0) instanceof ItemStack) {
            if (((ItemStack) giveToPlayer.getRewards().get(0)).getQuantity() > 1) {
                    /*ourplr.sendMessage(Text.of("You won ", TextColors.YELLOW,
                            ((ItemStack) giveToPlayer.getReward().getReward()).getQuantity() + " ",
                            TextSerializers.FORMATTING_CODE.deserialize(giveToPlayer.getReward().getRewardName()), TextColors.RESET, " from a ",
                            TextSerializers.FORMATTING_CODE.deserialize(vc.displayName), TextColors.RESET, "!"));*/
                ourplr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                        vc.getLangData().formatter(vc.getLangData().rewardMessage, ((ItemStack) giveToPlayer.getRewards().get(0)).getQuantity() + "", ourplr, vc, giveToPlayer, null, null)
                ));
                if (giveToPlayer.shouldAnnounce()) {
                    Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(
                            vc.getLangData().formatter(vc.getLangData().rewardAnnounceMessage, ((ItemStack) giveToPlayer.getRewards().get(0)).getQuantity() + "", ourplr, vc, giveToPlayer, null, null)
                    ));
                }
                mult = true;
            }
        }
        if (!mult) {
            String[] vowels = {"a", "e", "i", "o", "u"};
            if (Arrays.asList(vowels).contains(giveToPlayer.getRewardName().substring(0, 1).toLowerCase())) {
                ourplr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                        vc.getLangData().formatter(vc.getLangData().rewardMessage, "an", ourplr, vc, giveToPlayer, null, null)
                ));
                if (giveToPlayer.shouldAnnounce()) {
                    Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(
                            vc.getLangData().formatter(vc.getLangData().rewardAnnounceMessage, "an", ourplr, vc, giveToPlayer, null, null)
                    ));
                }
            } else {
                ourplr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                        vc.getLangData().formatter(vc.getLangData().rewardMessage, "a", ourplr, vc, giveToPlayer, null, null)
                ));
                if (giveToPlayer.shouldAnnounce()) {
                    Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(
                            vc.getLangData().formatter(vc.getLangData().rewardAnnounceMessage, "a", ourplr, vc, giveToPlayer, null, null)
                    ));
                }
            }
        }

    }
}
