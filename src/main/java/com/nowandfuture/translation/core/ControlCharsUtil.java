package com.nowandfuture.translation.core;

public class ControlCharsUtil {

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
                            case "x":
                                if(right.equals("auto")){
                                    autoX = true;
                                }else{
                                    x = Integer.parseInt(right);
                                }
                                break;
                            case "y":
                                if(right.equals("auto")){
                                    autoY = true;
                                }else{
                                    y = Integer.parseInt(right);
                                }
                                break;
                            case "scale":
                                if(right.equals("auto")){
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
