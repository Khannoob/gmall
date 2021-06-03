package edu.sysu.gmall.gateway.config;

import edu.sysu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 16:41
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
            if (publicKey == null) {
                throw new RuntimeException("公钥不存在.......");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
