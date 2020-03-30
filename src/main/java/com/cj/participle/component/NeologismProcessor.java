package com.cj.participle.component;

import java.util.List;

public interface NeologismProcessor {

    /**
     * collect new word
     * 收集新词
     * @param word 单词
     */
    void collect(String word);

    /**
     * process collected new word
     * 处理已收集的新词并返回
     * @return
     */
    List<List<String>> process();
}
