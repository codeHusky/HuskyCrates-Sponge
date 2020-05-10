package com.codehusky.huskycrates.crate.virtual;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.virtual.effects.Effect;
import com.codehusky.huskycrates.crate.virtual.effects.elements.Particle;
import com.codehusky.huskycrates.crate.virtual.views.SimpleView;
import com.codehusky.huskycrates.crate.virtual.views.SpinnerView;
import com.codehusky.huskycrates.crate.virtual.views.ViewConfig;
import com.codehusky.huskycrates.exception.*;
import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.element.Element;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;

public class Crate {
    private String id;
    private String name;
    private String previewTextOccurrence;
    private String previewTextRewardCount;

    private Hologram hologram;

    private Effect idleEffect;
    private Effect rejectEffect;
    private Effect winEffect;
    private Effect openEffect;

    private List<Slot> slots;

    private int slotChanceMax = 0;

    private Boolean scrambleSlots;

    private Boolean free;

    private boolean previewable;
    private Boolean previewShowsRewardCount;

    private long cooldownSeconds;

    //TODO unused variable
    private BlockType defaultBlock;

    private Boolean useLocalKey;
    private Key localKey;

    private HashMap<String, Integer> acceptedKeys = new HashMap<>();

    private ViewType viewType;

    private ViewConfig viewConfig;

    enum ViewType{
        SPINNER, // User watches as their desired reward is selected.
        ROULETTE, // User gets to pick when the reward they want is selected.
        INSTANT, // Basically just delivers a reward.
        SIMPLE // Delivers a reward with a slight delay.
    }

    private Messages messages;
    private boolean injection;
    public Crate(ConfigurationNode node){
        if(!node.hasMapChildren()){
            throw new ConfigParseError("Invalid data in crates.conf. Please remove it.",node.getPath());
        }
        slots = new ArrayList<>();
        this.id = node.getKey().toString();
        this.name = node.getNode("name").getString();

        this.useLocalKey = node.getNode("useLocalKey").getBoolean(false);

        this.injection = node.getNode("waitForInjection").getBoolean(false);


        ConfigurationNode aKeyNode = node.getNode("acceptedKeys");
        if(!aKeyNode.isVirtual()) {
            if (aKeyNode.hasListChildren()) {
                for(ConfigurationNode keynode : aKeyNode.getChildrenList()){
                    if(HuskyCrates.registry.isKey(keynode.getString())) {
                        acceptedKeys.put(keynode.getString(), 1);
                    }else{
                        throw new ConfigParseError("Invalid key id: " + keynode.getString(),keynode.getPath());
                    }
                }
            } else if (aKeyNode.hasMapChildren()) {
                for(Object key : aKeyNode.getChildrenMap().keySet()){
                    if(HuskyCrates.registry.isKey(key.toString())) {
                        acceptedKeys.put(key.toString(), aKeyNode.getNode(key).getInt(1));
                    }else{
                        throw new ConfigParseError("Invalid key id: " + key.toString(),aKeyNode.getNode(key).getPath());
                    }
                }
            } else {
                throw new ConfigParseError("Invalid key format specified. Odd.",aKeyNode.getPath());
            }
        }
        this.free = node.getNode("free").getBoolean(false);

        if(this.useLocalKey){
            boolean localKeyLaunchesCrate = node.getNode("localKeyLaunchesCrate").getBoolean(false);
            if(!node.getNode("localKey").isVirtual()){
                this.localKey = new Key("LOCALKEY_" + this.id, new Item(node.getNode("localKey")),localKeyLaunchesCrate);
            }else{
                this.localKey = new Key("LOCALKEY_" + this.id, new Item("&8" + this.name + " Key", ItemTypes.NETHER_STAR, null, 1, null, null, null, null),localKeyLaunchesCrate);
            }
        }else if(aKeyNode.isVirtual() && !this.free){
            throw new ConfigParseError("Non-free crate has no accepted keys!",node.getPath());
        }


        if(node.getNode("slots").isVirtual()){
            if(!this.injection) {
                throw new ConfigParseError("Crates must have associated slots!", node.getNode("slots").getPath());
            }else{
                HuskyCrates.instance.logger.warn("Crate with id of " + this.id + " is waiting for injection.");
            }
        }else{
            for(ConfigurationNode slot : node.getNode("slots").getChildrenList()){
                Slot thisSlot = new Slot(slot,this);
                slotChanceMax += thisSlot.getChance();
                slots.add(thisSlot);
            }
            if(slots.size() == 0){
                throw new ConfigParseError("Crates must have associated slots!", node.getNode("slots").getPath());
            }
        }

        messages = new Messages(node.getNode("messages"),this.id, HuskyCrates.crateMessages);

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
        }catch (Exception e){
            throw new ConfigParseError("Invalid view type!", node.getNode("viewType").getPath());
        }



        this.cooldownSeconds = node.getNode("cooldownSeconds").getLong(0);

        this.scrambleSlots = node.getNode("scrambleSlots").getBoolean(false);

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

        this.previewable = node.getNode("previewable").getBoolean(false);
        this.previewTextOccurrence=node.getNode("previewTextOccurrence").getString("Occurrence: ");
        this.previewTextRewardCount=node.getNode("previewTextRewardCount").getString("Rewards: ");
        this.previewShowsRewardCount = node.getNode("previewShowsRewardCount").getBoolean(true);
    }
    public Crate(String id, String name, Hologram hologram, Effect idleEffect, Effect rejectEffect, Effect winEffect, Effect openEffect, List<Slot> slots, boolean scrambleSlots, boolean free, boolean previewable, boolean previewRewardCount, long cooldownSeconds, boolean useLocalKey, Key localKey, HashMap<String, Integer> acceptedKeys, ViewType viewType, ViewConfig viewConfig,String previewTextOccurrence,String previewTextRewardCount){
        this.id = id;
        this.name = name;
        this.hologram = hologram;
        this.idleEffect = idleEffect;
        this.rejectEffect = rejectEffect;
        this.winEffect = winEffect;
        this.openEffect = openEffect;
        this.slots = slots;
        this.slotChanceMax = 0;
        for(Slot slot : slots){
            slotChanceMax += slot.getChance();
        }
        if(slots.size() == 0){
            throw new ConfigParseError("Crates must have associated slots!", Lists.newArrayList("Injected!!!").toArray());
        }
        this.scrambleSlots = scrambleSlots;
        this.free = free;
        this.previewable = previewable;
        this.previewShowsRewardCount = previewRewardCount;
        this.cooldownSeconds = cooldownSeconds;
        this.useLocalKey = useLocalKey;
        this.localKey = localKey;
        this.acceptedKeys = acceptedKeys;
        this.viewType = viewType;
        this.viewConfig = viewConfig;
        this.previewTextOccurrence=previewTextOccurrence;
        this.previewTextRewardCount=previewTextRewardCount;
    }

    public Crate getScrambledCrate() {
        ArrayList<Slot> newSlots = new ArrayList<>(slots);
        Collections.shuffle(newSlots);
        return new Crate(id,name,hologram,idleEffect,rejectEffect,winEffect,openEffect,newSlots,scrambleSlots,free,previewable,previewShowsRewardCount,cooldownSeconds,useLocalKey,localKey,acceptedKeys,viewType,viewConfig,previewTextOccurrence,previewTextRewardCount);
    }
    public boolean isScrambled() {
        return this.scrambleSlots;
    }
    public boolean isInjectable() {
        return this.injection;
    }

    public void injectSlot(Slot slot) {
        slots.add(slot);
        slotChanceMax += slot.getChance();
    }

    public void postInjectionChecks() {
        if(this.injection){
            if(this.slots.size() == 0){
                throw new InjectionMissedError("Injectable crates with no slots must be injected!");
            }else{
                HuskyCrates.instance.logger.info("Injection successful on " + this.id);
            }
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
        for(Map.Entry<String, Integer> entry : acceptedKeys.entrySet()){
            Key potential = HuskyCrates.registry.getKey(entry.getKey());
            if(potential.testKey(stack)) return true;
        }
        return false;
    }

    public boolean testVirtualKey(UUID playerUUID){
        for(Map.Entry<String, Integer> entry : acceptedKeys.entrySet()){
            if(HuskyCrates.registry.getVirtualKeyBalance(playerUUID,entry.getKey()) >= entry.getValue()){
                return true;
            }
        }
        return useLocalKey && HuskyCrates.registry.getVirtualKeyBalance(playerUUID,getLocalKey().getId()) >= 1;
    }

    public void consumeVirtualKeys(UUID playerUUID){
        if(testVirtualKey(playerUUID)){
            for(Map.Entry<String, Integer> entry : acceptedKeys.entrySet()){
                String keyID = entry.getKey();
                int consumed = entry.getValue();
                if(HuskyCrates.registry.getVirtualKeyBalance(playerUUID,keyID) >= consumed){
                        HuskyCrates.registry.removeVirtualKeys(playerUUID,keyID,consumed);
                    if(Sponge.getServer().getPlayer(playerUUID).isPresent()) {
                        Player player = Sponge.getServer().getPlayer(playerUUID).get();
                        player.sendMessage(
                                messages.getVirtualKeyConsumed(
                                        HuskyCrates.registry.getKey(keyID).getName(),
                                        consumed,
                                        HuskyCrates.registry.getVirtualKeyBalance(playerUUID, keyID)
                                )
                        );
                    }
                    return;
                }
            }
            if(useLocalKey && HuskyCrates.registry.getVirtualKeyBalance(playerUUID,getLocalKey().getId()) >= 1){
                HuskyCrates.registry.removeVirtualKeys(playerUUID,getLocalKey().getId(),1);
                if(Sponge.getServer().getPlayer(playerUUID).isPresent()) {
                    Player player = Sponge.getServer().getPlayer(playerUUID).get();
                    player.sendMessage(
                            messages.getVirtualKeyConsumed(
                                    getLocalKey().getName(),
                                    1,
                                    HuskyCrates.registry.getVirtualKeyBalance(playerUUID, getLocalKey().getId())
                            )
                    );
                }
                return;
            }
            throw new VirtualKeyStarvedError("No virtual key could be found to consume in exchange for a crate use. Report this to a developer.");
        }
    }

    public boolean hasLocalKey(){
        return useLocalKey;
    }

    public Key getLocalKey(){
        return localKey;
    }

    public boolean isPreviewable() {
        return previewable;
    }
    public boolean previewShowsRewards() {return previewShowsRewardCount; }

    public ItemStack getCratePlacementBlock(ItemType itemType, int damage) {
        ItemStack stack = ItemStack.builder()
                .itemType(itemType)
                .add(Keys.DISPLAY_NAME, Text.of(TextSerializers.FORMATTING_CODE.deserialize((this.name != null)?this.name:this.id)," Placement Block"))
                .build();
        return ItemStack.builder()
                .fromContainer(stack.toContainer().set(DataQuery.of("UnsafeData","HCCRATEID"),this.id).set(DataQuery.of("UnsafeDamage"),damage))
                .build();
    }

    public ItemStack getWandItem() {
        ItemStack stack = ItemStack.builder()
                .itemType(ItemTypes.BLAZE_ROD)
                .add(Keys.DISPLAY_NAME, Text.of(TextSerializers.FORMATTING_CODE.deserialize((this.name != null)?this.name:this.id)," Wand"))
                .add(Keys.ITEM_LORE, Arrays.asList(Text.of("Right click to set entity"),Text.of("or crate. You can also remove"),Text.of("crates from entities or crates with this.")))
                .build();
        return ItemStack.builder()
                .fromContainer(stack.toContainer().set(DataQuery.of("UnsafeData","HCCRATEID"),this.id))
                .build();
    }

    public ItemStack getCratePlacementBlock(int damage) {
        return this.getCratePlacementBlock(ItemTypes.CHEST,damage);
    }

    public static String extractCrateID(ItemStack stack){
        try {
            return stack.toContainer().get(DataQuery.of("UnsafeData", "HCCRATEID")).get().toString();
        }catch (Exception e){
            return null;
        }
    }

    public String getName() {
        return (this.name != null)?this.name:this.id;
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

    public HashMap<String, Integer> getAcceptedKeys() {
        return acceptedKeys;
    }

    public Boolean isFree() {
        return free;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public Messages getMessages() {
        return messages;
    }

    public long getCooldownSeconds(Player player){
        Long time = HuskyCrates.registry.getLastUse(this.id, player.getUniqueId());
        if( time == null ){
            return 0L;
        }
        return 1 + (long) Math.ceil(((time + (cooldownSeconds*1000)) - System.currentTimeMillis()) / 1000);
    }

    public boolean isTimedOut(UUID playerUUID) {
        Long time = HuskyCrates.registry.getLastUse(id,playerUUID);
        return time != null && (time + (cooldownSeconds*1000)) - System.currentTimeMillis() >= 0;
    }

    

    public void launchView(Crate ucrate, Player player, Location loc){
        HuskyCrates.registry.updateLastUse(id,player.getUniqueId());

        switch(viewType){
            case SPINNER:
                new SpinnerView(ucrate,player,loc);
                break;
            case INSTANT:
                player.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, player.getPosition(), 1.0);
                this.getSlot(selectSlot()).rewardPlayer(player,loc);
                break;
            case SIMPLE:
                new SimpleView(ucrate,player,loc);
                break;
            default:
                player.sendMessage(Text.of(TextColors.RED,"The view type \"" + viewType.name() + "\" is currently not supported."));
                break;
        }
    }

    public void launchPreview(Player player){
        StateContainer previewContainer = new StateContainer();
        Page.PageBuilder builder = Page.builder();

        builder.setTitle(Text.of(TextSerializers.FORMATTING_CODE.deserialize(getName())));
        builder.setAutoPaging(true);
        for(int j = 0; j  < slots.size(); j++) {
            ItemStack orig = slots.get(j).getDisplayItem().toItemStack();
            List<Text> oldLore = orig.getOrElse(Keys.ITEM_LORE,new ArrayList<>());
            double val = ((double)slots.get(j).getChance()/(double)slotChanceMax)*100;
            BigDecimal occurance = new BigDecimal(val).setScale(2,BigDecimal.ROUND_HALF_UP);
            if (previewShowsRewardCount) {
                oldLore.add(TextSerializers.FORMATTING_CODE.deserialize("&r&7"+previewTextRewardCount + slots.get(j).getRewards().size()));
            }
            oldLore.add(TextSerializers.FORMATTING_CODE.deserialize("&r&7"+previewTextOccurrence + ((val < 0.01)?"< 0.01":occurance.toString()) + "%"));
            builder.addElement(new Element(ItemStack.builder().from(orig).add(Keys.ITEM_LORE,oldLore).build()));
        }
        Page built = builder.build("preview");
        previewContainer.setInitialState(built);

        previewContainer.launchFor(player);
    }

    public static class Messages {
        private String rejectionNeedKey;
        private String rejectionCooldown;
        private String virtualKeyConsumed;
        private String crateID;

        public enum Type {
            RejectionNeedKey,
            RejectionCooldown
        }

        public String getRejectionCooldown() {
            return rejectionCooldown;
        }

        public String getRejectionNeedKey() {
            return rejectionNeedKey;
        }

        public String getVirtualKeyConsumed() {
            return virtualKeyConsumed;
        }

        public Messages(ConfigurationNode node, @Nullable String crateID, @Nullable Messages defaultMessages){
            this.rejectionNeedKey = node.getNode("rejectionNeedKey")
                    .getString((defaultMessages != null)?defaultMessages.getRejectionNeedKey():"&cYou need a {key.0.name} to use this crate.");
            this.rejectionCooldown = node.getNode("rejectionCooldown")
                    .getString((defaultMessages != null)?defaultMessages.getRejectionCooldown():"&aCalm down! &7You need to wait {cooldown.remaining} second{cooldown.remaining.plural} before you can use another {crate.name}&r&7!");
            this.virtualKeyConsumed = node.getNode("virtualKeyConsumed")
                    .getString((defaultMessages != null)?defaultMessages.getVirtualKeyConsumed():"&eYou just used {amount} {key}{amount.plural}&r&e from your key balance. You have {amountRemaining} {key}&r&e{amountRemaining.plural} left.");

            this.crateID = crateID;
        }

        public Messages(ConfigurationNode node,@Nullable String crateID){
            this(node, crateID, null);
        }

        public Messages(String rnk, String rc, String crateID){
            this.rejectionCooldown = rc;
            this.rejectionNeedKey = rnk;
            this.crateID = crateID;
        }

        public Messages clone() {
            return new Messages(this.rejectionNeedKey,this.rejectionCooldown,this.crateID);
        }

        public void setCrateID(String crateID) {
            this.crateID = crateID;
        }

        public Text format(Type messageType, Player player){
            if(!HuskyCrates.registry.isCrate(crateID)){
                throw new NoMessageContextError("Invalid crate id: " + crateID);
            }
            return format(messageType, HuskyCrates.registry.getCrate(crateID), player, null);
        }

        public Text getVirtualKeyConsumed(String keyName, Integer amountConsumed, Integer amountRemaining){
            return TextSerializers.FORMATTING_CODE.deserialize(
                    this.virtualKeyConsumed
                            .replace("{amount}",amountConsumed.toString())
                            .replace("{amount.plural}",(amountConsumed != 1)?"s":"")
                            .replace("{amountRemaining}",amountRemaining.toString())
                            .replace("{amountRemaining.plural}",(amountRemaining != 1)?"s":"")
                            .replace("{key}",keyName));
        }

        public Text format(Type messageType, Crate crate, Player player, Slot slot){
            String newMessage;
            switch(messageType){
                case RejectionNeedKey:
                    newMessage = rejectionNeedKey;
                    break;
                case RejectionCooldown:
                    newMessage = rejectionCooldown;
                    break;
                default:
                    throw new InvalidMessageTypeError("Invalid message type used!");
            }

            newMessage = newMessage
                    .replace("{crate.name}", crate.getName())
                    .replace("{crate.id}", crate.getId());

            ArrayList<Key> keys = new ArrayList<>();
            if(crate.hasLocalKey()){
                keys.add(crate.getLocalKey());
            }
            for(String keyID : crate.getAcceptedKeys().keySet()) {
                keys.add(HuskyCrates.registry.getKey(keyID));
            }

            int num = 0;
            for(Key key : keys) {
                Integer amountRequired = (crate.hasLocalKey() && num == 0)?1:crate.getAcceptedKeys().get(key.getId());
                newMessage = newMessage
                        .replace("{key." + num + ".id}",key.getId())
                        .replace("{key." + num + ".name}",key.getName())
                        .replace("{key." + num + ".amountRequired}",amountRequired.toString())
                        .replace("{key." + num + ".amountRequired.plural}",(amountRequired != 0)?"s":"")
                        .replace("{key." + key.getId() + ".id}",key.getId())
                        .replace("{key." + key.getId() + ".name}",key.getName())
                        .replace("{key." + key.getId() + ".amountRequired}",amountRequired.toString())
                        .replace("{key." + key.getId() + ".amountRequired.plural}",(amountRequired != 0)?"s":"");
                num++;
            }

            newMessage = newMessage
                    .replace("{cooldown.remaining}","" + crate.getCooldownSeconds(player))
                    .replace("{cooldown.remaining.plural}",(crate.getCooldownSeconds(player) != 1)?"s":"")
                    .replace("{cooldown.total}", "" + crate.getCooldownSeconds())
                    .replace("{cooldown.total.plural}",(crate.getCooldownSeconds() != 1)?"s":"");



            return TextSerializers.FORMATTING_CODE.deserialize(newMessage);
        }
    }
}
