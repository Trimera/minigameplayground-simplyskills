package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.simplyskills.SimplySkillsCompat;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles zone entry at the start of the player tick, before movement and damage.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerTickMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void minigameplayground$updateSimplySkillsState(CallbackInfo ci) {
        SimplySkillsCompat.tick((ServerPlayer) (Object) this);
    }
}
