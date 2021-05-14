package edu.sysu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;
import edu.sysu.gmall.common.bean.ResponseVo;
import edu.sysu.gmall.pms.entity.AttrEntity;
import edu.sysu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author Khan
 * @email khannoob@outlook.com
 * @date 2021-05-13 13:08:59
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryAttrGroupByCid(Long cid);

    List<AttrGroupEntity> queryAttrGroupAndAttrByCid(String catId);
}

