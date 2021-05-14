package edu.sysu.gmall.pms.controller;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.pms.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-14 10:24
 */
@RestController
@RequestMapping("pms/oss")
public class OssController {
    @Autowired
    private OssService ossService;
    @GetMapping("policy")
    public ResponseVo<Object> uploadBrandPic(HttpServletRequest request, HttpServletResponse response){
        Map<String, String> map = null;
        try {
            map = ossService.uploadBrandPic(request, response);
            return ResponseVo.ok(map);
        } catch (Exception e) {
            System.out.println("e = " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseVo.fail("获取签名失败!");
    }
}
