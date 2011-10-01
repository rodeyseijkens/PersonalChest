package nl.rodey.personalchest;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class pchestBlockListener extends BlockListener {
	private static Logger log = Logger.getLogger("Minecraft");
    private pchestManager chestManager;
	private pchestMain plugin;
	
	public pchestBlockListener(pchestMain plugin, pchestManager chestManager) {
		this.plugin = plugin;
        this.chestManager = chestManager;
	}
	
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
				if (plugin.checkpermissions(player,"pchest.remove",true))
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
