package com.cj.participle;

import com.cj.participle.component.DefaultNeologismProcessorFactory;
import com.cj.participle.component.Dictionary;
import com.cj.participle.component.NeologismProcessor;
import com.cj.participle.component.NeologismProcessorFactory;
import com.cj.participle.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WordSegmenter {

    public enum SearchMode {
        ALL, ONLY_PUBLIC, ONLY_USER
    }

    public static class WordSegmenterBuilder {

        private Dictionary publicDictionary;

        private Dictionary userDictionary;

        private NeologismProcessorFactory neologismProcessorFactory;

        public WordSegmenterBuilder withPublicDictionary(Dictionary publicDictionary) {
            this.publicDictionary = publicDictionary;
            return this;
        }

        public WordSegmenterBuilder withUserDictionary(Dictionary userDictionary) {
            this.userDictionary = userDictionary;
            return this;
        }

        public WordSegmenterBuilder withNeologismProcessorFactory(NeologismProcessorFactory neologismProcessorFactory) {
            this.neologismProcessorFactory = neologismProcessorFactory;
            return this;
        }

        public WordSegmenter build() {
            if (null == neologismProcessorFactory) {
                withNeologismProcessorFactory(DefaultNeologismProcessorFactory.getInstance());
            }
            return new WordSegmenter(publicDictionary, userDictionary, neologismProcessorFactory);
        }

    }

    public static WordSegmenterBuilder builder() {
        return new WordSegmenterBuilder();
    }

    private Dictionary publicDictionary;

    private Dictionary userDictionary;

    private NeologismProcessorFactory neologismProcessorFactory;

    public WordSegmenter(Dictionary publicDictionary, Dictionary userDictionary, NeologismProcessorFactory neologismProcessorFactory) {
        this.publicDictionary = publicDictionary;
        this.userDictionary = userDictionary;
        this.neologismProcessorFactory = neologismProcessorFactory;
    }

    public List<String> participle(final String sentence, SearchMode mode) {
        Dictionary dictionary = selectDictionary(mode);
        List<List<String>> result = participle(sentence, dictionary, dictionary.getMaxLength(), false);
        return result.stream().map(str -> String.join(" ", str)).collect(Collectors.toList());
    }

    public List<List<String>> participle(final String sentence, Dictionary dictionary, int maxLength, boolean isSub) {
        List<List<String>> result = new ArrayList<>();
        if (null == sentence || sentence.isEmpty()) {
            return result;
        }
        NeologismProcessor neologismProcessor = neologismProcessorFactory.getNeologismProcessor();
        int minLength = dictionary.getMinLength();
        int pointer = 0;
        int length = sentence.length();
        while (pointer < length) {
            int upper = Math.min(length, pointer + maxLength);
            String sub;
            Set<String> searchResults;

            do {
                sub = sentence.substring(pointer, upper);
                searchResults = dictionary.search(sub);
                upper --;
            } while (searchResults.isEmpty() && upper - pointer >= minLength);

            // 子串中不处理新词
            if (searchResults.isEmpty() && isSub) {
                result.clear();
                return result;
            }

            int subLength = sub.length();
            // 子串长度必须大于词典中最小长度单词的两倍才做子串分词处理
            if (subLength > minLength * 2) {
                List<List<String>> sonResult = participle(sub, dictionary, subLength - 1, true);
                sonResult.stream().filter(str -> !str.isEmpty()).map(str -> String.join(" ", str)).forEach(searchResults::add);
            }
            // 新词采集
            if (searchResults.isEmpty()) {
                neologismProcessor.collect(sub);
            } else {
                // 新词处理
                result.addAll(neologismProcessor.process());
                result.add(new ArrayList<>(searchResults));
            }
            pointer += subLength;
        }
        // 避免句子结尾有新词漏掉处理
        result.addAll(neologismProcessor.process());
        return CollectionUtils.descartes(result);
    }

    public WordSegmenter setUserDictionary(Dictionary userDictionary) {
        this.userDictionary = userDictionary;
        return this;
    }

    private Dictionary selectDictionary(SearchMode mode) {
        switch (mode) {
            case ALL:
                return Dictionary.builder().appendDictionary(publicDictionary, userDictionary).build();
            case ONLY_USER:
                return userDictionary;
            case ONLY_PUBLIC:
            default:
                return publicDictionary;
        }
    }

}
