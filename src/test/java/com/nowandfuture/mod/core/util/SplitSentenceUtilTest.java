package com.nowandfuture.mod.core.util;

import com.nowandfuture.mod.core.SentencePart;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static com.nowandfuture.mod.core.util.SplitSentenceUtil.spiltByDigit;
import static org.junit.jupiter.api.Assertions.*;

class SplitSentenceUtilTest {

    @Test
    public void testSpiltWords() {
        String text = "aaaa ... bbbb 82jbd";
        LinkedList<SentencePart> s = spiltByDigit(text);
        String[] res = {"aaaa ", "...", " bbbb ", "82", "jbd"};
        int i = 0;
        for (SentencePart p:
                s) {
            assertEquals(res[i++], p.getString(text));
        }
    }

}