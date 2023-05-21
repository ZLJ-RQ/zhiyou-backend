package com.rq.zhiyou.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/5/16 14:54
 */
public class Main {
    public static void main(String[] args) {
        String fileName="E:\\Desktop\\1.xlsx";
        synchronousRead(fileName);
    }

    /**
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<DemoData> list = EasyExcel.read(fileName).head(DemoData.class).sheet().doReadSync();
        for (DemoData data : list) {
            System.out.println(data);
        }
    }
}
