package ninja.leaping.modifyworld

import com.google.inject.Inject
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.Game
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.event.game.state.GameStoppingServerEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.event.Listener

/**
 * A simple sponge plugin
 */
@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION)
class ModifyworldPlugin @Inject constructor(private val logger: Logger, val game: Game) {
    // Give us a configuration to work from
    @Inject @DefaultConfig(sharedRoot = true) private lateinit var configLoader: ConfigurationLoader<CommentedConfigurationNode>

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        game.eventManager.registerListeners(this, Listeners(this))
        // Perform initialization tasks here
    }

    @Listener
    fun disable(event: GameStoppingServerEvent) {
        // Perform shutdown tasks here
    }
}

internal object Permissions {
    const val ROOT = "modifyworld"
    const val WHITELIST = "$ROOT.login"
    const val CHAT = "$ROOT.chat"
}
