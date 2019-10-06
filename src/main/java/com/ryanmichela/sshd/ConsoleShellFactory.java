package com.ryanmichela.sshd;

import com.ryanmichela.sshd.ConsoleCommandCompleter;
import com.ryanmichela.sshd.ConsoleLogFormatter;
import com.ryanmichela.sshd.FlushyOutputStream;
import com.ryanmichela.sshd.FlushyStreamHandler;
import com.ryanmichela.sshd.SshTerminal;
import com.ryanmichela.sshd.SshdPlugin;
import com.ryanmichela.sshd.StreamHandlerAppender;
import com.ryanmichela.sshd.implementations.SSHDCommandSender;
import com.ryanmichela.sshd.ConsoleLogFormatter;
import jline.console.ConsoleReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.shell.ShellFactory;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.StreamHandler;

public class ConsoleShellFactory implements ShellFactory {

	public Command createShell(ChannelSession cs) {
		return new ConsoleShell();
	}

	public class ConsoleShell implements Command, Runnable {

		private InputStream in;
		private OutputStream out;
		private OutputStream err;
		private ExitCallback callback;
		private Environment environment;
		private Thread thread;
		private String Username;

		StreamHandlerAppender streamHandlerAppender;
		public ConsoleReader ConsoleReader;
		public SSHDCommandSender SshdCommandSender;

		public InputStream getIn() {
			return in;
		}

		public OutputStream getOut() {
			return out;
		}

		public OutputStream getErr() {
			return err;
		}

		public Environment getEnvironment() {
			return environment;
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
		public void start(ChannelSession cs, Environment env) throws IOException
		{
			try
			{
				this.ConsoleReader = new ConsoleReader(in, new FlushyOutputStream(out), new SshTerminal());
				this.ConsoleReader.setExpandEvents(true);
				this.ConsoleReader.addCompleter(new ConsoleCommandCompleter());

				StreamHandler streamHandler = new FlushyStreamHandler(out, new ConsoleLogFormatter(), this.ConsoleReader);
				this.streamHandlerAppender		  = new StreamHandlerAppender(streamHandler);

				((Logger)LogManager.getRootLogger()).addAppender(this.streamHandlerAppender);

				this.environment = env;
				this.Username = env.getEnv().get(Environment.ENV_USER);
				this.SshdCommandSender = new SSHDCommandSender();
				this.SshdCommandSender.console = this;
				thread	    = new Thread(this, "SSHD ConsoleShell " + this.Username);
				thread.start();
			}
			catch (Exception e)
			{
				throw new IOException("Error starting shell", e);
			}
		}   

		@Override
		public void destroy(ChannelSession cs) { ((Logger)LogManager.getRootLogger()).removeAppender(this.streamHandlerAppender); }

		public void run()
		{
			try
			{
				if (!SshdPlugin.instance.getConfig().getString("Mode").equals("RPC"))
					printPreamble(this.ConsoleReader);
				while (true)
				{
					String command = this.ConsoleReader.readLine("\r>", null);
					// The user sent CTRL+D to close the shell, terminate the session.
					if (command == null)
						break;
					// Skip someone spamming enter
					if (command.trim().isEmpty())
						continue;
					// User wants to exit 
					if (command.equals("exit") || command.equals("quit"))
						break;
					// Clear the text from the screen (on supported terminals)
					if (command.equals("cls"))
					{
						this.ConsoleReader.clearScreen();
						continue;
					}
					// Hide the mkpasswd command input.
					Boolean mkpasswd = command.split(" ")[0].equals("mkpasswd");

					Bukkit.getScheduler().runTask(
						SshdPlugin.instance, () ->
						{
							if (SshdPlugin.instance.getConfig().getString("Mode").equals("RPC") && command.startsWith("rpc"))
							{
								// NO ECHO NO PREAMBLE AND SHIT
								String cmd = command.substring("rpc".length() + 1, command.length());
								Bukkit.dispatchCommand(this.SshdCommandSender, cmd);
							}
							else
							{
								if (!mkpasswd)
								{
									SshdPlugin.instance.getLogger().info("<" + this.Username + "> <" + (mkpasswd ? "True": "False") + "> " + command);
									
								}
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
							}
						});
				}
				// This should help stop one of the bugs where bytes are waiting to be written
				// but the client fucked off already so the plugin throws an exception.
				((Logger)LogManager.getRootLogger()).removeAppender(this.streamHandlerAppender);
			}
			catch (IOException e)
			{
				SshdPlugin.instance.getLogger().log(Level.SEVERE, "Error processing command from SSH", e);
			}
			finally
			{
				SshdPlugin.instance.getLogger().log(Level.INFO, this.Username + " disconnected from SSH.");
				callback.onExit(0);
			}
		}

		private void printPreamble(ConsoleReader cr) throws IOException
		{
			File f = new File(SshdPlugin.instance.getDataFolder(), "motd.txt");
			try 
			{
				BufferedReader br = new BufferedReader(new FileReader(f));

				String st;
				while ((st = br.readLine()) != null)
					cr.println(ConsoleLogFormatter.ColorizeString(st) + "\r");
			}
			catch (FileNotFoundException e)
			{
				SshdPlugin.instance.getLogger().log(Level.WARNING, "Could not open " + f + ": File does not exist.");
				// Not showing the SSH motd is not a fatal failure, let the session continue. 
			}

			// Doesn't really guarantee our actual system hostname but
			// it's better than not having one at all.
			cr.println("Connected to: " + InetAddress.getLocalHost().getHostName() + " (" + Bukkit.getServer().getName() + ")\r");
			cr.println(ConsoleLogFormatter.ColorizeString(Bukkit.getServer().getMotd()).replaceAll("\n", "\r\n"));
			cr.println("\r");
			cr.println("Type 'exit' to exit the shell." + "\r");
			cr.println("===============================================" + "\r");
		}
	}
}