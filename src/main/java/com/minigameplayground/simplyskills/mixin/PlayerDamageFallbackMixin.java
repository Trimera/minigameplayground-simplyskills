package com.minigameplayground.simplyskills.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.minigameplayground.damage.DamageFallbackContext;
import com.minigameplayground.damage.DamageProcessor;
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
 * core mod; it was moved here together with the Simply Skills compatibility layer and
 * keeps the same all-zones behaviour as before.
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

    /**
     * Wraps the vanilla magic-absorb call itself instead of relying on a RETURN
     * injection. Other mods that call {@code CallbackInfoReturnable.setReturnValue}
     * during that method cancel all later RETURN callbacks, which used to make the
     * core scaling disappear for TACZ bullets. Wrapping the call still sees the final
     * returned value, so scaling can be applied before vanilla subtracts health.
     */
    @WrapOperation(
            method = "actuallyHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb"
                            + "(Lnet/minecraft/world/damagesource/DamageSource;F)F"
            )
    )
    private float minigameplayground$scaleBeforeHealth(Player entity,
                                                       DamageSource source,
                                                       float amount,
                                                       Operation<Float> original) {
        float result = original.call(entity, source, amount);
        if (!(entity instanceof ServerPlayer victim)) {
            return result;
        }

        DamageFallbackContext context = DamageFallbackContext.current();
        if (context != null && context.matches(victim, source) && context.magicApplied()) {
            return result;
        }

        float scaled = DamageProcessor.processDamage(victim, source, result);
        if (!Float.isFinite(scaled)) {
            return result;
        }

        if (context != null && context.matches(victim, source)) {
            // Tell the RETURN fallback that the balance has already been applied.
            DamageFallbackContext.markMagicApplied(victim, source);
        }
        return scaled;
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
                    || DamageProcessor.isUnstoppable(source)) {
                return;
            }

            float healthLost = context.healthBefore() - victim.getHealth();
            if (healthLost <= 0.0F) {
                return;
            }

            // A lethal hit is clamped to the victim's remaining health by vanilla.
            // Scaling that tiny clamped value back up can restore the victim to a
            // microscopic non-zero health value and create a permanent health lock.
            // Keep lethal hits lethal; the outer hurt() handles totems and other
            // death-prevention hooks.
            if (victim.getHealth() <= 0.0F
                    || healthLost >= context.healthBefore() - 1.0E-6F) {
                victim.setHealth(0.0F);
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

            float newHealth = Math.max(0.0F, context.healthBefore() - scaledLoss);
            victim.setHealth(newHealth);
            // Leave death handling to vanilla and other mods so totems/death protection
            // are not bypassed.
        } finally {
            DamageFallbackContext.clear(context);
        }
    }
}
