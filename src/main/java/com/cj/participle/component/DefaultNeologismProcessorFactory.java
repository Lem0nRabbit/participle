package com.cj.participle.component;

public class DefaultNeologismProcessorFactory implements NeologismProcessorFactory {

    private static DefaultNeologismProcessorFactory instance = new DefaultNeologismProcessorFactory();

    public static DefaultNeologismProcessorFactory getInstance() {
        return instance;
    }

    @Override
    public NeologismProcessor getNeologismProcessor() {
        return new DefaultNeologismProcessor();
    }
}
