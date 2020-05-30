package com.nowandfuture.translation.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {

    /**
     * ChatTranslate : false
     * Enable : true
     * SpiltWords : true
     * RetainOrg : false
     * TranslateApis : [{"name":"baidu","secret":"dadasdasdsa","key":"13123123131"},{"name":"youdao","secret":"dadasdasdsa","key":"13123123131"}]
     */
    @SerializedName("ChatTranslate")
    private boolean ChatTranslate;
    @SerializedName("Enable")
    private boolean Enable;
    @SerializedName("SpiltWords")
    private boolean SpiltWords;
    @SerializedName("RetainOrg")
    private boolean RetainOrg;
    @SerializedName("TranslateApis")
    private List<TranslateApisEntity> TranslateApis;

    public Config(){
        ChatTranslate = false;
        Enable = true;
        SpiltWords = true;
        RetainOrg = false;
        TranslateApis = new ArrayList<>();
    }

    public void setChatTranslate(boolean ChatTranslate) {
        this.ChatTranslate = ChatTranslate;
    }

    public void setEnable(boolean Enable) {
        this.Enable = Enable;
    }

    public void setSpiltWords(boolean SpiltWords) {
        this.SpiltWords = SpiltWords;
    }

    public void setRetainOrg(boolean RetainOrg) {
        this.RetainOrg = RetainOrg;
    }

    public void setTranslateApis(List<TranslateApisEntity> TranslateApis) {
        this.TranslateApis = TranslateApis;
    }

    public boolean isChatTranslate() {
        return ChatTranslate;
    }

    public boolean isEnable() {
        return Enable;
    }

    public boolean isSpiltWords() {
        return SpiltWords;
    }

    public boolean isRetainOrg() {
        return RetainOrg;
    }

    public List<TranslateApisEntity> getTranslateApis() {
        return TranslateApis;
    }

    public class TranslateApisEntity {
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

    //invoke after TranslationManager initMaps
    public static void load(){
        if(TranslationManager.INSTANCE.getConfigDir().exists()){
            File file = createConfigFile();
            Config config = null;
            if(file != null){
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(file);
                    config = new Gson().fromJson(fileReader,Config.class);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }finally {
                    if(fileReader != null) {
                        try {
                            fileReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                boolean firstCreate = false;

                if(config == null){
                    config = new Config();
                    firstCreate = true;
                }

                TranslationManager.INSTANCE.setConfig(config);
                TranslationManager.INSTANCE.initConfig();

                if(firstCreate){
                    save();
                }
            }

        }
    }

    public static File createConfigFile(){
        boolean flag = true;
        File file = new File(TranslationManager.INSTANCE.getConfigDir().getAbsoluteFile() + "/" + FILE_NAME);
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
        Config config = TranslationManager.INSTANCE.buildConfig();

        File file = createConfigFile();
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
