package com.ryanmichela.sshd.bungee.objects;

import com.ryanmichela.sshd.common.objects.CommonSender;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeSender implements CommonSender {
	
	
	private final CommandSender sender;
	
	
	public BungeeSender(CommandSender sender) {
		this.sender = sender;
	}


	@Override
	public void sendMessage(BaseComponent message) {
		this.sender.sendMessage(message);
	}

	@Override
	public void sendMessage(BaseComponent... message) {
		this.sender.sendMessage(message);
	}
	
	@Override
	public void sendMessage(String message) {
		this.sender.sendMessage(TextComponent.fromLegacyText(message));
	}
	
	@Override
	public boolean hasPermission(String permission) {
		return this.sender.hasPermission(permission);
	}
	
	@Override
	public String getName() {
		return this.sender.getName();
	}
	
	public CommandSender getSender() {
		return this.sender;
	}
	

}
