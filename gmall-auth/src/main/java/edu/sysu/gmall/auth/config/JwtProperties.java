package edu.sysu.gmall.auth.config;

import edu.sysu.gmall.common.utils.JwtUtils;
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
    private String priKeyPath;
    private String secret;
    private Integer expireMinutes;
    private String cookieName;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String unick;

    @PostConstruct
    public void init() {
        try {
            File pubFile = new File(pubKeyPath);
            File priFile = new File(priKeyPath);
            if (privateKey == null || publicKey == null) {
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            privateKey = RsaUtils.getPrivateKey(priKeyPath);
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
