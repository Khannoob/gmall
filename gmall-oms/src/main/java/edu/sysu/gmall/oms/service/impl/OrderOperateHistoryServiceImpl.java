package edu.sysu.gmall.oms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.oms.mapper.OrderOperateHistoryMapper;
import edu.sysu.gmall.oms.entity.OrderOperateHistoryEntity;
import edu.sysu.gmall.oms.service.OrderOperateHistoryService;


@Service("orderOperateHistoryService")
public class OrderOperateHistoryServiceImpl extends ServiceImpl<OrderOperateHistoryMapper, OrderOperateHistoryEntity> implements OrderOperateHistoryService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OrderOperateHistoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OrderOperateHistoryEntity>()
        );

        return new PageResultVo(page);
    }

}