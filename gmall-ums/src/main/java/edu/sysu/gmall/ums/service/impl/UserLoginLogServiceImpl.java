package edu.sysu.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.ums.mapper.UserLoginLogMapper;
import edu.sysu.gmall.ums.entity.UserLoginLogEntity;
import edu.sysu.gmall.ums.service.UserLoginLogService;


@Service("userLoginLogService")
public class UserLoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLogEntity> implements UserLoginLogService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserLoginLogEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserLoginLogEntity>()
        );

        return new PageResultVo(page);
    }

}