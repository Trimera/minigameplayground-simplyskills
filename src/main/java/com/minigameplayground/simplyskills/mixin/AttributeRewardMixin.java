package com.minigameplayground.simplyskills.mixin;

import com.minigameplayground.simplyskills.SimplySkillsCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.reward.builtin.AttributeReward;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

/**
 * Removes Puffish Skills attribute rewards while a player is in a Simply Skills
 * disabled zone. The addon triggers a reward recalculation on zone transitions so
 * leaving the zone restores the unlocked skill attributes.
 *
 * <p>Puffish Skills does not expose the owning category in a reward update context, so
 * this applies to all Puffish attribute rewards. On servers that use other Puffish
 * skill trees, those attribute rewards are also suspended inside disabled zones.</p>
 */
@Mixin(value = AttributeReward.class, remap = false)
public abstract class AttributeRewardMixin {
    @Shadow(remap = false)
    @Final
    private List<UUID> uuids;

    @Shadow(remap = false)
    @Final
    private Attribute attribute;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true, remap = false)
    private void minigameplayground$removeSkillAttributes(RewardUpdateContext context,
                                                          CallbackInfo ci) {
        ServerPlayer player = context.getPlayer();
        if (!SimplySkillsCompat.isDisabled(player)) {
            return;
        }

        AttributeInstance instance = player.getAttribute(this.attribute);
        if (instance != null) {
            for (UUID uuid : this.uuids) {
                instance.removeModifier(uuid);
            }
        }
        ci.cancel();
    }
}
