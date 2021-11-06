package com.ryanmichela.sshd.common;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

import com.ryanmichela.sshd.common.objects.CommonSender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright 2013 Ryan Michela
 */
public interface SshdPlugin {
	
	default public void onLoad() {
		File file = new File(getDataFolder(), "config.yml");

		File authorizedKeys = new File(getDataFolder(), "authorized_keys");
		if (!authorizedKeys.exists())
			authorizedKeys.mkdirs();

		try {
			File motd = new File(getDataFolder(), "motd.txt");
			if (!motd.exists()) {
				InputStream link = (getClass().getResourceAsStream("/motd.txt"));
				Files.copy(link, motd.getAbsoluteFile().toPath());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (!file.exists()) {
				// Copy our config file.
				InputStream link = (getClass().getResourceAsStream("/config.yml"));
				Files.copy(link, file.getAbsoluteFile().toPath());
			}
			this.setConfiguration(ConfigurationProvider.getProvider(YamlConfiguration.class).load(file));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Don't go any lower than INFO or SSHD will cause a stack overflow exception.
		// SSHD will log that it wrote bites to the output stream, which writes
		// bytes to the output stream - ad nauseaum.
		getLogger().setLevel(Level.INFO);
	}

	default public void onEnable() {

		SshServer sshd = this.setSshServer(SshServer.setUpDefaultServer());
		sshd.setPort(this.getConfiguration().getInt("Port", 1025));
		String host = this.getConfiguration().getString("ListenAddress", "all");
		sshd.setHost(host.equals("all") ? null : host);

		File hostKey		= new File(getDataFolder(), "hostkey");
		File authorizedKeys = new File(getDataFolder(), "authorized_keys");

		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(hostKey.toPath()));
		sshd.setShellFactory(new ConsoleShellFactory(this));
		sshd.setPasswordAuthenticator(new ConfigPasswordAuthenticator(this));
		sshd.setPublickeyAuthenticator(new PublicKeyAuthenticator(authorizedKeys, this));

		if (this.getConfiguration().getBoolean("EnableSFTP", false)) {
			// Handle access control for SFTP.
			SftpSubsystemFactory.Builder builder = new SftpSubsystemFactory.Builder();
			
			/*builder.addSftpEventListener(new AbstractSftpEventListenerAdapter() {
				
				
				@Override
				public void creating(ServerSession session, Path localpath, Map<String, ?> attr) {
					isModificationAllowed(session, localpath);
				}
				@Override
				public void linking(ServerSession session, Path source, Path target, boolean symLink) {
					isAccessAllowed(session, target);
				}
				@Override
				public void modifyingAttributes(ServerSession session, Path localpath, Map<String, ?> attr) {
					isModificationAllowed(session, localpath);
				}
				
				
				
				
				
				protected void isAccessAllowed(ServerSession session, Path localpath) {
					try {
						Configuration usernameNamespace = getConfiguration().getSection("Credentials." + session.getUsername() + ".sftp");

						// They don't have SFTP enabled so deny them.
						if (usernameNamespace == null || !usernameNamespace.getBoolean("enabled")) {
							throw new UnsupportedOperationException(String.format("Denied %s read access to \"%s\"", session.getUsername(), localpath.toString()));
						}

						
						Configuration rules = usernameNamespace.getSection("rules");
						if (rules != null && !rules.getKeys().isEmpty()) {
							for (String path : rules.getKeys()) {
								// Check if the requesting path matches
								if (localpath.toString().matches(path)) {
									// Check if they have read permissions
									if (rules.getSection(path).getBoolean("readable")) return;

									throw new UnsupportedOperationException(String.format("Denied %s read access to \"%s\" matching rule \"%s\"", session.getUsername(), localpath.toString(), path));
								}
							}
						}
						if (!usernameNamespace.getString("default").equalsIgnoreCase("allow")) {
							throw new UnsupportedOperationException(String.format("Denied %s read access to \"%s\"", session.getUsername(), localpath.toString()));
						}
						
					} catch (Exception e) {
						e.printStackTrace();
						// Automatically deny.
						throw new UnsupportedOperationException(String.format("Denied %s read access to \"%s\"", session.getUsername(), localpath.toString()));
					}
				}

				protected void isModificationAllowed(ServerSession session, Path localpath) {
					try {
						boolean defaultbool = getConfiguration().getBoolean("Credentials.$default.sftp.enabled", false);
						Configuration usernameNamespace = getConfiguration().getSection("Credentials." + session.getUsername() + ".sftp");

						// They don't have SFTP enabled so deny them.
						if (usernameNamespace == null || !usernameNamespace.getBoolean("enabled", defaultbool)) {
							throw new UnsupportedOperationException(String.format("Denied %s read access to \"%s\"", session.getUsername(), localpath.toString()));
						}

						// Check a list of files against a path trying to be accessed.
						Configuration rules = usernameNamespace.getSection("rules");
						if (rules != null && !rules.getKeys().isEmpty()) {
							for (String path : rules.getKeys()) {
								// Check if the requesting path matches
								if (localpath.toString().matches(path)) {
									// Check if they have read permissions
									if (rules.getSection(path).getBoolean("writeable")) return;

									throw new UnsupportedOperationException(String.format("Denied %s modifications to \"%s\" matching rule \"%s\"", session.getUsername(), localpath.toString(), path));
								}
							}
						}
						if (!usernameNamespace.getString("default", "deny").equalsIgnoreCase("allow")) {
							throw new UnsupportedOperationException(String.format("Denied %s modifications to \"%s\"", session.getUsername(), localpath.toString()));
						}
						
					} catch (Exception e) {
						e.printStackTrace();
						// Automatically deny.
						throw new UnsupportedOperationException(String.format("Denied %s read access to \"%s\"", session.getUsername(), localpath.toString()));
					}
				}
			});
			*/

			sshd.setSubsystemFactories(Collections.singletonList(builder.build()));
			sshd.setFileSystemFactory(new VirtualFileSystemFactory(FileSystems.getDefault().getPath(getDataFolder().getAbsolutePath()).getParent().getParent()));
		}

		sshd.setCommandFactory(new ConsoleCommandFactory(this));
		try {
			sshd.start();
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to start SSH server! ", e);
		}
	}

	default public void onDisable() {
		try {
			this.getSshServer().stop();
		} catch (Exception e) {
			// do nothing
		}
	}
	
	public abstract File getDataFolder();
	public abstract Configuration setConfiguration(Configuration config);
	public abstract Configuration getConfiguration();
	public abstract SshServer setSshServer(SshServer server);
	public abstract SshServer getSshServer();
	public abstract Logger getLogger();
	public abstract Logger getMainLogger();
	public abstract TaskManager getTaskManager();
	public abstract CommonSender getConsoleSender();
	public abstract boolean dispatchCommand(CommonSender sender, String command);
	public abstract List<String> tabComplete(CommonSender sender, String cmdLine);
	public abstract CommonSender constructSSHDSender(ConsoleShellFactory.ConsoleShell console);
	public abstract void addHandler(StreamHandlerAppender handler);
	public abstract void removeHandler(StreamHandlerAppender handler);
	public abstract String getServerName();
	
}
