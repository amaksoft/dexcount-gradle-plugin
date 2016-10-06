package com.getkeepsafe.dexcount

import com.android.build.gradle.api.BaseVariantOutput
import com.android.dexdeps.FieldRef
import com.android.dexdeps.HasDeclaringClass
import com.android.dexdeps.MethodRef
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by amak on 10/3/16.
 */
class DexLibsCount extends DefaultTask {

    public BaseVariantOutput apkOrDex;

    def long startTime
    def long ioTime
    def long treegenTime
    def long outputTime


    @TaskAction
    void countMethods() {

        ArrayList<GrDep> deps = new ArrayList()
        project.configurations.compile.getResolvedConfiguration().getResolvedArtifacts().sort().eachWithIndex { at, idx ->
            def dep = at.getModuleVersion().getId()
            deps.add(new GrDep(dep.group, dep.name, dep.version, at.getFile()))
        }
        deps.add(new GrDep(project.group.toString(), project.name.toString(), project.version.toString(), apkOrDex.outputFile))

        try {
            PackageTree libTree = new PackageTree(project.name, false, null);

            deps.eachWithIndex { dep, idx ->
                println "indexing ${dep.file.name} ..."

                File extrFile = new File(dep.file.path);
                List<DexFile> dataList = DexFile.extractDexData(extrFile, 10);
                try {
                    for(DexFile dFile : dataList) {
                        dep.incTotalMethods(dFile.methodRefs.size())
                        for(MethodRef mRef : dFile.getMethodRefs()) {
                            def className = mRef.getDeclClassName().substring(1, mRef.getDeclClassName().length() - 1);
                            boolean imported = dFile.jarContents != null && !dFile.jarContents.contains(className + ".class")
                            if(dFile.jarContents != null && !imported) {
//                                println "own method ${className}.${mRef.name}"
                                libTree.addMethodRef(mRef, idx.toString());
                                dep.incOwnMethods()
                            } else {
//                                println "imported method ${className}.${mRef.name}"
                                libTree.addMethodRef(mRef, null);
                            }
                        }
                    }
                    for(DexFile dFile : dataList) {
                        dep.incTotalFields(dFile.fieldRefs.size())
                        for(FieldRef fRef : dFile.getFieldRefs()) {
                            def className = fRef.getDeclClassName().substring(1, fRef.getDeclClassName().length() - 1);
                            boolean imported = dFile.jarContents != null && !dFile.jarContents.contains(className + ".class")
                            if(dFile.jarContents != null && !imported) {
                                libTree.addFieldRef(fRef, idx.toString());
                                dep.incOwnFields()
                            } else {
                                libTree.addFieldRef(fRef, null);
                            }
                        }
                    }
                    println "own methods: ${dep.ownMethods}"
                    println "total methods: ${dep.totalMethods}"
                    println "own fields: ${dep.ownFields}"
                    println "total fields: ${dep.totalFields}"
                } finally {
                    for(DexFile dFile : dataList) {
                        dFile.dispose();
                    }
                }
            }

            JsonArray jsonArray = new JsonArray()
            deps.each { GrDep entry ->
                jsonArray.add(entry.toJsonObject())
            }

            File outFile = new File("${project.buildDir}/outputs/reports/libsReport", "libdata.js")
            outFile.parentFile.mkdirs();
            System.out.println("Writing to file ${outFile.path}");
            IOUtil.printToFile(outFile) { PrintStream out ->
                out.print("var data = { \"libs\": \n  ${jsonArray.toString()},\n")
                out.print("  \"dex\": ")
                libTree.printJson(out, new PrintOptions(
                        includeMethodCount: true,
                        includeFieldCount: true,
                        includeTotalMethodCount: true,
                        teamCityIntegration: false,
                        orderByMethodCount: true,
                        includeClasses: false,
                        printHeader: true,
                        maxTreeDepth: Integer.MAX_VALUE));
                out.print("\n}")
            }

        } catch (DexCountException e) {

        }
    }

    private static boolean isInClassFile(HasDeclaringClass ref, DexFile dFile) {
        def className = ref.getDeclClassName().substring(1, ref.getDeclClassName().length() - 1);
        return dFile.jarContents.contains(className+".class")
    }

}
