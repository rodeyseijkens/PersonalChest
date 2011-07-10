package nl.rodey.personalchest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class pchestManager {

	private static Logger log = Logger.getLogger("Minecraft");
	private final pchestMain plugin;
	
    public ItemStack[] chestContents=null;

	public pchestManager(pchestMain plugin) {
        this.plugin = plugin;
	}

	public boolean create(ItemStack[] chestContents, String blockFilename, String blockWorldName) {
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds"+ File.separator + blockWorldName);
		worldDataFolder.mkdirs();

		try {
			final File chestFile = new File(worldDataFolder , blockFilename + ".chest");
			if (chestFile.exists())
				chestFile.delete();
			chestFile.createNewFile();

			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));

			for(int i =0;i<chestContents.length;i++)
	        {
		        if(chestContents[i]!=null)
		        {
			        out.write(chestContents[i].getTypeId() + ":" + chestContents[i].getAmount() + ":" + chestContents[i].getDurability() + "\r\n");
		        }
		        else
		        {
		        	out.write("0:0:0\r\n");
		        }
	        }
			out.close();

			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Chest created!");
			}

		} catch (IOException e) {
			e.printStackTrace();
			
			return false;
		}
		
		return true;

	}
	
	public boolean createPersonal(ItemStack[] chestContents, String blockFilename, String blockWorldName, String playerName) {
		
		File personalChestFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Players" + File.separator + playerName + File.separator + "Worlds" + File.separator + blockWorldName);
		personalChestFolder.mkdirs();

		try {
			final File chestFile = new File(personalChestFolder , blockFilename + ".chest");
			if (chestFile.exists())
				chestFile.delete();
			chestFile.createNewFile();

			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));

			for(int i =0;i<chestContents.length;i++)
	        {
		        if(chestContents[i]!=null)
		        {
			        out.write(chestContents[i].getTypeId() + ":" + chestContents[i].getAmount() + ":" + chestContents[i].getDurability() + "\r\n");
		        }
		        else
		        {
		        	out.write("0:0:0\r\n");
		        }
	        }
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
			
			return false;
		}
		
		return true;

	}

	public boolean load(Chest chest, String blockFilename, String blockWorldName, Player player) {
		
		boolean complete = false;

		Inventory newInv = chest.getInventory();
		// First clear inventory befor loading file
		newInv.clear();
		
		String playerName = player.getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
		
		File personalChestFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Players" + File.separator + playerName + File.separator + "Worlds" + File.separator + blockWorldName);
		personalChestFolder.mkdirs();
		
		File personalchestFile = new File(personalChestFolder , blockFilename + ".chest");
		
		
		if (!chestFile.exists())
		{
			
			String data = plugin.pchestWorlds;
			if(data != null)
			{
				
				//Check if the world is an PersonalChest world
		        String[] worlds = data.split(",");
		        
		        for (String world : worlds)
		        {
		        	if(plugin.debug)
	    			{ 
	    				log.info("[PersonalChest] World Check: " + blockWorldName + " - " + world);
	    			}
		        	
		        	if(blockWorldName.equalsIgnoreCase(world))
		        	{

			        	if(plugin.debug)
		    			{ 
		    				log.info("[PersonalChest] World Check: They are Equal");
		    			}
			        	
		                chestContents = newInv.getContents();
		        		
		                if(create(chestContents, blockFilename, blockWorldName))
		                {
		        			if(plugin.debug)
		        			{ 
		        				log.info("[PersonalChest] Chest Added to list because it is an PersonalChest World");
		        			}
		                }
		                else
		                {
		        			if(plugin.debug)
		        			{ 
		        				log.info("[PersonalChest] Error occured while creating the new Chest");
		        			}
		                }
		        	}
		        	else
		        	{		    			
		    			//Check if player file is there to delete.
		    			personalchestFile.delete();
		    			
		    			// Punched Chest isn't a PersonalChest
		    			complete = true;
		    			
		    			return complete;
		        	}
		        }
			}
		}		
		
		if (!personalchestFile.exists())
		{		
			
			try {
				copy(chestFile, personalchestFile);
				if(plugin.debug)
				{ 
					log.info("[PersonalChest] Inventory copied "+ blockFilename+" "+playerName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			

		try {
			
			final BufferedReader in = new BufferedReader(new FileReader(personalchestFile));

			String line;
			int field = 0;
			while ((line = in.readLine()) != null) {
				if (line != "") {
					final String[] parts = line.split(":");
					try {
						int type = Integer.parseInt(parts[0]);
						int amount = Integer.parseInt(parts[1]);
						short damage = Short.parseShort(parts[2]);
						if (type != 0) {
							newInv.setItem(field, new ItemStack(type, amount, damage));
						}
					} catch (NumberFormatException e) {
						// ignore
					}
					++field;
				}
			}

			in.close();
			complete = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return complete;		
	}
	
	private void copy(File source, File target) throws IOException {

        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target);
    
        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }

	public boolean remove(Chest chest, String blockFilename, String blockWorldName) {
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
		
		// Add the original Items back in the chest
		Inventory newInv = chest.getInventory();	

		try {
			// First clear inventory befor loading file
			newInv.clear();
			
			final BufferedReader in = new BufferedReader(new FileReader(chestFile));

			String line;
			int field = 0;
			while ((line = in.readLine()) != null) {
				if (line != "") {
					final String[] parts = line.split(":");
					try {
						int type = Integer.parseInt(parts[0]);
						int amount = Integer.parseInt(parts[1]);
						short damage = Short.parseShort(parts[2]);
						if (type != 0) {
							newInv.setItem(field, new ItemStack(type, amount, damage));
						}
					} catch (NumberFormatException e) {
						// ignore
					}
					++field;
				}
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		if(chestFile.delete())
		{
			return true;
		}
		
		return false;
	}
	
	public boolean checkChestRegisterd(Block block)
	{
		Chest chest = (Chest)block.getState();	
		Inventory newInv = chest.getInventory();
		
		String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
		
		if (chestFile.exists())
		{
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Chest is Registerd "+ blockFilename);
			}
			return true;
		}
		else
		{
			String data = plugin.pchestWorlds;
			if(data != null)
			{
				
				//Check if the world is an PersonalChest world
		        String[] worlds = data.split(",");
		        
		        for (String world : worlds)
		        {
		        	if(plugin.debug)
	    			{ 
	    				log.info("[PersonalChest] World Check: " + blockWorldName + " - " + world);
	    			}
		        	
		        	if(blockWorldName.equalsIgnoreCase(world))
		        	{

			        	if(plugin.debug)
		    			{ 
		    				log.info("[PersonalChest] World Check: They are Equal");
		    			}
			        	
		                chestContents = newInv.getContents();
		        		
		                if(create(chestContents, blockFilename, blockWorldName))
		                {
		        			if(plugin.debug)
		        			{ 
		        				log.info("[PersonalChest] Chest Added to list because it is an PersonalChest World");
		        			}
		                }
		                else
		                {
		        			if(plugin.debug)
		        			{ 
		        				log.info("[PersonalChest] Error occured while creating the new Chest");
		        			}
		                }
		                
		    			return true;
		        	}
		        }
			}
		}
		
		return false;
	}
	
	public boolean checkDoubleChest(Block block) {
		if (block.getRelative(BlockFace.NORTH).getTypeId() == 54) {
			return true;
		} else if (block.getRelative(BlockFace.EAST).getTypeId() == 54) {
			return true;
		} else if (block.getRelative(BlockFace.SOUTH).getTypeId() == 54) {
			return true;
		} else if (block.getRelative(BlockFace.WEST).getTypeId() == 54) {
			return true;
		}
		return false;
	}

	public boolean checkChestOpened(String blockFilename, String blockWorldName) {

		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "OPEN");
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");

		if (chestFile.exists())
		{
			try {
				
				final BufferedReader in = new BufferedReader(new FileReader(chestFile));

				String line;
				line = in.readLine();

				Player playerInFile = plugin.getPlayerByString(line);
				
				if(playerInFile!=null && playerInFile.isOnline())
				{
					if(plugin.debug)
					{ 
						log.info("[PersonalChest] " + line + " is Online.");
					}
					in.close();
					return true;
				}
				else
				{
					if(plugin.debug)
					{ 
						log.info("[PersonalChest] " + line + " is Offline.");
					}
					in.close();
					removeChestOpened(blockFilename, blockWorldName);
					return false;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return false;
	}
	
	public void setChestOpened(String blockFilename, String blockWorldName, Player player) {

		String playerName = player.getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "OPEN");
		worldDataFolder.mkdirs();
		
		try {
			File chestFile = new File(worldDataFolder , blockFilename + ".chest");
			
			chestFile.createNewFile();
	
			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));

        	out.write(playerName);
        	
			out.close();
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void removeChestOpened(String blockFilename, String blockWorldName) {
			File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "OPEN");
			File chestFile = new File(worldDataFolder , blockFilename + ".chest");
			
			chestFile.delete();
	}
	
}
