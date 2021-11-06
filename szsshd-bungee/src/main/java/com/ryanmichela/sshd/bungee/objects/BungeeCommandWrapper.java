package com.ryanmichela.sshd.bungee.objects;

import com.ryanmichela.sshd.common.objects.CommonCommand;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommandWrapper extends Command implements TabExecutor {

	
	private final CommonCommand command;
	
	
	public BungeeCommandWrapper(CommonCommand command) {
		super(command.getName(), command.getPermission(), command.getAliases());
		this.command = command;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		this.command.onCommand(new BungeeSender(sender), args, getName());
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		return this.command.onTabComplete(new BungeeSender(sender), args);
	}
	

}
