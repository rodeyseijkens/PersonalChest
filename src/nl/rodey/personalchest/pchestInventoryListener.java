package nl.rodey.personalchest;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkitcontrib.event.inventory.InventoryCloseEvent;
import org.bukkitcontrib.event.inventory.InventoryListener;

public class pchestInventoryListener extends InventoryListener {
	private Logger log = Logger.getLogger("Minecraft");
	
    private final pchestMain plugin;
	private pchestManager chestManager;
	
    public ItemStack[] chestContents=null;

	public pchestInventoryListener(pchestMain plugin, pchestManager chestManager) {
		this.plugin = plugin;
		this.chestManager = chestManager;
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		
		if(event.getLocation() != null)
		{
			String locX = String.format("%.0f", event.getLocation().getX());			
			String locY = String.format("%.0f", event.getLocation().getY());		
			String locZ = String.format("%.0f", event.getLocation().getZ());
			
			String blockFilename = locX + "_" + locY + "_" + locZ;
			String blockWorldName = event.getLocation().getWorld().getName();
			String playerName = event.getPlayer().getName();

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
				chestManager.createPersonal(chestContents, blockFilename, blockWorldName, playerName);

				chestManager.removeChestOpened(blockFilename, blockWorldName);
				
				if(plugin.debug)
				{ 
					event.getPlayer().sendMessage("[PersonalChest] Inventory Saved: "+blockFilename+" "+playerName);
					log.info("[PersonalChest] Inventory Closed and Saved: "+blockFilename+" "+playerName);
				}
			}
			
			
		}
	}
}