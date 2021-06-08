package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponReloadCompleteEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final String weaponTitle;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponReloadCompleteEvent(final Player player, final String weaponTitle) {
        this.player = player;
        this.weaponTitle = weaponTitle;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public HandlerList getHandlers() {
        return WeaponReloadCompleteEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponReloadCompleteEvent.handlers;
    }
}
