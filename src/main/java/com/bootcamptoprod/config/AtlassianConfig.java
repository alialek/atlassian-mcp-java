package com.bootcamptoprod.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AtlassianConfig {

    @ConfigurationProperties("confluence")
    public static class ConfluenceConfig {
        private String url;
        private String username;
        private String apiToken;
        private String personalToken;
        private boolean sslVerify = true;
        private String spacesFilter;

        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getApiToken() { return apiToken; }
        public void setApiToken(String apiToken) { this.apiToken = apiToken; }
        public String getPersonalToken() { return personalToken; }
        public void setPersonalToken(String personalToken) { this.personalToken = personalToken; }
        public boolean isSslVerify() { return sslVerify; }
        public void setSslVerify(boolean sslVerify) { this.sslVerify = sslVerify; }
        public String getSpacesFilter() { return spacesFilter; }
        public void setSpacesFilter(String spacesFilter) { this.spacesFilter = spacesFilter; }

        public boolean isCloud() {
            return url != null && url.contains("atlassian.net");
        }

        public boolean isAuthConfigured() {
            return (username != null && apiToken != null) || personalToken != null;
        }
    }

    @ConfigurationProperties("jira")
    public static class JiraConfig {
        private String url;
        private String username;
        private String apiToken;
        private String personalToken;
        private boolean sslVerify = true;
        private String projectsFilter;

        // Getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getApiToken() { return apiToken; }
        public void setApiToken(String apiToken) { this.apiToken = apiToken; }
        public String getPersonalToken() { return personalToken; }
        public void setPersonalToken(String personalToken) { this.personalToken = personalToken; }
        public boolean isSslVerify() { return sslVerify; }
        public void setSslVerify(boolean sslVerify) { this.sslVerify = sslVerify; }
        public String getProjectsFilter() { return projectsFilter; }
        public void setProjectsFilter(String projectsFilter) { this.projectsFilter = projectsFilter; }

        public boolean isCloud() {
            return url != null && url.contains("atlassian.net");
        }

        public boolean isAuthConfigured() {
            return (username != null && apiToken != null) || personalToken != null;
        }
    }

    @ConfigurationProperties("zephyr")
    public static class ZephyrConfig {
        private String baseUrl;
        private String bearerToken;
        private boolean sslVerify = true;
        private int timeout = 30;

        // Getters and setters
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getBearerToken() { return bearerToken; }
        public void setBearerToken(String bearerToken) { this.bearerToken = bearerToken; }
        public boolean isSslVerify() { return sslVerify; }
        public void setSslVerify(boolean sslVerify) { this.sslVerify = sslVerify; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }

        public boolean isAuthConfigured() {
            return bearerToken != null && !bearerToken.isEmpty();
        }
    }
} 