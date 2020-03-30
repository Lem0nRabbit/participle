package com.cj.participle.component;

import java.util.*;

public class Dictionary {

    private Set<String> dictionary;

    private Map<String, Set<String>> actualWords;

    private int maxLength;

    private int minLength;

    private Dictionary(Set<String> dictionary) {
        this.dictionary = dictionary;
        this.actualWords = new HashMap<>();
        this.maxLength = 0;
        this.minLength = Integer.MAX_VALUE;
        for (String word : dictionary) {
            String newWord = word.replaceAll("[\\s-]", "");
            Set<String> words = this.actualWords.get(newWord);
            if (null != words) {
                words.add(word);
            } else if (!newWord.equals(word)) {
                words = new HashSet<>();
                words.add(word);
                this.actualWords.put(newWord, words);
            }
            this.maxLength = Math.max(maxLength, newWord.length());
            this.minLength = Math.min(minLength, newWord.length());
        }
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getMinLength() {
        return minLength;
    }

    public static DictionaryBuilder builder() {
        return new DictionaryBuilder();
    }

    public static class DictionaryBuilder {

        private Set<String> dictionary;

        private DictionaryBuilder() {
            this.dictionary = new HashSet<>();
        }

        public DictionaryBuilder appendDictionary(Dictionary... dictionaries) {
            Arrays.stream(dictionaries).filter(Objects::nonNull).map(dictionary -> dictionary.dictionary).forEach(dictionary::addAll);
            return this;
        }

        public DictionaryBuilder appendDictionary(String... words) {
            Arrays.stream(words).map(String::trim).forEach(dictionary::add);
            return this;
        }

        public Dictionary build() {
            return new Dictionary(dictionary);
        }

    }

    /**
     * One or more ways to find matching words in a dictionary
     * @param word
     * @return
     */
    public Set<String> search(String word) {
        Set<String> words = actualWords.get(word);
        if (null == words) {
            words = new HashSet<>();
            if (dictionary.contains(word)) {
                words.add(word);
            }
        }
        return words;
    }

}

