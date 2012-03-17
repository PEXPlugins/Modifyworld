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

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.tehkode.modifyworld.ModifyworldListener;
import ru.tehkode.permissions.PermissionUser;

/**
 *
 * @author t3hk0d3
 */
public class PlayerListener extends ModifyworldListener {

	public final static String WHITELIST_MESSAGE = "You are not allowed to join this server. Goodbye!";
	public final static String PROHIBITED_ITEM = "You have prohibited item \"%s\".";
	protected boolean checkInventory = false;
	protected boolean dropRestrictedItem = false;
	protected String whitelistKickMessage = WHITELIST_MESSAGE;
	protected String prohibitedItemMessage = PROHIBITED_ITEM;

	public PlayerListener(Plugin plugin, ConfigurationSection config) {
		super(plugin, config);

		this.whitelistKickMessage = config.getString("messages.whitelistMessage", this.whitelistKickMessage);
		this.prohibitedItemMessage = config.getString("messages.prohibitedItem", this.prohibitedItemMessage);
		this.checkInventory = config.getBoolean("itemRestrictions", this.checkInventory);
		this.dropRestrictedItem = config.getBoolean("drop-restricted-item", this.dropRestrictedItem);

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		if (event.isSneaking() && !permissionsManager.has(event.getPlayer(), "modifyworld.sneak")) {
			event.setCancelled(true);
			event.getPlayer().setSneaking(false);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerSprint(PlayerToggleSprintEvent event) {
		if (event.isSprinting() && !permissionsManager.has(event.getPlayer(), "modifyworld.sprint")) {
			event.setCancelled(true);
			event.getPlayer().setSprinting(false);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerPreLogin(PlayerPreLoginEvent event) {
		if (!enableWhitelist) {
			return;
		}

		PermissionUser user = this.permissionsManager.getUser(event.getName());

		if (user != null && !user.has("modifyworld.login", Bukkit.getServer().getWorlds().get(0).getName())) {
			event.disallow(PlayerPreLoginEvent.Result.KICK_WHITELIST, whitelistKickMessage);
			Logger.getLogger("Minecraft").info("Player \"" + user.getName() + "\" were kicked by Modifyworld - lack of modifyworld.login permission");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!enableWhitelist) {
			return;
		}

		PermissionUser user = this.permissionsManager.getUser(event.getPlayer());

		if (user != null && !user.has("modifyworld.login", Bukkit.getServer().getWorlds().get(0).getName())) {
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, whitelistKickMessage);
			//event.getPlayer().kickPlayer(whitelistKickMessage);
			Logger.getLogger("Minecraft").info("Player \"" + user.getName() + "\" were kicked by Modifyworld - lack of modifyworld.login permission");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		if (!permissionsManager.has(event.getPlayer(), "modifyworld.usebeds")) {
			informPlayer(event.getPlayer(), ChatColor.RED + "Sorry, you don't have enough permissions");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		String bucketName = event.getBucket().toString().toLowerCase().replace("_bucket", ""); // WATER_BUCKET -> water
		if (!permissionsManager.has(event.getPlayer(), "modifyworld.bucket.empty." + bucketName)) {
			informPlayerAboutDenial(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		String materialName = event.getBlockClicked().getType().toString().toLowerCase().replace("stationary_", ""); // STATIONARY_WATER -> water
		if (!permissionsManager.has(event.getPlayer(), "modifyworld.bucket.fill." + materialName)) {
			informPlayerAboutDenial(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().startsWith("/tell") && !permissionsManager.has(event.getPlayer(), "modifyworld.chat.private")) {
			informPlayerAboutDenial(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(PlayerChatEvent event) {
		if (!permissionsManager.has(event.getPlayer(), "modifyworld.chat")) {
			informPlayerAboutDenial(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (!canInteractWithItem(event.getPlayer(), "modifyworld.items.pickup.", event.getItem().getItemStack())) {
			event.setCancelled(true);
		}

		this.checkPlayerInventory(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (!canInteractWithItem(event.getPlayer(), "modifyworld.items.drop.", event.getItemDrop().getItemStack())) {
			informPlayerAboutDenial(event.getPlayer());
			event.setCancelled(true);
		}

		this.checkPlayerInventory(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInventoryOpen(PlayerInventoryEvent event) {
		this.checkPlayerInventory(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onItemHeldChange(PlayerItemHeldEvent event) {
		this.checkPlayerInventory(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (this.checkItemUse) {
			if (!permissionsManager.has(event.getPlayer(), "modifyworld.item.use." + getItemPermission(event.getPlayer().getItemInHand()) + ".on.entity." + getEntityName(event.getRightClicked()))) {
				event.setCancelled(true);
				informPlayerAboutDenial(event.getPlayer());
			}

			return;
		}

		if (!event.isCancelled() && !permissionsManager.has(event.getPlayer(), "modifyworld.interact." + getEntityName(event.getRightClicked()))) {
			event.setCancelled(true);
			informPlayerAboutDenial(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();

		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) { // item restriction check
			this.checkPlayerInventory(event.getPlayer());
		}

		if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK && action != Action.PHYSICAL) {
			return;
		}

		if (this.checkItemUse && action != Action.PHYSICAL) {
			if (!permissionsManager.has(event.getPlayer(), "modifyworld.item.use." + getItemPermission(event.getPlayer().getItemInHand()) + ".on.block." + getBlockPermission(event.getClickedBlock()))) {
				event.setCancelled(true);
				informPlayerAboutDenial(event.getPlayer());
			}

			return;
		}

		if (!event.isCancelled() && !canInteractWithBlock(event.getPlayer(), "modifyworld.blocks.interact.", event.getClickedBlock())) {
			informPlayerAboutDenial(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		Player player = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;

		if (player == null) {
			return;
		}

		if (!permissionsManager.has(player, "modifyworld.digestion")) {
			event.setCancelled(true);
		}
	}

	protected void checkPlayerInventory(Player player) {
		if (!checkInventory) {
			return;
		}

		Inventory inventory = player.getInventory();
		for (ItemStack stack : inventory.getContents()) {
			if (stack != null && !canInteractWithItem(player, "modifyworld.items.have.", stack)) {
				inventory.remove(stack);

				if (this.dropRestrictedItem) {
					player.getWorld().dropItemNaturally(player.getLocation(), stack);
				}

				informPlayer(player, String.format(this.prohibitedItemMessage, stack.getType().name()));
			}
		}
	}
}
