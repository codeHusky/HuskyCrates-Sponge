package com.codehusky.huskycrates.crate.virtual;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.effects.Effect;
import com.codehusky.huskycrates.crate.virtual.effects.elements.Particle;
import com.codehusky.huskycrates.crate.virtual.views.SpinnerView;
import com.codehusky.huskycrates.crate.virtual.views.ViewConfig;
import com.codehusky.huskycrates.exception.ConfigParseError;
import com.codehusky.huskycrates.exception.SlotSelectionError;
import com.flowpowered.math.vector.Vector3d;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;

public class Crate {
    private String id;
    private String name;

    private Hologram hologram;

    private Effect idleEffect;
    private Effect rejectEffect;
    private Effect winEffect;
    private Effect openEffect;

    private List<Slot> slots;

    private int slotChanceMax = 0;

    private Boolean scrambleSlots;

    private Boolean free;

    private long cooldownSeconds;

    private BlockType defaultBlock;

    private Boolean useLocalKey;
    private Key localKey;

    private HashMap<String, Integer> acceptedKeys = new HashMap<>();

    private ViewType viewType;

    private ViewConfig viewConfig;

    enum ViewType{
        SPINNER
    }

    public Crate(ConfigurationNode node){
        slots = new ArrayList<>();
        this.id = node.getKey().toString();
        this.name = node.getNode("name").getString();

        if(node.getNode("slots").isVirtual()){
            throw new ConfigParseError("Crates must have associated slots!", node.getNode("slots").getPath());
        }else{
            for(ConfigurationNode slot : node.getNode("slots").getChildrenList()){
                Slot thisSlot = new Slot(slot);
                slotChanceMax += thisSlot.getChance();
                slots.add(thisSlot);
            }
            if(slots.size() == 0){
                throw new ConfigParseError("Crates must have associated slots!", node.getNode("slots").getPath());
            }
        }

        try {
            this.viewType = ViewType.valueOf(node.getNode("viewType").getString().toUpperCase());
            switch(this.viewType){
                case SPINNER:
                    viewConfig = new SpinnerView.Config(node.getNode("viewConfig"));
                    break;
                default:
                    viewConfig = new ViewConfig(node.getNode("viewConfig"));
                    break;
            }
        }catch (IllegalArgumentException e){
            throw new ConfigParseError("Invalid view type!", node.getNode("viewType").getPath());
        }

        this.free = node.getNode("free").getBoolean(false);

        this.cooldownSeconds = node.getNode("cooldownSeconds").getLong(0);

        this.scrambleSlots = node.getNode("scrambleSlots").getBoolean(false);

        this.useLocalKey = node.getNode("useLocalKey").getBoolean(false);

        ConfigurationNode aKeyNode = node.getNode("acceptedKeys");
        if(!aKeyNode.isVirtual()) {
            if (aKeyNode.hasListChildren()) {
                for(ConfigurationNode keynode : aKeyNode.getChildrenList()){
                    acceptedKeys.put(keynode.getString(),1);
                }
            } else if (aKeyNode.hasMapChildren()) {
                for(Object key : aKeyNode.getChildrenMap().keySet()){
                    acceptedKeys.put(key.toString(),aKeyNode.getNode(key).getInt(1));
                }
            } else {
                throw new ConfigParseError("Invalid key format specified. Odd.",aKeyNode.getPath());
            }
        }

        if(this.useLocalKey){
            if(!node.getNode("localKey").isVirtual()){
                this.localKey = new Key("LOCALKEY_" + this.id, new Item(node.getNode("localKey")));
            }else{
                this.localKey = new Key("LOCALKEY_" + this.id, new Item("&8" + this.name + " Key", ItemTypes.NETHER_STAR, null, 1, null, null, null, null));
            }
        }else if(aKeyNode.isVirtual()){
            throw new ConfigParseError("Crate has no accepted keys!",node.getPath());
        }

        ConfigurationNode eNode = node.getNode("effects");
        if(!eNode.getNode("idle").isVirtual()){
            idleEffect = new Effect(eNode.getNode("idle"));
            if(idleEffect.isDisabled()) idleEffect = null;
        }
        if(!eNode.getNode("reject").isVirtual()){
            rejectEffect = new Effect(eNode.getNode("reject"));
            if(rejectEffect.isDisabled()) rejectEffect = null;
        }else{
            /*
            player.spawnParticles(,
                event.getTargetBlock().getPosition().clone().toDouble().add(0.5,1.3,0.5));

             */
            rejectEffect = new Effect(false,1,false,false,true, new ArrayList<>(Collections.singletonList(new Particle(ParticleEffect.builder().type(ParticleTypes.SMOKE).quantity(20).offset(new Vector3d(0.1, 0.3, 0.1)).build(), new Vector3d(0.5, 1.3, 0.5)))));
        }
        if(!eNode.getNode("win").isVirtual()){
            winEffect = new Effect(eNode.getNode("win"));
            if(winEffect.isDisabled()) winEffect = null;
        }
        if(!eNode.getNode("open").isVirtual()){
            openEffect = new Effect(eNode.getNode("open"));
            if(openEffect.isDisabled()) openEffect = null;
        }

        if(!node.getNode("hologram").isVirtual()){
            hologram = new Hologram(node.getNode("hologram"));
        }
    }

    public String getId() {
        return id;
    }

    public int selectSlot() {
        int chanceCuml = 0;
        int selection = new Random().nextInt(slotChanceMax+1);
        for(int i = 0; i < slots.size(); i++){
            chanceCuml += slots.get(i).getChance();
            if(selection <= chanceCuml){
                return i;
            }
        }
        throw new SlotSelectionError("Slot could not be selected for crate \"" + this.id + "\". chanceCuml=" + chanceCuml + "; selection=" + selection);
    }

    public boolean testKey(ItemStack stack){
        if(free) return true;
        if(useLocalKey){
            if(localKey.testKey(stack)) return true;
        }
        for(String thisid : acceptedKeys.keySet()){
            Key potential = HuskyCrates.registry.getKey(thisid);
            if(potential.testKey(stack)) return true;
        }
        return false;
    }

    public boolean hasLocalKey(){
        return useLocalKey;
    }

    public Key getLocalKey(){
        return localKey;
    }



    public ItemStack getCratePlacementBlock() {
        ItemStack stack = ItemStack.builder()
                .itemType(ItemTypes.CHEST)
                .add(Keys.DISPLAY_NAME, Text.of(TextSerializers.FORMATTING_CODE.deserialize(name)," crate placer"))
                .build();
        return ItemStack.builder()
                .fromContainer(stack.toContainer().set(DataQuery.of("UnsafeData","HCCRATEID"),this.id))
                .build();
    }

    public static String extractCrateID(ItemStack stack){
        try {
            return stack.toContainer().get(DataQuery.of("UnsafeData", "HCCRATEID")).get().toString();
        }catch (Exception e){
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public ViewType getViewType() {
        return viewType;
    }

    public ViewConfig getViewConfig() {
        return viewConfig;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public int getSlotCount() {
        return slots.size();
    }

    public Slot getSlot(int slot){
        return slots.get(slot);
    }

    public Effect getIdleEffect() {
        return idleEffect;
    }

    public Effect getOpenEffect() {
        return openEffect;
    }

    public Effect getRejectEffect() {
        return rejectEffect;
    }

    public Effect getWinEffect() {
        return winEffect;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Boolean isFree() {
        return free;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public boolean isTimedOut(UUID playerUUID) {
        Long time = HuskyCrates.registry.getLastUse(id,playerUUID);
        return time != null && (time + (cooldownSeconds*1000)) - System.currentTimeMillis() >= 0;
    }

    public void launchView(PhysicalCrate pcrate, Player player){
        HuskyCrates.registry.updateLastUse(id,player.getUniqueId());
        player.playSound(SoundTypes.BLOCK_WOOD_BUTTON_CLICK_OFF, player.getPosition(), 1.0);
        switch(viewType){
            case SPINNER:
                new SpinnerView(pcrate,player);
                break;
            default:
                break;
        }
    }
}
