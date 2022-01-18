package com.nowandfuture.mod.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Config {

    /**
     * ChatTranslate : false
     * Enable : true
     * SpiltWords : true
     * RetainOrg : false
     * DisplayNumber: 3
     * TranslateApis : [{"name":"baidu","secret":"dadasdasdsa","key":"13123123131"},{"name":"youdao","secret":"dadasdasdsa","key":"13123123131"}]
     */
    @SerializedName("ChatTranslate")
    private boolean chatTranslate;
    @SerializedName("Enable")
    private boolean enable;
    @SerializedName("SpiltWords")
    private boolean spiltWords;
    @SerializedName("RetainOrg")
    private boolean retainOrg;
    @SerializedName("DisplayNumber")
    private int displayNumber;
    @SerializedName("TranslateApis")
    private List<TranslateApisEntity> translateApisEntities;

    public Config(){
        chatTranslate = false;
        enable = true;
        spiltWords = true;
        retainOrg = false;
        displayNumber = 3;
        translateApisEntities = new ArrayList<>();
    }

    public void setChatTranslate(boolean ChatTranslate) {
        this.chatTranslate = ChatTranslate;
    }

    public void setEnable(boolean Enable) {
        this.enable = Enable;
    }

    public void setSpiltWords(boolean SpiltWords) {
        this.spiltWords = SpiltWords;
    }

    public void setRetainOrg(boolean RetainOrg) {
        this.retainOrg = RetainOrg;
    }

    public void setTranslateApis(List<TranslateApisEntity> TranslateApis) {
        this.translateApisEntities = TranslateApis;
    }

    public boolean isChatTranslate() {
        return chatTranslate;
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isSpiltWords() {
        return spiltWords;
    }

    public boolean isRetainOrg() {
        return retainOrg;
    }

    public List<TranslateApisEntity> getTranslateApis() {
        return translateApisEntities;
    }

    public int getDisplayNumber() {
        return displayNumber;
    }

    public void setDisplayNumber(int displayNumber) {
        this.displayNumber = displayNumber;
    }

    public static class TranslateApisEntity {
        /**
         * name : baidu
         * secret : dadasdasdsa
         * key : 13123123131
         */
        @SerializedName("name")
        private String name;
        @SerializedName("id")
        private String id;
        @SerializedName("key")
        private String key;

        public void setName(String name) {
            this.name = name;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getKey() {
            return key;
        }
    }

    private static final String FILE_NAME = "config.json";


    //invoke after TranslationManager#initMaps
    public static void load(){
        if(TranslationManager.INSTANCE.getConfigDir().exists()){
            final File file = tryCreateConfigFile();
            Config config = null;
            if(file != null){
                try (FileReader fileReader = new FileReader(file)){
                    config = new Gson().fromJson(fileReader,Config.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                boolean firstCreate = false;

                if(config == null){
                    config = new Config();
                    firstCreate = true;
                }

                TranslationManager.INSTANCE.setConfig(config);
                TranslationManager.INSTANCE.loadFromConfig();

                if(firstCreate){
                    save();
                }
            }

        }
    }

    public static File tryCreateConfigFile(){
        boolean flag = true;
        final File file = Paths.get(TranslationManager.INSTANCE.getConfigDir().getAbsolutePath(), FILE_NAME).toFile();
        if(!file.exists() || file.isDirectory()){
            try {
                flag = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                flag = false;
            }
        }
        return flag ? file : null;
    }

    public static void save(){
        final Config config = TranslationManager.INSTANCE.save2Config();
        final File file = tryCreateConfigFile();
        if(file != null){
            try (FileWriter fileWriter = new FileWriter(file)){

                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                gson.toJson(config,fileWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
