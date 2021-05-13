package edu.sysu.gmall.sms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.sms.mapper.SkuLadderMapper;
import edu.sysu.gmall.sms.entity.SkuLadderEntity;
import edu.sysu.gmall.sms.service.SkuLadderService;


@Service("skuLadderService")
public class SkuLadderServiceImpl extends ServiceImpl<SkuLadderMapper, SkuLadderEntity> implements SkuLadderService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuLadderEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuLadderEntity>()
        );

        return new PageResultVo(page);
    }

}