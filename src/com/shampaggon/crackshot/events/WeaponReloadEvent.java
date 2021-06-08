package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponReloadEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final String weaponTitle;
    private String soundsReload;
    private double reloadSpeed;
    private int reloadDuration;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponReloadEvent(final Player player, final String weaponTitle, final String reloadSounds, final int reloadDuration) {
        this.reloadSpeed = 1.0;
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.soundsReload = reloadSounds;
        this.reloadDuration = reloadDuration;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getSounds() {
        return this.soundsReload;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public int getReloadDuration() {
        return this.reloadDuration;
    }
    
    public double getReloadSpeed() {
        return this.reloadSpeed;
    }
    
    public void setReloadSpeed(double reloadSpeed) {
        if (reloadSpeed < 0.0) {
            reloadSpeed = 0.0;
        }
        this.reloadSpeed = reloadSpeed;
    }
    
    public void setReloadDuration(final int reloadDuration) {
        this.reloadDuration = reloadDuration;
    }
    
    public void setSounds(final String soundsReload) {
        this.soundsReload = soundsReload;
    }
    
    public HandlerList getHandlers() {
        return WeaponReloadEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponReloadEvent.handlers;
    }
}
