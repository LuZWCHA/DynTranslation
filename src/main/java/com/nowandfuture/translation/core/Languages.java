package com.nowandfuture.translation.core;

import com.google.common.collect.ImmutableList;
import com.optimaize.langdetect.i18n.LdLocale;

import java.util.ArrayList;
import java.util.List;

public class Languages {
    private static final ImmutableList<LdLocale> languages;

    static {
        List<LdLocale> names = new ArrayList<>();

        names.add(LdLocale.fromString("en"));
        names.add(LdLocale.fromString("ja"));
        names.add(LdLocale.fromString("zh-CN"));
        names.add(LdLocale.fromString("zh-TW"));

        languages = ImmutableList.copyOf(names);
    }

    private static final ImmutableList<String> shortTextLanguages;

    static {
        List<String> texts = new ArrayList<>();
        texts.add("en");
        shortTextLanguages = ImmutableList.copyOf(texts);
    }

    public static ImmutableList<LdLocale> getLanguages() {
        return languages;
    }
}
