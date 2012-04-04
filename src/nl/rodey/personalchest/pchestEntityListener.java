package nl.rodey.personalchest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class pchestEntityListener implements Listener {
	private pchestManager chestManager;
	
    /**
     * Blast radius for TNT / Creepers
     */
    public final static int BLAST_RADIUS = 4;
    
    public pchestEntityListener(pchestMain plugin, pchestManager chestManager) {
		this.chestManager = chestManager;
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (Block block : event.blockList()) {
        	
        	if(block.getType().equals(Material.CHEST)) 
        	{
        		boolean cancel = chestManager.checkChestStatus(block);
            	
                if (cancel) {
                    	event.setCancelled(true);
                        return;
                }
        	}
        }
    }

}
