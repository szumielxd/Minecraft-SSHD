package com.ryanmichela.sshd.bukkit.objects;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import com.ryanmichela.sshd.common.objects.CommonCommand;

import lombok.NonNull;

public class BukkitCommandWrapper implements TabExecutor {

	
	private final CommonCommand command;
	
	
	public BukkitCommandWrapper(CommonCommand command) {
		this.command = command;
	}
	
	public String getName() {
		return this.command.getName();
	}
	
	public String[] getAliases() {
		return this.command.getAliases();
	}
	
	public String getPermission() {
		return this.command.getPermission();
	}
	
	public String getPermissionMessage() {
		return this.command.getPermissionMessage();
	}

	@Override
	public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
		return this.command.onTabComplete(new BukkitSender(sender), args);
	}

	@Override
	public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
		return this.command.onCommand(new BukkitSender(sender), args, label);
	}
	

}
