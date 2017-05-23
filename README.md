# HuskyCrates
A simple, straightforward crate system for Sponge!

# Forum Topic
[Go here.](https://forums.spongepowered.org/t/huskycrates-cratesreloaded-but-free-or-something/16433)

# Config
HuskyCrates might not create a config automatically for itself, so make sure to make one if it doesn't.

**config > huskycrates > huskycrates.conf**

To configure crates, check the reference in `NEW CRATE FORMAT.md`.


Please note that if you want a chance maximum other than 100 right now, you cannot have any assumed chances. Make sure your numbers work out in the end.

# Commands
- `/crate <crate id>` - Gives you a placeable crate block.
- `/crate <crate id>  key [player]` - Give the player indicated in the command a crate key.
Crate IDs are the values you put first inside of the crates{} in the config. So like, command in the example would be the crate id.
- **Commands To Be Implemented**
 - `/crate chest <crate id> [quantity]` - Gives you a placeable crate block. Will look weird but trust me, it's the right thing.
 - `/crate key <crate id> [player] [quantity]` - Give you, or someone you choose, a crate key.
Once implemented the old command form will still be functional.

# Permissions
- `huskycrates.tester` - Allows a user to override the key removal in the inventory.
- `huskycrates` - Gives access to the `/crate` command.
- **Permissions To Be Implemented**
  - `huskycrates.<crate id>.use` - Allows a user to use a certain crate
  - `huskycrates.<crate id>.create` - Allows a user to get a certain crate's item
  - `huskycrates.<crate id>.key` - Allows a user to spawn a certain key for themselves
