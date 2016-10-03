package com.getkeepsafe.dexcount;

import com.android.dexdeps.FieldRef;
import com.android.dexdeps.MethodRef
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException

import java.lang.System;

/**
 * Created by amak on 9/27/16.
 */
public class Main {

    static PackageTree tree;

    public static void main(String[] args) {

        String outputFileName, inputFileName;
        OutputFormat oFormat = OutputFormat.LIST;

// create the command line parser
        CommandLineParser parser = new DefaultParser();

// create the Options
        Options options = new Options();
        options.addOption(Option.builder("f")
                .longOpt("output-format")
                .desc("specify output format. Options available:\n" +
                "\tLIST - .txt file with results represented as list\n" +
                "\tTREE - .txt file with tree-like result\n" +
                "\tJSON - .json file\n" +
                "\tYAML - .yml file")
                .hasArg()
                .argName("TYPE")
                .type(OutputFormat)
                .build());
        options.addOption(Option.builder("o")
                .longOpt("output-file")
                .required()
                .desc("specify output file name")
                .hasArg()
                .argName("FILE NAME")
                .build());
        options.addOption(Option.builder("i")
                .longOpt("input-file")
                .required()
                .desc("specify input file name")
                .hasArg()
                .argName("FILE NAME")
                .build());

        options.addOption("h", "help", false, "Print this help message")

        String header = "Do something useful with an input file\n\n";
        String footer = "\nPlease report issues at http://example.com/issues";

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("dexcounter", header, options, footer, true);
            }
            if (line.hasOption("output-format")) {
                oFormat = line.getOptionValue("output-format");
                System.out.println("oFormat = " + oFormat);
            }
            if (line.hasOption("output-file")) {
                outputFileName = line.getOptionValue("output-file");
                System.out.println("outputFileName = " + outputFileName);
            }
            if (line.hasOption("input-file")) {
                inputFileName = line.getOptionValue("input-file");
                System.out.println("inputFileName = " + inputFileName);
            }
        }
        catch (ParseException exp) {
            System.out.println("ERROR " + exp.getMessage());
            return;
        }
        File extrFile = new File(inputFileName);
        List<DexFile> dataList = DexFile.extractDexData(extrFile, 10);
        System.out.println(dataList);

        try {
            tree = new PackageTree();

                for(DexFile dFile : dataList) {
                    for(MethodRef mRef : dFile.getMethodRefs()) {
                        tree.addMethodRef(mRef);
                    }
                }
            for(DexFile dFile : dataList) {
                for(FieldRef fRef : dFile.getFieldRefs()) {
                    tree.addFieldRef(fRef);
                }
            }
        } finally {
            for(DexFile dFile : dataList) {
                dFile.dispose();
            }
        }
        FileWriter extrWriter = new FileWriter(outputFileName);
        tree.print(extrWriter, oFormat, new PrintOptions());
        extrWriter.close()
    }
}
