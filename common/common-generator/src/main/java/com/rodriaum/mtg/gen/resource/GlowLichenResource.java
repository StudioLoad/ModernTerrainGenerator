package com.rodriaum.mtg.gen.resource;

import com.rodriaum.mtg.config.biome.BiomeResourceBase;
import com.rodriaum.mtg.constants.Constants;
import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.util.materials.LocalMaterialBase;
import com.rodriaum.mtg.util.materials.LocalMaterialData;
import com.rodriaum.mtg.util.materials.LocalMaterialTag;

import java.util.ArrayList;
import java.util.List;

public class GlowLichenResource extends BiomeResourceBase
{
	public int minAltitude;
	public int maxAltitude;
	public int countMin;
	public int countMax;
	public int nearbyAttempts;
	public boolean canPlaceOnFloor;
	public boolean canPlaceOnCeiling;
	public boolean canPlaceOnWall;
	public float chanceOfSpreading;
	public List<LocalMaterialBase> canBePlacedOn;
	public GlowLichenResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		nearbyAttempts = readInt(args.get(0), 0, 1000);
		canPlaceOnFloor = args.get(1).equalsIgnoreCase("true");
		canPlaceOnCeiling = args.get(2).equalsIgnoreCase("true");
		canPlaceOnWall = args.get(3).equalsIgnoreCase("true");
		chanceOfSpreading = (float) readDouble(args.get(4), 0.0, 1.0);
		minAltitude = readInt(args.get(5), Constants.MIN_POSSIBLE_Y, Constants.MAX_POSSIBLE_Y);
		maxAltitude = readInt(args.get(6), minAltitude, Constants.MAX_POSSIBLE_Y);
		countMin = readInt(args.get(7), 0, 1000);
		countMax = readInt(args.get(8), 0, 1000);

		canBePlacedOn = new ArrayList<>();
		for (int i = 9; i < args.size(); i++)
		{
			String input = args.get(i);
			LocalMaterialTag tag = materialReader.readTag(input);
			if(tag != null)
			{
				canBePlacedOn.add(tag);
			} else {
				LocalMaterialData material = materialReader.readMaterial(input);
				if(material == null)
				{
					throw new InvalidConfigException("Invalid entry for glow lichen source block, material \"" + input + "\" could not be found.");
				}
				canBePlacedOn.add(material);
			}
		}
		if (canBePlacedOn.isEmpty()) canBePlacedOn.add(materialReader.readTag("mtg:stone"));
	}

	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder("GlowLichen(" + nearbyAttempts + "," + canPlaceOnWall + "," + canPlaceOnCeiling + "," + canPlaceOnWall+ "," + chanceOfSpreading);
		s.append(",").append(minAltitude).append(",").append(maxAltitude).append(",").append(countMin).append(",").append(countMax);
		for (LocalMaterialBase localMaterialBase : canBePlacedOn)
		{
			s.append(",");
			s.append(localMaterialBase.toString());
		}
		s.append(")");
		return s.toString();
	}
}
