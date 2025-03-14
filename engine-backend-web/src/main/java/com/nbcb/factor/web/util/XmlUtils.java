package com.nbcb.factor.web.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class XmlUtils {
    public XmlMapper xmlConfigAndEntity() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        xmlMapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
        return xmlMapper;
    }

    /**
     * 将Java对象转换为XML字符串
     *
     * @param obj 要转换的Java对象
     * @return 转换后的XML字符串如果转换过程中发生异常，则返回空字符串
     */
    public String toXml(Object obj) {
        // 创建XML映射器实例，使用特定的配置和实体
        XmlMapper xmlMapper = xmlConfigAndEntity();
        String xml = "";
        try {
            // 尝试将对象转换为XML字符串
            xml = xmlMapper.writeValueAsString(obj);
        } catch (Exception e) {
            // 如果发生异常，记录日志并返回空字符串
            log.info("xml转换异常", e);
        }
        return xml;
    }
}
