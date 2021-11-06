package com.ryanmichela.sshd.bukkit;

import com.ryanmichela.sshd.common.TaskManager;

public class BukkitTaskManager implements TaskManager {
	
	
	private final SshdPluginBukkit plugin;
	
	
	public BukkitTaskManager(SshdPluginBukkit plugin) {
		this.plugin = plugin;
	}
	

	@Override
	public void runAsync(Runnable task) {
		this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
	}
	
	@Override
	public void runSync(Runnable task) {
		this.plugin.getServer().getScheduler().runTask(plugin, task);
	}

}
