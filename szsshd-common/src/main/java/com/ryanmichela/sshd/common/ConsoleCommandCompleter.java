package com.ryanmichela.sshd.common;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

public class ConsoleCommandCompleter implements Completer {
	
	
	private final SshdPlugin plugin;
	
	
	public ConsoleCommandCompleter(SshdPlugin plugin) {
		this.plugin = plugin;
	}
	
	
	
	public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
		
		return cursor;
	}



	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		String buffer = line.line();
		Waitable<List<Candidate>> waitable = new Waitable<List<Candidate>>() {
			@Override
			protected List<Candidate> evaluate() {
				List<Candidate> list = plugin.tabComplete(plugin.getConsoleSender(), buffer).stream().map(Candidate::new).collect(Collectors.toList());
				return list;
			}
		};
		this.plugin.getTaskManager().runSync(waitable);
		try {
			List<Candidate> offers = waitable.get();
			if (offers == null) return;
			candidates.addAll(offers);
			return;
		} catch (ExecutionException e) {
			this.plugin.getLogger().log(Level.WARNING, "Unhandled exception when tab completing", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}

