package edu.sysu.gmall.index.controller;

import edu.sysu.gmall.index.service.IndexService;
import edu.sysu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-24 18:19
 */
@Controller
public class IndexController {
    @Autowired
    IndexService indexService;

    @GetMapping
    public String toIndex(Model model){
        List<CategoryEntity> categoryEntities = indexService.getL1Categories();
        model.addAttribute("categories",categoryEntities);
        //TODO 加载广告
        return "index";
    }
}
