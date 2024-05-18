package com.ryanmichela.sshd.bukkit.implementation;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ManuallyAbandonedConversationCanceller;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import com.ryanmichela.sshd.common.ConsoleLogFormatter;
import com.ryanmichela.sshd.common.ConsoleShellFactory;
import com.ryanmichela.sshd.common.SshdPlugin;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class SSHDBukkitSender implements ConsoleCommandSender, CommandSender {
	
	
	private final PermissibleBase perm = new PermissibleBase(this);
	private final SSHDConversationTracker conversationTracker = new SSHDConversationTracker();
	// Set by the upstream allocating function
	public final ConsoleShellFactory.ConsoleShell console;
	@SuppressWarnings("unused")
	private final SshdPlugin plugin;
	
	public SSHDBukkitSender(SshdPlugin plugin, ConsoleShellFactory.ConsoleShell console) {
		this.plugin = plugin;
		this.console = console;
	}

	public void sendMessage(String message) {
		this.sendRawMessage(message + "\r");
	}

	public void sendRawMessage(String message) {
		if (this.console.consoleReader != null) {
			this.console.consoleReader.printAbove(ConsoleLogFormatter.colorizeString(message).replace("\n", "\n\r"));
		}
	}

	public void sendMessage(String... messages) {
		Arrays.asList(messages).forEach(this::sendMessage);
	}

	public String getName() {
		return "SSHD Console";
	}

	public boolean isOp() {
		return true;
	}

	public void setOp(boolean value) {
		throw new UnsupportedOperationException("Cannot change operator status of server console");
	}

	public boolean beginConversation(Conversation conversation) {
		return this.conversationTracker.beginConversation(conversation);
	}

	public void abandonConversation(Conversation conversation) {
		this.conversationTracker.abandonConversation(conversation, new ConversationAbandonedEvent(conversation, new ManuallyAbandonedConversationCanceller()));
	}

	public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
		this.conversationTracker.abandonConversation(conversation, details);
	}

	public void acceptConversationInput(String input) {
		this.conversationTracker.acceptConversationInput(input);
	}

	public boolean isConversing() {
		return this.conversationTracker.isConversing();
	}

	public boolean isPermissionSet(String name) {
		return this.perm.isPermissionSet(name);
	}

	public boolean isPermissionSet(Permission perm) {
		return this.perm.isPermissionSet(perm);
	}

	public boolean hasPermission(String name) {
		return this.perm.hasPermission(name);
	}

	public boolean hasPermission(Permission perm) {
		return this.perm.hasPermission(perm);
	}

	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
		return this.perm.addAttachment(plugin, name, value);
	}

	public PermissionAttachment addAttachment(Plugin plugin) {
		return this.perm.addAttachment(plugin);
	}

	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
		return this.perm.addAttachment(plugin, name, value, ticks);
	}

	public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
		return this.perm.addAttachment(plugin, ticks);
	}

	public void removeAttachment(PermissionAttachment attachment) {
		this.perm.removeAttachment(attachment);
	}

	public void recalculatePermissions() {
		this.perm.recalculatePermissions();
	}

	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return this.perm.getEffectivePermissions();
	}

	public boolean isPlayer() {
		return false;
	}

	public Server getServer() {
		return Bukkit.getServer();
	}

	public CommandSender.Spigot spigot(){
		return null;
	}

	@Override
	public void sendRawMessage(UUID sender, String message) {
		this.sendRawMessage(message);
	}

	@Override
	public void sendMessage(UUID sender, String message) {
		this.sendMessage(message);
	}

	@Override
	public void sendMessage(UUID sender, String... messages) {
		this.sendMessage(messages);
	}
}