package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.simplyskills.SimplySkillsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.sweenus.simplyskills.util.DynamicDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables Simply Skills' dynamic damage reduction for victims inside disabled zones.
 * The method is a static Simply Skills utility, so it is targeted by name rather than
 * by a compile-time dependency.
 */
@Mixin(DynamicDamage.class)
public abstract class DynamicDamageMixin {
    @Inject(method = "dynamicDamageReduction", at = @At("HEAD"), cancellable = true)
    private static void minigameplayground$disableDdr(LivingEntity attacker,
                                                      LivingEntity victim,
                                                      float amount,
                                                      float lastDamage,
                                                      float currentDamage,
                                                      float lastDamageTime,
                                                      CallbackInfoReturnable<Float> cir) {
        if (victim instanceof ServerPlayer player && SimplySkillsCompat.isDisabled(player)) {
            cir.setReturnValue(amount);
        }
    }
}
