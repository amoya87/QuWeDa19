package cl.uchile.dcc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class DatasetStats {
  public static String TRIPLE_REGEX = "^([^\\p{Space}]+)\\s+(<[^>]+>)\\s+(.+)\\s?.$";
  
  public static int TICKS = 100000000;
  
  public static void main(String[] paramArrayOfString) throws IOException {
    Option option1 = new Option("l", "left input file");
    option1.setArgs(1);
    option1.setRequired(true);
    Option option2 = new Option("igz", "input file is GZipped");
    option2.setArgs(0);
    Option option3 = new Option("o1", "output file1");
    option3.setArgs(1);
    option3.setRequired(true);
    Option option4 = new Option("ogz", "output file should be GZipped");
    option4.setArgs(0);
    Option option5 = new Option("k", "print first k lines to std out when finished");
    option5.setArgs(1);
    option5.setRequired(false);
    Option option6 = new Option("t", "print first t lines to read in");
    option6.setArgs(1);
    option6.setRequired(false);
    Option option7 = new Option("h", "print help");
    Options options = new Options();
    options.addOption(option1);
    options.addOption(option2);
    options.addOption(option3);
    options.addOption(option4);
    options.addOption(option5);
    options.addOption(option6);
    options.addOption(option7);
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
    boolean bool1 = commandLine.hasOption(option2.getOpt());
    String str2 = commandLine.getOptionValue(option3.getOpt());
    boolean bool2 = commandLine.hasOption(option4.getOpt());
    int i = Integer.MAX_VALUE;
    if (commandLine.hasOption(option5.getOpt()))
      i = Integer.parseInt(commandLine.getOptionValue(option5.getOpt())); 
    int j = Integer.MAX_VALUE;
    if (commandLine.hasOption(option6.getOpt()))
      j = Integer.parseInt(commandLine.getOptionValue(option6.getOpt())); 
    diffGraph(str1, bool1, str2, bool2);
  }
  
  private static void diffGraph(String paramString1, boolean paramBoolean1, String paramString2, boolean paramBoolean2) throws IOException {
    GZIPInputStream gZIPInputStream;
    GZIPOutputStream gZIPOutputStream;
    FileInputStream fileInputStream = new FileInputStream(paramString1);
    if (paramBoolean1)
      gZIPInputStream = new GZIPInputStream(fileInputStream); 
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gZIPInputStream, "utf-8"));
    System.err.println("Reading from " + paramString1);
    FileOutputStream fileOutputStream = new FileOutputStream(paramString2 + "e.txt");
    if (paramBoolean2)
      gZIPOutputStream = new GZIPOutputStream(fileOutputStream); 
    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(gZIPOutputStream), "utf-8"));
    System.err.println("Writing to " + paramString2 + "\n");
    String str = bufferedReader.readLine();
    long l = 0L;
    HashSet<String> hashSet1 = new HashSet();
    HashSet<String> hashSet2 = new HashSet();
    byte b = 0;
    HashSet<String> hashSet3 = new HashSet();
    while (str != null) {
      Pattern pattern = Pattern.compile(TRIPLE_REGEX);
      String str1 = null;
      String str2 = null;
      String str3 = null;
      Matcher matcher = pattern.matcher(str);
      if (matcher.matches()) {
        str1 = matcher.group(1);
        str2 = matcher.group(2);
        str3 = matcher.group(3).trim();
      } else {
        System.err.println(str);
      } 
      if (isBlank(str1)) {
        hashSet3.add(str1);
      } else {
        hashSet1.add(str1);
      } 
      hashSet2.add(str2);
      hashSet1.add(str2);
      if (isBlank(str3)) {
        hashSet3.add(str3);
      } else if (isLiteral(str3)) {
        b++;
      } else {
        hashSet1.add(str3);
      } 
      str = bufferedReader.readLine();
      l++;
      if (l % TICKS == 0L) {
        System.err.println("Read" + l + " triples");
        System.err.println(MemStats.getMemStats() + "\n");
      } 
    } 
    System.err.print("Iteracion " + l + " entities = " + hashSet1.size() + " predicates = " + hashSet2
        .size());
    System.err.println(" literals = " + b + " blanks = " + hashSet3.size());
    bufferedReader.close();
    printWriter.close();
    System.err.println("Read" + l + " triples");
  }
  
  private static boolean isLiteral(String paramString) {
    return (!isUri(paramString) && !isBlank(paramString));
  }
  
  private static boolean isBlank(String paramString) {
    return paramString.substring(0, 1).equals("_");
  }
  
  private static boolean isUri(String paramString) {
    return paramString.substring(0, 1).equals("<");
  }
}