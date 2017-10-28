package com.codehusky.huskycrates.events;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.event.user.TargetUserEvent;

public class VKeyBalanceChangeEvent extends AbstractEvent implements TargetUserEvent {

    private final User user;
    private final String crateId;
    private final Integer oldBalance;
    private final Integer newBalance;

    public VKeyBalanceChangeEvent(User user, String crateId, Integer oldBalance, Integer newBalance) {
        this.user = user;
        this.crateId = crateId;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    public String getCrateId() {
        return this.crateId;
    }

    public Integer getOldBalance() {
        return this.oldBalance;
    }

    public Integer getNewBalance() {
        return this.newBalance;
    }

    @Override
    public Cause getCause() {
        return null;
    }

    @Override
    public User getTargetUser() {
        return this.user;
    }
}
