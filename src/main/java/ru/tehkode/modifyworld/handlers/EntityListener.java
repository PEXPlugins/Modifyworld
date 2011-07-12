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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.ConfigurationNode;
import ru.tehkode.modifyworld.EventHandler;
import ru.tehkode.modifyworld.ModifyworldListener;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

/**
 *
 * @author t3hk0d3
 */
public class EntityListener extends ModifyworldListener {

    public EntityListener(Plugin plugin, ConfigurationNode config) {
        super(plugin, config);
    }

    protected boolean canMessWithEntity(Player player, String basePermission, Entity entity) {
        if (entity instanceof Player) {
            PermissionUser entityUser = permissionsManager.getUser(((Player) entity).getName());

            if (entityUser == null) {
                return false;
            }

            for (PermissionGroup group : entityUser.getGroups()) {
                if (permissionsManager.has(player, basePermission + "group." + group.getName())) {
                    return true;
                }
            }

            return permissionsManager.has(player, basePermission + "player." + entityUser.getName());
        }

        return permissionsManager.has(player, basePermission + getEntityName(entity));
    }

    @EventHandler(Type.ENTITY_DAMAGE)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) { // player is damager
            EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
            if (!(edbe.getDamager() instanceof Player)) { // not caused by player
                return;
            }

            Player player = (Player) edbe.getDamager();
            if (!canMessWithEntity(player, "modifyworld.entity.damage.deal.", event.getEntity())) {
                informPlayerAboutDenial(player);
                event.setCancelled(true);
            }
        } else if (event.getEntity() instanceof Player) { // player are been damaged by someone
            Player player = (Player) event.getEntity();
            if (!canMessWithEntity(player, "modifyworld.entity.damage.take.", event.getEntity())) {
                informPlayerAboutDenial(player);
                event.setCancelled(true);
                event.setDamage(0);
            }
        }
    }

    @EventHandler(Type.ENTITY_TARGET)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            if (!permissionsManager.has(player, "modifyworld.entity.mobtarget." + getEntityName(event.getEntity()))) {
                event.setCancelled(true);
            }
        }
    }
}
