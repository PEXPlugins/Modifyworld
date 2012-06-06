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
package ru.tehkode.modifyworld.bukkit;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.modifyworld.ModifyworldListener;
import ru.tehkode.modifyworld.handlers.*;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 *
 * @author t3hk0d3
 */
public class Modifyworld extends JavaPlugin {
	
	protected final static Logger logger = Logger.getLogger("Minecraft");
	protected final static Class<? extends ModifyworldListener>[] LISTENERS = new Class[]{
		PlayerListener.class,
		EntityListener.class,
		BlockListener.class,
		VehicleListener.class
	};
	protected List<ModifyworldListener> listeners = new ArrayList<ModifyworldListener>();
	
	public Modifyworld() {
	}
	
	@Override
	public void onEnable() {
		// At first check PEX existance
		if (!PermissionsEx.isAvailable()) {
			logger.severe("[Modifyworld] PermissionsEx not found, disabling");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		
		FileConfiguration config = this.getConfig();
		
		if (!config.isBoolean("enable")) { // Migrate
			this.initializeConfiguration(config);
		}
		
		
		if (config.getBoolean("enable", false)) {
			this.registerListeners();
			logger.info("[Modifyworld] Modifyworld enabled!");
		} else {
			logger.info("[Modifyworld] Modifyworld disabled. Check config.yml!");
			this.getPluginLoader().disablePlugin(this);
		}
		
		this.saveConfig();
	}
	
	@Override
	public void onDisable() {
		this.listeners.clear();
		
		logger.info("[Modifyworld] Modifyworld disabled!");
	}
	
	protected void initializeConfiguration(FileConfiguration config) {
		// At migrate and setup defaults
		PermissionsEx pex = (PermissionsEx) this.getServer().getPluginManager().getPlugin("PermissionsEx");
		
		Configuration pexConfig = pex.getConfig();

		// Flags
		config.set("enable", pexConfig.get("permissions.modifyworld.enabled", false));
		config.set("itemRestrictions", pexConfig.getBoolean("permissions.modifyworld.itemRestrictions", false));
		config.set("informPlayers", pexConfig.getBoolean("permissions.informplayers.modifyworld", false));
		config.set("whitelist", pexConfig.getBoolean("permissions.modifyworld.whitelist", false));
		config.set("use-material-names", pexConfig.getBoolean("permissions.modifyworld.use-material-names", true));
		config.set("drop-restricted-item", pexConfig.getBoolean("permissions.modifyworld.drop-restricted-item", false));
		config.set("item-use-check", pexConfig.getBoolean("permissions.modifyworld.item-use-check", false));

		// Messages
		config.set("messages.whitelistMessage", pexConfig.getString("permissions.modifyworld.whitelistMessage", PlayerListener.WHITELIST_MESSAGE));
		config.set("messages.prohibitedItem", PlayerListener.PROHIBITED_ITEM);
		config.set("messages.permissionDenied", ModifyworldListener.PERMISSION_DENIED);
		
	}
	
	protected void registerListeners() {
		for (Class listenerClass : LISTENERS) {
			try {
				Constructor constructor = listenerClass.getConstructor(Plugin.class, ConfigurationSection.class);
				ModifyworldListener listener = (ModifyworldListener) constructor.newInstance(this, this.getConfig());
				this.listeners.add(listener);
			} catch (Throwable e) {
				logger.warning("[Modifyworld] Failed to initialize \"" + listenerClass.getName() + "\" listener");
				e.printStackTrace();
			}
		}
	}
}
