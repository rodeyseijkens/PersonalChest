package nl.rodey.personalchest;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.logging.Logger;

public class pchestBlockListener implements Listener {
	private static Logger log = Logger.getLogger("Minecraft");
    private pchestManager chestManager;
	private pchestMain plugin;
	
	public pchestBlockListener(pchestMain plugin, pchestManager chestManager) {
		this.plugin = plugin;
        this.chestManager = chestManager;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Block block = event.getBlock();
		
		if(block.getTypeId() == 54)
		{
			if(plugin.debug)
			{ 
				log.info("["+plugin.getDescription().getName()+"] Chest Placed");
			}
			
			Block chest2 = chestManager.getDoubleChest(block);
			Player player = event.getPlayer();
			
			if(chest2 != null)
			{
				if(chestManager.checkChestStatus(chest2))
				{
			        event.setCancelled(true);
			        player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " Can't place a chest next to a registerd chest.");
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{		
		Block block = event.getBlock();
		
		if(block.getTypeId() == 54)
		{
			if(plugin.debug)
			{ 
				log.info("["+plugin.getDescription().getName()+"] Chest Broke");
			}
			
			Player player = event.getPlayer();
			
			if(chestManager.checkChestStatus(block))
        	{
				if (player.hasPermission("pchest.edit"))
	        	{
					if(chestManager.remove(block))
			        {
			            // Message to user
			            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " PersonalChest has been unregisterd");
			        }
			        else
			        {
			            // Message to user
			            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " Could not unregister chest");

		        		event.setCancelled(true); 
			        }
				}
				else
				{
	        		player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " This chest is protected.");
	        		
	        		event.setCancelled(true); 
				}	
        	}
		}
	}

}
