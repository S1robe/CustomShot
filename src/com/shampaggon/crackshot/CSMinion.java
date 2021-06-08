package com.shampaggon.crackshot;

import org.bukkit.inventory.*;
import org.bukkit.util.*;
import org.bukkit.util.Vector;
import org.bukkit.configuration.file.*;
import java.io.*;
import org.bukkit.configuration.*;
import org.bukkit.enchantments.*;
import org.bukkit.plugin.*;
import org.bukkit.metadata.*;
import org.bukkit.permissions.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.*;
import org.bukkit.event.*;
import com.shampaggon.crackshot.events.*;
import org.bukkit.entity.*;
import org.bukkit.block.*;
import java.util.*;
import org.bukkit.command.*;
import org.bukkit.*;

public class CSMinion
{
    private final CSDirector plugin;
    public String heading;
    
    public CSMinion(final CSDirector plugin) {
        this.heading = "�f[�cCrackshot�f] �a- ";
        this.plugin = plugin;
    }
    
    public void clearRecipes() {
        try {
            for (final String parent_node : this.plugin.parentlist.values()) {
                if (this.plugin.getBoolean(parent_node + ".Crafting.Enable")) {
                    final ItemStack weapon = this.vendingMachine(parent_node);
                    for (final Recipe rec : this.plugin.getServer().getRecipesFor(weapon)) {
                        final Iterator<Recipe> it = this.plugin.getServer().recipeIterator();
                        while (it.hasNext()) {
                            if (it.next().getResult().isSimilar(rec.getResult())) {
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            }
        }
        catch (UnsupportedOperationException ex) {}
    }
    
    public void customRecipes() {
        for (final String parent_node : this.plugin.parentlist.values()) {
            if (this.plugin.getBoolean(parent_node + ".Crafting.Enable")) {
                final boolean shaped = this.plugin.getBoolean(parent_node + ".Crafting.Shaped");
                final int quantity = this.plugin.getInt(parent_node + ".Crafting.Quantity");
                final String ingredients = this.plugin.getString(parent_node + ".Crafting.Ingredients");
                final String[] args = ingredients.split(",");
                final ItemStack weapon = this.vendingMachine(parent_node);
                if (quantity > 1) {
                    weapon.setAmount(quantity);
                }
                if (shaped) {
                    if (args.length == 9) {
                        final ShapedRecipe recipe = new ShapedRecipe(weapon);
                        recipe.shape("ABC", "DEF", "GHI");
                        for (int c = 65, i = 0; c < 74; ++c, ++i) {
                            final ItemStack tomatoes = this.parseItemStack(args[i]);
                            if (tomatoes != null && tomatoes.getType() != Material.AIR) {
                                recipe.setIngredient((char)c, tomatoes.getType(), tomatoes.getDurability());
                            }
                        }
                        this.plugin.getServer().addRecipe(recipe);
                    }
                    else {
                        System.out.print("[CrackShot] The crafting recipe (" + ingredients + ") of weapon '" + parent_node + "' has " + args.length + " value(s) instead of 9.");
                    }
                }
                else {
                    final ShapelessRecipe recipe2 = new ShapelessRecipe(weapon);
                    for (int j = 0; j < args.length; ++j) {
                        final ItemStack tomatoes2 = this.parseItemStack(args[j]);
                        if (tomatoes2 != null && tomatoes2.getType() != Material.AIR) {
                            recipe2.addIngredient(1, tomatoes2.getType(), tomatoes2.getDurability());
                        }
                    }
                    this.plugin.getServer().addRecipe(recipe2);
                }
            }
        }
    }
    
    public ItemStack vendingMachine(final String parent_node) {
        boolean remote = false;
        boolean trap = false;
        boolean startGiven = false;
        String iName = this.plugin.getString(parent_node + ".Item_Information.Item_Name");
        final String iLore = this.plugin.getString(parent_node + ".Item_Information.Item_Lore");
        final boolean dualWield = this.plugin.getBoolean(parent_node + ".Shooting.Dual_Wield");
        final boolean reload = this.plugin.getBoolean(parent_node + ".Reload.Enable");
        final boolean swap = this.plugin.getBoolean(parent_node + ".Swap");
        final boolean keepUselessTag = !this.plugin.getBoolean(parent_node + ".Item_Information.Remove_Unused_Tag");
        final boolean rdeEnable = this.plugin.getBoolean(parent_node + ".Explosive_Devices.Enable");
        final String rdeInfo = this.plugin.getString(parent_node + ".Explosive_Devices.Device_Type");
        int reloadAmt = this.plugin.getInt(parent_node + ".Reload.Reload_Amount");
        final Integer startAmt = CSDirector.ints.get(parent_node + ".Reload.Starting_Amount");
        final String actType = this.plugin.getString(parent_node + ".Firearm_Action.Type");
        final String itemInfo = this.plugin.getString(parent_node + ".Item_Information.Item_Type");
        final String attachType = this.plugin.getString(parent_node + ".Item_Information.Attachments.Type");
        final String attachInfo = this.plugin.getString(parent_node + ".Item_Information.Attachments.Info");
        final char BLACK_LEFT_TRI = '\u25c0';
        final char WHITE_RIGHT_TRI = '\u25b7';
        final char INFINITY = '\u00d7';
        if (startAmt != null && startAmt <= reloadAmt) {
            startGiven = true;
            if (startAmt < 0) {
                reloadAmt = 0;
            }
            else {
                reloadAmt = startAmt;
            }
        }
        if (actType != null && (actType.equalsIgnoreCase("bolt") || actType.equalsIgnoreCase("lever")) && reloadAmt > reloadAmt - 1) {
            reloadAmt = ((reloadAmt - 1 < 0) ? 0 : (reloadAmt - 1));
        }
        if (rdeInfo != null) {
            if (rdeInfo.equalsIgnoreCase("remote") || rdeInfo.equalsIgnoreCase("itembomb")) {
                remote = true;
            }
            else if (rdeInfo.equalsIgnoreCase("trap")) {
                trap = true;
            }
        }
        if (itemInfo == null) {
            System.out.print("[CrackShot] The weapon '" + parent_node + "' has no value provided for Item_Type!");
            return null;
        }
        final ItemStack sniperID = this.parseItemStack(itemInfo);
        if (sniperID == null) {
            System.out.print("[CrackShot] The weapon '" + parent_node + "' has an invalid value for Item_Type!");
            return null;
        }
        final ItemMeta snipermeta = sniperID.getItemMeta();
        if (reload && !rdeEnable) {
            if (dualWield) {
                iName = iName + " �" + reloadAmt + " | " + reloadAmt + "�";
            }
            else if (attachType != null && attachType.equalsIgnoreCase("main")) {
                final boolean attachRelEnable = this.plugin.getBoolean(attachInfo + ".Reload.Enable");
                int attachRelAmt = this.plugin.getInt(attachInfo + ".Reload.Reload_Amount");
                final Integer attStartAmt = CSDirector.ints.get(attachInfo + ".Reload.Starting_Amount");
                if (attStartAmt != null && attStartAmt <= attachRelAmt) {
                    attachRelAmt = ((attStartAmt < 0) ? 0 : attStartAmt);
                }
                if (attachRelEnable) {
                    iName = iName + " �" + reloadAmt + " " + '\u25c0' + '\u25b7' + " " + attachRelAmt + "�";
                }
                else {
                    iName = iName + " �" + reloadAmt + " " + '\u25c0' + '\u25b7' + " " + '\u00d7' + "�";
                }
            }
            else {
                iName = iName + " �" + reloadAmt + "�";
            }
        }
        else if (remote) {
            String rdeCapacity = "N/A";
            final String deviceInfo = this.plugin.getString(parent_node + ".Explosive_Devices.Device_Info");
            final String[] refinedOre = (rdeInfo.equalsIgnoreCase("itembomb") && deviceInfo != null) ? deviceInfo.split(",") : this.returnRefinedOre(null, parent_node);
            if (refinedOre != null) {
                rdeCapacity = refinedOre[0];
            }
            iName = iName + " �" + rdeCapacity + "�";
        }
        else if (trap) {
            iName = iName + " �?�";
        }
        else if (dualWield) {
            iName = iName + " �" + '\u00d7' + " | " + '\u00d7' + "�";
        }
        else if (attachType != null && attachType.equalsIgnoreCase("main")) {
            final boolean attachRelEnable = this.plugin.getBoolean(attachInfo + ".Reload.Enable");
            int attachRelAmt = this.plugin.getInt(attachInfo + ".Reload.Reload_Amount");
            final Integer attStartAmt = CSDirector.ints.get(attachInfo + ".Reload.Starting_Amount");
            if (attStartAmt != null && attStartAmt <= attachRelAmt) {
                attachRelAmt = ((attStartAmt < 0) ? 0 : attStartAmt);
            }
            if (attachRelEnable) {
                iName = iName + " �" + '\u00d7' + " " + '\u25c0' + '\u25b7' + " " + attachRelAmt + "�";
            }
            else {
                iName = iName + " �" + '\u00d7' + " " + '\u25c0' + '\u25b7' + " " + '\u00d7' + "�";
            }
        }
        else if (keepUselessTag || actType != null) {
            iName = iName + " �" + '\u00d7' + "�";
        }
        if (actType != null && !dualWield) {
            if (actType.equalsIgnoreCase("bolt") || actType.equalsIgnoreCase("lever") || actType.equalsIgnoreCase("pump") || actType.equalsIgnoreCase("slide")) {
                if (startGiven && startAmt < 1) {
                    iName = iName.replaceAll("�", "\u25ab �");
                }
                else {
                    iName = iName.replaceAll("�", "\u25aa �");
                }
            }
            else if (actType.equalsIgnoreCase("revolver") || actType.equalsIgnoreCase("break")) {
                iName = iName.replaceAll("�", "\u25aa �");
            }
        }
        if (iLore != null) {
            final ArrayList<String> lore = new ArrayList<String>();
            final String[] lines = iLore.split("\\|");
            String[] array;
            for (int length = (array = lines).length, i = 0; i < length; ++i) {
                final String line = array[i];
                lore.add(line);
            }
            snipermeta.setLore(lore);
        }
        snipermeta.setDisplayName(iName);
        sniperID.setItemMeta(snipermeta);
        return sniperID;
    }
    
    public String identifyWeapon(final String weapon) {
        String closestParent = null;
        for (final String parentNode : this.plugin.parentlist.values()) {
            if (weapon.equalsIgnoreCase(parentNode)) {
                return parentNode;
            }
            if (closestParent != null) {
                continue;
            }
            if (!parentNode.toUpperCase().startsWith(weapon.toUpperCase())) {
                continue;
            }
            closestParent = parentNode;
        }
        return closestParent;
    }
    
    public void oneTime(final Player player) {
        if (player.getItemInHand().getAmount() == 1) {
            player.getInventory().clear(player.getInventory().getHeldItemSlot());
        }
        else {
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
        }
        this.plugin.unscopePlayer(player);
        player.updateInventory();
    }
    
    public void getWeaponCommand(final Player player, final String weapon, final boolean spawned, final String amount, final boolean given, final boolean byAPI) {
        final String parent_node = this.identifyWeapon(weapon);
        if (parent_node != null) {
            final String attachType = this.plugin.getString(parent_node + ".Item_Information.Attachments.Type");
            if (attachType == null || !attachType.equalsIgnoreCase("accessory")) {
                this.getWeaponHelper(player, parent_node, spawned, amount, given, byAPI);
                return;
            }
        }
        player.sendMessage(this.heading + "No weapon matches '" + weapon + "'.");
    }
    
    public void getWeaponHelper(final Player player, final String parentNode, final boolean spawned, final String amount, final boolean given, final boolean byAPI) {
        if (spawned && !player.hasPermission("crackshot.get." + parentNode) && !player.hasPermission("crackshot.get.all")) {
            player.sendMessage(this.heading + "You do not have permission to get this item.");
            return;
        }
        final ItemStack sniperID = this.vendingMachine(parentNode);
        if (sniperID == null) {
            player.sendMessage(this.heading + "You have failed to provide a value for 'Item_Type'.");
            return;
        }
        int intAmount = 1;
        if (amount != null) {
            try {
                intAmount = Integer.valueOf(amount);
            }
            catch (NumberFormatException ex) {}
        }
        if (intAmount > 64) {
            intAmount = 64;
        }
        if (intAmount < 1) {
            player.sendMessage(this.heading + "'" + intAmount + "' is not a valid amount.");
            return;
        }
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(this.heading + "Your inventory is full.");
            return;
        }
        String multiplier = "";
        if (intAmount > 1) {
            multiplier = " \u2715" + intAmount;
        }
        for (int count = 0; count < intAmount; ++count) {
            player.getInventory().addItem(sniperID);
        }
        String publicName = parentNode;
        if (publicName.length() > 19) {
            publicName = publicName.substring(0, 19) + "...";
        }
        if (spawned) {
            player.sendMessage(this.heading + "Successfully grabbed - " + publicName + multiplier);
        }
        else if (given && !byAPI) {
            final String itemName = this.plugin.getString(parentNode + ".Item_Information.Item_Name");
            CSMessages.sendMessage(player, this.heading, CSMessages.Message.WEAPON_RECEIVED.getMessage(itemName, String.valueOf('\u2715'), intAmount));
        }
        if (!byAPI) {
            this.plugin.playSoundEffects(player, parentNode, ".Item_Information.Sounds_Acquired", false, null);
        }
    }
    
    public Vector getAlignedDirection(final Location locA, final Location locB) {
        final Vector vector = locB.toVector().subtract(locA.toVector()).normalize();
        return vector;
    }
    
    public void loadGeneralConfig() {
        final String path = this.plugin.getDataFolder() + "/general.yml";
        final File tag = new File(path);
        if (!tag.exists()) {
            final File dFile = this.getDefaultConfig("general.yml");
            if (dFile != null) {
                try {
                    dFile.createNewFile();
                }
                catch (IOException ex2) {}
            }
            System.out.print("[CrackShot] General configuration added!");
        }
        if (tag != null) {
            try {
                this.plugin.weaponConfig = YamlConfiguration.loadConfiguration(tag);
                if (this.plugin.weaponConfig.getList("Disabled_Worlds") != null) {
                    this.plugin.disWorlds = this.plugin.weaponConfig.getList("Disabled_Worlds").toArray(new String[] { "0" });
                }
                final ConfigurationSection invCtrl = this.plugin.weaponConfig.getConfigurationSection("Inventory_Control");
                if (invCtrl != null) {
                    for (final String group : invCtrl.getKeys(false)) {
                        CSDirector.ints.put(group + ".Limit", this.plugin.weaponConfig.getInt("Inventory_Control." + group + ".Limit"));
                        CSDirector.strings.put(group + ".Message_Exceeded", this.plugin.weaponConfig.getString("Inventory_Control." + group + ".Message_Exceeded").replace("&", "�"));
                        CSDirector.strings.put(group + ".Sounds_Exceeded", this.plugin.weaponConfig.getString("Inventory_Control." + group + ".Sounds_Exceeded"));
                    }
                }
                CSDirector.bools.put("Merged_Reload.Disable", this.plugin.weaponConfig.getBoolean("Merged_Reload.Disable"));
                CSDirector.strings.put("Merged_Reload.Message_Denied", this.plugin.weaponConfig.getString("Merged_Reload.Message_Denied").replace("&", "�"));
                CSDirector.strings.put("Merged_Reload.Sounds_Denied", this.plugin.weaponConfig.getString("Merged_Reload.Sounds_Denied"));
            }
            catch (Exception ex) {
                System.out.print("[CrackShot] " + tag.getName() + " could not be loaded.");
            }
        }
    }
    
    public void loadMessagesConfig() {
        final String path = this.plugin.getDataFolder() + "/messages.yml";
        final File tag = new File(path);
        if (!tag.exists()) {
            final File dFile = this.getDefaultConfig("messages.yml");
            if (dFile != null) {
                try {
                    dFile.createNewFile();
                }
                catch (IOException ex2) {}
            }
            System.out.print("[CrackShot] Message configuration added!");
        }
        if (tag != null) {
            try {
                final FileConfiguration config = YamlConfiguration.loadConfiguration(tag);
                for (final String key : config.getKeys(true)) {
                    CSMessages.messages.put(key, config.getString(key));
                }
            }
            catch (Exception ex) {
                System.out.print("[CrackShot] " + tag.getName() + " could not be loaded.");
            }
        }
    }
    
    public File getDefaultConfig(final String fileName) {
        final File file = new File(this.plugin.getDataFolder() + "/" + fileName);
        final InputStream inputStream = CSDirector.class.getResourceAsStream("/" + fileName);
        if (inputStream == null) {
            return null;
        }
        try {
            final FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[4096];
            int read = 0;
            while ((read = inputStream.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
            inputStream.close();
            output.close();
            return file;
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public void loadWeapons(final Player player) {
        final String path = this.plugin.getDataFolder() + "/weapons";
        final File tag = new File(path);
        File[] fileList = tag.listFiles();
        if (fileList == null || fileList.length == 0) {
            final String[] specials = { "defaultWeapons.yml", "defaultExplosives.yml", "defaultAttachments.yml" };
            String[] array;
            for (int length = (array = specials).length, i = 0; i < length; ++i) {
                final String spec = array[i];
                final File dFile = this.grabDefaults(spec);
                if (dFile != null) {
                    try {
                        dFile.createNewFile();
                    }
                    catch (IOException ex) {}
                }
            }
            fileList = tag.listFiles();
            System.out.print("[CrackShot] Default weapons added!");
        }
        if (fileList == null) {
            System.out.print("[CrackShot] No weapons were loaded!");
            return;
        }
        File[] array2;
        for (int length2 = (array2 = fileList).length, j = 0; j < length2; ++j) {
            final File file = array2[j];
            if (file.getName().endsWith(".yml")) {
                this.plugin.weaponConfig = this.loadConfig(file, player);
                this.plugin.fillHashMaps(this.plugin.weaponConfig);
            }
        }
        this.completeList();
    }
    
    public File grabDefaults(final String defaultWeap) {
        final File file = new File(this.plugin.getDataFolder() + "/weapons/" + defaultWeap);
        final File directories = new File(this.plugin.getDataFolder() + "/weapons");
        if (!directories.exists()) {
            directories.mkdirs();
        }
        final InputStream inputStream = CSDirector.class.getResourceAsStream("/resources/" + defaultWeap);
        if (inputStream == null) {
            return null;
        }
        try {
            final FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[4096];
            int read = 0;
            while ((read = inputStream.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
            inputStream.close();
            output.close();
            return file;
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public YamlConfiguration loadConfig(final File file, final Player player) {
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        }
        catch (FileNotFoundException ex3) {}
        catch (IOException ex) {
            if (player != null) {
                player.sendMessage(this.heading + "The file '" + file.getName() + "' could not be loaded.");
            }
            ex.printStackTrace();
            if(CSDirector.debug){
            	StackTraceElement[] stack = ex.getStackTrace();
            	for(int i = 0; i < stack.length; i++){
            		player.sendMessage(stack[i].toString());
            	}
            }
        }
        catch (InvalidConfigurationException ex2) {
            if (player != null) {
                player.getWorld().playSound(player.getLocation(), SoundManager.get("GHAST_SCREAM"), 1.0f, 1.0f);
                player.sendMessage(this.heading + "The file '" + file.getName() + "' is incorrectly configured. View the error report in the console and fix it!");
            }
            ex2.printStackTrace();
            if(CSDirector.debug){
                StackTraceElement[] stack2 = ex2.getStackTrace();
                for(int i = 0; i < stack2.length; i++){
                    player.sendMessage(stack2[i].toString());
                }
            }
        }
        return config;
    }
    
    public void completeList() {
        int counter = 1;
        for (final String parent_node : this.plugin.parentlist.values()) {
            final String attachType = this.plugin.getString(parent_node + ".Item_Information.Attachments.Type");
            if (!this.plugin.getBoolean(parent_node + ".Item_Information.Hidden_From_List") && (attachType == null || !attachType.equalsIgnoreCase("accessory"))) {
                this.plugin.wlist.put(counter, parent_node);
                ++counter;
            }
        }
        CSDirector.ints.put("totalPages", (int)Math.ceil((counter - 1) / 18.0));
    }
    
    public void listWeapons(final Player sender, final String[] args) {
        int start = 1;
        int page = 1;
        int finalChapter = this.plugin.getInt("totalPages");
        if (finalChapter == 0) {
            finalChapter = 1;
        }
        if (args.length == 2 && !args[1].equalsIgnoreCase("all")) {
            int pageNumber;
            try {
                pageNumber = Integer.valueOf(args[1]);
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(this.heading + "You have provided an invalid page number.");
                return;
            }
            if (pageNumber < 1) {
                return;
            }
            start = 1 + (pageNumber - 1) * 18;
            page = pageNumber;
            if (start >= finalChapter * 18) {
                start = 1 + (finalChapter - 1) * 18;
            }
            if (page < 1) {
                page = 1;
            }
            else if (page > finalChapter) {
                page = finalChapter;
            }
        }
        int finish = start + 18;
        if (args.length == 2 && args[1].equalsIgnoreCase("all")) {
            finish = finalChapter * 18;
            sender.sendMessage("�b�l\u25ba �7Weapons �5[All pages] �b�l\u25c4");
        }
        else {
            sender.sendMessage("�b�l\ufd3e �cWeapons [Page " + page + "/" + finalChapter + "]:");
        }
        for (int i = start; i < finish; i += 2) {
            final String weapon = this.plugin.wlist.get(i);
            if (weapon == null) {
                break;
            }
            final String weapon2 = this.plugin.wlist.get(i + 1);
            sender.sendMessage(this.makePretty(weapon, weapon2));
        }
    }
    
    public String makePretty(String weapon, String weapon2) {
        weapon = ((weapon.length() > 18) ? (weapon.substring(0, 18) + "...") : weapon);
        final String tripleDot = weapon.replace("...", "O").replace("I", "");
        final int officialLength = weapon.replace("...", "O").length();
        final int count = officialLength - tripleDot.length();
        String padding = "";
        final int spaceLimit = 34 - (officialLength + 1) / 2;
        if (count != 0 && count % 2 != 0) {
            padding = " ";
        }
        for (int a = officialLength + 1; a < spaceLimit + count / 2; ++a) {
            padding = " " + padding;
        }
        if (weapon2 != null) {
            if (weapon2.length() > 18) {
                weapon2 = weapon2.substring(0, 18) + "...";
            }
            weapon = "�5\u00BB �c - �7" + weapon + padding + "�5\u00BB �c - �7" + weapon2;
        }
        else {
            weapon = "�5\u00BB �c - �7" + weapon + padding + "�5\u00BB";
        }
        return weapon;
    }
    
    public void removeEnchantments(final ItemStack item) {
        for (final Enchantment e : item.getEnchantments().keySet()) {
            item.removeEnchantment(e);
        }
    }
    
    public String extractReading(final String name) {
        if (!name.contains("�")) {
            return String.valueOf('\u00d7');
        }
        final String[] nameDigger = name.split("�");
        return nameDigger[1].split("�")[0];
    }
    
    public void replaceBrackets(final ItemStack item, String gapFiller, final String parent_node) {
        final String attachType = this.plugin.getAttachment(parent_node, item)[0];
        try {
            if (attachType != null) {
                final String[] ammoReading = this.extractReading(item.getItemMeta().getDisplayName()).split(" ");
                if (attachType.equalsIgnoreCase("main")) {
                    gapFiller = gapFiller + " " + ammoReading[1] + " " + ammoReading[2];
                }
                else if (attachType.equalsIgnoreCase("accessory")) {
                    gapFiller = ammoReading[0] + " " + ammoReading[1] + " " + gapFiller;
                }
            }
        }
        catch (IndexOutOfBoundsException ex) {
            this.resetItemName(item, parent_node);
            return;
        }
        final String refinedOre = item.getItemMeta().getDisplayName().replaceAll("(?<=�).*?(?=�)", gapFiller);
        this.setItemName(item, refinedOre);
    }
    
    public void resetItemName(final ItemStack item, final String parentNode) {
        final ItemStack correctItem = this.vendingMachine(parentNode);
        this.setItemName(item, correctItem.getItemMeta().getDisplayName());
    }
    
    public void setItemName(final ItemStack item, final String name) {
        final ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        item.setItemMeta(m);
    }
    
    public boolean isHesh(final Projectile proj, final LivingEntity victim, double projSpeed) {
        boolean retVal = false;
        final World regionWorld = victim.getWorld();
        final Location vicEyeLoc = victim.getEyeLocation();
        final Location locOne = new Location(regionWorld, vicEyeLoc.getX() + 0.5, vicEyeLoc.getY() + 0.5, vicEyeLoc.getZ() + 0.5);
        final Location locTwo = new Location(regionWorld, vicEyeLoc.getX() - 0.5, vicEyeLoc.getY() - 0.5, vicEyeLoc.getZ() - 0.5);
        if (projSpeed > 256.0) {
            projSpeed = 256.0;
        }
        for (double i = 0.0; i <= projSpeed; i += 0.8) {
            final Location finalLoc = proj.getLocation();
            final Vector direction = proj.getVelocity().normalize();
            direction.multiply(i);
            finalLoc.add(direction);
            if (this.isInsideCuboid(finalLoc, locOne, locTwo, regionWorld)) {
                retVal = true;
                break;
            }
        }
        return retVal;
    }
    
    public boolean durabilityCheck(final String item) {
        final String[] list = { "346", "398", "359" };
        for (int i = 256; i <= 259; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }
        for (int i = 267; i <= 279; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }
        for (int i = 283; i <= 286; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }
        for (int i = 290; i <= 294; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }
        for (int i = 298; i <= 317; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }
        String[] array;
        for (int length = (array = list).length, j = 0; j < length; ++j) {
            final String it = array[j];
            if (item.contains(it)) {
                return true;
            }
        }
        return false;
    }
    
    public void projectileLightning(final Location loc, final boolean zapNoDam) {
        if (zapNoDam) {
            loc.getWorld().strikeLightningEffect(loc);
        }
        else {
            loc.getWorld().strikeLightning(loc);
        }
    }
    
    public void explosionPackage(final LivingEntity victim, final String parent_node, final Player player) {
        if (parent_node != null) {
            String vicName = victim.getType().getName();
            final String shooterName = (player == null) ? "<shooter>" : player.getName();
            final boolean spawnedEnts = this.plugin.spawnEntities(victim, parent_node, ".Spawn_Entity_On_Hit.EntityType_Baby_Explode_Amount", player);
            this.givePotionEffects(victim, parent_node, ".Explosions.Explosion_Potion_Effect", "explosion");
            final int inc = this.plugin.getInt(parent_node + ".Explosions.Ignite_Victims");
            if (inc != 0) {
                victim.setFireTicks(inc);
            }
            this.plugin.playSoundEffects(victim, parent_node, ".Explosions.Sounds_Victim", false, null);
            if (victim == player) {
                return;
            }
            if (victim instanceof Player) {
                if (spawnedEnts) {
                    this.plugin.sendPlayerMessage(victim, parent_node, ".Spawn_Entity_On_Hit.Message_Victim", shooterName, vicName, "<flight>", "<damage>");
                }
                vicName = victim.getName();
                this.plugin.sendPlayerMessage(victim, parent_node, ".Explosions.Message_Victim", shooterName, vicName, "<flight>", "<damage>");
            }
            if (player != null) {
                if (spawnedEnts) {
                    this.plugin.sendPlayerMessage(player, parent_node, ".Spawn_Entity_On_Hit.Message_Shooter", shooterName, vicName, "<flight>", "<damage>");
                }
                this.plugin.sendPlayerMessage(player, parent_node, ".Explosions.Message_Shooter", shooterName, vicName, "<flight>", "<damage>");
                this.plugin.playSoundEffects(player, parent_node, ".Explosions.Sounds_Shooter", false, null);
            }
        }
    }
    
    public void callAndResponse(final Player victim, final Player fisherman, final Vehicle vehicle, final String[] mineInfo, final boolean shot) {
        if (victim.hasMetadata("CS_trigDelay")) {
            return;
        }
        if (fisherman == null) {
            if (vehicle == null) {
                this.detonateRDE(fisherman, victim, mineInfo, false);
            }
            else {
                this.mineAction(vehicle, mineInfo, fisherman, shot, null, victim);
            }
            return;
        }
        victim.setMetadata("CS_trigDelay", new FixedMetadataValue(this.plugin, false));
        this.tempVars(victim, "CS_trigDelay", 200L);
        victim.setMetadata("CS_singed", new FixedMetadataValue(this.plugin, false));
        this.illegalSlap(fisherman, victim, 0);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                if (victim.hasMetadata("CS_singed") && victim.getMetadata("CS_singed").get(0).asBoolean()) {
                    victim.removeMetadata("CS_singed", CSMinion.this.plugin);
                    victim.removeMetadata("CS_trigDelay", CSMinion.this.plugin);
                    if (vehicle == null) {
                        CSMinion.this.detonateRDE(fisherman, victim, mineInfo, false);
                    }
                    else {
                        CSMinion.this.mineAction(vehicle, mineInfo, fisherman, shot, victim.getName(), victim);
                    }
                }
            }
        }, 1L);
    }
    
    public void reseatTag(final Item item) {
        if (item.getVehicle() instanceof Entity) {
            return;
        }
        for (final Entity veh : item.getNearbyEntities(1.0, 10.0, 1.0)) {
            if (veh instanceof Minecart && !(veh.getPassenger() instanceof Entity)) {
                veh.setPassenger(item);
                break;
            }
        }
    }
    
    public void reseatTag(final Vehicle vehicle) {
        if (vehicle.getPassenger() instanceof Entity) {
            return;
        }
        for (final Entity ent : vehicle.getNearbyEntities(1.0, 10.0, 1.0)) {
            if (ent instanceof Item && !(ent.getVehicle() instanceof Entity)) {
                final ItemStack itemFuse = ((Item)ent).getItemStack();
                if (this.plugin.itemIsSafe(itemFuse) && itemFuse.getItemMeta().getDisplayName().startsWith("�cS3AGULLL~")) {
                    vehicle.setPassenger(ent);
                    break;
                }
                continue;
            }
        }
    }
    
    public void mineAction(final Vehicle vehicle, final String[] mineInfo, final Player fisherman, final boolean shot, final String vicName, final Entity victim) {
        if (fisherman != null && vicName != null) {
            this.plugin.sendPlayerMessage(fisherman, mineInfo[2], ".Explosive_Devices.Message_Trigger_Placer", mineInfo[1], vicName, "<flight>", "<damage>");
            this.plugin.playSoundEffects(fisherman, mineInfo[2], ".Explosive_Devices.Sounds_Alert_Placer", false, null);
        }
        if (victim instanceof Player && !mineInfo[1].equals(victim.getName())) {
            this.plugin.sendPlayerMessage((LivingEntity)victim, mineInfo[2], ".Explosive_Devices.Message_Trigger_Victim", mineInfo[1], vicName, "<flight>", "<damage>");
        }
        this.plugin.projectileExplosion(vehicle, mineInfo[2], shot, fisherman, true, false, null, null, false, 0);
        if (!shot) {
            this.plugin.playSoundEffects(vehicle, mineInfo[2], ".Explosive_Devices.Sounds_Trigger", false, null);
        }
        vehicle.getPassenger().remove();
    }
    
    public void tempVars(final Player player, final String metaData, final Long delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                player.removeMetadata(metaData, CSMinion.this.plugin);
            }
        }, delay);
    }
    
    public void illegalSlap(final Player player, final LivingEntity victim, final int dmg) {
        final PermissionAttachment attachment = player.addAttachment(this.plugin);
        attachment.setPermission("nocheatplus", true);
        attachment.setPermission("anticheat.check.exempt", true);
        victim.damage(dmg, player);
        player.removeAttachment(attachment);
    }
    
    public String[] fastenSeatbelts(final Item psngr) {
        if (this.plugin.itemIsSafe(psngr.getItemStack())) {
            final String itemName = psngr.getItemStack().getItemMeta().getDisplayName();
            if (itemName.contains("�cS3AGULLL")) {
                return itemName.split("~");
            }
        }
        return null;
    }
    
    public boolean bombIsInvalid(final Player player, final String[] deviceInfo, final String parentNode) {
        boolean retVal = false;
        final String debugMsg = this.heading + "The 'Device_Info' node of the weapon " + parentNode;
        String debugEnd = null;
        if (deviceInfo == null || deviceInfo.length != 4) {
            debugEnd = " is incorrectly formatted.";
            retVal = true;
        }
        else if (this.parseItemStack(deviceInfo[2]) == null) {
            debugEnd = " contains the value '" + deviceInfo[2] + "', which is not a valid item ID.";
            retVal = true;
        }
        else if (this.parseItemStack(deviceInfo[3]) == null) {
            debugEnd = " contains the value '" + deviceInfo[3] + "', which is not a valid item ID.";
            retVal = true;
        }
        else {
            try {
                if (Integer.valueOf(deviceInfo[0]) <= 0) {
                    debugEnd = " contains the value '" + deviceInfo[0] + "', which is not a number greater than 0.";
                    retVal = true;
                }
                Double.valueOf(deviceInfo[1]);
            }
            catch (NumberFormatException ex) {
                debugEnd = " contains an invalid number.";
                retVal = true;
            }
        }
        if (retVal) {
            player.sendMessage(debugMsg + debugEnd);
        }
        return retVal;
    }
    
    public String[] returnRefinedOre(final Player player, final String parent_node) {
        final String rdeOre = this.plugin.getString(parent_node + ".Explosive_Devices.Device_Info");
        final boolean playerExists = player != null;
        String msgToSend = null;
        Label_0299: {
            if (rdeOre != null) {
                final String[] rdeRefined = rdeOre.split("-");
                if (rdeRefined.length == 3) {
                    try {
                        if (Integer.valueOf(rdeRefined[0]) < 1) {
                            msgToSend = "'" + rdeRefined[0] + "' in '" + rdeOre + "' of weapon '" + parent_node + "' must be a positive number.";
                            break Label_0299;
                        }
                        if (rdeRefined[1].length() != 2) {
                            msgToSend = "'" + rdeRefined[1] + "' in '" + rdeOre + "' of weapon '" + parent_node + "' must be 2 characters long, not " + rdeRefined[1].length() + ".";
                            break Label_0299;
                        }
                        return rdeRefined;
                    }
                    catch (NumberFormatException ex) {
                        msgToSend = "'" + rdeRefined[0] + "' in '" + rdeOre + "' of weapon '" + parent_node + "' is not a valid number.";
                        break Label_0299;
                    }
                }
                msgToSend = "'" + rdeOre + "' of weapon '" + parent_node + "' has an incorrect format! The correct format is: Amount-UniqueID-Headname!";
            }
        }
        if (playerExists && msgToSend != null) {
            player.sendMessage(this.heading + msgToSend);
        }
        return null;
    }
    
    public void removeNamedItem(final Player player, final String itemInfo, final int totalAmt, final String weaponTitle, final boolean shop) {
        int removed = 0;
        final ItemStack item = this.parseItemStack(itemInfo);
        if (item == null) {
            return;
        }
        final ItemStack[] inv = player.getInventory().getContents();
        final String ammoName = this.plugin.getString(weaponTitle + ".Ammo.Ammo_Name_Check");
        final boolean checkName = ammoName != null;
        for (int i = 0; removed <= totalAmt && i < inv.length; ++i) {
            if (inv[i] != null && inv[i].getType() == item.getType() && inv[i].getDurability() == item.getDurability() && (!checkName || (this.plugin.itemIsSafe(inv[i]) && inv[i].getItemMeta().getDisplayName().contains(ammoName)))) {
                if (inv[i].getAmount() > totalAmt - removed) {
                    inv[i].setAmount(inv[i].getAmount() - (totalAmt - removed));
                    removed = totalAmt;
                }
                else {
                    removed += inv[i].getAmount();
                    inv[i] = null;
                }
            }
        }
        player.getInventory().setContents(inv);
        player.updateInventory();
        if (!this.containsItemStack(player, itemInfo, 1, weaponTitle) && !shop) {
            this.plugin.playSoundEffects(player, weaponTitle, ".Ammo.Sounds_Out_Of_Ammo", false, null);
        }
    }
    
    public int countItemStacks(final Player player, final String itemInfo, final String weaponTitle) {
        int count = 0;
        final ItemStack item = this.parseItemStack(itemInfo);
        if (item == null) {
            count = 0;
        }
        else {
            final String ammoName = this.plugin.getString(weaponTitle + ".Ammo.Ammo_Name_Check");
            final boolean checkName = ammoName != null;
            ItemStack[] contents;
            for (int length = (contents = player.getInventory().getContents()).length, i = 0; i < length; ++i) {
                final ItemStack itemSlot = contents[i];
                if (itemSlot != null && itemSlot.getType() == item.getType() && itemSlot.getDurability() == item.getDurability() && (!checkName || (this.plugin.itemIsSafe(itemSlot) && itemSlot.getItemMeta().getDisplayName().contains(ammoName)))) {
                    count += itemSlot.getAmount();
                }
            }
        }
        return count;
    }
    
    public boolean containsItemStack(final Player player, final String itemInfo, final int minAmount, final String weaponTitle) {
        final ItemStack item = this.parseItemStack(itemInfo);
        return item != null && this.countItemStacks(player, itemInfo, weaponTitle) >= minAmount;
    }
    
    public double getSuperDamage(final EntityType victimType, final String parent_node, double totalDmg) {
        final String superEffect = this.plugin.getString(parent_node + ".Abilities.Super_Effective");
        if (superEffect != null) {
            final String[] mobList = superEffect.split(",");
            String[] array;
            for (int length = (array = mobList).length, i = 0; i < length; ++i) {
                String mob = array[i];
                mob = mob.replace(" ", "");
                final String[] args = mob.split("-");
                try {
                    if (args.length == 2 && victimType == EntityType.valueOf(args[0])) {
                        totalDmg = (double)Math.round(totalDmg * Double.valueOf(args[1]));
                    }
                }
                catch (IllegalArgumentException ex) {
                    this.plugin.printM("The value provided for the Super_Effective node of the weapon '" + parent_node + "' is incorrect.");
                }
            }
        }
        return totalDmg;
    }
    
    public void displayFireworks(final Entity entity, final String parentNode, final String child_node) {
        if (!this.plugin.getBoolean(parentNode + ".Fireworks.Enable") || this.plugin.getString(parentNode + child_node) == null) {
            return;
        }
        final String[] fwList = this.plugin.getString(parentNode + child_node).split(",");
        String[] array;
        for (int length = (array = fwList).length, i = 0; i < length; ++i) {
            String fwInfo = array[i];
            fwInfo = fwInfo.replace(" ", "");
            final String[] args = fwInfo.split("-");
            if (args.length == 6) {
                try {
                    Firework fireWork;
                    if (entity instanceof LivingEntity) {
                        fireWork = (Firework)entity.getWorld().spawn(((LivingEntity)entity).getEyeLocation(), (Class)Firework.class);
                    }
                    else {
                        fireWork = (Firework)entity.getWorld().spawn(entity.getLocation(), (Class)Firework.class);
                    }
                    final FireworkMeta fireWorkMeta = fireWork.getFireworkMeta();
                    final FireworkEffect effect = FireworkEffect.builder().trail(Boolean.parseBoolean(args[1])).flicker(Boolean.parseBoolean(args[2])).withColor(Color.fromRGB(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]))).with(FireworkEffect.Type.valueOf(args[0].toUpperCase())).build();
                    fireWorkMeta.addEffects(effect);
                    fireWork.setFireworkMeta(fireWorkMeta);
                }
                catch (IllegalArgumentException ex) {
                    System.out.print("[CrackShot] '" + fwInfo + "' of weapon '" + parentNode + "' has an incorrect value for firework type, flicker, trail, or colour!");
                }
            }
            else {
                System.out.print("[CrackShot] '" + fwInfo + "' of weapon '" + parentNode + "' has an invalid format! The correct format is: Type-Trail-Flicker-Red-Blue-Green!");
            }
        }
    }
    
    public void givePotionEffects(final LivingEntity player, final String parentNode, final String childNode, final String event) {
        if (!event.equals("explosion")) {
            final String eventInfo = this.plugin.getString(parentNode + ".Potion_Effects.Activation");
            if (eventInfo == null || !eventInfo.toLowerCase().contains(event)) {
                return;
            }
        }
        if (this.plugin.getString(parentNode + childNode) == null) {
            return;
        }
        final String[] effectList = this.plugin.getString(parentNode + childNode).split(",");
        String[] array;
        for (int length = (array = effectList).length, i = 0; i < length; ++i) {
            String potFX = array[i];
            potFX = potFX.replace(" ", "");
            final String[] args = potFX.split("-");
            if (args.length == 3) {
                try {
                    final PotionEffectType potionType = PotionEffectType.getByName(args[0].toUpperCase());
                    int duration = Integer.parseInt(args[1]);
                    if (potionType.getDurationModifier() != 1.0) {
                        final double maths = duration * (1.0 / potionType.getDurationModifier());
                        duration = (int)maths;
                    }
                    player.removePotionEffect(potionType);
                    player.addPotionEffect(potionType.createEffect(duration, Integer.parseInt(args[2]) - 1));
                }
                catch (Exception ex) {
                    System.out.print("[CrackShot] '" + potFX + "' of weapon '" + parentNode + "' has an incorrect potion type, duration or level!");
                }
            }
            else {
                System.out.print("[CrackShot] '" + potFX + "' of weapon '" + parentNode + "' has an invalid format! The correct format is: Potion-Duration-Level!");
            }
        }
    }
  /*Particles*/
    public void giveParticleEffects(final Entity player, final String parentNode, final String childNode, final boolean muzzleFlash, final Location givenCoord) {
        if ((!this.plugin.getBoolean(parentNode + ".Particles.Enable") && givenCoord == null) || this.plugin.getString(parentNode + childNode) == null) {
            return;
        }
        Location loc = (player != null) ? player.getLocation() : givenCoord;
        final World world = loc.getWorld();
        if (muzzleFlash) {
            final Location eyeLoc = ((LivingEntity)player).getEyeLocation();
            loc = eyeLoc.toVector().add(eyeLoc.getDirection().multiply(1.5)).toLocation(world);
        }
        final String[] partList = this.plugin.getString(parentNode + childNode).split(",");
        String[] array;
        for (int length = (array = partList).length, j = 0; j < length; ++j) {
            String partFX = array[j];
            partFX = partFX.replace(" ", "");
            final String[] args = partFX.split("-");
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("smoke")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.SMOKE, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("lightning")) {
                    world.strikeLightningEffect(loc);
                }
                else if (args[0].equalsIgnoreCase("explosion")) {
                    world.createExplosion(loc, 0.0f);
                }
                else if (args[0].equalsIgnoreCase("blazeshoot")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.BLAZE_SHOOT, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("bowfire")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.BOW_FIRE, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("cloud")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.CLOUD, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("dust")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.COLOURED_DUST, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("crit")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.CRIT, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("endersignal")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.ENDER_SIGNAL, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("hugeexplosion")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.EXPLOSION_HUGE, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("largeexplosion")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.EXPLOSION_LARGE, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("extinguish")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.EXTINGUISH, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("fireworksspark")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.FIREWORKS_SPARK, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("flame")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.FLAME, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("flyingglyph")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.FLYING_GLYPH, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("footstep")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.FOOTSTEP, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("ghastshriek")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.GHAST_SHRIEK, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("happyvillager")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.HAPPY_VILLAGER, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("heart")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.HEART, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("instantspell")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.INSTANT_SPELL, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("largesmoke")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.LARGE_SMOKE, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("lavapop")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.LAVA_POP, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("lavadrip")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.LAVADRIP, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("magiccrit")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.MAGIC_CRIT, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("note")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.NOTE, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("portal")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.PORTAL, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("potionbreak")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.POTION_BREAK, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("potionswirl")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.POTION_SWIRL, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("slime")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.SLIME, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("smallsmoke")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.SMALL_SMOKE, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("snowshovel")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.SNOW_SHOVEL, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("snowballbreak")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.SNOWBALL_BREAK, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("spell")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.SPELL, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("splash")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.SPLASH, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("stepsound")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.STEP_SOUND, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("tilebreak")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.TILE_BREAK, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("tiledust")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.TILE_DUST, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("villagerthundercloud")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.VILLAGER_THUNDERCLOUD, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("voidfog")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.VOID_FOG, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("waterdrip")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.WATERDRIP, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("witchmagic")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.WITCH_MAGIC, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("zombiechewirondoor")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.ZOMBIE_CHEW_IRON_DOOR, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("zombiechewwoodendoor")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.ZOMBIE_CHEW_WOODEN_DOOR, i);
                    }
                }
                else if (args[0].equalsIgnoreCase("zombiedestroydoor")) {
                    for (int i = 0; i < 8; ++i) {
                        world.playEffect(loc, Effect.ZOMBIE_DESTROY_DOOR, i);
                    }
                }
            }
            else if (args.length == 2) {
                try {
                    if (args[0].equalsIgnoreCase("potion_splash")) {
                        world.playEffect(loc, Effect.POTION_BREAK, Integer.parseInt(args[1]));
                    }
                    else if (args[0].equalsIgnoreCase("block_break")) {
                        final int blockID = Integer.parseInt(args[1]);
                        if (blockID < 256) {
                            world.playEffect(loc, Effect.STEP_SOUND, blockID);
                        }
                        else {
                            this.plugin.printM("'" + partFX + "' was provided as a particle effect for the weapon '" + parentNode + "'. It contains '" + blockID + "', which is not a valid block ID.");
                        }
                    }
                    else if (args[0].equalsIgnoreCase("flames")) {
                        world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, Integer.parseInt(args[1]));
                    }
                }
                catch (NumberFormatException ex) {
                    this.plugin.printM("'" + partFX + "' was provided as a particle effect for the weapon '" + parentNode + "'. It contains '" + args[1] + "', which is not a valid number.");
                }
            }
        }
    }
    
    public boolean isInsideCuboid(final Location locPoint, final Location loc1, final Location loc2, final World world) {
        final double[] dim = new double[2];
        if (!locPoint.getWorld().equals(world)) {
            return false;
        }
        dim[0] = loc1.getX();
        dim[1] = loc2.getX();
        Arrays.sort(dim);
        if (locPoint.getX() > dim[1] || locPoint.getX() < dim[0]) {
            return false;
        }
        dim[0] = loc1.getY();
        dim[1] = loc2.getY();
        Arrays.sort(dim);
        if (locPoint.getY() > dim[1] || locPoint.getY() < dim[0]) {
            return false;
        }
        dim[0] = loc1.getZ();
        dim[1] = loc2.getZ();
        Arrays.sort(dim);
        return locPoint.getZ() <= dim[1] && locPoint.getZ() >= dim[0];
    }
    
    public boolean regionCheck(final Entity player, final String parent_node) {
        if (!this.plugin.getBoolean(parent_node + ".Region_Check.Enable")) {
            return true;
        }
        final String region_info = this.plugin.getString(parent_node + ".Region_Check.World_And_Coordinates");
        final String[] regions = region_info.split("\\|");
        boolean retVal = false;
        boolean relevance = false;
        String[] array;
        for (int length = (array = regions).length, i = 0; i < length; ++i) {
            String region = array[i];
            region = region.replace(" ", "");
            final String[] args = region.split(",");
            if (args != null && (args.length == 7 || args.length == 8)) {
                boolean blackList = args.length == 8 && Boolean.parseBoolean(args[7]);
                try {
                    final World regionWorld = Bukkit.getWorld(args[0]);
                    final Location locPoint = player.getLocation();
                    final Location locOne = new Location(regionWorld, Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
                    final Location locTwo = new Location(regionWorld, Double.valueOf(args[4]), Double.valueOf(args[5]), Double.valueOf(args[6]));
                    if (player.getWorld().equals(regionWorld)) {
                        relevance = true;
                        if (this.isInsideCuboid(locPoint, locOne, locTwo, regionWorld)) {
                            if (blackList) {
                                return false;
                            }
                            retVal = true;
                        }
                        else if (blackList) {
                            retVal = true;
                        }
                    }
                }
                catch (NumberFormatException ex) {
                    if (player instanceof Player) {
                        player.sendMessage(this.heading + "The value provided for the 'World_And_Coordinates' node of the weapon '" + parent_node + "' is incorrect. Double check the coordinates.");
                    }
                }
            }
            else if (player instanceof Player) {
                player.sendMessage(this.heading + "The 'World_And_Coordinates' node of the weapon '" + parent_node + "' has an incorrect number of arguments.");
            }
        }
        return !relevance || retVal;
    }
    
    public void weaponInteraction(final Player shooter, final String parent_node, final boolean leftClick) {
        final String projType = this.plugin.getString(parent_node + ".Shooting.Projectile_Type");
        final boolean underwater = this.plugin.getBoolean(parent_node + ".Extras.Disable_Underwater");
        final String[] validTypes = { "arrow", "snowball", "egg", "grenade", "flare", "fireball", "witherskull", "energy", "splash" };
        if (underwater) {
            final Location loc = shooter.getEyeLocation();
            if (loc.getBlock().getType().toString().toUpperCase().endsWith("WATER")) {
                return;
            }
        }
        if (projType != null) {
            String[] array;
            for (int length = (array = validTypes).length, i = 0; i < length; ++i) {
                final String type = array[i];
                if (projType.equalsIgnoreCase(type)) {
                    final WeaponPrepareShootEvent prepareEvent = new WeaponPrepareShootEvent(shooter, parent_node);
                    this.plugin.getServer().getPluginManager().callEvent(prepareEvent);
                    if (!prepareEvent.isCancelled()) {
                        this.plugin.fireProjectile(shooter, parent_node, leftClick);
                    }
                    return;
                }
            }
            shooter.sendMessage(this.heading + "'" + projType + "' is not a valid type of projectile!");
        }
    }
    
    public void callAirstrike(final Entity mark, final String parent_node, final Player player) {
        final int height = this.plugin.getInt(parent_node + ".Airstrikes.Height_Dropped");
        final int area = this.plugin.getInt(parent_node + ".Airstrikes.Area");
        final int spacing = this.plugin.getInt(parent_node + ".Airstrikes.Distance_Between_Bombs");
        int strikeNo = this.plugin.getInt(parent_node + ".Airstrikes.Multiple_Strikes.Number_Of_Strikes");
        int strikeDelay = this.plugin.getInt(parent_node + ".Airstrikes.Multiple_Strikes.Delay_Between_Strikes");
        final boolean multiStrike = this.plugin.getBoolean(parent_node + ".Airstrikes.Multiple_Strikes.Enable");
        final double coordinator = (area - 1) * (spacing / 2.0);
        final Location loc = mark.getLocation();
        final int y = loc.getBlockY();
        if (!multiStrike) {
            strikeNo = 1;
            strikeDelay = 1;
        }
        final Random r = new Random();
        final int vVar = this.plugin.getInt(parent_node + ".Airstrikes.Vertical_Variation");
        final int hVar = this.plugin.getInt(parent_node + ".Airstrikes.Horizontal_Variation");
        final String block = this.plugin.getString(parent_node + ".Airstrikes.Block_Type");
        if (block == null) {
            return;
        }
        String[] blockInfo = block.split("~");
        if (blockInfo.length < 2) {
            blockInfo = new String[] { blockInfo[0], "0" };
        }
        try {
            final Material blockMat = MaterialManager.getMaterial(block);
            final Byte secondaryData = Byte.valueOf(blockInfo[1]);
            this.plugin.sendPlayerMessage(player, parent_node, ".Airstrikes.Message_Call_Airstrike", player.getName(), "<victim>", "<flight>", "<damage>");
            this.giveParticleEffects(null, parent_node, ".Airstrikes.Particle_Call_Airstrike", false, loc);
            final WeaponExplodeEvent explodeEvent = new WeaponExplodeEvent(player, loc, parent_node, false, true);
            this.plugin.getServer().getPluginManager().callEvent(explodeEvent);
            for (int delay = 0; delay < strikeDelay * strikeNo; delay += strikeDelay) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                    @Override
                    public void run() {
                        CSMinion.this.plugin.playSoundEffects(mark, parent_node, ".Airstrikes.Sounds_Airstrike", false, null);
                        for (int iOne = 0; iOne < area; ++iOne) {
                            double x = loc.getBlockX() + iOne * spacing - coordinator;
                            for (int iTwo = 0; iTwo < area; ++iTwo) {
                                double z = loc.getBlockZ() + iTwo * spacing - coordinator;
                                int hD = y + height;
                                if (vVar != 0) {
                                    hD += r.nextInt(vVar);
                                }
                                if (hVar != 0) {
                                    x += r.nextInt(hVar) - r.nextInt(hVar);
                                    z += r.nextInt(hVar) - r.nextInt(hVar);
                                }
                                final FallingBlock bomb = loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x, hD, z), blockMat, secondaryData);
                                bomb.setDropItem(false);
                                bomb.setMetadata("CS_strike", new FixedMetadataValue(CSMinion.this.plugin, parent_node + "~" + player.getName()));
                            }
                        }
                    }
                }, delay);
                if (!multiStrike) {
                    break;
                }
            }
        }
        catch (IllegalArgumentException ex) {
            player.sendMessage(this.heading + "'" + block + "' in the 'Airstrikes' module of weapon '" + parent_node + "' is not a valid block-type.");
        }
    }
    
    public void detonateRDE(final Player player, final Player victim, final String[] itemInfo, final boolean clacker) {
        final World world = Bukkit.getServer().getWorld(itemInfo[1]);
        final Location loc = new Location(world, Integer.valueOf(itemInfo[2]) + 0.5, Integer.valueOf(itemInfo[3]) + 0.5, Integer.valueOf(itemInfo[4]) + 0.5);
        final Block c4 = world.getBlockAt(loc);
        if (MaterialManager.isSkullBlock(c4) && c4.getState() instanceof Skull) {
            String uniqueID = null;
            String storedPlayerName = clacker ? player.getName() : "Anonymous";
            Skull c4Block;
            try {
                c4Block = (Skull)c4.getState();
            }
            catch (ClassCastException ex) {
                return;
            }
            final boolean hasOwner = c4Block.hasOwner();
            if (clacker) {
                final String playerName = player.getName();
                final Map<String, String> placedHeads = this.plugin.c4_backup.get(playerName);
                if (placedHeads != null) {
                    final String key = c4.getWorld().getName() + "," + c4.getX() + "," + c4.getY() + "," + c4.getZ();
                    if (placedHeads.containsKey(key)) {
                        uniqueID = placedHeads.get(key);
                        placedHeads.remove(key);
                    }
                }
            }
            if (hasOwner || uniqueID != null) {
                if (hasOwner) {
                    final String grabInfo = c4Block.getOwner();
                    final String[] blockInfo = grabInfo.split("\u060c");
                    if (blockInfo.length < 1) {
                        return;
                    }
                    uniqueID = blockInfo[0];
                    storedPlayerName = blockInfo[1];
                }
                for (final String ids : this.plugin.rdelist.keySet()) {
                    if (ids.equalsIgnoreCase(uniqueID)) {
                        final String parent_node = this.plugin.rdelist.get(ids);
                        final String[] refinedOre = this.returnRefinedOre(player, parent_node);
                        if (refinedOre != null) {
                            c4Block.setOwner(refinedOre[2]);
                            c4Block.update(false);
                        }
                        if (!clacker) {
                            if (player != null) {
                                this.plugin.sendPlayerMessage(player, parent_node, ".Explosive_Devices.Message_Trigger_Placer", storedPlayerName.replace(String.valueOf('\u0638'), "..."), victim.getName(), "<flight>", "<damage>");
                                this.plugin.playSoundEffects(player, parent_node, ".Explosive_Devices.Sounds_Alert_Placer", false, null);
                            }
                            this.plugin.sendPlayerMessage(victim, parent_node, ".Explosive_Devices.Message_Trigger_Victim", storedPlayerName.replace(String.valueOf('\u0638'), "..."), victim.getName(), "<flight>", "<damage>");
                        }
                        c4Block.setMetadata("CS_transformers", new FixedMetadataValue(this.plugin, true));
                        this.plugin.playSoundEffects(null, parent_node, ".Explosive_Devices.Sounds_Trigger", false, loc);
                        this.plugin.projectileExplosion(null, parent_node, false, player, false, true, loc, c4, false, 0);
                        break;
                    }
                }
            }
        }
    }
    
    public boolean boobyAction(final Block block, final Entity victim, final ItemStack item) {
        for (final String name : this.plugin.boobs.keySet()) {
            if (this.plugin.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains(name)) {
                final String parent_node = this.plugin.boobs.get(name);
                String vicName = "Santa Claus";
                if (victim != null) {
                    if (victim instanceof Player) {
                        vicName = victim.getName();
                    }
                    else {
                        vicName = victim.getType().getName();
                    }
                }
                if (!this.getBoobean(3, parent_node)) {
                    return false;
                }
                final String detectedName = this.extractReading(item.getItemMeta().getDisplayName());
                if (detectedName.equals("?")) {
                    return false;
                }
                final Player planter = Bukkit.getServer().getPlayer(detectedName);
                if (victim != null) {
                    if (planter != null) {
                        if (planter == victim) {
                            return false;
                        }
                        this.plugin.sendPlayerMessage(planter, parent_node, ".Explosive_Devices.Message_Trigger_Placer", detectedName, vicName, "<flight>", "<damage>");
                        this.plugin.playSoundEffects(planter, parent_node, ".Explosive_Devices.Sounds_Alert_Placer", false, null);
                    }
                    if (victim instanceof Player) {
                        this.plugin.sendPlayerMessage((LivingEntity)victim, parent_node, ".Explosive_Devices.Message_Trigger_Victim", detectedName, vicName, "<flight>", "<damage>");
                    }
                }
                this.plugin.playSoundEffects(null, parent_node, ".Explosive_Devices.Sounds_Trigger", false, block.getLocation().add(0.5, 0.5, 0.5));
                this.plugin.projectileExplosion(null, parent_node, false, planter, false, true, null, block, true, 0);
                return true;
            }
        }
        return false;
    }
    
    public boolean getBoobean(final int entry, final String parent_node) {
        final String ore = this.plugin.getString(parent_node + ".Explosive_Devices.Device_Info");
        if (ore == null) {
            return false;
        }
        final String[] refinedOre = ore.split("-");
        return refinedOre.length == 5 && Boolean.parseBoolean(refinedOre[entry - 1]);
    }
    
    public ItemStack parseItemStack(final String ore) {
        ItemStack item = null;
        if (ore != null) {
            String[] refinedOre = ore.split("~");
            if (refinedOre.length == 1) {
                refinedOre = new String[] { refinedOre[0], "0" };
            }
            try {
                item = new ItemStack(MaterialManager.getMaterial(ore), 1, Short.valueOf(refinedOre[1]));
            }
            catch (Exception ex) {}
        }
        return item;
    }
    
    public void runCommand(final Player player, final String weaponTitle) {
        String commands = this.plugin.getString(weaponTitle + ".Extras.Run_Command");
        if (commands != null) {
            commands = commands.replaceAll("<shooter>", player.getName());
            final Server server = this.plugin.getServer();
            final String delimiter = "\u0e48\u0e4b\u0ec9";
            String[] split;
            for (int length = (split = commands.split("\u0e48\u0e4b\u0ec9")).length, i = 0; i < length; ++i) {
                final String command = split[i];
                if (command.startsWith("@")) {
                    server.dispatchCommand(server.getConsoleSender(), command.substring(1).trim());
                }
                else {
                    server.dispatchCommand(player, command.trim());
                }
            }
        }
    }
}
