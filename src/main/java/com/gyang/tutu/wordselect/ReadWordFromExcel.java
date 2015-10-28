package com.gyang.tutu.wordselect;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.gyang.tutu.util.LogUtil;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by declan.guo on 15-10-28.
 */
public class ReadWordFromExcel {

    /**
     * 词汇单选（4选1）正确列数为7列数据
     */
    private static final int COLUMN_SIZE = 7;

    public static List<WordExcelModel> readFromExcel(File excelFile) throws Exception {
        // 创建输入流
        InputStream stream = null;
        Workbook rwb = null;
        try {
            stream = new FileInputStream(excelFile);
            // 获取Excel文件对象
            rwb = Workbook.getWorkbook(stream);
            // 获取文件的指定工作表 默认的第一个
            Sheet sheet = rwb.getSheet("词汇单选");
            if(sheet.getColumns() == COLUMN_SIZE) {
                List<WordExcelModel> wordExcelModels = Lists.newArrayList();
                WordExcelModel wordExcelModel = null;
                Cell cell = null;
                String[] strArr = null;
                // 行数(表头的目录不需要，从1开始)
                for (int i = 1; i < sheet.getRows(); i++) {
                    // 创建一个数组 用来存储每一列的值
                    strArr = new String[sheet.getColumns()];
                    // 列数
                    for (int j = 0; j < sheet.getColumns(); j++) {
                        // 获取第i行，第j列的值
                        cell = sheet.getCell(j, i);
                        strArr[j] = cell.getContents();
                    }

                    if(strArr.length == COLUMN_SIZE) {
                        wordExcelModel = new WordExcelModel(strArr[0], Lists.newArrayList(strArr[1], strArr[2], strArr[3], strArr[4]), strArr[5], strArr[6]);
                        wordExcelModels.add(wordExcelModel);
                    } else {
                        LogUtil.logError("数据列数不对, 应该有7列属性信息, row = " + Arrays.toString(strArr));
                    }
                }

                return wordExcelModels;
            } else {
                LogUtil.logError("数据列数不对！");
                return Lists.newArrayList();
            }
        } catch (Exception e) {
            LogUtil.logError("读取Excel数据异常！");
            throw e;
        } finally {
            if(rwb != null) {
                rwb.close();
            }
            if(stream != null) {
                stream.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        File f = new File("/home/gyang/words.xls");
        System.out.println(JSON.toJSONString(readFromExcel(f)));
    }
}
