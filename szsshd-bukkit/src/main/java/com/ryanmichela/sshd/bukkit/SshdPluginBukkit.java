package com.ryanmichela.sshd.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.sshd.server.SshServer;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.ryanmichela.sshd.bukkit.implementation.SSHDBukkitSender;
import com.ryanmichela.sshd.bukkit.objects.BukkitCommandWrapper;
import com.ryanmichela.sshd.bukkit.objects.BukkitSender;
import com.ryanmichela.sshd.common.ConsoleShellFactory.ConsoleShell;
import com.ryanmichela.sshd.common.MkpasswdCommand;
import com.ryanmichela.sshd.common.SshdPlugin;
import com.ryanmichela.sshd.common.StreamHandlerAppender;
import com.ryanmichela.sshd.common.TaskManager;
import com.ryanmichela.sshd.common.objects.CommonSender;

import lombok.NonNull;
import net.md_5.bungee.config.Configuration;

/**
 * Copyright 2013 Ryan Michela
 */
public final class SshdPluginBukkit extends JavaPlugin implements SshdPlugin {
	private SshServer sshd;
	private static SshdPluginBukkit instance;
	private Configuration configuration;
	private BukkitTaskManager taskMgr;

	@Override
	public void onLoad() {
		SshdPlugin.super.onLoad();
	}

	@Override
	public void onEnable() {
		instance = this;
		SshdPlugin.super.onEnable();
		this.taskMgr = new BukkitTaskManager(this);
		BukkitCommandWrapper wrapper = new BukkitCommandWrapper(new MkpasswdCommand());
		PluginCommand command = this.getPluginCommand(wrapper.getName());
		command.setAliases(Arrays.asList(wrapper.getAliases()));
		command.setExecutor(wrapper);
		command.setPermission(wrapper.getPermission());
		command.setPermissionMessage(wrapper.getPermissionMessage());
		this.getCommandMap().register(this.getName(), command);
	}
	
	private PluginCommand getPluginCommand(String name) {
		Constructor<PluginCommand> constr;
		try {
			constr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constr.setAccessible(true);
			return constr.newInstance(name, this);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private CommandMap getCommandMap() {
		try {
			Field f = this.getServer().getClass().getDeclaredField("commandMap");
			f.setAccessible(true);
			return (CommandMap) f.get(this.getServer());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
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
	
	@Override
	public Logger getMainLogger() {
		return this.getServer().getLogger();
	}
	
	@Override
	public TaskManager getTaskManager() {
		return this.taskMgr;
	}
	
	@Override
	public CommonSender getConsoleSender() {
		return new BukkitSender(this.getServer().getConsoleSender());
	}
	
	@Override
	public boolean dispatchCommand(@NonNull CommonSender sender, String command) {
		this.getServer().dispatchCommand(((BukkitSender) sender).getSender(), command);
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommonSender sender, String cmdLine) {
		return this.getCommandMap().tabComplete(((BukkitSender) sender).getSender(), cmdLine);
	}

	@Override
	public CommonSender constructSSHDSender(ConsoleShell console) {
		return new BukkitSender(new SSHDBukkitSender(this, console));
	}

	@Override
	public void addHandler(StreamHandlerAppender handler) {
		((org.apache.logging.log4j.core.Logger)LogManager.getRootLogger()).addAppender(handler);
		handler.start();
	}

	@Override
	public void removeHandler(StreamHandlerAppender handler) {
		((org.apache.logging.log4j.core.Logger)LogManager.getRootLogger()).removeAppender(handler);
		handler.stop();
	}

	@Override
	public String getServerName() {
		return this.getServer().getName();
	}
}
