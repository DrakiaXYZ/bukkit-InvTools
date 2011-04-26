package net.TheDgtl.InvTools;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

// Permissions
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

/**
 * InvTools for Bukkit
 *
 * @author TheDgtl
 */
public class InvTools extends JavaPlugin {
    private final bListener blockListener = new bListener();
    private final sListener serverListener = new sListener();
    private final eListener entityListener = new eListener();
    private Logger log;
    PluginManager pm;
    
    Integer toolRepairPoint;
    Integer armorRepairPoint;
    HashMap<Integer, Boolean> tools;
    HashMap<Integer, Boolean> armor; 
    
    // Permissions
    Permissions permissions = null;
    
    public void onEnable() {
        pm = getServer().getPluginManager();
        log = Logger.getLogger("Minecraft");
        
        permissions = (Permissions)checkPlugin("Permissions");
        loadConfig();
        saveConfig();
        
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " v." + pdfFile.getVersion() + " is enabled.");
    }
    
    public void loadConfig() {
        try {
            Configuration config = this.getConfiguration();
            toolRepairPoint = config.getInt("toolRepairPoint", 30);
            armorRepairPoint = config.getInt("armorRepairPoint", 30);
            
            // Load tools that are invincible. Convert to integers.
            tools = new HashMap<Integer, Boolean>();
            String[] tmp = config.getString("Tools", "277,278,279,293").split(",");
            for (String tool : tmp) {
                if (tool.equals("")) continue;
                tools.put(Integer.parseInt(tool), true);
            }
            // Load invincible armor
            armor = new HashMap<Integer, Boolean>();
            tmp = config.getString("Armor", "310,311,312,313").split(",");
            for (String arm : tmp) {
                if (arm.equals("")) continue;
                armor.put(Integer.parseInt(arm), true);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception while loading InvTools/config.yml", e);
        }
    }
    
    public void saveConfig() {
        Configuration config = getConfiguration();
        config.setProperty("toolRepairPoint", toolRepairPoint);
        config.setProperty("armorRepairPoint", armorRepairPoint);
        StringBuilder b = new StringBuilder();
        for(Integer tool : tools.keySet()) {
            b.append(tool).append(",");
        }
        config.setProperty("Tools", b.toString());
        
        b = new StringBuilder();
        for (Integer arm : armor.keySet()) {
            b.append(arm).append(",");
        }
        config.setProperty("Armor", b.toString());
        config.save();
    }
    
    public void onDisable() {
    }
    
    public class bListener extends BlockListener {
        @Override
        public void onBlockBreak(BlockBreakEvent event) {
            Player player = event.getPlayer();
            if (player.getItemInHand().getDurability() >= toolRepairPoint) {
                if (hasPerm(player, "invtools.allowtools", true)) {
                    int itemInHand = player.getItemInHand().getTypeId();
                    if (tools.containsKey(itemInHand)) {
                        // Tool is invincible. Set damage to 0.
                        player.getItemInHand().setDurability((short)-1);
                        player.updateInventory();
                    }
                }
            }
        }
    }
    
    public class eListener extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player)event.getEntity();
            if (!hasPerm(player, "invtools.allowarmor", true)) return;
            
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (item.getDurability() < armorRepairPoint) continue;
                if (armor.containsKey(item.getTypeId())) {
                    item.setDurability((short)-1);
                    player.updateInventory();
                }
            }
        }
    }
    
    /*
     * Check if a plugin is loaded/enabled already. Returns the plugin if so, null otherwise
     */
    private Plugin checkPlugin(String p) {
        Plugin plugin = pm.getPlugin(p);
        return checkPlugin(plugin);
    }
    
    private Plugin checkPlugin(Plugin plugin) {
        if (plugin != null && plugin.isEnabled()) {
            log.info("[InvTools] Found " + plugin.getDescription().getName() + " (v" + plugin.getDescription().getVersion() + ")");
            return plugin;
        }
        return null;
    }
    
    /*
     * Check whether the player has the given permissions.
     */
    public boolean hasPerm(Player player, String perm, boolean def) {
        if (permissions != null) {
            return permissions.getHandler().has(player, perm);
        } else {
            return def;
        }
    }
    
    private class sListener extends ServerListener {
        @Override
        public void onPluginEnable(PluginEnableEvent event) {
            if (permissions == null) {
                if (event.getPlugin().getDescription().getName().equalsIgnoreCase("Permissions")) {
                    permissions = (Permissions)checkPlugin(event.getPlugin());
                }
            }
        }
        
        @Override
        public void onPluginDisable(PluginDisableEvent event) {
            if (event.getPlugin() == permissions) {
                log.info("[InvTools] Permissions plugin lost.");
                permissions = null;
            }
        }
    }
}
