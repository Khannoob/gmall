package edu.sysu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.wms.entity.WareOrderBillDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-14 19:41:47
 */
public interface WareOrderBillDetailService extends IService<WareOrderBillDetailEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

