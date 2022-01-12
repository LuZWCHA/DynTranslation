package com.nowandfuture.mod.core.util;

import java.util.HashSet;
import java.util.Set;

public class EnCnCharList {
    private final static String en_chars = "0123456789§#$%&\"\'()*+,-./:;<=>?@[\\]^_`{|}~\t\0\b";//?!
    private final static String zh_chars = "０１２３４５６７８９｀－＝【】、；‘’，。／＼～！＠＃￥％…＆×（）—＋｛｝｜：“”《》？〈〉＜＞〔〕〖〗『』";
    private final static Set<Character> searchSet;
    static {
        searchSet = new HashSet<>();
        init();
    }
    private static void init(){
        for (int i = 0;i<en_chars.length();i++) {
            searchSet.add(en_chars.charAt(i));
        }
        for (int i = 0;i<zh_chars.length();i++) {
            searchSet.add(zh_chars.charAt(i));
        }
    }

    public static boolean check(char c){
        return searchSet.contains(c);
    }

    public static String extraIdeographicChars(String text){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(!check(c))
                builder.append(c);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

}
