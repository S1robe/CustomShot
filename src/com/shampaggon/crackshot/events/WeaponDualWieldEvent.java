package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.inventory.*;
import org.bukkit.entity.*;

public class WeaponDualWieldEvent extends Event
{
    private static final HandlerList handlers;
    private boolean dualWield;
    private final ItemStack item;
    private final Player player;
    private final String weaponTitle;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponDualWieldEvent(final Player player, final String weaponTitle, final ItemStack item, final boolean dualWield) {
        this.dualWield = dualWield;
        this.item = item;
        this.player = player;
        this.weaponTitle = weaponTitle;
    }
    
    public ItemStack getItemStack() {
        return this.item;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public boolean isDualWield() {
        return this.dualWield;
    }
    
    public void setDualWield(final boolean dualWield) {
        this.dualWield = dualWield;
    }
    
    public HandlerList getHandlers() {
        return WeaponDualWieldEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponDualWieldEvent.handlers;
    }
}
