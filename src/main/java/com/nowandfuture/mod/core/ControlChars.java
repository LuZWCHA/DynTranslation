package com.nowandfuture.mod.core;

public class ControlChars {
    private final int offsetX;
    private final int offsetY;
    private final float scale;
    private final boolean autoOffsetX;
    private final boolean autoOffsetY;

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

    private final boolean autoScale;

    public ControlChars(int offsetX, int offsetY, float scale, boolean autoOffsetX, boolean autoOffsetY, boolean autoScale) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
        this.autoOffsetX = autoOffsetX;
        this.autoOffsetY = autoOffsetY;
        this.autoScale = autoScale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ControlChars that = (ControlChars) o;

        if (offsetX != that.offsetX) return false;
        if (offsetY != that.offsetY) return false;
        if (Float.compare(that.scale, scale) != 0) return false;
        if (autoOffsetX != that.autoOffsetX) return false;
        if (autoOffsetY != that.autoOffsetY) return false;
        return autoScale == that.autoScale;
    }

    @Override
    public int hashCode() {
        int result = offsetX;
        result = 31 * result + offsetY;
        result = 31 * result + (scale != +0.0f ? Float.floatToIntBits(scale) : 0);
        result = 31 * result + (autoOffsetX ? 1 : 0);
        result = 31 * result + (autoOffsetY ? 1 : 0);
        result = 31 * result + (autoScale ? 1 : 0);
        return result;
    }

    public ControlChars(int offsetX, int offsetY, float scale) {
        this(offsetX,offsetY,scale,false,false,false);
    }

    public ControlChars() {
        this(0,0,1f,true,true,true);
    }

    public static final ControlChars EMPTY = new ControlChars();

    public boolean isEmpty(){
        return EMPTY == this;
    }
}
