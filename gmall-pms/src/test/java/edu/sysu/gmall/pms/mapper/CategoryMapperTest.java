package edu.sysu.gmall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CategoryMapperTest {

    @Autowired
    CategoryMapper categoryMapper;
    @Test
    void queryL2CategoriesByPid() {
        categoryMapper.queryL2CategoriesByPid(1l).forEach(System.out::println);
    }
}