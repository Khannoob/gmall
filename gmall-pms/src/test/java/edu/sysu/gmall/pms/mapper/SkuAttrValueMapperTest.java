package edu.sysu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SkuAttrValueMapperTest {

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Test
    void queryMappingBySpuId() {

        List<Map<String,Object>> map = skuAttrValueMapper.queryMappingBySpuId(Arrays.asList(128l, 129l, 130l, 131l));
        System.out.println(map);
    }
}