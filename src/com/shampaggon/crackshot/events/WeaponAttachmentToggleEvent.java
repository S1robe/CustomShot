package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public class WeaponAttachmentToggleEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private final Player player;
    private final String weaponTitle;
    private final ItemStack item;
    private int toggleDelay;
    private boolean cancelled;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponAttachmentToggleEvent(final Player player, final String weaponTitle, final ItemStack item, final int toggleDelay) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.item = item;
        this.toggleDelay = toggleDelay;
    }
    
    public ItemStack getItemStack() {
        return this.item;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public int getToggleDelay() {
        return this.toggleDelay;
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
    
    public void setToggleDelay(final int toggleDelay) {
        this.toggleDelay = toggleDelay;
    }
    
    public HandlerList getHandlers() {
        return WeaponAttachmentToggleEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponAttachmentToggleEvent.handlers;
    }
}
