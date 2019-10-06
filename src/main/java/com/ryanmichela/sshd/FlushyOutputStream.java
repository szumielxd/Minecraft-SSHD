package com.ryanmichela.sshd;

import org.apache.sshd.common.SshException;
import org.apache.sshd.common.channel.exception.SshChannelClosedException;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * Copyright 2013 Ryan Michela
 */
public class FlushyOutputStream extends OutputStream 
{

	private OutputStream base;
	private boolean isClosed = false;

	public FlushyOutputStream(OutputStream base) 
	{
		this.base = base;
	}

	@Override
	public void write(int b) throws IOException 
	{
		this.write(BigInteger.valueOf(b).toByteArray());
	}

	@Override
	public void write(byte[] b) throws IOException 
	{
		this.write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException 
	{
		if (isClosed)
			return;

		try 
		{
			base.write(b, off, len);
			base.flush();
		}
		catch (SshChannelClosedException e)
		{
			// ignored.
		}
	}

	@Override
	public void close() throws IOException
	{
		isClosed = true;
		base.close();
	}
}
