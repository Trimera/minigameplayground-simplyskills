package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.simplyskills.SimplySkillsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides Simply Skills' Undying {@code isDead()} injection for players in disabled
 * zones. The vanilla result is returned after stripping the effect, so a player at zero
 * health dies normally instead of being reset to one point.
 */
@Mixin(value = LivingEntity.class, priority = 2000)
public abstract class LivingEntityIsDeadMixin {
    @Inject(method = "isDeadOrDying()Z", at = @At("HEAD"), cancellable = true)
    private void minigameplayground$disableUndying(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayer player)) {
            return;
        }
        if (!SimplySkillsCompat.isDisabled(player)) {
            return;
        }
        SimplySkillsCompat.stripEffects(player);
        cir.setReturnValue(player.getHealth() <= 0.0F);
    }
}
