package com.shampaggon.crackshot;

import java.util.*;
import org.bukkit.entity.*;

public class CSMessages
{
    public static Map<String, String> messages;
    
    static {
        CSMessages.messages = new HashMap<String, String>();
    }
    
    public static void sendMessage(final Player player, final String heading, final String message) {
        if (!message.isEmpty()) {
            player.sendMessage(heading + message);
        }
    }
    
    public enum Message
    {
        NP_WEAPON_USE("NP_WEAPON_USE", 0, "NP_WEAPON_USE", 0, "NP_Weapon_Use"), 
        NP_WEAPON_CRAFT("NP_WEAPON_CRAFT", 1, "NP_WEAPON_CRAFT", 1, "NP_Weapon_Craft"), 
        NP_STORE_CREATE("NP_STORE_CREATE", 2, "NP_STORE_CREATE", 2, "NP_Store_Create"), 
        NP_STORE_PURCHASE("NP_STORE_PURCHASE", 3, "NP_STORE_PURCHASE", 3, "NP_Store_Purchase"), 
        STORE_CREATED("STORE_CREATED", 4, "STORE_CREATED", 4, "Store_Created"), 
        STORE_CANNOT_AFFORD("STORE_CANNOT_AFFORD", 5, "STORE_CANNOT_AFFORD", 5, "Store_Cannot_Afford"), 
        STORE_ITEMS_NEEDED("STORE_ITEMS_NEEDED", 6, "STORE_ITEMS_NEEDED", 6, "Store_Items_Needed"), 
        STORE_PURCHASED("STORE_PURCHASED", 7, "STORE_PURCHASED", 7, "Store_Purchased"), 
        CANNOT_RELOAD("CANNOT_RELOAD", 8, "CANNOT_RELOAD", 8, "Cannot_Reload"), 
        WEAPON_RECEIVED("WEAPON_RECEIVED", 9, "WEAPON_RECEIVED", 9, "Weapon_Received");
        
        private final String nodeName;
        
        Message(final String s2, final int n2, final String s, final int n, final String nodeName) {
            this.nodeName = nodeName;
        }
        
        public String getNodeName() {
            return this.nodeName;
        }
        
        public String getMessage() {
            return CSMessages.messages.containsKey(this.nodeName) ? this.removeColourCodes(CSMessages.messages.get(this.nodeName)) : "";
        }
        
        public String getMessage(final String itemName) {
            return this.getMessage().replace("<item>", this.removeColourCodes(itemName));
        }
        
        public String getMessage(final int amount, final String itemName) {
            return this.getMessage().replace("<amount>", String.valueOf(amount)).replace("<item>", itemName);
        }
        
        public String getMessage(final String itemName, final String crossSymbol, final int amount) {
            return this.getMessage().replace("<item>", this.removeColourCodes(itemName)).replace("<cross>", crossSymbol).replace("<amount>", String.valueOf(amount));
        }
        
        public String removeColourCodes(final String string) {
            return string.replaceAll("([�])[\\S]{0,1}", "");
        }
    }
}
