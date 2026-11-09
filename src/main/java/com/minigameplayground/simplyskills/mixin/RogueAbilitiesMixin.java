package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.simplyskills.SimplySkillsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.sweenus.simplyskills.abilities.RogueAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables the Rogue Evasion Mastery dodge roll for players in disabled zones. Simply
 * Skills cancels the hit when this method returns false; returning true keeps the hit.
 */
@Mixin(RogueAbilities.class)
public abstract class RogueAbilitiesMixin {
    @Inject(method = "passiveRogueEvasionMastery", at = @At("HEAD"), cancellable = true)
    private static void minigameplayground$disableEvasion(Player player,
                                                          CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof ServerPlayer serverPlayer
                && SimplySkillsCompat.isDisabled(serverPlayer)) {
            cir.setReturnValue(true);
        }
    }
}
