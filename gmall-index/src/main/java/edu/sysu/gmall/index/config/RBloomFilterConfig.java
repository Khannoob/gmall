package edu.sysu.gmall.index.config;

import edu.sysu.gmall.index.feign.GmallPmsClient;
import edu.sysu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-28 20:52
 */
@Configuration
public class RBloomFilterConfig {
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    GmallPmsClient gmallPmsClient;
    @Bean
    public RBloomFilter rBloomFilter(){
        RBloomFilter<Object> bloom = redissonClient.getBloomFilter("bloom");
        bloom.tryInit(501, 0.03);
        List<CategoryEntity> cates = gmallPmsClient.queryCategoriesByPid(0l).getData();
        cates.forEach(categoryEntity -> {
            bloom.add(categoryEntity.getId());
        });
        return bloom;
    }
}
