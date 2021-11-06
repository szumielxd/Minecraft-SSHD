package com.ryanmichela.sshd.bungee.implementation;

import com.ryanmichela.sshd.common.ConsoleLogFormatter;
import com.ryanmichela.sshd.common.ConsoleShellFactory;
import com.ryanmichela.sshd.common.SshdPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.CommandSender;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class SSHDBungeeSender implements CommandSender {
	
	private ConsoleShellFactory.ConsoleShell console;
	@SuppressWarnings("unused")
	private final SshdPlugin plugin;
	
	
	public SSHDBungeeSender(SshdPlugin plugin, ConsoleShellFactory.ConsoleShell console) {
		this.plugin = plugin;
		this.console = console;
	}

	@Override
	public void sendMessage(String message) {
		this.sendRawMessage(message + "\r");
	}

	public void sendRawMessage(String message) {
		if (this.console.consoleReader == null) return;
		
		this.console.consoleReader.printAbove(ConsoleLogFormatter.colorizeString(message).replace("\n", "\n\r"));
		return;
	}

	@Override
	public void sendMessages(String... messages) {
		Arrays.asList(messages).forEach(this::sendMessage);
	}

	@Override
	public void sendMessage(BaseComponent... message) {
		sendMessage(BaseComponent.toLegacyText(message));
	}

	@Override
	public void sendMessage(BaseComponent message) {
		sendMessage(message.toLegacyText());
	}

	@Override
	public String getName() {
		return "SSHD CONSOLE";
	}

	@Override
	public Collection<String> getGroups() {
		return Collections.emptySet();
	}

	@Override
	public void addGroups(String... groups) {
		throw new UnsupportedOperationException("Console may not have groups");
	}

	@Override
	public void removeGroups(String... groups) {
		throw new UnsupportedOperationException("Console may not have groups");
	}

	@Override
	public boolean hasPermission(String permission) {
		return true;
	}

	@Override
	public void setPermission(String permission, boolean value) {
		throw new UnsupportedOperationException("Console has all permissions");
	}

	@Override
	public Collection<String> getPermissions() {
		return Collections.emptySet();
	}
}