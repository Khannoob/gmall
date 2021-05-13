package edu.sysu.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.ums.mapper.UserLevelMapper;
import edu.sysu.gmall.ums.entity.UserLevelEntity;
import edu.sysu.gmall.ums.service.UserLevelService;


@Service("userLevelService")
public class UserLevelServiceImpl extends ServiceImpl<UserLevelMapper, UserLevelEntity> implements UserLevelService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserLevelEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserLevelEntity>()
        );

        return new PageResultVo(page);
    }

}