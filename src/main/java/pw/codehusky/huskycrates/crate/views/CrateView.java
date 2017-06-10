package pw.codehusky.huskycrates.crate.views;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.crate.CrateCommandSource;
import pw.codehusky.huskycrates.crate.VirtualCrate;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolder;
import pw.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by lokio on 12/29/2016.
 */
public class CrateView {
    public VirtualCrate vc;
    public Player ourplr;
    public ArrayList<Object[]> items;
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
    public void handleReward(CrateRewardHolder giveToPlayer){
        if (giveToPlayer.getReward().getReward() instanceof String){
            Sponge.getCommandManager().process(new CrateCommandSource(), giveToPlayer.getReward().getReward().toString().replace("%p", ourplr.getName()));
        }else {
            //System.out.println(giveToPlayer.getReward().treatAsSingle());

            ourplr.getInventory().offer((ItemStack) giveToPlayer.getReward().getReward());
        }
        boolean mult = false;
        if (!giveToPlayer.getReward().treatAsSingle() &&  giveToPlayer.getReward().getReward() instanceof ItemStack) {
            if(((ItemStack) giveToPlayer.getReward().getReward()).getQuantity() > 1) {
                        /*ourplr.sendMessage(Text.of("You won ", TextColors.YELLOW,
                                ((ItemStack) giveToPlayer.getReward().getReward()).getQuantity() + " ",
                                TextSerializers.FORMATTING_CODE.deserialize(giveToPlayer.getReward().getRewardName()), TextColors.RESET, " from a ",
                                TextSerializers.FORMATTING_CODE.deserialize(vc.displayName), TextColors.RESET, "!"));*/
                ourplr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                        vc.langData.formatter(vc.langData.prefix + vc.langData.rewardMessage,((ItemStack) giveToPlayer.getReward().getReward()).getQuantity() + "",ourplr,vc,giveToPlayer)
                ));
                mult = true;
            }
        }
        if(!mult){
            String[] vowels = {"a", "e", "i", "o", "u"};
            if (Arrays.asList(vowels).contains(giveToPlayer.getReward().getRewardName().substring(0, 1).toLowerCase())) {
                ourplr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                        vc.langData.formatter(vc.langData.prefix + vc.langData.rewardMessage,"an",ourplr,vc,giveToPlayer)
                ));
            } else {
                ourplr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                        vc.langData.formatter(vc.langData.prefix + vc.langData.rewardMessage,"a",ourplr,vc,giveToPlayer)
                ));
            }
        }
    }
}
