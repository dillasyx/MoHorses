# Mo' Horses

Horses, horse breeding and the special horses of DrZhark's Mo' Creatures, rebuilt for current Minecraft
on top of the vanilla horse look. Heavily inspired by Mo' Creatures, licensed under the GPLv3.

- Minecraft 26.3, Java 25
- Fabric, NeoForge and Forge from one code base (`common` + one module per loader)
- Horses use the vanilla horse model and animations, with wings and horns added on top. The maggot uses [GeckoLib](https://github.com/bernie-g/geckolib) 5

## Horses

| Kind | How to get it | Notes |
|---|---|---|
| Zebra | Spawns in savannas, badlands and the desert (biome tag `mohorses:spawns_zebras`) | Drops leather like a horse. Breeds like a horse, carries a chest. Wears no armor. Can not be broken in by riding: tame it with sugar lumps (one in three tames it), only while you ride a black and white horse (black with white spots or patches, or white with black spots). Once tamed it can be saddled and ridden. Turns any music disc into the Shuffling disc, and dances (shuffles, with music notes) while a jukebox within 16 blocks plays it |
| Zorse | Zebra + Horse | Sterile |
| Zonkey | Zebra + Donkey | Sterile, no armor |
| Bat Horse | Essence of Darkness on a Zorse standing on the ground above the clouds | Flies. Breeds and heals with Essence of Darkness |
| Nightmare | Essence of Fire on a Zorse | Fire immune, no armor. Breeds like a horse, heals with Essence of Fire. Feed it redstone: for a minute it is faster and leaves a trail of fire when ridden (only particles without mob griefing) |
| Wild Nightmare | Spawns rarely and alone in the Nether Wastes, Soul Sand Valleys and Basalt Deltas (biome tag `mohorses:spawns_wild_nightmares`) | Hostile, can't be tamed. One in twenty comes with a Piglin Brute riding it, golden spear in hand. Drops leather, and with a 15 % chance a Heart of Fire when a player kills it |
| Unicorn | Essence of Light on a Nightmare | Crack the whip while riding and it charges, head down: only then does its horn hurt. Glides down slowly, leaving a trail of stars when it jumps or glides, no fall damage. Drops its horn when it dies. Breeds and heals with Essence of Light |
| Pegasus | Essence of Light on a Bat Horse standing on the ground above the clouds | Flies. Breeds and heals with Essence of Light |
| Dark Pegasus | Essence of Darkness on a Pegasus standing on the ground above the clouds | Flies, fire immune. Breeds and heals with Essence of Darkness |
| Fairy Horse | Unicorn + Pegasus (white) or Unicorn + Dark Pegasus (black), each fed its essence; or two Fairy Horses | Flies. The parents are used up: the Unicorn is gone (its gear drops), the Pegasus comes back in a Sky Amulet. Each one can be dyed once, in any colour (white dye makes it white) |
| Undead Unicorn | Essence of Undead on a Unicorn or Fairy Horse | Rots away into a Skeleton Unicorn |
| Undead Pegasus | Essence of Undead on a Pegasus, Dark Pegasus or Bat Horse | Flies. Rots away into a Skeleton Pegasus |
| Skeleton Unicorn / Pegasus | An Undead Unicorn / Pegasus that has rotted away | The Pegasus still flies. Essence of Light gives them their flesh back: a Unicorn / Pegasus again |
| Ghost Horse | 10 % chance when a tamed Zorse, Zonkey, Nightmare or Unicorn dies | Sea green, see-through and flickering. Hovers with its legs swept back, sinks slowly instead of falling. Belongs to the dead horse's owner |
| Ghost Pegasus | 10 % chance when a tamed Pegasus, Dark Pegasus, Bat Horse or Fairy Horse dies | Pale white, see-through and flickering, hovers with its legs swept back, flies |

Essence of Undead on a Zebra, Zorse, Zonkey or Nightmare, or on a vanilla horse, donkey or mule, turns it into a
vanilla zombie horse; Essence of Light on a vanilla skeleton horse makes it a horse again (they flicker
between their old and new look like the mod's horses). Like the other
essences it takes four seconds of flickering, with rotten flesh falling off; two or three maggots crawl out one after
the other while it lasts, and so does every undead horse with flesh left on it that dies (zombie horses included, skeletons not). The undead are hurt by
healing and healed by harming, and fit in the Bone Amulet. Essence of Undead heals the undead and the ghosts.

Undead Unicorns and Undead Pegasi burn in daylight like zombies (horse armor protects them, and wears down doing it);
the skeletons and ghosts don't. While you ride an undead horse (zombie or skeleton, vanilla or not), monsters leave you
alone, unless you attack them first.

Ghosts rise from the dead horse, fading in over three seconds, with the same owner, name and stats. They can't breed,
wear only Crystal Horse Armor (saddle and armor are as see-through as the ghost), fit in the Ghost Amulet, and never leave another ghost.

All of them can be saddled and carry a chest (shears take it off again). The Zorse wears the
normal horse armor, the winged and horned kinds (undead and skeletons included) and the ghosts only Crystal Horse
Armor (as strong as netherite; crafted from netherite horse armor, diamonds and glass). Zebras, Zonkeys and Nightmares wear none.

New horses get the stats of their vanilla counterpart: the horse-like kinds roll health, speed and jump like a
horse, the Zonkey is a donkey, the Undead Unicorn and Pegasus a zombie horse, the skeletons a skeleton horse. Foals
get the average of their parents, transformations and ghosts keep the stats of the horse.
Essences only work on tamed, grown horses. A transformation takes four seconds: the horse stands still and flickers
between its old and new look, faster and faster, before it turns.

Flying: while riding a winged horse, hold the jump key in mid-air to climb. Releasing it gives an extra flap; a longer
charge gives a stronger one.

Zombie horses, Undead Unicorns and Undead Pegasi rot away like the undead horses of Mo' Creatures: every 4 minutes another patch of flesh falls
off and a maggot crawls out (bits of flesh drop to the ground all the time), and after 5 stages only a skeleton is left, keeping its owner
and saddle.

Maggots also crawl out of composters: every item put into a composter has a 1 in 64 chance to bring one out. They wiggle
around harmlessly and drop slime balls when killed.

The whip (leads, leather, iron nuggets and a unicorn horn) cracks when used. Crack it while riding a Unicorn and the unicorn
lowers its head and charges for two seconds: everything its horn meets meanwhile takes 10 damage. Crack it at a
creature and the unicorn charges at that one.

Amulets hold a tamed horse with its saddle, armor and chest: use an empty amulet on the horse, and the full one on a
block to let it out. Pegasi and Dark Pegasi go in the Pegasus Amulet, Fairy Horses in the Fairy Amulet, the undead in
the Bone Amulet (vanilla zombie and skeleton horses and zombie nautiluses too), ghosts in the Ghost Amulet, every other kind in the Pet
Amulet. The Pet Amulet also holds any other tamed animal of yours: wolves, cats, parrots, vanilla horses and so on. Only
the registered owner can put an animal in (so not vanilla camels, which never get one), and only a grown one. Hold Alt over a full amulet to see the animal. Modpacks can keep
other mods' animals out of all amulets with the entity type tag `mohorses:amulet_excluded`.

With [Modonomicon](https://modrinth.com/mod/modonomicon) installed (an optional dependency), the mod adds the in-game
guide book "Mo' Horses Guide": craft it from a book and a sugar lump. Its five chapters (first steps, breeding,
essences, the undead and the items) explain everything on this page, with the horses shown in 3D, in all seven languages
of the mod.

The advancement tab "Mo' Horses" follows the breeding line from the first tamed Zebra to the Fairy Horse, plus the
undead, the ghosts and the items, up to the Stable Master challenge: own every kind of horse of the mod once.

## Building

```
./gradlew build
```

The jars end up in `fabric/build/libs`, `neoforge/build/libs` and `forge/build/libs`.
Use `./gradlew :fabric:runClient` (or `:neoforge:runClient`, `:forge:runClient`) to start a test client.
For multiplayer tests, `runServer` starts a dedicated server and `runClient -PjoinLocal` a client that joins it right away.

Recipes, item models and tags are generated with `./gradlew :fabric:runDatagen` into `common/src/main/generated/resources`
(used by all three loaders). Entity loot tables, lang, sounds and biome/block tags are written by hand in
`common/src/main/resources`.

## Layout

- `common` holds everything shared by the loaders; `fabric`, `neoforge` and `forge` only the loader specific setup
- `common/src/main/java/one/syx/mohorses/entity/HorseKind.java` describes every horse kind and holds the breeding and essence rules
- `common/src/main/java/one/syx/mohorses/entity/MoHorse.java` is the one entity class behind all kinds
- `common/src/main/java/one/syx/mohorses/client/model/MoHorseModel.java` is the vanilla horse model plus wings, horn and saddle bags; textures use the vanilla horse layout (64x64, or 128x128 with the wings/horn below it)
- `common/src/main/java/one/syx/mohorses/mixin` holds the small hooks into vanilla horses (essences, rotting zombie horses, ghosts, undead mounts, amulets)
- `fabric/src/main/java/one/syx/mohorses/datagen` generates recipes, item models and tags

## Credits and license

Mo' Horses is a fan project, heavily inspired by [Mo' Creatures](https://www.curseforge.com/minecraft/mc-mods/mo-creatures)
by DrZhark (models and textures also by BlockDaddy, code by Bloodshot), kept alive by the Mo' Creatures Extended
team (ACGaming, IcarussOne, xJon) and the Nostalgia Edition team (multision, TheidenHD). It is not affiliated with or
endorsed by them.

The horse textures are based on the Mo' Creatures originals, and the Shuffling music disc plays the Mo' Creatures
song. Mo' Creatures is licensed under the GNU GPLv3, and so is Mo' Horses: see [LICENSE](LICENSE).
