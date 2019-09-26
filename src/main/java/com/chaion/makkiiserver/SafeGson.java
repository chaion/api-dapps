package com.chaion.makkiiserver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Gson safe helper class to return default value
 */
public class SafeGson {

    public static String getJsonStringValue(JsonObject jo, String key) {
        try {
            if (jo.has(key)) {
                JsonElement je = jo.get(key);
                if (jo.get(key).isJsonPrimitive()) {
                    return je.getAsString();
                }
            }
        } catch (Exception e) {}
        return null;
    }

    public static int getJsonIntValue(JsonObject jo, String key) {
        try {
            if (jo.has(key)) {
                JsonElement je = jo.get(key);
                if (jo.get(key).isJsonPrimitive()) {
                    return je.getAsInt();
                }
            }
        } catch (Exception e) {}
        return 0;
    }

    public static long getJsonLongValue(JsonObject jo, String key) {
        try {
            if (jo.has(key)) {
                JsonElement je = jo.get(key);
                if (jo.get(key).isJsonPrimitive()) {
                    return je.getAsLong();
                }
            }
        } catch (Exception e) {}
        return 0L;
    }

    public static BigInteger getJsonBigIntegerValue(JsonObject jo, String key) {
        try {
            if (jo.has(key)) {
                JsonElement je = jo.get(key);
                if (jo.get(key).isJsonPrimitive()) {
                    return je.getAsBigInteger();
                }
            }
        } catch (Exception e) {}
        return null;
    }

    public static BigDecimal getJsonBigDecimalValue(JsonObject jo, String key) {
        try {
            if (jo.has(key)) {
                JsonElement je = jo.get(key);
                if (jo.get(key).isJsonPrimitive()) {
                    return je.getAsBigDecimal();
                }
            }
        } catch (Exception e) {}
        return null;
    }
}
