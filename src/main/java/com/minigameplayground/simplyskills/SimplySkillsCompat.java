package com.minigameplayground.simplyskills;

import com.minigameplayground.zone.PlaygroundZone;
import com.minigameplayground.zone.ZoneManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

/**
 * Shared checks for the Simply Skills compatibility addon.
 */
public final class SimplySkillsCompat {
    private static final String SIMPLY_SKILLS_NAMESPACE = "simplyskills";

    private SimplySkillsCompat() {
    }

    /**
     * Returns true when any zone containing the player has Simply Skills disabled.
     * Overlapping zones use the lenient "any zone may disable it" rule.
     */
    public static boolean isDisabled(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        for (PlaygroundZone zone : ZoneManager.getZonesAt(player)) {
            if (zone.isSimplySkillsDisabled()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSimplySkillsEffect(MobEffect effect) {
        if (effect == null) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
        return id != null && SIMPLY_SKILLS_NAMESPACE.equals(id.getNamespace());
    }

    /**
     * Removes every Simply Skills status effect from the player. Iterating a copy avoids
     * ConcurrentModificationException because removeEffect updates the active effect map.
     */
    public static void stripEffects(ServerPlayer player) {
        if (player == null) {
            return;
        }
        for (MobEffectInstance instance : List.copyOf(player.getActiveEffects())) {
            if (isSimplySkillsEffect(instance.getEffect())) {
                player.removeEffect(instance.getEffect());
            }
        }
    }
}
