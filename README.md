# HuskyCrates
A simple, straightforward crate system for Sponge!

# Forum Topic
[Go here.](https://forums.spongepowered.org/t/huskycrates-cratesreloaded-but-free-or-something/16433)

# Config
HuskyCrates might not create a config automatically for itself, so make sure to make one if it doesn't.

**config > huskycrates > huskycrates.conf**

To configure crates, here's a basic crate to get you started.

```
crates {
    commandcrate { # This line represents the crate id.
        items=[
            {
                amount=1 # The amount of an item.
                chance=50 # The chance, out of 100, that this item will be drawn.
                command="give %p minecraft:diamond 10" # A command to run on picking this item.
                id="minecraft:diamond" # The id of the item we're using
                name="Diamond Box" # A name
            },
            {
                amount=24
                # Chances are assumed for items that don't have it.
                id="minecraft:dirt"
                lore="Literally dirt. :)" # Lore.
                name=Dirt 
                #Enchanted dirt coming soon.
            }
            #Make sure your chances don't add past 100 or you'll get an error!
        ]
        name="§3Command Crate" # Make sure this looks good everywhere :)
        type=Spinner # Types will be added in the future, but keeping this as a Spinner will keep your config future proof.
    }
    #Keys will also be configurable in the future, so keep your eye out.
}
```
Please note that if you want a chance maximum other than 100 right now, you cannot have any assumed chances. Make sure your numbers work out in the end.

# Commands
- `/crate` - does nothing
- `/crate <crate id>` - Gives you a placeable crate block. Will look weird but trust me, it's the right thing.
- `/crate <crate id> key [player]` - Give you, or someone you choose, a crate key.
