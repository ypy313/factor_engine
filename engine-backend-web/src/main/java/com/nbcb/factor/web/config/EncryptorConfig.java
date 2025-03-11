package com.nbcb.factor.web.config;


import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class EncryptorConfig {

    /**
     * java -cp jasypt-1.9.2.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI
     * input="factor123"
     * password=EbfYkitulv73I2p0mXI50JMXoaxZTKJ7
     * algorithm=PBEWithMD5AndDES
     */
    @Bean("jasyptStringEncryptor")
    public StringEncryptor jasyptStringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("EbfYkitulv73I2p0mXI50JMXoaxZTKJ7");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setPoolSize("1");
        encryptor.setConfig(config);
        return encryptor;
    }

    public static void main(String args[]){
        args = new String[3];
        args[0] = "input=Factor,123";
        args[1] = "password=EbfYkitulv73I2p0mXI50JMXoaxZTKJ7";
        args[2] = "algorithm=PBEWithMD5AndDES";
        JasyptPBEStringEncryptionCLI.main(args);
    }
}