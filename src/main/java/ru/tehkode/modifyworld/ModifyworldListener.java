package ru.tehkode.modifyworld;

import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.ConfigurationNode;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 *
 * @author code
 */
public class ModifyworldListener implements Listener {

    public final static String PERMISSION_DENIED = "Sorry, you don't have enough permissions";
    protected String permissionDenied = PERMISSION_DENIED;
    protected PermissionManager permissionsManager;
    protected ConfigurationNode config;
    protected boolean informPlayers = false;

    public ModifyworldListener(Plugin plugin, ConfigurationNode config) {
        this.permissionsManager = PermissionsEx.getPermissionManager();
        this.config = config;

        this.registerEvents(plugin);

        this.informPlayers = config.getBoolean("informplayers", informPlayers);
        this.permissionDenied = config.getString("messages.permissionDenied", this.permissionDenied);
    }

    protected void informPlayer(Player player, String message) {
        if (this.informPlayers) {
            player.sendMessage(ChatColor.RED + message);
        }
    }

    protected void informPlayerAboutDenial(Player player) {
        this.informPlayer(player, this.permissionDenied);
    }

    protected String getEntityName(Entity entity) {
        if (entity instanceof Player) {
            return "player." + ((Player) entity).getName();
        } else if (entity instanceof Wolf) {
            Wolf wolf = (Wolf) entity;

            if (!wolf.isTamed()) {
                return "animal.wolf.untamed";
            } else if (wolf.getOwner() instanceof Player) {
                return "animal.wolf." + ((Player) wolf.getOwner()).getName();
            } else {
                return "animal.wolf";
            }
        }

        String entityName = entity.getClass().getSimpleName();

        if (entityName.startsWith("Craft")) {
            entityName = entityName.substring(5).toLowerCase();
        }

        if (Monster.class.isAssignableFrom(entity.getClass())) {
            entityName = "monster." + entityName;
        } else if (Animals.class.isAssignableFrom(entity.getClass())) {
            entityName = "animal." + entityName;
        }

        return entityName;
    }

    protected boolean canInteractWithMaterial(Player player, String basePermission, Material type) {
        if (permissionsManager.has(player, basePermission + type.getId())) {
            return true;
        }

        if (permissionsManager.has(player, basePermission + type.name().toLowerCase().replace("_", ""))) {
            return true;
        }

        return false;
    }

    private void registerEvents(Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (Method method : this.getClass().getMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) {
                continue;
            }

            EventHandler handler = method.getAnnotation(EventHandler.class);

            if (method.isAnnotationPresent(Toggleable.class)) {
                Toggleable toggle = method.getAnnotation(Toggleable.class);
                if (!config.getBoolean(toggle.value(), toggle.byDefault())) {
                    continue;
                }
            }

            pluginManager.registerEvent(handler.value(), this, this.getEventExecutor(method), Event.Priority.Normal, plugin);
        }
    }

    private EventExecutor getEventExecutor(final Method eventHandlerMethod) {
        return new EventExecutor() {

            @Override
            public void execute(Listener listener, Event event) {
                try {
                    eventHandlerMethod.invoke(listener, event);
                } catch (Exception e) {
                    Logger.getLogger("Minecraft").warning("[Modifyworld] Failed to execute Modifyworld event handler");
                    e.printStackTrace();
                }
            }
        };
    }
}
