package cl.uchile.dcc.pd;

import cl.uchile.dcc.util.MemStats;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class PredicateDiff {
  private static final String TRIPLE_REGEX = "^([^\\p{Space}]+)\\s+(<[^>]+>)\\s+(.+)\\s?.$";
  
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
    Option option4 = new Option("o", "output file1");
    option4.setArgs(1);
    option4.setRequired(true);
    Option option5 = new Option("h", "print help");
    Options options = new Options();
    options.addOption(option1);
    options.addOption(option2);
    options.addOption(option3);
    options.addOption(option4);
    options.addOption(option5);
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
    boolean bool = commandLine.hasOption(option3.getOpt());
    String str3 = commandLine.getOptionValue(option4.getOpt());
    diffGraph(str1, str2, bool, str3);
  }
  
  private static void diffGraph(String paramString1, String paramString2, boolean paramBoolean, String paramString3) throws IOException {
    GZIPInputStream gZIPInputStream1, gZIPInputStream2;
    FileInputStream fileInputStream1 = new FileInputStream(paramString1);
    if (paramBoolean)
      gZIPInputStream1 = new GZIPInputStream(fileInputStream1); 
    BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(gZIPInputStream1, "utf-8"));
    System.err.println("Reading from " + paramString1);
    FileInputStream fileInputStream2 = new FileInputStream(paramString2);
    if (paramBoolean)
      gZIPInputStream2 = new GZIPInputStream(fileInputStream2); 
    BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(gZIPInputStream2, "utf-8"));
    System.err.println("Reading from " + paramString2);
    FileOutputStream fileOutputStream = new FileOutputStream(paramString3);
    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(fileOutputStream), "utf-8"));
    System.err.println("Writing to " + paramString3 + "\n");
    String str1 = bufferedReader1.readLine();
    String str2 = bufferedReader2.readLine();
    long l1 = 0L;
    long l2 = 0L;
    HashMap<Object, Object> hashMap1 = new HashMap<>();
    HashMap<Object, Object> hashMap2 = new HashMap<>();
    HashMap<Object, Object> hashMap3 = new HashMap<>();
    while (str1 != null || str2 != null) {
      int k;
      if (str1 == null) {
        k = 1;
      } else if (str2 == null) {
        k = -1;
      } else {
        k = str1.compareTo(str2);
      } 
      if (k < 0) {
        parseAndRegister(str1, (Map)hashMap1);
        l1++;
        str1 = bufferedReader1.readLine();
      } else if (k > 0) {
        parseAndRegister(str2, (Map)hashMap3);
        l2++;
        str2 = bufferedReader2.readLine();
      } else {
        parseAndRegister(str1, (Map)hashMap2);
        l1++;
        l2++;
        str1 = bufferedReader1.readLine();
        str2 = bufferedReader2.readLine();
      } 
      if (l1 % TICKS == 0L) {
        System.err.println("Read" + l1 + " left triples");
        System.err.println(MemStats.getMemStats() + "\n");
        System.out.println();
      } 
    } 
    for (String str : Sets.union(hashMap1.keySet(), hashMap3.keySet()))
      printWriter.println(str + "\t" + ((((Integer)hashMap1.getOrDefault(str, Integer.valueOf(0))).intValue() + ((Integer)hashMap3.getOrDefault(str, Integer.valueOf(0))).intValue()) / (((Integer)hashMap1
          .getOrDefault(str, Integer.valueOf(0))).intValue() + ((Integer)hashMap3.getOrDefault(str, Integer.valueOf(0))).intValue() + ((Integer)hashMap2.getOrDefault(str, Integer.valueOf(0))).intValue()))); 
    int i = 0;
    for (Iterator<Integer> iterator1 = Iterables.concat(hashMap1.values(), hashMap3.values()).iterator(); iterator1.hasNext(); ) {
      int k = ((Integer)iterator1.next()).intValue();
      i += k;
    } 
    int j = i;
    for (Iterator<Integer> iterator2 = hashMap2.values().iterator(); iterator2.hasNext(); ) {
      int k = ((Integer)iterator2.next()).intValue();
      j += k;
    } 
    printWriter.println("<V>\t" + (i / j));
    printWriter.close();
  }
  
  private static void parseAndRegister(String paramString, Map<String, Integer> paramMap) {
    Pattern pattern = Pattern.compile("^([^\\p{Space}]+)\\s+(<[^>]+>)\\s+(.+)\\s?.$");
    String str = null;
    Matcher matcher = pattern.matcher(paramString);
    if (matcher.matches()) {
      str = matcher.group(2);
    } else {
      System.err.println(paramString);
    } 
    paramMap.putIfAbsent(str, Integer.valueOf(0));
    paramMap.put(str, Integer.valueOf(((Integer)paramMap.get(str)).intValue() + 1));
  }
}