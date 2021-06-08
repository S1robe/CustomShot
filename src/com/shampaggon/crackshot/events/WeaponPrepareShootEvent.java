package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponPrepareShootEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private final Player player;
    private final String weaponTitle;
    private boolean cancelled;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponPrepareShootEvent(final Player player, final String weaponTitle) {
        this.player = player;
        this.weaponTitle = weaponTitle;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public HandlerList getHandlers() {
        return WeaponPrepareShootEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponPrepareShootEvent.handlers;
    }
}
