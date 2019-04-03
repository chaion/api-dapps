package com.chaion.makkiserver.version;

import org.springframework.data.annotation.Id;

public class AppVersion {
    @Id
    private String id;
    private String version;
    private int versionCode;
    private String platform;
    private boolean mandatory;
    private String updates;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getUpdates() {
        return updates;
    }

    public void setUpdates(String updates) {
        this.updates = updates;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "AppVersion{" +
                "id='" + id + '\'' +
                ", version='" + version + '\'' +
                ", versionCode=" + versionCode +
                ", platform='" + platform + '\'' +
                ", mandatory=" + mandatory +
                ", updates='" + updates + '\'' +
                '}';
    }
}
