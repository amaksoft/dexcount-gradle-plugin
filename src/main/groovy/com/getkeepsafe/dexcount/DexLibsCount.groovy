package com.getkeepsafe.dexcount

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.builder.core.VariantConfiguration
import com.android.dexdeps.FieldRef
import com.android.dexdeps.HasDeclaringClass
import com.android.dexdeps.MethodRef
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

/**
 * Created by amak on 10/3/16.
 */
class DexLibsCount extends DefaultTask {

    public BaseVariant variant;
    public BaseVariantOutput apkOrDex;

    @TaskAction
    void countMethods() {
        try {
            PackageTree libTree = new PackageTree(project.name, false, null);
            HashMap<String, Integer> libs = new HashMap()
            ArrayList<File> inputs = new ArrayList<>()
            inputs.addAll(getProject().configurations.compile.files)
            inputs.add(apkOrDex.outputFile)
            inputs.each { file ->
                println "indexing ${file.name} ..."
                String hrFilesize = FileUtils.byteCountToDisplaySize(file.length())
                println "size: " + hrFilesize
                libTree.addSource(file.name);
                File extrFile = new File(file.path);
                List<DexFile> dataList = DexFile.extractDexData(extrFile, 10);
                try {
                    int totalMethods = 0;
                    int importedMethods = 0;
                    int totalFields = 0;
                    int importedFields = 0;
                    for(DexFile dFile : dataList) {
                        totalMethods += dFile.methodRefs.size()
                        for(MethodRef mRef : dFile.getMethodRefs()) {
                            def className = mRef.getDeclClassName().substring(1, mRef.getDeclClassName().length() - 1);
                            boolean imported = dFile.jarContents != null && !dFile.jarContents.contains(className + ".class")
                            if(dFile.jarContents != null && !imported) {
                                println "own method ${className}.${mRef.name}"
                                libTree.addMethodRef(mRef, file.name);
                            } else {
                                println "imported method ${className}.${mRef.name}"
                                importedMethods++;
                                libTree.addMethodRef(mRef, null);
                            }
                        }
                    }
                    for(DexFile dFile : dataList) {
                        totalFields += dFile.fieldRefs.size()
                        for(FieldRef fRef : dFile.getFieldRefs()) {
                            def className = fRef.getDeclClassName().substring(1, fRef.getDeclClassName().length() - 1);
                            boolean imported = dFile.jarContents != null && !dFile.jarContents.contains(className + ".class")
                            if(dFile.jarContents != null && !imported) {
                                libTree.addFieldRef(fRef, file.name);
                            } else {
                                importedFields++;
                                libTree.addFieldRef(fRef, null);
                            }
                        }
                    }
                    println "own methods: ${totalMethods - importedMethods}"
                    println "total methods: ${totalMethods}"
                    println "own fields: ${totalFields - importedFields}"
                    println "total fields: ${totalFields}"
                } finally {
                    for(DexFile dFile : dataList) {
                        dFile.dispose();
                    }
                }
            }

            System.out.println("Writing to file");
            FileWriter extrWriter = new FileWriter(getProject().buildDir.path +"/" + project.name + ".json");
            libTree.print(extrWriter, OutputFormat.JSON, new PrintOptions(
                    includeMethodCount: true,
                    includeFieldCount: true,
                    includeTotalMethodCount: true,
                    teamCityIntegration: false,
                    orderByMethodCount: true,
                    includeClasses: false,
                    printHeader: true,
                    maxTreeDepth: Integer.MAX_VALUE));
            extrWriter.close()
        } catch (DexCountException e) {

        }
    }

    public String humanReadableByteCount (long bytes, boolean si) {

        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log (bytes) / Math.log (unit));
        String siPre = (si ? "" : "i");
        String pre = (si ? "KMGTPE" : "KMGTPE").charAt (exp - 1)
        return String.format ("%.1f %s%sB",
                bytes / Math.pow (unit, exp),pre,siPre);
    }

    private static boolean isInClassFile(HasDeclaringClass ref, DexFile dFile) {
        def className = ref.getDeclClassName().substring(1, ref.getDeclClassName().length() - 1);
        return dFile.jarContents.contains(className+".class")
    }
}
