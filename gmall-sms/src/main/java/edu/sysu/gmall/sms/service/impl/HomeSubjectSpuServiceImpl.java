package edu.sysu.gmall.sms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.sms.mapper.HomeSubjectSpuMapper;
import edu.sysu.gmall.sms.entity.HomeSubjectSpuEntity;
import edu.sysu.gmall.sms.service.HomeSubjectSpuService;


@Service("homeSubjectSpuService")
public class HomeSubjectSpuServiceImpl extends ServiceImpl<HomeSubjectSpuMapper, HomeSubjectSpuEntity> implements HomeSubjectSpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<HomeSubjectSpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<HomeSubjectSpuEntity>()
        );

        return new PageResultVo(page);
    }

}