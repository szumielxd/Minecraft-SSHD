package com.ryanmichela.sshd.common;

import com.ryanmichela.sshd.common.objects.CommonSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.apache.sshd.server.shell.ShellFactory;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.common.util.logging.AbstractLoggingBean;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.StreamHandler;

public class ConsoleShellFactory implements ShellFactory {
	
	private final SshdPlugin plugin;
	
	public ConsoleShellFactory(SshdPlugin plugin) {
		this.plugin = plugin;
	}
	

	@Override
	public Command createShell(ChannelSession cs) {
		return new ConsoleShell();
	}

	public class ConsoleShell extends AbstractLoggingBean implements Command, Runnable {

		private InputStream in;
		private OutputStream out;
		private OutputStream err;
		private ExitCallback callback;
		private Environment environment;
		private Thread thread;
		private String Username;

		StreamHandlerAppender streamHandlerAppender;
		public LineReader consoleReader;
		public CommonSender sshdCommandSender;

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

		@Override
		public void setInputStream(InputStream in) {
			this.in = in;
		}

		@Override
		public void setOutputStream(OutputStream out) {
			this.out = out;
		}

		@Override
		public void setErrorStream(OutputStream err) {
			this.err = err;
		}

		@Override
		public void setExitCallback(ExitCallback callback) {
			this.callback = callback;
		}

		@Override
		public synchronized void start(ChannelSession cs, Environment env) throws IOException {
			try {
				Terminal terminal = TerminalBuilder.builder().system(false).streams(in, out).build();
				this.consoleReader = LineReaderBuilder.builder().terminal(terminal).completer(new ConsoleCommandCompleter(plugin)).build();

				StreamHandler streamHandler = new FlushyStreamHandler(out, new ConsoleLogFormatter(), this.consoleReader);
				this.streamHandlerAppender = new StreamHandlerAppender(streamHandler);
				plugin.addHandler(this.streamHandlerAppender);

				this.environment = env;
				this.Username = env.getEnv().get(Environment.ENV_USER);
				this.sshdCommandSender = plugin.constructSSHDSender(this);
				thread	    = new Thread(this, "SSHD ConsoleShell " + this.Username);
				thread.start();
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new IOException("Error starting shell", e);
			}
		}

		@Override
		public void destroy(ChannelSession cs) {
			plugin.removeHandler(this.streamHandlerAppender);
		}

		public void run() {
			try {
				if (!plugin.getConfiguration().getString("Mode").equals("RPC"))
					printPreamble(this.consoleReader);
				String command;
				while (true) {
					try {
						command = this.consoleReader.readLine("> ");
					} catch (EndOfFileException e) {
						// The user sent CTRL+D to close the shell, terminate the session.
						break;
					}
					
					// Skip someone spamming enter
					if (command.trim().isEmpty()) continue;
					
					// User wants to exit 
					if (command.equals("exit") || command.equals("quit")) break;
					
					// Emergency kill Java Virtual Machine - usefull on server crash
					if (command.equals("--kill-jvm")) {
						System.exit(9);
						break;
					}
					// Clear the text from the screen (on supported terminals)
					if (command.equals("cls")) {
						this.consoleReader.getTerminal().puts(Capability.clear_screen);
						this.consoleReader.getTerminal().flush();
						continue;
					}
					// Hide the mkpasswd command input from other users.
					Boolean mkpasswd = command.split(" ")[0].equals("mkpasswd");
					
					final String constCmd = command;

					plugin.getTaskManager().runSync(() -> {
							boolean rpc = plugin.getConfiguration().getString("Mode").equals("RPC") && constCmd.toLowerCase().startsWith("rpc ");
							CommonSender sender = rpc? this.sshdCommandSender : plugin.getConsoleSender();
							String cmd = rpc? constCmd.substring("rpc ".length()) : constCmd;
							if (!mkpasswd && !rpc) plugin.getLogger().info("<" + this.Username + "> " + cmd);
							if (!plugin.dispatchCommand(sender, cmd)) {
								sender.sendMessage(new ComponentBuilder("Command not found").color(ChatColor.RED).create());
							}
					});
				}
			} catch (IOException e) {
				e.printStackTrace();
				plugin.getLogger().log(Level.SEVERE, "Error processing command from SSH", e);
			} finally {
				plugin.getLogger().log(Level.INFO, this.Username + " disconnected from SSH.");
				callback.onExit(0);
			}
		}

		private void printPreamble(LineReader cr) throws IOException {
			File f = new File(plugin.getDataFolder(), "motd.txt");
			PrintWriter pw = cr.getTerminal().writer();
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String st;
				while ((st = br.readLine()) != null) pw.println(ConsoleLogFormatter.colorizeString(st) + "\r");
			}
			catch (FileNotFoundException e) {
				plugin.getLogger().log(Level.WARNING, "Could not open " + f + ": File does not exist.");
				// Not showing the SSH motd is not a fatal failure, let the session continue. 
			}

			// Doesn't really guarantee our actual system hostname but
			// it's better than not having one at all.
			pw.println("Connected to: " + InetAddress.getLocalHost().getHostName() + " ("+plugin.getServerName()+")\r");
			pw.println(ConsoleLogFormatter.colorizeString(plugin.getConfiguration().getString("motd")).replaceAll("\n", "\r\n"));
			pw.println("\r");
			pw.println("Type 'exit' or press Ctrl+D to exit the shell." + "\r");
			pw.println("Type '--kill-jvm' to EMERGENCY kill server." + "\r");
			pw.println("===============================================" + "\r");
		}
	}
}