package edu.sysu.gmall.pms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.BrandMapper;
import edu.sysu.gmall.pms.entity.BrandEntity;
import edu.sysu.gmall.pms.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandMapper, BrandEntity> implements BrandService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<BrandEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<BrandEntity>()
        );

        return new PageResultVo(page);
    }

}