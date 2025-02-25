package me.mogubea.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.time.Duration;
import java.util.Formatter;

public class LatinSmall {

    private static final LoadingCache<String, String> conversionCache = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(3)).build(LatinSmall::convert);

    public static String translate(String s) {
        return conversionCache.get(s.toUpperCase());
    }

    private static String convert(String s) {
        int len = s.length();
        Formatter smallCaps = new Formatter(new StringBuilder(len));
        for (int i = -1; ++i < len;) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z' && c != 'X') {
                smallCaps.format("%c", Character.codePointOf("LATIN LETTER SMALL CAPITAL " + c));
            } else if (c == 'X') {
                smallCaps.format("%c", 'x');
            } else {
                smallCaps.format("%c", c);
            }
        }
        return smallCaps.toString();
    }

}
