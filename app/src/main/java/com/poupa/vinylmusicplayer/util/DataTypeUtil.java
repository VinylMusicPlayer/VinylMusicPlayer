package com.poupa.vinylmusicplayer.util;

import com.poupa.vinylmusicplayer.model.CategoryInfo;

import java.lang.reflect.Type;

public class DataTypeUtil {
    public static Object checkType(String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            // If the value is in square brackets, it is an array
            CategoryInfo[] elements = value.substring(1, value.length() - 1).split(",");
            System.out.println("Array of type integer: " + elements.length);
            return elements;
        } else {
            try {
                //boolean boolValue = Boolean.parseBoolean(value);
                return Boolean.parseBoolean(value);
                //System.out.println("Boolean: " + boolValue);
            } catch (NumberFormatException e1) {
                try {
                    //int intValue = Integer.parseInt(value);
                    return Integer.parseInt(value);
                    //System.out.println("Integer: " + intValue);
                } catch (NumberFormatException e2) {
                    try {
                        //double doubleValue = Double.parseDouble(value);
                        return Float.parseFloat(value);
                        //System.out.println("Double: " + doubleValue);
                    } catch (NumberFormatException e3) {
                        // If none of the above, it is considered a string
                        //System.out.println("String: " + value);
                        return value;
                    }
                }
            }
        }
    }
}
