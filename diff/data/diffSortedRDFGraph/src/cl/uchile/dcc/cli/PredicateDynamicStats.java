package cl.uchile.dcc.cli;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.script.SimpleScriptContext;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import cl.uchile.dcc.utils.MemStats;
import cl.uchile.dcc.utils.PredAndDyn;

class MutableTripla {
	int total = 0;
	int add = 0;
	int del = 0;

	MutableTripla(int t, int a, int d) {
		total = t;
		add = a;
		del = d;
	}

	public void increment() {
		++total;
	}

	public void incrementAdd() {
		++add;
		++total;
	}

	public void incrementDel() {
		++del;
		++total;
	}

	public int getTotal() {
		return total;
	}
}

public class PredicateDynamicStats {
	public static String TRIPLE_REGEX = "^(<[^>]+>)\\s+(<[^>]+>)\\s+(.*)\\s?.$";

	public static int TICKS = 100000000;

	public static void main(String[] paramArrayOfString) throws IOException {
		Option option1 = new Option("l", "left input file");
		option1.setArgs(1);
		option1.setRequired(true);
		Option option2 = new Option("r", "right input file");
		option2.setArgs(1);
		option2.setRequired(true);
		Option option3 = new Option("igz", "input file is GZipped");
		option3.setArgs(0);
		Option option4 = new Option("o1", "output file1");
		option4.setArgs(1);
		option4.setRequired(true);
		Option option5 = new Option("o2", "output file2");
		option5.setArgs(1);
		option5.setRequired(true);
		Option option6 = new Option("ogz", "output file should be GZipped");
		option6.setArgs(0);
		Option option7 = new Option("k", "print first k lines to std out when finished");
		option7.setArgs(1);
		option7.setRequired(false);
		Option option8 = new Option("t", "print first t lines to read in");
		option8.setArgs(1);
		option8.setRequired(false);
		Option option9 = new Option("h", "print help");
		Options options = new Options();
		options.addOption(option1);
		options.addOption(option2);
		options.addOption(option3);
		options.addOption(option4);
		options.addOption(option5);
		options.addOption(option6);
		options.addOption(option7);
		options.addOption(option8);
		options.addOption(option9);
		BasicParser basicParser = new BasicParser();
		CommandLine commandLine = null;
		try {
			commandLine = basicParser.parse(options, paramArrayOfString);
		} catch (ParseException parseException) {
			System.err.println("***ERROR: " + parseException.getClass() + ": " + parseException.getMessage());
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp("parameters:", options);
			return;
		}
		if (commandLine.hasOption("h")) {
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp("parameters:", options);
			return;
		}
		String str1 = commandLine.getOptionValue(option1.getOpt());
		String str2 = commandLine.getOptionValue(option2.getOpt());
		boolean bool1 = commandLine.hasOption(option3.getOpt());
		String str3 = commandLine.getOptionValue(option4.getOpt());
		String str4 = commandLine.getOptionValue(option5.getOpt());
		boolean bool2 = commandLine.hasOption(option6.getOpt());
		int i = Integer.MAX_VALUE;
		if (commandLine.hasOption(option7.getOpt()))
			i = Integer.parseInt(commandLine.getOptionValue(option7.getOpt()));
		int j = Integer.MAX_VALUE;
		if (commandLine.hasOption(option8.getOpt()))
			j = Integer.parseInt(commandLine.getOptionValue(option8.getOpt()));
		diffGraph(str1, str2, bool1, str3, str4, bool2);
	}

	private static void diffGraph(String paramString1, String paramString2, boolean paramBoolean1, String paramString3,
			String paramString4, boolean paramBoolean2) throws IOException {
		GZIPInputStream gZIPInputStream1, gZIPInputStream2;
		GZIPOutputStream gZIPOutputStream1, gZIPOutputStream2;
		FileInputStream fileInputStream1 = new FileInputStream(paramString1);
		if (paramBoolean1)
			gZIPInputStream1 = new GZIPInputStream(fileInputStream1);
		BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(gZIPInputStream1, "utf-8"));
		System.err.println("Reading from " + paramString1);
		FileInputStream fileInputStream2 = new FileInputStream(paramString2);
		if (paramBoolean1)
			gZIPInputStream2 = new GZIPInputStream(fileInputStream2);
		BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(gZIPInputStream2, "utf-8"));
		System.err.println("Reading from " + paramString2);
		FileOutputStream fileOutputStream1 = new FileOutputStream(paramString3);
		FileOutputStream fileOutputStream2 = new FileOutputStream(paramString4);
		if (paramBoolean2) {
			gZIPOutputStream1 = new GZIPOutputStream(fileOutputStream1);
			gZIPOutputStream2 = new GZIPOutputStream(fileOutputStream2);
		}
		PrintWriter printWriter1 = new PrintWriter(
				new OutputStreamWriter(new BufferedOutputStream(gZIPOutputStream1), "utf-8"));
		PrintWriter printWriter2 = new PrintWriter(
				new OutputStreamWriter(new BufferedOutputStream(gZIPOutputStream2), "utf-8"));
		System.err.println("Writing to " + paramString3 + "\n");
		String str1 = bufferedReader1.readLine();
		String str2 = bufferedReader2.readLine();
		HashMap<Object, Object> hashMap1 = new HashMap<>();
		HashMap<Object, Object> hashMap2 = new HashMap<>();
		long l1 = 0L;
		long l2 = 0L;
		while (str1 != null || str2 != null) {
			int i;
			if (str1 == null) {
				i = 1;
			} else if (str2 == null) {
				i = -1;
			} else {
				i = str1.compareTo(str2);
			}
			if (i < 0) {
				printWriter1.println(str1);
				str1 = bufferedReader1.readLine();
				l1++;
				continue;
			}
			if (i > 0) {
				printWriter2.println(str2);
				str2 = bufferedReader2.readLine();
				l2++;
				continue;
			}
			str1 = bufferedReader1.readLine();
			str2 = bufferedReader2.readLine();
			l1++;
			l2++;
		}
		System.err.println("Read" + l1);
		System.err.println("Read" + l2);
		bufferedReader1.close();
		bufferedReader2.close();
		printWriter1.close();
		printWriter2.close();
	}
}
