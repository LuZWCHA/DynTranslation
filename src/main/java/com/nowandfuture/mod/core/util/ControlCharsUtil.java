package com.nowandfuture.mod.core.util;

import com.nowandfuture.mod.core.ControlChars;

public class ControlCharsUtil {
    private final static String X_VALUE = "x";
    private final static String Y_VALUE = "y";
    private final static String SCALE_VALUE = "scale";

    private final static String AUTO_STR = "auto";

    public static String removeControlChars(String text){
        if(text.startsWith("{")){
            int index = text.indexOf("}");
            if(index > 0){
                return text.substring(index + 1);
            }
        }
        return text;
    }

    public static ControlChars getControlChars(String text){
        if(text.startsWith("{")){
            int index = text.indexOf("}");
            if(index > 1){
                return getChars(text.substring(1,index));
            }
        }
        return ControlChars.EMPTY;
    }

    private static ControlChars getChars(String text){
        String[] splitText = text.split(",");
        if(splitText.length == 0){
            splitText = new String[]{text};
        }

        boolean error = false;
        int x = 0,y = 0;
        float scale = 1;
        boolean autoX = false,autoY = false,autoS = false;
        for (String property :
                splitText) {
            if(property.length() > 1){
                String[] propertyPair = property.split("=");
                if(propertyPair.length == 2){
                    String left = propertyPair[0].trim();
                    String right = propertyPair[1].trim();

                    try {
                        switch (left){
                            case X_VALUE:
                                if(AUTO_STR.equals(right)){
                                    autoX = true;
                                }else{
                                    x = Integer.parseInt(right);
                                }
                                break;
                            case Y_VALUE:
                                if(AUTO_STR.equals(right)){
                                    autoY = true;
                                }else{
                                    y = Integer.parseInt(right);
                                }
                                break;
                            case SCALE_VALUE:
                                if(AUTO_STR.equals(right)){
                                    autoS = true;
                                }else{
                                    scale = Float.parseFloat(right);
                                }
                                break;
                        }
                    }catch (NumberFormatException e){
                        error = true;
                        break;
                    }

                }else{
                    error = true;
                    break;
                }
            }else {
                error = true;
                break;
            }
        }

        return error ? ControlChars.EMPTY : new ControlChars(x,y,scale,autoX,autoY,autoS);

    }

}
