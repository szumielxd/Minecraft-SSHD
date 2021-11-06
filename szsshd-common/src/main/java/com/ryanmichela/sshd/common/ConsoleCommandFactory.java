package com.ryanmichela.sshd.common;

import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyright 2013 Ryan Michela
 */
public class ConsoleCommandFactory implements CommandFactory {
	
	
	private final SshdPlugin plugin;
	public ConsoleCommandFactory(SshdPlugin plugin) {
		this.plugin = plugin;
	}
	
	

    @Override
    public Command createCommand(ChannelSession cs, String command) {
        return new ConsoleCommand(command);
    }

    public class ConsoleCommand implements Command {

        private String command;

        @SuppressWarnings("unused")
		private InputStream in;
        @SuppressWarnings("unused")
		private OutputStream out;
        @SuppressWarnings("unused")
		private OutputStream err;
        private ExitCallback callback;

        public ConsoleCommand(String command) {
            this.command = command;
        }

        public void setInputStream(InputStream in) {
            this.in = in;
        }

        public void setOutputStream(OutputStream out) {
            this.out = out;
        }

        public void setErrorStream(OutputStream err) {
            this.err = err;
        }

        public void setExitCallback(ExitCallback callback) {
            this.callback = callback;
        }

        @Override
        public void start(ChannelSession cs, Environment environment) throws IOException {
            try {
                plugin.getLogger().info("[U: " + environment.getEnv().get(Environment.ENV_USER) + "] " + command);

                if (!plugin.dispatchCommand(plugin.getConsoleSender(), command)) {
                	plugin.getConsoleSender().sendMessage(new ComponentBuilder("Command not found").color(ChatColor.RED).create());
                }
            } 
            catch (Exception e) {
                plugin.getLogger().severe("Error processing command from SSH -" + e.getMessage());
            } finally {
                callback.onExit(0);
            }
        }

        @Override
        public void destroy(ChannelSession cn) {}
	}
}
