package edu.sysu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.pms.entity.SpuEntity;
import edu.sysu.gmall.pms.vo.SpuVo;

import java.util.List;
import java.util.Map;

/**
 * spu信息
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 13:08:59
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo queryPageByCid(Long categoryId, PageParamVo paramVo);

    void bigSave(SpuVo spuVo);
    void saveSpuDesc(SpuVo spuVo, Long spuId);

}

