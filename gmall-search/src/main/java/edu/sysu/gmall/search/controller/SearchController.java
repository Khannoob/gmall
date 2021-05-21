package edu.sysu.gmall.search.controller;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.search.pojo.SearchParamVo;
import edu.sysu.gmall.search.pojo.SearchResponseVo;
import edu.sysu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-20 12:34
 */
@RestController
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchService searchService;
    @GetMapping
    public ResponseVo<SearchResponseVo> search(SearchParamVo searchParamVo){
        SearchResponseVo searchResponseVo = searchService.search(searchParamVo);
        return ResponseVo.ok(searchResponseVo);
    }
}
