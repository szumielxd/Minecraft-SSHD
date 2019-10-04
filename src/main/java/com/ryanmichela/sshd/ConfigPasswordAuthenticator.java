package com.ryanmichela.sshd;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2013 Ryan Michela
 */
public class ConfigPasswordAuthenticator implements PasswordAuthenticator {

	private Map<String, Integer> FailCounts = new HashMap<String, Integer>();

	@Override
	public boolean authenticate(String username, String password, ServerSession ss)
	{
		if (SshdPlugin.instance.getConfig().getString("Credentials." + username).equals(password))
		{
			FailCounts.put(username, 0);
			return true;
		}
		SshdPlugin.instance.getLogger().info("Failed login for " + username + " using password authentication.");

		Integer tries = SshdPlugin.instance.getConfig().getInt("LoginRetries");

		try
		{
			Thread.sleep(3000);
			if (this.FailCounts.containsKey(username))
				this.FailCounts.put(username, this.FailCounts.get(username) + 1);
			else
				this.FailCounts.put(username, 1);

			if (this.FailCounts.get(username) >= tries)
			{
				this.FailCounts.put(username, 0);
				ss.close(true);
			}
		}
		catch (InterruptedException e)
		{
			// do nothing
		}
		return false;
	}
}
