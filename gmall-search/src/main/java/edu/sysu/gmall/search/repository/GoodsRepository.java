package edu.sysu.gmall.search.repository;

import edu.sysu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-19 16:23
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
