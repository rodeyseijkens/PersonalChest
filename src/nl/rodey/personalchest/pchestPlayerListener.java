package nl.rodey.personalchest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class pchestPlayerListener extends PlayerListener {	
    private pchestManager chestManager;
	
    public ItemStack[] chestContents=null;
    
	public pchestPlayerListener(pchestMain plugin, pchestManager chestManager) {
        this.chestManager = chestManager;
	}
    
    public void onPlayerInteract(PlayerInteractEvent event)
    {
    	
        Block block = event.getClickedBlock();
        if (block == null) return; 
        
        boolean cancel=false;
        
        
	        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
	        {
	        	if(block.getType().equals(Material.CHEST)) 
	            {
		        	if(chestManager.checkChestRegisterd(block))
		        	{
		                cancel = onChestInteract(block,event.getPlayer());
		    		}
		    		else
		    		{
		    			cancel = false;
		    		}
	            } 
	            
	            if(cancel) event.setCancelled(true);
	        }   
	        else if(event.getAction().equals(Action.LEFT_CLICK_BLOCK))
	        {	
	            if(block.getType().equals(Material.CHEST))
	            {
	            	if(chestManager.checkChestRegisterd(block))
	            	{
	            		cancel = true;
	            		event.getPlayer().sendMessage("[PersonalChest] This chest is protected.");
	            		
	                    if(cancel) event.setCancelled(true); 
	            	}
	            }
	        } 
        
        if(cancel) event.setCancelled(true);    		
    }
    
    private boolean onChestInteract(Block block, Player player)
    {
    	
        // By default we cancel access to treasure chests
    	boolean cancel = true;

		Chest chest = (Chest)block.getState();	
		
		if(chestManager.checkChestOpened(block))
		{
			cancel = true;
    		player.sendMessage("[PersonalChest] Chest is currently in use.");
		}
		else if(chestManager.load(player, chest, block))
		{
			chestManager.setChestOpened(block, player);
			cancel = false;
		}
				
        return cancel;
    }    
}