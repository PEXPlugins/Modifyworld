In porting to Sponge, the goals for the plugin are to match Bukkit functionality where possible, as well as take into account user requests made

# Sponge-specific items

- How to incorporate data API? (block metadata values) -- check with all possible combinations?

# Requests

- Restrict anvil functionality (only enchant, not rename) #95
- Don't drop restricted items (a dropper)
- Mod compatibility -- don't restrict mod compatibility where possible?
- Armor stands?
- Taming horses?
-


# Other features 

- Port over configuration
- Customizable messages (keyed by permission)

# Entity name formatting

- Complex? get parent
- Item? get permission for block
- Player? `player.<name>`
- Tameable? `animal.<name>[.<owner>]`

**Categories**: Player (Player.class), Item (Item.class), Animal (Animals.class, Squid.class), Monster (Monster.class, Slime.class, EnderDragon.class, Ghast.class), NPC (NPC.class), Projectile (Projectile.class)

# Permissions

**Default message**: Sorry, you don't have enough permissions
**Message format**: `&f[&2Modifyworld&f]&4 %s`

Check               | Permission (omitting `modifyworld.`)     | Message                       | Commentary
------------------- | ---------------------------------------- | ----------------------------- | ----------
Break block/hanging | `blocks.destroy.<material>`              | `&a$1&4 is too tough for you`
Place block/hanging | `blocks.place.<material>`                | `This is the wrong place for &a$1`
Interact block      | `blocks.interact.`                       | `You are too jelly for &2$1`
Player deal damage  | `damage.deal.<entity>`                   | `Your level is too low for &5$1`
Player take damage  | `damage.take.<entity>`                   | (none)
Player enviro dmg   | `damage.take.<cause>`                    | (none)
Player tame entity  | `tame.<entity>`                          | `This &a$1&4 is too ferocious`
Ent. target player  | `mobtarget.<targeter>`                   | (none)
Sneak               | `sneak`                                  | (none) | Split out into on/off?
Sprint              | `sprint`                                 | (none)
Login/Whitelist     | `login`                                  |  `You are not allowed to join this server. Goodbye!` (Log message as well)
Enter bed           | `usebeds`                                | `You can't sleep yet, there are monsters nearby`
Empty bucket        | `bucket.empty.<bucket-type>`             | `You suddenly realized you still need &a$1`
Fill bucket         | `bucket.fill.<bucket-type>`              | `This bucket is holey`
Block PM (/tell)    | `modifyworld.chat.private`               | `Listener is deaf`
Chat                | `chat`                                   | `Your mouth is too dry`
Pick up item        | `items.pickup.<item>`                    | (none)
Drop item           | `items.drop.<item>`                      | `This is indecent to scatter &a$1&4 around`
Hold item           | `items.hold.<item>`                      | `Beware, &a$1&4 is cursed!`
Transfer items      | `items.<put|take>.<item>.of.<inventory>` | (none)
Use item on entity  | `items.use.<item>.on.entity.<target>`    | `Stop, &a$1&4 won't fit into &a$3`
Interact (similar)  | `interact.<target>`                      | (none)
Right click/throw   | `items.throw.<item>`                     | (none)  | Potion (splash only), egg, snowball, expbottle
Right click/spawn   | `spawn.<entity-type>`                    | (none)
Click on block      | `items.use.<item>.on.block.<clicked>`    | (none)
Click on block      | `blocks.interact.<clicked>`              | `You are too jelly for &2$1`
Enchant item        | `items.enchant.<item>`                   |
Craft item          | `items.craft.<result>`                   | `Sorry. but &a$1&4 is too complicated`
Change food level   | `digestion`                              | (none) | Possible split ount into eating/deterioriation
Dmg/destroy vehicle | `vehicle.destry.<vehicle>`               | `This &a$1&4 is legal property of &bUnited States of America`
Enter vehicle       | `vehicle.enter.<vehicle>`                | Boat: `You are too heavy for this &a$1` Minecart: `Sorry, but &a$1&4 is too small`
Vehice collide w/ply| `vehicle.collide.<vehicle>`              | 





-
