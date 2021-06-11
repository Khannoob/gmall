package edu.sysu.gmall.wms.service.impl;

import edu.sysu.gmall.common.exception.OrderException;
import edu.sysu.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.sysu.gmall.common.bean.PageResultVo;
import edu.sysu.gmall.common.bean.PageParamVo;

import edu.sysu.gmall.wms.mapper.WareSkuMapper;
import edu.sysu.gmall.wms.entity.WareSkuEntity;
import edu.sysu.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RabbitTemplate rabbitTemplate;

    private static final String LOCK_PREFIX = "stock:lock:";
    private static final String KEY_PREFIX = "stock:info:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuLockVo> checkLock(String orderToken, List<SkuLockVo> lockVos) {
        if (CollectionUtils.isEmpty(lockVos)) {
            throw new OrderException("没有商品锁库存!");
        }
        //1.对所有商品进行验库存 锁库存
        lockVos.forEach(lockVo -> checkAndLock(lockVo));

        //2.检查是否所有商品都锁定成功
        boolean allMatch = lockVos.stream().allMatch(SkuLockVo::getLock);
        if (!allMatch) {
            //有一个不满足则对之前锁住的商品进行解锁
            lockVos.stream().filter(SkuLockVo::getLock).forEach(lockVo -> {
                baseMapper.unlockWare(lockVo.getWareSkuId(), lockVo.getCount());
            });

            return lockVos;
        }
        //3.都满足则只返回null 但是要redis缓存下所有以锁定的货物wareId 关单之后会用到
        redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, lockVos);
        //4.设置超时自动解锁库存 防止关单失败导致库存死锁 发送消息到延时队列
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.ttl", orderToken);
        return null;
    }

    //为了保证验库存和锁库存有原子性 必须加上分布式锁
    private void checkAndLock(SkuLockVo lockVo) {
        RLock lock = redissonClient.getFairLock(LOCK_PREFIX + lockVo.getSkuId());
        lock.lock();
        try {
            Integer count = lockVo.getCount();
            Long skuId = lockVo.getSkuId();
            List<WareSkuEntity> wareSkuEntities = baseMapper.checkWare(count, skuId);
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                lockVo.setLock(false);
                return;
            }
            //TODO:引入大数据接口,查询最佳配送仓库 我们这里直接取了第一个满足仓库
            Long id = wareSkuEntities.get(0).getId();
            if (baseMapper.lockWare(id, count) == 1) {
                lockVo.setLock(true);
                lockVo.setWareSkuId(id);
            }
        } finally {
            lock.unlock();
        }
    }
}