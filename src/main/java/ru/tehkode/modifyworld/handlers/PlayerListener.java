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
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.ConfigurationNode;
import ru.tehkode.modifyworld.EventHandler;
import ru.tehkode.modifyworld.ModifyworldListener;
import ru.tehkode.modifyworld.Toggleable;
import ru.tehkode.permissions.PermissionUser;

/**
 *
 * @author t3hk0d3
 */
public class PlayerListener extends ModifyworldListener {

    public final static String WHITELIST_MESSAGE = "You are not allowed to join this server. Goodbye!";
    public final static String PROHIBITED_ITEM = "You have prohibited item \"%s\".";
    protected boolean checkInventory = false;
    protected String whitelistKickMessage = WHITELIST_MESSAGE;
    protected String prohibitedItemMessage = PROHIBITED_ITEM;

    public PlayerListener(Plugin plugin, ConfigurationNode config) {
        super(plugin, config);

        this.whitelistKickMessage = config.getString("messages.whitelist", this.whitelistKickMessage);
        this.prohibitedItemMessage = config.getString("messages.prohibitedItem", this.prohibitedItemMessage);
        this.checkInventory = config.getBoolean("itemRestrictions", this.checkInventory);
    }

    @EventHandler(Type.PLAYER_PRELOGIN)
    @Toggleable("whitelist")
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {
        PermissionUser user = this.permissionsManager.getUser(event.getName());

        if (user != null && !user.has("modifyworld.login", Bukkit.getServer().getWorlds().get(0).getName())) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_WHITELIST, whitelistKickMessage);
            Logger.getLogger("Minecraft").info("Player \"" + user.getName() + "\" were kicked by Modifyworld - lack of modifyworld.login permission");
        }
    }

    @EventHandler(Type.PLAYER_LOGIN)
    @Toggleable("whitelist")
    public void onPlayerLogin(PlayerLoginEvent event) {
        PermissionUser user = this.permissionsManager.getUser(event.getPlayer());

        if (user != null && !user.has("modifyworld.login", Bukkit.getServer().getWorlds().get(0).getName())) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, whitelistKickMessage);
            event.getPlayer().kickPlayer(whitelistKickMessage);
            Logger.getLogger("Minecraft").info("Player \"" + user.getName() + "\" were kicked by Modifyworld - lack of modifyworld.login permission");
        }
    }

    @EventHandler(Type.PLAYER_BED_ENTER)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (!permissionsManager.has(event.getPlayer(), "modifyworld.usebeds")) {
            informPlayer(event.getPlayer(), ChatColor.RED + "Sorry, you don't have enough permissions");
            event.setCancelled(true);
        }
    }

    @EventHandler(Type.PLAYER_BUCKET_EMPTY)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        String bucketName = event.getBucket().toString().toLowerCase().replace("_bucket", ""); // WATER_BUCKET -> water
        if (!permissionsManager.has(event.getPlayer(), "modifyworld.bucket.empty." + bucketName)) {
            informPlayerAboutDenial(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(Type.PLAYER_BUCKET_FILL)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        String materialName = event.getBlockClicked().getType().toString().toLowerCase().replace("stationary_", ""); // STATIONARY_WATER -> water
        if (!permissionsManager.has(event.getPlayer(), "modifyworld.bucket.fill." + materialName)) {
            informPlayerAboutDenial(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(Type.PLAYER_COMMAND_PREPROCESS)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/tell") && !permissionsManager.has(event.getPlayer(), "modifyworld.chat.private")) {
            informPlayerAboutDenial(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(Type.PLAYER_CHAT)
    public void onPlayerChat(PlayerChatEvent event) {
        if (!permissionsManager.has(event.getPlayer(), "modifyworld.chat")) {
            informPlayerAboutDenial(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(Type.PLAYER_PICKUP_ITEM)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!canInteractWithMaterial(event.getPlayer(), "modifyworld.items.pickup.", event.getItem().getItemStack().getType())) {
            event.setCancelled(true);
        }

        this.checkPlayerInventory(event.getPlayer());
    }

    @EventHandler(Type.PLAYER_DROP_ITEM)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!canInteractWithMaterial(event.getPlayer(), "modifyworld.items.drop.", event.getItemDrop().getItemStack().getType())) {
            informPlayerAboutDenial(event.getPlayer());
            event.setCancelled(true);
        }

        this.checkPlayerInventory(event.getPlayer());
    }

    @EventHandler(Type.PLAYER_INVENTORY)
    public void onInventoryOpen(PlayerInventoryEvent event) {
        this.checkPlayerInventory(event.getPlayer());
    }

    @EventHandler(Type.PLAYER_ITEM_HELD)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        this.checkPlayerInventory(event.getPlayer());
    }

    @EventHandler(Type.PLAYER_INTERACT_ENTITY)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!permissionsManager.has(event.getPlayer(), "modifyworld.entity.interact." + getEntityName(event.getRightClicked()))) {
            event.setCancelled(true);
            informPlayerAboutDenial(event.getPlayer());
        }
    }

    @EventHandler(Type.PLAYER_INTERACT)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!canInteractWithMaterial(event.getPlayer(), "modifyworld.blocks.interact.", event.getClickedBlock().getType())) {
            informPlayerAboutDenial(event.getPlayer());
            event.setCancelled(true);
        }
    }

    protected void checkPlayerInventory(Player player) {
        if (!checkInventory) {
            return;
        }

        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.getContents()) {
            if (stack != null && !permissionsManager.has(player, "modifyworld.items.have." + stack.getTypeId())) {
                inventory.remove(stack);
                informPlayer(player, String.format(this.prohibitedItemMessage, stack.getType().name()));
            }
        }
    }
}
