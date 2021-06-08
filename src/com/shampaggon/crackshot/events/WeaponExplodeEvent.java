package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.*;

public class WeaponExplodeEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final Location location;
    private final String weaponTitle;
    private final boolean isSplit;
    private final boolean isAirstrike;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponExplodeEvent(final Player player, final Location location, final String weaponTitle, final boolean isSplit, final boolean isAirstrike) {
        this.player = player;
        this.location = location;
        this.weaponTitle = weaponTitle;
        this.isSplit = isSplit;
        this.isAirstrike = isAirstrike;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Location getLocation() {
        return this.location;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public boolean isSplit() {
        return this.isSplit;
    }
    
    public boolean isAirstrike() {
        return this.isAirstrike;
    }
    
    public HandlerList getHandlers() {
        return WeaponExplodeEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponExplodeEvent.handlers;
    }
}
