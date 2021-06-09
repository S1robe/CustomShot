package com.shampaggon.crackshot;

import org.bukkit.plugin.java.*;
import org.bukkit.configuration.file.*;
import org.bukkit.enchantments.*;
import org.bukkit.command.*;
import org.bukkit.command.Command;
import org.bukkit.potion.*;
import org.bukkit.metadata.*;
import org.bukkit.event.*;
import java.util.*;

import org.bukkit.permissions.*;
import org.bukkit.util.*;
import org.bukkit.util.Vector;
import org.bukkit.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.*;
import java.lang.reflect.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.block.*;
import org.bukkit.block.*;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.material.*;
import org.bukkit.material.Dispenser;
import org.bukkit.event.entity.*;
import java.util.regex.*;
import com.shampaggon.crackshot.events.*;

public class CSDirector extends JavaPlugin implements Listener
{
	public Map<String, int[]> zoomStorage;
	public Map<String, Collection<Integer>> burst_task_IDs;
	public Map<String, Collection<Integer>> global_reload_IDs;
	public Map<String, Set<String>> grouplist;
	public Map<String, Boolean> morobust;
	public FileConfiguration weaponConfig;
	public Set<String> melees;
	public CSDirector plugin;
	public Map<String, Integer> rpm_ticks;
	public Map<String, Integer> rpm_shots;
	public Map<String, Map<Integer, Long>> last_shot_list;
	public Map<String, Map<String, String>> c4_backup;
	public Map<String, Integer> delayed_reload_IDs;
	public Map<String, Map<String, Integer>> delay_list;
	public Map<String, Map<String, ArrayDeque<Item>>> itembombs;
	public Map<String, String> convIDs;
	public Map<String, String[]> enchlist;
	public Map<String, String> parentlist;
	public Map<String, String> rdelist;
	public Map<Integer, String> wlist;
	public Map<String, String> boobs;
	public static Map<String, Integer> ints;
	public static Map<String, Double> dubs;
	public static Map<String, Boolean> bools;
	public static Map<String, String> strings;
	public String[] disWorlds;
	public String heading;
	public String version;
	public final CSMinion csminion;
	public static boolean debug;

	//Creates the vital hashmaps between its classes
	static {
		CSDirector.ints = new HashMap<String, Integer>();
		CSDirector.dubs = new HashMap<String, Double>();
		CSDirector.bools = new HashMap<String, Boolean>();
		CSDirector.strings = new HashMap<String, String>();
	}

	//General construct, called when the plugin's onEnable is called.
	public CSDirector() {
		this.zoomStorage = new HashMap<String, int[]>();
		this.burst_task_IDs = new HashMap<String, Collection<Integer>>();
		this.global_reload_IDs = new HashMap<String, Collection<Integer>>();
		this.grouplist = new HashMap<String, Set<String>>();
		this.morobust = new HashMap<String, Boolean>();
		this.weaponConfig = null;
		this.melees = new HashSet<String>();
		this.plugin = this;
		this.rpm_ticks = new HashMap<String, Integer>();
		this.rpm_shots = new HashMap<String, Integer>();
		this.last_shot_list = new HashMap<String, Map<Integer, Long>>();
		this.c4_backup = new HashMap<String, Map<String, String>>();
		this.delayed_reload_IDs = new HashMap<String, Integer>();
		this.delay_list = new HashMap<String, Map<String, Integer>>();
		this.itembombs = new HashMap<String, Map<String, ArrayDeque<Item>>>();
		this.convIDs = new HashMap<String, String>();
		this.enchlist = new HashMap<String, String[]>();
		this.parentlist = new HashMap<String, String>();
		this.rdelist = new HashMap<String, String>();
		this.wlist = new HashMap<Integer, String>();
		this.boobs = new HashMap<String, String>();
		this.disWorlds = new String[] { "0" };
		this.heading = "�f[�cCrackshot�f] �a- ";
		this.version = "2.6";
		this.csminion = new CSMinion(this);
	}

	public void onEnable() {
		validateInstall();
		this.csminion.loadWeapons(null);								//loads weapons
		this.csminion.loadGeneralConfig();										//loads general.yml
		this.csminion.loadMessagesConfig();										//loads messages.yml
		this.csminion.customRecipes();											//loads the gun recipies specifically in weapons.yml
		Bukkit.getPluginManager().registerEvents(this, this);	//registers this class as an "event listener"
	}

	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);						//cancels any would-be tasks
		for (final Player player : Bukkit.getServer().getOnlinePlayers()) {		//unscopes, cancels reloads, bursts.
			this.removeInertReloadTag(player, 0, true);
			this.unscopePlayer(player);
			this.terminateAllBursts(player);
			this.terminateReload(player);
		}
		for (final Map<String, ArrayDeque<Item>> subList : this.itembombs.values()) { //defuses bombs
			for (final ArrayDeque<Item> subSubList : subList.values()) {
				while (!subSubList.isEmpty()) {
					subSubList.removeFirst().remove();
				}
			}
		}
		//clears any memory used by this plugin.
		this.zoomStorage.clear();
		this.burst_task_IDs.clear();
		this.global_reload_IDs.clear();
		CSDirector.bools.clear();
		CSDirector.ints.clear();
		CSDirector.strings.clear();
		this.morobust.clear();
		this.wlist.clear();
		this.rdelist.clear();
		this.boobs.clear();
		CSDirector.dubs.clear();
		this.grouplist.clear();
		this.melees.clear();
		this.enchlist.clear();
		this.convIDs.clear();
		this.parentlist.clear();
		this.itembombs.clear();
		this.last_shot_list.clear();
		this.c4_backup.clear();
		this.delayed_reload_IDs.clear();
		this.delay_list.clear();
		this.rpm_ticks.clear();
		this.rpm_shots.clear();
		CSMessages.messages.clear();
		this.csminion.clearRecipes();
	}

	private void validateInstall(){
		try {
			Material.valueOf("SKULL");
			Class.forName("org.bukkit.projectiles.ProjectileSource");
		}
		//This try catch is used to tell if we are using 1.9-12 or not.
		catch (IllegalArgumentException e) {
			MaterialManager.pre113 = false;
		}
		//this try catch checks if the class ProjectileSource exists, this only exists on 1.8+. its used to get the entity that did the shooting
		catch (ClassNotFoundException e2) {
			this.printM("Failed to load. Your version of CraftBukkit is outdated!");
			this.setEnabled(false);
		}
	}

	/**
	 * This reads from a config file and populates the respective hashmaps
	 * @param config a weapon config file
	 */
	public void fillHashMaps(final FileConfiguration config) {
		//loads general.yml?
		for (final String string : config.getKeys(true)) {
			Object obj = config.get(string);
			if (obj instanceof Boolean) {
				CSDirector.bools.put(string, (Boolean)obj); //stores booleans with their respective tag in a hashmap
			}
			else if (obj instanceof Integer) {
				CSDirector.ints.put(string, (Integer)obj); //stores integers with their respective tag into a hashmap
			}
			else {
				if (!(obj instanceof String)) { //skips strings
					continue;
				}
				obj = ((String)obj).replaceAll("&", "�");
				CSDirector.strings.put(string, (String)obj);
			}
		}
		// this seems to load from weapons.yml and any subsequent file in the weapons folder
		for (final String parent_node : config.getKeys(false)) {
			final String[] specials = { ".Item_Information.Item_Type", ".Ammo.Ammo_Item_ID", ".Shooting.Projectile_Subtype", ".Crafting.Ingredients", ".Explosive_Devices.Device_Info", ".Airstrikes.Block_Type", ".Cluster_Bombs.Bomblet_Type", ".Shrapnel.Block_Type", ".Explosions.Damage_Multiplier" };
			String[] array;
			//specific array for loading the #specials category.
			for (int length = (array = specials).length, j = 0; j < length; ++j) {
				final String spec = array[j];
				CSDirector.strings.put(parent_node + spec, config.getString(parent_node + spec));
			}
			final String[] spread = { ".Shooting.Bullet_Spread", ".Sneak.Bullet_Spread", ".Scope.Zoom_Bullet_Spread" };
			String[] array2;
			//specific array for loading the #spread categories.
			for (int length2 = (array2 = spread).length, k = 0; k < length2; ++k) {
				final String spre = array2[k];
				CSDirector.dubs.put(parent_node + spre, config.getDouble(parent_node + spre));
			}
			final String invCtrl = this.getString(parent_node + ".Item_Information.Inventory_Control");
			//this is specifically to check if inv control is enabled (Populated)
			if (invCtrl != null) {
				final String[] groups = invCtrl.replaceAll(" ", "").split(",");
				String[] array3;
				for (int length3 = (array3 = groups).length, l = 0; l < length3; ++l) {
					final String group = array3[l];
					final Set<String> list;
					if (this.grouplist.containsKey(group)) {
						list = this.grouplist.get(group);
					}
					else {
						list = new HashSet<>();
					}
					list.add(parent_node);
					this.grouplist.put(group, list);
				}
			}
			final String enchantKey = this.getString(parent_node + ".Item_Information.Enchantment_To_Check");
			//this is for if we are checking an enchant on the item
			if (enchantKey != null) {
				final String[] enchantInfo = enchantKey.split("-");
				if (enchantInfo.length == 2) {
					if (Enchantment.getByName(enchantInfo[0]) == null) {
						this.printM("For the weapon '" + parent_node + "', the value provided for 'Enchantment_To_Check' does not contain a valid enchantment type.");
					}
					else {
						try {
							Integer.valueOf(enchantInfo[1]);
							this.enchlist.put(parent_node, enchantInfo);
						}
						catch (NumberFormatException ex) {
							this.printM("For the weapon '" + parent_node + "', the value provided for 'Enchantment_To_Check' does not contain a valid enchantment level.");
						}
					}
				}
			}
			final boolean skipName = this.getBoolean(parent_node + ".Item_Information.Skip_Name_Check");
			// this is if we are skipping name check, it will check the durability (the item data or the stone ->:3
			if (skipName) {
				final String itemInfo = this.getString(parent_node + ".Item_Information.Item_Type");
				final ItemStack item = this.csminion.parseItemStack(itemInfo);
				if (item != null) {
					this.convIDs.put(item.getType() + "-" + item.getDurability(), parent_node);
				}
			}
			final boolean Swap = this.getBoolean(parent_node + ".Item_Information.Swap");
			if (Swap) {
				final String itemInfo2 = this.getString(parent_node + ".Item_Information.Item_Type");
				final ItemStack item2 = this.csminion.parseItemStack(itemInfo2);
				if (item2 != null) {
					this.convIDs.put(item2.getType() + "-" + item2.getDurability(), parent_node);
				}
			}								//this is if the weapon as an attatchment.
			boolean accessory = false;
			final String attachType = this.getString(parent_node + ".Item_Information.Attachments.Type");	//accessory
			if (attachType != null && attachType.equalsIgnoreCase("accessory")) {
				accessory = true;
			}				//accessory
			//if we dont have an attatchemnt
			if (!accessory) {
				final String it = config.getString(parent_node + ".Item_Information.Item_Type");
				final String itemName = config.getString(parent_node + ".Item_Information.Item_Has_Durability");
				if (it == null) {
					this.printM("The weapon '" + parent_node + "' does not have a value for Item_Type.");
				}
				else if (itemName == null && this.csminion.durabilityCheck(it)) {
					this.morobust.put(parent_node, true);
				}
			}
			final List<String> commandList = config.getStringList(parent_node + ".Extras.Run_Command");
			//if there are any commands to be ran after shooting
			if (!commandList.isEmpty()) {
				StringBuilder stringList = new StringBuilder();
				final String delimiter = "\u0e48\u0e4b\u0ec9";
				for (int i = 0; i < commandList.size(); ++i) {
					final String command = commandList.get(i).trim();
					if (i != 0) {
						stringList.append("\u0e48\u0e4b\u0ec9");
					}
					if (command.startsWith("@")) {
						stringList.append("@").append(command.substring(1).trim());
					}
					else {
						stringList.append(command.trim());
					}
				}
				CSDirector.strings.put(parent_node + ".Extras.Run_Command", stringList.toString().replaceAll("&", "�"));
			}
			String name = config.getString(parent_node + ".Item_Information.Item_Name");
			//just adds color change to the name believe its &f
			if (accessory) {
				name = "�f" + parent_node;
			}
			//a console message if the weapon has np itemname
			if (name == null) {
				this.printM("The weapon '" + parent_node + "' does not have a value for Item_Name.");
			}
			//format the items name if were good and then check if were unique, if not print error
			else {
				name = name.replaceAll("&", "�");
				final String colorCodes = ChatColor.getLastColors(name);
				if (colorCodes.length() > 1) {
					name = name + "�" + colorCodes.substring(colorCodes.length() - 1);
				}
				else if (colorCodes.length() == 0) {
					name = "�f" + name + "�f";
				}
				CSDirector.strings.put(parent_node + ".Item_Information.Item_Name", name);
				if (!this.parentlist.containsKey(name)) {
					this.parentlist.put(name, parent_node);
				}
				else if (!accessory) {
					this.printM("Each weapon must have a unique name, but two or more weapons have '" + config.getString(parent_node + ".Item_Information.Item_Name") + "' for Item_Name! Tip: '&eWeapon' and '&eWea&epon' are different, but both look the same.");
				}
			}
			//are we a melee weapon?
			final boolean meleeMode = this.getBoolean(parent_node + ".Item_Information.Melee_Mode");
			//do we havea melee attatchment?
			final String meleeAttach = this.getString(parent_node + ".Item_Information.Melee_Attachment");
			//if we are good for both above and the attatchment is the main attatchment, then register this weapon as a melee weapon
			if (meleeAttach != null || meleeMode || (attachType != null && attachType.equalsIgnoreCase("main"))) {
				this.melees.add(parent_node);
			}
			//are we explosive, if so, add it as an explosive
			if (config.getBoolean(parent_node + ".Explosive_Devices.Enable")) {
				final String rdeOre = config.getString(parent_node + ".Explosive_Devices.Device_Info");
				if (rdeOre != null) {
					final String[] rdeRefined = rdeOre.split("-");
					if (rdeRefined.length == 3) {
						this.rdelist.put(rdeRefined[1], parent_node);
					}
				}
			}
			//also if we are a bomb, are we a trap?
			if (config.getBoolean(parent_node + ".Explosive_Devices.Enable")) {
				final String rdeInfo = config.getString(parent_node + ".Explosive_Devices.Device_Type");
				if (rdeInfo == null) {
					continue;
				}
				if (!rdeInfo.equalsIgnoreCase("trap")) {
					continue;
				}
				this.boobs.put(this.getString(parent_node + ".Item_Information.Item_Name"), parent_node);
			}
		}
	}


	//roots: crackshot, cs, cra, shot, s
	// Syntax						# args
	//root get <name> <amt> 		2 / 3
	//root config reload			2
	//root help 					1
	//root reload					1
	//root list <page or all> 		2
	//root debug [on/off]			2
	//root give <plr> <name> [amt] 	3/4
	//aliases are registered into plugin.yml

	/**
	 * created automatically by anything that is a "CommandExecutor"
	 * @param sender the sender of a given command
	 * @param command the command root that is being sent, ex /'gamemode' c name; gms is the command
	 * @param aliasUsed if an alias is used /'gms' would be an alias
	 * @param args /gamemode 'c name' is a 2 arg string array
	 * @return returns if the command is successful or not
	 */
	public boolean onCommand(final CommandSender sender, final Command command, final String aliasUsed, final String[] args){
		if(sender.hasPermission("crackshot.reloadplugin") && command.getName().equals("scr")){
			return sendReloadResponse(sender);
		}
		//crackshot rooted commands see Above
		if (command.getName().equalsIgnoreCase("crackshot")) {
			//help & reload
			if(args.length == 1){
				if(args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("help")){
					return sendHelpResponse(sender);
				}
				else if(sender.hasPermission("crackshot.debug.all") && (args[0].equalsIgnoreCase("debug") || args[0].equalsIgnoreCase("d"))){
					CSDirector.debug = !CSDirector.debug;
					return sendDebugResponse(sender);
				}
				if (sender instanceof final Player player && args[0].equalsIgnoreCase("reload")) {
					return sendPlayerReloadResponse(player);
				}

			}
			//conf rl, list #, debug o/f, get 1
			else if(args.length == 2){
				if(sender.hasPermission("crackshot.reloadplugin") && (args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("reload")
						|| args[0].equalsIgnoreCase("conf") && args[1].equalsIgnoreCase("rel" )
						|| args[0].equalsIgnoreCase("c") && args[1].equalsIgnoreCase("r"))){
					return sendReloadResponse(sender);
				}
				else if(sender.hasPermission("crackshot.list") && (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l"))){
					return this.csminion.listWeapons(sender, args[1]);
				}
				else if(sender.hasPermission("crackshot.debug.all") && (args[0].equalsIgnoreCase("debug") || args[0].equalsIgnoreCase("d"))){
					if(args[1].equalsIgnoreCase("on")
							|| args[1].equalsIgnoreCase("true"))
						CSDirector.debug = true;
					else if (args[1].equalsIgnoreCase("off")
							|| args[1].equalsIgnoreCase("false"))
						CSDirector.debug = false;
					return sendDebugResponse(sender);
				}
				else if(sender instanceof Player && args[0].equalsIgnoreCase("get")){
					return this.csminion.getWeaponCommand((Player)sender, args[1], true, "1", false, false);
				}
			}
			//get <amt> , give 1
			else if(args.length == 3){
				if(sender instanceof Player && args[0].equalsIgnoreCase("get")){
					return this.csminion.getWeaponCommand((Player)sender, args[1], true, args[2], false, false);
				}
				else if(args[0].equalsIgnoreCase("give")){
					return sendGiveResponse(sender, args);
				}
			}
			//give <amt>
			else if(args.length == 4){
				if(args[0].equalsIgnoreCase("give")){
					return sendGiveResponse(sender, args);
				}
			}
			sender.sendMessage(this.heading + "Invalid command.");
			return false;
		}
		return true;
	}

	/**
	 * This is a subroutine to parse the reload command and its aliases.
	 * @param sender the sender of the command
	 * @return a boolean, true if the command executed successfull, false otherwise.
	 */
	private boolean sendReloadResponse(CommandSender sender){
		if (!sender.hasPermission("crackshot.reloadplugin")) {
			sender.sendMessage(this.heading + "You do not have permission to do this.");
			return true;
		}
		//clears data
		this.disWorlds = new String[] { "0" };
		this.csminion.clearRecipes();
		CSDirector.bools.clear();
		CSDirector.ints.clear();
		CSDirector.strings.clear();
		this.morobust.clear();
		this.wlist.clear();
		this.rdelist.clear();
		this.boobs.clear();
		CSDirector.dubs.clear();
		this.grouplist.clear();
		this.melees.clear();
		this.enchlist.clear();
		this.convIDs.clear();
		this.parentlist.clear();
		CSMessages.messages.clear();

		if (sender instanceof Player) {
			this.csminion.loadWeapons((Player) sender);
		}
		else {
			this.csminion.loadWeapons(null);
		}
		this.csminion.loadGeneralConfig();
		this.csminion.loadMessagesConfig();
		this.csminion.customRecipes();
		sender.sendMessage(this.heading + "�dConfiguration reloaded.");
		return true;
	}

	private boolean sendPlayerReloadResponse(Player player){
		final String parent_node2 = this.returnParentNode(player);
		if (parent_node2 == null) {
			CSMessages.sendMessage(player, this.heading, CSMessages.Message.CANNOT_RELOAD.getMessage());
			return true;
		}
		if (!player.hasPermission("crackshot.use." + parent_node2) && !player.hasPermission("crackshot.use.all")) {
			CSMessages.sendMessage(player, this.heading, CSMessages.Message.NP_WEAPON_USE.getMessage());
			return false;
		}
		this.reloadAnimation(player, parent_node2);
		return true;
	}

	private boolean sendHelpResponse(CommandSender sender){
		sender.sendMessage("�r                    �f[�cCrackshot�f]�f         ");
		sender.sendMessage("�7\u2192 �6Authors: �6Shampaggon, �c� �aDayDream_, �5S1robe");
		sender.sendMessage("�7\u2192 �6Version: �6" + this.version);
		sender.sendMessage("�7\u2192 �5Aliases: �7/shot, /cra, /cs, /s");
		sender.sendMessage("�7\u2192 �bMain Commands:");
		sender.sendMessage("�7\u2192 �c- �a/shot list [all or {pagenumber}]");
		sender.sendMessage("�7\u2192 �c- �a/shot give {player} {weapon} {amount}");
		sender.sendMessage("�7\u2192 �c- �a/s get {weapon} {amount}");
		sender.sendMessage("�7\u2192 �c- �a/scr [reloads the configuration files]");
		sender.sendMessage("�7\u2192 �c- �a/s d [enables debug mode]");
		return true;
	}

	private boolean sendDebugResponse(CommandSender sender){
		sender.sendMessage("�cCrackshot debug mode is now: " + CSDirector.debug);
		return true;
	}

	private boolean sendGiveResponse(CommandSender sender, String[] args){
		String amount = "1";
		if (args.length == 4) {
			amount = args[3];
		}
		final String parent_node = this.csminion.identifyWeapon(args[2]);
		if (parent_node == null) {
			sender.sendMessage(this.heading + "No weapon matches '" + args[2] + "'.");
			return false;
		}
		if (sender.hasPermission("crackshot.give." + parent_node) && !sender.hasPermission("crackshot.give.all")) {
			final Player receiver = Bukkit.getServer().getPlayer(args[1]);
			if (receiver == null) {
				sender.sendMessage(this.heading + "No player named '" + args[1] + "' was found.");
				return false;
			}
			if (receiver.getInventory().firstEmpty() != -1) {
				sender.sendMessage(this.heading + "Package delivered to " + receiver.getName() + ".");
				return this.csminion.getWeaponCommand(receiver, parent_node, false, amount, true, false);
			}
			sender.sendMessage(this.heading + receiver.getName() + "'s inventory is full.");
			return false;
		}
		sender.sendMessage(this.heading + "You do not have permission to give this item.");
		return false;
	}


	@EventHandler
	public void OnPlayerInteract(final PlayerInteractEvent event) {
		Action eventAction = event.getAction();
		Block clickedBlock = event.getClickedBlock();
		if (eventAction != Action.PHYSICAL) {

			//if we left click a wall sign and event is a shop event, then dont process it here go to this.ShopEvent
			if (eventAction == Action.LEFT_CLICK_BLOCK && clickedBlock.getType() == Material.WALL_SIGN && this.shopEvent(event)) {
				return;
			}
			//if we left click a skull and that skull has the tag CS_Transformer, its used for somethign else (TBDL)
			if (eventAction == Action.LEFT_CLICK_BLOCK && MaterialManager.isSkullBlock(clickedBlock) && clickedBlock.hasMetadata("CS_transformers")) {
				event.setCancelled(true);
			}

			final Player shooter = event.getPlayer();					//shooeter duh
			final ItemStack item = shooter.getItemInHand();				//item in main hand (gun?)
			final String parent_node = this.returnParentNode(shooter);	//the weapons.yml tag node for this item

			//if we dont find a name for this weapon its not somethign we should handle
			if (parent_node == null) {
				return;
			}
			// if this gu nhas a melee mode but our hot bar is invalid (
			if (!this.getBoolean(parent_node + ".Item_Information.Melee_Mode") && !this.validHotbar(shooter, parent_node)) {
				return;
			}
			if (!this.regionAndPermCheck(shooter, parent_node, false)) {
				return;
			}

			final boolean rightShoot = this.getBoolean(parent_node + ".Shooting.Right_Click_To_Shoot");
			final boolean dualWield = this.isDualWield(shooter, parent_node, item);
			final boolean leftClick = eventAction == Action.LEFT_CLICK_AIR || eventAction == Action.LEFT_CLICK_BLOCK;
			final boolean rightClick = eventAction == Action.RIGHT_CLICK_AIR || eventAction == Action.RIGHT_CLICK_BLOCK;
			final boolean rdeEnable = this.getBoolean(parent_node + ".Explosive_Devices.Enable");
			final String[] attachTypeAndInfo = this.getAttachment(parent_node, item);

			if (attachTypeAndInfo[0] != null) {
				if (attachTypeAndInfo[0].equalsIgnoreCase("accessory") && rdeEnable) {
					shooter.sendMessage(this.heading + "The weapon '" + parent_node + "' is an attachment. It cannot use the Explosive_Devices module!");
					return;
				}
				if (dualWield) {
					shooter.sendMessage(this.heading + "The weapon '" + parent_node + "' cannot use attachments and be dual wielded at the same time!");
					return;
				}
			}
			if (eventAction == Action.LEFT_CLICK_BLOCK) {
				final boolean noBlockDmg = this.getBoolean(parent_node + ".Shooting.Cancel_Left_Click_Block_Damage");
				if (noBlockDmg) {
					event.setCancelled(true);
				}
			}
			if (eventAction == Action.RIGHT_CLICK_AIR || eventAction == Action.RIGHT_CLICK_BLOCK) {
				final boolean rightInteract = this.getBoolean(parent_node + ".Shooting.Cancel_Right_Click_Interactions");
				if (rightInteract) {
					event.setCancelled(true);
				}
			}
			if (!item.getItemMeta().getDisplayName().contains("�")) {
				shooter.setItemInHand(this.csminion.vendingMachine(parent_node));
				event.setCancelled(true);
				return;
			}
			if (!this.getBoolean(parent_node + ".Item_Information.Remove_Unused_Tag")) {
				this.checkCorruption(item, attachTypeAndInfo[0] != null, dualWield);
			}
			if ((rightShoot && rightClick) || (!rightShoot && leftClick) || dualWield) {
				if (rdeEnable) {
					final String type = this.getString(parent_node + ".Explosive_Devices.Device_Type");
					if (type != null) {
						if (type.equalsIgnoreCase("remote") || type.equalsIgnoreCase("itembomb")) {
							this.detonateC4(shooter, item, parent_node, type);
						}
						else if (type.equalsIgnoreCase("trap") && this.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains("�?�")) {
							final String itemName = this.getString(parent_node + ".Item_Information.Item_Name");
							this.csminion.setItemName(shooter.getInventory().getItemInHand(), itemName + " �" + shooter.getName() + "�");
							this.playSoundEffects(shooter, parent_node, ".Explosive_Devices.Sounds_Deploy", false, null);
						}
						else if (type.equalsIgnoreCase("landmine")) {
							this.csminion.oneTime(shooter);
							this.playSoundEffects(shooter, parent_node, ".Explosive_Devices.Sounds_Deploy", false, null);
							this.deployMine(shooter, parent_node, null);
						}
					}
				}
				else if (item.getType() != Material.BOW) {
					this.csminion.weaponInteraction(shooter, parent_node, leftClick);
				}
			}
			else if (!dualWield && ((rightShoot && leftClick) || (!rightShoot && rightClick))) {
				if (this.getBoolean(parent_node + ".Reload.Reload_With_Mouse")) {
					this.reloadAnimation(shooter, parent_node);
					return;
				}
				if (this.tossBomb(shooter, parent_node, item, rdeEnable)) {
					return;
				}
				if (attachTypeAndInfo[0] != null) {
					final int gunSlot = shooter.getInventory().getHeldItemSlot();
					final boolean hasDelay = shooter.hasMetadata("togglesnoShooting" + gunSlot);
					if (hasDelay) {
						return;
					}
					final boolean main = attachTypeAndInfo[0].equalsIgnoreCase("main");
					final boolean accessory = attachTypeAndInfo[0].equalsIgnoreCase("accessory");
					if (main || accessory) {
						if (main) {
							final String attachValid = this.getString(attachTypeAndInfo[1] + ".Item_Information.Attachments.Type");
							if (attachTypeAndInfo[1] == null) {
								shooter.sendMessage(this.heading + "The weapon '" + parent_node + "' is missing the weapon title of an attachment!");
								return;
							}
							if (attachValid == null) {
								shooter.sendMessage(this.heading + "The weapon '" + parent_node + "' has an invalid attachment. The weapon '" + attachTypeAndInfo[1] + "' has to be an accessory!");
								return;
							}
						}
						final int toggleDelay = this.getInt(parent_node + ".Item_Information.Attachments.Toggle_Delay");
						final WeaponAttachmentToggleEvent toggleEvent = new WeaponAttachmentToggleEvent(shooter, parent_node, item, toggleDelay);
						this.getServer().getPluginManager().callEvent(toggleEvent);
						if (toggleEvent.isCancelled()) {
							return;
						}
						this.playSoundEffects(shooter, parent_node, ".Item_Information.Attachments.Sounds_Toggle", false, null);
						this.reloadShootDelay(shooter, parent_node, gunSlot, toggleEvent.getToggleDelay(), "noShooting", "toggles");
						this.terminateAllBursts(shooter);
						this.terminateReload(shooter);
						this.removeInertReloadTag(shooter, 0, true);
						if (this.itemIsSafe(item)) {
							final String itemName2 = item.getItemMeta().getDisplayName();
							final String triOne = String.valueOf('\u25b6');
							final String triTwo = String.valueOf('\u25b7');
							final String triThree = String.valueOf('\u25c0');
							final String triFour = String.valueOf('\u25c1');
							if (itemName2.contains(triThree)) {
								this.csminion.setItemName(item, itemName2.replaceAll(triThree + triTwo, triFour + triOne));
							}
							else {
								this.csminion.setItemName(item, itemName2.replaceAll(triFour + triOne, triThree + triTwo));
							}
						}
						return;
					}
				}
				final boolean zoomEnable = this.getBoolean(parent_node + ".Scope.Enable");
				final boolean nightScope = this.getBoolean(parent_node + ".Scope.Night_Vision");
				if (!zoomEnable || shooter.hasMetadata("markOfTheReload")) {
					return;
				}
				final int zoomAmount = this.getInt(parent_node + ".Scope.Zoom_Amount");
				if (zoomAmount < 0 || zoomAmount == 0 || zoomAmount > 10) {
					return;
				}
				final WeaponScopeEvent scopeEvent = new WeaponScopeEvent(shooter, parent_node, !shooter.hasMetadata("ironsights"));
				this.getServer().getPluginManager().callEvent(scopeEvent);
				if (scopeEvent.isCancelled()) {
					return;
				}
				this.playSoundEffects(shooter, parent_node, ".Scope.Sounds_Toggle_Zoom", false, null);
				if (shooter.hasPotionEffect(PotionEffectType.SPEED)) {
					for (final PotionEffect pe : shooter.getActivePotionEffects()) {
						if (pe.getType().toString().contains("SPEED")) {
							if (shooter.hasMetadata("ironsights")) {
								this.unscopePlayer(shooter, true);
								break;
							}
							if (!shooter.hasPotionEffect(PotionEffectType.NIGHT_VISION) && nightScope) {
								shooter.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(2400, -1));
								shooter.setMetadata("night_scoping", new FixedMetadataValue(this, true));
							}
							shooter.setMetadata("ironsights", new FixedMetadataValue(this, parent_node));
							this.zoomStorage.put(shooter.getName(), new int[] { pe.getDuration(), pe.getAmplifier() });
							shooter.removePotionEffect(PotionEffectType.SPEED);
							shooter.addPotionEffect(PotionEffectType.SPEED.createEffect(2400, -zoomAmount));
							break;
						}
					}
				}
				else {
					if (!shooter.hasPotionEffect(PotionEffectType.NIGHT_VISION) && nightScope) {
						shooter.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(2400, -1));
						shooter.setMetadata("night_scoping", new FixedMetadataValue(this, true));
					}
					shooter.setMetadata("ironsights", new FixedMetadataValue(this, parent_node));
					shooter.addPotionEffect(PotionEffectType.SPEED.createEffect(2400, -zoomAmount));
				}
			}
		}
		else if (MaterialManager.isPressurePlate(clickedBlock)) {
			final Player victim = event.getPlayer();
			final List<Entity> l = victim.getNearbyEntities(4.0, 4.0, 4.0);
			for (final Entity e : l) {
				if (e instanceof ItemFrame) {
					this.csminion.boobyAction(clickedBlock, victim, ((ItemFrame)e).getItem());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamage(final EntityDamageByEntityEvent event) {
		final Entity entVictim = event.getEntity();
		final Entity entDmger = event.getDamager();
		boolean cancelMelee = false;
		if (entVictim instanceof Player && entVictim.hasMetadata("CS_singed")) {
			cancelMelee = true;
			if (!event.isCancelled()) {
				entVictim.setMetadata("CS_singed", new FixedMetadataValue(this, true));
				event.setCancelled(true);
			}
			else {
				entVictim.removeMetadata("CS_singed", this);
			}
		}
		if (entVictim instanceof Player && entVictim.hasMetadata("deep_fr1ed")) {
			cancelMelee = true;
			String parent_node = null;
			Player pPlayer = null;
			boolean nodam = false;
			final Player victim = (Player)entVictim;
			final int damage = victim.getMetadata("deep_fr1ed").get(0).asInt();
			victim.removeMetadata("deep_fr1ed", this);
			if (victim.hasMetadata("CS_nodam")) {
				nodam = true;
			}
			if (victim.hasMetadata("CS_potex") && victim.getMetadata("CS_potex") != null) {
				parent_node = victim.getMetadata("CS_potex").get(0).asString();
			}
			if (entDmger instanceof Player) {
				pPlayer = (Player)entDmger;
			}
			victim.removeMetadata("CS_potex", this);
			if (!event.isCancelled()) {
				this.csminion.explosionPackage(victim, parent_node, pPlayer);
				if (!nodam) {
					event.setDamage(damage);
				}
				else {
					event.setCancelled(true);
				}
			}
		}
		if (entDmger instanceof Player && entVictim instanceof LivingEntity) {
			final Player player = (Player)entDmger;
			final Location finalLoc = player.getEyeLocation();
			final Vector direction = player.getEyeLocation().getDirection().normalize().multiply(0.5);
			for (int i = 0; i < 10; ++i) {
				finalLoc.add(direction);
				if (finalLoc.getBlock().getType() != Material.AIR) {
					this.OnPlayerInteract(new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, player.getItemInHand(), null, null));
					break;
				}
			}
		}
		if (entVictim instanceof LargeFireball && entVictim.hasMetadata("CS_NoDeflect")) {
			event.setCancelled(true);
			return;
		}
		if (entDmger instanceof Player && event.getDamage() == 8.0 && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
			final List<Entity> witherNet = entVictim.getNearbyEntities(4.0, 4.0, 4.0);
			for (final Entity closeEnt : witherNet) {
				if (closeEnt instanceof WitherSkull && ((Projectile)closeEnt).getShooter() == entDmger) {
					event.setCancelled(true);
				}
			}
		}
		if (!cancelMelee && entDmger instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && !event.isCancelled() && entVictim instanceof LivingEntity) {
			final Player player = (Player)entDmger;
			final String parentNode = this.returnParentNode(player);
			if (parentNode != null && this.regionAndPermCheck(player, parentNode, true)) {
				int punchDelay = this.getInt(parentNode + ".Shooting.Delay_Between_Shots");
				final int gunSlot = player.getInventory().getHeldItemSlot();
				if (!player.hasMetadata(parentNode + "meleeDelay" + gunSlot)) {
					if (this.getBoolean(parentNode + ".Item_Information.Melee_Mode")) {
						final ItemStack item = player.getItemInHand();
						final String ammoInfo = this.getString(parentNode + ".Ammo.Ammo_Item_ID");
						final boolean reloadOn = this.getBoolean(parentNode + ".Reload.Enable");
						final boolean ammoPerShot = this.getBoolean(parentNode + ".Ammo.Take_Ammo_Per_Shot");
						final boolean ammoEnable = this.getBoolean(parentNode + ".Ammo.Enable");
						final boolean takeAmmo = this.getBoolean(parentNode + ".Reload.Take_Ammo_On_Reload");
						final int detectedAmmo = this.getAmmoBetweenBrackets(player, parentNode, item);
						if (!this.validHotbar(player, parentNode)) {
							return;
						}
						player.setMetadata(parentNode + "meleeDelay" + gunSlot, new FixedMetadataValue(this, true));
						this.csminion.tempVars(player, parentNode + "meleeDelay" + gunSlot, (long)punchDelay);
						if (ammoEnable) {
							if (!takeAmmo && !ammoPerShot) {
								player.sendMessage(this.heading + "The weapon '" + parentNode + "' has enabled the Ammo module, but at least one of the following nodes need to be set to true: Take_Ammo_On_Reload, Take_Ammo_Per_Shot.");
								return;
							}
							if (!this.csminion.containsItemStack(player, ammoInfo, 1, parentNode) && (ammoPerShot || (takeAmmo && detectedAmmo == 0))) {
								this.playSoundEffects(player, parentNode, ".Ammo.Sounds_Shoot_With_No_Ammo", false, null);
								return;
							}
						}
						if (this.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains(String.valueOf('\u1d3f'))) {
							if (detectedAmmo <= 0) {
								this.reloadAnimation(player, parentNode);
								return;
							}
							this.terminateReload(player);
							this.removeInertReloadTag(player, 0, true);
						}
						if (reloadOn) {
							if (detectedAmmo <= 0) {
								this.reloadAnimation(player, parentNode);
								return;
							}
							this.ammoOperation(player, parentNode, detectedAmmo, item);
						}
						else {
							final String itemName = item.getItemMeta().getDisplayName();
							if (itemName.contains("�") && !itemName.contains(String.valueOf('\u00d7'))) {
								this.csminion.replaceBrackets(item, String.valueOf('\u00d7'), parentNode);
							}
						}
						this.dealDamage(player, (LivingEntity)entVictim, event, parentNode);
					}
					else {
						final String meleeNode = this.getString(parentNode + ".Item_Information.Melee_Attachment");
						if (meleeNode != null) {
							punchDelay = this.getInt(meleeNode + ".Shooting.Delay_Between_Shots");
							if (this.melees.contains(meleeNode)) {
								if (this.validHotbar(player, parentNode)) {
									player.setMetadata(parentNode + "meleeDelay" + gunSlot, new FixedMetadataValue(this, true));
									this.csminion.tempVars(player, parentNode + "meleeDelay" + gunSlot, (long)punchDelay);
									this.dealDamage(player, (LivingEntity)entVictim, event, meleeNode);
								}
							}
							else {
								player.sendMessage(this.heading + "The weapon '" + parentNode + "' has an unknown melee attachment. '" + meleeNode + "' could not be found!");
							}
						}
					}
				}
				else {
					event.setCancelled(true);
				}
			}
		}
		if ((entDmger instanceof WitherSkull || entDmger instanceof LargeFireball) && event.getDamager().hasMetadata("projParentNode")) {
			event.setCancelled(true);
		}
		if (entDmger instanceof Player && entVictim instanceof Player && entVictim.hasMetadata("CS_Energy") && !event.isCancelled()) {
			this.dealDamage(entDmger, (LivingEntity)entVictim, event, entVictim.getMetadata("CS_Energy").get(0).asString());
			entVictim.removeMetadata("CS_Energy", this);
		}
		if ((entDmger instanceof Arrow || entDmger instanceof Egg || entDmger instanceof Snowball) && entDmger.hasMetadata("projParentNode") && entVictim instanceof LivingEntity && !event.isCancelled()) {
			this.dealDamage(entDmger, (LivingEntity)entVictim, event, entDmger.getMetadata("projParentNode").get(0).asString());
		}
		if (entDmger instanceof TNTPrimed && entDmger.hasMetadata("CS_Label")) {
			if (entDmger.hasMetadata("nullify") && (entVictim instanceof Painting || entVictim instanceof ItemFrame || entVictim instanceof Item || entVictim instanceof ExperienceOrb)) {
				event.setCancelled(true);
			}
			if (entDmger.hasMetadata("CS_nodam") || entVictim.hasMetadata("CS_shrapnel")) {
				if (entVictim instanceof Player) {
					entVictim.setMetadata("CS_nodam", new FixedMetadataValue(this, true));
					this.csminion.tempVars((Player)entVictim, "CS_nodam", 2L);
				}
				event.setCancelled(true);
			}
			String parent_node = null;
			double totalDmg = event.getDamage();
			if (entDmger.hasMetadata("CS_potex")) {
				parent_node = entDmger.getMetadata("CS_potex").get(0).asString();
				if (event.getDamage() > 1.0 && parent_node != null) {
					try {
						final String multiString = this.getString(parent_node + ".Explosions.Damage_Multiplier");
						if (multiString != null) {
							final double multiplier = Integer.valueOf(multiString) * 0.01;
							totalDmg *= multiplier;
							totalDmg = this.csminion.getSuperDamage(entVictim.getType(), parent_node, totalDmg);
						}
					}
					catch (IllegalArgumentException ex) {}
				}
			}
			final int knockBack = this.getInt(parent_node + ".Explosions.Knockback");
			if (knockBack != 0 && !entVictim.hasMetadata("CS_shrapnel")) {
				final Vector vector = this.csminion.getAlignedDirection(entDmger.getLocation(), entVictim.getLocation());
				entVictim.setVelocity(vector.multiply(knockBack * 0.1));
			}
			String pName = "Player";
			Player pPlayer2 = null;
			if (entDmger.hasMetadata("CS_pName")) {
				pName = entDmger.getMetadata("CS_pName").get(0).asString();
				pPlayer2 = Bukkit.getServer().getPlayer(pName);
			}
			final boolean noDam = entVictim instanceof Player && entDmger.hasMetadata("0wner_nodam") && entVictim.getName().equals(pName);
			if (noDam) {
				totalDmg = 0.0;
			}
			final WeaponDamageEntityEvent weaponEvent = new WeaponDamageEntityEvent(pPlayer2, entVictim, entDmger, parent_node, totalDmg, false, false, false);
			this.getServer().getPluginManager().callEvent(weaponEvent);
			event.setDamage(weaponEvent.getDamage());
			if (weaponEvent.isCancelled()) {
				event.setCancelled(true);
			}
			else if (entVictim instanceof Player) {
				final Player victim2 = (Player)entVictim;
				if (noDam) {
					event.setCancelled(true);
					return;
				}
				victim2.setNoDamageTicks(0);
				if (entDmger.hasMetadata("CS_ffcheck")) {
					if (victim2.getName().equals(pName)) {
						this.csminion.explosionPackage(victim2, parent_node, pPlayer2);
					}
					else if (pPlayer2 != null) {
						victim2.setMetadata("deep_fr1ed", new FixedMetadataValue(this, event.getDamage()));
						if (parent_node != null) {
							victim2.setMetadata("CS_potex", new FixedMetadataValue(this, parent_node));
						}
						this.csminion.illegalSlap(pPlayer2, victim2, 0);
						event.setCancelled(true);
					}
				}
				else {
					this.csminion.explosionPackage(victim2, parent_node, pPlayer2);
				}
			}
			else if (entVictim instanceof LivingEntity) {
				((LivingEntity)entVictim).setNoDamageTicks(0);
				this.csminion.explosionPackage((LivingEntity)entVictim, parent_node, pPlayer2);
			}
		}
		if (entVictim instanceof Player && !event.isCancelled()) {
			final Player blocker = (Player)entVictim;
			final String parentNode = this.returnParentNode(blocker);
			if (parentNode == null) {
				return;
			}
			int durabPerHit = this.getInt(parentNode + ".Riot_Shield.Durability_Loss_Per_Hit");
			final boolean riotEnable = this.getBoolean(parentNode + ".Riot_Shield.Enable");
			final boolean durabDmg = this.getBoolean(parentNode + ".Riot_Shield.Durablity_Based_On_Damage");
			final boolean noProj = this.getBoolean(parentNode + ".Riot_Shield.Do_Not_Block_Projectiles");
			final boolean noMelee = this.getBoolean(parentNode + ".Riot_Shield.Do_Not_Block_Melee_Attacks");
			final boolean forceField = this.getBoolean(parentNode + ".Riot_Shield.Forcefield_Mode");
			final boolean mustBlock = this.getBoolean(parentNode + ".Riot_Shield.Only_Works_While_Blocking");
			if (mustBlock && !blocker.isBlocking()) {
				return;
			}
			if (!riotEnable || !this.regionAndPermCheck(blocker, parentNode, false)) {
				return;
			}
			if (entDmger instanceof Projectile) {
				if (noProj) {
					return;
				}
				if (!forceField) {
					final Projectile objProj = (Projectile)entDmger;
					final double faceAngle = blocker.getLocation().getDirection().dot(((Entity)objProj.getShooter()).getLocation().getDirection());
					if (faceAngle > 0.0 && !(objProj.getShooter() instanceof Skeleton)) {
						return;
					}
				}
			}
			else {
				if (noMelee) {
					return;
				}
				if (!forceField) {
					final double faceAngle2 = blocker.getLocation().getDirection().dot(entDmger.getLocation().getDirection());
					if (faceAngle2 > 0.0) {
						return;
					}
				}
			}
			if (durabDmg) {
				durabPerHit *= (int)event.getDamage();
			}
			final ItemStack shield = blocker.getInventory().getItemInHand();
			shield.setDurability((short)(shield.getDurability() + durabPerHit));
			this.playSoundEffects(blocker, parentNode, ".Riot_Shield.Sounds_Blocked", false, null);
			if (shield.getType().getMaxDurability() <= shield.getDurability()) {
				this.playSoundEffects(blocker, parentNode, ".Riot_Shield.Sounds_Break", false, null);
				blocker.getInventory().clear(blocker.getInventory().getHeldItemSlot());
				blocker.updateInventory();
			}
			event.setCancelled(true);
		}
	}

	public void dealDamage(final Entity entDmger, final LivingEntity victim, final EntityDamageByEntityEvent event, final String parent_node) {
		final boolean energyMode = entDmger instanceof Player;
		Projectile objProj = null;
		Player shooter;
		if (!energyMode) {
			objProj = (Projectile)entDmger;
			shooter = (Player)objProj.getShooter();
			objProj.setMetadata("Collided", new FixedMetadataValue(this, true));
		}
		else {
			shooter = (Player)entDmger;
		}
		if (shooter == null) {
			return;
		}
		final double projSpeed = this.getInt(parent_node + ".Shooting.Projectile_Speed") * 0.1;
		final boolean hitEnable = this.getBoolean(parent_node + ".Hit_Events.Enable");
		final boolean headShots = this.getBoolean(parent_node + ".Headshot.Enable");
		final boolean bsEnable = this.getBoolean(parent_node + ".Backstab.Enable");
		final boolean critEnable = this.getBoolean(parent_node + ".Critical_Hits.Enable");
		final boolean fireEnable = this.getBoolean(parent_node + ".Shooting.Projectile_Incendiary.Enable");
		final int fireDuration = this.getInt(parent_node + ".Shooting.Projectile_Incendiary.Duration");
		final boolean zapEnable = this.getBoolean(parent_node + ".Lightning.Enable");
		final boolean resetHits = this.getBoolean(parent_node + ".Abilities.Reset_Hit_Cooldown");
		final boolean flightEnable = this.getBoolean(parent_node + ".Damage_Based_On_Flight_Time.Enable");
		final String makeSpeak = this.getString(parent_node + ".Extras.Make_Victim_Speak");
		final String makeRunCmd = this.getString(parent_node + ".Extras.Make_Victim_Run_Commmand");
		final String runConsole = this.getString(parent_node + ".Extras.Run_Console_Command");
		final int knockBack = this.getInt(parent_node + ".Abilities.Knockback");
		final String bonusDrops = this.getString(parent_node + ".Abilities.Bonus_Drops");
		final int activTime = this.getInt(parent_node + ".Explosions.Projectile_Activation_Time");
		int projFlight = 0;
		double projTotalDmg = this.getInt(parent_node + ".Shooting.Projectile_Damage");
		boolean BS = false;
		boolean crit = false;
		boolean boomHS = false;
		if (flightEnable && !energyMode) {
			final int dmgPerTick = this.getInt(parent_node + ".Damage_Based_On_Flight_Time.Bonus_Damage_Per_Tick");
			final int flightMax = this.getInt(parent_node + ".Damage_Based_On_Flight_Time.Maximum_Damage");
			final int flightMin = this.getInt(parent_node + ".Damage_Based_On_Flight_Time.Minimum_Damage");
			final boolean negDmg = dmgPerTick < 0 && flightMax < 0;
			projFlight = objProj.getTicksLived();
			int tickDmgTotal = projFlight * dmgPerTick;
			if (tickDmgTotal < flightMin && flightMin != 0) {
				tickDmgTotal = 0;
			}
			if (!negDmg) {
				if (tickDmgTotal > flightMax && flightMax != 0) {
					tickDmgTotal = flightMax;
				}
			}
			else if (tickDmgTotal < flightMax && flightMax != 0) {
				tickDmgTotal = flightMax;
			}
			projTotalDmg += tickDmgTotal;
		}
		if (bsEnable) {
			final int bsBonusDmg = this.getInt(parent_node + ".Backstab.Bonus_Damage");
			final double faceAngle = victim.getLocation().getDirection().dot(shooter.getLocation().getDirection());
			if (faceAngle > 0.0) {
				BS = true;
				projTotalDmg += bsBonusDmg;
			}
		}
		if (critEnable) {
			final int critBonus = this.getInt(parent_node + ".Critical_Hits.Bonus_Damage");
			final int critChance = this.getInt(parent_node + ".Critical_Hits.Chance");
			final Random ranGen = new Random();
			final int Chance = ranGen.nextInt(100);
			if (Chance <= critChance) {
				crit = true;
				projTotalDmg += critBonus;
			}
		}
		if (headShots && !energyMode && this.csminion.isHesh(objProj, victim, projSpeed)) {
			boomHS = true;
			projTotalDmg += this.getInt(parent_node + ".Headshot.Bonus_Damage");
		}
		projTotalDmg = this.csminion.getSuperDamage(victim.getType(), parent_node, projTotalDmg);
		if (projTotalDmg < 0.0) {
			projTotalDmg = 0.0;
		}
		final WeaponDamageEntityEvent weaponEvent = new WeaponDamageEntityEvent(shooter, victim, objProj, parent_node, projTotalDmg, boomHS, BS, crit);
		this.getServer().getPluginManager().callEvent(weaponEvent);
		if (weaponEvent.isCancelled()) {
			if (event != null) {
				event.setCancelled(true);
			}
			return;
		}
		if (resetHits) {
			this.setTempVulnerability(victim);
		}
		if (event != null) {
			event.setDamage(weaponEvent.getDamage());
		}
		else {
			victim.damage(weaponEvent.getDamage(), shooter);
		}
		if (knockBack != 0) {
			victim.setVelocity(shooter.getLocation().getDirection().multiply(knockBack));
		}
		if (energyMode || objProj.getTicksLived() >= activTime) {
			this.projectileExplosion(victim, parent_node, false, shooter, false, false, null, null, false, 0);
		}
		if (zapEnable) {
			final boolean zapNoDmg = this.getBoolean(parent_node + ".Lightning.No_Damage");
			this.csminion.projectileLightning(victim.getLocation(), zapNoDmg);
		}
		if (fireEnable && fireDuration != 0) {
			victim.setFireTicks(fireDuration);
		}
		final String flyTime = String.valueOf(projFlight);
		final String dmgTotal = String.valueOf(projTotalDmg);
		final String nameShooter = shooter.getName();
		String nameVic = "Entity";
		if (victim instanceof Player) {
			nameVic = victim.getName();
		}
		else if (victim instanceof LivingEntity) {
			nameVic = victim.getType().getName();
		}
		if (boomHS) {
			this.sendPlayerMessage(shooter, parent_node, ".Headshot.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
			this.sendPlayerMessage(victim, parent_node, ".Headshot.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
			this.playSoundEffects(shooter, parent_node, ".Headshot.Sounds_Shooter", false, null);
			this.playSoundEffects(victim, parent_node, ".Headshot.Sounds_Victim", false, null);
			this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Headshot", false, null);
			this.csminion.displayFireworks(victim, parent_node, ".Fireworks.Firework_Headshot");
			this.csminion.givePotionEffects(shooter, parent_node, ".Potion_Effects.Potion_Effect_Shooter", "head");
			this.csminion.givePotionEffects(victim, parent_node, ".Potion_Effects.Potion_Effect_Victim", "head");
		}
		if (crit) {
			this.sendPlayerMessage(shooter, parent_node, ".Critical_Hits.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
			this.sendPlayerMessage(victim, parent_node, ".Critical_Hits.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
			this.playSoundEffects(shooter, parent_node, ".Critical_Hits.Sounds_Shooter", false, null);
			this.playSoundEffects(victim, parent_node, ".Critical_Hits.Sounds_Victim", false, null);
			this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Critical", false, null);
			this.csminion.displayFireworks(victim, parent_node, ".Fireworks.Firework_Critical");
			this.csminion.givePotionEffects(shooter, parent_node, ".Potion_Effects.Potion_Effect_Shooter", "crit");
			this.csminion.givePotionEffects(victim, parent_node, ".Potion_Effects.Potion_Effect_Victim", "crit");
		}
		if (BS) {
			this.sendPlayerMessage(shooter, parent_node, ".Backstab.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
			this.sendPlayerMessage(victim, parent_node, ".Backstab.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
			this.playSoundEffects(shooter, parent_node, ".Backstab.Sounds_Shooter", false, null);
			this.playSoundEffects(victim, parent_node, ".Backstab.Sounds_Victim", false, null);
			this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Backstab", false, null);
			this.csminion.displayFireworks(victim, parent_node, ".Fireworks.Firework_Backstab");
			this.csminion.givePotionEffects(shooter, parent_node, ".Potion_Effects.Potion_Effect_Shooter", "back");
			this.csminion.givePotionEffects(victim, parent_node, ".Potion_Effects.Potion_Effect_Victim", "back");
		}
		if (hitEnable) {
			this.sendPlayerMessage(shooter, parent_node, ".Hit_Events.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
			this.sendPlayerMessage(victim, parent_node, ".Hit_Events.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
			this.playSoundEffects(shooter, parent_node, ".Hit_Events.Sounds_Shooter", false, null);
			this.playSoundEffects(victim, parent_node, ".Hit_Events.Sounds_Victim", false, null);
		}
		this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Impact_Anything", false, null);
		this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Hit", false, null);
		this.csminion.displayFireworks(victim, parent_node, ".Fireworks.Firework_Hit");
		this.csminion.givePotionEffects(shooter, parent_node, ".Potion_Effects.Potion_Effect_Shooter", "hit");
		this.csminion.givePotionEffects(victim, parent_node, ".Potion_Effects.Potion_Effect_Victim", "hit");
		if (this.spawnEntities(victim, parent_node, ".Spawn_Entity_On_Hit.EntityType_Baby_Explode_Amount", shooter)) {
			this.sendPlayerMessage(shooter, parent_node, ".Spawn_Entity_On_Hit.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
			this.sendPlayerMessage(victim, parent_node, ".Spawn_Entity_On_Hit.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
		}
		if (victim instanceof Player) {
			if (makeSpeak != null) {
				((Player)victim).chat(this.variableParser(makeSpeak, nameShooter, nameVic, flyTime, dmgTotal));
			}
			if (makeRunCmd != null) {
				this.executeCommands(victim, parent_node, ".Extras.Make_Victim_Run_Commmand", nameShooter, nameVic, flyTime, dmgTotal, false);
			}
		}
		if (runConsole != null) {
			this.executeCommands(shooter, parent_node, ".Extras.Run_Console_Command", nameShooter, nameVic, flyTime, dmgTotal, true);
		}
		if (!(victim instanceof Player) && victim.getHealth() <= 0.0 && bonusDrops != null) {
			final String[] dropInfo = bonusDrops.split(",");
			String[] array;
			for (int length = (array = dropInfo).length, i = 0; i < length; ++i) {
				final String drop = array[i];
				try {
					shooter.getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(MaterialManager.getMaterial(drop)));
				}
				catch (IllegalArgumentException ex) {
					this.printM("'" + drop + "' of weapon '" + parent_node + "' for 'Bonus_Drops' is not a valid item ID!");
					break;
				}
			}
		}
	}

	public void setTempVulnerability(final LivingEntity ent) {
		final int maxNoDamageTicks = ent.getMaximumNoDamageTicks();
		ent.setMaximumNoDamageTicks(0);
		ent.setNoDamageTicks(0);
		if (!ent.hasMetadata("[CS] NHC")) {
			ent.setMetadata("[CS] NHC", new FixedMetadataValue(this, true));
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					ent.setMaximumNoDamageTicks(maxNoDamageTicks);
					ent.setNoDamageTicks(0);
					ent.removeMetadata("[CS] NHC", CSDirector.this.plugin);
				}
			}, 2L);
		}
	}

	@EventHandler
	public void onProjectileHit(final ProjectileHitEvent event) {
		if ((event.getEntity() instanceof Arrow || event.getEntity() instanceof Egg || event.getEntity() instanceof Snowball) && event.getEntity().hasMetadata("projParentNode") && event.getEntity().getShooter() instanceof Player) {
			final Player shooter = (Player)event.getEntity().getShooter();
			final Projectile objProj = event.getEntity();
			final String parentNode = objProj.getMetadata("projParentNode").get(0).asString();
			Location destLoc = objProj.getLocation();
			objProj.removeMetadata(parentNode, this);
			final boolean collided = event.getEntity().hasMetadata("Collided");
			final boolean terrain = this.getBoolean(parentNode + ".Particles.Particle_Terrain");
			final boolean airstrike = this.getBoolean(parentNode + ".Airstrikes.Enable");
			final boolean zapEnable = this.getBoolean(parentNode + ".Lightning.Enable");
			final boolean zapNoDam = this.getBoolean(parentNode + ".Lightning.No_Damage");
			final boolean zapImpact = this.getBoolean(parentNode + ".Lightning.On_Impact_With_Anything");
			final boolean arrowImpact = this.getBoolean(parentNode + ".Shooting.Remove_Arrows_On_Impact");
			final boolean explodeImpact = this.getBoolean(parentNode + ".Explosions.On_Impact_With_Anything");
			final int actTime = this.getInt(parentNode + ".Explosions.Projectile_Activation_Time");
			final String breakBlocks = this.getString(parentNode + ".Abilities.Break_Blocks");
			final String[] blockList = (breakBlocks == null) ? null : breakBlocks.split("-");
			Block hitBlock = objProj.getLocation().getBlock();
			if (!collided) {
				double projSpeed = this.getInt(parentNode + ".Shooting.Projectile_Speed") * 0.1;
				if (projSpeed > 256.0) {
					projSpeed = 256.0;
				}
				double i = 0.0;
				while (i <= projSpeed) {
					final Location finalLoc = objProj.getLocation();
					final Vector direction = objProj.getVelocity().normalize();
					direction.multiply(i);
					finalLoc.add(direction);
					hitBlock = finalLoc.getBlock();
					if (hitBlock.getType() != Material.AIR) {
						if (terrain) {
							objProj.getWorld().playEffect(finalLoc, Effect.STEP_SOUND, (Object)hitBlock.getType());
						}
						if (blockList == null) {
							break;
						}
						if (blockList.length != 2) {
							break;
						}
						boolean passWhiteList = false;
						final boolean whiteList = Boolean.parseBoolean(blockList[0]);
						final String blockMat = hitBlock.getType().toString();
						if (!this.csminion.regionCheck(objProj, parentNode)) {
							break;
						}
						String[] split;
						for (int length = (split = blockList[1].split(",")).length, j = 0; j < length; ++j) {
							String compMat = split[j];
							final boolean hasSecdat = compMat.contains("~");
							final String[] secdat = hasSecdat ? compMat.split("~") : new String[] { "", "" };
							final Material mat = MaterialManager.getMaterial(compMat);
							if (mat != null) {
								secdat[0] = mat.toString();
								compMat = mat.toString();
							}
							if (blockMat.equals(hasSecdat ? secdat[0] : compMat) && (!hasSecdat || hitBlock.getData() == Byte.valueOf(secdat[1]))) {
								if (!whiteList) {
									final List<Block> brokenBlocks = new ArrayList<Block>();
									brokenBlocks.add(hitBlock);
									final EntityExplodeEvent breakBlockEvent = new EntityExplodeEvent(objProj, objProj.getLocation(), brokenBlocks, 0.0f);
									this.getServer().getPluginManager().callEvent(breakBlockEvent);
									hitBlock.setType(Material.AIR);
									break;
								}
								passWhiteList = true;
							}
						}
						if (whiteList && !passWhiteList) {
							final List<Block> brokenBlocks2 = new ArrayList<Block>();
							brokenBlocks2.add(hitBlock);
							final EntityExplodeEvent breakBlockEvent2 = new EntityExplodeEvent(objProj, objProj.getLocation(), brokenBlocks2, 0.0f);
							this.getServer().getPluginManager().callEvent(breakBlockEvent2);
							hitBlock.setType(Material.AIR);
							break;
						}
						break;
					}
					else {
						destLoc = finalLoc;
						++i;
					}
				}
				if (explodeImpact && objProj.getTicksLived() >= actTime) {
					final Entity tempOrb = objProj.getWorld().spawn(destLoc, (Class)ExperienceOrb.class);
					this.projectileExplosion(tempOrb, parentNode, false, shooter, false, false, null, null, false, 0);
					tempOrb.remove();
				}
				if (zapEnable && zapImpact) {
					this.csminion.projectileLightning(destLoc, zapNoDam);
				}
				this.csminion.giveParticleEffects(null, parentNode, ".Particles.Particle_Impact_Anything", false, destLoc);
			}
			this.playSoundEffects(null, parentNode, ".Hit_Events.Sounds_Impact", false, destLoc);
			this.csminion.giveParticleEffects(null, parentNode, ".Airstrikes.Particle_Call_Airstrike", false, destLoc);
			if (airstrike) {
				this.csminion.callAirstrike(event.getEntity(), parentNode, shooter);
			}
			if (arrowImpact && objProj.getType() == EntityType.ARROW) {
				objProj.remove();
			}
			final WeaponHitBlockEvent blockHitEvent = new WeaponHitBlockEvent(shooter, objProj, parentNode, hitBlock, destLoc.getBlock());
			this.getServer().getPluginManager().callEvent(blockHitEvent);
		}
	}

	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent event) {
		final Entity boomer = event.getEntity();
		if (boomer instanceof TNTPrimed) {
			if (boomer.hasMetadata("CS_potex")) {
				final String parent_node = boomer.getMetadata("CS_potex").get(0).asString();
				this.playSoundEffects(boomer, parent_node, ".Explosions.Sounds_Explode", false, null);
			}
			if (boomer.hasMetadata("nullify") && event.blockList() != null) {
				event.blockList().clear();
			}
			if (MaterialManager.isSkullBlock(boomer.getLocation().getBlock()) && !boomer.hasMetadata("C4_Friendly")) {
				final BlockState state = boomer.getLocation().getBlock().getState();
				if (state instanceof Skull) {
					Skull skull;
					try {
						skull = (Skull)state;
					}
					catch (ClassCastException ex) {
						return;
					}
					if (skull.getOwner().contains("\u060c")) {
						boomer.getLocation().getBlock().removeMetadata("CS_transformers", this);
						boomer.getLocation().getBlock().setType(Material.AIR);
					}
				}
			}
		}
		else if ((boomer instanceof WitherSkull || boomer instanceof LargeFireball) && boomer.hasMetadata("projParentNode") && ((Projectile)boomer).getShooter() instanceof Player) {
			final Player shooter = (Player)((Projectile)boomer).getShooter();
			final String parent_node2 = boomer.getMetadata("projParentNode").get(0).asString();
			if (boomer.getTicksLived() >= this.getInt(parent_node2 + ".Explosions.Projectile_Activation_Time")) {
				this.projectileExplosion(boomer, parent_node2, false, shooter, false, false, null, null, false, 0);
			}
			event.setCancelled(true);
		}
	}

	public void playSoundEffectsScaled(final Entity player, final String parentNode, final String childNode, final boolean reload, final double scale, final String... customSounds) {
		final String soundList = (customSounds.length == 0) ? this.getString(parentNode + childNode) : customSounds[0];
		if (soundList == null) {
			return;
		}
		String[] split;
		for (int length = (split = soundList.replaceAll(" ", "").split(",")).length, i = 0; i < length; ++i) {
			final String soundStrip = split[i];
			final String[] soundInfo = soundStrip.split("-");
			if (soundInfo.length != 4) {
				this.printM("'" + soundStrip + "' of weapon '" + parentNode + "' has an invalid format! The correct format is: Sound-Volume-Pitch-Delay!");
			}
			else {
				try {
					final Sound sound = SoundManager.get(soundInfo[0].toUpperCase());
					final float volume = Float.parseFloat(soundInfo[1]);
					final float pitch = Float.parseFloat(soundInfo[2]);
					final long delay = (long)(Long.parseLong(soundInfo[3]) * scale);
					final int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
						}
					}, delay);
					if (reload) {
						final String playerName = player.getName();
						Collection<Integer> taskIDs = this.global_reload_IDs.get(playerName);
						if (taskIDs == null) {
							taskIDs = new ArrayList<Integer>();
							this.global_reload_IDs.put(playerName, taskIDs);
						}
						taskIDs.add(taskID);
					}
				}
				catch (IllegalArgumentException ex) {
					this.printM("'" + soundStrip + "' of weapon '" + parentNode + "' contains either an invalid number or sound!");
				}
			}
		}
	}

	public void playSoundEffects(final Entity player, final String parentNode, final String childNode, final boolean reload, final Location givenCoord, final String... customSounds) {
		final String soundList = (customSounds.length == 0) ? this.getString(parentNode + childNode) : customSounds[0];
		if (soundList == null) {
			return;
		}
		String[] split;
		for (int length = (split = soundList.replaceAll(" ", "").split(",")).length, i = 0; i < length; ++i) {
			final String soundStrip = split[i];
			final String[] soundInfo = soundStrip.split("-");
			if (soundInfo.length != 4) {
				this.printM("'" + soundStrip + "' of weapon '" + parentNode + "' has an invalid format! The correct format is: Sound-Volume-Pitch-Delay!");
			}
			else {
				try {
					final Sound sound = SoundManager.get(soundInfo[0].toUpperCase());
					final float volume = Float.parseFloat(soundInfo[1]);
					final float pitch = Float.parseFloat(soundInfo[2]);
					final long delay = Long.parseLong(soundInfo[3]);
					final int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							if (player == null) {
								givenCoord.getWorld().playSound(givenCoord, sound, volume, pitch);
							}
							else {
								player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
							}
						}
					}, delay);
					if (reload) {
						final String playerName = player.getName();
						Collection<Integer> taskIDs = this.global_reload_IDs.get(playerName);
						if (taskIDs == null) {
							taskIDs = new ArrayList<Integer>();
							this.global_reload_IDs.put(playerName, taskIDs);
						}
						taskIDs.add(taskID);
					}
				}
				catch (IllegalArgumentException ex) {
					this.printM("'" + soundStrip + "' of weapon '" + parentNode + "' contains either an invalid number or sound!");
				}
			}
		}
	}

	public void fireProjectile(final Player player, final String parentNode, final boolean leftClick) {
		final int gunSlot = player.getInventory().getHeldItemSlot();
		final int shootDelay = this.getInt(parentNode + ".Shooting.Delay_Between_Shots");
		final int projAmount = this.getInt(parentNode + ".Shooting.Projectile_Amount");
		final boolean ammoEnable = this.getBoolean(parentNode + ".Ammo.Enable");
		final boolean oneTime = this.getBoolean(parentNode + ".Extras.One_Time_Use");
		final String deviceType = this.getString(parentNode + ".Explosive_Devices.Device_Type");
		final String proType = this.getString(parentNode + ".Shooting.Projectile_Type");
		final ItemStack item = player.getInventory().getItemInHand();
		final boolean isFullyAuto = this.getBoolean(parentNode + ".Fully_Automatic.Enable");
		int fireRate = this.getInt(parentNode + ".Fully_Automatic.Fire_Rate");
		final boolean burstEnable = this.getBoolean(parentNode + ".Burstfire.Enable");
		int burstShots = this.getInt(parentNode + ".Burstfire.Shots_Per_Burst");
		int burstDelay = this.getInt(parentNode + ".Burstfire.Delay_Between_Shots_In_Burst");
		final boolean meleeMode = this.getBoolean(parentNode + ".Item_Information.Melee_Mode");
		final boolean shootDisable = this.getBoolean(parentNode + ".Shooting.Disable");
		final boolean reloadOn = this.getBoolean(parentNode + ".Reload.Enable");
		final boolean dualWield = this.isDualWield(player, parentNode, item);
		if (shootDisable || meleeMode) {
			return;
		}
		final Vector shiftVector = this.determinePosition(player, dualWield, leftClick);
		final Location projLoc = player.getEyeLocation().toVector().add(shiftVector.multiply(0.2)).toLocation(player.getWorld());
		final String actType = this.getString(parentNode + ".Firearm_Action.Type");
		final boolean tweakyAction = actType != null && (actType.toLowerCase().contains("bolt") || actType.toLowerCase().contains("lever") || actType.toLowerCase().contains("pump"));
		if (player.hasMetadata(parentNode + "shootDelay" + gunSlot + leftClick)) {
			return;
		}
		if (player.hasMetadata(parentNode + "noShooting" + gunSlot)) {
			return;
		}
		if (player.hasMetadata("togglesnoShooting" + gunSlot)) {
			return;
		}
		if (oneTime && ammoEnable) {
			player.sendMessage(this.heading + "For '" + parentNode + "' - the 'One_Time_Use' node is incompatible with weapons using the Ammo module.");
			return;
		}
		if (proType != null && (proType.equalsIgnoreCase("grenade") || proType.equalsIgnoreCase("flare")) && projAmount == 0) {
			player.sendMessage(this.heading + "The weapon '" + parentNode + "' is missing a value for 'Projectile_Amount'.");
			return;
		}
		if (isFullyAuto) {
			if (burstEnable) {
				player.sendMessage(this.heading + "The weapon '" + parentNode + "' is using Fully_Automatic and Burstfire at the same time. Pick one; you cannot enable both!");
				return;
			}
			if (shootDelay > 1) {
				player.sendMessage(this.heading + "For '" + parentNode + "' - the Fully_Automatic module can only be used if 'Delay_Between_Shots' is removed or set to a value no greater than 1.");
				return;
			}
			if (fireRate <= 0 || fireRate > 16) {
				player.sendMessage(this.heading + "The weapon '" + parentNode + "' has an invalid value for 'Fire_Rate'. The accepted values are 1 to 16.");
				return;
			}
		}
		if (this.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains("\u1d3f")) {
			if (this.getAmmoBetweenBrackets(player, parentNode, item) <= 0) {
				this.reloadAnimation(player, parentNode);
				return;
			}
			if (!dualWield) {
				this.terminateReload(player);
				this.removeInertReloadTag(player, 0, true);
			}
			else {
				final int[] ammoReading = this.grabDualAmmo(item, parentNode);
				if ((ammoReading[0] > 0 && leftClick) || (ammoReading[1] > 0 && !leftClick)) {
					this.terminateReload(player);
					this.removeInertReloadTag(player, 0, true);
				}
			}
		}
		if (player.hasMetadata(parentNode + "reloadShootDelay" + gunSlot)) {
			return;
		}
		if (!tweakyAction && (actType == null || !actType.equalsIgnoreCase("slide") || !item.getItemMeta().getDisplayName().contains("\u25ab"))) {
			player.setMetadata(parentNode + "shootDelay" + gunSlot + leftClick, new FixedMetadataValue(this, true));
			this.csminion.tempVars(player, parentNode + "shootDelay" + gunSlot + leftClick, (long)shootDelay);
		}
		final String ammoInfo = this.getString(parentNode + ".Ammo.Ammo_Item_ID");
		final boolean ammoPerShot = this.getBoolean(parentNode + ".Ammo.Take_Ammo_Per_Shot");
		final double zoomAcc = this.getDouble(parentNode + ".Scope.Zoom_Bullet_Spread");
		final boolean sneakOn = this.getBoolean(parentNode + ".Sneak.Enable");
		final boolean sneakToShoot = this.getBoolean(parentNode + ".Sneak.Sneak_Before_Shooting");
		final boolean sneakNoRec = this.getBoolean(parentNode + ".Sneak.No_Recoil");
		final double sneakAcc = this.getDouble(parentNode + ".Sneak.Bullet_Spread");
		final boolean exploDevs = this.getBoolean(parentNode + ".Explosive_Devices.Enable");
		final boolean takeAmmo = this.getBoolean(parentNode + ".Reload.Take_Ammo_On_Reload");
		final String dragRemInfo = this.getString(parentNode + ".Shooting.Removal_Or_Drag_Delay");
		final String[] dragRem = (dragRemInfo == null) ? null : dragRemInfo.split("-");
		if (dragRem != null) {
			try {
				Integer.valueOf(dragRem[0]);
			}
			catch (NumberFormatException ex) {
				player.sendMessage(this.heading + "For the weapon '" + parentNode + "', the 'Removal_Or_Drag_Delay' node is incorrectly configured.");
				return;
			}
		}
		if (this.getBoolean(parentNode + ".Ammo.Take_Ammo_On_Reload")) {
			player.sendMessage(this.heading + "For the weapon '" + parentNode + "', the Ammo module does not support the 'Take_Ammo_On_Reload' node. Did you mean to place it in the Reload module?");
			return;
		}
		if (ammoEnable) {
			if (!takeAmmo && !ammoPerShot) {
				player.sendMessage(this.heading + "The weapon '" + parentNode + "' has enabled the Ammo module, but at least one of the following nodes need to be set to true: Take_Ammo_On_Reload, Take_Ammo_Per_Shot.");
				return;
			}
			if (!this.csminion.containsItemStack(player, ammoInfo, 1, parentNode)) {
				final boolean isPumpOrBolt = actType != null && !actType.equalsIgnoreCase("pump") && !actType.equalsIgnoreCase("bolt");
				final boolean hasLoadedChamber = item.getItemMeta().getDisplayName().contains("\u25aa �");
				if (ammoPerShot || (takeAmmo && this.getAmmoBetweenBrackets(player, parentNode, item) == 0 && (isPumpOrBolt || !hasLoadedChamber))) {
					this.playSoundEffects(player, parentNode, ".Ammo.Sounds_Shoot_With_No_Ammo", false, null);
					return;
				}
			}
		}
		if (sneakToShoot && (!player.isSneaking() || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)) {
			return;
		}
		if (this.checkBoltPosition(player, parentNode)) {
			return;
		}
		if (!burstEnable) {
			burstShots = 1;
		}
		if (isFullyAuto) {
			burstShots = 5;
			burstDelay = 1;
		}
		final double projSpeed = this.getInt(parentNode + ".Shooting.Projectile_Speed") * 0.1;
		final boolean setOnFire = this.getBoolean(parentNode + ".Shooting.Projectile_Flames");
		final boolean noBulletDrop = this.getBoolean(parentNode + ".Shooting.Remove_Bullet_Drop");
		if (this.getBoolean(parentNode + ".Scope.Zoom_Before_Shooting") && !player.hasMetadata("ironsights")) {
			return;
		}
		final int shootReloadBuffer = this.getInt(parentNode + ".Reload.Shoot_Reload_Buffer");
		if (shootReloadBuffer > 0) {
			Map<Integer, Long> lastShot = this.last_shot_list.get(player.getName());
			if (lastShot == null) {
				lastShot = new HashMap<Integer, Long>();
				this.last_shot_list.put(player.getName(), lastShot);
			}
			lastShot.put(gunSlot, System.currentTimeMillis());
		}
		int burstStart = 0;
		if (isFullyAuto) {
			final WeaponFireRateEvent event = new WeaponFireRateEvent(player, parentNode, item, fireRate);
			this.getServer().getPluginManager().callEvent(event);
			fireRate = event.getFireRate();
			final String playerName = player.getName();
			if (!this.rpm_ticks.containsKey(playerName)) {
				this.rpm_ticks.put(playerName, 1);
			}
			if (!this.rpm_shots.containsKey(playerName)) {
				this.rpm_shots.put(playerName, 0);
			}
			burstStart = this.rpm_shots.get(playerName);
			this.rpm_shots.put(playerName, 5);
		}
		final int fireRateFinal = fireRate;
		final int itemSlot = player.getInventory().getHeldItemSlot();
		for (int burst = burstStart; burst < burstShots; ++burst) {
			final boolean isLastShot = burst >= burstShots - 1;
			final int task_ID = Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					if (isFullyAuto) {
						final String playerName = player.getName();
						final int shotsLeft = CSDirector.this.rpm_shots.get(playerName) - 1;
						CSDirector.this.rpm_shots.put(playerName, shotsLeft);
						final int tick = CSDirector.this.rpm_ticks.get(playerName);
						CSDirector.this.rpm_ticks.put(playerName, (tick >= 20) ? 1 : (tick + 1));
						if (shotsLeft == 0) {
							CSDirector.this.burst_task_IDs.remove(playerName);
						}
						if (!CSDirector.this.isValid(tick, fireRateFinal)) {
							return;
						}
					}
					else if (isLastShot) {
						CSDirector.this.burst_task_IDs.remove(player.getName());
					}
					final ItemStack item = player.getInventory().getItemInHand();
					if (!oneTime) {
						if (CSDirector.this.switchedTheItem(player, parentNode) || itemSlot != player.getInventory().getHeldItemSlot()) {
							CSDirector.this.unscopePlayer(player);
							CSDirector.this.terminateAllBursts(player);
							return;
						}
						boolean normalAction = false;
						if (actType == null) {
							normalAction = true;
							final String attachType = CSDirector.this.getAttachment(parentNode, item)[0];
							final String filter = item.getItemMeta().getDisplayName();
							if (attachType == null || !attachType.equalsIgnoreCase("accessory")) {
								if (filter.contains("\u25aa �")) {
									CSDirector.this.csminion.setItemName(item, filter.replaceAll("\u25aa �", "�"));
								}
								else if (filter.contains("\u25ab �")) {
									CSDirector.this.csminion.setItemName(item, filter.replaceAll("\u25ab �", "�"));
								}
								else if (filter.contains("\u06d4 �")) {
									CSDirector.this.csminion.setItemName(item, filter.replaceAll("\u06d4 �", "�"));
								}
							}
						}
						else if (!tweakyAction) {
							normalAction = true;
						}
						if (ammoEnable && ammoPerShot && !CSDirector.this.csminion.containsItemStack(player, ammoInfo, 1, parentNode)) {
							CSDirector.this.burst_task_IDs.remove(player.getName());
							return;
						}
						if (reloadOn) {
							if (item.getItemMeta().getDisplayName().contains("\u1d3f")) {
								return;
							}
							final int detectedAmmo = CSDirector.this.getAmmoBetweenBrackets(player, parentNode, item);
							if (normalAction) {
								if (detectedAmmo <= 0) {
									CSDirector.this.reloadAnimation(player, parentNode);
									return;
								}
								if (!dualWield) {
									CSDirector.this.ammoOperation(player, parentNode, detectedAmmo, item);
								}
								else if (!CSDirector.this.ammoSpecOps(player, parentNode, detectedAmmo, item, leftClick)) {
									return;
								}
							}
						}
						else {
							final String itemName = item.getItemMeta().getDisplayName();
							if (itemName.contains("�") && !itemName.contains(String.valueOf('\u00d7')) && !exploDevs) {
								CSDirector.this.csminion.replaceBrackets(item, String.valueOf('\u00d7'), parentNode);
							}
						}
					}
					double bulletSpread = CSDirector.this.getDouble(parentNode + ".Shooting.Bullet_Spread");
					if (player.isSneaking() && sneakOn) {
						bulletSpread = sneakAcc;
					}
					if (player.hasMetadata("ironsights")) {
						bulletSpread = zoomAcc;
					}
					if (bulletSpread == 0.0) {
						bulletSpread = 0.1;
					}
					final boolean noVertRecoil = CSDirector.this.getBoolean(parentNode + ".Abilities.No_Vertical_Recoil");
					final boolean jetPack = CSDirector.this.getBoolean(parentNode + ".Abilities.Jetpack_Mode");
					final double recoilAmount = CSDirector.this.getInt(parentNode + ".Shooting.Recoil_Amount") * 0.1;
					if (recoilAmount != 0.0 && (!sneakOn || !sneakNoRec || !player.isSneaking())) {
						if (!jetPack) {
							final Vector velToAdd = player.getLocation().getDirection().multiply(-recoilAmount);
							if (noVertRecoil) {
								velToAdd.multiply(new Vector(1, 0, 1));
							}
							player.setVelocity(velToAdd);
						}
						else {
							player.setVelocity(new Vector(0.0, recoilAmount, 0.0));
						}
					}
					final boolean clearFall = CSDirector.this.getBoolean(parentNode + ".Shooting.Reset_Fall_Distance");
					if (clearFall) {
						player.setFallDistance(0.0f);
					}
					CSDirector.this.csminion.giveParticleEffects(player, parentNode, ".Particles.Particle_Player_Shoot", true, null);
					CSDirector.this.csminion.givePotionEffects(player, parentNode, ".Potion_Effects.Potion_Effect_Shooter", "shoot");
					CSDirector.this.csminion.displayFireworks(player, parentNode, ".Fireworks.Firework_Player_Shoot");
					CSDirector.this.csminion.runCommand(player, parentNode);
					if (CSDirector.this.getBoolean(parentNode + ".Abilities.Hurt_Effect")) {
						player.playEffect(EntityEffect.HURT);
					}
					final String projectile_type = CSDirector.this.getString(parentNode + ".Shooting.Projectile_Type");
					int timer = CSDirector.this.getInt(parentNode + ".Explosions.Explosion_Delay");
					final boolean airstrike = CSDirector.this.getBoolean(parentNode + ".Airstrikes.Enable");
					if (airstrike) {
						timer = CSDirector.this.getInt(parentNode + ".Airstrikes.Flare_Activation_Delay");
					}
					final String soundsShoot = CSDirector.this.getString(parentNode + ".Shooting.Sounds_Shoot");
					final WeaponPreShootEvent event = new WeaponPreShootEvent(player, parentNode, soundsShoot, bulletSpread, leftClick);
					CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
					CSDirector.this.playSoundEffects(player, parentNode, null, false, null, event.getSounds());
					if (event.isCancelled()) {
						return;
					}
					bulletSpread = event.getBulletSpread();
					for (int i = 0; i < projAmount; ++i) {
						final Random r = new Random();
						final double yaw = Math.toRadians(-player.getLocation().getYaw() - 90.0f);
						final double pitch = Math.toRadians(-player.getLocation().getPitch());
						final double[] spread = { 1.0, 1.0, 1.0 };
						for (int t = 0; t < 3; ++t) {
							spread[t] = (r.nextDouble() - r.nextDouble()) * bulletSpread * 0.1;
						}
						final double x = Math.cos(pitch) * Math.cos(yaw) + spread[0];
						final double y = Math.sin(pitch) + spread[1];
						final double z = -Math.sin(yaw) * Math.cos(pitch) + spread[2];
						final Vector dirVel = new Vector(x, y, z);
						if (proType != null && (proType.equalsIgnoreCase("grenade") || proType.equalsIgnoreCase("flare"))) {
							CSDirector.this.launchGrenade(player, parentNode, timer, dirVel.multiply(projSpeed), null, 0);
						}
						else if (proType.equalsIgnoreCase("energy")) {
							final PermissionAttachment attachment = player.addAttachment(CSDirector.this.plugin);
							attachment.setPermission("nocheatplus", true);
							attachment.setPermission("anticheat.check.exempt", true);
							final String proOre = CSDirector.this.getString(parentNode + ".Shooting.Projectile_Subtype");
							if (proOre == null) {
								player.sendMessage(CSDirector.this.heading + "The weapon '" + parentNode + "' does not have a value for 'Projectile_Subtype'.");
								return;
							}
							final String[] proInfo = proOre.split("-");
							if (proInfo.length != 4) {
								player.sendMessage(CSDirector.this.heading + "The value provided for 'Projectile_Subtype' of the weapon '" + parentNode + "' has an incorrect format.");
								return;
							}
							int wallLimit = 0;
							int hitCount = 0;
							int wallCount = 0;
							int range;
							int hitLimit;
							double radius;
							try {
								range = Integer.valueOf(proInfo[0]);
								hitLimit = Integer.valueOf(proInfo[3]);
								if (proInfo[2].equalsIgnoreCase("all")) {
									wallLimit = -1;
								}
								else if (!proInfo[2].equalsIgnoreCase("none")) {
									wallLimit = Integer.valueOf(proInfo[2]);
								}
								radius = Double.valueOf(proInfo[1]);
							}
							catch (NumberFormatException ex) {
								player.sendMessage(CSDirector.this.heading + "The value provided for 'Projectile_Subtype' of the weapon '" + parentNode + "' contains an invalid number.");
								break;
							}
							final Set<Block> hitBlocks = new HashSet<Block>();
							final Set<Integer> hitMobs = new HashSet<Integer>();
							final Vector vecShift = dirVel.normalize().multiply(radius);
							final Location locStart = player.getEyeLocation();
							Label_2389:
								for (double k = 0.0; k < range; k += radius) {
									locStart.add(vecShift);
									final Block hitBlock = locStart.getBlock();
									if (hitBlock.getType() == Material.AIR) {
										final FallingBlock tempEnt = player.getWorld().spawnFallingBlock(locStart, Material.AIR, (byte)0);
										for (final Entity ent : tempEnt.getNearbyEntities(radius, radius, radius)) {
											if (ent instanceof LivingEntity && ent != player && !hitMobs.contains(ent.getEntityId()) && !ent.isDead()) {
												if (ent instanceof Player) {
													ent.setMetadata("CS_Energy", new FixedMetadataValue(CSDirector.this.plugin, parentNode));
													((LivingEntity)ent).damage(0.0, player);
												}
												else {
													CSDirector.this.dealDamage(player, (LivingEntity)ent, null, parentNode);
												}
												hitMobs.add(ent.getEntityId());
												++hitCount;
												if (hitLimit != 0 && hitCount >= hitLimit) {
													break Label_2389;
												}
												continue;
											}
										}
										tempEnt.remove();
									}
									else if (wallLimit != -1 && !hitBlocks.contains(hitBlock)) {
										if (++wallCount > wallLimit) {
											break;
										}
										hitBlocks.add(hitBlock);
									}
								}
							CSDirector.this.callShootEvent(player, null, parentNode);
							CSDirector.this.playSoundEffects(player, parentNode, ".Shooting.Sounds_Projectile", false, null);
							player.removeAttachment(attachment);
						}
						else if (proType.equalsIgnoreCase("splash")) {
							final ThrownPotion splashPot = (ThrownPotion)player.getWorld().spawn(projLoc, (Class)ThrownPotion.class);
							final ItemStack potType = CSDirector.this.csminion.parseItemStack(CSDirector.this.getString(parentNode + ".Shooting.Projectile_Subtype"));
							if (potType != null) {
								try {
									splashPot.setItem(potType);
								}
								catch (IllegalArgumentException ex2) {
									player.sendMessage(CSDirector.this.heading + "The value for 'Projectile_Subtype' of weapon '" + parentNode + "' is not a splash potion!");
								}
							}
							if (setOnFire) {
								splashPot.setFireTicks(6000);
							}
							if (noBulletDrop) {
								CSDirector.this.noArcInArchery(splashPot, dirVel.multiply(projSpeed));
							}
							splashPot.setShooter(player);
							splashPot.setMetadata("projParentNode", new FixedMetadataValue(CSDirector.this.plugin, parentNode));
							splashPot.setVelocity(dirVel.multiply(projSpeed));
							CSDirector.this.callShootEvent(player, splashPot, parentNode);
							if (dragRem != null) {
								CSDirector.this.prepareTermination(splashPot, Boolean.parseBoolean(dragRem[1]), Long.valueOf(dragRem[0]));
							}
						}
						else {
							Projectile snowball;
							if (projectile_type.equalsIgnoreCase("arrow")) {
								snowball = (Projectile)player.getWorld().spawnEntity(projLoc, EntityType.ARROW);
							}
							else if (projectile_type.equalsIgnoreCase("egg")) {
								snowball = (Projectile)player.getWorld().spawnEntity(projLoc, EntityType.EGG);
								snowball.setMetadata("CS_Hardboiled", new FixedMetadataValue(CSDirector.this.plugin, true));
							}
							else if (projectile_type.equalsIgnoreCase("firework")) {
								snowball = (Projectile)player.getWorld().spawnEntity(projLoc, EntityType.FIREWORK);
								snowball.setMetadata("CS_Firework", new FixedMetadataValue(CSDirector.this.plugin, true));

							}
							else if (projectile_type.equalsIgnoreCase("fireball")) {
								snowball = player.launchProjectile((Class)LargeFireball.class);
								if (Boolean.parseBoolean(CSDirector.this.getString(parentNode + ".Shooting.Projectile_Subtype"))) {
									snowball.setMetadata("CS_NoDeflect", new FixedMetadataValue(CSDirector.this.plugin, true));
								}
							}
							else if (projectile_type.equalsIgnoreCase("witherskull")) {
								snowball = player.launchProjectile((Class)WitherSkull.class);
							}
							else {
								snowball = (Projectile)player.getWorld().spawnEntity(projLoc, EntityType.SNOWBALL);
							}
							if (setOnFire) {
								snowball.setFireTicks(6000);
							}
							if (noBulletDrop) {
								CSDirector.this.noArcInArchery(snowball, dirVel.multiply(projSpeed));
							}
							snowball.setShooter(player);
							snowball.setVelocity(dirVel.multiply(projSpeed));
							snowball.setMetadata("projParentNode", new FixedMetadataValue(CSDirector.this.plugin, parentNode));
							CSDirector.this.callShootEvent(player, snowball, parentNode);
							CSDirector.this.playSoundEffects(snowball, parentNode, ".Shooting.Sounds_Projectile", false, null);
							if (dragRem != null) {
								CSDirector.this.prepareTermination(snowball, Boolean.parseBoolean(dragRem[1]), Long.valueOf(dragRem[0]));
							}
						}
					}
				}
			}, burstDelay * burst + 1L);
			if (oneTime && burst == 0 && (deviceType == null || (!deviceType.equalsIgnoreCase("remote") && !deviceType.equalsIgnoreCase("trap")))) {
				this.csminion.oneTime(player);
			}
			final String user = player.getName();
			Collection<Integer> values = this.burst_task_IDs.get(user);
			if (values == null) {
				values = new ArrayList<Integer>();
				this.burst_task_IDs.put(user, values);
			}
			values.add(task_ID);
		}
	}

	public void noArcInArchery(final Projectile proj, final Vector direction) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				if (!proj.isDead()) {
					proj.setVelocity(direction);
					CSDirector.this.noArcInArchery(proj, direction);
				}
			}
		}, 1L);
	}

	public void callShootEvent(final Player player, final Entity objProj, final String weaponTitle) {
		final WeaponShootEvent event = new WeaponShootEvent(player, objProj, weaponTitle);
		this.getServer().getPluginManager().callEvent(event);
	}

	public void reloadAnimation(final Player player, final String parent_node, final boolean... reloadStart) {
		if (!this.getBoolean(parent_node + ".Reload.Enable") || player.hasMetadata("markOfTheReload")) {
			return;
		}
		final String playerName = player.getName();
		if (this.delayed_reload_IDs.containsKey(playerName)) {
			Bukkit.getScheduler().cancelTask(this.delayed_reload_IDs.get(playerName));
			this.delayed_reload_IDs.remove(playerName);
		}
		int relDuration = this.getInt(parent_node + ".Reload.Reload_Duration");
		final ItemStack held = player.getItemInHand();
		final boolean isStart = reloadStart.length == 0;
		final boolean takeAsMag = this.getBoolean(parent_node + ".Reload.Take_Ammo_As_Magazine");
		final boolean takeAmmo = this.getBoolean(parent_node + ".Reload.Take_Ammo_On_Reload");
		final boolean reloadIndie = this.getBoolean(parent_node + ".Reload.Reload_Bullets_Individually");
		final boolean ammoEnable = this.getBoolean(parent_node + ".Ammo.Enable");
		final String ammoInfo = this.getString(parent_node + ".Ammo.Ammo_Item_ID");
		final int openTime = this.getInt(parent_node + ".Firearm_Action.Open_Duration");
		final int closeTime = this.getInt(parent_node + ".Firearm_Action.Close_Duration") + this.getInt(parent_node + ".Firearm_Action.Reload_Close_Delay");
		boolean akimboSingleReload = false;
		String reloadSound = ".Reload.Sounds_Reloading";
		final boolean dualWield = this.isDualWield(player, parent_node, held);
		final int reloadAmt = dualWield ? (this.getReloadAmount(player, parent_node, held) * 2) : this.getReloadAmount(player, parent_node, held);
		final String replacer = dualWield ? (reloadAmt / 2 + " | " + reloadAmt / 2) : String.valueOf(reloadAmt);
		final String actionType = dualWield ? null : this.getString(parent_node + ".Firearm_Action.Type");
		if (reloadAmt <= 0) {
			player.sendMessage(this.heading + "The weapon '" + parent_node + "' is using the Reload module, but is missing a valid value for 'Reload_Amount'.");
			return;
		}
		if (this.getBoolean(parent_node + ".Reload.Destroy_When_Empty") && held != null && held.getType() != Material.AIR && held.hasItemMeta()) {
			if (this.getAmmoBetweenBrackets(player, parent_node, held) == 0) {
				final boolean validAction = actionType == null || actionType.equalsIgnoreCase("slide") || actionType.equalsIgnoreCase("break") || actionType.equalsIgnoreCase("revolver");
				if (validAction || !held.getItemMeta().getDisplayName().contains("\u25aa")) {
					player.setItemInHand(null);
				}
			}
			return;
		}
		if (this.getBoolean("Merged_Reload.Disable") && held.getAmount() > 1) {
			final String deniedMsg = this.getString("Merged_Reload.Message_Denied");
			if (deniedMsg != null) {
				player.sendMessage(deniedMsg);
			}
			this.playSoundEffects(player, "Merged_Reload", "Sounds_Denied", false, null);
			return;
		}
		boolean boltAct = false;
		final boolean pumpAct = actionType != null && actionType.equalsIgnoreCase("pump");
		boolean breakAct = false;
		boolean slide = false;
		if (actionType != null) {
			if (actionType.equalsIgnoreCase("break") || actionType.equalsIgnoreCase("revolver")) {
				breakAct = true;
			}
			else if (actionType.equalsIgnoreCase("slide")) {
				slide = true;
			}
			else if (actionType.equalsIgnoreCase("bolt") || actionType.equalsIgnoreCase("lever")) {
				boltAct = true;
			}
		}
		final boolean finalBreakAct = breakAct;
		final boolean isSwitched = this.switchedTheItem(player, parent_node);
		final boolean isOutOfAmmo = takeAmmo && ammoEnable && !this.csminion.containsItemStack(player, ammoInfo, 1, parent_node);
		if (isSwitched || isOutOfAmmo) {
			this.removeInertReloadTag(player, 0, true);
			if (isOutOfAmmo) {
				player.removeMetadata("markOfTheReload", this);
				if (boltAct && !held.getItemMeta().getDisplayName().contains("\u25aa") && !held.getItemMeta().getDisplayName().contains("�0")) {
					this.correctBoltPosition(player, parent_node, false, closeTime, false, true, pumpAct, false);
				}
			}
			return;
		}
		if (!dualWield) {
			final String attachType = this.getAttachment(parent_node, held)[0];
			final String displayName = held.getItemMeta().getDisplayName();
			final boolean isAccessory = attachType != null && attachType.equalsIgnoreCase("accessory");
			final boolean boltFull = boltAct && displayName.contains("\u25aa �" + (reloadAmt - 1)) && !isAccessory;
			if (boltFull) {
				player.removeMetadata("markOfTheReload", this);
				return;
			}
			if (displayName.contains("�" + reloadAmt + "�") || (isAccessory && displayName.contains(reloadAmt + "�")) || (attachType != null && attachType.equalsIgnoreCase("main") && displayName.contains("�" + reloadAmt))) {
				if (finalBreakAct) {
					this.checkBoltPosition(player, parent_node);
				}
				else if (!displayName.contains("\u25aa")) {
					this.correctBoltPosition(player, parent_node, false, closeTime, false, true, pumpAct, false);
				}
				player.removeMetadata("markOfTheReload", this);
				return;
			}
			if (slide && displayName.contains("\u25ab") && openTime > 0) {
				this.correctBoltPosition(player, parent_node, true, openTime, true, false, false, false);
				return;
			}
			if (!pumpAct && !slide && !isAccessory) {
				if (!finalBreakAct && (displayName.contains("\u25aa") || displayName.contains("\u25ab"))) {
					this.correctBoltPosition(player, parent_node, true, openTime, true, false, false, false);
					return;
				}
				if (displayName.contains("\u25aa")) {
					this.correctBoltPosition(player, parent_node, true, openTime, true, false, false, false);
					return;
				}
			}
		}
		else {
			final int[] ammoReading = this.grabDualAmmo(held, parent_node);
			if (ammoReading[0] + ammoReading[1] >= reloadAmt) {
				player.removeMetadata("markOfTheReload", this);
				return;
			}
			final boolean oneIsFull = ammoReading[0] == reloadAmt / 2 || ammoReading[1] == reloadAmt / 2;
			final boolean oneAmmoOnly = takeAmmo && ammoEnable && this.csminion.countItemStacks(player, ammoInfo, parent_node) == 1;
			if (!reloadIndie && (oneIsFull || oneAmmoOnly)) {
				relDuration = this.getInt(parent_node + ".Reload.Dual_Wield.Single_Reload_Duration");
				reloadSound = ".Reload.Dual_Wield.Sounds_Single_Reload";
				akimboSingleReload = true;
			}
		}
		this.terminateReload(player);
		this.removeInertReloadTag(player, 0, true);
		this.unscopePlayer(player);
		player.setMetadata("markOfTheReload", new FixedMetadataValue(this, true));
		this.terminateAllBursts(player);
		if (!held.getItemMeta().getDisplayName().contains("\u1d3f")) {
			this.csminion.setItemName(held, String.valueOf(String.valueOf(held.getItemMeta().getDisplayName())) + '\u1d3f');
		}
		if (reloadIndie && isStart) {
			relDuration += this.getInt(parent_node + ".Reload.First_Reload_Delay");
		}
		final int shootReloadBuffer = this.getInt(parent_node + ".Reload.Shoot_Reload_Buffer");
		if (shootReloadBuffer > 0) {
			final Map<Integer, Long> map = this.last_shot_list.get(playerName);
			if (map != null) {
				final Long lastShot = map.get(player.getInventory().getHeldItemSlot());
				if (lastShot != null) {
					final int ticksPassed = (int)((System.currentTimeMillis() - lastShot) / 50L);
					final int ticksToWait = shootReloadBuffer - ticksPassed;
					if (ticksToWait > 0) {
						relDuration += ticksToWait;
					}
				}
			}
		}
		final WeaponReloadEvent event = new WeaponReloadEvent(player, parent_node, this.getString(parent_node + reloadSound), relDuration);
		this.plugin.getServer().getPluginManager().callEvent(event);
		final String soundsReload = event.getSounds();
		relDuration = event.getReloadDuration();
		if (event.getReloadSpeed() != 1.0) {
			final double reloadSpeed = event.getReloadSpeed();
			relDuration *= (int)reloadSpeed;
			if (!reloadIndie) {
				this.playSoundEffectsScaled(player, parent_node, null, true, reloadSpeed, soundsReload);
			}
		}
		else if (!reloadIndie) {
			this.playSoundEffects(player, parent_node, null, true, null, soundsReload);
		}
		final int reloadShootDelay = akimboSingleReload ? this.getInt(parent_node + ".Reload.Dual_Wield.Single_Reload_Shoot_Delay") : this.getInt(parent_node + ".Reload.Reload_Shoot_Delay");
		final int task_ID = Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				if (takeAmmo && ammoEnable && !CSDirector.this.csminion.containsItemStack(player, ammoInfo, 1, parent_node)) {
					CSDirector.this.removeInertReloadTag(player, 0, true);
					return;
				}
				CSDirector.this.terminateReload(player);
				if (CSDirector.this.switchedTheItem(player, parent_node)) {
					return;
				}
				final ItemStack item = player.getInventory().getItemInHand();
				if (item.getItemMeta().getDisplayName().contains("\u1d3f")) {
					CSDirector.this.csminion.givePotionEffects(player, parent_node, ".Potion_Effects.Potion_Effect_Shooter", "reload");
					CSDirector.this.removeInertReloadTag(player, 0, true);
					int currentAmmo = CSDirector.this.getAmmoBetweenBrackets(player, parent_node, item);
					if (takeAmmo && ammoEnable) {
						if (reloadIndie) {
							if (!dualWield) {
								++currentAmmo;
								CSDirector.this.csminion.replaceBrackets(item, String.valueOf(currentAmmo), parent_node);
							}
							else {
								final int[] ammoReading = CSDirector.this.grabDualAmmo(item, parent_node);
								int leftGun = ammoReading[0];
								int rightGun = ammoReading[1];
								if (leftGun == reloadAmt / 2 || leftGun > rightGun) {
									++rightGun;
								}
								else if (rightGun == reloadAmt / 2 || rightGun > leftGun || leftGun == rightGun) {
									++leftGun;
								}
								CSDirector.this.csminion.replaceBrackets(item, leftGun + " | " + rightGun, parent_node);
							}
							CSDirector.this.reloadShootDelay(player, parent_node, player.getInventory().getHeldItemSlot(), reloadShootDelay);
							CSDirector.this.playSoundEffects(player, parent_node, null, false, null, soundsReload);
							CSDirector.this.csminion.removeNamedItem(player, ammoInfo, 1, parent_node, false);
							final WeaponReloadCompleteEvent event = new WeaponReloadCompleteEvent(player, parent_node);
							CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
							CSDirector.this.reloadAnimation(player, parent_node, false);
							return;
						}
						if (!takeAsMag) {
							int invAmmo = CSDirector.this.csminion.countItemStacks(player, ammoInfo, parent_node);
							final int fillAmt = reloadAmt - currentAmmo;
							currentAmmo += invAmmo;
							if (currentAmmo > reloadAmt) {
								currentAmmo = reloadAmt;
							}
							if (!dualWield) {
								CSDirector.this.csminion.replaceBrackets(item, String.valueOf(currentAmmo), parent_node);
							}
							else if (currentAmmo < reloadAmt) {
								final int[] ammoReading2 = CSDirector.this.grabDualAmmo(item, parent_node);
								int leftGun2 = ammoReading2[0];
								int rightGun2 = ammoReading2[1];
								while (invAmmo > 0) {
									if (leftGun2 == reloadAmt / 2 || leftGun2 > rightGun2) {
										++rightGun2;
									}
									else if (rightGun2 == reloadAmt / 2 || rightGun2 > leftGun2 || leftGun2 == rightGun2) {
										++leftGun2;
									}
									--invAmmo;
								}
								CSDirector.this.csminion.replaceBrackets(item, leftGun2 + " | " + rightGun2, parent_node);
							}
							else {
								CSDirector.this.csminion.replaceBrackets(item, replacer, parent_node);
							}
							CSDirector.this.csminion.removeNamedItem(player, ammoInfo, fillAmt, parent_node, false);
						}
						else if (!dualWield) {
							CSDirector.this.csminion.replaceBrackets(item, replacer, parent_node);
							CSDirector.this.csminion.removeNamedItem(player, ammoInfo, 1, parent_node, false);
						}
						else {
							int invAmmo = CSDirector.this.csminion.countItemStacks(player, ammoInfo, parent_node);
							final int[] ammoReading3 = CSDirector.this.grabDualAmmo(item, parent_node);
							int amtToRemove = 0;
							for (int i = 0; i < 2; ++i) {
								if (ammoReading3[i] != reloadAmt / 2 && invAmmo > 0) {
									ammoReading3[i] = reloadAmt / 2;
									++amtToRemove;
									--invAmmo;
								}
							}
							CSDirector.this.csminion.replaceBrackets(item, ammoReading3[0] + " | " + ammoReading3[1], parent_node);
							CSDirector.this.csminion.removeNamedItem(player, ammoInfo, amtToRemove, parent_node, false);
						}
						CSDirector.this.reloadShootDelay(player, parent_node, player.getInventory().getHeldItemSlot(), reloadShootDelay);
						if (finalBreakAct) {
							CSDirector.this.checkBoltPosition(player, parent_node);
						}
						else if (!item.getItemMeta().getDisplayName().contains("\u25aa �")) {
							CSDirector.this.correctBoltPosition(player, parent_node, false, closeTime, false, true, pumpAct, false);
						}
						CSDirector.this.removeInertReloadTag(player, 0, true);
						final WeaponReloadCompleteEvent event = new WeaponReloadCompleteEvent(player, parent_node);
						CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
					}
					else {
						if (reloadIndie) {
							if (!dualWield) {
								++currentAmmo;
								CSDirector.this.csminion.replaceBrackets(item, String.valueOf(currentAmmo), parent_node);
							}
							else {
								final int[] ammoReading = CSDirector.this.grabDualAmmo(item, parent_node);
								int leftGun = ammoReading[0];
								int rightGun = ammoReading[1];
								if (leftGun == reloadAmt / 2 || leftGun > rightGun) {
									++rightGun;
								}
								else if (rightGun == reloadAmt / 2 || rightGun > leftGun || leftGun == rightGun) {
									++leftGun;
								}
								CSDirector.this.csminion.replaceBrackets(item, leftGun + " | " + rightGun, parent_node);
							}
							CSDirector.this.reloadShootDelay(player, parent_node, player.getInventory().getHeldItemSlot(), reloadShootDelay);
							CSDirector.this.playSoundEffects(player, parent_node, null, false, null, soundsReload);
							final WeaponReloadCompleteEvent event = new WeaponReloadCompleteEvent(player, parent_node);
							CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
							CSDirector.this.reloadAnimation(player, parent_node, false);
							return;
						}
						player.removeMetadata("markOfTheReload", CSDirector.this.plugin);
						CSDirector.this.reloadShootDelay(player, parent_node, player.getInventory().getHeldItemSlot(), reloadShootDelay);
						CSDirector.this.csminion.replaceBrackets(item, replacer, parent_node);
						if (finalBreakAct) {
							CSDirector.this.checkBoltPosition(player, parent_node);
						}
						else if (!item.getItemMeta().getDisplayName().contains("\u25aa �")) {
							CSDirector.this.correctBoltPosition(player, parent_node, false, closeTime, false, true, pumpAct, false);
						}
						final WeaponReloadCompleteEvent event = new WeaponReloadCompleteEvent(player, parent_node);
						CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
					}
				}
			}
		}, relDuration);
		final String user = player.getName();
		Collection<Integer> values_reload = this.global_reload_IDs.get(user);
		if (values_reload == null) {
			values_reload = new ArrayList<Integer>();
			this.global_reload_IDs.put(user, values_reload);
		}
		values_reload.add(task_ID);
	}

	public void reloadShootDelay(final Player player, final String parentNode, final int gunSlot, final int delay, final String... customTag) {
		if (delay < 1) {
			return;
		}
		final String playerName = player.getName();
		Map<String, Integer> tagsAndDelays = this.delay_list.get(playerName);
		if (tagsAndDelays == null) {
			tagsAndDelays = new HashMap<String, Integer>();
			this.delay_list.put(playerName, tagsAndDelays);
		}
		final String metadataTag = ((customTag.length > 1) ? customTag[1] : parentNode) + ((customTag.length > 0) ? customTag[0] : "reloadShootDelay") + gunSlot;
		final Integer prevTaskID = tagsAndDelays.get(metadataTag);
		if (prevTaskID != null) {
			Bukkit.getScheduler().cancelTask(prevTaskID);
		}
		player.setMetadata(metadataTag, new FixedMetadataValue(this, true));
		final int newTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			@Override
			public void run() {
				player.removeMetadata(metadataTag, CSDirector.this.plugin);
				final Map<String, Integer> tagsAndDelays = CSDirector.this.delay_list.get(playerName);
				if (tagsAndDelays != null) {
					tagsAndDelays.remove(metadataTag);
				}
			}
		}, delay);
		tagsAndDelays.put(metadataTag, newTaskID);
	}

	public void projectileExplosion(final Entity objProj, final String parent_node, final boolean grenade, final Player player, final boolean landmine, final boolean rde, final Location loc, final Block c4, final boolean trap, final int cTimes) {
		if (!this.getBoolean(parent_node + ".Explosions.Enable") || (!rde && !this.csminion.regionCheck(objProj, parent_node))) {
			return;
		}
		final int delay = grenade ? 0 : this.getInt(parent_node + ".Explosions.Explosion_Delay");
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				Location location = null;
				World world = null;
				if (!rde) {
					world = objProj.getWorld();
					location = objProj.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
					if (objProj instanceof WitherSkull || objProj instanceof LargeFireball) {
						final BlockIterator checker = new BlockIterator(world, objProj.getLocation().toVector(), objProj.getVelocity().normalize().multiply(-1), 0.0, 4);
						Block block = null;
						while (checker.hasNext()) {
							block = checker.next();
							if (block.getType() == Material.AIR) {
								location = block.getLocation().add(0.5, 0.5, 0.5);
								break;
							}
						}
					}
					if (landmine) {
						objProj.remove();
					}
				}
				else if (!trap) {
					c4.removeMetadata("CS_transformers", CSDirector.this.plugin);
					c4.setType(Material.AIR);
					location = loc;
					world = loc.getWorld();
				}
				else {
					c4.removeMetadata("CS_btrap", CSDirector.this.plugin);
					location = c4.getRelative(BlockFace.UP).getLocation().add(0.5, 0.5, 0.5);
					world = c4.getLocation().getWorld();
				}
				final boolean airstrike = CSDirector.this.getBoolean(parent_node + ".Airstrikes.Enable");
				final boolean cEnable = CSDirector.this.getBoolean(parent_node + ".Cluster_Bombs.Enable");
				final int cOfficialTimes = CSDirector.this.getInt(parent_node + ".Cluster_Bombs.Number_Of_Splits");
				if (!cEnable || airstrike || cTimes >= cOfficialTimes) {
					final boolean shrapEnable = CSDirector.this.getBoolean(parent_node + ".Shrapnel.Enable");
					if (shrapEnable) {
						final String shrapType = CSDirector.this.getString(parent_node + ".Shrapnel.Block_Type");
						final int shrapAmount = CSDirector.this.getInt(parent_node + ".Shrapnel.Amount");
						final int shrapSpeed = CSDirector.this.getInt(parent_node + ".Shrapnel.Speed");
						final boolean placeBlocks = CSDirector.this.getBoolean(parent_node + ".Shrapnel.Place_Blocks");
						String[] blockInfo = shrapType.split("~");
						if (blockInfo.length < 2) {
							blockInfo = new String[] { blockInfo[0], "0" };
						}
						final Material blockMat = MaterialManager.getMaterial(shrapType);
						if (blockMat == null) {
							player.sendMessage(CSDirector.this.heading + "'" + shrapType + "' of weapon '" + parent_node + "' is not a valid block-type.");
							return;
						}
						Byte secData;
						try {
							secData = Byte.valueOf(blockInfo[1]);
						}
						catch (NumberFormatException ex) {
							player.sendMessage(CSDirector.this.heading + "'" + shrapType + "' of weapon '" + parent_node + "' has an invalid secondary data value.");
							return;
						}
						final Random r = new Random();
						for (int i = 0; i < shrapAmount; ++i) {
							location.setPitch((float)(-(r.nextInt(90) + r.nextInt(90))));
							location.setYaw((float)r.nextInt(360));
							final FallingBlock shrapnel = location.getWorld().spawnFallingBlock(location, blockMat, secData);
							if (!placeBlocks) {
								shrapnel.setMetadata("CS_shrapnel", new FixedMetadataValue(CSDirector.this.plugin, true));
							}
							shrapnel.setDropItem(false);
							final double shrapSpeedF = shrapSpeed * ((100 - (r.nextInt(25) - r.nextInt(25))) * 0.001);
							shrapnel.setVelocity(location.getDirection().multiply(shrapSpeedF));
						}
					}
					final WeaponExplodeEvent explodeEvent = new WeaponExplodeEvent(player, location, parent_node, false, false);
					CSDirector.this.plugin.getServer().getPluginManager().callEvent(explodeEvent);
					CSDirector.this.csminion.displayFireworks(objProj, parent_node, ".Fireworks.Firework_Explode");
					final boolean ownerNoDam = CSDirector.this.getBoolean(parent_node + ".Explosions.Enable_Owner_Immunity");
					final boolean noDam = CSDirector.this.getBoolean(parent_node + ".Explosions.Explosion_No_Damage");
					final boolean frenFire = CSDirector.this.getBoolean(parent_node + ".Explosions.Enable_Friendly_Fire");
					final boolean noGrief = CSDirector.this.getBoolean(parent_node + ".Explosions.Explosion_No_Grief");
					final boolean isFire = CSDirector.this.getBoolean(parent_node + ".Explosions.Explosion_Incendiary");
					int boomRadius = CSDirector.this.getInt(parent_node + ".Explosions.Explosion_Radius");
					if (boomRadius > 20) {
						boomRadius = 20;
					}
					final TNTPrimed tnt = (TNTPrimed)location.getWorld().spawn(location, (Class)TNTPrimed.class);
					tnt.setYield((float)boomRadius);
					tnt.setIsIncendiary(isFire);
					tnt.setFuseTicks(0);
					tnt.setMetadata("CS_Label", new FixedMetadataValue(CSDirector.this.plugin, true));
					tnt.setMetadata("CS_potex", new FixedMetadataValue(CSDirector.this.plugin, parent_node));
					if (!rde) {
						tnt.setMetadata("C4_Friendly", new FixedMetadataValue(CSDirector.this.plugin, true));
					}
					if (noGrief) {
						tnt.setMetadata("nullify", new FixedMetadataValue(CSDirector.this.plugin, true));
					}
					if (noDam) {
						tnt.setMetadata("CS_nodam", new FixedMetadataValue(CSDirector.this.plugin, true));
					}
					if (player != null) {
						tnt.setMetadata("CS_pName", new FixedMetadataValue(CSDirector.this.plugin, player.getName()));
						if (!frenFire) {
							tnt.setMetadata("CS_ffcheck", new FixedMetadataValue(CSDirector.this.plugin, true));
						}
						if (ownerNoDam) {
							tnt.setMetadata("0wner_nodam", new FixedMetadataValue(CSDirector.this.plugin, true));
						}
					}
					return;
				}
				final int cAmount = CSDirector.this.getInt(parent_node + ".Cluster_Bombs.Number_Of_Bomblets");
				final int cSpeed = CSDirector.this.getInt(parent_node + ".Cluster_Bombs.Speed_Of_Bomblets");
				final int timer = CSDirector.this.getInt(parent_node + ".Cluster_Bombs.Delay_Before_Detonation");
				final Random r2 = new Random();
				final int totalAmount = (int)Math.pow(cAmount, cOfficialTimes);
				if (totalAmount > 1000) {
					if (player != null) {
						player.sendMessage(String.valueOf(String.valueOf(CSDirector.this.heading)) + cAmount + " to the power of " + cOfficialTimes + " equates to " + totalAmount + " bomblets and consequent explosions! For your safety, CrackShot does not accept total bomblet amounts of over 1000. Please lower the value for 'Number_Of_Splits' and/or 'Number_Of_Bomblets' for the weapon '" + parent_node + "'.");
					}
					return;
				}
				for (int j = 0; j < cAmount; ++j) {
					location.setPitch((float)(-(r2.nextInt(90) + r2.nextInt(90))));
					location.setYaw((float)r2.nextInt(360));
					final double cSpeedF = cSpeed * ((100 - (r2.nextInt(25) - r2.nextInt(25))) * 0.001);
					CSDirector.this.launchGrenade(player, parent_node, timer, location.getDirection().multiply(cSpeedF), location, cTimes + 1);
				}
				CSDirector.this.csminion.giveParticleEffects(null, parent_node, ".Cluster_Bombs.Particle_Release", false, location);
				CSDirector.this.playSoundEffects(null, parent_node, ".Cluster_Bombs.Sounds_Release", false, location);
				final WeaponExplodeEvent explodeEvent2 = new WeaponExplodeEvent(player, location, parent_node, true, false);
				CSDirector.this.plugin.getServer().getPluginManager().callEvent(explodeEvent2);
			}
		}, Math.abs(delay));
	}

	public void prepareTermination(final Entity proj, final boolean remove, final Long delay) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				if (remove) {
					proj.remove();
				}
				else {
					proj.setVelocity(proj.getVelocity().multiply(0.25));
				}
			}
		}, delay);
	}

	@EventHandler
	public void onPlayerItemHeld(final PlayerItemHeldEvent event) {
		final Player player = event.getPlayer();
		this.removeInertReloadTag(player, event.getPreviousSlot(), false);
		this.removeInertReloadTag(player, event.getNewSlot(), false);
		this.unscopePlayer(player);
		this.terminateAllBursts(player);
		this.terminateReload(player);
		final ItemStack heldItem = player.getInventory().getItem(event.getNewSlot());
		if (heldItem != null) {
			final String[] pc = this.itemParentNode(heldItem, player);
			if (pc == null || !Boolean.valueOf(pc[1])) {
				return;
			}
			final ItemStack weapon = this.csminion.vendingMachine(pc[0]);
			weapon.setAmount(player.getInventory().getItem(event.getNewSlot()).getAmount());
			player.getInventory().setItem(event.getNewSlot(), weapon);
		}
	}

	@EventHandler
	public void onPlayerDisconnect(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		this.removeInertReloadTag(player, 0, true);
		this.unscopePlayer(player);
		this.terminateAllBursts(player);
		this.terminateReload(player);
		final String playerName = player.getName();
		if (this.itembombs.containsKey(playerName)) {
			final Map<String, ArrayDeque<Item>> subList = this.itembombs.get(playerName);
			for (final ArrayDeque<Item> subSubList : subList.values()) {
				while (!subSubList.isEmpty()) {
					subSubList.removeFirst().remove();
				}
			}
			this.itembombs.remove(playerName);
		}
		this.delay_list.remove(playerName);
		this.delayed_reload_IDs.remove(playerName);
		this.c4_backup.remove(playerName);
		this.last_shot_list.remove(playerName);
		this.rpm_ticks.remove(playerName);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGunThrow(final PlayerDropItemEvent event) {
		final ItemStack trash = event.getItemDrop().getItemStack();
		final String[] pc = this.itemParentNode(trash, event.getPlayer());
		if (pc == null) {
			return;
		}
		if (!this.getBoolean(pc[0] + ".Reload.Enable")) {
			return;
		}
		if (this.getBoolean(pc[0] + ".Reload.Reload_With_Mouse")) {
			return;
		}
		final Player player = event.getPlayer();
		player.getInventory().getHeldItemSlot();
		if (!player.hasMetadata("dr0p_authorised")) {
			event.setCancelled(true);
			this.delayedReload(player, pc[0]);
		}
	}

	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		this.removeInertReloadTag(player, 0, true);
		this.unscopePlayer(player);
		this.terminateAllBursts(player);
		this.terminateReload(player);
		final List<ItemStack> newInv = new ArrayList<ItemStack>();
		final Iterator<ItemStack> it = event.getDrops().iterator();
		while (it.hasNext()) {
			final ItemStack item = it.next();
			if (item != null && this.itemIsSafe(item)) {
				final String[] parent_node = this.itemParentNode(item, player);
				if (parent_node == null) {
					continue;
				}
				if (!this.getBoolean(parent_node[0] + ".Abilities.Death_No_Drop")) {
					continue;
				}
				newInv.add(item);
				it.remove();
			}
		}
		if (!newInv.isEmpty()) {
			final ItemStack[] newStack = newInv.toArray(new ItemStack[newInv.size()]);
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					player.getInventory().setContents(newStack);
				}
			});
		}
		if (event.getDeathMessage() != null) {
			String message = event.getDeathMessage().replaceAll("(?<=�).*?(?=�)", "");
			message = message.replaceAll(" �", "");
			message = message.replaceAll(String.valueOf('\u1d3f'), "");
			message = message.replaceAll("[�\u25aa\u25ab\u06d4]", "");
			event.setDeathMessage(message);
		}
		if (event.getEntity().getKiller() instanceof Player) {
			final Player shooter = event.getEntity().getKiller();
			final String parent_node2 = this.returnParentNode(shooter);
			if (parent_node2 == null) {
				return;
			}
			String msg = this.getString(parent_node2 + ".Custom_Death_Message.Normal");
			if (msg == null) {
				return;
			}
			msg = msg.replaceAll("<shooter>", shooter.getName());
			msg = msg.replaceAll("<victim>", player.getName());
			event.setDeathMessage(msg);
		}
	}

	@EventHandler
	public void clickGun(final InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			final ItemStack currentItem = event.getCurrentItem();
			final Player player = (Player)event.getWhoClicked();
			if (event.getSlotType() == InventoryType.SlotType.QUICKBAR) {
				this.removeInertReloadTag(player, event.getSlot(), false);
				this.unscopePlayer(player);
				this.terminateAllBursts(player);
				this.terminateReload(player);
			}
			if (event.getSlot() != -1 && currentItem != null) {
				final String[] pc = this.itemParentNode(currentItem, player);
				if (pc == null) {
					return;
				}
				if (event.getInventory().getType() == InventoryType.ANVIL && event.getSlot() == 2 && event.getSlotType() == InventoryType.SlotType.RESULT) {
					player.playSound(player.getLocation(), SoundManager.get("WOOD_CLICK"), 0.5f, 2.0f);
					event.setCancelled(true);
					return;
				}
				if (!Boolean.valueOf(pc[1])) {
					return;
				}
				final ItemStack weapon = this.csminion.vendingMachine(pc[0]);
				weapon.setAmount(currentItem.getAmount());
				event.setCurrentItem(weapon);
			}
			if (event.getSlot() == -999) {
				final ItemStack trash = event.getCursor();
				final String[] pc2 = this.itemParentNode(trash, player);
				if (pc2 == null) {
					return;
				}
				player.setMetadata("dr0p_authorised", new FixedMetadataValue(this, true));
				this.csminion.tempVars(player, "dr0p_authorised", 1L);
			}
		}
	}

	public void unscopePlayer(final Player player, final boolean... manual) {
		if (player.hasMetadata("ironsights")) {
			final String pName = player.getName();
			final String parentNode = player.getMetadata("ironsights").get(0).asString();
			if (manual.length == 0) {
				final WeaponScopeEvent scopeEvent = new WeaponScopeEvent(player, parentNode, false);
				this.getServer().getPluginManager().callEvent(scopeEvent);
				if (scopeEvent.isCancelled()) {
					return;
				}
			}
			player.removeMetadata("ironsights", this);
			player.removePotionEffect(PotionEffectType.SPEED);
			if (player.hasMetadata("night_scoping")) {
				player.removeMetadata("night_scoping", this);
				player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			}
			if (this.zoomStorage.containsKey(pName)) {
				final int[] durAmp = this.zoomStorage.get(pName);
				player.addPotionEffect(PotionEffectType.SPEED.createEffect(durAmp[0], durAmp[1]));
			}
			this.zoomStorage.remove(pName);
		}
	}

	public void removeInertReloadTag(final Player player, final int item_slot, final boolean no_slot) {
		ItemStack item = player.getInventory().getItem(item_slot);
		if (no_slot) {
			item = player.getInventory().getItemInHand();
		}
		if (item != null && this.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains(String.valueOf('\u1d3f'))) {
			final String cleaner = item.getItemMeta().getDisplayName().replaceAll(String.valueOf('\u1d3f'), "");
			if (no_slot) {
				this.csminion.setItemName(player.getInventory().getItemInHand(), cleaner);
			}
			else {
				this.csminion.setItemName(player.getInventory().getItem(item_slot), cleaner);
			}
		}
	}

	public boolean switchedTheItem(final Player player, final String parent_node) {
		final ItemStack item = player.getInventory().getItemInHand();
		final String attachType = this.getAttachment(parent_node, item)[0];
		final boolean attachment = attachType != null && attachType.equalsIgnoreCase("accessory");
		return item == null || !this.itemIsSafe(item) || (!attachment && this.isDifferentItem(item, parent_node));
	}

	public void terminateAllBursts(final Player player) {
		final Collection<Integer> values = this.burst_task_IDs.get(player.getName());
		if (values != null) {
			for (final int taskID : values) {
				Bukkit.getScheduler().cancelTask(taskID);
			}
		}
		this.burst_task_IDs.remove(player.getName());
		this.rpm_shots.remove(player.getName());
	}

	public void terminateReload(final Player player) {
		final String playerName = player.getName();
		final Collection<Integer> values = this.global_reload_IDs.get(playerName);
		if (values != null) {
			for (final Integer value : values) {
				Bukkit.getScheduler().cancelTask(value);
			}
		}
		this.global_reload_IDs.remove(playerName);
		player.removeMetadata("markOfTheReload", this);
		if (this.delayed_reload_IDs.containsKey(playerName)) {
			Bukkit.getScheduler().cancelTask(this.delayed_reload_IDs.get(playerName));
			this.delayed_reload_IDs.remove(playerName);
		}
	}

	public int getAmmoBetweenBrackets(final Player player, final String parent_node, final ItemStack item) {
		final boolean reloadEnable = this.getBoolean(parent_node + ".Reload.Enable");
		final boolean dualWield = this.isDualWield(player, parent_node, item);
		int reloadAmt = this.getReloadAmount(player, parent_node, item);
		final String replacer = dualWield ? (reloadAmt + " | " + reloadAmt) : String.valueOf(reloadAmt);
		if (dualWield) {
			reloadAmt *= 2;
		}
		final String attachType = this.getAttachment(parent_node, item)[0];
		final String bracketInfo = this.csminion.extractReading(item.getItemMeta().getDisplayName());
		int detectedAmmo = reloadAmt;
		try {
			if (attachType != null) {
				final int[] ammoReading = this.grabDualAmmo(item, parent_node);
				if (attachType.equalsIgnoreCase("main")) {
					detectedAmmo = ammoReading[0];
				}
				else if (attachType.equalsIgnoreCase("accessory")) {
					detectedAmmo = ammoReading[1];
				}
			}
			else if (dualWield) {
				String strInBracks = bracketInfo;
				strInBracks = strInBracks.replaceAll(" ", "");
				final String[] dualAmmo = strInBracks.split("\\|");
				if (dualAmmo[0].equals(String.valueOf('\u00d7')) || dualAmmo[1].equals(String.valueOf('\u00d7'))) {
					return 125622;
				}
				detectedAmmo = Integer.valueOf(dualAmmo[0]) + Integer.valueOf(dualAmmo[1]);
			}
			else {
				if (bracketInfo.equals(String.valueOf('\u00d7')) && !reloadEnable) {
					return 125622;
				}
				detectedAmmo = Integer.valueOf(bracketInfo);
			}
		}
		catch (Exception ex) {
			this.csminion.replaceBrackets(item, replacer, parent_node);
		}
		if (detectedAmmo > reloadAmt) {
			this.csminion.replaceBrackets(item, replacer, parent_node);
		}
		return detectedAmmo;
	}

	public void executeCommands(final LivingEntity player, final String parentNode, final String childNode, final String shooterName, final String vicName, final String flightTime, final String totalDmg, final boolean console) {
		final String[] commandList = this.getString(parentNode + childNode).split("\\|");
		String[] array;
		for (int length = (array = commandList).length, i = 0; i < length; ++i) {
			final String cmd = array[i];
			if (console) {
				this.getServer().dispatchCommand(this.getServer().getConsoleSender(), this.variableParser(cmd, shooterName, vicName, flightTime, totalDmg));
			}
			else {
				((Player)player).performCommand(this.variableParser(cmd, shooterName, vicName, flightTime, totalDmg));
			}
		}
	}

	public String variableParser(String filter, final String shooter, final String victim, final String flightTime, final String totalDmg) {
		filter = filter.replaceAll("<shooter>", shooter).replaceAll("<victim>", victim).replaceAll("<damage>", totalDmg).replaceAll("<flight>", flightTime);
		return filter;
	}

	public void sendPlayerMessage(final LivingEntity player, final String parentNode, final String childNode, final String shooterName, final String vicName, final String flightTime, final String totalDmg) {
		final String message = this.getString(parentNode + childNode);
		if (message == null) {
			return;
		}
		if (player instanceof Player) {
			player.sendMessage(this.variableParser(message, shooterName, vicName, flightTime, totalDmg));
		}
	}

	public boolean spawnEntities(final LivingEntity player, final String parentNode, final String childNode, final LivingEntity tamer) {
		if (!this.getBoolean(parentNode + ".Spawn_Entity_On_Hit.Enable")) {
			return false;
		}
		final String entName = this.getString(parentNode + ".Spawn_Entity_On_Hit.Mob_Name");
		final String proType = this.getString(parentNode + ".Shooting.Projectile_Type");
		final boolean targetVictim = this.getBoolean(parentNode + ".Spawn_Entity_On_Hit.Make_Entities_Target_Victim");
		final boolean noDrop = this.getBoolean(parentNode + ".Spawn_Entity_On_Hit.Entity_Disable_Drops");
		final int timedDeath = this.getInt(parentNode + ".Spawn_Entity_On_Hit.Timed_Death");
		final int spawnChance = this.getInt(parentNode + ".Spawn_Entity_On_Hit.Chance");
		if (this.getString(parentNode + childNode) == null) {
			return false;
		}
		if (proType.equalsIgnoreCase("energy")) {
			this.printM("For the weapon '" + parentNode + "', the 'energy' projectile-type does not support the Spawn_Entity_On_Hit module.");
			return false;
		}
		final Random generator = new Random();
		final int diceRoll = generator.nextInt(100);
		if (diceRoll > spawnChance) {
			return false;
		}
		final String[] entList = this.getString(parentNode + childNode).split(",");
		String[] array;
		for (int length = (array = entList).length, j = 0; j < length; ++j) {
			final String entity = array[j];
			final String spaceFilter = entity.replace(" ", "");
			final String[] args = spaceFilter.split("-");
			if (args.length == 4) {
				int entAmount = 0;
				try {
					entAmount = Integer.parseInt(args[3]);
				}
				catch (NumberFormatException ex) {
					this.printM("'" + entAmount + "' in the node 'EntityType_Baby_Explode_Amount' of weapon '" + parentNode + "' is not a valid number!");
					break;
				}
				for (int i = 0; i < entAmount; ++i) {
					String mobEnum = args[0].toUpperCase();
					if (args[0].equals("ZOMBIE_VILLAGER")) {
						mobEnum = "ZOMBIE";
					}
					else if (args[0].equals("WITHER_SKELETON")) {
						mobEnum = "SKELETON";
					}
					else if (args[0].equals("TAMED_WOLF")) {
						mobEnum = "WOLF";
					}
					EntityType entType;
					try {
						entType = EntityType.valueOf(mobEnum);
					}
					catch (IllegalArgumentException ex2) {
						this.printM("'" + args[0] + "' of weapon '" + parentNode + "' is not a valid entity!");
						break;
					}
					final LivingEntity spawnedMob = (LivingEntity)player.getWorld().spawnEntity(player.getLocation(), entType);
					if (Boolean.parseBoolean(args[1])) {
						if (spawnedMob instanceof Zombie) {
							((Zombie)spawnedMob).setBaby(true);
						}
						else if (spawnedMob instanceof Creeper) {
							((Creeper)spawnedMob).setPowered(true);
						}
						else if (spawnedMob instanceof Ageable) {
							((Ageable)spawnedMob).setBaby();
						}
					}
					if (args[0].equalsIgnoreCase("ZOMBIE_VILLAGER")) {
						((Zombie)spawnedMob).setVillager(true);
					}
					else if (args[0].equalsIgnoreCase("WITHER_SKELETON")) {
						((Skeleton)spawnedMob).setSkeletonType(Skeleton.SkeletonType.WITHER);
					}
					else if (args[0].equalsIgnoreCase("TAMED_WOLF") && tamer instanceof AnimalTamer) {
						((Wolf)spawnedMob).setOwner((AnimalTamer)tamer);
					}
					if (entName != null) {
						spawnedMob.setCustomName(entName);
						spawnedMob.setCustomNameVisible(true);
					}
					if (Boolean.parseBoolean(args[2])) {
						spawnedMob.setMetadata("CS_Boomer", new FixedMetadataValue(this, true));
					}
					if (noDrop) {
						spawnedMob.setMetadata("CS_NoDrops", new FixedMetadataValue(this, true));
					}
					if (targetVictim) {
						spawnedMob.damage(0.0, player);
					}
					if (timedDeath != 0) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
							@Override
							public void run() {
								spawnedMob.damage(400.0);
							}
						}, timedDeath);
					}
				}
			}
			else {
				this.printM("'" + spaceFilter + "' of weapon '" + parentNode + "' has an invalid format!");
			}
		}
		return true;
	}

	@EventHandler
	public void onSpawnedEntityDeath(final EntityDeathEvent event) {
		if (event.getEntity().hasMetadata("CS_Boomer")) {
			final TNTPrimed tnt = (TNTPrimed)event.getEntity().getWorld().spawn(event.getEntity().getLocation(), (Class)TNTPrimed.class);
			tnt.setYield(2.0f);
			tnt.setFuseTicks(0);
			tnt.setMetadata("nullify", new FixedMetadataValue(this, true));
		}
		if (event.getEntity().hasMetadata("CS_NoDrops")) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}

	@EventHandler
	public void createGunShop(final SignChangeEvent event) {
		final String lineOne = event.getLine(0);
		if (!lineOne.contains("[CS]")) {
			return;
		}
		final String filter = lineOne.replaceAll(Pattern.quote("[CS]"), "");
		try {
			Integer.valueOf(filter);
		}
		catch (NumberFormatException ex) {
			return;
		}
		for (final String parent_node : this.parentlist.values()) {
			if (this.getBoolean(parent_node + ".SignShops.Enable")) {
				if (!event.getPlayer().hasPermission("crackshot.shops." + parent_node) && !event.getPlayer().hasPermission("crackshot.shops.all")) {
					CSMessages.sendMessage(event.getPlayer(), this.heading, CSMessages.Message.NP_STORE_CREATE.getMessage());
					return;
				}
				final int gunID = this.getInt(parent_node + ".SignShops.Sign_Gun_ID");
				if (gunID != 0 && gunID == Integer.valueOf(filter)) {
					event.setLine(0, "�fStore No\u1390 " + gunID);
					CSMessages.sendMessage(event.getPlayer(), this.heading, CSMessages.Message.STORE_CREATED.getMessage());
					break;
				}
				continue;
			}
		}
	}

	public boolean shopEvent(final PlayerInteractEvent event) {
		boolean retVal = false;
		final Sign signState = (Sign)event.getClickedBlock().getState();
		if (signState.getLine(0).contains("�fStore No\u1390")) {
			final Player player = event.getPlayer();
			final String signLineOne = signState.getLine(0).replaceAll("�fStore No\u1390 ", "");
			for (final String parentNode : this.parentlist.values()) {
				if (this.getBoolean(parentNode + ".SignShops.Enable") && this.getString(parentNode + ".SignShops.Price") != null) {
					final int gunID = this.getInt(parentNode + ".SignShops.Sign_Gun_ID");
					final String priceInfo = this.getString(parentNode + ".SignShops.Price");
					final String[] signInfo = priceInfo.split("-");
					int shopID;
					try {
						shopID = Integer.valueOf(signLineOne);
					}
					catch (NumberFormatException ex) {
						break;
					}
					String currency;
					int amount;
					try {
						currency = signInfo[0];
						amount = Integer.valueOf(signInfo[1]);
					}
					catch (NumberFormatException ex2) {
						player.sendMessage(this.heading + "'Price: " + priceInfo + "' of weapon '" + parentNode + "' does not contain a valid item ID and/or amount!");
						break;
					}
					if (gunID != shopID) {
						continue;
					}
					final boolean creativeMode = player.getGameMode() != GameMode.CREATIVE;
					if (creativeMode || (!player.hasPermission("crackshot.store." + parentNode) && !player.hasPermission("crackshot.store.all"))) {
						event.setCancelled(true);
					}
					if (!player.hasPermission("crackshot.buy." + parentNode) && !player.hasPermission("crackshot.buy.all")) {
						CSMessages.sendMessage(player, this.heading, CSMessages.Message.NP_STORE_PURCHASE.getMessage());
						break;
					}
					if (!creativeMode) {
						break;
					}
					if (this.csminion.countItemStacks(player, signInfo[0], parentNode) < amount) {
						CSMessages.sendMessage(player, this.heading, CSMessages.Message.STORE_CANNOT_AFFORD.getMessage());
						CSMessages.sendMessage(player, this.heading, CSMessages.Message.STORE_ITEMS_NEEDED.getMessage(amount, MaterialManager.getMaterial(currency).toString()));
						break;
					}
					if (player.getInventory().firstEmpty() != -1) {
						this.csminion.removeNamedItem(player, signInfo[0], amount, parentNode, true);
						this.csminion.getWeaponCommand(player, parentNode, false, null, false, false);
						final String milk = this.getString(parentNode + ".Item_Information.Item_Name");
						CSMessages.sendMessage(player, this.heading, CSMessages.Message.STORE_PURCHASED.getMessage(milk));
						retVal = true;
						break;
					}
					break;
				}
			}
		}
		return retVal;
	}

	public boolean checkBoltPosition(final Player player, final String parent_node) {
		final ItemStack item = player.getInventory().getItemInHand();
		final String actType = this.getString(parent_node + ".Firearm_Action.Type");
		if (actType == null || this.isDualWield(player, parent_node, item)) {
			return false;
		}
		final String[] validTypes = { "bolt", "lever", "pump", "break", "revolver", "slide" };
		String[] array;
		for (int length = (array = validTypes).length, i = 0; i < length; ++i) {
			final String str = array[i];
			if (actType.equalsIgnoreCase(str)) {
				break;
			}
			if (str.equals("slide")) {
				this.printM("'" + actType + "' of weapon '" + parent_node + "' is not a valid firearm action! The accepted values are slide, bolt, lever, pump, break or revolver!");
				return false;
			}
		}
		final int openTime = this.getInt(parent_node + ".Firearm_Action.Open_Duration");
		final int closeTime = this.getInt(parent_node + ".Firearm_Action.Close_Duration");
		if (!this.itemIsSafe(item)) {
			return false;
		}
		final String itemName = item.getItemMeta().getDisplayName();
		final int chamberPos = itemName.lastIndexOf("�") + 3;
		final char chamber = itemName.charAt(chamberPos);
		if (chamber == '�') {
			this.csminion.setItemName(item, itemName.replace("�", "\u25aa �"));
		}
		else if (chamber != '\u25aa' && chamber != '\u25ab' && chamber != '\u06d4') {
			this.csminion.setItemName(item, itemName.substring(0, chamberPos) + '\u25aa' + itemName.substring(chamberPos + 1));
		}
		final int detectedAmmo = this.getAmmoBetweenBrackets(player, parent_node, item);
		if (!actType.toLowerCase().contains("break") && !actType.toLowerCase().contains("revolver") && !actType.toLowerCase().contains("slide")) {
			final boolean chamberFired = chamber == '\u25aa';
			final boolean chamberOpened = chamber == '\u06d4';
			if (chamberFired) {
				this.csminion.setItemName(item, itemName.replace("\u25aa", "\u25ab"));
			}
			this.correctBoltPosition(player, parent_node, !chamberOpened, chamberOpened ? closeTime : openTime, detectedAmmo <= 0, false, false, false);
			return !chamberFired;
		}
		if (detectedAmmo <= 0) {
			this.reloadAnimation(player, parent_node);
			final boolean ammoEnable = this.getBoolean(parent_node + ".Ammo.Enable");
			final String ammoInfo = this.getString(parent_node + ".Ammo.Ammo_Item_ID");
			final boolean takeAmmo = this.getBoolean(parent_node + ".Reload.Take_Ammo_On_Reload");
			if (ammoEnable && takeAmmo && !this.csminion.containsItemStack(player, ammoInfo, 1, parent_node)) {
				this.playSoundEffects(player, parent_node, ".Ammo.Sounds_Shoot_With_No_Ammo", false, null);
			}
			return true;
		}
		if (chamber == '\u25ab') {
			this.correctBoltPosition(player, parent_node, false, closeTime, false, false, false, true);
			return true;
		}
		return false;
	}

	public void correctBoltPosition(final Player player, final String parent_node, final boolean boltPull, final int delay, final boolean reloadPrep, final boolean reloadFin, final boolean pumpExit, final boolean breakAct) {
		final String actType = this.getString(parent_node + ".Firearm_Action.Type");
		if (actType == null || this.isDualWield(player, parent_node, player.getItemInHand())) {
			return;
		}
		final String[] validTypes = { "bolt", "lever", "pump", "break", "revolver", "slide" };
		String[] array;
		for (int length = (array = validTypes).length, i = 0; i < length; ++i) {
			final String str = array[i];
			if (actType.equalsIgnoreCase(str)) {
				break;
			}
			if (str.equals("slide")) {
				this.printM("'" + actType + "' of weapon '" + parent_node + "' is not a valid firearm action! The accepted values are slide, bolt, lever, pump, break or revolver!");
				return;
			}
		}
		final int heldSlot = player.getInventory().getHeldItemSlot();
		if (player.hasMetadata("fiddling")) {
			return;
		}
		player.setMetadata("fiddling", new FixedMetadataValue(this, true));
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				player.removeMetadata("fiddling", CSDirector.this.plugin);
				final ItemStack item = player.getInventory().getItemInHand();
				final int currentSlot = player.getInventory().getHeldItemSlot();
				final int closeTime = CSDirector.this.getInt(parent_node + ".Firearm_Action.Close_Duration");
				final int closeShootDelay = CSDirector.this.getInt(parent_node + ".Firearm_Action.Close_Shoot_Delay");
				if (!CSDirector.this.itemIsSafe(item)) {
					return;
				}
				final String itemName = item.getItemMeta().getDisplayName();
				if (CSDirector.this.isDifferentItem(item, parent_node)) {
					return;
				}
				final int chamberPos = itemName.lastIndexOf("�") + 3;
				final char chamber = itemName.charAt(chamberPos);
				if (chamber == '�') {
					CSDirector.this.csminion.setItemName(item, itemName.replace("�", "\u25aa �"));
					return;
				}
				if (chamber != '\u25aa' && chamber != '\u25ab' && chamber != '\u06d4') {
					CSDirector.this.csminion.setItemName(item, itemName.substring(0, chamberPos) + '\u25aa' + itemName.substring(chamberPos + 1));
					return;
				}
				final boolean isAttachment = itemName.contains(String.valueOf('\u25b6'));
				final boolean isReloading = itemName.contains(String.valueOf('\u1d3f'));
				final boolean switchedItems = CSDirector.this.switchedTheItem(player, parent_node) || heldSlot != currentSlot;
				final boolean isCocked = reloadFin && chamber == '\u25aa';
				if (isAttachment || isReloading || switchedItems || isCocked) {
					return;
				}
				if (breakAct) {
					CSDirector.this.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Close", false, null);
					CSDirector.this.csminion.setItemName(item, itemName.replaceAll("\u25ab", "\u25aa"));
					CSDirector.this.reloadShootDelay(player, parent_node, currentSlot, closeShootDelay, "noShooting");
					return;
				}
				if (pumpExit && chamber == '\u25ab') {
					CSDirector.this.correctBoltPosition(player, parent_node, true, 0, false, false, false, false);
					return;
				}
				if (reloadPrep) {
					final boolean isBreak = actType.equalsIgnoreCase("break") || actType.equalsIgnoreCase("revolver");
					String nameToSet = itemName.replaceAll("\u25aa", "\u25ab");
					if (!isBreak) {
						nameToSet = nameToSet.replaceAll("\u25ab", "\u06d4");
					}
					if (!itemName.contains("\u1d3f")) {
						CSDirector.this.csminion.setItemName(item, nameToSet + '\u1d3f');
					}
					else {
						CSDirector.this.csminion.setItemName(item, nameToSet);
					}
					final int reloadOpenDelay = CSDirector.this.getInt(parent_node + ".Firearm_Action.Reload_Open_Delay");
					CSDirector.this.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Open", reloadOpenDelay > 0, null);
					if (reloadOpenDelay > 0) {
						CSDirector.this.delayedReload(player, parent_node, reloadOpenDelay);
						CSDirector.this.reloadShootDelay(player, parent_node, currentSlot, reloadOpenDelay, "noShooting");
					}
					else {
						CSDirector.this.reloadAnimation(player, parent_node);
					}
					return;
				}
				if (boltPull) {
					CSDirector.this.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Open", false, null);
					CSDirector.this.csminion.setItemName(item, itemName.replaceAll("\u25ab", "\u06d4"));
					CSDirector.this.correctBoltPosition(player, parent_node, false, closeTime, false, false, false, false);
				}
				else if (actType.equalsIgnoreCase("slide") && (chamber == '\u25ab' || chamber == '\u06d4')) {
					if (chamber == '\u25ab') {
						CSDirector.this.csminion.setItemName(item, itemName.replaceAll("\u25ab", "\u25aa"));
					}
					else {
						CSDirector.this.csminion.setItemName(item, itemName.replaceAll("\u06d4", "\u25aa"));
					}
					CSDirector.this.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Close", false, null);
					CSDirector.this.reloadShootDelay(player, parent_node, currentSlot, closeShootDelay, "noShooting");
				}
				else {
					final int detectedAmmo = CSDirector.this.getAmmoBetweenBrackets(player, parent_node, item);
					if (detectedAmmo > 0) {
						CSDirector.this.csminion.setItemName(item, itemName.replaceAll("\u06d4", "\u25aa"));
						CSDirector.this.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Close", false, null);
						CSDirector.this.reloadShootDelay(player, parent_node, currentSlot, closeShootDelay, "noShooting");
						if (detectedAmmo != 125622) {
							CSDirector.this.ammoOperation(player, parent_node, detectedAmmo, item);
						}
					}
					else {
						CSDirector.this.reloadAnimation(player, parent_node);
					}
				}
			}
		}, delay);
	}

	public void ammoOperation(final Player player, final String parent_node, int detectedAmmo, final ItemStack item) {
		final boolean ammoEnable = this.getBoolean(parent_node + ".Ammo.Enable");
		final String ammoInfo = this.getString(parent_node + ".Ammo.Ammo_Item_ID");
		final boolean takeAmmo = this.getBoolean(parent_node + ".Ammo.Take_Ammo_Per_Shot");
		--detectedAmmo;
		this.csminion.replaceBrackets(item, String.valueOf(detectedAmmo), parent_node);
		if (ammoEnable && takeAmmo) {
			this.csminion.removeNamedItem(player, ammoInfo, 1, parent_node, false);
		}
		if (detectedAmmo == 0) {
			final String actType = this.getString(parent_node + ".Firearm_Action.Type");
			this.playSoundEffects(player, parent_node, ".Reload.Sounds_Out_Of_Ammo", false, null);
			if (!this.itemIsSafe(item)) {
				return;
			}
			final String itemName = item.getItemMeta().getDisplayName();
			if (actType != null) {
				if (actType.equalsIgnoreCase("bolt") || actType.equalsIgnoreCase("lever") || actType.equalsIgnoreCase("pump")) {
					if (!itemName.contains("\u25aa")) {
						this.delayedReload(player, parent_node);
					}
				}
				else if (actType.equalsIgnoreCase("break") || actType.equalsIgnoreCase("revolver") || actType.equalsIgnoreCase("slide")) {
					if (actType.toLowerCase().contains("slide") && itemName.contains("\u25aa")) {
						final int openTime = this.getInt(parent_node + ".Firearm_Action.Open_Duration");
						if (openTime < 1) {
							this.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Open", false, null);
						}
						this.csminion.setItemName(item, itemName.replaceAll("\u25aa", "\u25ab"));
					}
					this.delayedReload(player, parent_node);
				}
			}
			else {
				this.delayedReload(player, parent_node);
			}
		}
	}

	public boolean ammoSpecOps(final Player player, final String parentNode, final int detectedAmmo, final ItemStack item, final boolean leftClick) {
		final boolean ammoEnable = this.getBoolean(parentNode + ".Ammo.Enable");
		final boolean takeAmmo = this.getBoolean(parentNode + ".Ammo.Take_Ammo_Per_Shot");
		final String ammoInfo = this.getString(parentNode + ".Ammo.Ammo_Item_ID");
		final int[] ammoReading = this.grabDualAmmo(item, parentNode);
		int ammoAmount;
		if (leftClick) {
			if (ammoReading[0] <= 0) {
				this.playSoundEffects(player, parentNode, ".Reload.Dual_Wield.Sounds_Shoot_With_No_Ammo", false, null);
				return false;
			}
			ammoAmount = ammoReading[0] - 1;
			this.csminion.replaceBrackets(item, ammoAmount + " | " + ammoReading[1], parentNode);
		}
		else {
			if (ammoReading[1] <= 0) {
				this.playSoundEffects(player, parentNode, ".Reload.Dual_Wield.Sounds_Shoot_With_No_Ammo", false, null);
				return false;
			}
			ammoAmount = ammoReading[1] - 1;
			this.csminion.replaceBrackets(item, ammoReading[0] + " | " + ammoAmount, parentNode);
		}
		if (ammoAmount <= 0) {
			this.playSoundEffects(player, parentNode, ".Reload.Sounds_Out_Of_Ammo", false, null);
		}
		if (ammoEnable && takeAmmo) {
			this.csminion.removeNamedItem(player, ammoInfo, 1, parentNode, false);
		}
		if (detectedAmmo - 1 == 0) {
			this.reloadAnimation(player, parentNode);
		}
		return true;
	}

	public int[] grabDualAmmo(final ItemStack item, final String parentNode) {
		try {
			String strInBracks = this.csminion.extractReading(item.getItemMeta().getDisplayName());
			String[] dualAmmo = strInBracks.split(" ");
			if (dualAmmo.length != 3) {
				this.csminion.resetItemName(item, parentNode);
				strInBracks = this.csminion.extractReading(item.getItemMeta().getDisplayName());
				dualAmmo = strInBracks.split(" ");
			}
			int leftGun;
			if (dualAmmo[0].equals(String.valueOf('\u00d7'))) {
				leftGun = 1;
			}
			else {
				leftGun = Integer.valueOf(dualAmmo[0]);
			}
			int rightGun;
			if (dualAmmo[2].equals(String.valueOf('\u00d7'))) {
				rightGun = 1;
			}
			else {
				rightGun = Integer.valueOf(dualAmmo[2]);
			}
			return new int[] { leftGun, rightGun };
		}
		catch (NumberFormatException ex) {
			return new int[2];
		}
	}

	@EventHandler
	public void explosiveTipCrossbow(final EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player && event.getForce() == 1.0f) {
			final Player shooter = (Player)event.getEntity();
			final String parentNode = this.returnParentNode(shooter);
			if (parentNode == null) {
				return;
			}
			event.setCancelled(true);
			if (!this.regionAndPermCheck(shooter, parentNode, false)) {
				return;
			}
			this.csminion.weaponInteraction(shooter, parentNode, false);
		}
	}

	public String isSkipNameItem(final ItemStack item) {
		final String itemInfo = item.getType() + "-" + item.getDurability();
		return this.convIDs.get(itemInfo);
	}

	public String convItem(final ItemStack item) {
		String retNode = this.isSkipNameItem(item);
		if (retNode == null && item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
			final Map<Enchantment, Integer> enchList = item.getEnchantments();
			for (final String parentNode : this.enchlist.keySet()) {
				final String[] enchInfo = this.enchlist.get(parentNode);
				final Enchantment givenEnch = Enchantment.getByName(enchInfo[0]);
				final int enchLevel = Integer.valueOf(enchInfo[1]);
				final ItemStack comp = this.csminion.parseItemStack(this.getString(parentNode + ".Item_Information.Item_Type"));
				final boolean equal = comp != null && comp.getType() == item.getType() && (comp.getDurability() == item.getDurability() || this.hasDurab(parentNode));
				if (equal && enchList.containsKey(givenEnch) && enchList.get(givenEnch) == enchLevel) {
					retNode = parentNode;
					break;
				}
			}
		}
		return retNode;
	}

	public String getPureName(String itemName) {
		final int nameLength = itemName.length() - 1;
		final int lastIndex = itemName.lastIndexOf("�");
		if (lastIndex != -1 && lastIndex + 2 <= nameLength) {
			itemName = itemName.substring(0, lastIndex + 2);
		}
		return itemName;
	}

	public String returnParentNode(final Player player) {
		String retNode = null;
		final ItemStack item = player.getItemInHand();
		if (item == null) {
			return null;
		}
		if (this.itemIsSafe(item)) {
			String parentNode = this.isSkipNameItem(item);
			if (parentNode == null) {
				parentNode = this.parentlist.get(this.getPureName(item.getItemMeta().getDisplayName()));
			}
			if (parentNode != null) {
				if (player.getItemInHand().getItemMeta().getDisplayName().contains(String.valueOf('\u25b6'))) {
					retNode = this.getAttachment(parentNode, item)[1];
				}
				else {
					retNode = parentNode;
				}
			}
		}
		else {
			final String convNode = this.convItem(item);
			if (convNode != null && this.regionAndPermCheck(player, convNode, true)) {
				this.csminion.removeEnchantments(item);
				final ItemStack weapon = this.csminion.vendingMachine(convNode);
				weapon.setAmount(player.getItemInHand().getAmount());
				player.setItemInHand(weapon);
			}
		}
		return retNode;
	}

	public String[] itemParentNode(final ItemStack item, final Player player) {
		String[] retVal = null;
		if (this.itemIsSafe(item)) {
			String parentNode = this.isSkipNameItem(item);
			if (parentNode == null) {
				parentNode = this.parentlist.get(this.getPureName(item.getItemMeta().getDisplayName()));
			}
			if (parentNode != null) {
				if (item.getItemMeta().getDisplayName().contains(String.valueOf('\u25b6'))) {
					final String attachInfo = this.getAttachment(parentNode, item)[1];
					retVal = new String[] { attachInfo, "false" };
				}
				else {
					retVal = new String[] { parentNode, "false" };
				}
			}
		}
		else {
			final String convNode = this.convItem(item);
			if (convNode != null && player != null && this.regionAndPermCheck(player, convNode, true)) {
				this.csminion.removeEnchantments(item);
				retVal = new String[] { convNode, "true" };
			}
		}
		return retVal;
	}

	@EventHandler
	public void onCraft(final CraftItemEvent event) {
		for (final String parent_node : this.parentlist.values()) {
			if (this.getBoolean(parent_node + ".Crafting.Enable")) {
				final ItemStack weapon = this.csminion.vendingMachine(parent_node);
				if (!event.getRecipe().getResult().isSimilar(weapon)) {
					continue;
				}
				if (!(event.getWhoClicked() instanceof Player)) {
					break;
				}
				final Player crafter = (Player)event.getWhoClicked();
				if (!crafter.hasPermission("crackshot.craft." + parent_node) && !crafter.hasPermission("crackshot.craft.all")) {
					event.setCancelled(true);
					CSMessages.sendMessage(crafter, this.heading, CSMessages.Message.NP_WEAPON_CRAFT.getMessage());
					break;
				}
				break;
			}
		}
	}

	void printM(final String msg) {
		System.out.print("[CrackShot] " + msg);
	}

	public double getDouble(final String nodes) {
		final Double result = CSDirector.dubs.get(nodes);
		return (result != null) ? result : 0.0;
	}

	public boolean getBoolean(final String nodes) {
		final Boolean result = CSDirector.bools.get(nodes);
		return result != null && result;
	}

	public int getInt(final String nodes) {
		final Integer result = CSDirector.ints.get(nodes);
		return (result != null) ? result : 0;
	}

	public String getString(final String nodes) {
		final String result = CSDirector.strings.get(nodes);
		return result;
	}

	public boolean hasDurab(final String nodes) {
		final Boolean result = this.morobust.get(nodes);
		return result != null && result;
	}

	public boolean regionAndPermCheck(final Player shooter, final String parentNode, final boolean noMsg) {
		String[] disWorlds;
		for (int length = (disWorlds = this.disWorlds).length, i = 0; i < length; ++i) {
			final String worName = disWorlds[i];
			if (worName == null) {
				break;
			}
			final World world = Bukkit.getWorld(worName);
			if (world == shooter.getWorld()) {
				return false;
			}
		}
		if (!shooter.hasPermission("crackshot.use." + parentNode) && !shooter.hasPermission("crackshot.use.all")) {
			if (!noMsg) {
				CSMessages.sendMessage(shooter, this.heading, CSMessages.Message.NP_WEAPON_USE.getMessage());
			}
			return false;
		}
		if (!shooter.hasPermission("crackshot.bypass." + parentNode) && !shooter.hasPermission("crackshot.bypass.all") && !this.csminion.regionCheck(shooter, parentNode)) {
			if (!noMsg && this.getString(parentNode + ".Region_Check.Message_Of_Denial") != null) {
				shooter.sendMessage(this.getString(parentNode + ".Region_Check.Message_Of_Denial"));
			}
			return false;
		}
		return true;
	}

	@EventHandler
	public void onEggSplat(final PlayerEggThrowEvent event) {
		if (event.getEgg().hasMetadata("CS_Hardboiled")) {
			event.setHatching(false);
		}
	}
	public void launchGrenade(final Player player, final String parent_node, int delay, final Vector vel, final Location splitLoc, final int cTimes) {
		final boolean cEnable = this.getBoolean(parent_node + ".Cluster_Bombs.Enable");
		final int cOfficialTimes = this.getInt(parent_node + ".Cluster_Bombs.Number_Of_Splits");
		String itemType = this.getString(parent_node + ".Shooting.Projectile_Subtype");
		String nodeName = "Projectile_Subtype:";
		if (cEnable && cTimes != 0) {
			nodeName = "Bomblet_Type:";
			itemType = this.getString(parent_node + ".Cluster_Bombs.Bomblet_Type");
		}
		if (itemType == null) {
			player.sendMessage(this.heading + "The '" + nodeName + "' node of '" + parent_node + "' has not been defined.");
			return;
		}
		final ItemStack item = this.csminion.parseItemStack(itemType);
		if (item == null) {
			player.sendMessage(this.heading + "The '" + nodeName + "' node of '" + parent_node + "' has an incorrect value.");
			return;
		}
		Location loc = player.getEyeLocation();
		if (splitLoc != null) {
			loc = splitLoc;
		}
		final Item grenade = player.getWorld().dropItem(loc, item);
		grenade.setVelocity(vel);
		grenade.setPickupDelay(delay + 20);
		final ItemStack grenStack = grenade.getItemStack();
		this.csminion.setItemName(grenStack, "\u0aee" + grenade.getUniqueId());
		grenade.setItemStack(grenStack);
		this.callShootEvent(player, grenade, parent_node);
		final boolean airstrike = this.getBoolean(parent_node + ".Airstrikes.Enable");
		final int cDelay = this.getInt(parent_node + ".Cluster_Bombs.Delay_Before_Split");
		final int cDelayDiff = this.getInt(parent_node + ".Cluster_Bombs.Detonation_Delay_Variation");
		if (cEnable && !airstrike && cTimes < cOfficialTimes) {
			if (cTimes == 0) {
				this.playSoundEffects(grenade, parent_node, ".Shooting.Sounds_Projectile", false, null);
			}
			delay = cDelay;
		}
		else if (cEnable) {
			if (cDelay != 0 && cDelayDiff != 0) {
				final Random r = new Random();
				delay += r.nextInt(cDelayDiff) - r.nextInt(cDelayDiff);
			}
		}
		else {
			this.playSoundEffects(grenade, parent_node, ".Shooting.Sounds_Projectile", false, null);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				final boolean zapEnable = CSDirector.this.getBoolean(parent_node + ".Lightning.Enable");
				final boolean zapNoDam = CSDirector.this.getBoolean(parent_node + ".Lightning.No_Damage");
				if (!airstrike) {
					if (zapEnable) {
						CSDirector.this.csminion.projectileLightning(grenade.getLocation(), zapNoDam);
					}
					CSDirector.this.projectileExplosion(grenade, parent_node, true, player, true, false, null, null, false, cTimes);
				}
				else {
					CSDirector.this.csminion.callAirstrike(grenade, parent_node, player);
				}
				grenade.remove();
			}
		}, delay);
	}

	@EventHandler
	public void onAnyDamage(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
			final Player shooter = (Player)event.getEntity();
			final ItemStack heldItem = shooter.getItemInHand();
			if (heldItem != null && this.itemIsSafe(heldItem)) {
				final String parentNode = this.returnParentNode(shooter);
				if (parentNode == null) {
					return;
				}
				if (this.getBoolean(parentNode + ".Abilities.No_Fall_Damage")) {
					event.setCancelled(true);
				}
			}
		}
	}

	public void delayedReload(final Player player, final String parentNode, final long... delay) {
		final int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				CSDirector.this.reloadAnimation(player, parentNode);
				CSDirector.this.delayed_reload_IDs.remove(player.getName());
			}
		}, (delay.length == 0) ? 1L : delay[0]);
		this.delayed_reload_IDs.put(player.getName(), taskID);
	}

	@EventHandler
	public void onPickUp(final PlayerPickupItemEvent event) {
		if (this.csminion.fastenSeatbelts(event.getItem()) != null) {
			this.csminion.reseatTag(event.getItem());
			event.setCancelled(true);
			if (!(event.getItem().getVehicle() instanceof Minecart)) {
				event.getItem().remove();
			}
		}
		else {
			final ItemStack item = event.getItem().getItemStack();
			if (this.itemIsSafe(item)) {
				final String fullName = item.getItemMeta().getDisplayName();
				if (fullName.contains("\u0aee")) {
					event.setCancelled(true);
					event.getItem().remove();
				}
				else {
					final String itemName = this.getPureName(fullName);
					if (this.boobs.containsKey(itemName)) {
						final String parentNode = this.boobs.get(itemName);
						if (!this.csminion.getBoobean(2, parentNode)) {
							return;
						}
						final Player picker = event.getPlayer();
						final String detectedName = this.csminion.extractReading(fullName);
						if (detectedName.equals("?")) {
							return;
						}
						final Player planter = Bukkit.getServer().getPlayer(detectedName);
						if (planter == picker) {
							return;
						}
						event.getItem().setPickupDelay(60);
						this.slapAndReaction(picker, planter, event.getItem().getLocation().getBlock(), parentNode, null, null, detectedName, event.getItem());
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onItemSpawn(final ItemSpawnEvent event) {
		final ItemStack item = event.getEntity().getItemStack();
		if (MaterialManager.isSkullItem(item.getType()) && item.hasItemMeta()) {
			final SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
			if (skullMeta != null && skullMeta.hasOwner() && skullMeta.getOwner().contains("\u060c")) {
				event.setCancelled(true);
				event.getEntity().remove();
			}
		}
	}

	@EventHandler
	public void onEntityInteract(final PlayerInteractEntityEvent event) {
		final Entity ent = event.getRightClicked();
		if (ent instanceof Minecart) {
			this.csminion.reseatTag((Vehicle)event.getRightClicked());
			if (ent.getPassenger() instanceof Item) {
				event.setCancelled(true);
			}
		}
		else if (ent instanceof Villager || ent instanceof Horse) {
			final Player player = event.getPlayer();
			final ItemStack heldItem = player.getItemInHand();
			final String parentNode = this.returnParentNode(player);
			if (parentNode != null && this.getBoolean(parentNode + ".Shooting.Cancel_Right_Click_Interactions")) {
				this.OnPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, heldItem, null, null));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void tagDespawn(final ItemDespawnEvent event) {
		if (this.csminion.fastenSeatbelts(event.getEntity()) != null) {
			event.setCancelled(true);
		}
		final ItemStack item = event.getEntity().getItemStack();
		if (this.itemIsSafe(item)) {
			final String itemName = this.getPureName(item.getItemMeta().getDisplayName());
			if (itemName.contains("\u0aee\u0aee")) {
				event.setCancelled(true);
			}
			else if (this.boobs.containsKey(itemName)) {
				final String parentNode = this.boobs.get(itemName);
				if (this.csminion.getBoobean(5, parentNode)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onMobShotgun(final VehicleEnterEvent event) {
		if (event.getVehicle() instanceof Minecart) {
			this.csminion.reseatTag(event.getVehicle());
			if (event.getVehicle().getPassenger() instanceof Item) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBoatMine(final VehicleEntityCollisionEvent event) {
		if (!(event.getVehicle() instanceof Minecart)) {
			return;
		}
		this.csminion.reseatTag(event.getVehicle());
		if (event.getVehicle().getPassenger() instanceof Item && event.getEntity() instanceof LivingEntity) {
			final Entity victim = event.getEntity();
			final Item psngr = (Item)event.getVehicle().getPassenger();
			final String[] seagullInfo = this.csminion.fastenSeatbelts(psngr);
			if (seagullInfo == null) {
				return;
			}
			event.setCancelled(true);
			final Player fisherman = Bukkit.getServer().getPlayer(seagullInfo[1]);
			final WeaponTriggerEvent trigEvent = new WeaponTriggerEvent(fisherman, (LivingEntity)victim, seagullInfo[2]);
			this.getServer().getPluginManager().callEvent(trigEvent);
			if (!trigEvent.isCancelled()) {
				if (fisherman != null && victim instanceof Player) {
					if (victim.getName().equals(seagullInfo[1])) {
						event.setCancelled(false);
					}
					else {
						this.csminion.callAndResponse((Player)victim, fisherman, event.getVehicle(), seagullInfo, false);
					}
				}
				else {
					this.csminion.mineAction(event.getVehicle(), seagullInfo, fisherman, false, victim.getType().getName(), victim);
				}
			}
		}
	}

	@EventHandler
	public void onBoatMineShoot(final VehicleDamageEvent event) {
		if (!(event.getVehicle() instanceof Minecart)) {
			return;
		}
		this.csminion.reseatTag(event.getVehicle());
		if (event.getVehicle().getPassenger() instanceof Item) {
			final Entity attacker = event.getAttacker();
			final Item psngr = (Item)event.getVehicle().getPassenger();
			final String[] seagullInfo = this.csminion.fastenSeatbelts(psngr);
			if (seagullInfo == null) {
				return;
			}
			event.setCancelled(true);
			final Player fisherman = Bukkit.getServer().getPlayer(seagullInfo[1]);
			if (attacker instanceof Player) {
				final Player player = (Player)attacker;
				if (player.getName().equals(seagullInfo[1])) {
					this.csminion.mineAction(event.getVehicle(), seagullInfo, fisherman, true, null, attacker);
				}
				else {
					this.csminion.callAndResponse(player, fisherman, event.getVehicle(), seagullInfo, true);
				}
			}
			else {
				this.csminion.mineAction(event.getVehicle(), seagullInfo, fisherman, true, null, attacker);
			}
		}
	}

	public void deployMine(final Player player, final String parent_node, final Location loc) {
		final String nodeInfo = this.getString(parent_node + ".Explosive_Devices.Device_Info");
		final String[] deviceInfo = (nodeInfo == null) ? null : nodeInfo.split(",");
		final ItemStack fuseItem = this.csminion.parseItemStack(deviceInfo[0]);
		final Location spawnLoc = (loc == null) ? player.getLocation().add(0.0, 0.75, 0.0) : loc;
		if (fuseItem == null) {
			player.sendMessage(this.heading + "No valid item-ID for 'Device_Info' of the weapon '" + parent_node + "' has been provided.");
			return;
		}
		EntityType cartType = EntityType.MINECART;
		if (deviceInfo.length == 2) {
			try {
				cartType = EntityType.valueOf(deviceInfo[1].toUpperCase());
			}
			catch (IllegalArgumentException ex) {
				player.sendMessage(this.heading + "The 'Device_Info' node of the weapon '" + parent_node + "' contains '" + deviceInfo[1] + "', which is not a valid minecart type.");
			}
		}
		final Entity mine = player.getWorld().spawnEntity(spawnLoc, cartType);
		final ItemMeta metaPsngr = fuseItem.getItemMeta();
		metaPsngr.setDisplayName("�cS3AGULLL~" + player.getName() + "~" + parent_node + "~" + mine.getUniqueId().toString());
		fuseItem.setItemMeta(metaPsngr);
		final Entity fusePassenger = player.getWorld().dropItem(spawnLoc, fuseItem);
		mine.setPassenger(fusePassenger);
		final WeaponPlaceMineEvent event = new WeaponPlaceMineEvent(player, mine, parent_node);
		this.getServer().getPluginManager().callEvent(event);
	}

	@EventHandler
	public void airstrikeKaboom(final EntityChangeBlockEvent event) {
		if (event.getEntity().hasMetadata("CS_strike")) {
			final Entity bomb = event.getEntity();
			final String info = bomb.getMetadata("CS_strike").get(0).asString();
			final String[] parsedInfo = info.split("~");
			final Player player = Bukkit.getServer().getPlayer(parsedInfo[1]);
			this.projectileExplosion(bomb, parsedInfo[0], false, player, true, false, null, null, false, 0);
			bomb.remove();
			event.setCancelled(true);
		}
		else if (event.getEntity().hasMetadata("CS_shrapnel")) {
			event.getEntity().remove();
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onC4Place(final BlockPlaceEvent event) {
		final Player placer = event.getPlayer();
		if (event.getItemInHand() == null) {
			return;
		}
		final String[] parent_node = this.itemParentNode(event.getItemInHand(), placer);
		if (parent_node == null) {
			return;
		}
		if (!this.regionAndPermCheck(placer, parent_node[0], false) || !this.getBoolean(parent_node[0] + ".Explosive_Devices.Enable")) {
			event.setCancelled(true);
			return;
		}
		placer.updateInventory();
		final String type = this.getString(parent_node[0] + ".Explosive_Devices.Device_Type");
		if (type == null || !type.equalsIgnoreCase("remote")) {
			return;
		}
		if (this.itemIsSafe(event.getItemInHand()) && event.getItemInHand().getItemMeta().getDisplayName().contains("�0�")) {
			event.setCancelled(true);
			return;
		}
		final boolean placeAnywhere = this.getBoolean(parent_node[0] + ".Explosive_Devices.Remote_Bypass_Regions");
		final boolean allowed = !event.isCancelled() && event.canBuild();
		final Block block = event.getBlockPlaced();
		event.setCancelled(true);
		if (allowed || placeAnywhere) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					CSDirector.this.setupC4(placer, block, parent_node);
				}
			});
		}
	}

	public void setupC4(final Player placer, final Block block, final String[] parent_node) {
		final Material mat = MaterialManager.getSkullBlock();
		if (mat == null) {
			throw new UnsupportedOperationException();
		}
		block.setType(mat);
		if (MaterialManager.pre113) {
			try {
				final Method method = block.getClass().getMethod("setData", Byte.TYPE);
				method.invoke(block, 1);
			}
			catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		final BlockState state = block.getState();
		if (state instanceof Skull) {
			int capacity = 0;
			String uniqueID = null;
			Skull skull;
			try {
				skull = (Skull)state;
			}
			catch (ClassCastException ex) {
				return;
			}
			if (MaterialManager.pre113) {
				skull.setSkullType(SkullType.PLAYER);
			}
			final String[] refinedOre = this.csminion.returnRefinedOre(placer, parent_node[0]);
			if (refinedOre != null) {
				capacity = Integer.valueOf(refinedOre[0]);
				uniqueID = refinedOre[1];
			}
			String storedOwner = placer.getName();
			if (storedOwner.length() > 13) {
				storedOwner = storedOwner.substring(0, 12) + '\u0638';
			}
			skull.setOwner(uniqueID + "\u060c" + storedOwner);
			if (MaterialManager.pre113) {
				skull.setRotation(this.getBlockDirection(placer.getLocation().getYaw()));
			}
			skull.update(true);
			final String world = placer.getWorld().getName();
			final String x = String.valueOf(block.getLocation().getBlockX());
			final String y = String.valueOf(block.getLocation().getBlockY());
			final String z = String.valueOf(block.getLocation().getBlockZ());
			Map<String, String> placedHeads = this.c4_backup.get(storedOwner);
			if (placedHeads == null) {
				placedHeads = new HashMap<String, String>();
				this.c4_backup.put(storedOwner, placedHeads);
			}
			placedHeads.put(world + "," + x + "," + y + "," + z, uniqueID);
			final ItemStack detonator = placer.getItemInHand();
			final boolean ammoEnable = this.getBoolean(parent_node[0] + ".Ammo.Enable");
			final String ammoInfo = this.getString(parent_node[0] + ".Ammo.Ammo_Item_ID");
			final boolean takeAmmo = this.getBoolean(parent_node[0] + ".Ammo.Take_Ammo_Per_Shot");
			final String bracketInfo = this.csminion.extractReading(detonator.getItemMeta().getDisplayName());
			int detectedAmmo = 0;
			try {
				detectedAmmo = Integer.valueOf(bracketInfo);
			}
			catch (NumberFormatException ex2) {}
			if (detectedAmmo <= 0) {
				block.setType(Material.AIR);
				return;
			}
			if (ammoEnable && takeAmmo) {
				if (!this.csminion.containsItemStack(placer, ammoInfo, 1, parent_node[0])) {
					this.playSoundEffects(placer, parent_node[0], ".Ammo.Sounds_Shoot_With_No_Ammo", false, null);
					block.setType(Material.AIR);
					return;
				}
				this.csminion.removeNamedItem(placer, ammoInfo, 1, parent_node[0], false);
			}
			this.csminion.replaceBrackets(detonator, String.valueOf(detectedAmmo - 1), parent_node[0]);
			if (detonator.getItemMeta().hasLore()) {
				final List<String> lore = detonator.getItemMeta().getLore();
				final String lastLine = lore.get(lore.size() - 1);
				if (lastLine.contains(String.valueOf('\u1390'))) {
					final String numInBrack = lastLine.split("\\[")[1].split("\\]")[0];
					final int lastNumber = Integer.valueOf(numInBrack);
					if (lastNumber >= capacity) {
						block.setType(Material.AIR);
						return;
					}
					lore.add("�e�l[" + (lastNumber + 1) + "]�r�e " + world.toUpperCase() + '\u1390' + " " + x + ", " + y + ", " + z);
				}
				else {
					lore.add("�e�l[1]�r�e " + world.toUpperCase() + '\u1390' + " " + x + ", " + y + ", " + z);
				}
				final ItemMeta detmeta = detonator.getItemMeta();
				detmeta.setLore(lore);
				detonator.setItemMeta(detmeta);
				placer.getInventory().setItemInHand(detonator);
				this.playSoundEffects(placer, parent_node[0], ".Explosive_Devices.Sounds_Deploy", false, null);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void breakC4(final BlockBreakEvent event) {
		if (MaterialManager.isSkullBlock(event.getBlock())) {
			final BlockState state = event.getBlock().getState();
			if (state instanceof Skull) {
				Skull skull;
				try {
					skull = (Skull)state;
				}
				catch (ClassCastException ex) {
					return;
				}
				final String ownerOre = skull.getOwner();
				if (ownerOre != null && ownerOre.contains("\u060c")) {
					final String[] refinedOwner = ownerOre.split("\u060c");
					final Block block = event.getBlock();
					final Player breaker = event.getPlayer();
					Player placer = null;
					final List<Player> candidates = Bukkit.matchPlayer(refinedOwner[1].replace(String.valueOf('\u0638'), ""));
					if (candidates != null && !candidates.isEmpty()) {
						placer = candidates.get(0);
					}
					final String world = block.getWorld().getName();
					final String x = String.valueOf(block.getLocation().getBlockX());
					final String y = String.valueOf(block.getLocation().getBlockY());
					final String z = String.valueOf(block.getLocation().getBlockZ());
					final String[] itemInfo = { "-", world, x, y, z };
					for (final String exploDevID : this.rdelist.keySet()) {
						if (exploDevID.equals(refinedOwner[0])) {
							final String parent_node = this.rdelist.get(exploDevID);
							final boolean bypassRegions = this.getBoolean(parent_node + ".Explosive_Devices.Remote_Bypass_Regions");
							if (!event.isCancelled() || bypassRegions) {
								if (breaker != placer) {
									this.csminion.callAndResponse(breaker, placer, null, itemInfo, false);
								}
								else {
									final String msg = this.getString(parent_node + ".Explosive_Devices.Message_Disarm");
									if (msg != null) {
										breaker.sendMessage(msg);
									}
									block.removeMetadata("CS_transformers", this);
									block.setType(Material.AIR);
								}
								event.setCancelled(true);
								break;
							}
							break;
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void liquidContact(final BlockFromToEvent event) {
		if (MaterialManager.isSkullBlock(event.getToBlock())) {
			final BlockState state = event.getToBlock().getState();
			if (state instanceof Skull) {
				Skull skull;
				try {
					skull = (Skull)state;
				}
				catch (ClassCastException ex) {
					return;
				}
				if (skull.getOwner() != null && skull.getOwner().contains("\u060c")) {
					event.setCancelled(true);
				}
			}
		}
	}

	public Vector determinePosition(final Player player, final boolean dualWield, final boolean leftClick) {
		int leftOrRight = 90;
		if (dualWield && leftClick) {
			leftOrRight = -90;
		}
		final double playerYaw = (player.getLocation().getYaw() + 90.0f + leftOrRight) * 3.141592653589793 / 180.0;
		final double x = Math.cos(playerYaw);
		final double y = Math.sin(playerYaw);
		final Vector vector = new Vector(x, 0.0, y);
		return vector;
	}

	public boolean itemIsSafe(final ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().getDisplayName() != null;
	}

	public float findNormal(float yaw) {
		while (yaw <= -180.0f) {
			yaw += 360.0f;
		}
		while (yaw > 180.0f) {
			yaw -= 360.0f;
		}
		return yaw;
	}

	public BlockFace getBlockDirection(float yaw) {
		yaw = this.findNormal(yaw);
		switch ((int)yaw) {
		case 0: {
			return BlockFace.NORTH;
		}
		case 90: {
			return BlockFace.EAST;
		}
		case 180: {
			return BlockFace.SOUTH;
		}
		case 270: {
			return BlockFace.WEST;
		}
		default: {
			if (yaw >= -45.0f && yaw < 45.0f) {
				return BlockFace.NORTH;
			}
			if (yaw >= 45.0f && yaw < 135.0f) {
				return BlockFace.EAST;
			}
			if (yaw >= -135.0f && yaw < -45.0f) {
				return BlockFace.WEST;
			}
			return BlockFace.SOUTH;
		}
		}
	}

	@EventHandler
	public void trapCard(final InventoryOpenEvent event) {
		if (event.getInventory().getType() != InventoryType.CHEST || !(event.getPlayer() instanceof Player) || this.boobs.isEmpty()) {
			return;
		}
		final Player opener = (Player)event.getPlayer();
		final Inventory chest = event.getInventory();
		Block block = null;
		if (chest.getHolder() instanceof Chest) {
			final Chest chestHolder = (Chest)chest.getHolder();
			if (chestHolder != null) {
				block = chestHolder.getBlock();
			}
		}
		else if (chest.getHolder() instanceof DoubleChest) {
			block = ((DoubleChest)chest.getHolder()).getLocation().getBlock();
		}
		if (block == null) {
			return;
		}
		if (block.hasMetadata("CS_btrap")) {
			event.setCancelled(true);
			return;
		}
		final ItemStack[] contents = chest.getContents();
		ItemStack[] array;
		for (int length = (array = contents).length, i = 0; i < length; ++i) {
			final ItemStack susItem = array[i];
			if (susItem != null && this.itemIsSafe(susItem)) {
				final String weaponTitle = this.getPureName(susItem.getItemMeta().getDisplayName());
				if (this.boobs.containsKey(weaponTitle)) {
					final String parentNode = this.boobs.get(weaponTitle);
					if (!this.csminion.getBoobean(1, parentNode)) {
						return;
					}
					final String ammoReading = this.csminion.extractReading(susItem.getItemMeta().getDisplayName());
					if (ammoReading.equals("?")) {
						break;
					}
					final Player planter = Bukkit.getServer().getPlayer(ammoReading);
					if (planter == event.getPlayer()) {
						break;
					}
					if (!this.csminion.getBoobean(4, parentNode)) {
						susItem.setAmount(susItem.getAmount() - 1);
					}
					this.slapAndReaction(opener, planter, block, parentNode, chest, contents, ammoReading, null);
					return;
				}
			}
		}
	}

	public void slapAndReaction(final Player opener, final Player planter, final Block block, final String parent_node, final Inventory chest, final ItemStack[] content, final String planterName, final Item picked) {
		if (opener.hasMetadata("CS_trigDelay")) {
			return;
		}
		if (planter == null) {
			this.activateTrapCard(opener, planter, block, parent_node, chest, content, planterName, picked);
			return;
		}
		opener.setMetadata("CS_trigDelay", new FixedMetadataValue(this, false));
		this.csminion.tempVars(opener, "CS_trigDelay", 200L);
		opener.setMetadata("CS_singed", new FixedMetadataValue(this, false));
		this.csminion.illegalSlap(planter, opener, 0);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				if (opener.hasMetadata("CS_singed") && opener.getMetadata("CS_singed").get(0).asBoolean()) {
					opener.removeMetadata("CS_singed", CSDirector.this.plugin);
					opener.removeMetadata("CS_trigDelay", CSDirector.this.plugin);
					CSDirector.this.activateTrapCard(opener, planter, block, parent_node, chest, content, planterName, picked);
				}
			}
		}, 1L);
	}

	public void activateTrapCard(final Player opener, final Player planter, final Block block, final String parent_node, final Inventory chest, final ItemStack[] content, final String planterName, final Item picked) {
		final boolean unlimited = this.csminion.getBoobean(4, parent_node);
		if (planter != null) {
			this.sendPlayerMessage(planter, parent_node, ".Explosive_Devices.Message_Trigger_Placer", planterName, opener.getName(), "<flight>", "<damage>");
			this.playSoundEffects(planter, parent_node, ".Explosive_Devices.Sounds_Alert_Placer", false, null);
		}
		if (picked == null) {
			this.projectileExplosion(null, parent_node, false, planter, false, true, null, block, true, 0);
			block.setMetadata("CS_btrap", new FixedMetadataValue(this, false));
			if (!unlimited) {
				chest.setContents(content);
			}
		}
		else {
			this.projectileExplosion(null, parent_node, false, planter, false, true, null, block.getRelative(BlockFace.DOWN), true, 0);
			if (!unlimited) {
				picked.remove();
			}
		}
		this.sendPlayerMessage(opener, parent_node, ".Explosive_Devices.Message_Trigger_Victim", planterName, opener.getName(), "<flight>", "<damage>");
		this.playSoundEffects(null, parent_node, ".Explosive_Devices.Sounds_Trigger", false, block.getLocation().add(0.5, 0.5, 0.5));
	}

	@EventHandler
	public void onHopperGulp(final InventoryPickupItemEvent event) {
		final ItemStack item = event.getItem().getItemStack();
		if (this.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains("\u0aee")) {
			event.getItem().remove();
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onTrapDispense(final BlockDispenseEvent event) {
		final Block block = event.getBlock();
		if (block.getType() != Material.DISPENSER) {
			return;
		}
		final MaterialData data = block.getState().getData();
		final Dispenser dispenser = (Dispenser)data;
		final BlockFace face = ((Directional)dispenser).getFacing();
		if (this.csminion.boobyAction(block.getRelative(face).getRelative(BlockFace.DOWN), null, event.getItem())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPressurePlate(final EntityInteractEvent event) {
		if (MaterialManager.isPressurePlate(event.getBlock()) && event.getEntity() instanceof LivingEntity) {
			final List<Entity> l = event.getEntity().getNearbyEntities(4.0, 4.0, 4.0);
			for (final Entity e : l) {
				if (e instanceof ItemFrame) {
					this.csminion.boobyAction(event.getBlock(), event.getEntity(), ((ItemFrame)e).getItem());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSplash(final PotionSplashEvent event) {
		final ThrownPotion splashPot = event.getEntity();
		if (splashPot.hasMetadata("projParentNode")) {
			final Entity shooter = (Entity)splashPot.getShooter();
			if (shooter != null && shooter instanceof Player) {
				final PermissionAttachment attachment = shooter.addAttachment(this);
				attachment.setPermission("nocheatplus", true);
				attachment.setPermission("anticheat.check.exempt", true);
				final String parentNode = splashPot.getMetadata("projParentNode").get(0).asString();
				final boolean enableExplode = this.getBoolean(parentNode + ".Explosions.Enable");
				final boolean impactExplode = this.getBoolean(parentNode + ".Explosions.On_Impact_With_Anything");
				if (enableExplode && impactExplode) {
					this.projectileExplosion(null, parentNode, false, (Player)shooter, false, true, null, splashPot.getLocation().getBlock(), true, 0);
				}
				for (final Entity ent : event.getAffectedEntities()) {
					if (ent != shooter && !ent.isDead() && !event.isCancelled()) {
						if (ent instanceof Player) {
							ent.setMetadata("CS_Energy", new FixedMetadataValue(this, parentNode));
							((LivingEntity)ent).damage(0.0, shooter);
						}
						else {
							this.dealDamage(shooter, (LivingEntity)ent, null, parentNode);
						}
					}
				}
				event.setCancelled(true);
				shooter.removeAttachment(attachment);
			}
		}
	}

	public boolean validHotbar(final Player shooter, final String parent_node) {
		boolean retVal = true;
		final String invCtrl = this.getString(parent_node + ".Item_Information.Inventory_Control");
		if (invCtrl != null) {
			final Inventory playerInv = shooter.getInventory();
			final String[] groupList = invCtrl.replaceAll(" ", "").split(",");
			String[] array;
			for (int length = (array = groupList).length, j = 0; j < length; ++j) {
				final String invGroup = array[j];
				final int groupLimit = this.getInt(invGroup + ".Limit");
				int groupCount = 0;
				for (int i = 0; i < 9; ++i) {
					final ItemStack checkItem = playerInv.getItem(i);
					if (checkItem != null && this.itemIsSafe(checkItem)) {
						final String[] checkParent = this.itemParentNode(checkItem, shooter);
						if (checkParent != null) {
							final String groupCheck = this.getString(checkParent[0] + ".Item_Information.Inventory_Control");
							if (groupCheck != null && groupCheck.contains(invGroup)) {
								++groupCount;
							}
						}
					}
				}
				if (groupCount > groupLimit) {
					this.sendPlayerMessage(shooter, invGroup, ".Message_Exceeded", "<shooter>", "<victim>", "<flight>", "<damage>");
					this.playSoundEffects(shooter, invGroup, ".Sounds_Exceeded", false, null);
					retVal = false;
				}
			}
		}
		return retVal;
	}

	public boolean tossBomb(final Player player, final String parentNode, final ItemStack heldItem, final boolean rdeEnable) {
		boolean retVal = false;
		final String type = this.getString(parentNode + ".Explosive_Devices.Device_Type");
		if (rdeEnable && type != null && type.equalsIgnoreCase("itembomb")) {
			final int gunSlot = player.getInventory().getHeldItemSlot();
			final String metaTag = parentNode + "shootDelay" + gunSlot;
			if (player.hasMetadata(metaTag)) {
				return false;
			}
			player.setMetadata(metaTag, new FixedMetadataValue(this, true));
			this.csminion.tempVars(player, metaTag, (long)this.getInt(parentNode + ".Shooting.Delay_Between_Shots"));
			final String preInfo = this.getString(parentNode + ".Explosive_Devices.Device_Info");
			final String[] deviceInfo = (preInfo == null) ? null : preInfo.split(",");
			if (this.csminion.bombIsInvalid(player, deviceInfo, parentNode)) {
				return true;
			}
			final double speed = Double.valueOf(deviceInfo[1]) * 0.1;
			final ItemStack bombType = this.csminion.parseItemStack(deviceInfo[2]);
			final boolean ammoEnable = this.getBoolean(parentNode + ".Ammo.Enable");
			final String ammoInfo = this.getString(parentNode + ".Ammo.Ammo_Item_ID");
			final boolean takeAmmo = this.getBoolean(parentNode + ".Ammo.Take_Ammo_Per_Shot");
			int detectedAmmo = 0;
			final String bracketInfo = this.csminion.extractReading(heldItem.getItemMeta().getDisplayName());
			try {
				detectedAmmo = Integer.valueOf(bracketInfo);
			}
			catch (NumberFormatException ex) {}
			if (detectedAmmo <= 0) {
				return true;
			}
			if (ammoEnable && takeAmmo) {
				if (!this.csminion.containsItemStack(player, ammoInfo, 1, parentNode)) {
					this.playSoundEffects(player, parentNode, ".Ammo.Sounds_Shoot_With_No_Ammo", false, null);
					return true;
				}
				this.csminion.replaceBrackets(heldItem, String.valueOf(detectedAmmo - 1), parentNode);
				this.csminion.removeNamedItem(player, ammoInfo, 1, parentNode, false);
			}
			else {
				this.csminion.replaceBrackets(heldItem, String.valueOf(detectedAmmo - 1), parentNode);
			}
			final Item itemBomb = player.getWorld().dropItem(player.getEyeLocation(), bombType);
			itemBomb.setVelocity(player.getEyeLocation().getDirection().multiply(speed));
			itemBomb.setPickupDelay(24000);
			this.playSoundEffects(player, parentNode, ".Explosive_Devices.Sounds_Deploy", false, null);
			final String playerName = player.getName();
			Map<String, ArrayDeque<Item>> subList = this.itembombs.get(playerName);
			if (subList == null) {
				subList = new HashMap<String, ArrayDeque<Item>>();
				this.itembombs.put(playerName, subList);
			}
			ArrayDeque<Item> subSubList = subList.get(parentNode);
			if (subSubList == null) {
				subSubList = new ArrayDeque<Item>();
				subList.put(parentNode, subSubList);
			}
			subSubList.add(itemBomb);
			if (subSubList.size() > Integer.valueOf(deviceInfo[0])) {
				subSubList.removeFirst().remove();
			}
			final ItemStack grenStack = itemBomb.getItemStack();
			this.csminion.setItemName(grenStack, playerName + "\u0aee\u0aee" + itemBomb.getUniqueId());
			itemBomb.setItemStack(grenStack);
			this.callShootEvent(player, itemBomb, parentNode);
			retVal = true;
		}
		return retVal;
	}

	public void detonateC4(final Player shooter, final ItemStack item, final String parentNode, final String deviceType) {
		List<String> lore = null;
		String[] deviceInfo = null;
		final String playerName = shooter.getName();
		boolean rdeFound = false;
		boolean itemMode = false;
		boolean noneToBoom = true;
		if (deviceType.equalsIgnoreCase("itembomb")) {
			final String itemName = item.getItemMeta().getDisplayName();
			final String preInfo = this.getString(parentNode + ".Explosive_Devices.Device_Info");
			deviceInfo = (preInfo == null) ? null : preInfo.split(",");
			if (this.csminion.bombIsInvalid(shooter, deviceInfo, parentNode) || itemName.contains("�" + deviceInfo[0] + "�")) {
				return;
			}
			rdeFound = true;
			itemMode = true;
			if (this.itembombs.containsKey(playerName)) {
				final int delay = this.getInt(parentNode + ".Explosions.Explosion_Delay");
				final ItemStack detItem = this.csminion.parseItemStack(deviceInfo[3]);
				final ArrayDeque<Item> subSubList = this.itembombs.get(playerName).get(parentNode);
				if (subSubList != null) {
					while (!subSubList.isEmpty()) {
						noneToBoom = false;
						final Item bomb = subSubList.removeFirst();
						this.playSoundEffects(bomb, parentNode, ".Explosive_Devices.Sounds_Trigger", false, null);
						this.projectileExplosion(bomb, parentNode, false, shooter, false, false, null, null, false, 0);
						detItem.setItemMeta(bomb.getItemStack().getItemMeta());
						bomb.setItemStack(detItem);
						this.prepareTermination(bomb, true, (long)delay);
					}
					this.itembombs.get(playerName).remove(parentNode);
				}
			}
		}
		else if (item.getItemMeta().hasLore()) {
			lore = item.getItemMeta().getLore();
			final Iterator<String> it = lore.iterator();
			while (it.hasNext()) {
				String line = it.next();
				if (line.contains(String.valueOf('\u1390'))) {
					line = line.replace(" ", "");
					final String[] itemInfo = line.split("]�r�e|\\\u1390|,");
					this.csminion.detonateRDE(shooter, null, itemInfo, true);
					it.remove();
					rdeFound = true;
				}
			}
		}
		if (rdeFound) {
			String capacity = "0";
			final String[] refinedOre = itemMode ? deviceInfo : this.csminion.returnRefinedOre(shooter, parentNode);
			if (refinedOre != null) {
				capacity = refinedOre[0];
			}
			if (!itemMode || !noneToBoom) {
				this.playSoundEffects(shooter, parentNode, ".Explosive_Devices.Sounds_Alert_Placer", false, null);
			}
			if (!this.getBoolean(parentNode + ".Extras.One_Time_Use")) {
				this.csminion.replaceBrackets(item, capacity, parentNode);
			}
			else if (item.getItemMeta().getDisplayName() != null && item.getItemMeta().getDisplayName().contains("�0�")) {
				shooter.getInventory().setItemInHand(null);
				shooter.updateInventory();
				return;
			}
			if (!itemMode) {
				final ItemMeta detmeta = item.getItemMeta();
				detmeta.setLore(lore);
				item.setItemMeta(detmeta);
				shooter.getInventory().setItemInHand(item);
			}
		}
	}

	public void checkCorruption(final ItemStack item, final boolean isAttachment, final boolean isDual) {
		String itemName = item.getItemMeta().getDisplayName();
		final boolean noBracket = !itemName.contains("�");
		final boolean noArrow = isAttachment && !itemName.contains(String.valueOf('\u25c0')) && !itemName.contains(String.valueOf('\u25c1'));
		if (noBracket || noArrow) {
			final Pattern pattern = Pattern.compile("-?\\d+");
			final int startingPos = (isAttachment || isDual) ? getLastChar(itemName, ' ', 3) : itemName.lastIndexOf(" ");
			final String[] bracketInfo = itemName.substring(startingPos + 1).split(" ");
			final String[] ammo = { "", "", "" };
			if (isAttachment || isDual) {
				for (int i = 0; i < 3; i += 2) {
					final Matcher matcher = pattern.matcher(bracketInfo[i]);
					ammo[i] = (matcher.find() ? matcher.group() : String.valueOf('\u00d7'));
				}
				final String splitter = isDual ? " | " : " \u25c0\u25b7 ";
				itemName = itemName.substring(0, startingPos + 1) + "�" + ammo[0] + splitter + ammo[2] + "�";
			}
			else {
				final Matcher matcher2 = pattern.matcher(bracketInfo[0]);
				ammo[0] = (matcher2.find() ? matcher2.group() : String.valueOf('\u00d7'));
				itemName = itemName.substring(0, startingPos + 1) + "�" + ammo[0] + "�";
			}
			this.csminion.setItemName(item, itemName);
		}
	}

	public static int getLastChar(final String str, final char c, int n) {
		int pos;
		for (pos = str.lastIndexOf(c); n-- > 1 && pos != -1; pos = str.lastIndexOf(c, pos - 1)) {}
		return pos;
	}

	public int getReloadAmount(final Player player, final String weaponTitle, final ItemStack item) {
		final int capacity = this.getInt(weaponTitle + ".Reload.Reload_Amount");
		final WeaponCapacityEvent event = new WeaponCapacityEvent(player, weaponTitle, item, capacity);
		this.getServer().getPluginManager().callEvent(event);
		return event.getCapacity();
	}

	public String[] getAttachment(final String weaponTitle, final ItemStack item) {
		final String attachType = this.getString(weaponTitle + ".Item_Information.Attachments.Type");
		if (attachType == null || attachType.equalsIgnoreCase("accessory")) {
			return new String[] { attachType, null };
		}
		final String attachment = this.getString(weaponTitle + ".Item_Information.Attachments.Info");
		final WeaponAttachmentEvent event = new WeaponAttachmentEvent(weaponTitle, item, attachment);
		this.getServer().getPluginManager().callEvent(event);
		return new String[] { event.isCancelled() ? null : attachType, event.getAttachment() };
	}

	public boolean isDualWield(final Player player, final String weaponTitle, final ItemStack item) {
		final boolean dualWield = this.getBoolean(weaponTitle + ".Shooting.Dual_Wield");
		final WeaponDualWieldEvent event = new WeaponDualWieldEvent(player, weaponTitle, item, dualWield);
		this.getServer().getPluginManager().callEvent(event);
		return event.isDualWield();
	}

	public boolean isDifferentItem(final ItemStack item, final String weaponTitle) {
		if (this.getBoolean(weaponTitle + ".Item_Information.Skip_Name_Check")) {
			final String itemWeaponTitle = this.isSkipNameItem(item);
			return itemWeaponTitle == null || !itemWeaponTitle.equals(weaponTitle);
		}
		final String itemName = this.getString(weaponTitle + ".Item_Information.Item_Name");
		return !item.getItemMeta().getDisplayName().startsWith(itemName);
	}

	public boolean isValid(int tick, final int fireRate) {
		switch (fireRate) {
		case 1: {
			return tick % 4 == 1;
		}
		case 2: {
			tick %= 7;
			return tick == 1 || tick == 4;
		}
		case 3: {
			return tick % 3 == 1;
		}
		case 4: {
			tick %= 5;
			return tick == 1 || tick == 3;
		}
		case 5: {
			tick %= 7;
			return tick == 1 || tick == 3 || tick == 5;
		}
		case 6: {
			return tick % 2 == 1;
		}
		case 7: {
			return tick == 2 || tick % 2 == 1;
		}
		case 8: {
			tick %= 5;
			return tick == 1 || tick == 2 || tick == 4;
		}
		case 9: {
			tick %= 6;
			return tick != 2 && tick != 0;
		}
		case 10: {
			return tick % 3 != 0;
		}
		case 11: {
			return tick % 4 != 0;
		}
		case 12: {
			return tick % 5 != 0;
		}
		case 13: {
			return tick % 6 != 0;
		}
		case 14: {
			return tick % 10 != 0;
		}
		case 15: {
			return tick != 20;
		}
		case 16: {
			return true;
		}
		default: {
			return true;
		}
		}
	}
}
