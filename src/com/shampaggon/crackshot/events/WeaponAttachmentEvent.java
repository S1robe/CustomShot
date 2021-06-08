package com.shampaggon.crackshot.events;

import org.bukkit.event.*;
import org.bukkit.inventory.*;

public class WeaponAttachmentEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private final String weaponTitle;
    private final ItemStack item;
    private String attachment;
    private boolean cancelled;
    
    static {
        handlers = new HandlerList();
    }
    
    public WeaponAttachmentEvent(final String weaponTitle, final ItemStack item, final String attachment) {
        this.weaponTitle = weaponTitle;
        this.item = item;
        this.attachment = attachment;
    }
    
    public String getAttachment() {
        return this.attachment;
    }
    
    public ItemStack getItemStack() {
        return this.item;
    }
    
    public void setAttachment(final String attachment) {
        this.attachment = attachment;
    }
    
    public String getWeaponTitle() {
        return this.weaponTitle;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public HandlerList getHandlers() {
        return WeaponAttachmentEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return WeaponAttachmentEvent.handlers;
    }
}
