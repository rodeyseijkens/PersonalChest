package nl.rodey.personalchest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class pchestInventoryListener implements Listener {	
    private final pchestMain plugin;
	private pchestManager chestManager;
	private Logger log = Logger.getLogger("Minecraft");
	
    public ItemStack[] chestContents=null;

	public pchestInventoryListener(pchestMain plugin, pchestManager chestManager) {
		this.plugin = plugin;
		this.chestManager = chestManager;
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		
		Player player = (Player) event.getPlayer();
		
		Location BLocation = getPlayerOpenedChest(player);
		
		if(BLocation != null)
		{
			String locX = String.format("%.0f", BLocation.getX());			
			String locY = String.format("%.0f", BLocation.getY());		
			String locZ = String.format("%.0f", BLocation.getZ());
			
			String blockFilename = locX + "_" + locY + "_" + locZ;
			String blockWorldName = BLocation.getWorld().getName();
			String playerName = event.getPlayer().getName();
			
			Location blockLoc = BLocation;
			
			Block block = blockLoc.getBlock();

	    	if(plugin.debug)
			{ 
				log.info("["+plugin.getDescription().getName()+"] Inventory Close Event");
			}

			if (event.getInventory() != null) {
				Inventory inv = event.getInventory();
		        chestContents = inv.getContents();
				
				File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
				File chestFile = new File(worldDataFolder , blockFilename + ".chest");
				
				if (!chestFile.exists())
				{			    	
					return;
				}
				else
				{				
					chestManager.createPersonal(playerName, chestContents, block);
				}

				chestManager.removeChestOpened(block, player);
				
				return;
			}
		}
	}

	private Location getPlayerOpenedChest(HumanEntity player) {
		String playerName = player.getName();	
		
		File playerDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Players" + File.separator + playerName);
		File chestFile = new File(playerDataFolder , "chests.open");
		
		if(chestFile.exists())
		{
			try {
				
				final BufferedReader in = new BufferedReader(new FileReader(chestFile));
	
				String line;
				line = in.readLine();
				
				// Return if empty
				if(line == null)
				{
					return null;
				}
				
				String[] totInfo = line.split("#");
	
				String[] blockInfo = totInfo[1].split("_");
	
		    	if(plugin.debug)
				{ 
					log.info("["+plugin.getDescription().getName()+"] Opened Block Location" + blockInfo[0] +" - "+ blockInfo[1] +" - "+ blockInfo[2] +" - "+ blockInfo[3]);
				}
	
		    	World worldName = plugin.getServer().getWorld(totInfo[0]);
		    	int cordX = Integer.parseInt(blockInfo[0]);
		    	int cordY = Integer.parseInt(blockInfo[1]);
		    	int cordZ = Integer.parseInt(blockInfo[2]);
		    	
		    	Location location = new Location(worldName, cordX, cordY, cordZ);
				return location;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
		
	}
}