package com.codehusky.huskycrates.crate.physical;

import com.codehusky.huskycrates.crate.virtual.Hologram;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.List;

public class HologramInstance {
    private Hologram hologram;
    private Location<World> location;
    private boolean isEntity;

    public HologramInstance(Hologram hologram, Location<World> location, boolean isEntity){
        this.hologram = hologram;
        this.location = location;
        this.isEntity = isEntity;


        init();
    }
    public void cleanup() {
        cleanup(location);
    }

    public static void cleanup(Location<World> location){
        //double maxYOffset = Math.max(hologram.getYOffset(),hologram.getYOffset());
        if(location == null) return;
        Collection<Entity> potential = location.getExtent().getNearbyEntities(location.getPosition().clone().add(0.5,0,0.5).add(0,0.4,0),0.8);
        for(int i = 1; i < 2; i++){
            potential.addAll(location.getExtent().getNearbyEntities(location.getPosition().clone().add(0.5,0,0.5).add(0,0.4+(0.8*i),0),0.8));
        }
        for(Entity e : potential){
            if(e instanceof ArmorStand) {
                e.remove();
            }
        }
    }

    public void init() {
        cleanup();

        int linenum = 0;
        for(String line : hologram.getLines()){
            Text textline = TextSerializers.FORMATTING_CODE.deserialize(line);
            ArmorStand as = (ArmorStand) location.getExtent().createEntity(EntityTypes.ARMOR_STAND,
                        location.getPosition()
                            .clone()
                            .add(0.5,1,0.5)
                            .add(0,hologram.getEntityYOffset() - (linenum * 0.23),0));

            as.offer(Keys.HAS_GRAVITY,false);
            as.offer(Keys.ARMOR_STAND_IS_SMALL,true);
            as.offer(Keys.CUSTOM_NAME_VISIBLE,true);
            as.offer(Keys.INVULNERABLE,true);
            as.offer(Keys.INVISIBLE,true);
            as.offer(Keys.INVULNERABILITY_TICKS,-1);
            as.offer(Keys.DISPLAY_NAME,textline);
            as.offer(Keys.ARMOR_STAND_MARKER,true);
            as.offer(Keys.ARMOR_STAND_HAS_ARMS,false);

            location.getExtent().spawnEntity(as);
            linenum++;
        }
    }

    public Location<World> getLocation() {
        return location;
    }

    public Hologram getHologram() {
        return hologram;
    }
}
