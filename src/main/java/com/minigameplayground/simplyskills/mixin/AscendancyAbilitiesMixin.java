package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.simplyskills.SimplySkillsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.sweenus.simplyskills.abilities.AscendancyAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables the Ascendancy Torment damage-redirect hook for victims in disabled zones.
 */
@Mixin(AscendancyAbilities.class)
public abstract class AscendancyAbilitiesMixin {
    @Inject(method = "tormentEffect", at = @At("HEAD"), cancellable = true)
    private static void minigameplayground$disableTorment(Player victim,
                                                          DamageSource source,
                                                          float amount,
                                                          CallbackInfoReturnable<Boolean> cir) {
        if (victim instanceof ServerPlayer serverPlayer
                && SimplySkillsCompat.isDisabled(serverPlayer)) {
            cir.setReturnValue(false);
        }
    }
}
