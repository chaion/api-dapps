package com.chaion.makkiserver.exception;

public enum CodedErrorEnum {

    PKG_SIZE_TOO_LARGE(101, "DApp package size exceeds the maximum limitation"),
    PKG_STORE_ERROR(102, "Failed to store file"),
    PKG_UNZIP_ERROR(103, "Failed to unzip this file"),
    PKG_INVALID_STRUCTURE(104, "Package content structure is invalid"),
    PKG_INVALID_MANIFEST(105, "invalid manifest.json format"),
    PKG_MANIFEST_NAME_MISSING(106, "name field is missing in manifest.json"),
    PKG_MANIFEST_NAME_LENGTH_INVALID(107, "name field must be less than 24 characters"),
    PKG_MANIFEST_TAGLINE_LENGTH_INVALID(108, "tagline field must be less than 24 characters"),
    PKG_MANIFEST_FULL_DESCRIPTION_MISSING(109, "full description is missing in manifest.json"),
    PKG_MANIFEST_FULL_DESCRIPTION_LENGTH_INVALID(110, "full description field must be less than 1000 characters"),
    PKG_MANIFEST_AUTHOR_MISSING(111, "author is missing in manifest.json"),
    PKG_MANIFEST_CATEGORY_MISSING(112, "category is missing in manifest.json"),
    PKG_MANIFEST_CATEGORY_INVALID(113, "category is invalid"),
    PKG_MANIFEST_WEBSITE_URL_INVALID(114, "website url is invalid"),
    PKG_MANIFEST_EMAIL_INVALID(115, "email url is invalid"),
    PKG_MANIFEST_LOGO_MISSING(116, "logo is missing in manifest.json"),
    PKG_MANIFEST_LOGO_FILE_NOT_FOUND(117, "logo file is not found"),
    PKG_MANIFEST_LOGO_SIZE_INVALID(118, "logo image width and height are invalid."),
    PKG_MANIFEST_PLATFORM_MISSING(119, "platform is missing in manifest.json"),
    PKG_MANIFEST_PLATFORM_INVALID(120, "platform is invalid"),
    PKG_MANIFEST_ADVERTISE_IMAGE_MISSING(126, "advertise image is missing in manifest.json"),
    PKG_MANIFEST_ADVERTISE_IMAGE_FILE_NOT_FOUND(127, "advertise image file is not found."),
    PKG_MANIFEST_ADVERTISE_IMAGE_SIZE_INVALID(128, "advertise image width and height are invalid."),
    ERROR_FILE_NOT_FOUND(129, "File not found"),
    ERROR_STORE_IMAGE(130, "Failed to save image"),
    ERROR_INVAID_PARAM_CATEGORIES(131, "Invalid parameter: categories");

    private int code;
    private String msg;

    CodedErrorEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
