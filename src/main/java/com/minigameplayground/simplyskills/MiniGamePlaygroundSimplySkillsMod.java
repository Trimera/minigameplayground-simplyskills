package com.minigameplayground.simplyskills;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optional compatibility layer for Simply Skills.
 *
 * <p>MiniGamePlayground zones can mark themselves as "Simply Skills disabled". This
 * addon then removes Simply Skills effects and attribute rewards from players in those
 * zones and suppresses its damage/death-prevention hooks. Players outside such zones keep
 * the full Simply Skills behaviour.</p>
 */
public final class MiniGamePlaygroundSimplySkillsMod implements ModInitializer {
    public static final String MOD_ID = "minigameplayground-simplyskills";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Force a reward recalculation after respawn/reconnect. Otherwise Puffish's
        // login/respawn reward application could restore attributes while the player is
        // already standing in a disabled zone.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer != null) {
                SimplySkillsCompat.forget(newPlayer.getUUID());
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler.player != null) {
                SimplySkillsCompat.forget(handler.player.getUUID());
            }
        });

        LOGGER.info("MiniGamePlayground Simply Skills compatibility initialized.");
    }
}
