package com.cursedgerbil.sleepisfortheweak;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class ConfigHandler {
    public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec spec;

    //General settings
    public static final ConfigValue<Boolean> disableSleeping;
    public static final ConfigValue<Boolean> disableSetSpawnpoint;
    public static final ConfigValue<Boolean> disableTimeSkip;
    public static final ConfigValue<Boolean> disableForceWakeUp;
    public static final ConfigValue<Boolean> disableSleepTimeCheck;
    public static final ConfigValue<Boolean> disableSleepMonsterCheck;
    public static final ConfigValue<Integer> sleepMonsterRadius;

    //Multiplayer sleep settings
    public static final ConfigValue<Double> playersSleepingPercentage;
    public static final ConfigValue<Integer> playersSleepingAmount;
    public static final ConfigValue<Boolean> bothCriteriaRequired;
    public static final ConfigValue<Boolean> sleepCountIgnoreCreative;
    public static final ConfigValue<Boolean> sleepCountIgnoreSpectator;

    //Sleeping effect settings
    public static final ConfigValue<Double> sleepHealing;
    public static final ConfigValue<Integer> sleepTimescale;

    //Phantom settings
    public static final ConfigValue<Boolean> disablePhantoms;
    public static final ConfigValue<Boolean> phantomsGrow;
    public static final ConfigValue<Boolean> phantomsOnlyAttackInsomniacs;
    public static final ConfigValue<Integer> phantomSpawnTimeRequirement;
    public static final ConfigValue<Integer> phantomSpawnTimeCooldown;
    public static final ConfigValue<Double> phantomSpawnBaseAmount;
    public static final ConfigValue<Double> phantomSpawnExtraAmount;
    public static final ConfigValue<Double> phantomNaturalSpawnOverworld;
    public static final ConfigValue<Double> phantomNaturalSpawnNether;
    public static final ConfigValue<Double> phantomNaturalSpawnEnd;

    static {
        builder.push("General Settings");
        disableSleeping = builder.comment("Completely disable sleeping. Default: false").define("disableSleeping", false);
        disableSetSpawnpoint = builder.comment("Disable setting your spawnpoint by right-clicking on a bed. Default: false").define("disableSetSpawnpoint", false);
        disableTimeSkip = builder.comment("Disable skipping the time to day when sleeping. Default: false").define("disableTimeSkip", false);
        disableForceWakeUp = builder.comment("Disable being kicked out of bed after sleeping. Default: false\n"
                + "You'll still be kicked out of bed at daytime unless disableTimeCheck is enabled.").define("disableForceWakeUp", false);
        disableSleepTimeCheck = builder.comment("If enabled, you can sleep at any time of day. Default: false").define("disableSleepTimeCheck", false);
        builder.pop();

        builder.push("Multiplayer Sleep Settings");
        playersSleepingPercentage = builder.comment("The percentage of total players who must be sleeping to change time (0-100). Default: 50\n"
                + "Even if this and playersSleepingAmount are 0, someone must sleep to trigger time changes.").define("playersSleepingPercentage", 50.0);
        playersSleepingAmount = builder.comment("A flat amount of players who must be sleeping to change time. Default: 0").define("playersSleepingAmount", 0);
        bothCriteriaRequired = builder.comment("Whether both options above must be met to change time, or just one. Default: false").define("bothCriteriaRequired", false);
        sleepCountIgnoreCreative = builder.comment("Whether sleeping percentage calculations should ignore players in Creative mode. Default: true").define("sleepCountIgnoreCreative", true);
        sleepCountIgnoreSpectator = builder.comment("Whether sleeping percentage calculations should ignore players in Spectator mode. Default: true").define("sleepCountIgnoreSpectator", true);
        disableSleepMonsterCheck = builder.comment("Completely disable the monster check when trying to sleep. Default: false").define("disableSleepMonsterCheck", false);
        sleepMonsterRadius = builder.comment("The radius in blocks the game checks for monsters when trying to sleep. Default: 5.").define("sleepMonsterRadius", 5);
        builder.pop();

        builder.push("Sleeping Effect Settings");
        sleepHealing = builder.comment("When sleeping, players will be healed for this much health per second. Set to 0 to disable. Default: 0").define("sleepHealing", 0.0);
        sleepTimescale = builder.comment("When enough players are sleeping, time will progress at this multiplier. Set to 1 to disable. Default: 1").define("sleepTimescale", 1);
        builder.pop();

        builder.push("Phantom Settings");
        disablePhantoms = builder.comment("Completely disable phantoms from spawning as a result of not sleeping. Default: false").define("disablePhantoms", false);
        phantomsGrow = builder.comment("If enabled, phantoms will grow larger the longer you haven't slept (a cut vanilla value). Default: false\n"
                +  "Does not affect naturally spawning phantoms.").define("phantomsGrow", false);
        phantomsOnlyAttackInsomniacs = builder.comment("If enabled, phantoms will only attack players who have stayed awake long enough for them\n"
                + "to spawn. Default: true.").define("phantomsOnlyAttackInsomniacs", true);
        phantomSpawnTimeRequirement = builder.comment("The number of ticks a player must stay awake for before phantoms can spawn on them.\n"
                + "24000 ticks = 1 day. Default: 72000").define("phantomSpawnTimeRequirement", 72000);
        phantomSpawnTimeCooldown = builder.comment("The cooldown between phantoms spawning on a player. 24000 ticks = 1 day. Default: 24000").define("phantomSpawnTimeCooldown", 24000);
        phantomSpawnBaseAmount = builder.comment("When phantoms spawn on an eligible player, how many spawn by default? Default: 3").define("phantomSpawnBaseAmount", 3.0);
        phantomSpawnExtraAmount = builder.comment("How many extra phantoms to spawn for each day the player has gone without sleeping\n"
                + "Decimals and negatives supported, will round up. Default: 1").define("phantomSpawnExtraAmount", 1.0);
        phantomNaturalSpawnOverworld = builder.comment("The spawn modifier for Phantoms in the overworld. (0-1) Default: 0"
                + "At 1, Phantoms will spawn as often as zombies, skeletons, spiders and creepers. At 0, phantoms will not"
                + "spawn naturally. Phantoms only spawn at night on blocks exposed to the sky.").define("phantomNaturalSpawnOverworld", 0.0);
        phantomNaturalSpawnNether = builder.comment("The spawn modifier for Phantoms in the Nether. (0-1) Default: 0"
                + "At 1, Phantoms will spawn as often as zombified piglins. At 0, none will spawn.").define("phantomNaturalSpawnNether", 0.0);
        phantomNaturalSpawnEnd = builder.comment("The spawn modifier for Phantoms in the End. (0-1) Default: 0"
                + "At 1, Phantoms will spawn 10x as often as endermen (so low values work best!)").define("phantomNaturalSpawnEnd", 0.0);
        builder.pop();

        spec = builder.build();
    }

}
