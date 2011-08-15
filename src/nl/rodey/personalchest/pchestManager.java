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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.Vector;

public class pchestManager {

	private static Logger log = Logger.getLogger("Minecraft");
	private final pchestMain plugin;
	
    public ItemStack[] chestContents=null;

	public pchestManager(pchestMain plugin) {
        this.plugin = plugin;
	}
	 
	private WorldGuardPlugin getWorldGuard() {
	    Plugin pluginWG = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (pluginWG == null || !(pluginWG instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) pluginWG;
	}
	
	private Residence getResidence() {
	    Plugin pluginRes = plugin.getServer().getPluginManager().getPlugin("Residence");
	 
	    // WorldGuard may not be loaded
	    if (pluginRes == null || !(pluginRes instanceof Residence)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (Residence) pluginRes;
	}
	
	public boolean create(ItemStack[] chestContents, Block block) {
		String blockWorldName = block.getWorld().getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds"+ File.separator + blockWorldName);
		worldDataFolder.mkdirs();		

		
		if(!checkDoubleChest(block))
		{
        	if(plugin.debug)
			{ 
				log.info("[PersonalChest] Saved Single Chest");
			}

    		return saveSingleChest(chestContents, block, worldDataFolder);
		}
		else
		{
        	if(plugin.debug)
			{ 
				log.info("[PersonalChest] Saved Double Chest");
			}
        	
        	return createDoubleChest(block, worldDataFolder);
		}

	}
	
	public boolean createPersonal(String playerName, ItemStack[] chestContents, Block block) {

		String blockWorldName = block.getWorld().getName();
		
		File personalChestFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Players" + File.separator + playerName + File.separator + "Worlds" + File.separator + blockWorldName);
		personalChestFolder.mkdirs();

		
		if(!checkDoubleChest(block))
		{
        	if(plugin.debug)
			{ 
				log.info("[PersonalChest] Saved Single Chest");
			}

    		return saveSingleChest(chestContents, block, personalChestFolder);
		}
		else
		{
        	if(plugin.debug)
			{ 
				log.info("[PersonalChest] Saved Double Chest");
			}
        	
        	return saveDoubleChest(chestContents, block, personalChestFolder);
		}
	}

	private boolean checkPersonalChestWorld(Block block)
	{
		
		String blockWorldName = block.getWorld().getName();
		
		String dataWorlds = plugin.pchestWorlds;
		if(dataWorlds != null)
		{
			
			//Check if the world is an PersonalChest world
			String[] worlds = dataWorlds.split(",");		        
	        for (String world : worlds)
	        {
	        	world = world.trim();
	        	
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
		        	
		        	return true;
	        	}
	        }
		}

		return false;
	}

	private boolean checkPersonalChestRegion(Block block)
	{
		Location loc = block.getLocation();
		
		String dataRegions = plugin.pchestRegions;
		if(dataRegions != null)
		{
	        WorldGuardPlugin worldGuard = getWorldGuard();
	        Residence ResidencePlugin = getResidence();
	        
			//Check if the world is an PersonalChest world	
			String[] regions = dataRegions.split(",");
	        
	        for (String regionString : regions)
	        {
	        	regionString = regionString.trim();
	        	
    			if(plugin.debug)
    			{ 
    				log.info("[PersonalChest] Region Check");
    			}
    			
	        	String[] regionSplit = regionString.split("\\.");
	        	World world = plugin.getServer().getWorld(regionSplit[0]);
	        	String regionName = regionSplit[1];
	        	
	        	
	        	if(ResidencePlugin != null)
	        	{
					ClaimedResidence res = Residence.getResidenceManger().getByLoc(loc);
					
					if ( (res.getName().equalsIgnoreCase(regionName)) && (res.getWorld().equalsIgnoreCase(world.getName())) ) 
					{
	        			if(plugin.debug)
	        			{ 
	        				log.info("[PersonalChest] Region is defined");
	        			}
			        	return true;
					}
	        	}
	        	
	        	if(worldGuard != null)
	        	{
		        	// Check WorldGuard
		    		ProtectedRegion region = worldGuard.getRegionManager(world).getRegion(regionName);
		    		Vector v = new Vector(block.getX(), block.getY(), block.getZ());
		    		
					if (region.contains(v)) 
					{
		    			if(plugin.debug)
		    			{ 
		    				log.info("[PersonalChest] Region is defined");
		    			}
			        	return true;
					}
	        	}

	        }
		}
		
		return false;
	}
	
	public boolean load(Player player, Block block) {

		// Set the chest to opened
		setChestOpened(block, player);
		
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
		
		boolean complete = false;

		Chest chest = (Chest)block.getState();
		Inventory newInv = chest.getInventory();
		// First clear inventory befor loading file
		newInv.clear();
		
        chestContents = newInv.getContents();
		
		String playerName = player.getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
		
		File personalChestFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Players" + File.separator + playerName + File.separator + "Worlds" + File.separator + blockWorldName);
		personalChestFolder.mkdirs();
		
		File personalchestFile = new File(personalChestFolder , blockFilename + ".chest");
		
		if (!chestFile.exists())
		{			
			//Check if PersonalChest World
			if(checkPersonalChestWorld(block))
			{                
                // Check region inside a world then cancle else create
                if(checkPersonalChestRegion(block))
                {
        			if(plugin.debug)
        			{ 
        				log.info("[PersonalChest] No PersonalChest Region");
        			}
                }
                else
                {
                	create(chestContents, block);
                }
			}
			else if(checkPersonalChestRegion(block))//Check in PersonalChest Region
			{
				create(chestContents, block);				
			}
			else
        	{		    			
    			//Check if player file is there to delete.
    			personalchestFile.delete();
    			
    			// Chest isn't a PersonalChest
    			complete = true;
    			
    			return complete;
        	}
		
		}
		
		if(!checkDoubleChest(block))
		{
			
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
			
        	if(plugin.debug)
			{ 
				log.info("[PersonalChest] Load Single Chest");
			}

			complete = loadSingleChest(newInv, personalchestFile);
		}
		else
		{

			Block block2 = getDoubleChest(block);

			String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
			
			File personalchestFile2 = new File(personalChestFolder , blockFilename2 + ".chest");
			
			File chestFile2 = new File(worldDataFolder , blockFilename2 + ".chest");
			
			if (!personalchestFile.exists())
			{		
				try {
					copy(chestFile, personalchestFile);
					copy(chestFile2, personalchestFile2);
					if(plugin.debug)
					{ 
						log.info("[PersonalChest] Inventory copied "+ blockFilename+" & "+ blockFilename2+" "+playerName);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        	if(plugin.debug)
			{ 
				log.info("[PersonalChest] Load Double Chest");
			}
			complete = loadDoubleChest(newInv, personalchestFile, personalChestFolder, block);
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
	
	private void removePlayerChestFile(Block block)
	{
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
		
		// Delete Chest File From All Players
		File folder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Players");
		File[] listOfFolders = folder.listFiles();
		
		for (int i = 0; i < listOfFolders.length; i++) {
			File playerWorldDataFolder = new File(listOfFolders[i], "Worlds" + File.separator + blockWorldName);
			File playerChestFile = new File(playerWorldDataFolder , blockFilename + ".chest");
			
			if (playerChestFile.exists())
			{

				playerChestFile.delete();
	        	if(plugin.debug)
				{ 
					log.info("[PersonalChest] Deleted "+ listOfFolders[i] +" File: " + playerChestFile.getName());
				}
				
			}
		}
	}

	public boolean remove(Block block) {        
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
		
		// Add the original Items back in the chest
		Chest chest = (Chest) block.getState();
		Inventory newInv = chest.getInventory();	
		
		if(checkDoubleChest(block))
		{
        	if(plugin.debug)
			{ 
				log.info("[PersonalChest] Load Double Chest");
			}
        	loadDoubleChest(newInv, chestFile, worldDataFolder, block);
		}
		else
		{
        	if(plugin.debug)
			{ 
				log.info("[PersonalChest] Load Single Chest");
			}
			loadSingleChest(newInv, chestFile);		
		}
		
		if(chestFile.delete())
		{			
			
			removePlayerChestFile(block);
			
			// Remove Double Chest
			if(checkDoubleChest(block))
    		{
    			Block block2 = getDoubleChest(block);

    	    	String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
    			//String blockWorldName2 = block2.getWorld().getName();

				File chestFile2 = new File(worldDataFolder , blockFilename2 + ".chest");
				
				if(chestFile2.delete())
				{
					removePlayerChestFile(block2);
				}			
			
    		}			
			
			//Check if PersonalChest World
			if(checkPersonalChestWorld(block))
			{                
                // Check region inside a world then cancle else create
                if(checkPersonalChestRegion(block))
                {
        			if(plugin.debug)
        			{ 
        				log.info("[PersonalChest] No PersonalChest Region");
        			}
                }
                else
                {
                	File worldDataFolderRemoved = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "REMOVED");
	        		worldDataFolderRemoved.mkdirs();
	        		
	        		try {		        	
	        			File chestFileRemoved = new File(worldDataFolderRemoved , blockFilename + ".chest");
	        			
	        			chestFileRemoved.createNewFile();
	        			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFileRemoved));

	                	out.write("Removed");
	                	
	        			out.close();
	        			
			        	if(plugin.debug)
		    			{ 
		    				log.info("[PersonalChest] Removed Chest File Created");
		    			}
	        	
	        		} catch (IOException e) {
	        			e.printStackTrace();
	        		}
	        		
	        		if(checkDoubleChest(block))
	        		{
	        			Block block2 = getDoubleChest(block);

	        	    	String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
	        	    	
	        	    	try {
	        				File chestFileRemoved2 = new File(worldDataFolderRemoved , blockFilename2 + ".chest");
	        				
	        				chestFileRemoved2.createNewFile();
	        				
		        			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFileRemoved2));

		                	out.write("Removed");
		                	
		        			out.close();

				        	if(plugin.debug)
			    			{ 
			    				log.info("[PersonalChest] Removed Chest File 2 Created");
			    			}
				        	
	        			} catch (IOException e) {
	        				e.printStackTrace();
	        			}
	        		}
                }
			}
			else if(checkPersonalChestRegion(block))//Check in PersonalChest Region
			{
				File worldDataFolderRemoved = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "REMOVED");
        		worldDataFolderRemoved.mkdirs();
        		
        		try {		        	
        			File chestFileRemoved = new File(worldDataFolderRemoved , blockFilename + ".chest");
        			
        			chestFileRemoved.createNewFile();
        			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFileRemoved));

                	out.write("Removed");
                	
        			out.close();
        			
		        	if(plugin.debug)
	    			{ 
	    				log.info("[PersonalChest] Removed Chest File Created");
	    			}
        	
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        		
        		if(checkDoubleChest(block))
        		{
        			Block block2 = getDoubleChest(block);

        	    	String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
        	    	
        	    	try {
        				File chestFileRemoved2 = new File(worldDataFolderRemoved , blockFilename2 + ".chest");
        				
        				chestFileRemoved2.createNewFile();
        				
	        			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFileRemoved2));

	                	out.write("Removed");
	                	
	        			out.close();

			        	if(plugin.debug)
		    			{ 
		    				log.info("[PersonalChest] Removed Chest File 2 Created");
		    			}
			        	
        			} catch (IOException e) {
        				e.printStackTrace();
        			}
        		}				
			}			
			
			return true;
		}
		
		return false;
	}

	public boolean checkChestRemoved(Block block)
	{
		String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "REMOVED");
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
		
		if (chestFile.exists())
		{
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Chest is Removed from world"+ blockFilename);
			}
			return true;
		}
		
		return false;
	}
	
    public boolean checkChestStatus(Block block)
	{
		String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");		
		
		Chest chest = (Chest) block.getState();

		Inventory newInv = chest.getInventory();
    	
        chestContents = newInv.getContents();
	
        if (chestFile.exists())
		{
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Chest is Registerd "+ blockFilename);
			}
			
			
			//Check if PersonalChest World
			if(checkPersonalChestWorld(block))
			{       

    			if(plugin.debug)
    			{ 
    				log.info("[PersonalChest] No PersonalChest Region");
    			}
    			
                // Check region inside a world then cancle else create
                if(checkPersonalChestRegion(block))
                {
        			if(plugin.debug)
        			{ 
        				log.info("[PersonalChest] No PersonalChest Region");
        			}
        			
        			//Remove Chest
        			remove(block);
        			
        			return false;
                }
                else
                {
                	if(plugin.debug)
        			{ 
        				log.info("[PersonalChest] Chest is in PersonalChest World");
        			}
                	
        			return true;	
                }
			}
			else if(checkPersonalChestRegion(block))//Check in PersonalChest Region
			{
            	if(plugin.debug)
    			{ 
    				log.info("[PersonalChest] Chest is PersonalChest Region");
    			}
            	
				return true;			
			}
			else
			{	

	        	if(plugin.debug)
				{ 
					log.info("[PersonalChest] Chest is Only Registerd");
				}
	        	
				return true;	
			}
		}
		else if(checkChestRemoved(block))
		{
			return false;
		}
		else
		{			
			//Check if PersonalChest World
			if(checkPersonalChestWorld(block))
			{                
                // Check region inside a world then cancle else create
                if(checkPersonalChestRegion(block))
                {
        			if(plugin.debug)
        			{ 
        				log.info("[PersonalChest] No PersonalChest Region");
        			}
                }
                else
                {
                	if(create(chestContents, block))
	                {
	        			if(plugin.debug)
	        			{ 
	        				log.info("[PersonalChest] Chest Added to list because it is an PersonalChest World");
	        			}
	        			
		    			return true;
	                }
	                else
	                {
	        			if(plugin.debug)
	        			{ 
	        				log.info("[PersonalChest] Error occured while creating the new Chest");
	        			}
	                }
                }
			}
			else if(checkPersonalChestRegion(block))//Check in PersonalChest Region
			{
				if(create(chestContents, block))
                {
        			if(plugin.debug)
        			{ 
        				log.info("[PersonalChest] Chest Added to list because it is an PersonalChest Region");
        			}
        			
	    			return true;
                }
                else
                {
        			if(plugin.debug)
        			{ 
        				log.info("[PersonalChest] Error occured while creating the new Chest");
        			}
        			
        			return false;
                }				
			}
		}	

		if(plugin.debug)
		{ 
			log.info("[PersonalChest] Chest is not Registerd");
		}
		
		return false;
	}
	
	public String checkOtherChestPosition(Block block, Block block2) {
		if (block.getRelative(BlockFace.NORTH).getTypeId() == 54) {
			return "LEFT";
		} else if (block.getRelative(BlockFace.EAST).getTypeId() == 54) {
			return "RIGHT";
		} else if (block.getRelative(BlockFace.SOUTH).getTypeId() == 54) {
			return "RIGHT";
		} else if (block.getRelative(BlockFace.WEST).getTypeId() == 54) {
			return "LEFT";
		}
		return "LEFT";
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
	
	public Block getDoubleChest(Block block) {
		if (block.getRelative(BlockFace.NORTH).getTypeId() == 54) {
			return block.getRelative(BlockFace.NORTH);
		} else if (block.getRelative(BlockFace.EAST).getTypeId() == 54) {
			return block.getRelative(BlockFace.EAST);
		} else if (block.getRelative(BlockFace.SOUTH).getTypeId() == 54) {
			return block.getRelative(BlockFace.SOUTH);
		} else if (block.getRelative(BlockFace.WEST).getTypeId() == 54) {
			return block.getRelative(BlockFace.WEST);
		}
		return null;
	}

	public boolean checkChestOpened(Block block, Player actionPlayer) 
	{
	
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "OPEN");
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");

		int chestRadius = 5;
		
		if (chestFile.exists())
		{
			try {
				
				final BufferedReader in = new BufferedReader(new FileReader(chestFile));

				String line;
				line = in.readLine();

				Player playerInFile = plugin.getPlayerByString(line);
				
				if(playerInFile == actionPlayer)
				{
					if(plugin.debug)
					{ 
						log.info("[PersonalChest] " + line + " is the used player");
					}
					removeChestOpened(block);
					return false;
				}
				
				if(playerInFile!=null && playerInFile.isOnline())
				{
					if(plugin.debug)
					{ 
						log.info("[PersonalChest] " + line + " is Online.");
					}
					
					in.close();

					double chestRadiusxX = block.getX() + chestRadius;
					double chestRadiusXx = block.getX() - chestRadius;
					
					double chestRadiusyY = block.getY() + chestRadius;
					double chestRadiusYy = block.getY() - chestRadius;
					
					double chestRadiuszZ = block.getZ() + chestRadius;
					double chestRadiusZz = block.getZ() - chestRadius;
					
					if( chestRadiusxX > playerInFile.getLocation().getX() && playerInFile.getLocation().getX() > chestRadiusXx && chestRadiusyY > playerInFile.getLocation().getY() && playerInFile.getLocation().getY() > chestRadiusYy && chestRadiuszZ > playerInFile.getLocation().getZ() && playerInFile.getLocation().getZ() > chestRadiusZz )
					{
						if(plugin.debug)
						{ 
							log.info("[PersonalChest] " + line + " is in range.");
							log.info("[PersonalChest] X: "+ chestRadiusXx + " < " + playerInFile.getLocation().getX() + " > " +chestRadiusxX );
							log.info("[PersonalChest] Y: "+ chestRadiusYy + " < " + playerInFile.getLocation().getY() + " > " +chestRadiusyY );
							log.info("[PersonalChest] Z: "+ chestRadiusZz + " < " + playerInFile.getLocation().getZ() + " > " +chestRadiuszZ );
						}
						return true;
					}
					else
					{
						if(plugin.debug)
						{ 
							log.info("[PersonalChest] " + line + " is out of range.");
							log.info("[PersonalChest] X: "+ chestRadiusXx + " < " + playerInFile.getLocation().getX() + " > " +chestRadiusxX );
							log.info("[PersonalChest] Y: "+ chestRadiusYy + " < " + playerInFile.getLocation().getY() + " > " +chestRadiusyY );
							log.info("[PersonalChest] Z: "+ chestRadiusZz + " < " + playerInFile.getLocation().getZ() + " > " +chestRadiuszZ );
						}
						removeChestOpened(block);
						return false;
					}
					
				}
				else
				{
					if(plugin.debug)
					{ 
						log.info("[PersonalChest] " + line + " is Offline.");
					}
					in.close();
					removeChestOpened(block);
					return false;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return false;
	}
	
	public void setChestOpened(Block block, Player player) 
	{
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();

		String playerName = player.getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "OPEN");
		worldDataFolder.mkdirs();
		
		try {
			File chestFile = new File(worldDataFolder , blockFilename + ".chest");
			
			chestFile.createNewFile();
	
			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));

        	out.write(playerName);
        	
			out.close();
			

			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Chest OPENED File created");
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(checkDoubleChest(block))
		{
			Block block2 = getDoubleChest(block);

	    	String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
	    	
	    	try {
				File chestFile2 = new File(worldDataFolder , blockFilename2 + ".chest");
				
				chestFile2.createNewFile();
		
				final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile2));

	        	out.write(playerName);
	        	
				out.close();

				if(plugin.debug)
				{ 
					log.info("[PersonalChest] Chest OPENED File 2 created");
				}
		
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void removeChestOpened(Block block) {
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();
		
		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "OPEN");
		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
		
		if(chestFile.delete())
		{
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] OPEN File deleted");
			}
		}
		
		if(checkDoubleChest(block))
		{
			Block block2 = getDoubleChest(block);

	    	String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
	    	
			File chestFile2 = new File(worldDataFolder , blockFilename2 + ".chest");
			
			if(chestFile2.delete())
			{
				if(plugin.debug)
				{ 
					log.info("[PersonalChest] OPEN File 2 deleted");
				}
			}
		}
	}
	
	public boolean saveSingleChest(ItemStack[] chestContents, Block block, File dataFolder)
	{
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();

		// Delete remove file when created
    	File worldDataFolderRemoved = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "REMOVED");			        	
		File chestFileRemoved = new File(worldDataFolderRemoved , blockFilename + ".chest");
    	
		if (chestFileRemoved.exists())
		{
			chestFileRemoved.delete();		
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Removed File is deleted");
			}			
		}	
		
		try {
			final File chestFile = new File(dataFolder , blockFilename + ".chest");
			if (chestFile.exists())
				chestFile.delete();
			chestFile.createNewFile();

			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));

			for(int i =0;i<27;i++)
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
			
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public boolean createDoubleChest(Block block, File dataFolder)
	{
		Chest chest = (Chest) block.getState();
		Inventory inv = chest.getInventory();
		
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
		String blockWorldName = block.getWorld().getName();

		// Delete remove file when created
    	File worldDataFolderRemoved = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "REMOVED");			        	
		File chestFileRemoved = new File(worldDataFolderRemoved , blockFilename + ".chest");
    	
		if (chestFileRemoved.exists())
		{
			chestFileRemoved.delete();
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Removed File is deleted");
			}			
		}	    	
    	
		Block block2 = getDoubleChest(block);
		Chest chest2 = (Chest) block2.getState();
		Inventory inv2 = chest2.getInventory();
		
		String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
		String blockWorldName2 = block2.getWorld().getName();

		// Delete remove file when created
    	File worldDataFolderRemoved2 = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName2 + File.separator + "REMOVED");			        	
		File chestFileRemoved2 = new File(worldDataFolderRemoved2 , blockFilename2 + ".chest");
    	
		if (chestFileRemoved2.exists())
		{
			chestFileRemoved2.delete();	
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Removed File is deleted");
			}		
		}	
		
        ItemStack[] chestContents1 = inv.getContents();
        ItemStack[] chestContents2 = inv2.getContents();
        
		if(checkOtherChestPosition(block, block2) == "RIGHT")
		{
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Other Chest is on the RIGHT side");
			}
		}
		else
		{
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Other Chest is on the LEFT side");
			}
		}
		
		try {
			final File chestFile = new File(dataFolder , blockFilename + ".chest");
			if (chestFile.exists())
				chestFile.delete();
			chestFile.createNewFile();

			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));

			for(int i =0;i<27;i++)
	        {
		        if(chestContents1[i]!=null)
		        {
			        out.write(chestContents1[i].getTypeId() + ":" + chestContents1[i].getAmount() + ":" + chestContents1[i].getDurability() + "\r\n");
		        }
		        else
		        {
		        	out.write("0:0:0\r\n");
		        }
	        }
			out.close();
			
			// Save Chest 2
			final File chestFile2 = new File(dataFolder , blockFilename2 + ".chest");
			if (chestFile2.exists())
				chestFile2.delete();
			chestFile2.createNewFile();

			final BufferedWriter out2 = new BufferedWriter(new FileWriter(chestFile2));

			for(int i=0;i<27;i++)
	        {
		        if(chestContents2[i]!=null)
		        {
			        out2.write(chestContents2[i].getTypeId() + ":" + chestContents2[i].getAmount() + ":" + chestContents2[i].getDurability() + "\r\n");
		        }
		        else
		        {
		        	out2.write("0:0:0\r\n");
		        }
	        }
			out2.close();

			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Chest created!");
			}
			
			//Clear inventory
			inv.clear();
			inv2.clear();
			
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			
			return false;
		}
	}

	
	public boolean saveDoubleChest(ItemStack[] chestContents, Block block, File dataFolder)
	{
		Chest chest = (Chest) block.getState();
		Inventory inv = chest.getInventory();
		
    	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
    	
		Block block2 = getDoubleChest(block);
		Chest chest2 = (Chest) block2.getState();
		Inventory inv2 = chest2.getInventory();
		
		String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();

		int startPos1 = 0;
		int endPos1 = 27;
		int startPos2 = 27;
		int endPos2 = 54;
		
		if(checkOtherChestPosition(block, block2) == "RIGHT")
		{
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Other Chest is on the RIGHT side");
			}
			startPos1 = 27;
			endPos1 = 54;
			startPos2 = 0;
			endPos2 = 27;
		}
		else
		{
			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Other Chest is on the LEFT side");
			}
			
		}
		
		try {
			final File chestFile = new File(dataFolder , blockFilename + ".chest");
			if (chestFile.exists())
				chestFile.delete();
			chestFile.createNewFile();

			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));

			for(int i =startPos1;i<endPos1;i++)
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
			
			// Save Chest 2
			final File chestFile2 = new File(dataFolder , blockFilename2 + ".chest");
			if (chestFile2.exists())
				chestFile2.delete();
			chestFile2.createNewFile();

			final BufferedWriter out2 = new BufferedWriter(new FileWriter(chestFile2));

			for(int i=startPos2;i<endPos2;i++)
	        {
		        if(chestContents[i]!=null)
		        {
			        out2.write(chestContents[i].getTypeId() + ":" + chestContents[i].getAmount() + ":" + chestContents[i].getDurability() + "\r\n");
		        }
		        else
		        {
		        	out2.write("0:0:0\r\n");
		        }
	        }
			out2.close();

			if(plugin.debug)
			{ 
				log.info("[PersonalChest] Chest created!");
			}
			
			//Clear inventory
			inv.clear();
			inv2.clear();
			
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public boolean loadSingleChest(Inventory inv, File chestFile)
	{		
		try {
			
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
							inv.setItem(field, new ItemStack(type, amount, damage));
						}
					} catch (NumberFormatException e) {
						// ignore
					}
				}
				field++;
			}

			in.close();
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean loadDoubleChest(Inventory inv, File chestFile, File dataFolder, Block block)
	{
		Block block2 = getDoubleChest(block);

		String blockFilename = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
		
		File chestFile2 = new File(dataFolder , blockFilename + ".chest");
	

		Chest chest = (Chest) block2.getState();
		Inventory inv2 = chest.getInventory();
		
		inv.clear();
		inv2.clear();
		
		try {
			
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
							inv.setItem(field, new ItemStack(type, amount, damage));
						}
					} catch (NumberFormatException e) {
						// ignore
					}
				}
				field++;
			}

			in.close();
			
			final BufferedReader in2 = new BufferedReader(new FileReader(chestFile2));

			String line2;
			int field2 = 0;
			while ((line2 = in2.readLine()) != null) {
				if (line2 != "") {
					final String[] parts = line2.split(":");
					try {
						int type = Integer.parseInt(parts[0]);
						int amount = Integer.parseInt(parts[1]);
						short damage = Short.parseShort(parts[2]);
						if (type != 0) {
							inv2.setItem(field2, new ItemStack(type, amount, damage));
						}
					} catch (NumberFormatException e) {
						// ignore
					}
				}
				field2++;
			}

			in2.close();
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
