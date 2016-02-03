package ninja.leaping.modifyworld

import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.message.MessageChannelEvent
import org.spongepowered.api.event.network.ClientConnectionEvent

import xyz.aoeu.spongekt.*;

/**
 * Event listeners for Modifyworld checks
 */

class Listeners(private val plugin: ModifyworldPlugin) {
    @Listener
    fun whitelistListener(event: ClientConnectionEvent.Login) {
        if (!event.targetUser.hasPermission(Permissions.WHITELIST)) {
            event.setMessage(+"You are not whitelisted! Make sure you've been invited!")
            event.isCancelled = true
        }
    }

    @Listener
    fun chatListener(event: MessageChannelEvent.Chat) {
        event.cause.first(Player::class.java).ifPresent {
            if (!it.hasPermission(Permissions.CHAT)) { // TODO: Figure out notification
                event.isCancelled = true;
            }
        }
    }
}
