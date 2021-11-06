package com.ryanmichela.sshd.common;

public interface TaskManager {
	
	
	public void runAsync(Runnable task);
	public void runSync(Runnable task);
	

}
