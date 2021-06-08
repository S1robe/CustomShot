package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponPreShootEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private final Player player;
    private final String weaponTitle;
    private String soundsShoot;
    private double bulletSpread;
    private final boolean isLeftClick;
    private boolean cancelled;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponPreShootEvent(final Player player, final String weaponTitle, final String soundsShoot, final double bulletSpread, final boolean isLeftClick) {
        this.player = player;
        this.weaponTitle = weaponTitle;
        this.soundsShoot = soundsShoot;
        this.bulletSpread = bulletSpread;
        this.isLeftClick = isLeftClick;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public double getBulletSpread() {
        return this.bulletSpread;
    }
    
    public String getSounds() {
        return this.soundsShoot;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public boolean isLeftClick() {
        return this.isLeftClick;
    }
    
    public void setBulletSpread(final double bulletSpread) {
        this.bulletSpread = Math.abs(bulletSpread);
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public void setSounds(final String soundsShoot) {
        this.soundsShoot = soundsShoot;
    }
    
    public HandlerList getHandlers() {
        return WeaponPreShootEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponPreShootEvent.handlers;
    }
}
