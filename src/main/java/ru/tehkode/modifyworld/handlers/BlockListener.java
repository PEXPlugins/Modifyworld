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
package ru.tehkode.modifyworld.handlers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.plugin.Plugin;
import ru.tehkode.modifyworld.ModifyworldListener;
import ru.tehkode.modifyworld.PlayerInformer;

/**
 *
 * @author t3hk0d3
 */
public class BlockListener extends ModifyworldListener {

	public BlockListener(Plugin plugin, ConfigurationSection config, PlayerInformer informer) {
		super(plugin, config, informer);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {
		if (permissionDenied(event.getPlayer(), "modifyworld.blocks.destroy", event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (permissionDenied(event.getPlayer(), "modifyworld.blocks.place", event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		if (event.getRemover() instanceof Player
				&& permissionDenied((Player) event.getRemover(), "modifyworld.blocks.destroy", event.getEntity().getType())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPaintingPlace(HangingPlaceEvent event) {
		if (permissionDenied(event.getPlayer(), "modifyworld.blocks.place", event.getEntity().getType())) {
			event.setCancelled(true);
		}
	}
}
