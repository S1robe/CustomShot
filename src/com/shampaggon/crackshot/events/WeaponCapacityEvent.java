package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public class WeaponCapacityEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final String weaponTitle;
    private final ItemStack item;
    private int capacity;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponCapacityEvent(final Player player, final String weaponTitle, final ItemStack item, final int capacity) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.item = item;
        this.capacity = capacity;
    }
    
    public int getCapacity() {
        return this.capacity;
    }
    
    public ItemStack getItemStack() {
        return this.item;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public void setCapacity(final int capacity) {
        this.capacity = ((capacity < 1) ? 1 : capacity);
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public HandlerList getHandlers() {
        return WeaponCapacityEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponCapacityEvent.handlers;
    }
}
