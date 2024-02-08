package com.poupa.vinylmusicplayer.util;

import com.poupa.vinylmusicplayer.model.CategoryInfo;

import java.lang.reflect.Type;

public class DataTypeUtil {
    public static Object checkType(String value) {
        try {
            if(value.equals("true")) {
                return true;
            } else if(value.equals("false")) {
                return false;
            }
            throw new NumberFormatException();
        } catch (NumberFormatException e1) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e2) {
                try {
                    return Float.parseFloat(value);
                } catch (NumberFormatException e3) {
                    // If none of the above, it is considered a string
                    return value;
                }
            }
        }
    }
}
