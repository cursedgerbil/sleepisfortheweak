package com.cursedgerbil.sleepisfortheweak.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Either;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;

@Mixin(ServerPlayer.class)
public class MixinSleepingRequirements {

    @Inject(at = @At("HEAD"), method = "startSleepInBed", cancellable = true)
    private void startSleepInBed(BlockPos p_9115_, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> callback) {
        Either<Player.BedSleepingProblem, Unit> returnval = callback.getReturnValue();
        if (returnval.left() == java.util.Optional.of(Player.BedSleepingProblem.NOT_SAFE)) {
            Either<Player.BedSleepingProblem, Unit> either = Either.right(Unit.INSTANCE);
            callback.setReturnValue(either);
        }
    }
}
