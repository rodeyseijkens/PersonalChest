package nl.rodey.personalchest;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class pchestBlockListener extends BlockListener {
    private pchestManager chestManager;
	private pchestMain plugin;
	
	public pchestBlockListener(pchestMain plugin, pchestManager chestManager) {
		this.plugin = plugin;
        this.chestManager = chestManager;
	}
	
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Block block = event.getBlock();
		Block chest2 = chestManager.getDoubleChest(block);
		Player player = event.getPlayer();
		
		if(block.getTypeId() == 54)
		{
			if(chest2 != null)
			{
				if(chestManager.checkChestStatus(block))
				{
			        event.setCancelled(true);
			        player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " Can't place a chest next to a registerd chest.");
				}
			}
		}
	}

}
