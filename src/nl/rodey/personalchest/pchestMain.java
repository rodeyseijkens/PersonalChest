package nl.rodey.personalchest;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

public class pchestMain extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	
	private pchestManager chestManager = new pchestManager(this);
	private final pchestListener playerListener = new pchestListener(this, chestManager);
	public final pchestInventoryListener inventoryListener = new pchestInventoryListener(this, chestManager);
	
    public static PermissionHandler Permissions = null;
    public boolean usingpermissions = false;
	public boolean debug = false;
	public String pchestWorlds = null;

	@Override
	public void onEnable() {
		
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" loading...");
				
        // Load configuration
        loadConfig();
       
        // Register Player Listeners
        playerListener.registerEvents();
		
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
	
	public void ShowHelp(Player player)
	{
        player.sendMessage("[PersonalChest] Usable commands:");
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
        debug = config.getBoolean("debug", false);
        pchestWorlds = config.getString("worlds", null);
        if(pchestWorlds != null)
        {
        	log.info("[PersonalChest] All Chests Worlds: " + pchestWorlds);
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
        log.severe("[pchest " + pdfFile.getVersion() + "] " + message);
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
}