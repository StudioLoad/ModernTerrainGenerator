package com.rodriaum.mtg.gen.resource;

import com.rodriaum.mtg.constants.Constants;
import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;

import java.util.List;
import java.util.Random;

public class DungeonResource extends FrequencyResourceBase
{
	private final int maxAltitude;
	private final int minAltitude;

	public DungeonResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(3, args);

		this.frequency = 1;
		this.rarity = readDouble(args.get(0), 1, Integer.MAX_VALUE);
		this.minAltitude = readInt(args.get(1), Constants.MIN_POSSIBLE_Y, Constants.MAX_POSSIBLE_Y);
		this.maxAltitude = readInt(args.get(2), minAltitude, Constants.MAX_POSSIBLE_Y);
	}

	@Override
	public String toString()
	{
		return "Dungeon(" + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, int x, int z)
	{
		int y = getValidYInRange(random, this.minAltitude, this.maxAltitude, world);
		world.placeDungeon(random, world.getDecorationArea().getChunkBeingDecoratedCenterX(), y, world.getDecorationArea().getChunkBeingDecoratedCenterZ());		
	}	
}
