package ru.tehkode.modifyworld;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.HashMap;
import java.util.Map;

public class PlayerInformer {

	public final static String PERMISSION_DENIED = "Sorry, you don't have enough permissions";
	public final static String DEFAULT_MESSAGE_FORMAT = "&f[&2Modifyworld&f]&4 %s";
	// Default message format
	protected String messageFormat = DEFAULT_MESSAGE_FORMAT;
	protected Map<String, String> messages = new HashMap<String, String>();
	// Flags
	protected boolean enabled = false;
	protected boolean individualMessages = false;
	protected String defaultMessage = PERMISSION_DENIED;

	public PlayerInformer(ConfigurationSection config) {
		this.enabled = config.getBoolean("inform-players", enabled);

		this.loadConfig(config.getConfigurationSection("messages"));
	}

	private void loadConfig(ConfigurationSection config) {

		this.defaultMessage = config.getString("default-message", this.defaultMessage);
		this.messageFormat = config.getString("message-format", this.messageFormat);
		this.individualMessages = config.getBoolean("individual-messages", this.individualMessages);

		this.importMessages(config);

		for (String permission : config.getKeys(true)) {
			if (!config.isString(permission)) {
				continue;
			}

			setMessage(permission, config.getString(permission.replace("/", ".")));
		}
	}

	public void setMessage(String permission, String message) {
		messages.put(permission, message);
	}

	public String getMessage(String permission) {
		if (messages.containsKey(permission)) {
			return messages.get(permission);
		}

		String perm = permission;
		int index;

		while ((index = perm.lastIndexOf(".")) != -1) {
			perm = perm.substring(0, index);

			if (messages.containsKey(perm)) {
				String message = messages.get(perm);
				messages.put(permission, message);
				return message;
			}
		}

		return this.defaultMessage;
	}

	public String getMessage(Player player, String permission) {
		String message = null;
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
			message = getMessageVault(player, permission);
		}
		if (message == null) {
			try {
				Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx");
				message = getMessagePEX(player, permission);
			} catch (ClassNotFoundException ignore) {
			}
		}

		if (message != null) {
			return message;
		}

		return getMessage(permission);
	}

	public String getMessagePEX(Player player, String permission) {
		if (PermissionsEx.isAvailable()) {
			PermissionUser user = PermissionsEx.getUser(player);

			String message;
			String perm = permission;
			int index;

			while ((index = perm.lastIndexOf(".")) != -1) {
				perm = perm.substring(0, index);

				message = user.getOption("permission-denied-" + perm, player.getWorld().getName(), null);
				if (message == null) {
					continue;
				}

				return message;
			}

			message = user.getOption("permission-denied", player.getWorld().getName(), null);

			if (message != null) {
				return message;
			}
		}
		return null;
	}

	private String getMessageVault(Player player, String permission) {
		Chat chat = Bukkit.getServer().getServicesManager().load(Chat.class);
		if (chat != null) {
			String message;
			String perm = permission;
			int index;

			while ((index = perm.lastIndexOf(".")) != -1) {
				perm = perm.substring(0, index);

				message = chat.getPlayerInfoString(player.getWorld(), player.getName(), "permission-denied-" + perm, null);
				if (message == null) {
					continue;
				}

				return message;
			}

			message = chat.getPlayerInfoString(player.getWorld(), player.getName(), "permission-denied", null);
			if (message != null) {
				return message;
			}
		}
		return null;
	}

	public void informPlayer(Player player, String permission, Object... args) {
		if (!enabled) {
			return;
		}

		String message = getMessage(player, permission).replace("$permission", permission);

		for (int i = 0; i < args.length; i++) {
			message = message.replace("$" + (i + 1), describeObject(args[i]));
		}

		if (message != null && !message.isEmpty()) {
			player.sendMessage(String.format(messageFormat, message).replaceAll("&([a-z0-9])", "\u00A7$1"));
		}
	}

	protected String describeObject(Object obj) {
		if (obj instanceof ComplexEntityPart) { // Complex entities
			return describeObject(((ComplexEntityPart) obj).getParent());
		} else if (obj instanceof Item) { // Dropped items
			return describeMaterial(((Item) obj).getItemStack().getType());
		} else if (obj instanceof ItemStack) { // Items
			return describeMaterial(((ItemStack) obj).getType());
		} else if (obj instanceof Entity) { // Entities
			return ((Entity) obj).getType().toString().toLowerCase().replace("_", " ");
		} else if (obj instanceof Block) { // Blocks
			return describeMaterial(((Block) obj).getType());
		} else if (obj instanceof Material) { // Just material
			return describeMaterial((Material) obj);
		}

		return obj.toString();
	}

	private String describeMaterial(Material material) {
		// TODO: implement data id

		if (material == Material.INK_SACK) {
			return "dye";
		}

		return material.toString().toLowerCase().replace("_", " ");
	}

	// For backward compatibility
	private void importMessages(ConfigurationSection config) {
		// This should NOT be refactored, because it would be stupid :D
		if (config.isString("whitelistMessage")) {
			setMessage("modifyworld.login", config.getString("whitelistMessage"));
			config.set("whitelistMessage", null);
		}

		if (config.isString("prohibitedItem")) {
			setMessage("modifyworld.items.have", config.getString("prohibitedItem"));
			config.set("prohibitedItem", null);
		}

		if (config.isString("permissionDenied")) {
			setMessage("modifyworld", config.getString("permissionDenied"));
			config.set("permissionDenied", null);
		}
	}
}
