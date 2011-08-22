package nl.rodey.personalchest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

public class pchestMain extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	
	private pchestManager chestManager = new pchestManager(this);
	private PluginManager pm;
	
    public static PermissionHandler Permissions = null;
    public boolean usingpermissions = false;
	public boolean debug = false;
	public String pchestWorlds = null;
	public String pchestResRegions = null;
	public String pchestWGRegions = null;

	@Override
	public void onEnable() {
		
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" loading...");
		
		log = getServer().getLogger();
	    final PluginManager pm = getServer().getPluginManager();
	    if (pm.getPlugin("Spout") == null)
        try {
        	downloadSprout(log, new URL("http://ci.getspout.org/job/Spout/promotion/latest/Recommended/artifact/target/spout-dev-SNAPSHOT.jar"), new File("plugins/Spout.jar"));
            pm.loadPlugin(new File("plugins" + File.separator + "Spout.jar"));
            pm.enablePlugin(pm.getPlugin("Spout"));
        } catch (final Exception ex) {
            log.warning("[LogBlock] Failed to install Spout, you may have to restart your server or install it manually.");
        }
	
        // Load configuration
        loadConfig();
       
        // Register Player Listeners
        registerEvents();
		
        // Register player commands
        getCommand("pchest").setExecutor(new pchestCommand(this, chestManager));
        
        // Check and load permissions
        Plugin permissions = getServer().getPluginManager().getPlugin("Permissions");
		if (Permissions == null)
		{
		    if (permissions != null)
		    {
			    Permissions = ((Permissions)permissions).getHandler();
			    log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is enabled with permissions!");
			    usingpermissions = true;
		    }
		    else
		    {
		    	log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is enabled without permissions!");
		    	usingpermissions = false;
		    }
		}
	}

	@Override
	public void onDisable() {		
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is disabled!");
	}	

	public void registerEvents()
    {	
		// Must be loaded after library check
		final pchestPlayerListener playerListener = new pchestPlayerListener(this, chestManager);
		final pchestInventoryListener inventoryListener = new pchestInventoryListener(this, chestManager);
		final pchestEntityListener entityListener = new pchestEntityListener(this, chestManager);
		
        pm = getServer().getPluginManager();

        /* Entity events */
        pm.registerEvent(Type.ENTITY_EXPLODE, entityListener, Event.Priority.Normal, this);

        /* Player events */
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
        
        /* Inventory events */
		pm.registerEvent(Type.CUSTOM_EVENT, inventoryListener, Event.Priority.Normal, this);
    }
	
	public void ShowHelp(Player player)
	{
        player.sendMessage(ChatColor.GREEN + "[PersonalChest]" + ChatColor.WHITE + " Usable commands:");
        player.sendMessage("/pchest [create|remove]");
        
		return;
    }
	
    public void loadConfig()
    {
        // Ensure config directory exists
        File configDir = this.getDataFolder();
        if (!configDir.exists())
            configDir.mkdir();

        // Check for existance of config file
        File configFile = new File(this.getDataFolder().toString()
                + "/config.yml");
        Configuration config = new Configuration(configFile);

        config.load();
        debug = config.getBoolean("Debug", false);
        pchestWorlds = config.getString("Worlds", null);
        if(pchestWorlds != null)
        {
        	log.info("[PersonalChest] All Chests Worlds: " + pchestWorlds);
        }
        pchestResRegions = config.getString("ResidenceRegions", null);
        pchestWGRegions = config.getString("WorldGuardRegions", null);
        if(pchestResRegions != null)
        {
        	log.info("[PersonalChest] All Residence Regions: " + pchestResRegions);
        }
        
        if(pchestWGRegions != null)
        {
        	log.info("[PersonalChest] All World Guard Regions: " + pchestWGRegions);
        }

        // Create default configuration if required
        if (!configFile.exists())
        {
            try
            {
                configFile.createNewFile();
            } 
            catch (IOException e)
            {
                reportError(e, "IOError while creating config file");
            }

            config.save();
        }        
        
    }

    public void reportError(Exception e, String message)
    {
        reportError(e, message, true);
    }

    public void reportError(Exception e, String message, boolean dumpStackTrace)
    {
        PluginDescriptionFile pdfFile = this.getDescription();
        log.severe("[PersonalChest] " + pdfFile.getVersion() + " - " + message);
        if (dumpStackTrace)
            e.printStackTrace();
    }

	public Player getPlayerByString(String playerName)
	{
		Player player = getServer().getPlayer(playerName);
		
		return player;
	}
	
	public boolean checkpermissions(Player player, String string, Boolean standard)
	{
		return ( (player.isOp() == true) || (usingpermissions ? Permissions.has(player,string) : standard));
	}

	private static void downloadSprout(Logger log, URL url, File file) throws IOException {
	    if (!file.getParentFile().exists())
	        file.getParentFile().mkdir();
	    if (file.exists())
	        file.delete();
	    file.createNewFile();
	    final int size = url.openConnection().getContentLength();
	    log.info("Downloading " + file.getName() + " (" + size / 1024 + "kb) ...");
	    final InputStream in = url.openStream();
	    final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
	    final byte[] buffer = new byte[1024];
	    int len, downloaded = 0, msgs = 0;
	    final long start = System.currentTimeMillis();
	    while ((len = in.read(buffer)) >= 0) {
	        out.write(buffer, 0, len);
	        downloaded += len;
	        if ((int)((System.currentTimeMillis() - start) / 500) > msgs) {
	            log.info((int)((double)downloaded / (double)size * 100d) + "%");
	            msgs++;
	        }
	    }
	    in.close();
	    out.close();
	    log.info("Download finished");
	}
}