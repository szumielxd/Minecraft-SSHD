package com.ryanmichela.sshd.bungee;

import com.ryanmichela.sshd.common.TaskManager;

public class BungeeTaskManager implements TaskManager {
	
	
	private final SshdPluginBungee plugin;
	
	
	public BungeeTaskManager(SshdPluginBungee plugin) {
		this.plugin = plugin;
	}
	

	@Override
	public void runAsync(Runnable task) {
		this.plugin.getProxy().getScheduler().runAsync(plugin, task);
	}
	@Override
	public void runSync(Runnable task) {
		this.plugin.getProxy().getScheduler().runAsync(plugin, task);
	}

}
