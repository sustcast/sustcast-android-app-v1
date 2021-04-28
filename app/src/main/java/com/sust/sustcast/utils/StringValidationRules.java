package com.sust.sustcast.utils;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Patterns;

public class StringValidationRules {
    public static StringRule NOT_EMPTY = s -> TextUtils.isEmpty(s.toString());
    public static StringRule EMAIL = s -> !android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches();
    public static StringRule PASSWORD = s -> s.length() < 6;
    public static StringRule PHONE = s -> !Patterns.PHONE.matcher(s).matches();
    public static MatchRule CONFIRMPASSWORD = (s,t) -> ! (s.toString().equals(t) && s.length()>0);
    public interface StringRule {
        boolean validate(Editable s);
    }
    public interface MatchRule{
        boolean validate(Editable s, String t);
    }
}
