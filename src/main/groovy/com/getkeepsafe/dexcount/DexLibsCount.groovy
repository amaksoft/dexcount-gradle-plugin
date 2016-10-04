package com.getkeepsafe.dexcount

import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.VariantConfiguration
import com.android.dexdeps.FieldRef
import com.android.dexdeps.MethodRef
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Created by amak on 10/3/16.
 */
class DexLibsCount extends DefaultTask {

    public BaseVariant variant;

    @TaskAction
    void countMethods() {
        try {
            PackageTree libTree = new PackageTree(project.name, false, null);
            getProject().configurations.compile.each {
                println "${it.name}"
                File extrFile = new File(it.path);
                List<DexFile> dataList = DexFile.extractDexData(extrFile, 10);
                System.out.println(dataList);
                try {
                    for(DexFile dFile : dataList) {
                        for(MethodRef mRef : dFile.getMethodRefs()) {
                            libTree.addMethodRef(mRef, it.name);
                        }
                    }
                    for(DexFile dFile : dataList) {
                        for(FieldRef fRef : dFile.getFieldRefs()) {
                            libTree.addFieldRef(fRef, it.name);
                        }
                    }
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
                    orderByMethodCount: false,
                    includeClasses: true,
                    printHeader: true,
                    maxTreeDepth: Integer.MAX_VALUE));
            extrWriter.close()
        } catch (DexCountException e) {

        }
    }

}
