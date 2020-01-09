package com.chaion.makkiiserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = {"classpath:git.properties"}, ignoreResourceNotFound = true)
public class GitConfig {

    @Value("${git.commit.id.describe}")
    private String gitDescribe;

    @Value("${git.build.version}")
    private String gitBuildVersion;

    @Value("${git.build.time}")
    private String gitBuildTime;

    @Value("${git.branch}")
    private String gitBranch;

    public String getGitDescribe() {
        return gitDescribe;
    }

    public void setGitDescribe(String gitDescribe) {
        this.gitDescribe = gitDescribe;
    }

    public String getGitBuildVersion() {
        return gitBuildVersion;
    }

    public void setGitBuildVersion(String gitBuildVersion) {
        this.gitBuildVersion = gitBuildVersion;
    }

    public String getGitBuildTime() {
        return gitBuildTime;
    }

    public void setGitBuildTime(String gitBuildTime) {
        this.gitBuildTime = gitBuildTime;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }
}
