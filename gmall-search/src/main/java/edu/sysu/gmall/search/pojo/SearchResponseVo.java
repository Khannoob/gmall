package edu.sysu.gmall.search.pojo;

import edu.sysu.gmall.pms.entity.BrandEntity;
import edu.sysu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-20 22:07
 */
@Data
public class SearchResponseVo {
    private List<BrandEntity> brands;
    private List<CategoryEntity> categoryEntities;
    private List<SearchResponseAttrVo> filters;
    private Integer pageSize;
    private Integer pageNum;
    private List<Goods> goodsList;
    // 总记录数
    private Long total;
}
