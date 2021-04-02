package com.softium.datacenter.paas.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import com.softium.datacenter.paas.api.dto.DistributorDTO;
import com.softium.datacenter.paas.api.entity.DataOne;
import com.softium.datacenter.paas.api.entity.OriginSale;
import com.softium.datacenter.paas.api.entity.Test;
import com.softium.datacenter.paas.web.automap.methodEnum;
import com.softium.datacenter.paas.web.manage.DistributorService;
import com.softium.datacenter.paas.api.mapper.DataOneMapper;
import com.softium.datacenter.paas.api.mapper.OriginSaleMapper;
import com.softium.datacenter.paas.api.mapper.TestMapper;
import com.softium.datacenter.paas.web.service.TestService;
import com.softium.datacenter.paas.web.utils.poi.ExeclDownloadUtil;
import com.softium.framework.common.dto.ActionResult;
import com.softium.framework.common.dto.PageRequest;
import com.softium.framework.common.query.Condition;
import com.softium.framework.common.query.Criteria;
import com.softium.framework.common.query.Operator;
import com.softium.framework.orm.common.ORMapping;
import com.softium.framework.orm.common.database.Table;
import com.softium.framework.orm.common.mybatis.sharding.ShardingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Fanfan.Gong
 **/
@RestController
@RequestMapping("test")
public class TestController {
    @Autowired
    private DistributorService distributorService;
    @Autowired
    private TestService testService;
    @Autowired
    private TestMapper testMapper;
    @Autowired
    private DataOneMapper dataOneMapper;
    @Resource
    ShardingManager shardingManager;
    @Autowired
    OriginSaleMapper originSaleMapper;
    private static final Integer BATCH_SIZE = 100;
    @GetMapping("add")
    public ActionResult<List<DistributorDTO>> add() {
        DistributorDTO distributor = new DistributorDTO();
        distributor.setName("经销商2");
        return distributorService.searchDistributor(distributor);
    }
    @GetMapping("addtest")
    public String addtest(){
        List<Test> list=new ArrayList<>();
        String zhi="hello,hello,hello,nihao,wang,world,king";
        List<String> stringList= Arrays.asList(zhi.split(","));
        for(String aa:stringList){
            Test test=new Test();
            test.setId(UUID.randomUUID().toString());
            test.setTestName(aa);
            test.setVersion(Long.valueOf("12345"));
            test.setCreateBy("55555");
            test.setUpdateBy("66666");
            test.setCreateTime(new Date());
            test.setUpdateTime(new Date());
            test.setIsDeleted(0);
            list.add(test);
        }
        testMapper.batchInsert(list);
        return "添加成功";
    }
    @GetMapping("addone")
    public String addone(){
        List<DataOne> list=new ArrayList<>();
        String zhi="hello,hello,hello,nihao,wang,world,king";
        List<String> stringList= Arrays.asList(zhi.split(","));
        for(String aa:stringList){
            DataOne test=new DataOne();
            test.setId(UUID.randomUUID().toString());
            test.setTestName(aa);
            test.setVersion(Long.valueOf("99999"));
            test.setCreateBy("88888");
            test.setUpdateBy("55555");
            list.add(test);
        }
        dataOneMapper.batchInsert(list);
        return "添加成功";
    }
    @GetMapping("chaxun")
    public String chaxun(){
        Class<?> poClass=Test.class;
        Table table= ORMapping.get(poClass);
        String value1=shardingManager.getShardingTableNameByValue(table,"wang");
        String value2=shardingManager.getShardingTableNameByValue(ORMapping.get(DataOne.class),"wang");
        List<Map<String,Object>> list=testMapper.testselect(value1,value2,"wang");

        return JSONArray.parseArray(JSONArray.toJSON(list).toString()).toJSONString();
    }
    @GetMapping("shijian")
    public void shijian(){
        StopWatch watch=new StopWatch();
        watch.start();
        List<Test> a= new ArrayList<>();
        Random random=new Random();
        for(int i=0;i<10000;i++){
            Test test=new Test();
            test.setId(UUID.randomUUID().toString());
            test.setIsDeleted(0);
            test.setTestName(String.valueOf(random.nextInt(5)+1));
            test.setVersion((long) i);
            a.add(test);
        }
        /*Set<String> idSet = new HashSet<>();
        Map<String, Object> objectMap = new HashMap<>();
        for(Test po:a){
            String name= po.getTestName();
            idSet.add(name);
            objectMap.put(name,idSet);
        }
        Map<String, String> tableNameMap=shardingManager.getShardingTableNameByValues(ORMapping.get(Test.class),idSet);
        System.out.println("共分为:"+tableNameMap.size());*/
        if(!CollectionUtils.isEmpty(a)) {
            int size = a.size();
            int count = size % BATCH_SIZE == 0 ? size / BATCH_SIZE : (size / BATCH_SIZE) + 1;
            for (int i = 0; i < count; i++) {
                int start = i * BATCH_SIZE;
                int end = (i + 1) * BATCH_SIZE;
                if (end > a.size()) {
                    end = a.size();
                }
            }
        }
        //自带插入
        //testMapper.batchInsert(a);
        watch.stop();
        System.out.println("共耗时:"+watch.getTotalTimeSeconds()+"秒");
    }
    @GetMapping("fenye")
    public ActionResult<PageInfo<List<OriginSale>>> fenye(){
        PageRequest<OriginSale> pageRequest=new PageRequest<>();
        pageRequest.setNeedCount(true);
        pageRequest.setNeedPaging(true);
        pageRequest.setPageSize(100);
        pageRequest.setPageNo(1);
        Criteria<OriginSale> criteria=new Criteria<>();
        criteria.addCriterion(new Condition("tenantId", Operator.equal,"5d4cbdc6-fcaa-11ea-8cfb-00163e16f32a"));
        pageRequest.setCriteria(criteria);
        ActionResult actionResult=new ActionResult<>();
        actionResult.setData(originSaleMapper.findPage(pageRequest));
        return actionResult;
    }
    @GetMapping("hashzhi")
    public void zhi(){
        for(int i=0;i<1000;i++){
            String originTable=shardingManager.getShardingTableNameByValue(ORMapping.get(OriginSale.class),"5d4cbdc6-fcaa-11ea-8cfb-00163e16f32a");
            System.out.println("生成表:"+originTable);
        }
    }
    @GetMapping("fenbiao")
    public String fenbiao(@RequestParam("code") String code){
        String originTable=shardingManager.shardingTable(ORMapping.get(OriginSale.class),code);
        return originTable;
        }
    @GetMapping("meiju")
    public void meiju(){
        String zhi="PM";
        switch (zhi){
            case "PM":
                methodEnum.PM.handleMethod("123456");
                break;
        }
    }
    @GetMapping("xiazai")
    public Object downloadModel(){
        ResponseEntity<InputStreamResource> response = null;
        try {
            String path="E:\\demopage\\uploaddata\\测试11606459204047.xlsx";
            String filename="测试11606459204047";
            response = ExeclDownloadUtil.downloadFile(path, filename, "导入模板");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return response;
    }
}
