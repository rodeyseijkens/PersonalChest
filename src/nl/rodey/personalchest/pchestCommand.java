package nl.rodey.personalchest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

public class pchestCommand implements CommandExecutor {
	private static Logger log = Logger.getLogger("Minecraft");
	private final pchestMain plugin;
	private final pchestManager chestManager;
    public ItemStack[] chestContents=null;
    
	public pchestCommand(pchestMain plugin, pchestManager chestManager) {
        this.plugin = plugin;
		this.chestManager = chestManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split)
    {
		
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;

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

            // Create a new pchest
            if (token.equalsIgnoreCase("create"))
            {
            	if (! player.hasPermission("pchest.edit"))
        		{
        	        // Message to user
        	        player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + "  You\'re not allowed to use this command.");
        			return true;
        		}
            	else
            	{
            		CommandCreate(player, arg);
            	}
            }
            // Create a new pchest
            else if (token.equalsIgnoreCase("remove"))
            {
            	if (!player.hasPermission("pchest.edit"))
        		{
        	        // Message to user
        	        player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + "  You\'re not allowed to use this command.");
        			return true;
        		}
            	else
            	{
            		CommandRemove(player, arg);
            	}
            }
            // Create a new pchest
            else if (token.equalsIgnoreCase("info"))
            {
                CommandInfo(player, arg);
            }
            // Show the Help
            else
            {
                plugin.ShowHelp(player);
            }
        }

        return true;
    }
	
    private void CommandCreate(Player player, String arg)
    {
        Block block = player.getTargetBlock(null, 3);
        Material m = block.getType();
        if (m != Material.CHEST)
        {
            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " To register a personal chest you need to be in front of a chest");
            return;
        }
		
		if(plugin.debug)
		{ 
			log.info("["+plugin.getDescription().getName()+"] Argument: " + arg);
		}
        
		Chest chest = (Chest) block.getState();
		Inventory inv = chest.getInventory();
        chestContents = inv.getContents();
		
        if(arg.equalsIgnoreCase(""))
        {
	        if(chestManager.create(chestContents, block))
	        {
	            // Message to user
	            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " This personal chest has been created");
	        }
        }        
        
        return;
    }

	private void CommandRemove(Player player, String arg)
    {
		if(plugin.debug)
		{ 
			log.info("["+plugin.getDescription().getName()+"] Argument: " + arg);
		}		
		
        Block block = player.getTargetBlock(null, 3);
        Material m = block.getType();
        if (m != Material.CHEST)
        {
            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " To unregister a personal chest you need to be in front of a chest");
            return;
        }
		
        if(chestManager.remove(block))
        {
            // Message to user
            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " This personal chest has been unregisterd");
        }
        else
        {
            // Message to user
            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " Could not unregister chest");
        }
        
        return;
    }
	
	private void CommandInfo(Player player, String arg)
    {
		if(plugin.debug)
		{ 
			log.info("["+plugin.getDescription().getName()+"] Argument: " + arg);
		}		
		
        Block block = player.getTargetBlock(null, 3);
        Material m = block.getType();
        if (m != Material.CHEST)
        {
            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " To view info of a personal chest you need to be in front of a chest");
            return;
        }
		
        if(chestManager.checkChestStatus(block))
        {
            // Message to user
            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " This chest is registerd as a PersonalChest");
        }
        else
        {
            // Message to user
            player.sendMessage(ChatColor.GREEN + "["+plugin.getDescription().getName()+"]" + ChatColor.WHITE + " This chest is not registerd");
        }
        
        return;
    }

}
