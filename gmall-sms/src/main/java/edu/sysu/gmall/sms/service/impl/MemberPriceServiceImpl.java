package edu.sysu.gmall.sms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.sms.mapper.MemberPriceMapper;
import edu.sysu.gmall.sms.entity.MemberPriceEntity;
import edu.sysu.gmall.sms.service.MemberPriceService;


@Service("memberPriceService")
public class MemberPriceServiceImpl extends ServiceImpl<MemberPriceMapper, MemberPriceEntity> implements MemberPriceService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<MemberPriceEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<MemberPriceEntity>()
        );

        return new PageResultVo(page);
    }

}