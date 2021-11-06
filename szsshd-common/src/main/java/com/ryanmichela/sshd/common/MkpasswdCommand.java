package com.ryanmichela.sshd.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ryanmichela.sshd.common.objects.CommonCommand;
import com.ryanmichela.sshd.common.objects.CommonSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class MkpasswdCommand extends CommonCommand {
	
	public MkpasswdCommand() {
		super("mkpasswd");
	}

	public void sendSyntax(CommonSender sender, boolean invalid)
	{
		if (invalid) sender.sendMessage(new ComponentBuilder("Invalid Syntax").color(ChatColor.RED).create());
		sender.sendMessage(new ComponentBuilder("/mkpasswd <help|hash> <password>").color(ChatColor.GREEN).create());
		sender.sendMessage(new ComponentBuilder("Supported Hashes: SHA256, PBKDF2, BCRYPT, PLAIN").color(ChatColor.BLUE).create());
	}

	@Override
	public boolean onCommand(CommonSender sender, String[] args, String label) {
		String algoritm, password;
		try {
			// Stupid bukkit, we have to concatenate the arguments together if they're using
			// spaces in their passwords otherwise it won't be as strong as it should be.
			algoritm = args[0];
			password = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			if (password.length() == 0)
				throw new ArrayIndexOutOfBoundsException(); // shortcut 
		} catch (ArrayIndexOutOfBoundsException e) {
			this.sendSyntax(sender, false);
			return true;
		}

		// If they're a player, check and make sure they have a permission
		// If they're not a player (aka, the console), just return true.
		boolean hasperm = sender.hasPermission("sshd.mkpasswd");

		if (hasperm) { 
			try {
				String hash = "";
				// Dumb but whatever. Some people are really dense.
				if (algoritm.equalsIgnoreCase("PLAIN")) {
					// I mean c'mon...
					sender.sendMessage(TextComponent.fromLegacyText("\u00A79Your Hash: \u00A7cIt's literally your unhashed password."));
					return true;
				}
				else if (algoritm.equalsIgnoreCase("pbkdf2")) {
					hash = Cryptography.PBKDF2_HashPassword(password);
				} else if (algoritm.equalsIgnoreCase("bcrypt")) {
					hash = Cryptography.BCrypt_HashPassword(password);
				} else if (algoritm.equalsIgnoreCase("sha256")) {
					hash = Cryptography.SHA256_HashPassword(password);
				} else {
					this.sendSyntax(sender, !algoritm.equalsIgnoreCase("help"));
					return true;
				}

				sender.sendMessage(new ComponentBuilder("Your Hash: " + hash).color(ChatColor.BLUE).create());
			}
			catch (Exception e)
			{
				// We're console, just print the stack trace.
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public String getPermissionMessage() {
		return null;
	}

	@Override
	public List<String> onTabComplete(CommonSender sender, String[] args) {
		return new ArrayList<>();
	}
}
