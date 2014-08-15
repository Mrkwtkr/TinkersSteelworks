package tsteelworks.lib;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import nf.fr.ephys.cookiecore.helpers.RegistryHelper;
import tsteelworks.common.core.TSLogger;

import java.util.HashMap;

public class DeepTankGlassTypes {
	private static HashMap<GlassType, Integer> glassTypes = new HashMap<>();

	public static Integer getBlockCapacity(GlassType glass) {
		return glassTypes.get(glass);
	}

	public static void addGlassType(ItemStack stack, int capacity) {
		GlassType glass = new GlassType(stack);
		glassTypes.put(new GlassType(stack), capacity);

		// this should prevent out of bounds exceptions when getting the display name
		if (glass.metadata == null) {
			stack.setItemDamage(0);
		}

		TSLogger.info("Registered deep tank glass type " + stack.getDisplayName() + " with capacity of " + capacity + "mB");
	}

	/**
	 * Adds a glass type using a string following this format:
	 * modName:blockName@metadata|capacity
	 *
	 * @param data the data to parse
	 */
	public static void parseGlassType(String data) {
		// you know what would be great java ? lua-like assignation. like: blockName, capacity = data.split("|");
		String[] splitData = data.split("\\|");

		if (splitData.length != 2) {
			TSLogger.warning("Parsing deep tank glass " + data + ". INVALID FORMAT");

			return;
		}

		int capacity;
		try {
			capacity = Integer.parseInt(splitData[1]);
		} catch(NumberFormatException e) {
			TSLogger.warning("Parsing deep tank glass " + data + ". INVALID FORMAT: capacity is not an integer");
			return;
		}

		ItemStack stack = RegistryHelper.getItemStack(splitData[0]);

		if (stack == null) {
			TSLogger.warning("Parsing deep tank glass " + data + ". INVALID FORMAT: block does not exist");

			return;
		}

		addGlassType(stack, capacity);
	}

	public static class GlassType {
		private Integer metadata;
		private Block block;

		public GlassType(Block block, int metadata) {
			this.block = block;

			this.metadata = metadata > 15 ? null : metadata;
		}

		public GlassType(ItemStack item) {
			this.block = Block.getBlockFromItem(item.getItem());

			this.metadata = item.getItemDamage() > 15 ? null : item.getItemDamage();
		}

		public GlassType(GlassType glass) {
			this.metadata = glass.metadata;
			this.block = glass.block;
		}

		@Override
		public String toString() {
			return block.getLocalizedName() + '#' + (metadata == null ? '*' : metadata);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			GlassType glassType = (GlassType) o;

			if (!block.equals(glassType.block))
				return false;

			if (metadata != null && glassType.metadata != null && !metadata.equals(glassType.metadata)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return block.hashCode();
		}

		public void setMetadata(Integer metadata) {
			this.metadata = metadata;
		}

		public void setBlock(Block block) {
			this.block = block;
		}
	}
}