package pw.codehusky.huskycrates.lang;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.PhysicalCrate;
import pw.codehusky.huskycrates.crate.VirtualCrate;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SharedLangData {
    public String prefix;
    public String rewardMessage;
    public String rewardAnnounceMessage;
    public String noKeyMessage;
    public String freeCrateWaitMessage;
    private void defaults() {
        //"", ,,
        prefix = "";
        rewardMessage = "%prefix%You won %a %R&r from a %C&r!";
        rewardAnnounceMessage = "%p just won %R&r from a %C&r!";
        noKeyMessage = "%prefix%You need a %K&r to open this crate.";
        freeCrateWaitMessage = "%prefix%&7Please wait %t more second(s)";
        endings();
        if(HuskyCrates.instance != null){
            if(HuskyCrates.instance.langData != null){
                prefix = HuskyCrates.instance.langData.prefix;
                rewardMessage = HuskyCrates.instance.langData.rewardMessage;
                rewardAnnounceMessage = HuskyCrates.instance.langData.rewardAnnounceMessage;
                noKeyMessage = HuskyCrates.instance.langData.noKeyMessage;
                freeCrateWaitMessage = HuskyCrates.instance.langData.freeCrateWaitMessage;
            }
        }
    }
    public SharedLangData(){
        defaults();
        endings();
    }
    public void endings() {
        prefix += "&r";
        rewardMessage += "&r";
        rewardAnnounceMessage += "&r";
        noKeyMessage += "&r";
        freeCrateWaitMessage+= "&r";
    }
    public SharedLangData(SharedLangData base, ConfigurationNode node){
        prefix = base.prefix;
        rewardMessage = base.rewardMessage;
        rewardAnnounceMessage = base.rewardAnnounceMessage;
        noKeyMessage = base.noKeyMessage;
        freeCrateWaitMessage = base.freeCrateWaitMessage;
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
            noKeyMessage = node.getNode("freeCrateWaitMessage").getString(freeCrateWaitMessage);
        }
        endings();
    }
    public SharedLangData(ConfigurationNode node){
        defaults(); //defaults, then do overrides.
        if(!node.getNode("prefix").isVirtual()){
            prefix = node.getNode("prefix").getString(prefix);
        }
        if(!node.getNode("rewardMessage").isVirtual()){
            rewardMessage = node.getNode("rewardMessage").getString(rewardMessage);
        }
        if(!node.getNode("rewardAnnounceMessage").isVirtual()){
            //HuskyCrates.instance.logger.info("overriding");
            rewardAnnounceMessage = node.getNode("rewardAnnounceMessage").getString(rewardAnnounceMessage);
            //HuskyCrates.instance.logger.info(rewardAnnounceMessage);
        }
        if(!node.getNode("noKeyMessage").isVirtual()){
            noKeyMessage = node.getNode("noKeyMessage").getString(noKeyMessage);
        }
        if(!node.getNode("freeCrateWaitMessage").isVirtual()){
            noKeyMessage = node.getNode("freeCrateWaitMessage").getString(freeCrateWaitMessage);
        }
        endings();
    }
    public SharedLangData(String prefix, String rewardMessage, String rewardAnnounceMessage, String noKeyMessage, String freeCrateWaitMessage){
        this.prefix = prefix;
        this.rewardMessage = rewardMessage;
        this.rewardAnnounceMessage = rewardAnnounceMessage;
        this.noKeyMessage = noKeyMessage;
        this.freeCrateWaitMessage = freeCrateWaitMessage;
        endings();
    }
    public String formatter(String toFormat, String aOrAn, Player context, VirtualCrate vc, CrateRewardHolder rewardHolder, PhysicalCrate ps){
        String formatted = toFormat;
        formatted = formatted.replaceAll("%prefix%",prefix);
        if(aOrAn != null)
            formatted = formatted.replaceAll("%a",aOrAn);
        if(rewardHolder != null) {
            formatted = formatted.replaceAll("%R", rewardHolder.getReward().getRewardName());
            formatted = formatted.replaceAll("%r", TextSerializers.FORMATTING_CODE.stripCodes(rewardHolder.getReward().getRewardName()));
        }
        if(vc != null) {
            formatted = formatted.replaceAll("%C", vc.displayName);
            formatted = formatted.replaceAll("%c", TextSerializers.FORMATTING_CODE.stripCodes(vc.displayName));
            formatted = formatted.replaceAll("%K",vc.displayName + " Key");
            formatted = formatted.replaceAll("%k",TextSerializers.FORMATTING_CODE.stripCodes(vc.displayName + " Key"));
        }
        if(context != null) {
            formatted = formatted.replaceAll("%P", context.getName());
            formatted = formatted.replaceAll("%p", TextSerializers.FORMATTING_CODE.stripCodes(context.getName()));
        }
        if(ps != null){
            LocalDateTime lastUsed = ps.lastUsed.get(context.getUniqueId());
            LocalDateTime minimumWait = lastUsed.plusSeconds((int) ps.vc.getOptions().get("freeCrateDelay"));
            formatted = formatted.replaceAll("%t", "" +(LocalDateTime.now().until(minimumWait, ChronoUnit.SECONDS)+1));
        }
        return formatted;
    }
}
