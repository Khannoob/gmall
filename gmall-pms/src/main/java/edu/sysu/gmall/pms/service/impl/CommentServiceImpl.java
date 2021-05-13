package edu.sysu.gmall.pms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.pms.mapper.CommentMapper;
import edu.sysu.gmall.pms.entity.CommentEntity;
import edu.sysu.gmall.pms.service.CommentService;


@Service("commentService")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentEntity> implements CommentService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CommentEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CommentEntity>()
        );

        return new PageResultVo(page);
    }

}