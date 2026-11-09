package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.damage.DamageFallbackContext;
import com.minigameplayground.damage.DamageProcessor;
import com.minigameplayground.simplyskills.SimplySkillsCompat;
import com.minigameplayground.zone.PlaygroundZone;
import com.minigameplayground.zone.ZoneManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restores MiniGamePlayground's damage balance when Simply Skills cancels the normal
 * {@code getDamageAfterMagicAbsorb} return callback. This fallback used to live in the
 * core mod specifically for Simply Skills; it now only applies inside zones where
 * Simply Skills is disabled, so zones that keep Simply Skills active retain its behavior.
 */
@Mixin(Player.class)
public abstract class PlayerDamageFallbackMixin {
    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void minigameplayground$beginDamageFallback(DamageSource source, float amount,
                                                        CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer victim) {
            DamageFallbackContext.begin(victim, source, victim.getHealth());
        }
    }

    @Inject(method = "actuallyHurt", at = @At("RETURN"))
    private void minigameplayground$applyDamageFallback(DamageSource source, float amount,
                                                        CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer victim)) {
            return;
        }

        DamageFallbackContext context = DamageFallbackContext.current();
        if (context == null || !context.matches(victim, source)) {
            return;
        }

        try {
            if (context.magicApplied()
                    || DamageProcessor.isUnstoppable(source)
                    || !SimplySkillsCompat.isDisabled(victim)) {
                return;
            }

            float healthLost = context.healthBefore() - victim.getHealth();
            if (healthLost <= 0.0F) {
                return;
            }

            PlaygroundZone victimZone = ZoneManager.getZone(victim);
            if (victimZone == null || victimZone.isDamageDisabled()) {
                return;
            }

            float scaledLoss = DamageProcessor.processDamage(victim, source, healthLost);
            if (!Float.isFinite(scaledLoss) || scaledLoss == healthLost) {
                return;
            }

            victim.setHealth(Math.max(0.0F, context.healthBefore() - scaledLoss));
            // Leave death handling to vanilla and other mods so totems/death protection
            // are not bypassed.
        } finally {
            DamageFallbackContext.clear(context);
        }
    }
}
