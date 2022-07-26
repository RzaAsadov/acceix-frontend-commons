/*
 * The MIT License
 *
 * Copyright 2022 Rza Asadov (rza dot asadov at gmail dot com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.acceix.frontend.web.commons;

import de.neuland.jade4j.template.TemplateLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author zrid
 */
public class JarTemplateLoader implements TemplateLoader {

    private final BufferedReader bufferedReader;
    
    public JarTemplateLoader(BufferedReader t_bufferedReader) {
            bufferedReader = t_bufferedReader;
    }
    
    
    

    @Override
    public long getLastModified(String string) throws IOException {
        return -1;
    }

    @Override
    public Reader getReader(String string) throws IOException {
            System.out.println("READER REQUESTED ");
            String s;
            while ( (s = bufferedReader.readLine()) != null) {
                System.out.println(s);
            }
            return bufferedReader;
    }
    
}
