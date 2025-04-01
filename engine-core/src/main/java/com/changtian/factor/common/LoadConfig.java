package com.changtian.factor.common;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 加载配置文件
 */
@Slf4j
public class LoadConfig {
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static Properties prop;
    private static final String FILE_PROPERTIES = "application.properties";

    synchronized static private void init() {
        log.info("start load properties ");
        InputStream stream = null;
        InputStream streamSub = null;
        try {
            stream = LoadConfig.class.getClassLoader().getResourceAsStream(FILE_PROPERTIES);

            prop = new Properties();
            assert stream != null;
            prop.load(new InputStreamReader(stream, DEFAULT_ENCODING));
            String profile = prop.getProperty("spring.profiles.active");
            String subFileProperties = String.format("application-%s.properties", profile);

            streamSub = LoadConfig.class.getClassLoader().getResourceAsStream(subFileProperties);
            assert streamSub != null;
            prop.load(new InputStreamReader(streamSub, DEFAULT_ENCODING));


        } catch (Exception ex) {
            log.error("LoadConfig error", ex);
        } finally {
            try {
                if (null != stream) {
                    stream.close();
                }

                if (null != streamSub) {
                    streamSub.close();
                }
            } catch (Exception e) {
                log.error("close io stream  error", e);
            }

        }
    }

    public static Properties getProp() {
        if (prop == null) {
            init();
        }
        return prop;
    }

}
