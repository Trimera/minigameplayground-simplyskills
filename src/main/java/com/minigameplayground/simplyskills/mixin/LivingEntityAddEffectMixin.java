package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.simplyskills.SimplySkillsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents Simply Skills from applying new effects to players inside zones where
 * Simply Skills is disabled.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAddEffectMixin {
    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            at = @At("HEAD"), cancellable = true)
    private void minigameplayground$blockSimplySkillsEffect(MobEffectInstance instance,
                                                            CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer player
                && instance != null
                && SimplySkillsCompat.isDisabled(player)
                && SimplySkillsCompat.isSimplySkillsEffect(instance.getEffect())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    private void minigameplayground$blockSimplySkillsEffectWithSource(MobEffectInstance instance,
                                                                      Entity source,
                                                                      CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer player
                && instance != null
                && SimplySkillsCompat.isDisabled(player)
                && SimplySkillsCompat.isSimplySkillsEffect(instance.getEffect())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "forceAddEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"), cancellable = true)
    private void minigameplayground$blockForcedSimplySkillsEffect(MobEffectInstance instance,
                                                                  Entity source,
                                                                  CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player
                && instance != null
                && SimplySkillsCompat.isDisabled(player)
                && SimplySkillsCompat.isSimplySkillsEffect(instance.getEffect())) {
            ci.cancel();
        }
    }
}
