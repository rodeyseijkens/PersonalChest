package nl.rodey.personalchest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
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
	private final pchestListener playerListener = new pchestListener(this, chestManager);
	public final pchestInventoryListener inventoryListener = new pchestInventoryListener(this, chestManager);
	
    public static PermissionHandler Permissions = null;
    public boolean usingpermissions = false;
	public boolean debug = false;
	public String pchestWorlds = null;

	@Override
	public void onEnable() {
		
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" loading...");
		
		log = getServer().getLogger();
	    final PluginManager pm = getServer().getPluginManager();
	    if (pm.getPlugin("BukkitContrib") == null)
        try {
        	downloadBukkitContrib(log, new URL("http://bit.ly/autoupdateBukkitContrib"), new File("plugins/BukkitContrib.jar"));
            pm.loadPlugin(new File("plugins" + File.separator + "BukkitContrib.jar"));
            pm.enablePlugin(pm.getPlugin("BukkitContrib"));
        } catch (final Exception ex) {
            log.warning("[LogBlock] Failed to install BukkitContrib, you may have to restart your server or install it manually.");
        }
	
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
	
	private static void downloadBukkitContrib(Logger log, URL url, File file) throws IOException {
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
