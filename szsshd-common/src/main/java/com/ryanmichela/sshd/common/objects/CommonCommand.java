package com.ryanmichela.sshd.common.objects;

import java.util.List;

public abstract class CommonCommand {
	
	
	private final String name;
	private final String permission;
	private final String[] aliases;
	
	
	public CommonCommand(String name) {
		this(name, null);
	}
	
	public CommonCommand(String name, String permission, String... aliases) {
		this.name = name;
		this.permission = permission;
		this.aliases = aliases;
	}
	
	
	
	public String[] getAliases() {
		return this.aliases;
	}
	public String getName() {
		return this.name;
	}
	public String getPermission() {
		return this.permission;
	}
	public boolean hasPermission(CommonSender sender) {
		return this.permission == null || sender.hasPermission(this.permission);
	}
	public abstract String getPermissionMessage();
	public abstract List<String> onTabComplete(CommonSender sender, String[] args);
	public abstract boolean onCommand(CommonSender sender, String[] args, String label);

}
