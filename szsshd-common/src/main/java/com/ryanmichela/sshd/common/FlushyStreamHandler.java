package com.ryanmichela.sshd.common;

import org.jline.reader.LineReader;

import java.io.OutputStream;
import java.util.logging.*;

/**
 * Copyright 2013 Ryan Michela
 */
public class FlushyStreamHandler extends StreamHandler 
{
    private LineReader reader;

    public FlushyStreamHandler(OutputStream out, Formatter formatter, LineReader reader) {
        super(out, formatter);
        this.reader = reader;
        setLevel(Level.INFO);
    }

    @Override
    public synchronized void publish(LogRecord record) {
        record.setMessage(record.getMessage().replace("\n", "\n\r"));
        this.reader.printAbove(this.getFormatter().format(record));
    }

    @Override
    public synchronized void flush() {
    }
}
