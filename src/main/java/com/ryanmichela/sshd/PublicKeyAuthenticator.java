package com.ryanmichela.sshd;

import org.apache.commons.lang.ArrayUtils;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import java.io.File;
import java.util.List;
import java.io.FileReader;
import java.security.PublicKey;

/**
 * Copyright 2013 Ryan Michela
 */
public class PublicKeyAuthenticator implements PublickeyAuthenticator
{

  private File authorizedKeysDir;

  public PublicKeyAuthenticator(File authorizedKeysDir) { this.authorizedKeysDir = authorizedKeysDir; }

	@Override public boolean authenticate(String username, PublicKey key, ServerSession session)
	{
		byte[] keyBytes = key.getEncoded();
		File keyFile	= new File(authorizedKeysDir, username);

		if (keyFile.exists())
		{
			try
            {
                List<AuthorizedKeyEntry> pklist = AuthorizedKeyEntry.readAuthorizedKeys(keyFile.toPath());
                
                PublickeyAuthenticator auth = PublickeyAuthenticator.fromAuthorizedEntries(username, session, pklist,
                        PublicKeyEntryResolver.IGNORING);

				boolean accepted = auth.authenticate(username, key, session);

                if (accepted)
                {
					SshdPlugin.instance.getLogger().info(
						username + " successfully authenticated via SSH session using key file " + keyFile.getAbsolutePath());
				}
                else
                {
					SshdPlugin.instance.getLogger().info(
						username + " failed authentication via SSH session using key file " + keyFile.getAbsolutePath());
				}
                return accepted;
				/*

				FileReader  fr = new FileReader(keyFile);
				PemDecoder  pd = new PemDecoder(fr);
				PublicKey k  = pd.getPemBytes();
				pd.close();

				if (k != null)
				{
					if (ArrayUtils.isEquals(key.getEncoded(), k.getEncoded()))
					{
						return true;
					}
				}
				else
				{
					SshdPlugin.instance.getLogger().severe("Failed to parse PEM file. " + keyFile.getAbsolutePath());
                }
                */
			}
			catch (Exception e)
			{
				SshdPlugin.instance.getLogger().severe("Failed to process public key " + keyFile.getAbsolutePath() + " " + e.getMessage());
			}
		}
		else
		{
			SshdPlugin.instance.getLogger().warning("Could not locate public key for " + username
													+ ". Make sure the user's key is named the same as their user name "
													+ "without a file extension.");
		}

		return false;
	}
}
