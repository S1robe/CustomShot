package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponShootEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final Entity objProj;
    private final String weaponTitle;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponShootEvent(final Player player, final Entity objProj, final String weaponTitle) {
        this.player = player;
        this.objProj = objProj;
        this.weaponTitle = weaponTitle;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Entity getProjectile() {
        return this.objProj;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public HandlerList getHandlers() {
        return WeaponShootEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponShootEvent.handlers;
    }
}
