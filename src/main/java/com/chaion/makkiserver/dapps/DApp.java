package com.chaion.makkiserver.dapps;

public class DApp {

    public static final int NAME_MAX_LENGTH = 24;
    public static final int TAGLINE_MAX_LENGTH = 50;
    public static final int FULL_DESCRIPTION_MAX_LENGTH = 1000;
    public static final int LOGO_WIDTH = 128;
    public static final int LOGO_HEIGHT = 128;
    public static final int ADVERTISE_IMAGE_WIDTH = 128;
    public static final int ADVERTISE_IMAGE_HEIGHT = 128;

    private String id;
    private String name;
    private String tagline;
    private String fullDescription;
    private String author;
    private String websiteUrl;
    private String contactEmail;
    private String logoUrl;
    private String advertiseImageUrl;
    private Platform platform;
    private Category category;
    private DAppType type;
    // type=embed
    private String packUrl;
    private String version;
    private String updates;
    // type=external
    private String launchUrl;
    // type=app
    private String androidLink;
    private String iOSLink;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getUpdates() {
        return updates;
    }

    public void setUpdates(String updates) {
        this.updates = updates;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getAdvertiseImageUrl() {
        return advertiseImageUrl;
    }

    public void setAdvertiseImageUrl(String advertiseImageUrl) {
        this.advertiseImageUrl = advertiseImageUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getLaunchUrl() {
        return launchUrl;
    }

    public void setLaunchUrl(String launchUrl) {
        this.launchUrl = launchUrl;
    }

    public DAppType getType() {
        return type;
    }

    public void setType(DAppType type) {
        this.type = type;
    }

    public String getPackUrl() {
        return packUrl;
    }

    public void setPackUrl(String packUrl) {
        this.packUrl = packUrl;
    }

    public String getAndroidLink() {
        return androidLink;
    }

    public void setAndroidLink(String androidLink) {
        this.androidLink = androidLink;
    }

    public String getiOSLink() {
        return iOSLink;
    }

    public void setiOSLink(String iOSLink) {
        this.iOSLink = iOSLink;
    }
}
