package com.crashinvaders.common;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Awesome solution from http://stackoverflow.com/a/15638381/3802890 */
public class IniParser {

    private Pattern section = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private Pattern keyValue = Pattern.compile("\\s*([^=]*)=(.*)");
    private Map<String, Map<String, String>> entries = new HashMap<>();
    private boolean loaded = false;

    public IniParser() {
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load(File file) throws IOException {
        load(new FileReader(file));
    }

    public void load(String iniString) throws IOException {
        load(new StringReader(iniString));
    }

    public void load(Reader reader) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            String section = null;
            while ((line = br.readLine()) != null) {
                Matcher m = this.section.matcher(line);
                if (m.matches()) {
                    section = m.group(1).trim();
                } else if (section != null) {
                    m = keyValue.matcher(line);
                    if (m.matches()) {
                        String key = m.group(1).trim();
                        String value = m.group(2).trim();
                        Map<String, String> kv = entries.get(section);
                        if (kv == null) {
                            entries.put(section, kv = new HashMap<>());
                        }
                        kv.put(key, value);
                    }
                }
            }
            loaded = true;
        }
    }

    public String getString(String section, String key, String defaultValue) {
        Map<String, String> kv = entries.get(section);
        if (kv == null || kv.get(key) == null) {
            return defaultValue;
        }
        return kv.get(key);
    }

    public int getInt(String section, String key, int defaultValue) {
        Map<String, String> kv = entries.get(section);
        if (kv == null || kv.get(key) == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(kv.get(key));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public float getFloat(String section, String key, float defaultValue) {
        Map<String, String> kv = entries.get(section);
        if (kv == null || kv.get(key) == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(kv.get(key));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public double getDouble(String section, String key, double defaultValue) {
        Map<String, String> kv = entries.get(section);
        if (kv == null || kv.get(key) == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(kv.get(key));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public boolean getBoolean(String section, String key, boolean defaultValue) {
        Map<String, String> kv = entries.get(section);
        if (kv == null || kv.get(key) == null) {
            return defaultValue;
        }
        try {
            int intValue = Integer.parseInt(kv.get(key));
            return intValue != 0;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
}
