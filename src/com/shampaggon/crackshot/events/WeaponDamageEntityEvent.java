package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;

public class WeaponDamageEntityEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private final Player player;
    private final Entity victim;
    private final Entity dmgSource;
    private final String weaponTitle;
    private double totalDmg;
    private final boolean headShot;
    private final boolean backStab;
    private final boolean critHit;
    private boolean cancelled;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponDamageEntityEvent(final Player player, final Entity victim, final Entity dmgSource, final String weaponTitle, final double totalDmg, final boolean headShot, final boolean backStab, final boolean critHit) {
        this.player = player;
        this.victim = victim;
        this.dmgSource = dmgSource;
        this.weaponTitle = weaponTitle;
        this.totalDmg = totalDmg;
        this.headShot = headShot;
        this.backStab = backStab;
        this.critHit = critHit;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Entity getVictim() {
        return this.victim;
    }
    
    public Entity getDamager() {
        return this.dmgSource;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public double getDamage() {
        return this.totalDmg;
    }
    
    public boolean isHeadshot() {
        return this.headShot;
    }
    
    public boolean isBackstab() {
        return this.backStab;
    }
    
    public boolean isCritical() {
        return this.critHit;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public void setDamage(final double totalDmg) {
        this.totalDmg = totalDmg;
    }
    
    public HandlerList getHandlers() {
        return WeaponDamageEntityEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponDamageEntityEvent.handlers;
    }
}
