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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
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
	protected ConfigurationSection config;
	protected boolean informPlayers = false;
	protected boolean useMaterialNames = true;
	protected boolean checkMetadata = false;
	protected boolean checkItemUse = false;
	protected boolean enableWhitelist = false;
	
	public ModifyworldListener(Plugin plugin, ConfigurationSection config) {
		this.permissionsManager = PermissionsEx.getPermissionManager();
		this.config = config;

		this.registerEvents(plugin);

		this.informPlayers = config.getBoolean("informPlayers", informPlayers);
		this.permissionDenied = config.getString("messages.permissionDenied", this.permissionDenied);
		this.useMaterialNames = config.getBoolean("use-material-names", useMaterialNames);
		this.checkMetadata = config.getBoolean("check-metadata", checkMetadata);
		this.checkItemUse = config.getBoolean("item-use-check", checkItemUse);
		this.enableWhitelist = config.getBoolean("whitelist", enableWhitelist);
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

		String entityName = entity.toString().substring(5).toLowerCase();
		EntityCategory category = EntityCategory.fromEntity(entity);

		if (category == null) {
			return entityName; // category unknown (ender crystal)
		}

		return category.getNameDot() + entityName;
	}

	// Functional programming fuck yeah
	protected String getMaterialPermission(Material type) {
		return this.useMaterialNames ? type.name().toLowerCase().replace("_", "") : Integer.toString(type.getId());
	}
	
	protected String getMaterialPermission(Material type, byte metadata) {
		return this.getMaterialPermission(type) + (metadata > 0 ? ":" + metadata : "");
	}
	
	protected String getItemPermission(ItemStack item) {
		return this.getMaterialPermission(item.getType(), item.getData().getData());
	}
	
	protected String getBlockPermission(Block block) {
		return this.getMaterialPermission(block.getType(), block.getData());
	}

	protected boolean canInteractWithMaterial(Player player, String basePermission, Material type) {
		return permissionsManager.has(player, basePermission + this.getMaterialPermission(type));
	}

	protected boolean canInteractWithItem(Player player, String basePermission, ItemStack item) {
		return permissionsManager.has(player, basePermission + this.getMaterialPermission(item.getType(), item.getData().getData()));
	}

	protected boolean canInteractWithBlock(Player player, String basePermission, Block block) {
		return permissionsManager.has(player, basePermission + this.getMaterialPermission(block.getType(), block.getData()));
	}

	private void registerEvents(Plugin plugin) {
		PluginManager pluginManager = plugin.getServer().getPluginManager();
		pluginManager.registerEvents(this, plugin);
	}
}
