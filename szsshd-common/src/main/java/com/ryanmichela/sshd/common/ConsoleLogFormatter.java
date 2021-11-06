package com.ryanmichela.sshd.common;

/**
 * Copyright 2013 Ryan Michela
 */

import net.md_5.bungee.api.ChatColor;
import org.fusesource.jansi.Ansi;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class ConsoleLogFormatter extends Formatter {
	
    private SimpleDateFormat dateFormat;
    private static final Map<ChatColor, String> replacements = new HashMap<>();
    private static final Pattern RGB_PATTERN = Pattern.compile("§x(§[0-9a-f]){6}", Pattern.CASE_INSENSITIVE);
    static {
    	replacements.put(ChatColor.BLACK, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString());
		replacements.put(ChatColor.DARK_BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString());
		replacements.put(ChatColor.DARK_GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString());
		replacements.put(ChatColor.DARK_AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString());
		replacements.put(ChatColor.DARK_RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString());
		replacements.put(ChatColor.DARK_PURPLE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString());
		replacements.put(ChatColor.GOLD, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString());
		replacements.put(ChatColor.GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString());
		replacements.put(ChatColor.DARK_GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fgBright(Ansi.Color.BLACK).boldOff().toString());
		replacements.put(ChatColor.BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fgBright(Ansi.Color.BLUE).boldOff().toString());
		replacements.put(ChatColor.GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fgBright(Ansi.Color.GREEN).boldOff().toString());
		replacements.put(ChatColor.AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fgBright(Ansi.Color.CYAN).boldOff().toString());
		replacements.put(ChatColor.RED, Ansi.ansi().a(Ansi.Attribute.RESET).fgBright(Ansi.Color.RED).boldOff().toString());
		replacements.put(ChatColor.LIGHT_PURPLE, Ansi.ansi().a(Ansi.Attribute.RESET).fgBright(Ansi.Color.MAGENTA).boldOff().toString());
		replacements.put(ChatColor.YELLOW, Ansi.ansi().a(Ansi.Attribute.RESET).fgBright(Ansi.Color.YELLOW).boldOff().toString());
		replacements.put(ChatColor.WHITE, Ansi.ansi().a(Ansi.Attribute.RESET).fgBright(Ansi.Color.WHITE).boldOff().toString());
		replacements.put(ChatColor.MAGIC, Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString());
		replacements.put(ChatColor.BOLD, Ansi.ansi().a(Ansi.Attribute.CONCEAL_ON).toString());
		replacements.put(ChatColor.STRIKETHROUGH, Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString());
		replacements.put(ChatColor.UNDERLINE, Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString());
		replacements.put(ChatColor.ITALIC, Ansi.ansi().a(Ansi.Attribute.ITALIC).toString());
		replacements.put(ChatColor.RESET, Ansi.ansi().a(Ansi.Attribute.RESET).toString());
    }

	public ConsoleLogFormatter() {
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    public static String colorizeString(String str) {
    	String result = str;
        try {
        	// RGB support
        	ChatColor.WHITE.getColor(); // Check if supports RGB
            result = downsampleColors(result);
        } catch (NoSuchMethodError e) {}
        
		for (ChatColor color : replacements.keySet()) {
			if (replacements.containsKey(color)) {
				result = result.replaceAll("(?i)" + color.toString(), replacements.get(color));
			} else {
				result = result.replaceAll("(?i)" + color.toString(), "");
			}
		}
        result += Ansi.ansi().reset().toString();
        return result;
	}
    
    public static String downsampleColors(String str) {
        return RGB_PATTERN.matcher(str).replaceAll(res -> {
        	String matched = res.group();
        	Color c1 = Color.getColor(new StringBuilder()
        			.append(matched.charAt(3)).append(matched.charAt(5))
        			.append(matched.charAt(7)).append(matched.charAt(9))
        			.append(matched.charAt(11)).append(matched.charAt(13)).toString());
        	int distance = Integer.MAX_VALUE;
        	ChatColor nearest = ChatColor.WHITE;
        	for (ChatColor color : replacements.keySet()) {
        		Color c2 = color.getColor();
        		if (c2 != null) {
        			int dist = (int) (Math.pow((c1.getRed()-c2.getRed()), 2) + Math.pow((c1.getGreen()-c2.getGreen()), 2) + Math.pow((c1.getBlue()-c2.getBlue()), 2));
        			if (dist < distance) {
        				distance = dist;
        				nearest = color;
        			}
        		}
        	}
        	return nearest.toString();
        });
    }

  	public String format(LogRecord logrecord) {
		colorize(logrecord);
		StringBuilder stringbuilder = new StringBuilder();

		stringbuilder.append(" [");
		stringbuilder.append(this.dateFormat.format(logrecord.getMillis())).append(" ");

		stringbuilder.append(logrecord.getLevel().getName()).append("]: ");
		stringbuilder.append(this.formatMessage(logrecord));
		stringbuilder.append('\n');
		Throwable throwable = logrecord.getThrown();

		if (throwable != null) {
			StringWriter stringwriter = new StringWriter();

			throwable.printStackTrace(new PrintWriter(stringwriter));
			stringbuilder.append(stringwriter.toString());
		}

		return stringbuilder.toString().replace("\n", "\r\n");
	}

	private void colorize(LogRecord logrecord) {
		String result = colorizeString(logrecord.getMessage());
		logrecord.setMessage(result);
    }
}

