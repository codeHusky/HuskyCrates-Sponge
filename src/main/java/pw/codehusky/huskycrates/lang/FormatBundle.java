package pw.codehusky.huskycrates.lang;

import org.spongepowered.api.entity.living.player.Player;
import pw.codehusky.huskycrates.crate.PhysicalCrate;
import pw.codehusky.huskycrates.crate.VirtualCrate;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolder;

public class FormatBundle {
    private String toFormat;
    private String aOrAn;
    private Player plr;
    private VirtualCrate vc;
    private CrateRewardHolder rewardHolder;
    private PhysicalCrate ps;
    private Integer amount;
    FormatBundle(String tf, String a, Player p, VirtualCrate v, CrateRewardHolder crh, PhysicalCrate pc, Integer am) {
        toFormat = tf;
        aOrAn = a;
        plr=p;
        vc=v;
        rewardHolder=crh;
        ps=pc;
        amount=am;
    }
    public class Builder {
        private String toFormat = null;
        private String aOrAn = null;
        private Player plr = null;
        private VirtualCrate vc = null;
        private CrateRewardHolder rewardHolder = null;
        private PhysicalCrate ps = null;
        private Integer amount = null;
        public Builder(){}
        public void toFormat(String toFormat){
            this.toFormat = toFormat;
        }
        public void aOrAn(String aOrAn){
            this.aOrAn = aOrAn;
        }
        public void plr(Player plr){
            this.plr = plr;
        }
        public void vc(VirtualCrate vc){
            this.vc = vc;
        }
        public void rewardHolder(CrateRewardHolder rewardHolder){
            this.rewardHolder = rewardHolder;
        }
        public void ps(PhysicalCrate ps){
            this.ps = ps;
        }
        public void amount(Integer amount){
            this.amount = amount;
        }
        public FormatBundle build() {
            return new FormatBundle(toFormat,aOrAn,plr,vc,rewardHolder,ps,amount);
        }
    }
}
