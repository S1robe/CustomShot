package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponTriggerEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private final Player player;
    private final LivingEntity victim;
    private final String weaponTitle;
    private boolean cancelled;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponTriggerEvent(final Player player, final LivingEntity victim, final String weaponTitle) {
        this.player = player;
        this.victim = victim;
        this.weaponTitle = weaponTitle;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public LivingEntity getVictim() {
        return this.victim;
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
        return WeaponTriggerEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponTriggerEvent.handlers;
    }
}
