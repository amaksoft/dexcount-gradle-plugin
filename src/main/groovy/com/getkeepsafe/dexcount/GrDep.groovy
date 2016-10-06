package com.getkeepsafe.dexcount

import com.google.gson.JsonObject
import org.apache.commons.io.FileUtils

/**
 * Created by amak on 10/6/16.
 */
class GrDep {
    String group
    String name
    String version

    File file

    int ownMethods = 0
    int totalMethods = 0
    int ownFields = 0
    int totalFields = 0

    GrDep(String group, String name, String version, File file) {
        this.group = group
        this.name = name
        this.version = version
        this.file = file
    }

    String getGroup() {
        return group
    }

    String getName() {
        return name
    }

    String getVersion() {
        return version
    }

    File getFile() {
        return file
    }

    int getOwnMethods() {
        return ownMethods
    }

    void setOwnMethods(int ownMethods) {
        this.ownMethods = ownMethods
    }

    void incOwnMethods(int inc) {
        this.ownMethods += inc
    }

    void incOwnMethods() {
        this.ownMethods++
    }

    int getTotalMethods() {
        return totalMethods
    }

    void setTotalMethods(int totalMethods) {
        this.totalMethods = totalMethods
    }

    void incTotalMethods(int inc) {
        this.totalMethods += inc
    }

    void incTotalMethods() {
        this.totalMethods++
    }

    int getOwnFields() {
        return ownFields
    }

    void setOwnFields(int ownFields) {
        this.ownFields = ownFields
    }
    void incOwnFields(int inc) {
        this.ownFields += ownFields
    }
    void incOwnFields() {
        this.ownFields++
    }

    int getTotalFields() {
        return totalFields
    }

    void setTotalFields(int totalFields) {
        this.totalFields = totalFields
    }
    void incTotalFields(int inc) {
        this.totalFields += inc
    }
    void incTotalFields() {
        this.totalFields++
    }

    public JsonObject toJsonObject() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "${group}:${name}:${version}")
        json.addProperty("filename", file.name)
        if(file != null && file.exists())
            json.addProperty("fileSize", FileUtils.byteCountToDisplaySize(file.length()))
        else
            json.addProperty("fileSize", "not found")
        json.addProperty("ownMethods", ownMethods)
        json.addProperty("totalMethods", totalMethods)
        json.addProperty("ownFields", ownFields)
        json.addProperty("totalFields", totalFields)
        return json
    }

    @Override
    public String toString() {
        return "GrDep{" +
                "group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", file=" + file +
                ", ownMethods=" + ownMethods +
                ", totalMethods=" + totalMethods +
                ", ownFields=" + ownFields +
                ", totalFields=" + totalFields +
                '}';
    }
}
