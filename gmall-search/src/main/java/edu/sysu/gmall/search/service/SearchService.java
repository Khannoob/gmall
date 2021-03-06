package edu.sysu.gmall.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sysu.gmall.pms.entity.BrandEntity;
import edu.sysu.gmall.pms.entity.CategoryEntity;
import edu.sysu.gmall.search.pojo.Goods;
import edu.sysu.gmall.search.pojo.SearchParamVo;
import edu.sysu.gmall.search.pojo.SearchResponseAttrVo;
import edu.sysu.gmall.search.pojo.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-05-20 12:34
 */
@Service
public class SearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Autowired
    private final static ObjectMapper MAPPER = new ObjectMapper();

    public SearchResponseVo search(SearchParamVo searchParamVo) {
        try {
            SearchResponse searchResponse = restHighLevelClient.search(buildDSL(searchParamVo), RequestOptions.DEFAULT);
            SearchResponseVo responseVo = new SearchResponseVo();
            Integer pageNum = searchParamVo.getPageNum();
            Integer pageSize = searchParamVo.getPageSize();
            responseVo.setPageNum(pageNum);
            responseVo.setPageSize(pageSize);
            return parseResult(searchResponse,responseVo);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private SearchResponseVo parseResult(SearchResponse searchResponse,SearchResponseVo responseVo) {
        SearchHits hits = searchResponse.getHits();
        responseVo.setTotal(hits.getTotalHits());
        SearchHit[] hitsHits = hits.getHits();
        //??????Goods _source ??? highlight??????
        if (hitsHits != null && hitsHits.length != 0) {
            responseVo.setGoodsList(Stream.of(hitsHits).map(hitsHit -> {
                try {
                    String sourceAsString = hitsHit.getSourceAsString();

                    Goods goods = MAPPER.readValue(sourceAsString, Goods.class);
                    HighlightField highlightField = hitsHit.getHighlightFields().get("title");
                    String title = highlightField.getFragments()[0].toString();
                    goods.setTitle(title);
                    return goods;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }).collect(Collectors.toList()));
        }
        //?????????????????? Aggr
        Map<String, Aggregation> aggregations = searchResponse.getAggregations().asMap();
        //??????brandId??????
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregations.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            responseVo.setBrands(buckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                brandEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                //?????????Map
                Map<String, Aggregation> subAggregations = ((Terms.Bucket) bucket).getAggregations().asMap();
                //brandName??????
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) subAggregations.get("brandNameAgg");
                brandEntity.setName(brandNameAgg.getBuckets().get(0).getKeyAsString());
                //logo??????
                ParsedStringTerms logoAgg = (ParsedStringTerms) subAggregations.get("logoAgg");
                brandEntity.setLogo(logoAgg.getBuckets().get(0).getKeyAsString());
                return brandEntity;
            }).collect(Collectors.toList()));
        }
        //??????categoryIdAgg??????
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregations.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryIdAggBuckets)) {
            responseVo.setCategoryEntities(categoryIdAggBuckets.stream().map(categoryIdAggBucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(((Terms.Bucket) categoryIdAggBucket).getKeyAsNumber().longValue());
                //??????categoryNameAgg??????
                Map<String, Aggregation> subAggregations = ((Terms.Bucket) categoryIdAggBucket).getAggregations().asMap();
                ParsedStringTerms categoryNameAgg = (ParsedStringTerms) subAggregations.get("categoryNameAgg");
                categoryEntity.setName(categoryNameAgg.getBuckets().get(0).getKeyAsString());
                return categoryEntity;
            }).collect(Collectors.toList()));
        }
        //??????searchAttrs????????????
        ParsedNested attrAgg = (ParsedNested) aggregations.get("attrAgg");
        // ????????????????????????????????????id????????????
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)) {
            responseVo.setFilters(attrIdAggBuckets.stream().map(attrIdAggBucket -> {
                SearchResponseAttrVo responseAttrVo = new SearchResponseAttrVo();
                responseAttrVo.setAttrId(((Terms.Bucket) attrIdAggBucket).getKeyAsNumber().longValue());
                // ????????????????????????????????????Name????????????
                ParsedStringTerms attrNameAgg = ((Terms.Bucket) attrIdAggBucket).getAggregations().get("attrNameAgg");
                responseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                // ????????????????????????????????????Value????????????
                ParsedStringTerms attrValueAgg = ((Terms.Bucket) attrIdAggBucket).getAggregations().get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrValueAggBuckets)) {
                    List<String> attrValues = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    responseAttrVo.setAttrValues(attrValues);
                }
                return responseAttrVo;
            }).collect(Collectors.toList()));
        }


        responseVo.setPageNum(responseVo.getPageNum());
        responseVo.setPageSize(responseVo.getPageSize());
        responseVo.setTotal(responseVo.getTotal());
        return responseVo;
    }

    private SearchRequest buildDSL(SearchParamVo searchParamVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        SearchRequest request = new SearchRequest(new String[]{"goods"}, sourceBuilder);
        //1.????????????
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String keyword = searchParamVo.getKeyword();
        List<Long> brandIds = searchParamVo.getBrandId();
        List<Long> categoryIds = searchParamVo.getCategoryId();
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        Boolean store = searchParamVo.getStore();
        List<String> props = searchParamVo.getProps();
        //1.1. ???????????????
        if (StringUtils.isNotBlank(keyword)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword));
        }
        //1.2. ????????????
        if (!CollectionUtils.isEmpty(brandIds)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandIds));
        }
        //1.3. ????????????
        if (!CollectionUtils.isEmpty(categoryIds)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", categoryIds));
        }
        //1.4. ??????????????????
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(priceFrom).lte(priceTo));
        //1.5. ???????????????
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("store", store));
        }
        //1.6. ?????????????????? ["5:256G-512G", "4:8G-12G"]
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                String[] attrs = StringUtils.split(prop, ":");
                if (attrs != null && attrs.length == 2) {
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrId", attrs[0]));
                    String[] attrValues = StringUtils.split(attrs[1], "-");
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));
                }
            });
        }

        sourceBuilder.query(boolQueryBuilder);
        //2.??????
        Integer sort = searchParamVo.getSort();
        switch (sort) {
            case 1:
                sourceBuilder.sort("price", SortOrder.DESC);
                break;
            case 2:
                sourceBuilder.sort("price", SortOrder.ASC);
                break;
            case 4:
                sourceBuilder.sort("sales", SortOrder.DESC);
                break;
            case 3:
                sourceBuilder.sort("createTime", SortOrder.DESC);
                break;
            default:
                break;
        }
        //3.??????
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        //4.??????
        sourceBuilder.highlighter(new HighlightBuilder().field("title")
                .preTags("<font style='color:pink'>").postTags("</font>"));
        //5.??????
        //5.1. ????????????
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        //5.2. ????????????
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //5.3. ??????????????????
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));
        //6.???????????????
        System.out.println(sourceBuilder);
        sourceBuilder.fetchSource(new String[]{"tile","subTile","defaultImage","price","skuId"}, null);
        return request;
    }
}
