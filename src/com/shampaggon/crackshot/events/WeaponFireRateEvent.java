package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public class WeaponFireRateEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final String weaponTitle;
    private final ItemStack item;
    private int fireRate;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponFireRateEvent(final Player player, final String weaponTitle, final ItemStack item, final int fireRate) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.item = item;
        this.fireRate = fireRate;
    }
    
    public int getFireRate() {
        return this.fireRate;
    }
    
    public void setFireRate(final int fireRate) {
        if (fireRate <= 0 || fireRate > 16) {
            throw new IllegalArgumentException("Fire rate not in range [1..16]: " + fireRate);
        }
        this.fireRate = fireRate;
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
    
    public HandlerList getHandlers() {
        return WeaponFireRateEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponFireRateEvent.handlers;
    }
}
