package de.neofonie.common.cinderella.captcha.text;

import java.util.concurrent.ThreadLocalRandom;

public class TextGeneratorImpl implements TextGenerator {

    private static final String CHARS = "BCDFGHJKLMNPQRSTVWXYZ";
    private static final String VOCAL = "AEIOU";
    private static final String NUMBER = "123456789";
    private int length = 10;
    private int numbers = 3;

    @Override
    public String generateText() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length - numbers; i++) {
            stringBuilder.append(random(i % 3 == 1 ? VOCAL : CHARS));
        }
        for (int i = 0; i < numbers; i++) {
            stringBuilder.append(random(NUMBER));
        }
        return stringBuilder.toString();
    }

    private char random(String chars) {
        return chars.charAt(ThreadLocalRandom.current().nextInt(chars.length()));
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setNumbers(int numbers) {
        this.numbers = numbers;
    }
}
