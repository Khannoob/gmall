package edu.sysu.gmall.index.controller;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.index.service.IndexService;
import edu.sysu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

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
        List<CategoryEntity> categoryEntities = indexService.queryL1Categories();
        model.addAttribute("categories",categoryEntities);
        //TODO 加载广告
        return "index";
    }
    @GetMapping("index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryL2Categories(@PathVariable Long pid){
        List<CategoryEntity> categoryEntities = indexService.queryL2Categories(pid);
        return ResponseVo.ok(categoryEntities);
    }
    @GetMapping("index/test/lock")
    @ResponseBody
    public ResponseVo testLock(){
        indexService.testLock();
        return ResponseVo.ok();
    }
}
