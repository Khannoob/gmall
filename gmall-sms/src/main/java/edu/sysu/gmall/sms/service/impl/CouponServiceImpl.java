package edu.sysu.gmall.sms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.sms.mapper.CouponMapper;
import edu.sysu.gmall.sms.entity.CouponEntity;
import edu.sysu.gmall.sms.service.CouponService;


@Service("couponService")
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponEntity> implements CouponService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CouponEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CouponEntity>()
        );

        return new PageResultVo(page);
    }

}