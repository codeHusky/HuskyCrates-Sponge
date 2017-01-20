package pw.codehusky.huskycrates.crate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import pw.codehusky.huskycrates.HuskyCrates;

import java.util.UUID;

/**
 * Created by lokio on 1/2/2017.
 */
@SuppressWarnings("deprecation")
public class PhysicalCrate {
    private Location<World> location;
    private String crateId;
    private ArmorStand as;
    private HuskyCrates huskyCrates;
    public PhysicalCrate(Location<World> crateLocation, String crateId, HuskyCrates huskyCrates){
        this.location = crateLocation;
        this.crateId = crateId;
        this.huskyCrates = huskyCrates;
    }
    public void initParticles() {
        Extent extent = location.getExtent();
        as = (ArmorStand)  extent.createEntity(EntityTypes.ARMOR_STAND,location.getPosition());
        as.setCreator(UUID.fromString(huskyCrates.armorStandIdentifier));
        as.offer(Keys.HAS_GRAVITY,false);
        as.offer(Keys.INVISIBLE,true);
        as.offer(Keys.ARMOR_STAND_MARKER,true);
        as.offer(Keys.CUSTOM_NAME_VISIBLE,true);
        String name = huskyCrates.crateUtilities.getVirtualCrate(crateId).displayName;
        as.offer(Keys.DISPLAY_NAME, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(name));
        as.setLocation(location.copy().add(0.5,1,0.5));
        extent.spawnEntity(as,huskyCrates.genericCause);
    }
    public void runParticles() {
        try {
            double time = Sponge.getServer().getRunningTimeTicks() * 0.25;
            double size = 0.8;

            double x = Math.sin(time) * size;
            double y = Math.sin(time * 2) * 0.2 - 0.45;
            double z = Math.cos(time) * size;
            as.getWorld().spawnParticles(
                    ParticleEffect.builder()
                            .type(ParticleTypes.REDSTONE_DUST)
                            .option(ParticleOptions.COLOR, Color.ofRgb(255, 139, 41))
                            .build(),
                    as.getLocation()
                            .getPosition()
                            .add(x, y, z));

            x = Math.cos(time + 10) * size;
            y = Math.sin(time * 2 + 10) * 0.2 - 0.55;
            z = Math.sin(time + 10) * size;
            as.getWorld().spawnParticles(
                    ParticleEffect.builder()
                            .type(ParticleTypes.REDSTONE_DUST)
                            .option(ParticleOptions.COLOR, Color.ofRgb(49, 49, 49))
                            .build(),
                    as.getLocation()
                            .getPosition()
                            .add(x, y, z));
        }catch (Exception e){

        }
    }
}
