/*
 * PermissionsEx - Permissions plugin for Bukkit
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
package ru.tehkode.modifyworld.bukkit;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import ru.tehkode.modifyworld.ModifyworldListener;
import ru.tehkode.modifyworld.handlers.BlockListener;
import ru.tehkode.modifyworld.handlers.EntityListener;
import ru.tehkode.modifyworld.handlers.PlayerListener;
import ru.tehkode.modifyworld.handlers.VehicleListener;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 *
 * @author t3hk0d3
 */
public class Modifyworld extends JavaPlugin {

    protected final static Logger logger = Logger.getLogger("Minecraft");
    protected final static Class<? extends ModifyworldListener>[] LISTENERS = new Class[]{
        PlayerListener.class, EntityListener.class, BlockListener.class, VehicleListener.class
    };
    protected List<ModifyworldListener> listeners = new ArrayList<ModifyworldListener>();

    public Modifyworld() {
    }

    @Override
    public void onEnable() {
        // At first check PEX existance
        try {
            PermissionsEx.getPermissionManager();
        } catch (Throwable e) {
            logger.severe("[Modifyworld] PermissionsEx not found, disabling");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        Configuration config = this.getConfiguration();

        if (config.getProperty("enabled") == null) { // Migrate
            this.initializeConfiguration(config);
        }


        if (config.getBoolean("enable", false)) {
            logger.info("[Modifyworld] Modifyworld enabled!");
            this.registerListeners();
        }

        config.save();
    }

    @Override
    public void onDisable() {
        logger.info("[Modifyworld] Modifyworld disabled!");
    }

    protected void initializeConfiguration(Configuration config) {
        // At migrate and setup defaults
        PermissionsEx pex = (PermissionsEx) this.getServer().getPluginManager().getPlugin("PermissionsEx");

        ru.tehkode.permissions.config.Configuration pexConfig = pex.getConfig();

        // Flags
        config.setProperty("enable", pexConfig.getBoolean("permissions.modifyworld.enabled", false));
        config.setProperty("itemRestrictions", pexConfig.getBoolean("permissions.modifyworld.itemRestrictions", false));
        config.setProperty("informPlayers", pexConfig.getBoolean("permissions.informplayers.modifyworld", false));
        config.setProperty("whitelist", pexConfig.getBoolean("permissions.modifyworld.whitelist", false));

        // Messages
        config.setProperty("message.whitelistMessage", pexConfig.getString("permissions.modifyworld.whitelistMessage", PlayerListener.WHITELIST_MESSAGE));
        config.setProperty("message.prohibitedItem", PlayerListener.PROHIBITED_ITEM);
        config.setProperty("message.permissionDenied", ModifyworldListener.PERMISSION_DENIED);

    }

    protected void registerListeners() {
        for (Class listenerClass : LISTENERS) {
            try {
                Constructor constructor = listenerClass.getConstructor(Plugin.class, ConfigurationNode.class);
                ModifyworldListener listener = (ModifyworldListener) constructor.newInstance(this, this.getConfiguration());
                this.listeners.add(listener);
            } catch (Throwable e) {
                logger.warning("[Modifyworld] Failed to initialize \"" + listenerClass.getName() + "\" listener");
                e.printStackTrace();
            }
        }
    }
}
