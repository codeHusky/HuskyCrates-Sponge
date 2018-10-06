package com.codehusky.huskycrates.event;

import com.codehusky.huskycrates.HuskyCrates;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class CrateInjectionEvent extends AbstractEvent {

    private Cause cause;

    public CrateInjectionEvent(){
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {

            frame.pushCause(HuskyCrates.instance);

            this.cause = frame.getCurrentCause();
        }
    }

    @Override
    public Cause getCause() {
        return cause;
    }


}
