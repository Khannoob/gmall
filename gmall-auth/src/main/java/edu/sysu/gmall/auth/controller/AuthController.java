package edu.sysu.gmall.auth.controller;

import edu.sysu.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 11:36
 */
@Controller
public class AuthController {
    @Autowired
    AuthService authService;

    //因为是通过这里跳转到login页面的 那个是实现login功能
    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl",defaultValue = "http://gmall.com/") String returnUrl, Model model) {
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    @PostMapping("login")
    public String login(@RequestParam String returnUrl,
                        @RequestParam String loginName,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        authService.login(loginName, password,request,response);
        return "redirect:" + returnUrl;
    }
}
