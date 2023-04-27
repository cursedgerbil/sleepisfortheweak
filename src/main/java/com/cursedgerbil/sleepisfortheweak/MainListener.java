package com.cursedgerbil.sleepisfortheweak;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.cursedgerbil.sleepisfortheweak.network.SleepIsForTheWeakVariables;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber
public class MainListener {

    static final Field sleepfield = ObfuscationReflectionHelper.findField(Player.class, "f_36110_");
    static final Field targetfield = ObfuscationReflectionHelper.findField(Phantom.PhantomAttackPlayerTargetGoal.class, "f_33192_");
    static final Field mobtargetselector = ObfuscationReflectionHelper.findField(Mob.class, "f_21346_");
    static final Field targetinggoal = ObfuscationReflectionHelper.findField(WrappedGoal.class, "f_25994_");

    public static boolean checkPhantomSpawnRules(EntityType<? extends LivingEntity> entity, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        System.out.println("checkin phantom spawns");
        if (level.getLevel().dimension() == Level.OVERWORLD && Math.random() < ConfigHandler.phantomNaturalSpawnOverworld.get()) {
            System.out.println("it overworld");
            if (Monster.isDarkEnoughToSpawn(level, pos, random) && level.getLevel().isNight() && level.canSeeSky(pos)) {
                System.out.println("it good");
                return true;
            }
        }
        if (level.getLevel().dimension() == Level.NETHER && Math.random() < ConfigHandler.phantomNaturalSpawnNether.get()) {
            return true;
        }
        if (level.getLevel().dimension() == Level.END && Math.random() < ConfigHandler.phantomNaturalSpawnEnd.get()) {
            return true;
        }
        System.out.println("sus");
        return false;
    }
    @SubscribeEvent
    public static void StartSleepBedEvent(SleepingTimeCheckEvent event) {
        Result result = event.getResult();
        if (ConfigHandler.disableSleeping.get() == true) {
            result = Result.DENY;
        }
        else {
            if (ConfigHandler.disableSleepTimeCheck.get() == true) {
                result = Result.ALLOW;
            }
        }
        event.setResult(result);
    }

    @SubscribeEvent
    public static void EntitySpawnEvent(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Phantom) {
            //tp phantoms up because they dum dum
            event.getEntity().setPos(event.getEntity().getX(), event.getEntity().getY() + 30, event.getEntity().getZ());
        }
        //change phantom ai
        if (ConfigHandler.phantomsOnlyAttackInsomniacs.get() && event.getEntity() instanceof Phantom pha) {
            GoalSelector phantomtargetselector = pha.targetSelector;
            Set<WrappedGoal> aigoals = phantomtargetselector.getAvailableGoals();
            aigoals.forEach(f -> {
                Goal targetgoal = f.getGoal();
                pha.targetSelector.removeGoal(targetgoal);
                targetfield.setAccessible(true);
                TargetingConditions conditions = null;
                Predicate<LivingEntity> selector = null;
                try {
                    conditions = (TargetingConditions) targetfield.get(targetgoal);
                    if (conditions != null) {
                        selector = conditions.selector;
                        Predicate<LivingEntity> newselector = s -> (s.getCapability(SleepIsForTheWeakVariables.PLAYER_VARIABLES_CAPABILITY)
                                .orElse(new SleepIsForTheWeakVariables.PlayerVariables()).phantomTimer >= ConfigHandler.phantomSpawnTimeRequirement.get());
                        if (selector != null) {
                            conditions.selector(selector.and(newselector));
                        }
                        else {
                            conditions.selector(newselector);
                        }
                        targetfield.set(targetgoal, conditions);

                        WrappedGoal g = f;
                        targetinggoal.setAccessible(true);
                        targetinggoal.set(g, targetgoal);
                        phantomtargetselector.removeGoal(f);
                        phantomtargetselector.addGoal(0, g);
                        mobtargetselector.setAccessible(true);
                        mobtargetselector.set(event.getEntity(), phantomtargetselector);

                    }

                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }
            });
        }
    }

    @SubscribeEvent
    public static void SetSpawnEvent(PlayerSetSpawnEvent event) {
        Result result = event.getResult();
        Entity entity = event.getEntity();
        Level level = entity.getLevel();
        if (ConfigHandler.disableSetSpawnpoint.get() == true && level.getBlockState(event.getNewSpawn()).getBlock() instanceof BedBlock) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void SetWakeEvent(SleepFinishedTimeEvent event) {
        if (ConfigHandler.disableTimeSkip.get() == true) {
            event.setTimeAddition(event.getLevel().dayTime());
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            ServerPlayer player = (ServerPlayer) event.player;
            ServerLevel level = (ServerLevel) player.getLevel();
            ServerStatsCounter ssc = player.getStats();
            ssc.setValue(player, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 0);

            //complete sleep override

            player.getCapability(SleepIsForTheWeakVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.phantomTimer += 1;
                if (player.isSleeping()) {
                    capability.sleepTimer += 1;
                    if (ConfigHandler.sleepHealing.get() > 0) {
                        player.heal((float) (ConfigHandler.sleepHealing.get() / 20));
                    }
                }
                else {
                    capability.sleepTimer = 0;
                }
                if (capability.sleepTimer >= 200) {
                    List<ServerPlayer> players = level.players();
                    int sleepingplayers = 0;
                    int invalidplayers = 0;
                    ServerPlayer firstplayer = null;
                    for (int p = 0; p < players.size(); p++) {
                        firstplayer = players.get(0);
                        ServerPlayer plr = players.get(p);
                        if (plr.isSleeping()) {
                            sleepingplayers += 1;
                        }
                        if ((plr.isCreative() && ConfigHandler.sleepCountIgnoreCreative.get()) || (plr.isSpectator() && ConfigHandler.sleepCountIgnoreSpectator.get())) {
                            invalidplayers += 1;
                        }
                    }
                    int criterion = 0;
                    if ((sleepingplayers - invalidplayers) >= ConfigHandler.playersSleepingAmount.get()) {
                        criterion += 1;
                    }
                    if (((sleepingplayers - invalidplayers) / players.size()) >= (ConfigHandler.playersSleepingPercentage.get() / 100)) {
                        criterion += 1;
                    }
                    if ((criterion == 2 && ConfigHandler.bothCriteriaRequired.get() == true) || (criterion >= 1 && ConfigHandler.bothCriteriaRequired.get() == false)) {

                        //wake up
                        capability.phantomTimer = 0;
                        if (!ConfigHandler.disableTimeSkip.get()) {
                            if (level.getDayTime() > 13000) {
                                level.setDayTime(0);
                            }
                        }
                        if (!ConfigHandler.disableForceWakeUp.get()) {
                            player.stopSleeping();
                        }
                        if (ConfigHandler.sleepTimescale.get() > 1 && player == firstplayer) {
                            level.setDayTime((level.getDayTime() + (long) (ConfigHandler.sleepTimescale.get() - 1)));
                        }

                    }


                }

                //carry over phantom timer
                if (capability.phantomTimer == ConfigHandler.phantomSpawnTimeRequirement.get()) {
                    event.player.addTag("Insomniac");
                }
                else {
                    event.player.removeTag("Insomniac");
                }

                //custom phantom system
                if (ConfigHandler.disablePhantoms.get() == false) {
                    if (capability.phantomTimer == ConfigHandler.phantomSpawnTimeRequirement.get() ||
                            (capability.phantomTimer >= (ConfigHandler.phantomSpawnTimeRequirement.get() + ConfigHandler.phantomSpawnTimeCooldown.get()) &&
                                    (capability.phantomTimer - ConfigHandler.phantomSpawnTimeRequirement.get()) % ConfigHandler.phantomSpawnTimeCooldown.get() == 0)) {
                        if (level.getDayTime() > 13000 && level.dimension() == Level.OVERWORLD) {
                            int phantomstospawn = 0;
                            RandomSource randomsource = level.getRandom();
                            phantomstospawn += ConfigHandler.phantomSpawnBaseAmount.get();
                            phantomstospawn += Math.ceil(Math.floor(capability.phantomTimer / 24000) * ConfigHandler.phantomSpawnExtraAmount.get());
                            if (!player.isSpectator() && phantomstospawn > 0) {
                                BlockPos blockpos = player.blockPosition();
                                if (blockpos.getY() >= level.getSeaLevel() && level.canSeeSky(blockpos)) {
                                    BlockPos blockpos1 = blockpos.above(20 + randomsource.nextInt(15)).east(-10 + randomsource.nextInt(21)).south(-10 + randomsource.nextInt(21));
                                    BlockState blockstate = level.getBlockState(blockpos);
                                    FluidState fluidstate = level.getFluidState(blockpos);
                                    if (NaturalSpawner.isValidEmptySpawnBlock(level, blockpos, blockstate, fluidstate, EntityType.PHANTOM)) {
                                        for (int i = 0; i < phantomstospawn; i++) {
                                            Phantom phantom = EntityType.PHANTOM.create(level);
                                            phantom.moveTo(blockpos1, 0.0f, 0.0f);
                                            if (ForgeHooks.canEntitySpawn(phantom,  level, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), null, MobSpawnType.NATURAL) != -1) {
                                                phantom.finalizeSpawn(level, level.getCurrentDifficultyAt(blockpos), MobSpawnType.NATURAL, null, null);
                                                if (ConfigHandler.phantomsGrow.get()) {
                                                    phantom.setPhantomSize((int) Math.floor(capability.phantomTimer / 24000));
                                                }
                                                level.addFreshEntityWithPassengers(phantom);

                                            }
                                        }
                                    }
                                }
                            }

                        }
                        else {capability.phantomTimer -= 400;}
                    }
                }
                capability.syncPlayerVariables(player);
            });

            //disable vanilla sleep skips
            //MOJANG. Why did you make Player.sleepCounter private? Like, literally, WHY!? YOU HAVE MADE MY LIFE 10X HARDER FOR NO REASON!!!
            //Reflection is scary
            sleepfield.setAccessible(true);
            try {
                sleepfield.set(event.player, 0);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
    }


}
