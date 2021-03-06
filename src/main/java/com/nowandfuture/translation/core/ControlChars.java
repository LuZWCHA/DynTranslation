package com.nowandfuture.translation.core;

public class ControlChars {
    private int offsetX;
    private int offsetY;
    private float scale;
    private boolean autoOffsetX;
    private boolean autoOffsetY;

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public float getScale() {
        return scale;
    }

    public boolean isAutoOffsetX() {
        return autoOffsetX;
    }

    public boolean isAutoOffsetY() {
        return autoOffsetY;
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    private boolean autoScale;

    public ControlChars(int offsetX, int offsetY, float scale, boolean autoOffsetX, boolean autoOffsetY, boolean autoScale) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
        this.autoOffsetX = autoOffsetX;
        this.autoOffsetY = autoOffsetY;
        this.autoScale = autoScale;
    }

    public ControlChars(int offsetX, int offsetY, float scale) {
        this(offsetX,offsetY,scale,false,false,false);
    }

    public ControlChars() {
        this(0,0,1f,true,true,true);
    }

    public static final ControlChars EMPTY = new ControlChars();

    public boolean isEMPTY(){
        return EMPTY == this;
    }
}
