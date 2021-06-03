package edu.sysu.gmall.auth;

import edu.sysu.gmall.common.utils.JwtUtils;
import edu.sysu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
    private static final String pubKeyPath = "D:\\project\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\project\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "zzsw");
    }

        @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "101");
        map.put("username", "zws");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjEwMSIsInVzZXJuYW1lIjoiendzIiwiZXhwIjoxNjIyNTIyODk5fQ.H3pLRw_0NSMDn0ywcxmv9J_jF-edlNS6ducBvFdraFwpHi8bUJDM-vk3D-Ohue_zJ5Q-ncqYTvBbaR4lJzgrMn_afyYBd1TifvVOq_guNoYHUSNTYkFf7iwvcks__fHadUchMMQkOmnS2SWNEn47GsAagFlDRYz-UhgNBjpuHfaqAan0G4RcF9-6e42eWPbXi0Q1VjchyKGMpdo6sucX1jD8BT2bMuiN_E9Q1YgpOyjJ1L8TtCN-7xia9RoyAu4M7-PQH5GUmo3a2CGmbQWd_W1TSq9dPCBxxPfAW-6p7bdnn7NsQ-tYouQtbMPlFX5hWUzzuwxiYShpUNmC_7q5rA";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}