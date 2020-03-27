package com.ryanmichela.sshd;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.session.helpers.AbstractSession;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.apache.sshd.server.subsystem.sftp.SimpleAccessControlSftpEventListener;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import com.ryanmichela.sshd.ConsoleShellFactory;
import com.ryanmichela.sshd.MkpasswdCommand;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List; 
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Copyright 2013 Ryan Michela
 */
public class SshdPlugin extends JavaPlugin
{
	private SshServer sshd;
	public static SshdPlugin instance;

	public static List<ConfigurationSection> GetSections(ConfigurationSection source) 
	{
		if (source == null)
			return null;

		List<ConfigurationSection> nodes = new ArrayList<ConfigurationSection>();
		for (String key : source.getKeys(false)) 
		{
			if (source.isConfigurationSection(key)) 
				nodes.add(source.getConfigurationSection(key));
		}
		return nodes;
	}

	@Override public void onLoad()
	{
		saveDefaultConfig();
		File authorizedKeys = new File(getDataFolder(), "authorized_keys");
		if (!authorizedKeys.exists())
			authorizedKeys.mkdirs();

		try
		{
			File motd = new File(getDataFolder(), "motd.txt");
			if (!motd.exists())
			{
				InputStream link = (getClass().getResourceAsStream("/motd.txt"));
				Files.copy(link, motd.getAbsoluteFile().toPath());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// Don't go any lower than INFO or SSHD will cause a stack overflow exception.
		// SSHD will log that it wrote bites to the output stream, which writes
		// bytes to the output stream - ad nauseaum.
		getLogger().setLevel(Level.INFO);
	}

	@Override public void onEnable()
	{
		instance = this;

		sshd = SshServer.setUpDefaultServer();
		sshd.setPort(getConfig().getInt("Port", 1025));
		String host = getConfig().getString("ListenAddress", "all");
		sshd.setHost(host.equals("all") ? null : host);

		File hostKey		= new File(getDataFolder(), "hostkey");
		File authorizedKeys = new File(getDataFolder(), "authorized_keys");

		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(hostKey.toPath()));
		sshd.setShellFactory(new ConsoleShellFactory());
		sshd.setPasswordAuthenticator(new ConfigPasswordAuthenticator());
		sshd.setPublickeyAuthenticator(new PublicKeyAuthenticator(authorizedKeys));

		if (getConfig().getBoolean("EnableSFTP", false))
		{
			// Handle access control for SFTP.
			SftpSubsystemFactory.Builder builder = new SftpSubsystemFactory.Builder();
			builder.addSftpEventListener(new SimpleAccessControlSftpEventListener() 
			{
				protected boolean isAccessAllowed(ServerSession session, String remote, Path localpath)
				{
					try
					{
						ConfigurationSection UsernameNamespace = getConfig().getConfigurationSection("Credentials." + session.getUsername() + ".sftp");

						// They don't have SFTP enabled so deny them.
						if (UsernameNamespace == null || !UsernameNamespace.getBoolean("enabled"))
							return false;

						
						List<ConfigurationSection> rules = GetSections(UsernameNamespace.getConfigurationSection("rules"));
						if (rules != null)
						{
							for (ConfigurationSection path : rules)
							{
								// Check if the requesting path matches
								if (localpath.toString().matches(path.getName()))
								{
									// Check if they have read permissions
									if (path.getBoolean("readable"))
										return true;

									getLogger().info(String.format("Denied %s read access to \"%s\" matching rule \"%s\"", session.getUsername(), localpath.toString(), path.getName()));
									return false;
								}
							}
						}

						return UsernameNamespace.getString("default").equalsIgnoreCase("allow");
					}
					catch (Exception e)
					{
						e.printStackTrace();
						// Automatically deny.
						return false;
					}
				}

				protected boolean isModificationAllowed(ServerSession session, String remote, Path localpath)
				{
					try
					{
						boolean defaultbool = getConfig().getBoolean("Credentials.$default.sftp.enabled", false);
						ConfigurationSection UsernameNamespace = getConfig().getConfigurationSection("Credentials." + session.getUsername() + ".sftp");

						// They don't have SFTP enabled so deny them.
						if (UsernameNamespace == null || !UsernameNamespace.getBoolean("enabled", defaultbool))
							return false;

						// Check a list of files against a path trying to be accessed.
						List<ConfigurationSection> rules = GetSections(UsernameNamespace.getConfigurationSection("rules"));
						if (rules != null)
						{
							for (ConfigurationSection path : rules)
							{
								// Check if the requesting path matches
								if (localpath.toString().matches(path.getName()))
								{
									// Check if they have read permissions
									if (path.getBoolean("writeable"))
										return true;

									getLogger().info(String.format("Denied %s modifications to \"%s\" matching rule \"%s\"", session.getUsername(), localpath.toString(), path.getName()));
									return false;
								}
							}
						}

						return UsernameNamespace.getString("default", "deny").equalsIgnoreCase("allow");
					}
					catch (Exception e)
					{
						e.printStackTrace();
						// Automatically deny.
						return false;
					}
				}
			});

			sshd.setSubsystemFactories(Collections.singletonList(builder.build()));
			sshd.setFileSystemFactory(new VirtualFileSystemFactory(FileSystems.getDefault().getPath(getDataFolder().getAbsolutePath()).getParent().getParent()));
		}

		this.getCommand("mkpasswd").setExecutor(new MkpasswdCommand());

		sshd.setCommandFactory(new ConsoleCommandFactory());
		try
		{
			sshd.start();
		}
		catch (IOException e)
		{
			getLogger().log(Level.SEVERE, "Failed to start SSH server! ", e);
		}
	}

	@Override public void onDisable()
	{
		try
		{
			// Terminate any active sessions
			for (AbstractSession as : sshd.getActiveSessions())
				as.close(true);
			// Pass "true" to stop immediately!
			sshd.stop(true);
		}
		catch (Exception e)
		{
			// do nothing
			e.printStackTrace();
		}
	}
}
