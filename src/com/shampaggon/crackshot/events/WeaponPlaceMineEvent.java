package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponPlaceMineEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final Entity mine;
    private final String weaponTitle;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponPlaceMineEvent(final Player player, final Entity mine, final String weaponTitle) {
        this.player = player;
        this.mine = mine;
        this.weaponTitle = weaponTitle;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Entity getMine() {
        return this.mine;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public HandlerList getHandlers() {
        return WeaponPlaceMineEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponPlaceMineEvent.handlers;
    }
}
