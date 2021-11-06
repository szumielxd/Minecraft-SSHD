package com.ryanmichela.sshd.common.objects;

import java.util.UUID;

public interface CommonPlayer extends CommonSender {

	public void chat(String message);
	public UUID getUniqueId();
	public void disconnect(String reason);
	public void connect(String server);
	public void executeServerCommand(String command);
	public String getWorldName();
	public void sendToWorld(String worldName);

}
