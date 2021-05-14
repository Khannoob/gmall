package edu.sysu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.wms.entity.WareEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-14 19:41:47
 */
public interface WareService extends IService<WareEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

