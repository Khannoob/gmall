package edu.sysu.gmall.sms.service.impl;

import com.sun.org.apache.bcel.internal.generic.NEW;
import edu.sysu.gmall.sms.entity.SkuFullReductionEntity;
import edu.sysu.gmall.sms.entity.SkuLadderEntity;
import edu.sysu.gmall.sms.mapper.SkuFullReductionMapper;
import edu.sysu.gmall.sms.mapper.SkuLadderMapper;
import edu.sysu.gmall.sms.service.SkuFullReductionService;
import edu.sysu.gmall.sms.service.SkuLadderService;
import edu.sysu.gmall.sms.vo.ItemSalesVo;
import edu.sysu.gmall.sms.vo.SkuSaleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.sms.mapper.SkuBoundsMapper;
import edu.sysu.gmall.sms.entity.SkuBoundsEntity;
import edu.sysu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Autowired
    private SkuFullReductionService skuFullReductionService;
    @Autowired
    private SkuLadderService skuLadderService;

    @Transactional
    @Override
    public void bigSave(SkuSaleVo skuSaleVo) {
        //3.1 保存sms_sku_bounds表
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo, skuBoundsEntity);
        List<Integer> work = skuSaleVo.getWork();
        if (work != null && work.size() == 4) {
            skuBoundsEntity.setWork(work.get(3) * 8 + work.get(2) * 4 + work.get(1) * 2 + work.get(0));
        }
        this.save(skuBoundsEntity);
        //3.2 保存sms_sku_full_reduction表
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleVo.getAddFullOther());
        skuFullReductionService.save(skuFullReductionEntity);
        //3.3 保存sms_sku_ladder表
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo, skuLadderEntity);
        skuLadderEntity.setAddOther(skuSaleVo.getAddLadderOther());
        skuLadderService.save(skuLadderEntity);
    }

    @Autowired
    SkuFullReductionMapper skuFullReductionMapper;

    @Autowired
    SkuLadderMapper skuLadderMapper;
    @Override
    public List<ItemSalesVo> allSales(Long skuId) {
        ArrayList<ItemSalesVo> itemSalesVos = new ArrayList<>();

        SkuBoundsEntity boundsEntity = this.baseMapper.selectOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (boundsEntity != null) {
            ItemSalesVo bounds = new ItemSalesVo();
            bounds.setType("积分");
            bounds.setDesc("赠送成长积分:" + boundsEntity.getGrowBounds() + ",赠送购物积分:" + boundsEntity.getBuyBounds() + "!");
            itemSalesVos.add(bounds);
        }

        SkuFullReductionEntity fullReductionEntity = skuFullReductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (fullReductionEntity != null) {
            ItemSalesVo fullReduction = new ItemSalesVo();
            fullReduction.setType("满减");
            fullReduction.setDesc("满:" + fullReductionEntity.getFullPrice() + "元,立减:" + fullReductionEntity.getReducePrice() + "元!");
            itemSalesVos.add(fullReduction);
        }

        SkuLadderEntity ladderEntity = skuLadderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (ladderEntity != null) {
            ItemSalesVo ladder = new ItemSalesVo();
            ladder.setType("打折");
            ladder.setDesc("买:" + ladderEntity.getFullCount() + "件商品,打:" + ladderEntity.getDiscount().divide(new BigDecimal(10)) + "折!");
            itemSalesVos.add(ladder);
        }

        return itemSalesVos;
    }

}