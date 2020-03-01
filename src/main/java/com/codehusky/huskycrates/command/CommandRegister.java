package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandRegister {
    public static void register(HuskyCrates plugin) {
        CommandSpec mainCommand = CommandSpec.builder()
                .child(CommandSpec.builder()
                        .executor(new BlockCommand())
                        .arguments(new CrateArgument(Text.of("crate")),
                                GenericArguments.optionalWeak(GenericArguments.catalogedElement(Text.of("block"),BlockType.class)),
                                GenericArguments.optionalWeak(GenericArguments.player(Text.of("player"))),
                                GenericArguments.optional(GenericArguments.integer(Text.of("amount"))),
                                GenericArguments.optional(GenericArguments.integer(Text.of("damage"))))
                        .permission("huskycrates.block.base")
                        .build(),"b","blk","block","chest")
                .child(CommandSpec.builder()
                        .executor(new WandCommand())
                        .arguments(new CrateArgument(Text.of("crate")),
                                GenericArguments.optionalWeak(GenericArguments.player(Text.of("player"))))
                        .permission("huskycrates.wand.base")
                        .build(),"w","wand")
                .child(CommandSpec.builder()
                        .executor(new KeyCommand())
                        .arguments(GenericArguments.optionalWeak(GenericArguments.literal(Text.of("virtual"),"v")),
                                GenericArguments.firstParsing(new CrateArgument(Text.of("crate")), new KeyArgument(Text.of("key"))),
                                GenericArguments.optional(GenericArguments.firstParsing(GenericArguments.literal(Text.of("all"),"@a"), GenericArguments.player(Text.of("player")))),
                                GenericArguments.optionalWeak(GenericArguments.integer(Text.of("amount"))))
                        .permission("huskycrates.key.base")
                        .build(),"k","key")
                .child(CommandSpec.builder()
                        .executor(new OpenCommand())
                        .arguments(GenericArguments.optionalWeak(GenericArguments.literal(Text.of("keyless"),"nokey")),
                                new CrateArgument(Text.of("crate")),
                                GenericArguments.optional(GenericArguments.firstParsing(GenericArguments.literal(Text.of("all"),"@a"), GenericArguments.player(Text.of("player")))))
                        .permission("huskycrates.open.base")
                        .build(),"o","open","use")
                .child(CommandSpec.builder()
                        .executor(new BalanceCommand())
                        .arguments(GenericArguments.optionalWeak(GenericArguments.user(Text.of("player"))),
                                GenericArguments.optionalWeak(GenericArguments.uuid(Text.of("uuid"))),
                                GenericArguments.optionalWeak(GenericArguments.string(Text.of("username"))))
                        .permission("huskycrates.bal.base")
                        .build(),"bal","balance")
                .child(CommandSpec.builder()
                        .executor(new ReloadCommand())
                        .permission("huskycrates.reload")
                        .build(),"rl","r","reload")
                .child(CommandSpec.builder()
                        .executor(new ItemNBTCommand())
                        .permission("huskycrates.admin")
                        .build(),"itemnbt","nbt","in")
                .child(CommandSpec.builder()
                        .executor(new ItemGenerateCommand())
                        .permission("huskycrates.admin")
                        .build(),"genitem","generateitem","gitem")
                .child(CommandSpec.builder()
                        .executor(new InventoryGenerateCommand())
                        .permission("huskycrates.admin")
                        .build(),"geninvent","generateinventory","ginv")
                .child(CommandSpec.builder()
                        .executor(new CrateGenerateCommand())
                        .permission("huskycrates.admin")
                        .arguments(GenericArguments.string(Text.of("name")),
                                GenericArguments.choices(Text.of("type"), ImmutableMap.<String, String> builder().put("item", "item").put("inventory", "inventory").build()))
                        .build(),"gencrate","generatecrate","gcrate")
                .child(CommandSpec.builder()
                        .executor(new MainCommand())
                        .build(),"h","help")
                .executor(new MainCommand())
                .description(Text.of("Main HuskyCrates Command."))
                .extendedDescription(Text.of("For more information on the commands you can use with HuskyCrates, please visit the HuskyCrates documentation.\nYou can find a link to the documentation within /hc help."))
                .build();
        if(Sponge.getCommandManager().get("crate").isPresent() && HuskyCrates.FORCE_CRATE_CMD) {
            Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get("crate").get());
            Sponge.getCommandManager().register(plugin,mainCommand,"crate");
        }
        Sponge.getCommandManager().register(plugin,mainCommand,"hc","husky","huskycrates");
    }

    public static class CrateArgument extends CommandElement {

        public CrateArgument(@Nullable Text key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            if(HuskyCrates.registry.isCrate(args.peek())){
                return HuskyCrates.registry.getCrate(args.next());
            }
            throw args.createError(Text.of("\"" +args.next() + "\" is not a valid crate."));
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            try {
                if(HuskyCrates.registry.isCrate(args.peek())){
                    return Collections.singletonList(args.next());
                }else{
                    List<String> poss = new ArrayList<>();
                    for(String crateID : HuskyCrates.registry.getCrates().keySet()){
                        if(crateID.indexOf(args.peek()) == 0){
                            poss.add(crateID);
                        }
                    }
                    args.next();
                    return poss;
                }
            } catch (ArgumentParseException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }
    }

    public static class KeyArgument extends CommandElement {

        public KeyArgument(@Nullable Text key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            if(HuskyCrates.registry.isKey(args.peek())){
                return HuskyCrates.registry.getKey(args.next());
            }
            throw args.createError(Text.of("\"" +args.next() + "\" is not a valid key."));
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            try {
                if(HuskyCrates.registry.isKey(args.peek())){
                    return Collections.singletonList(args.next());
                }else{
                    List<String> poss = new ArrayList<>();
                    for(String crateID : HuskyCrates.registry.getKeys().keySet()){
                        if(crateID.indexOf(args.peek()) == 0){
                            poss.add(crateID);
                        }
                    }
                    args.next();
                    return poss;
                }
            } catch (ArgumentParseException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }
    }
}
