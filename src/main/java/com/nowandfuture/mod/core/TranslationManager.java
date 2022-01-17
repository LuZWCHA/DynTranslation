package com.nowandfuture.mod.core;

import com.baidu.translate.demo.TranslateData;
import com.github.houbb.segment.bs.SegmentBs;
import com.github.houbb.segment.support.segment.mode.impl.SegmentModes;
import com.github.houbb.segment.support.segment.result.impl.SegmentResultHandlers;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nowandfuture.mod.DynTranslationMod;
import com.nowandfuture.mod.core.api.GuiUtilsAccessor;
import com.nowandfuture.mod.core.api.IMixinProfiler;
import com.nowandfuture.mod.core.api.ITranslateApi;
import com.nowandfuture.mod.core.cache.FIFOCache;
import com.nowandfuture.mod.core.cache.LRUCache;
import com.nowandfuture.mod.core.forgeimpl.ForgeInterfaces;
import com.nowandfuture.mod.core.mcapi.IMinecraftInterfaces;
import com.nowandfuture.mod.core.util.*;
import com.nowandfuture.translate.MyNMTTransApi;
import com.optimaize.langdetect.DetectedLanguage;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.nowandfuture.mod.core.forgeimpl.MinecraftUtil.*;

//import net.minecraft.client.Minecraft;

public enum TranslationManager {
    INSTANCE;

    //---------------------- Forge and Minecraft API --------------------------
    private final static IMinecraftInterfaces.IMinecraftGame game = new ForgeInterfaces.ForgeMinecraftGame();
    private final static IMinecraftInterfaces.ILanguage languageManager = new ForgeInterfaces.ForgeLanguage();

    private final IMinecraftInterfaces.IProfiler profilerGet = new ForgeInterfaces.ForgeProfiler();
    private final IMinecraftInterfaces.IResource resourceGet = new ForgeInterfaces.FMLResource();


    private final Queue<String> recordQueue;
    //mark weather the words are appeared at the record queue
    private final Set<String> markSet;

    //cache of map and extended-words map
    private final LRUCache<String, String> translationCache;
    //precise match set
    private final Set<String> preciseMatchSet;

    //cache the task to be search by other resolutions
    private final FIFOCache<String, TranslateTask> translateTaskQueue;

    private Map<String, Map<String, String>> containerFontMap;
    private File configDir;

    private LanguageDetector languageDetector;
    private TextObjectFactory textObjectFactory;

    private static final int MAX_RECORD_SIZE = 1000;
    private static final int MAX_CACHE_SIZE = 1000;
    private static final String WILDCARD_CHARACTER = "*";
    private static final String CONFIG_DIR_NAME = DynTranslationMod.MOD_ID;
    private static final String JSON_MAP_PREFIX = DynTranslationMod.MOD_ID + "_";
    private static final String RECORD_FILE_NAME = "record_";

    private static final String DEFAULT_CONTENT = "{\n\"" + WILDCARD_CHARACTER + "\":{}\n}";
    private static final String JSON_POSTFIX = ".json";
    private static final String JSON_MAP_DEFAULT_NAME = JSON_MAP_PREFIX + "default" + JSON_POSTFIX;
    private static final Character AT = '@';
    private static final String PROFILER_CHAT = "chat";

    private final AtomicBoolean doTranslate = new AtomicBoolean(true);
    private final AtomicReference<Class> toastClazz = new AtomicReference<>(null);

    private Object currentGui;
    private final IMixinProfiler profiler;
    private ArrayList<File> fileList;

    private boolean enable = false;
    private boolean printFormatChars = true;
    private boolean enableSpiltWords = true;
    private int displayNumber;

    private boolean enableChatTranslate = false;
    private boolean retainOrg = false;

    private Config config;

    //false,false -> true,false -> false,true -> false,false
    private final AtomicBoolean start = new AtomicBoolean(false),
            end = new AtomicBoolean(false);

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ScheduledExecutorService scheduledService = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());

    //reuse
    private final TranslationRes resObj = new TranslationRes("");

    public ArrayList<File> getFileList() {
        return fileList;
    }

    public File getConfigDir() {
        return configDir;
    }

    public boolean isRecording() {
        return start.get() && !end.get();
    }

    public void startRecord() {
        if (!start.get() && !end.get()) {
            recordQueue.clear();
            markSet.clear();
            start.set(true);
            end.set(false);
            notify(languageManager.getTranslation("chat.dyntranslation.record.start"));
        }
    }

    public void endRecord() {
        if (start.get() && !end.get()) {
            start.set(false);
            end.set(true);
            notify(languageManager.getTranslation("chat.dyntranslation.record.saving"));
            saveRecords();
        }
    }

    private final ThreadPoolExecutor IOExecutor;

    private final static double MIN_CONFIDENCE = .666f;

    TranslationManager() {
        IOExecutor = new ThreadPoolExecutor(1, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        recordQueue = new LinkedList<>();
        translationCache = new LRUCache<>(MAX_CACHE_SIZE);
        translateTaskQueue = new FIFOCache<>(MAX_CACHE_SIZE);
        containerFontMap = new HashMap<>();
        markSet = new HashSet<>();
        preciseMatchSet = new HashSet<>();
        profiler = profilerGet.get();

        //load all languages:
        List<LanguageProfile> languageProfiles;
        try {
            languageProfiles = new LanguageProfileReader().readBuiltIn(Languages.getLanguages());
            //build language detector:
            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .minimalConfidence(MIN_CONFIDENCE)
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
        createConfigDir();
        loadFromJsonMaps();
    }

    public void initConfig() {
        this.enable = config.isEnable();
        this.retainOrg = config.isRetainOrg();
        this.enableChatTranslate = config.isChatTranslate();
        this.enableSpiltWords = config.isSpiltWords();
        this.displayNumber = config.getDisplayNumber();
        loadTranslationApiBy(config);
    }

    private void loadTranslationApiBy(Config config) {
        if (!config.getTranslateApis().isEmpty()) {
            Config.TranslateApisEntity entity = config.getTranslateApis().get(0);
            NetworkTranslateHelper.initApi(entity.getName(), entity.getId(), entity.getKey());
        }
        NetworkTranslateHelper.initNMTApi();
    }

    public void createConfigDir() {
        configDir = new File(resourceGet.getConfigPath().toAbsolutePath() + "/" + CONFIG_DIR_NAME);

        if (!configDir.exists()) {
            try {
                boolean flag = configDir.mkdirs();
                if (!flag) {
                    throw new RuntimeException("dyntranslation folder not created !");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Optional<File> createDefaultJsonMap() {
        File defaultJson = new File(configDir.getAbsolutePath() + "/" + JSON_MAP_DEFAULT_NAME);
        int error = 0;
        if (!defaultJson.exists()) {
            try {
                boolean flag = defaultJson.createNewFile();
                if (!flag) {
                    throw new RuntimeException("dyntranslation json file was not created!");
                } else {
                    try (FileWriter writer = new FileWriter(defaultJson)) {
                        writer.append(DEFAULT_CONTENT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        error++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                error++;
            }
        }

        return error > 0 ? Optional.empty() : Optional.of(defaultJson);
    }

    public void loadConfig() {
        IOExecutor.execute(Config::load);
    }

    public void loadFromJsonMaps() {
        translationCache.clear();
        IOExecutor.execute(new Runnable() {

            final String sm = languageManager.getTranslation("chat.dyntranslation.load.successful");
            final String fm = languageManager.getTranslation("chat.dyntranslation.load.failed");
            final String nm = languageManager.getTranslation("chat.dyntranslation.rule.number");
            private int errorCount = 0;

            @Override
            public void run() {
                createConfigDir();
                if (configDir.exists() && configDir.isDirectory()) {
                    File[] files = configDir.listFiles((dir, name) -> name.startsWith(JSON_MAP_PREFIX) && name.endsWith(JSON_POSTFIX));
                    if (files == null) files = new File[0];

                    fileList = Lists.newArrayList(files);

                    if (fileList.isEmpty()) {
                        Optional<File> df = createDefaultJsonMap();
                        df.ifPresent(fileList::add);
                    }

                    containerFontMap.clear();
                    preciseMatchSet.clear();

                    for (File mapFile :
                            fileList) {
                        Map<String, Map<String, String>> newMap = loadMap(mapFile);
                        Map<String, Map<String, String>> replacedMap = new TreeMap<>();
                        if (newMap != null) {

                            //replace the precise-symbol '@'
                            each(newMap, (name, orgText, translation) -> {
                                //check the "@" number
                                int count = 0;
                                for (int i = 0; i < orgText.length(); i++) {
                                    if (orgText.charAt(i) == AT) {
                                        count++;
                                    } else {
                                        break;
                                    }
                                }

                                if (count > 0) {

                                    int rmAtNum = (count + 1) / 2;

                                    String actuText = orgText.substring(rmAtNum);

                                    if ((count & 1) == 1) {
                                        preciseMatchSet.add(actuText);
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

                        } else {
                            errorCount++;
                        }
                    }

                    if (containerFontMap == null) containerFontMap = new HashMap<>();

                    int size = 0;
                    for (Map<String, String> map :
                            containerFontMap.values()) {
                        size += map.size();
                    }

                    if (errorCount == 0)
                        TranslationManager.this.notify(sm + size + nm);
                    else
                        TranslationManager.this.notify(fm + size + nm);

                }
            }

            private Map<String, Map<String, String>> loadMap(File mapFile) {

                Map<String, Map<String, String>> json = null;
                try (BufferedReader reader = new BufferedReader(new FileReader(mapFile))) {
                    Gson gson = new Gson();
                    Type stringMapType =
                            new TypeToken<Map<String, Map<String, String>>>() {
                            }.getType();
                    json = gson.fromJson(reader, stringMapType);
                    if (json != null && !json.isEmpty())
                        expandFormatMap(json);
                } catch (Exception e) {
                    errorCount++;
                    e.addSuppressed(new Exception("translation config can't be read!"));
                    e.printStackTrace();
                }

                return json;
            }
        });
    }

    public void saveConfig() {
        IOExecutor.execute(Config::save);
    }

    public void saveRecords() {
        IOExecutor.execute(() -> {
            File file = new File(configDir.getAbsolutePath() + "/" + RECORD_FILE_NAME + System.currentTimeMillis() + JSON_POSTFIX);
            if (!file.exists()) {
                try {
                    boolean flag = file.createNewFile();
                    if (!flag) {
                        throw new RuntimeException("record file create failed!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TranslationManager.this.notify(e.getMessage());
                }
            }

            synchronized (recordQueue) {
                if (file.exists()) {
                    try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
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
                                if (noFormat == null) continue;
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

                        TranslationManager.this.notify(languageManager.getTranslation("chat.dyntranslation.file.save") + file.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                        TranslationManager.this.notify(languageManager.getTranslation("chat.dyntranslation.file.save.failed"));

                    }
                }

                start.set(false);
                end.set(false);
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

                    if (newText != null && !newText.equals(m.getKey())) {
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

    public void languageChanged() {
        getNetworkQueue().clear();
        getNetworkCache().clear();
    }

    public TranslationRes translate(String text) {

        String trans;

        String curSection = profiler.getCurrentSection();
        boolean renderInChat = PROFILER_CHAT.equals(curSection) ||
                ((game.isChatScreen(currentGui)));
        boolean isRenderToast = toastClazz.get() != null;

        if (!renderInChat && doTranslate.get()) {
            String containerName = WILDCARD_CHARACTER;
            if (currentGui != null) {
                containerName = currentGui.getClass().getCanonicalName();
            } else if (isRenderToast) {
                containerName = toastClazz.get().getCanonicalName();
            } else if (GuiUtilsAccessor.isTooltipRendering()) {
                containerName = WILDCARD_CHARACTER;
            }

            String combineName = combine(containerName, text);
            if (start.get() && !end.get() && !markSet.contains(combineName)) {
                recordQueue.offer(combineName);
                markSet.add(combineName);

                if (recordQueue.size() > MAX_RECORD_SIZE) {
                    recordQueue.clear();
                    markSet.clear();
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

                if (!controlChars.isEmpty())
                    trans = ControlCharsUtil.removeControlChars(trans);

                trans = text.replace(strings, trans);

                return resObj.set(controlChars, trans);
            }

            //if still not find it, we try to find it(not formatted),in the 2nd-cache.
            trans = searchGenTranslation(containerName, strings);
            if (trans != null) {
                ControlChars controlChars = ControlCharsUtil.getControlChars(trans);
                if (!controlChars.isEmpty())
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

                if (!controlChars.isEmpty())
                    trans = ControlCharsUtil.removeControlChars(trans);

                trans = text.replace(strings, trans);
                return resObj.set(controlChars, trans);
            }

            //if no translation find, we have to spilt the word to list of words, find the word's translation
            //one by one, if still noting get, to search the result on the Internet;

            //step one, a simple split by "1234567890." for any language, O((k+d)n) time complexity, 2 >= k >= 1, the d value may reach n/2 when nearly every world has to be replace, the worst case may be O(n^2)
            //However, at normal case the replace time will very small, always 1 or 2, and the n dose this too.
            TranslationRes res = getSimpleSpiltTranslation(containerName, text);
            if (res.text != null) {
                return resObj.set(res);
            }
            //step two, a slow way to spilt all worlds and search the result, for English it's fast, but for Jap or Chn it will be very slow.
            searchTranslation(containerName, text);
        }

        return resObj.notTranslated();
    }

    // TODO: 2022/1/14 rewrite the replace to make it O(n)
    private TranslationRes getSimpleSpiltTranslation(String containerName, String text) {

        LinkedList<SentencePart> sentenceParts = SplitSentenceUtil.spiltByDigit(text);
        ArrayList<String> digitList = new ArrayList<>(2);
        String reformatString = SplitSentenceUtil.getSearchString(sentenceParts, text, digitList);
        String trans = searchLocalTranslation(containerName, reformatString, false);
        ControlChars controlChars = ControlChars.EMPTY;
        if (trans != null) {
            controlChars = ControlCharsUtil.getControlChars(trans);

            if (!controlChars.isEmpty())
                trans = ControlCharsUtil.removeControlChars(trans);

            for (int i = 0; i < digitList.size(); i++) {
                trans = trans.replace("{" + i + "}", digitList.get(i));
            }
        }

        return new TranslationRes(controlChars, trans);
    }

    public String getNetworkTranslate(String unformattedText) {
        return getNetworkCache().get(unformattedText);
    }

    public Optional<String> translateChatText(String text) {
        if (!game.isGamePause()) {
            String result = getNetworkCache().get(text);
            String noFormat;

            if (result == null) {
                noFormat = removeFormat(text);
                result = getNetworkCache().get(noFormat);
            } else {
                return Optional.of(result);
            }

            if (result == null) {
                if (!networkQueue.contains(noFormat)) {
                    networkQueue.offer(noFormat);
                }
                if (!networkQueue.isEmpty()) {
                    submitNetworkTask();
                }

            } else {
                return Optional.of(result);
            }

            return Optional.empty();
        }
        return Optional.empty();
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
        return preciseMatchSet.contains(word);
    }

    private void searchTranslation(String containerName, String text) {
        if (game.isGamePause()) {
            return;
        }

        if (enableSpiltWords && !executorService.isShutdown() &&
                !translateTaskQueue.containsKey(combine(containerName, text))) {
            translateTaskQueue.put(combine(containerName, text), new TranslateTask(containerName, text));
        }
    }

    private String combine(String containerName, String text) {
        return containerName + "," + text;
    }

    private String[] spilt(String combineName) {
        return combineName.split(",", 2);
    }

    private String title = "DynTranslation";

    private void notify(String text) {
        if (game.isWorldCreated()) {
            title = languageManager.getTranslation("string.dyntranslation.name");
            game.notifyByToast(title, text);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCurrentGui(@Nullable Object currentGui) {
        this.currentGui = currentGui;
    }

    public Object getCurrentGui() {
        return currentGui;
    }

    public void submitTask() {
        if (!enableSpiltWords) {
            translateTaskQueue.clear();
            return;
        }
        if (game.isGamePause()) {
            return;
        }

        if (!translateTaskQueue.isEmpty()) {
            if (executorService.isShutdown()) {
                executorService = Executors.newSingleThreadExecutor();
            }
            String text = translateTaskQueue.keySet().iterator().next();
            TranslateTask translateTask = translateTaskQueue.get(text);
            translateTaskQueue.remove(text);
            executorService.submit(translateTask);
        }
    }

    public void submitNetworkTask() {
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
            getNetworkCache().clear();
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
        String chat = enable ? languageManager.getTranslation("chat.dyntranslation.enable") :
                languageManager.getTranslation("chat.dyntranslation.disable");
        notify(chat);
        saveConfig();
    }

    public void setEnableSpiltWords(boolean enableSpiltWords) {
        if (!enableSpiltWords && this.enableSpiltWords)
            translateTaskQueue.clear();
        if (this.enableSpiltWords != enableSpiltWords) {
            this.enableSpiltWords = enableSpiltWords;
            translationCache.clear();
            String message = enableSpiltWords ?
                    languageManager.getTranslation("chat.dyntranslation.spilt.enable") :
                    languageManager.getTranslation("chat.dyntranslation.spilt.disable");
            notify(message);
            saveConfig();
        }
    }

    public boolean isEnableSpiltWords() {
        return enableSpiltWords;
    }

    public void setEnableChatTranslate(boolean enableChatTranslate) {
        if (!enableChatTranslate) {
            getNetworkQueue().clear();
        }
        if (enableChatTranslate != this.enableChatTranslate) {
            this.enableChatTranslate = enableChatTranslate;
            notify(languageManager.getTranslation(enableChatTranslate ?
                    "chat.dyntranslation.chat.translate.enable" :
                    "chat.dyntranslation.chat.translate.disable"));
            saveConfig();
        }
    }

    public void setPrintFormatChars(boolean printFormatChars) {
        this.printFormatChars = printFormatChars;
        saveConfig();
    }

    public Config setupConfig() {
        config.setChatTranslate(enableChatTranslate);
        config.setEnable(enable);
        config.setRetainOrg(retainOrg);
        config.setSpiltWords(enableSpiltWords);
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public AtomicBoolean getDoTranslate() {
        return doTranslate;
    }

    public AtomicReference<Class> getToastClazz() {
        return toastClazz;
    }

    public int getDisplayNumber() {
        return displayNumber;
    }

    public interface IVisitor {
        void onVisit(String name, String orgText, String translation);
    }

    private static void each(Map<String, Map<String, String>> source, IVisitor visitor) {
        source.forEach(
                (key1, value1) ->
                        value1.forEach(
                                (key, value) -> {
                                    visitor.onVisit(key1, key, value);
                                }
                        )
        );
    }

    private String getTranslation(@Nonnull String containerName, String text) {
        Map<String, String> stringMap = containerFontMap.get(containerName);
        if (stringMap != null) return stringMap.get(text);
        stringMap = containerFontMap.get(WILDCARD_CHARACTER);
        if (stringMap != null) return stringMap.get(text);
        return null;
    }

    //-----------------------------------------------------------------------------------------------
    private final int WORD_CACHE_SIZE = 50;
    private final int WORD_QUEUE_SIZE = 100;

    //network tasks' search cache
    private final Map<String, String> networkCache =
            new LRUCache<>(WORD_CACHE_SIZE);
    private final LinkedBlockingDeque<String> networkQueue =
            new LinkedBlockingDeque<>(WORD_QUEUE_SIZE);

    public Map<String, String> getNetworkCache() {
        return networkCache;
    }

    public LinkedBlockingDeque<String> getNetworkQueue() {
        return networkQueue;
    }

    private static class TranslateTask implements Runnable {
        private final String containerName, text;
        private final TranslationManager manager =
                TranslationManager.INSTANCE;

        public TranslateTask(String containerName, String text) {
            this.containerName = containerName;
            this.text = text;
        }

        @Override
        public void run() {

            if (EnCnCharList.extraIdeographicChars(text) != null) {

                final String language = languageManager.getLanguageCode();
                final List<String> resultList = new LinkedList<>();

                boolean separator;
                //if language is Chinese or Japanese
                if (language.startsWith("zh_") || language.startsWith("ja_")) {
                    separator = false;
                } else if (language.startsWith("en_")) {//if language is English
                    separator = true;
                } else {
                    separator = true;
                }

                if (manager.getLanguageDetector() != null) {
                    @SuppressWarnings("UnstableApiUsage")
                    TextObject textObject = manager.getTextObjectFactory().forText(text);
                    Optional<LdLocale> result = manager.languageDetector.detect(textObject).toJavaUtil();

                    result.ifPresent(ldLocale -> {
                        if (ldLocale.getLanguage().startsWith("zh")) {
                            //Chinese
                            //spilt by segment tool
                            resultList.addAll(SegmentBs.newInstance()
                                    .segmentMode(SegmentModes.greedyLength())
                                    .segment(text, SegmentResultHandlers.word()));
                        } else if (ldLocale.getLanguage().startsWith("en")) {
                            //English
                            //spilt by ' '
                            resultList.addAll(Lists.newArrayList(text.split(" ")));
                        } else if (ldLocale.getLanguage().startsWith("ja")) {
                            // TODO: 2020/7/5 to support Japanese

                        }
                    });
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
                                trans = word;
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
        private final TranslationManager manager = TranslationManager.INSTANCE;

//        private final String string;

//        public NetworkTranslateTask(String string){
//            this.string = string;
//        }

        @Override
        public void run() {
            if (manager.enable && manager.enableChatTranslate) {
                String languageCode = languageManager.getLanguageCode();
                String string = manager.getNetworkQueue().poll();

                try {
                    ITranslateApi.TranslateResult<TranslateData> result = NetworkTranslateHelper.translate(string, languageCode.toLowerCase().substring(0, 2));

                    if (result.getState() == ITranslateApi.STATE.FAILED) {
                        //do nothing
                    } else {
                        Gson gson = new Gson();
                        TranslateData translateData = gson.fromJson(result.getResult(), TranslateData.class);
                        if (!translateData.getTransResult().isEmpty()) {
                            manager.getNetworkCache().put(string, translateData.getTransResult().get(0).getDst());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    //---------------------------- This part is item name translation using NMT ---------------------------------------

    //TODO remove the relationship with minecraft server_executor thread pool.
    private final ReentrantLock lock = new ReentrantLock();
    private final FIFOCache<String, List<String>> nmtCache = new FIFOCache<>(MAX_CACHE_SIZE);
    private final Set<String> submitTasks = new HashSet<>();
    //limit the request frequency
    //failed request will increase the next one to be sent
    //the time wait is BASE_TIME_WAIT + wait_time * WAIT_TIME_UNIT
    //for the default case, request_wait_time = 500 + 250 * accumulate_failed_times
    // and the max wait time is 32 * 250 + 500 = 8.5 seconds
    private final RequestWait requestWait = RequestWait.DEFAULT();

    /**
     * @param text the original text to be translated.
     * @param res the translate result of the text
     * @param noTranslate if the original text is the target language, we do not translate it.
     * @throws InterruptedException the cache is thread save for putting and taking. If the thread has been interrupted,
     * the result will be discard.
     */
    public void finishTask(String text, List<String> res, boolean noTranslate) throws InterruptedException {
        lock.lockInterruptibly();
        if(noTranslate){
            nmtCache.put(text, Lists.newArrayList(text));
            requestWait.request(true);
        }else {

            if (res != null) {
                nmtCache.put(text, res);
                requestWait.request(true);
            } else {
                requestWait.request(false);
            }
        }
        submitTasks.remove(text);
        lock.unlock();
    }

    public boolean isWaitingRes(String text) {
        return submitTasks.contains(text);
    }

    public Optional<List<String>> getNMTTranslation(String text, int number) {

        final String clearText = EnCnCharList.extraIdeographicChars(text);

        List<String> trans = nmtCache.get(text);
        if (trans != null) {
            return Optional.of(trans);
        }

        boolean requestNotTooFrequency = requestWait.tryAccept();

        if (!requestNotTooFrequency) {
            long left = requestWait.leftWaitTime();
            return Optional.of(Lists.newArrayList(languageManager.getTranslation("string.dyntranslation.nextrequest.wait", left)));
        }

        if (!submitTasks.contains(text)) {
            submitTasks.add(text);
            game.runAtBackground(() -> {
                Optional<LdLocale> result = languageDetector.detect(clearText).toJavaUtil();
                result.ifPresent(
                        ldLocale -> {
                            //translate English only.
                            if(ldLocale.getLanguage().startsWith("en")){
                                try {
                                    ITranslateApi.TranslateResult<MyNMTTransApi.MyNMTTransRes> result1 = NetworkTranslateHelper.translateByNMT(text, "en", "zh");
                                    MyNMTTransApi.MyNMTTransRes res = new GsonBuilder().create().fromJson(result1.getResult(), MyNMTTransApi.MyNMTTransRes.class);
                                    if (res != null) { finishTask(text, res.getTranslation().getTo().subList(0, number), false);
                                    } else {
                                        finishTask(text, null, false);
                                    }
                                } catch (IOException | InterruptedException e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        finishTask(text, null, false);
                                    } catch (InterruptedException ignored) {

                                    }
                                }
                            }else {
                                try {
                                    //put the origin text as translation.
                                    finishTask(text, null, true);
                                } catch (InterruptedException ignored) {

                                }
                            }
                        }
                );

            });
        }

        return Optional.of(Lists.newArrayList(text));

    }

}
