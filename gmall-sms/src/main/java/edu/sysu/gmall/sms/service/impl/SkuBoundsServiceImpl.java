package edu.sysu.gmall.sms.service.impl;

import com.sun.org.apache.bcel.internal.generic.NEW;
import edu.sysu.gmall.sms.entity.SkuFullReductionEntity;
import edu.sysu.gmall.sms.entity.SkuLadderEntity;
import edu.sysu.gmall.sms.service.SkuFullReductionService;
import edu.sysu.gmall.sms.service.SkuLadderService;
import edu.sysu.gmall.sms.vo.SkuSaleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Override
    public void bigSave(SkuSaleVo skuSaleVo) {
        //3.1 保存sms_sku_bounds表
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo,skuBoundsEntity);
        List<Integer> work = skuSaleVo.getWork();
        if (work !=null&& work.size()==4){
            skuBoundsEntity.setWork(work.get(3)*8+work.get(2)*4+work.get(1)*2+work.get(0));
        }
        this.save(skuBoundsEntity);
        //3.2 保存sms_sku_full_reduction表
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleVo.getAddFullOther());
        skuFullReductionService.save(skuFullReductionEntity);
        //3.3 保存sms_sku_ladder表
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo,skuLadderEntity);
        skuLadderEntity.setAddOther(skuSaleVo.getAddLadderOther());
        skuLadderService.save(skuLadderEntity);
    }

}