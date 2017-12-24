package com.codehusky.huskycrates.lang;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.PhysicalCrate;
import com.codehusky.huskycrates.crate.VirtualCrate;
import com.codehusky.huskycrates.crate.config.CrateReward;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class LangData {
    public String prefix;
    public String rewardMessage;
    public String rewardAnnounceMessage;
    public String noKeyMessage;
    public String freeCrateWaitMessage;
    public String withdrawInsufficient;
    public String withdrawSuccess;
    public String depositSuccess;
    public String vkeyUseNotifier;
    public String receivePhysicalKey;
    public String receivePhysicalKeyEnder;
    public String receiveVirtualkey;
    private void defaults() {
        prefix = "";
        rewardMessage = "%prefix%You won %a %R&r from a %C&r!";
        rewardAnnounceMessage = "%p just won %R&r from a %C&r!";
        noKeyMessage = "%prefix%You need a %K&r to open this crate.";
        freeCrateWaitMessage = "%prefix%&7Please wait %t more second(s)";
        withdrawInsufficient = "&cInsufficient balance! (Tried to withdraw %amount%, have %bal%)";
        withdrawSuccess = "&aYou have successfully withdrawn %amount% %C&r&a Key(s).";
        depositSuccess = "&aSuccessfully deposited %amount% %C&r&a Key(s).";
        vkeyUseNotifier = "&7You now have %bal% virtual key(s) for this crate.";
        endings();
        if(HuskyCrates.instance != null){
            if(HuskyCrates.instance.langData != null){
                prefix = HuskyCrates.instance.langData.prefix;
                rewardMessage = HuskyCrates.instance.langData.rewardMessage;
                rewardAnnounceMessage = HuskyCrates.instance.langData.rewardAnnounceMessage;
                noKeyMessage = HuskyCrates.instance.langData.noKeyMessage;
                freeCrateWaitMessage = HuskyCrates.instance.langData.freeCrateWaitMessage;
                withdrawInsufficient = HuskyCrates.instance.langData.withdrawInsufficient;
                withdrawSuccess = HuskyCrates.instance.langData.withdrawSuccess;
                depositSuccess = HuskyCrates.instance.langData.depositSuccess;
                vkeyUseNotifier = HuskyCrates.instance.langData.vkeyUseNotifier;
            }
        }
    }
    public LangData(){
        defaults();
        endings();
    }
    public void endings() {
        prefix += "&r";
        rewardMessage += "&r";
        rewardAnnounceMessage += "&r";
        noKeyMessage += "&r";
        freeCrateWaitMessage+= "&r";
        withdrawInsufficient+= "&r";
        withdrawSuccess+= "&r";
        depositSuccess+= "&r";
        vkeyUseNotifier+= "&r";
    }
    private void configNodeOverrides(ConfigurationNode node){
        if(!node.getNode("prefix").isVirtual()){
            prefix = node.getNode("prefix").getString(prefix);
        }
        if(!node.getNode("rewardMessage").isVirtual()){
            rewardMessage = node.getNode("rewardMessage").getString(rewardMessage);
        }
        if(!node.getNode("rewardAnnounceMessage").isVirtual()){
            rewardAnnounceMessage = node.getNode("rewardAnnounceMessage").getString(rewardAnnounceMessage);
        }
        if(!node.getNode("noKeyMessage").isVirtual()){
            noKeyMessage = node.getNode("noKeyMessage").getString(noKeyMessage);
        }
        if(!node.getNode("freeCrateWaitMessage").isVirtual()){
            freeCrateWaitMessage = node.getNode("freeCrateWaitMessage").getString(freeCrateWaitMessage);
        }
        if(!node.getNode("withdrawInsufficient").isVirtual()){
            withdrawInsufficient = node.getNode("withdrawInsufficient").getString(withdrawInsufficient);
        }
        if(!node.getNode("withdrawSuccess").isVirtual()){
            withdrawSuccess = node.getNode("withdrawSuccess").getString(withdrawSuccess);
        }
        if(!node.getNode("depositSuccess").isVirtual()){
            depositSuccess = node.getNode("depositSuccess").getString(depositSuccess);
        }
        if(!node.getNode("vkeyUseNotifier").isVirtual()){
            vkeyUseNotifier = node.getNode("vkeyUseNotifier").getString(vkeyUseNotifier);
        }
    }
    public LangData(LangData base, ConfigurationNode node){
        prefix = base.prefix;
        rewardMessage = base.rewardMessage;
        rewardAnnounceMessage = base.rewardAnnounceMessage;
        noKeyMessage = base.noKeyMessage;
        freeCrateWaitMessage = base.freeCrateWaitMessage;
        withdrawInsufficient = base.withdrawInsufficient;
        withdrawSuccess = base.withdrawSuccess;
        depositSuccess = base.depositSuccess;
        vkeyUseNotifier = base.vkeyUseNotifier;
        configNodeOverrides(node);
        endings();
    }
    public LangData(ConfigurationNode node){
        defaults(); //defaults, then do overrides.
        configNodeOverrides(node);
        endings();
    }
    public String formatter(String toFormat, String aOrAn, Player plr, VirtualCrate vc, CrateReward rewardHolder, PhysicalCrate ps, Integer amount){
        String formatted = toFormat;
        formatted = formatted.replace("%prefix%",prefix);
        if(aOrAn != null)
            formatted = formatted.replace("%a",aOrAn);
        if(rewardHolder != null) {
            formatted = formatted.replace("%R", rewardHolder.getRewardName());
            formatted = formatted.replace("%r", TextSerializers.FORMATTING_CODE.stripCodes(rewardHolder.getRewardName()));
        }
        if(vc != null) {
            formatted = formatted.replace("%C", vc.displayName);
            formatted = formatted.replace("%c", TextSerializers.FORMATTING_CODE.stripCodes(vc.displayName));
            formatted = formatted.replace("%K",vc.displayName + " Key");
            formatted = formatted.replace("%k",TextSerializers.FORMATTING_CODE.stripCodes(vc.displayName + " Key"));
        }
        if(plr != null) {
            formatted = formatted.replace("%P", plr.getName());
            formatted = formatted.replace("%p", TextSerializers.FORMATTING_CODE.stripCodes(plr.getName()));
            if(vc != null) {
                formatted = formatted.replace("%bal%", vc.getVirtualKeyBalance(plr) + "");
            }
        }
        if(amount != null){
            formatted = formatted.replace("%amount%","" + amount);
        }
        if(ps != null){
            if(ps.vc.getOptions().containsKey("freeCrateDelay")) {
                LocalDateTime lastUsed = ps.vc.lastUsed.get(plr.getUniqueId());
                LocalDateTime minimumWait = lastUsed.plusSeconds((int) ps.vc.getOptions().get("freeCrateDelay"));
                formatted = formatted.replace("%t", "" + (LocalDateTime.now().until(minimumWait, ChronoUnit.SECONDS) + 1));
            }
        }
        return formatted;
    }
}
