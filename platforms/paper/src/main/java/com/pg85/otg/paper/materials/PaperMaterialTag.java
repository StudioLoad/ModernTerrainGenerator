package com.pg85.otg.paper.materials;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.materials.LocalMaterialTag;

import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class PaperMaterialTag extends LocalMaterialTag
{
	public static LocalMaterialTag ofString(String name)
	{
		// If otg: or no domain was supplied, try OTG tags.
		// If no domain was supplied, first try OTG tags.
		if(!name.contains(":") || name.startsWith(Constants.MOD_ID_SHORT + ":"))
		{
			Block[] blockTag = PaperMaterials.OTG_BLOCK_TAGS.get(name.trim().toLowerCase().replace(Constants.MOD_ID_SHORT + ":", ""));
			if(blockTag != null)
			{
				return new PaperMaterialTag(blockTag, Constants.MOD_ID_SHORT + ":" + name.trim().toLowerCase().replace(Constants.MOD_ID_SHORT + ":", ""));
			}
		}
		final Identifier resourceLocation;
		try
		{
			resourceLocation = Identifier.parse(name.trim().toLowerCase());
		} catch(Exception ex) {
			return null;
		}
		TagKey<Block> blockTag = getBlockTagFromResourceLocation(resourceLocation);
		return blockTag == null ? null : new PaperMaterialTag(blockTag, resourceLocation.toString());
	}
	
	private final String name;
	private final TagKey<Block> blockTag;
	private final Block[] otgBlockTag;

	private PaperMaterialTag(TagKey<Block> blockTag, String name)
	{
		this.otgBlockTag = null;
		this.blockTag = blockTag;
		this.name = name;
	}
	
	private PaperMaterialTag(Block[] otgBlockTag, String name)
	{
		this.otgBlockTag = otgBlockTag;
		this.blockTag = null;
		this.name = name;
	}

	public boolean isOTGTag(Block block)
	{
		if(this.otgBlockTag != null)
		{
			for(Block otgTagBlock : this.otgBlockTag)
			{
				if(otgTagBlock == block)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public TagKey<Block> getTag()
	{
		return this.blockTag;
	}

	public Block[] getOtgBlockTag()
	{
		if (otgBlockTag != null)
		{
			return otgBlockTag.clone();
		}
		return null;
	}

	@Override
	public String toString()
	{
		// TODO: Fetch the registry name from the tag object.
		return this.name;
	}

	// Adding this because I can't find a method to do lookup by name. Really weird that this doesn't exist.
	// It's scuffed but it's the best we got for now
	// -auth
	private static TagKey<Block> getBlockTagFromResourceLocation(Identifier loc)
	{
		// Tag identifiers are data-driven. Creating a TagKey is sufficient; the server
		// binds it to the actual tag contents when the tag registry is loaded.
		return TagKey.create(Registries.BLOCK, loc);
	}
}