package com.ryanmichela.sshd.common;

import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.security.PublicKey;

/**
 * Copyright 2013 Ryan Michela
 */
public class PublicKeyAuthenticator implements PublickeyAuthenticator {
	
	
	private File authorizedKeysDir;
	private Map<String, Integer> FailCounts = new HashMap<String, Integer>();
	private final SshdPlugin plugin;
	
	
	public PublicKeyAuthenticator(File authorizedKeysDir, SshdPlugin plugin) {
		this.authorizedKeysDir = authorizedKeysDir;
		this.plugin = plugin;
	}

	@Override
	public boolean authenticate(String username, PublicKey key, ServerSession session) {
		//byte[] keyBytes = key.getEncoded();
		File keyFile 	= new File(authorizedKeysDir, username);
		Integer tries   = this.plugin.getConfiguration().getInt("LoginRetries");

		if (keyFile.exists()) {
			try {
				// Read all the public key entries
                List<AuthorizedKeyEntry> pklist = AuthorizedKeyEntry.readAuthorizedKeys(keyFile.toPath());
                // Get an authenticator
                PublickeyAuthenticator auth = PublickeyAuthenticator.fromAuthorizedEntries(username, session, pklist,
                        PublicKeyEntryResolver.IGNORING);

				// Validate that the logging in user has the same valid SSH key
				if (auth.authenticate(username, key, session)) {
					FailCounts.put(username, 0);
					return true;
				} else {
					this.plugin.getLogger().info(username + " failed authentication via SSH session using key file " + keyFile.getAbsolutePath());
				}

				// If the user fails with several SSH keys, then terminate the connection.
				if (this.FailCounts.containsKey(username)) this.FailCounts.put(username, this.FailCounts.get(username) + 1);
				else this.FailCounts.put(username, 1);

				if (this.FailCounts.get(username) >= tries) {
					this.FailCounts.put(username, 0);
					this.plugin.getLogger().info("Too many failures for " + username + ", disconnecting.");
					session.close(true);
				}

				return false;
			} catch (Exception e) {
				this.plugin.getLogger().severe("Failed to process public key " + keyFile.getAbsolutePath() + " " + e.getMessage());
			}
		} else {
			this.plugin.getLogger().warning("Could not locate public key for " + username
					+ ". Make sure the user's key is named the same as their user name "
					+ "without a file extension.");
		}

		return false;
	}
}
