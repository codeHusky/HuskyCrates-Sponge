package com.codehusky.huskycrates;

import com.codehusky.huskycrates.crate.VirtualCrate;
import org.spongepowered.api.entity.living.player.User;
import java.util.Optional;

public class HuskyAPI {

    private HuskyCrates huskyCrates = HuskyCrates.instance;

    private static HuskyAPI huskyAPI;

    public HuskyAPI() {
        huskyAPI = this;
    }

    public static HuskyAPI getAPI() {
        return huskyAPI;
    }

    public Optional<Integer> getKeyBal(User user, String id) {
        VirtualCrate vc = huskyCrates.crateUtilities.getVirtualCrate(id);
        if(vc != null) {
            return Optional.of(vc.getVirtualKeyBalance(user));
        }
        return Optional.empty();
    }

    public void setKeyBal(User user, String id, int balance) {
        VirtualCrate vc = huskyCrates.crateUtilities.getVirtualCrate(id);
        if(vc != null) {
            vc.setVirtualKeys(user, balance);
        }
    }

    public void takeKeyBal(User user, String id, int val) {
        VirtualCrate vc = huskyCrates.crateUtilities.getVirtualCrate(id);
        if(vc != null) {
            vc.takeVirtualKeys(user, val);
        }
    }

    public void giveKeyBal(User user, String id, int val) {
        VirtualCrate vc = huskyCrates.crateUtilities.getVirtualCrate(id);
        if(vc != null) {
            vc.giveVirtualKeys(user, val);
        }
    }


}
