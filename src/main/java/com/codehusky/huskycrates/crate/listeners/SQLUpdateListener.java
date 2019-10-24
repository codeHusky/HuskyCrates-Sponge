package com.codehusky.huskycrates.crate.listeners;

import com.codehusky.huskycrates.HuskyCrates;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLUpdateListener {
    @Listener
    public void virtualKeyUpdateOnJoin(ClientConnectionEvent.Join event, @Root Player player){
        if(HuskyCrates.instance.virtualKeyDB){
            UUID playerUUID = player.getUniqueId();
            Connection virtualKeyConnection = HuskyCrates.registry.getAppropriateVirtualKeyConnection();
            if(virtualKeyConnection == null) {
                HuskyCrates.instance.logger.error("SQL DIRTY PUSH FAILURE - FOR PLAYER");
                return;
            }
            try{
                ResultSet keyBalances = virtualKeyConnection.prepareStatement("SELECT * FROM KEYBALANCES WHERE USERUUID = '" + player.getUniqueId().toString() + "'").executeQuery();
                while(keyBalances.next()){
                    String keyID = keyBalances.getString("keyID");
                    Integer amount = keyBalances.getInt("amount");
                    HuskyCrates.registry.setVirtualKeysNoDirty(playerUUID,keyID, amount);
                }
                virtualKeyConnection.close();
            }catch (SQLException e){
                e.printStackTrace();
                HuskyCrates.instance.logger.error("SQL DIRTY PUSH UPDATE FAILURE");
                try {
                    virtualKeyConnection.close();
                } catch (SQLException ignored){}
            }
        }
    }

}
