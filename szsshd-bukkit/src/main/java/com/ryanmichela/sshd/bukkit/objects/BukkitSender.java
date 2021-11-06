package com.ryanmichela.sshd.bukkit.objects;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ryanmichela.sshd.common.objects.CommonSender;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class BukkitSender implements CommonSender {
	
	
	private final CommandSender sender;
	
	
	public BukkitSender(CommandSender sender) {
		this.sender = sender;
	}
	
	@Override
	public void sendMessage(BaseComponent message) {
		if (this.sender instanceof Player) ((Player) this.sender).spigot().sendMessage(message);
		else this.sender.sendMessage(TextComponent.toLegacyText(message));
	}

	@Override
	public void sendMessage(BaseComponent... message) {
		if (this.sender instanceof Player) ((Player) this.sender).spigot().sendMessage(message);
		else this.sender.sendMessage(TextComponent.toLegacyText(message));
	}
	
	@Override
	public void sendMessage(String message) {
		this.sender.sendMessage(message);
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
