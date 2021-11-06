package com.ryanmichela.sshd.bukkit;

import com.ryanmichela.sshd.common.SshdPlugin;

import lombok.NonNull;

public class SshdProvider {
	
	
	private static SshdPlugin INSTANCE;
	
	
	public static void register(@NonNull SshdPlugin plugin) {
		INSTANCE = plugin;
	}
	
	
	public static void unregister() {
		INSTANCE = null;
	}
	
	
	public static SshdPlugin get() {
		return INSTANCE;
	}
	
	
	private SshdProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }
	

}
