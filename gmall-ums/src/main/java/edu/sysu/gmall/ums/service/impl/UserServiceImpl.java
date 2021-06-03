package edu.sysu.gmall.ums.service.impl;

import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.ums.mapper.UserMapper;
import edu.sysu.gmall.ums.entity.UserEntity;
import edu.sysu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkUserInfo(String data, Integer type) {
        switch (type) {
            case 1:
                return baseMapper.selectCount(new QueryWrapper<UserEntity>().eq("username", data)) > 0;
            case 2:
                return baseMapper.selectCount(new QueryWrapper<UserEntity>().eq("phone", data)) > 0;
            case 3:
                return baseMapper.selectCount(new QueryWrapper<UserEntity>().eq("email", data)) > 0;
            default:
                return null;
        }
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        //TODO:查询验证码与手机号是否一致
        //1.生成盐
        String uuid = UUID.randomUUID().toString();
        String salt = uuid.substring(0, 6);
        userEntity.setSalt(salt);
        //2.生成MD5加盐加密的密码
        String password = DigestUtils.md5Hex(salt + userEntity.getPassword());
        userEntity.setPassword(password);
        //3.保存默认属性
        userEntity.setCreateTime(new Date());
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setGrowth(1000);
        userEntity.setLevelId(1l);
        userEntity.setStatus(1);
        userEntity.setIntegration(1000);
        //4.保存到数据库
        baseMapper.insert(userEntity);
        //TODO:删除redis保存的验证码 防止重复注册
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        //根据 用户名和密码查询用户 返回用户信息 登录名可能是手机 username 邮箱
        List<UserEntity> userEntities = baseMapper.selectList(new QueryWrapper<UserEntity>().eq("username", loginName).
                or().eq("phone", loginName).or().eq("email", loginName));
        if (CollectionUtils.isEmpty(userEntities))
            return null;
        for (UserEntity userEntity : userEntities) {
            String salt = userEntity.getSalt();
            String pwd = salt + password;
            if (StringUtils.equals(userEntity.getPassword(), DigestUtils.md5Hex(pwd)))
                return userEntity;
        }
        return null;
    }
}