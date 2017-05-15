# New item format for items
### This is for 0.10.0 and higher only.

```
crate {
  items = [
    {
      ##Display, but by default also reward item.
      id="minecraft:dirt"
      count=1
      formatversion=1
      huskydata{
        weight=10 #higher weight = higher chance
        
        ##for commands...
        reward {
          type="cmd"
          command="/give %P diamond 500"
          overrideRewardName="Huge Load of Diamonds!"
          treatAsSingle=true
        }
        
        ##for items...
        reward{
          type="item"
          ##optional customization
          overrideCount=16
          treatAsSingle=true 
          overrideRewardName="Medium Pack of Diamonds" #e.g. the player is rewarded
          
          ##if you want a different reward item than your display item...
          overrideItem {
            id="minecraft:diamond"
            count=16
            name="HuskyCrate Diamonds"
            lore=[
              "Legend has it, these diamonds have been passed down from generation to generation."
            ]
            ##overrideItems are parsed like standard items, so anything you can do with a display item you can do with overrideItems.
            ##The only difference is that reward items don't have huskydata. That's parsed seperate.
          }
        }
      }
      #commented out values aren't implemented if i recall correctly
      ## Below are optional values you would use if you wanted to have some fun. ##
      #damage=0
      #unbreakable=false
      name="&5woah so cool :)"
      lore = [
        "&9Blue!!!"
      ]
      enchants {
        sharpness=255 #sharpness lvl 255
      }
      #attribute modifiers not implemented, not sure what they are
      #skullowner="lokio27"
      #life=6000 # 5 minutes before despawn
      #hideflags=1-63
      #candestroy=[
      #  "minecraft:stone"
      #]
      #pickupdelay=0 #instant pickup
      #generation=0 #original
    },
    {...}
  ]
}
```
