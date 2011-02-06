package com.bukkit.TheDgtl.InvTools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.BlockDamageLevel;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

// Permissions
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;

/**
 * InvTools for Bukkit
 *
 * @author TheDgtl
 */
public class InvTools extends JavaPlugin {
    private final ITBlockListener blockListener = new ITBlockListener(this);
    private Logger log;
    
	Boolean groupPolicy;
	Integer repairPoint;
	Map<Integer, Boolean> tools;
    
	// Permissions
	PermissionHandler Permissions = null;
    
    public InvTools(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
    	super(pluginLoader, instance, desc, folder, plugin, cLoader);
    	log = Logger.getLogger("Minecraft");
    }
    
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        
        setupPermissions();
        loadConfig();
        
        pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Monitor, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " v." + pdfFile.getVersion() + " is enabled.");
    }
    
	public void loadConfig() {
		try {
			Configuration config = this.getConfiguration();
			groupPolicy = config.getBoolean("groupPolicy", false);
			if (Permissions == null) groupPolicy = false;
			repairPoint = config.getInt("repairPoint", 30);
			
			// Load tools that are invincible. Convert to integers.
			tools = new HashMap<Integer, Boolean>();
			String[] tmp = config.getString("Tools", "277,278,279,293").split(",");
			for (String tool : tmp) {
				if (tool.equals("")) continue;
				tools.put(Integer.parseInt(tool), true);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception while loading InvTools/config.yml", e);
		}
	}
    
    public void onDisable() {
    }
    
    public class ITBlockListener extends BlockListener {

        public ITBlockListener(final InvTools plugin) {
        }

        @Override
        public void onBlockDamage(BlockDamageEvent event) {
        	if (event.getDamageLevel() != BlockDamageLevel.BROKEN) return;
        	Player player = event.getPlayer();
        	if (player.getItemInHand().getDurability() >= repairPoint) {
	    		int itemInHand = player.getItemInHand().getTypeId();
	    		Boolean inGroup = false;
	    		
	    		if (tools.containsKey(itemInHand)) {
	    			if (groupPolicy && Permissions.has(player, "invtools.allow")) inGroup = true;
	    			if (!groupPolicy || inGroup) {
	    				// Tool is invincible. Set damage to 0.
	    				player.getItemInHand().setDurability((short)0);
	    		}
        	}
        }
        }
    }
    
    public void setupPermissions() {
    	Plugin perm  = this.getServer().getPluginManager().getPlugin("Permissions");

    	if(Permissions == null) {
    	    if(perm != null) {
    			Permissions = ((Permissions)perm).getHandler();
    	    } else {
    	    	log.info("[" + this.getDescription().getName() + "] Permission system not enabled.");
    	    }
    	}
    }
}
