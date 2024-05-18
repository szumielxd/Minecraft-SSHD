package com.ryanmichela.sshd.common;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.DefaultErrorHandler;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Copyright 2014 Ryan Michela
 */
public class StreamHandlerAppender implements Appender {

	@Getter private StreamHandler streamHandler;
	private UUID uuid;
	@Getter private State state = null;
	@Getter private ErrorHandler handler = new DefaultErrorHandler(this);

	public StreamHandlerAppender(StreamHandler streamHandler) {
		this.streamHandler = streamHandler;
		uuid = UUID.randomUUID();
	}

	@Override
	public void append(LogEvent logEvent) {
		java.util.logging.Level level;

		if (logEvent.getLevel().equals(org.apache.logging.log4j.Level.DEBUG))
			level = java.util.logging.Level.FINE;
		else if (logEvent.getLevel().equals(org.apache.logging.log4j.Level.INFO))
			level = java.util.logging.Level.INFO;
		else if (logEvent.getLevel().equals(org.apache.logging.log4j.Level.WARN))
			level = java.util.logging.Level.WARNING;
		else if (logEvent.getLevel().equals(org.apache.logging.log4j.Level.ERROR))
			level = java.util.logging.Level.SEVERE;
		else
			level = java.util.logging.Level.INFO;
	

		String message = logEvent.getMessage().getFormattedMessage();
		LogRecord logRecord = new LogRecord(level, message);
		streamHandler.publish(logRecord);
	}

	@Override
	public String getName() {
		return "StreamHandlerAppender:" + uuid.toString();
	}

	@Override
	public Layout<? extends Serializable> getLayout() {
		return null;
	}

	@Override
	public boolean ignoreExceptions() {
		return false;
	}

	@Override
	public void setHandler(ErrorHandler errorHandler) {
	}

	@Override
	public void start() {
		this.state = State.STARTED;
	}

	@Override
	public void stop() {
		this.state = State.STOPPED;
	}

	@Override
	public boolean isStarted() {
		return this.state == State.STARTED;
	}

	@Override
	public boolean isStopped() {
		return this.state == State.STOPPED || this.state == State.STOPPING;
	}

	@Override
	public void initialize() {
		this.state = State.INITIALIZED;
	}
}