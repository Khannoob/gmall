package edu.sysu.gmall.item.controller;

import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.item.service.ItemService;
import edu.sysu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-31 10:13
 */
@Controller
public class ItemController {
    @Autowired
    ItemService itemService;

    @GetMapping("{skuId}.html")
    public String itemDetail(@PathVariable Long skuId, Model model){
        ItemVo itemVo = itemService.itemDetail(skuId);
        model.addAttribute("itemVo",itemVo);
        return "item";
    }
}
