package com.ryanmichela.sshd;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Arrays;

import com.ryanmichela.sshd.Cryptography;
import com.ryanmichela.sshd.SshdPlugin;

class MkpasswdCommand implements CommandExecutor
{
	// Because Spigot's failed syntax API is really less than ideal (you should be required to add a
	// SendSyntax function override), we're just always going to return true even for syntax failures
	// as we will handle the syntax message internally. This also lets us send the messages more
	// securely to the client without people knowing we're using the command. This prevents password
	// or hash leakages from the user to other connected users. Plus this syntax will show how
	// to both use the command and what hashes we support which is important for people who don't
	// know how to RTFM. - Justin
	private void SendSyntax(CommandSender sender, boolean invalid)
	{
		if (invalid)
			sender.sendMessage("\u00A7cInvalid Syntax\u00A7r");
		sender.sendMessage("\u00A7a/mkpasswd <help|hash> <password>\u00A7r");
		sender.sendMessage("\u00A79Supported Hashes: SHA256, PBKDF2, BCRYPT, PLAIN\u00A7r");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		// If we're not mkpasswd, just fuck off.
		if (!label.equalsIgnoreCase("mkpasswd"))
			return false;

		String algoritm, password;
		try
		{
			// Stupid bukkit, we have to concatenate the arguments together if they're using
			// spaces in their passwords otherwise it won't be as strong as it should be.
			algoritm = args[0];
			password = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			if (password.trim().isEmpty()) // Shortcut to the catch statement below.
				throw new ArrayIndexOutOfBoundsException();
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// ignore it.
			this.SendSyntax(sender, true);
			return true;
		}

		boolean hasperm = (sender instanceof Player) ? ((Player)sender).hasPermission("sshd.mkpasswd") : true;

		if (hasperm)
		{ 
			try
			{
				String hash = "";
				// Dumb but whatever. Some people are really dense.
				if (algoritm.equalsIgnoreCase("PLAIN"))
				{
					// I mean c'mon...
					sender.sendMessage("\u00A79Your Hash: \u00A7cIt's literally your unhashed password.");
					return true;
				}
				else if (algoritm.equalsIgnoreCase("pbkdf2"))
					hash = Cryptography.PBKDF2_HashPassword(password);
				else if (algoritm.equalsIgnoreCase("bcrypt"))
					hash = Cryptography.BCrypt_HashPassword(password);
				else if (algoritm.equalsIgnoreCase("sha256"))
					hash = Cryptography.SHA256_HashPassword(password);
				else
				{
					this.SendSyntax(sender, !algoritm.equalsIgnoreCase("help"));
					return true;
				}

				sender.sendMessage("\u00A79Your Hash: " + hash + "\u00A7r");
			}
			catch (Exception e)
			{
				// We're console, just print the stack trace.
				e.printStackTrace();
				sender.sendMessage("\u00A7cAn error occured. Please check console for details.\u00A7r");
			}
		}

		return true;
	}
}
