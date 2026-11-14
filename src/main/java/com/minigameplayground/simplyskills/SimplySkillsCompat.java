package com.minigameplayground.simplyskills;

import com.minigameplayground.zone.PlaygroundZone;
import com.minigameplayground.zone.ZoneManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.reward.builtin.AttributeReward;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared checks for the Simply Skills compatibility addon.
 */
public final class SimplySkillsCompat {
    private static final String SIMPLY_SKILLS_NAMESPACE = "simplyskills";
    private static final Set<UUID> DISABLED_PLAYERS = new HashSet<>();

    private SimplySkillsCompat() {
    }

    /**
     * Called at the start of the player tick, before movement and before any damage.
     * Entering a disabled zone therefore removes effects and attribute rewards in the
     * same tick instead of waiting for the end-of-server-tick cleanup.
     */
    public static void tick(ServerPlayer player) {
        if (player == null) {
            return;
        }
        boolean disabled = isDisabled(player);
        UUID playerId = player.getUUID();
        boolean wasDisabled = DISABLED_PLAYERS.contains(playerId);
        if (disabled != wasDisabled) {
            if (disabled) {
                DISABLED_PLAYERS.add(playerId);
            } else {
                DISABLED_PLAYERS.remove(playerId);
            }
            // Re-evaluate Puffish attribute rewards synchronously. Entering removes
            // them; leaving lets the normal calculation add them back.
            SkillsAPI.updateRewards(player, AttributeReward.class);
        }
        if (disabled) {
            stripEffects(player);
            clampHealth(player);
        }
    }

    /**
     * Simply Skills' skill tree can grant max-health attributes. When those rewards are
     * removed on entering a disabled zone, vanilla does not automatically shrink the
     * current health, leaving the player above their new maximum. Clamp it every tick
     * while the zone is disabled so the health bar and damage calculations stay valid.
     */
    public static void clampHealth(ServerPlayer player) {
        if (player == null) {
            return;
        }
        float maxHealth = player.getMaxHealth();
        if (Float.isFinite(maxHealth) && maxHealth > 0.0F
                && player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    public static void forget(UUID playerId) {
        if (playerId != null) {
            DISABLED_PLAYERS.remove(playerId);
        }
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
