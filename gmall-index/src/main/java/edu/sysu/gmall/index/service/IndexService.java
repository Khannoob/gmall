package edu.sysu.gmall.index.service;

import edu.sysu.gmall.index.feign.GmallPmsClient;
import edu.sysu.gmall.pms.api.GmallPmsApi;
import edu.sysu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-24 18:26
 */
@Service
public class IndexService {

    @Autowired
    GmallPmsClient gmallPmsClient;
    public List<CategoryEntity> getL1Categories() {
        return gmallPmsClient.queryCategoriesByPid(0l).getData();
    }
}
