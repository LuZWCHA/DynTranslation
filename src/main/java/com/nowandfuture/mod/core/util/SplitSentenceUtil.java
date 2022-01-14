package com.nowandfuture.mod.core.util;

import com.nowandfuture.mod.core.SentencePart;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

public class SplitSentenceUtil {

    private static final Set<Character> digitSet;
    private static final String digitString = "1234567890０１２３４５６７８９";
    private static final char DOT = '.';

    static {
        digitSet = new HashSet<>();

        for (char c : digitString.toCharArray()) {
            digitSet.add(c);
        }
    }


    public static LinkedList<SentencePart> spiltByDigit(@Nonnull String text){
        LinkedList<SentencePart> sentenceParts = new LinkedList<>();
        SentencePart lastSentencePart;

        if(text.length() == 0){
            return sentenceParts;
        }
        short lastFlag = getFlag(text.charAt(0));

        long lastSentenceEnd = 0;

        for (int i = 1; i < text.length(); i ++) {
            char c = text.charAt(i);
            short f = getFlag(c);

            if(f == 1){
                SentencePart part = SentencePart.newPart(lastSentenceEnd, i);
                part.flag = lastFlag;
                sentenceParts.add(part);
                part = SentencePart.newPart(i, ++i);
                part.flag = f;
                sentenceParts.add(part);
                lastSentenceEnd = i;
                //update the flag because of i increase once.
                if(i < text.length()) {
                    f = getFlag(text.charAt(i));
                }
            }else if(f != lastFlag && lastFlag != 1){
                SentencePart part = SentencePart.newPart(lastSentenceEnd, i);
                part.flag = lastFlag;
                sentenceParts.add(part);
                lastSentenceEnd = i;
            }

            lastFlag = f;
        }

        if(lastSentenceEnd < text.length()){
            SentencePart part = SentencePart.newPart(lastSentenceEnd, text.length());
            part.flag = lastFlag;
            sentenceParts.add(part);
        }

        Iterator<SentencePart> it = sentenceParts.iterator();
        lastSentencePart = it.next();
        while (it.hasNext()){
            SentencePart cur = it.next();
            if(lastSentencePart.flag != 2 && cur.flag != 2){
                lastSentencePart.end = cur.end;
                it.remove();
            }else {
                lastSentencePart = cur;
            }
        }

        return sentenceParts;
    }

    public static String getSearchString(LinkedList<SentencePart> sentenceParts, String orgText, @Nonnull ArrayList<String> digitList){

        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (SentencePart part :
                sentenceParts) {
            if (part.flag != 2) {
                digitList.add(part.getString(orgText));
                builder.append("{").append(i).append("}");
                i ++;
            }else{
                builder.append(part.getString(orgText));
            }
        }

        return builder.toString();
    }

    private static boolean isDigit(char c){
        return digitSet.contains(c);
    }

    private static boolean isDot(char c){
        return c == DOT;
    }

    private static short getFlag(char c){
        if(isDigit(c)){
            return 0;
        }else if(isDot(c)){
            return 1;
        }
        return 2;
    }



}
