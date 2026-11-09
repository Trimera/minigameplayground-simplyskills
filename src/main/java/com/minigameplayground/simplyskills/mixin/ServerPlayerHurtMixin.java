package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.simplyskills.SimplySkillsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes Simply Skills effects immediately before a player is damaged. This covers
 * effects that were applied before the player entered the zone or during the same tick.
 */
@Mixin(value = ServerPlayer.class, priority = 2000)
public abstract class ServerPlayerHurtMixin {
    @Inject(method = "hurt", at = @At("HEAD"))
    private void minigameplayground$stripSimplySkillsEffects(DamageSource source,
                                                             float amount,
                                                             CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (SimplySkillsCompat.isDisabled(player)) {
            SimplySkillsCompat.stripEffects(player);
        }
    }
}
