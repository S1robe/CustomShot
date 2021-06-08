package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponScopeEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private final Player player;
    private final String weaponTitle;
    private final boolean zoomIn;
    private boolean cancelled;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponScopeEvent(final Player player, final String weaponTitle, final boolean zoomIn) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.zoomIn = zoomIn;
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
    
    public boolean isZoomIn() {
        return this.zoomIn;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public HandlerList getHandlers() {
        return WeaponScopeEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponScopeEvent.handlers;
    }
}
