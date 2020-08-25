package com.nowandfuture.mod.gui;

import net.minecraft.client.gui.widget.button.CheckboxButton;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class ExtendedCheckButton extends CheckboxButton {

    private final List<OnCheckedStateChanged> listeners;

    public ExtendedCheckButton(int xIn, int yIn, int widthIn, int heightIn, String msg, boolean isChecked) {
        super(xIn, yIn, widthIn, heightIn, msg, isChecked);
        listeners = new LinkedList<>();
    }

    public void addListener(@Nonnull OnCheckedStateChanged checkedStateChanged){
        if(!listeners.contains(checkedStateChanged))
            listeners.add(checkedStateChanged);
    }

    public void removeListener(@Nonnull OnCheckedStateChanged checkedStateChanged){
        listeners.remove(checkedStateChanged);
    }

    public interface OnCheckedStateChanged{
        void onChanged(boolean checked);
    }

    @Override
    public void onPress() {
        boolean temp = isChecked();
        super.onPress();
        if(temp != isChecked()){
            for (OnCheckedStateChanged c :
                    listeners) {
                c.onChanged(isChecked());
            }
        }
    }
}
