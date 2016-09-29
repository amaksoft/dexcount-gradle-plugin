package com.getkeepsafe.dexcount;

import com.android.dexdeps.FieldRef;
import com.android.dexdeps.MethodRef
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException

import java.lang.System;

/**
 * Created by amak on 9/27/16.
 */
public class Main {

    static PackageTree tree;

    public static void main(String[] args) {

// create the command line parser
        CommandLineParser parser = new DefaultParser();

// create the Options
        Options options = new Options();
        options.addOption( "a", "all", false, "do not hide entries starting with ." );
        options.addOption( "A", "almost-all", false, "do not list implied . and .." );
        options.addOption( "b", "escape", false, "print octal escapes for nongraphic "
                + "characters" );
        options.addOption( OptionBuilder.withLongOpt( "block-size" )
                .withDescription( "use SIZE-byte blocks" )
                .hasArg()
                .withArgName("SIZE")
                .create() );
        options.addOption( "B", "ignore-backups", false, "do not list implied entried "
                + "ending with ~");
        options.addOption( "c", false, "with -lt: sort by, and show, ctime (time of last "
                + "modification of file status information) with "
                + "-l:show ctime and sort by name otherwise: sort "
                + "by ctime" );
        options.addOption( "C", false, "list entries by columns" );

        String header = "Do something useful with an input file\n\n";
        String footer = "\nPlease report issues at http://example.com/issues";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("myapp", header, options, footer, true);

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            // validate that block-size has been set
            if( line.hasOption( "block-size" ) ) {
                // print the value of block-size
                System.out.println( line.getOptionValue( "block-size" ) );
            }
        }
        catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }
        File extrFile = new File(args[0]);
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
        tree.print(new FileWriter(args[1]), OutputFormat.JSON, new PrintOptions());
    }
}
