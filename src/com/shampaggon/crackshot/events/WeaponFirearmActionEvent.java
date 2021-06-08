package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponFirearmActionEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final String weaponTitle;
    private double speed;
    private final boolean reload;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponFirearmActionEvent(final Player player, final String weaponTitle, final boolean reload) {
        this.speed = 1.0;
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.reload = reload;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public double getSpeed() {
        return this.speed;
    }
    
    public boolean isReload() {
        return this.reload;
    }
    
    public void setSpeed(double speed) {
        if (speed < 0.0) {
            speed = 0.0;
        }
        this.speed = speed;
    }
    
    public HandlerList getHandlers() {
        return WeaponFirearmActionEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponFirearmActionEvent.handlers;
    }
}
