package com.nowandfuture.translation.core;

import com.baidu.translate.demo.TranslateData;
import com.github.houbb.segment.bs.SegmentBs;
import com.github.houbb.segment.support.segment.mode.impl.SegmentModes;
import com.github.houbb.segment.support.segment.result.impl.SegmentResultHandlers;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nowandfuture.translation.DynTranslationMod;
import com.nowandfuture.translation.core.util.SplitSentenceUtil;
import com.nowandfuture.translation.core.util.Utils;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import joptsimple.internal.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public enum TranslationManager {
    INSTANCE;
    private final Queue<String> recordQueue;
    private Set<String> searchSet;
    private FIFOCache<String, TranslateTask> searchWaitingQueue;

    private LRUCache<String, String> translationCache;
    private Set<String> preciseSet;

    private Map<String, Map<String, String>> containerFontMap;
    private File configDir;

    private LanguageDetector languageDetector;
    private TextObjectFactory textObjectFactory;

    private static final int MAX_RECORD_SIZE = 1000;
    private static final String WILDCARD_CHARACTER = "*";
    private static final String CONFIG_DIR_NAME = DynTranslationMod.MOD_ID;
    private static final String JSON_MAP_PREFIX = DynTranslationMod.MOD_ID + "_";
    private static final String JSON_MAP_DEFAULT_NAME = JSON_MAP_PREFIX + "default.json";
    private static final String RECORD_FILE_NAME = "record_";

    private Gui currentGui;
    private IMixinProfiler profiler;

    private boolean enable = false;
    private boolean printFormatChars = true;
    private boolean enableSpiltWords = true;

    private boolean enableChatTranslate = false;
    private boolean retainOrg = false;

    private Config config;

    //false,false -> true,false -> false,true -> false,false
    private final AtomicBoolean start = new AtomicBoolean(false);
    private final AtomicBoolean end = new AtomicBoolean(false);

    //in one thread, the thread will try best to consume the translation task
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    //in one thread, the thread will consume the translation task every one second
    private ScheduledExecutorService scheduledService = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());

    //return immediately once get the translation.
    //any new search task will broke the previous one
    private ExecutorService quickSearchService = Executors.newSingleThreadExecutor();

    //reuse
    private final TranslationRes resObj = new TranslationRes("");

    public File getConfigDir() {
        return configDir;
    }

    public void startRecord() {
        if (!start.get() && !end.get()) {
            recordQueue.clear();
            searchSet.clear();
            start.set(true);
            end.set(false);
            sendMessage(I18n.format("chat.dyntranslation.record.start"));
        }
    }

    public void endRecord() {
        if (start.get() && !end.get()) {
            start.set(false);
            end.set(true);
            sendMessage(I18n.format("chat.dyntranslation.record.saving"));
            saveRecords();
        }
    }


    private final ThreadPoolExecutor IOExecutor;
    private final List<File> loadedFiles;

    TranslationManager() {
        IOExecutor = new ThreadPoolExecutor(1, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        recordQueue = new LinkedList<>();
        translationCache = new LRUCache<>(1000);
        searchWaitingQueue = new FIFOCache<>(1000);
        containerFontMap = new HashMap<>();
        searchSet = new HashSet<>();
        preciseSet = new HashSet<>();
        loadedFiles = new ArrayList<>();
        profiler = (IMixinProfiler) Minecraft.getMinecraft().profiler;

        //load all languages:
        List<LanguageProfile> languageProfiles;
        try {
            languageProfiles = new LanguageProfileReader().readBuiltIn(Languages.getLanguages());
            //build language detector:
            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .minimalConfidence(0.666)
                    .withProfiles(languageProfiles)
                    .build();

            //create a text object factory
            textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        loadMaps();
        loadConfig();
    }

    private void loadMaps() {
        createDir();
        loadFromJsonMaps();
    }

    public void initConfig() {
        this.enable = config.isEnable();
        this.retainOrg = config.isRetainOrg();
        this.enableChatTranslate = config.isChatTranslate();
        this.enableSpiltWords = config.isSpiltWords();
        if (!config.getTranslateApis().isEmpty()) {
            Config.TranslateApisEntity entity = config.getTranslateApis().get(0);
            NetworkTranslateHelper.initApi(entity.getName(), entity.getId(), entity.getKey());
        }
        NetworkTranslateHelper.initNMTApi();
    }

    public void createDir() {
        configDir = new File(Loader.instance().getConfigDir().getAbsolutePath() +
                "/" + CONFIG_DIR_NAME);

        if (!configDir.exists()) {
            try {
                boolean flag = configDir.mkdirs();
                if (!flag) {
                    throw new RuntimeException("dyntranslation direction not created !");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createDefaultJsonMap() {
        File defaultJson = new File(configDir.getAbsolutePath() + "/" + JSON_MAP_DEFAULT_NAME);

        if (!defaultJson.exists()) {
            try {
                boolean flag = defaultJson.createNewFile();
                if (!flag) {
                    throw new RuntimeException("dyntranslation json file not created !");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadConfig() {
        IOExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Config.load();
            }
        });
    }

    public void loadFromJsonMaps() {
        translationCache.clear();
        IOExecutor.execute(new Runnable() {
            String sm = I18n.format("chat.dyntranslation.load.succssful");
            String fm = I18n.format("chat.dyntranslation.load.failed");
            String m = I18n.format("chat.dyntranslation.rule.number");
            private int failedNumber = 0;

            @Override
            public void run() {
                createDir();
                if (configDir.exists() && configDir.isDirectory()) {
                    File[] files = configDir.listFiles((dir, name)
                            -> name.startsWith(JSON_MAP_PREFIX) && name.endsWith(".json"));

                    if (files == null || files.length == 0) {
                        createDefaultJsonMap();
                    }

                    containerFontMap.clear();
                    preciseSet.clear();
                    loadedFiles.clear();

                    if (files != null) {
                        for (File map :
                                files) {
                            final Map<String, Map<String, String>> newMap = loadMap(map);
                            final Map<String, Map<String, String>> replacedMap = new TreeMap<>();

                            if (newMap != null) {

                                //replace the precise-symbol '@'
                                each(newMap, (name, orgText, translation) -> {
                                    //check the "@" number
                                    int count = 0;
                                    for (int i = 0; i < orgText.length(); i++) {
                                        if (orgText.charAt(i) == '@') {
                                            count++;
                                        } else {
                                            break;
                                        }
                                    }

                                    if (count > 0) {

                                        int rmAtNum = (count + 1) / 2;

                                        String actuText = orgText.substring(rmAtNum);

                                        if ((count & 1) == 1) {
                                            preciseSet.add(actuText);
                                        }
                                        //replace @
                                        if (!replacedMap.containsKey(name)) {
                                            replacedMap.put(name, new HashMap<>());
                                        }
                                        replacedMap.get(name).put(orgText, actuText);
                                    }
                                });

                                if (!replacedMap.isEmpty()) {
                                    each(replacedMap, (name, orgText, actuText) -> {
                                        String translation = newMap.get(name).get(orgText);
                                        newMap.get(name).remove(orgText);
                                        newMap.get(name).put(actuText, translation);
                                    });
                                }

                                //combine the map into containerFontMap
                                Utils.combineMaps(newMap, containerFontMap);
                                loadedFiles.add(map);
                            }
                        }
                    }

                    int size = 0;

                    for (Map<String, String> map :
                            containerFontMap.values()) {
                        size += map.size();
                    }

                    if (failedNumber == 0)
                        sendMessage(sm + size + m);
                    else
                        sendMessage(fm + size + m);

                }
            }

            private Map<String, Map<String, String>> loadMap(File mapFile) {

                Map<String, Map<String, String>> json = null;
                try (BufferedReader reader = new BufferedReader(new FileReader(mapFile))) {
                    Gson gson = new GsonBuilder().create();
                    Type stringMapType =
                            new TypeToken<Map<String, Map<String, String>>>() {
                            }.getType();
                    json = gson.fromJson(reader, stringMapType);
                    if (json != null && !json.isEmpty())
                        expandFormatMap(json);
                } catch (Exception e) {
                    failedNumber++;
                    e.addSuppressed(new Exception("translation config can't read successful!"));
                    e.printStackTrace();
                }

                return json;
            }
        });
    }


    public void saveConfig() {
        IOExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Config.save();
            }
        });
    }

    public void saveRecords() {
        IOExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (recordQueue) {
                    File file = new File(configDir.getAbsolutePath() + "/" +
                            RECORD_FILE_NAME + System.currentTimeMillis() + ".json");
                    if (!file.exists()) {
                        try {
                            boolean flag = file.createNewFile();
                            if (!flag) {
                                throw new RuntimeException("record file create failed");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(e.getMessage());
                        }
                    }
                    if (file.exists()) {
                        try (
                                OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)
                        ) {
                            Map<String, Map<String, String>> temp2 = new TreeMap<>();
                            for (String combineName :
                                    recordQueue) {
                                String[] names = spilt(combineName);
                                String containerName = names[0];
                                String text = names[1];

                                if (!temp2.containsKey(containerName)) {
                                    temp2.put(containerName, new HashMap<>());
                                }

                                Map<String, String> temp = temp2.get(containerName);

                                if (text.length() >= 1) {
                                    String noFormat = removeFormat(text);
                                    noFormat = noFormat.replace("\n", "\\n");
                                    String simple = EnCnCharList.extraIdeographicChars(noFormat);
                                    if (simple != null) {
                                        if (printFormatChars) {
                                            temp.put(text, copyFormat(text));
                                        }
                                        temp.put(simple, Strings.EMPTY);
                                    }
                                }
                            }

                            Gson gson = new GsonBuilder()
                                    .setPrettyPrinting()
                                    .create();

                            gson.toJson(temp2, fileWriter);

                            sendMessage(I18n.format("chat.dyntranslation.file.save") + file.getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                            sendMessage(I18n.format("chat.dyntranslation.file.save.failed"));
                        }
                    }

                    start.set(false);
                    end.set(false);
                }
            }
        });
    }

    private void expandFormatMap(Map<String, Map<String, String>> map) {

        Map<String, Map<String, String>> expand = new HashMap<>();

        for (Map.Entry<String, Map<String, String>> pair :
                map.entrySet()) {
            Map<String, String> newMap = new HashMap<>();
            for (Map.Entry<String, String> m :
                    pair.getValue().entrySet()) {
                if (isContainFormat167(m.getKey())) {
                    String newText = removeFormat(m.getKey());
                    String newTranslation = removeFormat(m.getValue());

                    if (!newText.equals(m.getKey())) {
                        newMap.put(newText, newTranslation);
                    }

                }
            }

            expand.put(pair.getKey(), newMap);
        }

        for (Map.Entry<String, Map<String, String>> pair :
                expand.entrySet()) {
            map.get(pair.getKey()).putAll(pair.getValue());
        }

    }

    private boolean isContainFormat167(String text) {
        return text.contains("§");
    }

    private String removeFormat(@Nullable String text) {
        if (text == null) return null;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            char c0 = text.charAt(i);
            if (c0 == 167 && i + 1 < text.length()) {
                ++i;
            } else {
                builder.append(c0);
            }
        }
        return builder.toString();
    }

    private String copyFormat(String text) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            char c0 = text.charAt(i);
            if (c0 == 167 && i + 1 < text.length()) {
                builder.append(c0);
                builder.append(text.charAt(++i));
            }
        }
        return builder.toString();
    }

    public void languageChanged() {
        getNetworkQueue().clear();
        getNetworkDict().clear();
    }

    public TranslationRes translate(String text) {

        String trans;

        boolean renderInChat = "chat".equals(profiler.getCurrentSection()) ||
                (((currentGui instanceof GuiNewChat) || (currentGui instanceof GuiChat)) && profiler.getCurrentSection().isEmpty());

        if (!renderInChat) {
            String containerName = WILDCARD_CHARACTER;
            if (currentGui != null) containerName = currentGui.getClass().getCanonicalName();
            if (!profiler.getCurrentSection().isEmpty() || GuiUtilsAccessor.isTooltipRendering())
                containerName = WILDCARD_CHARACTER;

            String combineName = combine(containerName, text);
            if (start.get() && !end.get() && !searchSet.contains(combineName)) {
                recordQueue.offer(combineName);
                searchSet.add(combineName);

                if (recordQueue.size() > MAX_RECORD_SIZE) {
                    recordQueue.clear();
                    searchSet.clear();
                }
            }

            //get the translation from map
            trans = getTranslation(containerName, text);

            if (trans != null) {
                return resObj.set(ControlCharsUtil.getControlChars(trans), ControlCharsUtil.removeControlChars(trans));
            }

            //to search the translation from the 2nd-cache first
            trans = searchGenTranslation(containerName, text);
            if (trans != null) {
                ControlChars controlChars = ControlCharsUtil.getControlChars(trans);
                trans = ControlCharsUtil.removeControlChars(trans);
                return resObj.set(controlChars, trans);
            }

            //if not find, we try to remove the format strings of the text, and search again
            String strings = removeFormat(text);
            trans = getTranslation(containerName, strings);

            if (strings != null && trans != null) {
                ControlChars controlChars = ControlCharsUtil.getControlChars(trans);
                addGenTranslation(containerName, text, trans);

                if (!controlChars.isEMPTY())
                    trans = ControlCharsUtil.removeControlChars(trans);

                trans = text.replace(strings, trans);

                return resObj.set(controlChars, trans);
            }

            //if still not find it, we try to find it(not formatted),in the 2nd-cache.
            trans = searchGenTranslation(containerName, strings);
            if (trans != null) {
                ControlChars controlChars = ControlCharsUtil.getControlChars(trans);
                if (!controlChars.isEMPTY())
                    trans = ControlCharsUtil.removeControlChars(trans);
                return resObj.set(controlChars, trans);
            }

            //if we still not find it, we try to remove the number and some char such as '-',':','5'
            //we may get the result from the prepared map
            if (strings != null)
                strings = EnCnCharList.extraIdeographicChars(strings);
            trans = getTranslation(containerName, strings);
            if (strings != null && trans != null) {
                ControlChars controlChars = ControlCharsUtil.getControlChars(trans);
                addGenTranslation(containerName, text, trans);

                if (!controlChars.isEMPTY())
                    trans = ControlCharsUtil.removeControlChars(trans);

                trans = text.replace(strings, trans);
                return resObj.set(controlChars, trans);
            }

            //if no translation find, we have to spilt the word to list of words, find the word's translation
            //one by one, if still noting get, to search the result on the Internet;

            //step one, a simple split by "1234567890." for any language, O((k+d)n) time complexity, 2 >= k >= 1, the d value may reach n/2 when nearly every world has to be replace, the worst case may be O(n^2)
            //However, at normal case the replace time will very small, always 1 or 2, and the n dose this too.
            TranslationRes res = getSimpleSpiltTranslation(containerName, removeFormat(text));
            if(res.text != null){
                return resObj.set(res);
            }
            //step two, a slow way to spilt all worlds and search the result, for English it's fast, but for Jap or Chn it will be very slow.
            addTask2Queue(containerName, text);
        }

        return resObj.set(text);
    }

    public String getNetworkTranslateFromCache(String unFormattedText) {
        return getNetworkDict().get(unFormattedText);
    }

    public String translateTextFromNetwork(String text) {
        if (!Minecraft.getMinecraft().isGamePaused()) {
            String result = getNetworkDict().get(text);
            String noFormat;
            if (result == null) {
                noFormat = removeFormat(text);
                result = getNetworkDict().get(noFormat);
            } else {
                return result;
            }

            if (result == null) {
                if (!queue.contains(noFormat)) {
                    queue.offer(noFormat);
                }

                if (!queue.isEmpty()) {
                    submitNetworkTranslationTask();
                }
            } else {
                return result;
            }

            return text;
        }
        return text;
    }

    // TODO: 2022/1/14 rewrite the replace to make it O(n)
    private TranslationRes getSimpleSpiltTranslation(String containerName, String text) {

        LinkedList<SentencePart> sentenceParts = SplitSentenceUtil.spiltByDigit(text);
        ArrayList<String> digitList = new ArrayList<>(2);
        String reformatString = SplitSentenceUtil.getSearchString(sentenceParts, text, digitList);
        String trans = searchLocalTranslation(containerName, reformatString, false);
        ControlChars controlChars = ControlChars.EMPTY;
        if(trans != null){
            controlChars = ControlCharsUtil.getControlChars(trans);

            if (!controlChars.isEMPTY())
                trans = ControlCharsUtil.removeControlChars(trans);

            for (int i = 0; i < digitList.size(); i++) {
                trans = trans.replace("{"+ i + "}", digitList.get(i));
            }
        }

        return new TranslationRes(controlChars, trans);
    }

    public synchronized void addGenTranslation(String containerName, String word, String translation) {
        translationCache.put(combine(containerName, word), translation);
    }

    public String searchGenTranslation(String containerName, String text) {
        String trans = null;
        if (!containerName.equals(WILDCARD_CHARACTER)) {
            trans = translationCache.get(combine(containerName, text));

        } else {
            for (String name :
                    containerFontMap.keySet()) {
                trans = translationCache.get(combine(name, text));
                if (trans != null) break;
            }
        }

        return trans;
    }

    public String searchLocalTranslation(String containerName, String text) {
        return searchLocalTranslation(containerName, text, true);
    }

    public String searchLocalTranslation(String containerName, String text, boolean removeCC) {
        String trans = getTranslation(containerName, text);

        if (trans == null) {
            trans = searchGenTranslation(containerName, text);
        }
        if (removeCC && trans != null) {
            trans = ControlCharsUtil.removeControlChars(trans);
        }
        return trans;
    }

    public boolean containsAtPreciseSet(String word) {
        return preciseSet.contains(word);
    }

    private void addTask2Queue(String containerName, String text) {
        if (Minecraft.getMinecraft().isGamePaused()) {
            return;
        }
        if (enableSpiltWords && !executorService.isShutdown() &&
                !searchWaitingQueue.containsKey(combine(containerName, text))) {
            searchWaitingQueue.put(combine(containerName, text), new TranslateTask(containerName, text));
        }
    }

    private String combine(String containerName, String text) {
        return containerName + "," + text;
    }

    private String[] spilt(String combineName) {
        return combineName.split(",", 2);
    }

    private void sendMessage(String text) {
        if (Minecraft.getMinecraft().player != null)
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("§5<DynTranslation> §7" + text));
    }

    public void setCurrentGui(@Nullable Gui currentGui) {
        this.currentGui = currentGui;
    }

    public Gui getCurrentGui() {
        return currentGui;
    }

    /**
     * submit a translation task to translate service per-tick
     */
    public void submitLocalTranslationTask() {
        if (!enableSpiltWords || Minecraft.getMinecraft().isGamePaused()) {
            return;
        }

        if (!searchWaitingQueue.isEmpty()) {
            if (executorService.isShutdown()) {
                executorService = Executors.newSingleThreadExecutor();
            }
            String text = searchWaitingQueue.keySet().iterator().next();
            TranslateTask translateTask = searchWaitingQueue.get(text);
            searchWaitingQueue.remove(text);
            executorService.submit(translateTask);
        }
    }

    public void submitNetworkTranslationTask() {
        if (scheduledService.isShutdown()) {
            scheduledService = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());
        }
        scheduledService.scheduleAtFixedRate(new NetworkTranslateTask(), 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void stopTranslateThread() {
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
            translationCache.clear();
        }

        if (!scheduledService.isShutdown()) {
            scheduledService.shutdownNow();
            getNetworkQueue().clear();
            getNetworkDict().clear();
        }
    }

    public TextObjectFactory getTextObjectFactory() {
        return textObjectFactory;
    }

    public LanguageDetector getLanguageDetector() {
        return languageDetector;
    }

    public boolean isRetainOrg() {
        return retainOrg;
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isEnableChatTranslate() {
        return enableChatTranslate;
    }

    public void setRetainOrg(boolean retainOrg) {
        this.retainOrg = retainOrg;
        saveConfig();
    }


    public void setEnable(boolean enable) {
        this.enable = enable;
        String chat = enable ? I18n.format("chat.dyntranslation.enable") :
                I18n.format("chat.dyntranslation.disable");
        sendMessage(chat);
        saveConfig();
    }

    public void setEnableSpiltWords(boolean enableSpiltWords) {
        if (!enableSpiltWords && this.enableSpiltWords)
            searchWaitingQueue.clear();
        if (this.enableSpiltWords != enableSpiltWords) {
            this.enableSpiltWords = enableSpiltWords;
            translationCache.clear();
            String message = enableSpiltWords ?
                    I18n.format("chat.dyntranslation.spilt.enable") :
                    I18n.format("chat.dyntranslation.spilt.disable");
            sendMessage(message);
            saveConfig();
        }
    }

    public void setEnableChatTranslate(boolean enableChatTranslate) {
        if (!enableChatTranslate) {
            getNetworkQueue().clear();
        }
        if (enableChatTranslate != this.enableChatTranslate) {
            this.enableChatTranslate = enableChatTranslate;
            sendMessage(I18n.format(enableChatTranslate ? "chat.dyntranslation.chat.translate.enable" :
                    "chat.dyntranslation.chat.translate.disable"));
            saveConfig();
        }
    }

    public void setPrintFormatChars(boolean printFormatChars) {
        this.printFormatChars = printFormatChars;
        saveConfig();
    }

    public Config buildConfig() {
        config.setChatTranslate(enableChatTranslate);
        config.setEnable(enable);
        config.setRetainOrg(retainOrg);
        config.setSpiltWords(enableSpiltWords);
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public interface IVisitor {
        void onVisit(String name, String orgText, String translation);
    }

    private static void each(Map<String, Map<String, String>> source, IVisitor visitor) {
        source.entrySet().forEach(new Consumer<Map.Entry<String, Map<String, String>>>() {
            @Override
            public void accept(Map.Entry<String, Map<String, String>> stringMapEntry) {
                stringMapEntry.getValue().forEach((key, value) -> {
                    visitor.onVisit(stringMapEntry.getKey(), key, value);
                });
            }
        });
    }

    private String getTranslation(@Nonnull String containerName, String text) {
        Map<String, String> stringMap = containerFontMap.get(containerName);
        if (stringMap != null) return stringMap.get(text);
        stringMap = containerFontMap.get(WILDCARD_CHARACTER);
        if (stringMap != null) return stringMap.get(text);
        return null;
    }


    //-----------------------------------------------------------------------------------------------

    private final Map<String, String> wordDict = new LRUCache<>(50);
    private final LinkedBlockingDeque<String> queue = new FixedSizeBlockingDeque<>(100);

    public Map<String, String> getNetworkDict() {
        return wordDict;
    }

    public LinkedBlockingDeque<String> getNetworkQueue() {
        return queue;
    }

    private static class TranslateTask implements Runnable {
        private String containerName, text;
        TranslationManager manager = TranslationManager.INSTANCE;

        public TranslateTask(String containerName, String text) {
            this.containerName = containerName;
            this.text = text;
        }

        @Override
        public void run() {

            if (EnCnCharList.extraIdeographicChars(text) != null) {

                String language = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
                List<String> resultList = new LinkedList<>();
                boolean separator;
                if (language.startsWith("zh_") || language.startsWith("ja_")) {
                    separator = false;
                } else if (language.startsWith("en_")) {
                    separator = true;
                } else {
                    separator = true;
                }

                if (manager.getLanguageDetector() != null) {
                    TextObject textObject = manager.getTextObjectFactory().forText(text);
                    Optional<LdLocale> result = manager.languageDetector.detect(textObject);
                    if (result.isPresent()) {
                        LdLocale ldLocale = result.get();
                        if (ldLocale.getLanguage().startsWith("zh")) {
                            //Chinese
                            resultList = SegmentBs.newInstance()
                                    .segmentMode(SegmentModes.greedyLength())
                                    .segment(text, SegmentResultHandlers.word());
                        } else if (ldLocale.getLanguage().startsWith("en")) {
                            //English
                            resultList = Lists.newArrayList(text.split(" "));
                        } else if (ldLocale.getLanguage().startsWith("ja")) {
                            //do nothing
                        }
                    }
                }

                StringBuilder reconstructString = new StringBuilder();
                boolean flag = false, first = true;
                if (!resultList.isEmpty()) {
                    for (String word :
                            resultList) {
                        if (first) first = false;
                        else {
                            if (separator) reconstructString.append(' ');
                        }

                        if (manager.containsAtPreciseSet(word)) {
                            reconstructString.append(word);
                        } else {
                            String trans = manager.searchLocalTranslation(containerName, word);
                            if (trans == null) {
//                                word = manager.removeFormat(word);
//                                word = EnCnCharList.extraIdeographicChars(word);
//                                if(word != null) {
//                                    trans = getTranslationFromNetwork(word, "auto", "zh");
//                                }
                                if (trans == null) {
                                    trans = word;
                                } else {
                                    flag = true;
                                }
                            } else {
                                flag = true;
                            }
                            reconstructString.append(trans);
                        }
                    }
                }

                if (flag)
                    manager.addGenTranslation(containerName, text, reconstructString.toString());
            }
        }
    }

    private static class NetworkTranslateTask implements Runnable {
        TranslationManager manager = TranslationManager.INSTANCE;

        public NetworkTranslateTask() {

        }

        @Override
        public void run() {
//            if (!manager.getNetworkQueue().isEmpty()) {

                try {
                    String text = manager.getNetworkQueue().poll(2000, TimeUnit.MILLISECONDS);
                    String language = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();

                    String json = NetworkTranslateHelper.translate(text, "auto", language.toLowerCase().substring(0, 2));
                    if (json.contains("error")) {

                    } else {
                        Gson gson = new Gson();
                        TranslateData translateData = gson.fromJson(json, TranslateData.class);
                        if (!translateData.getTransResult().isEmpty()) {
                            manager.getNetworkDict().put(text, translateData.getTransResult().get(0).getDst());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException ignored) {

                }
        }

//        }

    }

}
