package edu.sysu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.sysu.gmall.pms.entity.AttrEntity;
import org.apache.commons.lang3.RandomUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.CategoryMapper;
import edu.sysu.gmall.pms.entity.CategoryEntity;
import edu.sysu.gmall.pms.service.CategoryService;
import org.springframework.util.CollectionUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoriesByPid(Long parentId) {
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (parentId == -1) {
            return this.list(queryWrapper);
        }
        queryWrapper.eq(CategoryEntity::getParentId, parentId);
        return this.list(queryWrapper);
    }

    private static final String KEY_PREFIX = "pms:category:";
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<CategoryEntity> queryL2CategoriesByPid(Long pid) {
        List<CategoryEntity> categories = baseMapper.queryL2CategoriesByPid(pid);
        return categories;
    }

    @Override
    public List<CategoryEntity> queryCatesL123ByL3Cid(Long cid) {
        ArrayList<CategoryEntity> categoryEntities = new ArrayList<>();
        //三级分类
        CategoryEntity categoryEntity3 = baseMapper.selectById(cid);

        if (categoryEntity3 != null) {
            //二级分类
            CategoryEntity categoryEntityL2 = baseMapper.selectById(categoryEntity3.getParentId());

            if (categoryEntityL2 != null) {
                //一级分类
                CategoryEntity categoryEntityL1 = baseMapper.selectById(categoryEntityL2.getParentId());
                categoryEntities.add(categoryEntityL1);
            }
            categoryEntities.add(categoryEntityL2);
        }
        categoryEntities.add(categoryEntity3);
        return categoryEntities;
    }
}