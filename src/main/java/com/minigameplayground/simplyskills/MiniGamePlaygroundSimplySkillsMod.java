package com.minigameplayground.simplyskills;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.reward.builtin.AttributeReward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Optional compatibility layer for Simply Skills.
 *
 * <p>MiniGamePlayground zones can mark themselves as "Simply Skills disabled". This
 * addon then removes Simply Skills effects from players in those zones and suppresses
 * its damage/death-prevention hooks. Players outside such zones keep the full Simply
 * Skills behaviour.</p>
 */
public final class MiniGamePlaygroundSimplySkillsMod implements ModInitializer {
    public static final String MOD_ID = "minigameplayground-simplyskills";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Set<UUID> DISABLED_PLAYERS = new HashSet<>();

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                boolean disabled = SimplySkillsCompat.isDisabled(player);
                UUID playerId = player.getUUID();
                boolean wasDisabled = DISABLED_PLAYERS.contains(playerId);
                if (disabled != wasDisabled) {
                    if (disabled) {
                        DISABLED_PLAYERS.add(playerId);
                    } else {
                        DISABLED_PLAYERS.remove(playerId);
                    }
                    // Re-evaluate Puffish attribute rewards. Entering the zone removes
                    // them; leaving the zone lets the normal reward calculation add them
                    // back.
                    SkillsAPI.updateRewards(player, AttributeReward.class);
                }
                if (disabled) {
                    SimplySkillsCompat.stripEffects(player);
                }
            }
        });

        // Force a reward recalculation after respawn/reconnect. Otherwise Puffish's
        // login/respawn reward application could restore attributes while the player is
        // already standing in a disabled zone.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer != null) {
                DISABLED_PLAYERS.remove(newPlayer.getUUID());
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler.player != null) {
                DISABLED_PLAYERS.remove(handler.player.getUUID());
            }
        });

        LOGGER.info("MiniGamePlayground Simply Skills compatibility initialized.");
    }
}
