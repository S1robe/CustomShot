package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.block.*;

public class WeaponHitBlockEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final Entity objProj;
    private final String weaponTitle;
    private final Block hitBlock;
    private final Block airBlock;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponHitBlockEvent(final Player player, final Entity objProj, final String weaponTitle, final Block hitBlock, final Block airBlock) {
        this.player = player;
        this.objProj = objProj;
        this.weaponTitle = weaponTitle;
        this.hitBlock = hitBlock;
        this.airBlock = airBlock;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Entity getProjectile() {
        return this.objProj;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public Block getBlock() {
        return this.hitBlock;
    }
    
    public Block getAirBlock() {
        return this.airBlock;
    }
    
    public HandlerList getHandlers() {
        return WeaponHitBlockEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponHitBlockEvent.handlers;
    }
}
