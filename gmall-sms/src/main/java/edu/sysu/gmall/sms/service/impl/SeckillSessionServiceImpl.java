package edu.sysu.gmall.sms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.sms.mapper.SeckillSessionMapper;
import edu.sysu.gmall.sms.entity.SeckillSessionEntity;
import edu.sysu.gmall.sms.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionMapper, SeckillSessionEntity> implements SeckillSessionService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SeckillSessionEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageResultVo(page);
    }

}