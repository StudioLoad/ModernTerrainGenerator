package com.rodriaum.mtg.paper.networking;

import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.rodriaum.mtg.constants.Constants;
import com.rodriaum.mtg.core.OTG;
import com.rodriaum.mtg.core.presets.Preset;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.paper.MTGPlugin;
import com.rodriaum.mtg.paper.gen.OTGNoiseChunkGenerator;
import com.rodriaum.mtg.util.logging.LogCategory;
import com.rodriaum.mtg.util.logging.LogLevel;

import io.netty.buffer.Unpooled;
import net.minecraft.server.level.ServerLevel;

public class NetworkingListener implements Listener
{
	private final MTGPlugin plugin;

	public NetworkingListener(MTGPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		sendDataPacket(event.getPlayer());
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event)
	{
		sendDataPacket(event.getPlayer());
	}

	private void sendDataPacket(Player player)
	{
		ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();

		if (!(world.getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			return;
		}

		Preset preset = ((OTGNoiseChunkGenerator) world.getChunkSource().getGenerator()).getPreset();

		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(32766));
		String presetName = preset.getFolderName().toLowerCase();
		
		buffer.writeByte((byte) 1);
		buffer.writeInt(preset.getAllBiomeConfigs().size());
		buffer.writeUtf(presetName);

		for (IBiomeConfig biome : preset.getAllBiomeConfigs())
		{
			String key = biome.getRegistryKey().toResourceLocationString();
			BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key);

			if (wrapper != null)
			{
				buffer.writeUtf(key.replace(Constants.MOD_ID_SHORT + ":" + presetName + ".", ""));
				wrapper.encode(buffer);
			}
		}

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN,
					"Sending sync data of size " + buffer.writerIndex() + " bytes to " + player.getName() + ".");
			player.sendPluginMessage(plugin, Constants.MOD_ID_SHORT + ":spigot", buffer.array());
		}, 100); // 100 ticks is arbitrary, but 20 wasn't long enough
		// TODO is there a different event we can use to send this, where we don't need a delay
	}
}
