package com.ryanmichela.sshd.bungee;

import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.apache.sshd.server.SshServer;

import com.ryanmichela.sshd.bungee.implementation.SSHDBungeeSender;
import com.ryanmichela.sshd.bungee.objects.BungeeCommandWrapper;
import com.ryanmichela.sshd.bungee.objects.BungeeSender;
import com.ryanmichela.sshd.common.ConsoleShellFactory.ConsoleShell;
import com.ryanmichela.sshd.common.MkpasswdCommand;
import com.ryanmichela.sshd.common.SshdPlugin;
import com.ryanmichela.sshd.common.StreamHandlerAppender;
import com.ryanmichela.sshd.common.TaskManager;
import com.ryanmichela.sshd.common.objects.CommonSender;

import lombok.NonNull;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Copyright 2013 Ryan Michela
 */
public final class SshdPluginBungee extends Plugin implements SshdPlugin {
	private SshServer sshd;
	private static SshdPluginBungee instance;
	private Configuration configuration;
	private BungeeTaskManager taskMgr;

	@Override
	public void onLoad() {
		SshdPlugin.super.onLoad();
	}

	@Override
	public void onEnable() {
		instance = this;
		SshdPlugin.super.onEnable();
		this.taskMgr = new BungeeTaskManager(this);
		getProxy().getPluginManager().registerCommand(this, new BungeeCommandWrapper(new MkpasswdCommand()));
	}

	@Override
	public void onDisable() {
		SshdPlugin.super.onDisable();
	}

	public static SshdPlugin getInstance() {
		return instance;
	}

	@Override
	public Configuration setConfiguration(Configuration config) {
		return this.configuration = config;
	}

	@Override
	public Configuration getConfiguration() {
		return this.configuration;
	}

	@Override
	public SshServer setSshServer(SshServer server) {
		return this.sshd = server;
	}

	@Override
	public SshServer getSshServer() {
		return this.sshd;
	}

	public Logger getMainLogger() {
		return this.getProxy().getLogger();
	}

	public TaskManager getTaskManager() {
		return this.taskMgr;
	}

	public CommonSender getConsoleSender() {
		return new BungeeSender(this.getProxy().getConsole());
	}

	public boolean dispatchCommand(@NonNull CommonSender sender, String command) {
		return this.getProxy().getPluginManager().dispatchCommand(((BungeeSender) sender).getSender(), command);
	}
	
	public List<String> tabComplete(CommonSender sender, String cmdLine) {
		ArrayList<String> list = new ArrayList<>();
		if (cmdLine.indexOf(32) == -1) {
			for (Entry<String, Command> cmd : this.getProxy().getPluginManager().getCommands()) {
				if (cmd.getKey().toLowerCase().startsWith(cmdLine)) {
					String permission = cmd.getValue().getPermission();
					if (permission == null || permission.isEmpty() || sender.hasPermission(permission)) list.add(cmd.getKey());
				}
			}
			return list;
		} else {
			this.getProxy().getPluginManager().dispatchCommand(((BungeeSender) sender).getSender(), cmdLine, list);
		}
		return list;
	}

	@Override
	public CommonSender constructSSHDSender(ConsoleShell console) {
		return new BungeeSender(new SSHDBungeeSender(this, console));
	}

	@Override
	public void addHandler(StreamHandlerAppender handler) {
		this.getProxy().getLogger().addHandler(handler.getStreamHandler());
	}

	@Override
	public void removeHandler(StreamHandlerAppender handler) {
		this.getProxy().getLogger().removeHandler(handler.getStreamHandler());
	}

	@Override
	public String getServerName() {
		return this.getProxy().getName();
	}
}
