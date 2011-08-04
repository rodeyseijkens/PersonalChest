package nl.rodey.personalchest;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.TileEntityChest;

public class pchest extends TileEntityChest {

	@Override
	public boolean a_(EntityHuman entityhuman) {
		return true;
	}
}
