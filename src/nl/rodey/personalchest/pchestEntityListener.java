package nl.rodey.personalchest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class pchestEntityListener extends EntityListener {
	private pchestManager chestManager;
	
    /**
     * Blast radius for TNT / Creepers
     */
    public final static int BLAST_RADIUS = 4;
    
    public pchestEntityListener(pchestMain plugin, pchestManager chestManager) {
		this.chestManager = chestManager;
	}

	public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (Block block : event.blockList()) {
        	
        	if(block.getType().equals(Material.CHEST)) 
        	{
        		boolean cancel = chestManager.checkChestRegisterd(block);
            	
                if (cancel) {
                    	event.setCancelled(true);
                        return;
                }
        	}
        }
    }

}
