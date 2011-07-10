package nl.rodey.personalchest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class pchestCommand implements CommandExecutor {
	private final pchestMain plugin;
	private final pchestManager chestManager;
	private Chest chest;
    public ItemStack[] chestContents=null;
    
	public pchestCommand(pchestMain plugin, pchestManager chestManager) {
        this.plugin = plugin;
		this.chestManager = chestManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
            String label, String[] split)
    {
		
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;
		
		if (!plugin.checkpermissions(player,"pchest.edit",true))
		{
	        // Message to user
	        player.sendMessage("[PersonalChest] You\'re not allowed to use this command.");
			return true;
		}

        if (split.length == 0)
            plugin.ShowHelp(player);
        else
        {
            String token = split[0];

            // Splurge the remaining bits together
            String arg = "";
            if (split.length > 1)
            {
                for (int i = 1; i < split.length; i++)
                    arg = arg + split[i] + " ";
                arg = arg.trim();
            }

            // Create a new treasure hunt
            if (token.equalsIgnoreCase("create"))
                CommandCreate(player);
            
            // Create a new treasure hunt
            else if (token.equalsIgnoreCase("remove"))
                CommandRemove(player);
            
            // Set the currently edited hunt
            else
                plugin.ShowHelp(player);
        }

        return true;
    }
	
    private void CommandCreate(Player player)
    {
        Block block = player.getTargetBlock(null, 3);
        Material m = block.getType();
        if (m != Material.CHEST)
        {
            player.sendMessage("[PersonalChest] To register a personal chest you need to be in front of a chest");
            return;
        }
        
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
		
		chest = (Chest)block.getState();
		Inventory inv = chest.getInventory();
        chestContents = inv.getContents();
		
        if(chestManager.create(chestContents, blockFilename, blockWorldName))
        {
            // Message to user
            player.sendMessage("[PersonalChest] This personal chest has been created");
        }		
        
        return;
    }
	
    private void CommandRemove(Player player)
    {
        Block block = player.getTargetBlock(null, 3);
        Material m = block.getType();
        if (m != Material.CHEST)
        {
            player.sendMessage("[PersonalChest] To unregister a personal chest you need to be in front of a chest");
            return;
        }
        
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
       
		chest = (Chest)block.getState();
		
        if(chestManager.remove(chest, blockFilename, blockWorldName))
        {
            // Message to user
            player.sendMessage("[PersonalChest] This personal chest has been unregisterd");
        }
        else
        {
            // Message to user
            player.sendMessage("[PersonalChest] Could not unregister chest");
        }
        
        return;
    }

}
