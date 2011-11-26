#PersonalChest - Bukkit Plugin for Player Bound Chests

##Example:
* Admin creates a chest and places 2 dirt inside
* Admin registers the chest as a personal chest with "/pchest create" (while standing in front of the chest).
* Player 1 walks to the chest, opens it and removes 1 dirt. There remains 1 dirt in the chest for Player 1.
* Player 2 walks to the chest, opens it, sees the original 2 dirt and removes them both.
* Player 1 would still see the 1 dirt they left.
* Player 3 would see the original 2 dirt.

##Features:
* Treasure Chest like capabilities.
* One chest different inventories for each player.
* Entire world auto create PersonalChests.
* Region Based PersonalChest (WorldGuard and/or Residence).
* Large chest support.
* Spout server side only!
* Anti creeper and grief support.

##Commands:
* `/pchest create`
Create a PersonalChest from the chest you are standing in front of.
* `/pchest remove`
Unregister the PersonalChest you are standing in front of.
* `/pchest info`
Determine if the chest you are in front of is registered as a PersonalChest

##Permissions:
* `pchest.edit`
Allows the player to create or remove a PersonalChest (defaults to op)
* `pchest.open`
Allows the player to open a PersonalChest (defaults to true)

##Config:
    Debug: false
    Worlds: world1,world2,world3
    ResidenceRegions: world4.region1, world4.region2
    WorldGuardRegions: world1.region1

**Debug**: When enabled logs additional debug information
**Worlds**: Worlds where all chests will default to being a PersonalChest when opened.
This is useful when your world is a custom world with chests and inventory in it.
**Regions**: Invert the 'default' PersonalChest status for this region. If the region is on a 'normal world' then chests
will default to being a PersoanlChest when opened. If the world is listed as a 'PersonalChest world' then the chests in
this region will default to being 'normal' chests.

Description for above 'sample' configuration:

* Debug is not enabled
* world1, world2 and worlds3 will all automatically register personal chests.
* world4 does not automatically register chests.
* region1 of world1 cancels the automatic registration.
* region1 and region2 of world4 enable automatic registration.

##Links:
Source is on [GitHub](https://github.com/rodeyseijkens/PersonalChest)

Plugin is available on [BukkitDev](http://dev.bukkit.org/server-mods/personalchest/)

Original postings on [Bukkit Forums](http://forums.bukkit.org/threads/mech-fun-personalchests-v1-0-4-player-bound-chests-permissions-spout-1337.24980/)
