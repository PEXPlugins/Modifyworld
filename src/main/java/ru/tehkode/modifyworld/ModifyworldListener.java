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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author t3hk0d3
 */
public abstract class ModifyworldListener implements Listener {

	protected PlayerInformer informer;
	protected ConfigurationSection config;
	protected boolean informPlayers = false;
	protected boolean useMaterialNames = true;
	protected boolean checkMetadata = false;
	protected boolean checkItemUse = false;
	protected boolean enableWhitelist = false;

	public ModifyworldListener(Plugin plugin, ConfigurationSection config, PlayerInformer informer) {
		this.informer = informer;
		this.config = config;

		this.registerEvents(plugin);

		this.informPlayers = config.getBoolean("informPlayers", informPlayers);
		this.useMaterialNames = config.getBoolean("use-material-names", useMaterialNames);
		this.checkMetadata = config.getBoolean("check-metadata", checkMetadata);
		this.checkItemUse = config.getBoolean("item-use-check", checkItemUse);
		this.enableWhitelist = config.getBoolean("whitelist", enableWhitelist);
	}

	private String getEntityName(Entity entity) {

		if (entity instanceof ComplexEntityPart) {
			return getEntityName(((ComplexEntityPart) entity).getParent());
		}

		String entityName = formatEnumString(entity.getType().toString());

		if (entity instanceof Item) {
			entityName = getItemPermission(((Item) entity).getItemStack());
		}

		if (entity instanceof Player) {
			return "player." + ((Player) entity).getName();
		} else if (entity instanceof Tameable) {
			Tameable animal = (Tameable) entity;

			return "animal." + entityName + (animal.isTamed() ? "." + animal.getOwner().getName() : "");
		}


		EntityCategory category = EntityCategory.fromEntity(entity);

		if (category == null) {
			return entityName; // category unknown (ender crystal)
		}

		return category.getNameDot() + entityName;
	}
	
	private String getInventoryTypePermission(InventoryType type) {
		return formatEnumString(type.name());
	}

	// Functional programming fuck yeah
	private String getMaterialPermission(Material type) {
		return this.useMaterialNames ? formatEnumString(type.name()) : Integer.toString(type.getId());
	}

	private String getMaterialPermission(Material type, byte metadata) {
		return getMaterialPermission(type) + (metadata > 0 ? ":" + metadata : "");
	}

	private String getBlockPermission(Block block) {
		return getMaterialPermission(block.getType(), block.getData());
	}

	public String getItemPermission(ItemStack item) {
		return getMaterialPermission(item.getType(), item.getData().getData());
	}

	/*
	protected boolean permissionDenied(Player player, String basePermission, Entity entity) {
		if (entity instanceof Player && PermissionsEx.isAvailable()) {
			PermissionUser entityUser = PermissionsEx.getUser((Player)entity);

			for (PermissionGroup group : entityUser.getGroups()) {
				if (permissionDenied(player, basePermission, "group", group.getName())) {
					return true;
				}
			}

			return permissionDenied(player, basePermission, "player", entityUser.getName());
		}

		return permissionDenied(player, basePermission, entity);
	}
	*/

	protected boolean permissionDenied(Player player, String basePermission, Object... arguments) {
		String permission = assemblePermission(basePermission, arguments);
		boolean isDenied = !player.hasPermission(permission);

		if (isDenied) {
			this.informer.informPlayer(player, permission, arguments);
		}

		return isDenied;
	}

	protected boolean _permissionDenied(Player player, String permission, Object... arguments) {
		return !player.hasPermission(assemblePermission(permission, arguments));
	}

	protected String assemblePermission(String permission, Object... arguments) {
		StringBuilder builder = new StringBuilder(permission);

		if (arguments != null) {
			for (Object obj : arguments) {
				if (obj == null) {
					continue;
				}

				builder.append('.');
				builder.append(getObjectPermission(obj));
			}
		}

		return builder.toString();
	}

	protected String getObjectPermission(Object obj) {
		if (obj instanceof Entity) {
			return (getEntityName((Entity) obj));
		} else if (obj instanceof EntityType) {
			return formatEnumString(((EntityType)obj).name());
		} else if (obj instanceof BlockState) {
			return (getBlockPermission(((BlockState)obj).getBlock()));
		} else if (obj instanceof ItemStack) {
			return (getItemPermission((ItemStack) obj));
		} else if (obj instanceof Material) {
			return (getMaterialPermission((Material) obj));
		} else if (obj instanceof Block) {
			return (getBlockPermission((Block) obj));
		} else if (obj instanceof InventoryType) {
			return getInventoryTypePermission((InventoryType)obj);
		}

		return (obj.toString());
	}

	private void registerEvents(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	private String formatEnumString(String enumName) {
		return enumName.toLowerCase().replace("_", "");
	}
}
