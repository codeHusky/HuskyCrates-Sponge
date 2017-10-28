package com.codehusky.huskycrates.commands;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.commands.elements.CrateElement;
import com.codehusky.huskycrates.commands.elements.OperationElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class HuskyCommandManager {

    private HuskyCrates huskyCrates = HuskyCrates.instance;


    public CommandSpec getHuskySpec() {
        return huskySpec;
    }

    public CommandSpec getCrateSpec() {
        return crateSpec;
    }

    CommandSpec key = CommandSpec.builder()
            .description(Text.of("Get a key for a specified crate."))
            .arguments(
                    new CrateElement(Text.of("type")),
                    GenericArguments.playerOrSource(Text.of("player")),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .permission("huskycrates.key")
            .executor(new Key())
            .build();

    CommandSpec keyAll = CommandSpec.builder()
            .description(Text.of("Give everyone a specified amount of keys for a crate."))
            .arguments(
                    new CrateElement(Text.of("type")),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .permission("huskycrates.keyAll")
            .executor(new KeyAll())
            .build();

    CommandSpec vKey = CommandSpec.builder()
            .description(Text.of("Credits a user an amount of virtual keys for a specified crate."))
            .arguments(
                    new OperationElement(Text.of("operation")),
                    new CrateElement(Text.of("type")),
                    GenericArguments.userOrSource(Text.of("player")),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .permission("huskycrates.vKey")
            .executor(new VirtualKey())
            .build();

    CommandSpec vKeyAll = CommandSpec.builder()
            .description(Text.of("Credits everyone a specified amount of virtual keys for a crate."))
            .arguments(
                    new CrateElement(Text.of("type")),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .permission("huskycrates.vKeyAll")
            .executor(new VirtualKeyAll())
            .build();

    CommandSpec wand = CommandSpec.builder()
            .description(Text.of("Give Runningelf an entity wand for crates."))
            .arguments(
                    new CrateElement(Text.of("type"))
            )
            .permission("huskycrates.wand")
            .executor(new Wand())
            .build();

    CommandSpec chest = CommandSpec.builder()
            .description(Text.of("Get the placeable crate item."))
            .permission("huskycrates.chest")
            .arguments(
                    new CrateElement(Text.of("type")),
                    GenericArguments.playerOrSource(Text.of("player")),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            ).executor(new Chest())
            .build();

    CommandSpec keyBal = CommandSpec.builder()
            .description(Text.of("Check your (or another user's) virtual key balance."))
            .arguments(
                    GenericArguments.userOrSource(Text.of("player"))
            )
            .permission("huskycrates.keybal.self")
            .executor(new KeyBal())
            .build();

    CommandSpec deposit = CommandSpec.builder()
            .description(Text.of("Transfer the held physical key(s) into your virtual key balance."))
            .permission("huskycrates.depositkey")
            .executor(new DepositKey())
            .build();

    CommandSpec withdraw = CommandSpec.builder()
            .description(Text.of("Convert an amount of virtual key(s) into a physical key(s)."))
            .permission("huskycrates.withdrawkey")
            .arguments(
                    new CrateElement(Text.of("type")),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .executor(new WithdrawKey())
            .build();

    CommandSpec crateSpec = CommandSpec.builder()
            .description(Text.of("Main crates command"))
            .child(key, "key")
            .child(chest, "chest")
            .child(keyAll, "keyAll")
            .child(vKey, "vkey", "virtualkey")
            .child(vKeyAll, "vkeyall", "virtualkeyall")
            .child(keyBal, "bal", "keybal")
            .child(wand, "wand")
            .child(deposit, "deposit", "depositkey", "ptov")
            .child(withdraw, "withdraw", "withdrawkey", "vtop")
            .arguments(GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of(""))))
            .executor(new Crate(huskyCrates))
            .build();
    CommandSpec huskySpec = CommandSpec.builder()
            .executor(new Husky(huskyCrates))
            .build();

}
