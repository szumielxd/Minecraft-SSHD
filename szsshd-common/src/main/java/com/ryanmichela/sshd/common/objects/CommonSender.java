package com.ryanmichela.sshd.common.objects;

import net.md_5.bungee.api.chat.BaseComponent;

public interface CommonSender {
	
	
	public void sendMessage(String message);
	public void sendMessage(BaseComponent message);
	public void sendMessage(BaseComponent... message);
	public boolean hasPermission(String permission);
	public String getName();
	

}
