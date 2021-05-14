package edu.sysu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.wms.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-14 19:41:47
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

