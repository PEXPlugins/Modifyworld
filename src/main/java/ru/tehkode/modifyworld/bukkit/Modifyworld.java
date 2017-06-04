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

import java.io.InputStreamReader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.modifyworld.ModifyworldListener;
import ru.tehkode.modifyworld.PlayerInformer;
import ru.tehkode.modifyworld.handlers.BlockListener;
import ru.tehkode.modifyworld.handlers.EntityListener;
import ru.tehkode.modifyworld.handlers.PlayerListener;
import ru.tehkode.modifyworld.handlers.VehicleListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author t3hk0d3
 */
public class Modifyworld extends JavaPlugin {

	protected final static Class<? extends ModifyworldListener>[] LISTENERS = new Class[]{
		PlayerListener.class,
		EntityListener.class,
		BlockListener.class,
		VehicleListener.class
	};
	protected List<ModifyworldListener> listeners = new ArrayList<ModifyworldListener>();
	protected PlayerInformer informer;
	protected File configFile;
	protected FileConfiguration config;

	@Override
	public void onLoad() {
		configFile = new File(this.getDataFolder(), "config.yml");
	}

	@Override
	public void onEnable() {
		this.config = this.getConfig();

		if (!config.isConfigurationSection("messages")) {
			this.getLogger().severe("Deploying default config");
			this.initializeConfiguration(config);
		}

		this.informer = new PlayerInformer(config);

		this.registerListeners();
		this.getLogger().info("Modifyworld enabled!");

		this.saveConfig();
	}

	@Override
	public void onDisable() {
		this.listeners.clear();
		this.config = null;

		this.getLogger().info("Modifyworld successfully disabled!");
	}

	protected void initializeConfiguration(FileConfiguration config) {
		// Flags
		config.set("item-restrictions", false);
		config.set("inform-players", true);
		config.set("whitelist", false);
		config.set("use-material-names", true);
		config.set("drop-restricted-item", false);
		config.set("item-use-check", false);
		config.set("check-metadata", true);
	}

	protected void registerListeners() {
		for (Class listenerClass : LISTENERS) {
			try {
				Constructor constructor = listenerClass.getConstructor(Plugin.class, ConfigurationSection.class, PlayerInformer.class);
				ModifyworldListener listener = (ModifyworldListener) constructor.newInstance(this, this.getConfig(), this.informer);
				this.listeners.add(listener);
			} catch (Throwable e) {
				this.getLogger().warning("Failed to initialize \"" + listenerClass.getName() + "\" listener");
				e.printStackTrace();
			}
		}
	}

    public InputStream getLocalizedResource(String path) {
        return getLocalizedResource(path, Locale.getDefault());
    }

    public InputStream getLocalizedResource(String path, Locale locale) {
        InputStream ret;
        ret = getResource("lang/" + locale.toString() + "/" + path); // Country-specific
        if (ret == null && !locale.getCountry().isEmpty()) { // Available without country-specific variant
            ret = getResource("lang/" + locale.getLanguage() + "/" + path);
        }
        if (ret == null) { // Unlocalized
            ret = getResource(path);
        }
        return ret;
    }

    private YamlConfiguration loadBaseLanguage(String path, Locale locale) throws IOException, InvalidConfigurationException {
        InputStream load = getResource("lang/" + locale.getLanguage() + "/" + path);
        if (load != null) {
            YamlConfiguration conf = new YamlConfiguration();
            conf.options().copyDefaults(true);
            conf.load(new InputStreamReader(load));
            YamlConfiguration def = loadUnlocalized(path);
            if (def != null) {
                conf.setDefaults(def);
            }
            return conf;
        }
        return null;
    }

    private YamlConfiguration loadUnlocalized(String path) throws IOException, InvalidConfigurationException {
        InputStream load = getResource(path);
        if (load != null) {
            YamlConfiguration conf = new YamlConfiguration();
            conf.load(new InputStreamReader(load));
            return conf;
        }
        return null;
    }

    public YamlConfiguration getLocalizedConfig(String path) throws InvalidConfigurationException, IOException {
        return getLocalizedConfig(path, Locale.getDefault());
    }

    public YamlConfiguration getLocalizedConfig(String path, Locale locale) throws InvalidConfigurationException, IOException {
        YamlConfiguration base = new YamlConfiguration();
        InputStream load =  getResource("lang/" + locale.toString() + "/" + path); // Country-specific
        if (load != null) {
            base.load(new InputStreamReader(load));
            base.options().copyDefaults(true);
            YamlConfiguration def = loadBaseLanguage(path, locale);
            if (def == null) {
                def = loadUnlocalized(path);
            }
            if (def != null) {
                base.setDefaults(def);
            }
        } else {
            base = loadBaseLanguage(path, locale);
            if (base == null) {
                base = loadUnlocalized(path);
            }
        }
        return base;
    }

	@Override
	public FileConfiguration getConfig() {
		if (this.config == null) {
			this.reloadConfig();
		}

		return this.config;
	}

	@Override
	public void saveConfig() {
		try {
			this.config.save(configFile);
		} catch (IOException e) {
			this.getLogger().severe("Failed to save configuration file: " + e.getMessage());
		}
	}

	@Override
	public void reloadConfig() {
		this.config = new YamlConfiguration();
		config.options().pathSeparator('/');

		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			this.getLogger().severe("Configuration file not found - deploying default one");
			InputStream defConfigStream = getLocalizedResource("config.yml");
			if (defConfigStream != null) {
				try {
					this.config.load(new InputStreamReader(defConfigStream));
				} catch (Exception de) {
					this.getLogger().severe("Default config file is broken. Please tell this to Modifyworld author.");
				}
			}
		} catch (Exception e) {
			this.getLogger().severe("Failed to load configuration file: " + e.getMessage());
		}

		InputStream defConfigStream = getLocalizedResource("config.yml");
		if (defConfigStream != null) {
			this.config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
		}
	}
}
