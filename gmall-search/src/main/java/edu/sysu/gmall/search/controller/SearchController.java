package edu.sysu.gmall.search.controller;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.search.pojo.SearchParamVo;
import edu.sysu.gmall.search.pojo.SearchResponseVo;
import edu.sysu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-20 12:34
 */
@Controller
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchService searchService;
    @GetMapping
    public String search(SearchParamVo searchParamVo, Model model){
        SearchResponseVo searchResponseVo = searchService.search(searchParamVo);
        model.addAttribute("response",searchResponseVo);
        model.addAttribute("searchParam",searchParamVo);
//        System.out.println(searchResponseVo.getTotal());
//        System.out.println(searchResponseVo.getPageSize());
        return "search";
    }
}
