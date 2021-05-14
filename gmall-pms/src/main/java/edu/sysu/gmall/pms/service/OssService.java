package edu.sysu.gmall.pms.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-14 11:24
 */
public interface OssService {
    Map<String, String> uploadBrandPic(HttpServletRequest request, HttpServletResponse response);
}
