/*
 *
 * The MIT License (MIT)
 * Copyright (c) 2016 Neofonie GmbH
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.neofonie.cinderella.core.captcha.text;

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
