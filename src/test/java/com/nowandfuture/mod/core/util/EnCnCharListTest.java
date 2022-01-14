package com.nowandfuture.mod.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnCnCharListTest {

    @Test
    void extraIdeographicChars() {
        String real = "hi world!";
        assertEquals(real, EnCnCharList.extraIdeographicChars("hi$%, world0!12.&"));
        assertEquals(real, EnCnCharList.extraIdeographicChars("hi$%, wor2ld012.&!"));
        assertEquals(real, EnCnCharList.extraIdeographicChars("h】i$%, wor2ld0７!８12.&"));
        assertNull(EnCnCharList.extraIdeographicChars("０１２３４５６７８９｀－＝【】、；‘’，。／＼～！＠＃￥％…＆×（）—＋｛｝｜：“”《》？〈〉＜＞〔〕〖〗『』"));

    }
}