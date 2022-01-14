package com.nowandfuture.mod.core.util;

import com.nowandfuture.mod.core.ControlChars;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControlCharsUtilTest {

    @Test
    void removeControlChars() {
        String text1 = "{x=1,y=0,scale=1}hi";
        String normal1 = ControlCharsUtil.removeControlChars(text1);
        assertEquals(normal1, "hi");

        String text2 = "{}  hi";
        String normal2 = ControlCharsUtil.removeControlChars(text2);
        assertEquals(normal2, "  hi");

        String text3 = "}{hi";
        String normal3 = ControlCharsUtil.removeControlChars(text3);
        assertEquals(normal3, "}{hi");

        String text4 = "{x}{hi";
        String normal4 = ControlCharsUtil.removeControlChars(text4);
        assertEquals(normal4, "{hi");

        String text5 = "x{x}{hi";
        String normal5 = ControlCharsUtil.removeControlChars(text5);
        assertEquals(normal5, "x{x}{hi");
    }

    @Test
    void getControlChars() {
        String text1 = "{x=1,y=0,scale=1}hi";
        ControlChars controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(new ControlChars(1,0,1), controlChars);

        text1 = "{x=1,y=2}hi";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(new ControlChars(1,2,1), controlChars);

        text1 = "{x=1,y=2hi";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(ControlChars.EMPTY, controlChars);

        text1 = "{}{x=1,y=2}hi";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(ControlChars.EMPTY, controlChars);

        text1 = "";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(ControlChars.EMPTY, controlChars);

        text1 = "{x,y,z}hi";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(ControlChars.EMPTY, controlChars);

        text1 = "{y=2,y=3,}hi";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(new ControlChars(0,3,1), controlChars);

        text1 = "{y=2,x=0    ,    y=3,}hi";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(new ControlChars(0,3,1), controlChars);

        text1 = "{y=2,y=3,scale=1,scale=auto}hi";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(new ControlChars(0,3,1, false,false,true), controlChars);

        text1 = "{y=2,y=3,scale=2,scale=auto}hi";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(new ControlChars(0,3,2, false,false,true), controlChars);

        text1 = "{y=2,y=3,scale=2,,}hi";
        controlChars = ControlCharsUtil.getControlChars(text1);
        assertEquals(new ControlChars(0,3,2), controlChars);
    }
}