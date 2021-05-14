package edu.sysu.gmall.wms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.wms.mapper.WareOrderBillDetailMapper;
import edu.sysu.gmall.wms.entity.WareOrderBillDetailEntity;
import edu.sysu.gmall.wms.service.WareOrderBillDetailService;


@Service("wareOrderBillDetailService")
public class WareOrderBillDetailServiceImpl extends ServiceImpl<WareOrderBillDetailMapper, WareOrderBillDetailEntity> implements WareOrderBillDetailService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareOrderBillDetailEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareOrderBillDetailEntity>()
        );

        return new PageResultVo(page);
    }

}