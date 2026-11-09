package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.simplyskills.SimplySkillsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.sweenus.simplyskills.util.HelperMethods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes all Simply Skills skill-tree checks fail for players in disabled zones. This
 * turns off passives and active skills at their common gate instead of trying to list
 * every individual ability.
 */
@Mixin(HelperMethods.class)
public abstract class HelperMethodsMixin {
    @Inject(method = "isUnlocked", at = @At("HEAD"), cancellable = true)
    private static void minigameplayground$disableSkillChecks(String tree,
                                                              String skill,
                                                              LivingEntity entity,
                                                              CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ServerPlayer player && SimplySkillsCompat.isDisabled(player)) {
            cir.setReturnValue(false);
        }
    }
}
