/*
 * Modifyworld - PermissionsEx ruleset plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.modifyworld;

import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
 * @author t3hk0d3
 */
public abstract class ModifyworldListener implements Listener {

    public final static String PERMISSION_DENIED = "Sorry, you don't have enough permissions";
    protected String permissionDenied = PERMISSION_DENIED;
    protected PermissionManager permissionsManager;
    protected ConfigurationNode config;
    protected boolean informPlayers = false;
    protected boolean useMaterialNames = true;
    protected boolean checkMetadata = false;

    public ModifyworldListener(Plugin plugin, ConfigurationNode config) {
        this.permissionsManager = PermissionsEx.getPermissionManager();
        this.config = config;

        this.registerEvents(plugin);

        this.informPlayers = config.getBoolean("informPlayers", informPlayers);
        this.permissionDenied = config.getString("messages.permissionDenied", this.permissionDenied);
        this.useMaterialNames = config.getBoolean("use-material-names", useMaterialNames);
        this.checkMetadata = config.getBoolean("check-metadata", checkMetadata);
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

            if (wolf.isTamed() && wolf.getOwner() instanceof Player) {
                return "animal.wolf." + ((Player) wolf.getOwner()).getName();
            } else {
                return "animal.wolf";
            }
        }
        
        // Fixtures for Bukkit dev lazyness
        if (entity instanceof Ghast){
            return "monster.ghast";
        }
        
        if (entity instanceof Squid){
            return "animal.squid";
        }
        
        if (entity instanceof Slime){
            return "monster.slime";
        }
        
        
        String entityName = entity.getClass().getSimpleName();

        if (entityName.startsWith("Craft")) {
            entityName = entityName.substring(5).toLowerCase();
        }

        if (Monster.class.isAssignableFrom(entity.getClass())) {
            entityName = "monster." + entityName;
        } else if (Animals.class.isAssignableFrom(entity.getClass())) {
            entityName = "animal." + entityName;
        } else if (Projectile.class.isAssignableFrom(entity.getClass())) {
            entityName = "projectile." + entityName;
        }
        
        return entityName;
    }

    protected String getMaterialPermission(String basePermission, Material type){
        return basePermission + (this.useMaterialNames ? type.name().toLowerCase().replace("_", "") : type.getId() );
    }
    
    protected boolean canInteractWithMaterial(Player player, String basePermission, Material type) {
        return permissionsManager.has(player,  this.getMaterialPermission(basePermission, type));
    }
    
    protected boolean canInteractWithBlock(Player player, String basePermission, Block block) {
        String permission = this.getMaterialPermission(basePermission, block.getType());
        
        if (this.checkMetadata) {
            permission += "." + block.getData();
        }
                
        return permissionsManager.has(player, permission);
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
                    Logger.getLogger("Minecraft").warning("[Modifyworld] Failed to execute Modifyworld event handler for Event." + event.getEventName());
                    e.printStackTrace();
                }
            }
        };
    }
}
