package com.shampaggon.crackshot;

import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.projectiles.*;
import org.bukkit.plugin.*;
import org.bukkit.metadata.*;
import org.bukkit.entity.*;

public class CSUtility
{
    private final CSDirector classOne;
    private final CSMinion classTwo;
    
    public CSUtility() {
        this.classOne = (CSDirector)Bukkit.getServer().getPluginManager().getPlugin("CrackShot");
        this.classTwo = this.classOne.csminion;
    }
    
    public boolean giveWeapon(final Player receiver, final String weaponTitle, final int amount) {
        boolean success = false;
        if (receiver != null && receiver.getInventory().firstEmpty() != -1) {
            this.classTwo.getWeaponCommand(receiver, weaponTitle, false, String.valueOf(amount), true, true);
            success = true;
        }
        return success;
    }
    
    public ItemStack generateWeapon(final String weaponTitle) {
        return this.classTwo.vendingMachine(weaponTitle);
    }
    
    public void generateExplosion(final Player player, final Location loc, final String weaponTitle) {
        this.classOne.projectileExplosion(null, weaponTitle, false, player, false, true, null, loc.getBlock(), true, 0);
    }
    
    public void spawnMine(final Player player, final Location loc, final String weaponTitle) {
        this.classOne.deployMine(player, weaponTitle, loc);
    }
    
    public void setProjectile(final Player player, final Projectile proj, final String weaponTitle) {
        if (player != null) {
            final EntityType projType = proj.getType();
            switch (projType) {
                case ARROW:
                case SNOWBALL:
                case FIREBALL:
                case WITHER_SKULL:
                case EGG: {
                    proj.setShooter(player);
                    proj.setMetadata("projParentNode", new FixedMetadataValue(this.classOne, weaponTitle));
                    break;
                }
            }
        }
    }
    
    public String getWeaponTitle(final ItemStack item) {
        if (item == null) {
            return null;
        }
        final String[] weaponInfo = this.classOne.itemParentNode(item, null);
        return (weaponInfo == null) ? null : weaponInfo[0];
    }
    
    public String getWeaponTitle(final Projectile proj) {
        return (proj != null && proj.hasMetadata("projParentNode")) ? proj.getMetadata("projParentNode").get(0).asString() : null;
    }
    
    public String getWeaponTitle(final TNTPrimed tnt) {
        return (tnt != null && tnt.hasMetadata("CS_potex")) ? tnt.getMetadata("CS_potex").get(0).asString() : null;
    }
    
    public CSDirector getHandle() {
        return this.classOne;
    }
}
