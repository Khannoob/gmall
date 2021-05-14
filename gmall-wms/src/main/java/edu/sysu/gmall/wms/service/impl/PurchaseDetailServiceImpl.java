package edu.sysu.gmall.wms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.wms.mapper.PurchaseDetailMapper;
import edu.sysu.gmall.wms.entity.PurchaseDetailEntity;
import edu.sysu.gmall.wms.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailMapper, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<PurchaseDetailEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<PurchaseDetailEntity>()
        );

        return new PageResultVo(page);
    }

}