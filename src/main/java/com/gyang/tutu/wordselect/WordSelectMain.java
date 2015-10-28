package com.gyang.tutu.wordselect;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.gyang.tutu.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by gyang on 15-10-27.
 */
public class WordSelectMain {

    private static final ContentType CONTENT_TYPE = ContentType.create("application/json", Consts.UTF_8);
    private static String logFilePath = StringUtils.EMPTY;
    private static String wordsExcel = StringUtils.EMPTY;

    private static final StopWatch STOP_WATCH = new StopWatch();

    public static void main(String[] args) throws Exception {
        STOP_WATCH.start();
        init();

        File excelFile = new File(wordsExcel);
        List<WordExcelModel> wordExcelModels = null;
        try {
            wordExcelModels = ReadWordFromExcel.readFromExcel(excelFile);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        LogUtil.logInfo("总计读取到 " + wordExcelModels.size() + " 条数据。");
        if(CollectionUtils.isEmpty(wordExcelModels)) {
            LogUtil.logError("读取数据记录为空!");
            return;
        }

        process(wordExcelModels);
    }

    private static void init() throws Exception {
        Properties properties = loadResource();
        wordsExcel = properties.getProperty("excel", StringUtils.EMPTY);
        if(StringUtils.isEmpty(wordsExcel)) {
            LogUtil.logError("未配置Excel数据文件，请检查input.txt是否含有excel属性！");

            throw new Exception("解析input.txt文件异常！");
        }

        logFilePath = properties.getProperty("logFilePath");
        if(StringUtils.isEmpty(logFilePath)) {
            LogUtil.logError("未配置log存放路径，默认使用与当前工程相同的路径！");
            logFilePath = new File("").getCanonicalPath();
        }

        LogUtil.start(logFilePath);
    }

    private static Properties loadResource() throws Exception {
        File resourceFile = null;
        InputStream in = null;
        try {
            resourceFile = new File(new File("").getCanonicalPath(), "input.txt");
            in = new BufferedInputStream(new FileInputStream(resourceFile));

            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.logError("读取配置文件input.txt异常，请检查是否存在该文件！");
            throw new Exception("读取配置文件input.txt异常，请检查是否存在该文件！");
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void process(List<WordExcelModel> wordExcelModels) {
        // 录入数据成功计数器
        int successCount = 0;
        for(WordExcelModel wordExcelModel : wordExcelModels) {
            try {
                String html = htmlText();
                String questionId = getQuestionId(html);
                String codeId = getCodeId(html);
                int numIndex = getNumIndex(html);
                String[] selectOptionIds = getSelectOptionIds(html);

                WordSelectOptionModel wordModel = new WordSelectOptionModel();
                wordModel.setId(questionId);
                wordModel.setCode(codeId);
                wordModel.setNum(String.valueOf(numIndex));
                wordModel.setStem(wordExcelModel.getStem());
                wordModel.setAnalytical(wordExcelModel.getAnalysis());
                wordModel.setOptions(Lists.newArrayList(new SelectOptionModel(selectOptionIds[0], wordExcelModel.getSelectOptions().get(0), questionId, "", 1),
                        new SelectOptionModel(selectOptionIds[1], wordExcelModel.getSelectOptions().get(1), questionId, "", 2),
                        new SelectOptionModel(selectOptionIds[2], wordExcelModel.getSelectOptions().get(2), questionId, "", 3),
                        new SelectOptionModel(selectOptionIds[3], wordExcelModel.getSelectOptions().get(3), questionId, "", 4)));
                for(SelectOptionModel selectOptionModel : wordModel.getOptions()) {
                    if(StringUtils.equals(StringUtils.trim(selectOptionModel.getContent()), StringUtils.trim(wordExcelModel.getCorrectOption()))) {
                        wordModel.setCorrectOptionses(Lists.newArrayList(String.valueOf(selectOptionModel.getId())));
                        break;
                    }
                }
                if(CollectionUtils.isEmpty(wordModel.getCorrectOptionses())) {
                    LogUtil.logError("未匹配到正确的选项！ " + JSON.toJSONString(wordExcelModel));
                    continue;
                }

                JSONObject paramJSON = buildPostData(wordModel);
                System.out.println(paramJSON.toString());

                String result = postData(paramJSON.toString());
                try {
                    JSONObject resultJSON = new JSONObject(result);
                    if(resultJSON != null && resultJSON.has("success")) {
                        boolean success = resultJSON.getBoolean("success");
                        if(success) {
                            LogUtil.logInfo("数据录入成功！ row = " + JSON.toJSONString(wordExcelModel));
                            successCount++;
                        } else {
                            LogUtil.logError("数据录入失败！ row = " + JSON.toJSONString(wordExcelModel));
                        }
                    }
                } catch (Exception e) {
                    LogUtil.logError("数据录入失败！ row = " + JSON.toJSONString(wordExcelModel) + ", error msg: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                LogUtil.logError("数据录入失败！ row = " + JSON.toJSONString(wordExcelModel) + ", error msg: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        STOP_WATCH.stop();
        LogUtil.logInfo("数据录入完毕，总计 " + wordExcelModels.size() + " 条数据，录入成功总计 " + successCount + " 条数据，一共耗时 " + (STOP_WATCH.getTime() / 1000.0) + "秒！");
    }

    private static String htmlText() throws Exception {
        CloseableHttpClient httpClient = HttpClients.custom().build();
        HttpGet httpGet = new HttpGet("http://www.tutumet.com/education/question/addEngVc");
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");
        httpGet.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Cookie", "_ttyk_=1DC5AB8D46800B869DA99077864D1985; _ttyk_session_=h+wD1Qn2aQuSv3OphjUt10ZLRp+gpF9S");
        httpGet.setHeader("Host", "www.tutumet.com");
        httpGet.setHeader("Referer", "http://www.tutumet.com/education/question/engVc");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.120 Safari/537.36");
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        String result = EntityUtils.toString(httpResponse.getEntity());

        System.out.println(result);
        return result;
    }

    private static String getCodeId(String htmlText) throws Exception {
        Parser parser = new Parser();
        parser.setInputHTML(htmlText);

        // 过滤questionId
        NodeFilter codeIdFilter = new HasAttributeFilter("id", "code");
        NodeList codeNodeList = parser.extractAllNodesThatMatch(codeIdFilter);
        int questionIdNodeSize = codeNodeList.size();
        if(questionIdNodeSize != 1) {
            System.out.println("ERROR");
            return null;
        }

        Node questionIdNode = codeNodeList.elementAt(0);
        String codeIdText = questionIdNode.getText();
        if(StringUtils.isNotEmpty(codeIdText)) {
            String[] codeIdValueArr = codeIdText.split(" ");
            if(codeIdValueArr != null && codeIdValueArr.length == 4) {
                String codeIdValue = StringUtils.substringBetween(codeIdValueArr[2], "\"", "\"");

                return codeIdValue;
            }
        }

        return null;
    }

    private static String getQuestionId(String htmlText) throws Exception {
        Parser parser = new Parser();
        parser.setInputHTML(htmlText);

        // 过滤questionId
        NodeFilter questionIdFilter = new HasAttributeFilter("id", "questionId");
        NodeList questionNodeList = parser.extractAllNodesThatMatch(questionIdFilter);
        int questionIdNodeSize = questionNodeList.size();
        if(questionIdNodeSize != 1) {
            System.out.println("ERROR");
            return null;
        }

        Node questionIdNode = questionNodeList.elementAt(0);
        String questtionIdText = questionIdNode.getText();
        if(StringUtils.isNotEmpty(questtionIdText)) {
            String[] questionIdValueArr = questtionIdText.split(" ");
            if(questionIdValueArr != null && questionIdValueArr.length == 4) {
                String questionIdValue = StringUtils.substringBetween(questionIdValueArr[2], "\"", "\"");

                return questionIdValue;
            }
        }

        return null;
    }

    private static int getNumIndex(String htmlText) throws Exception {
        Parser parser = new Parser();
        parser.setInputHTML(htmlText);

        // 过滤num
        NodeFilter numFilter = new HasAttributeFilter("id", "num");
        NodeList numNodeList = parser.extractAllNodesThatMatch(numFilter);
        int numIdNodeSize = numNodeList.size();
        if(numIdNodeSize != 1) {
            LogUtil.logError("获取页面上 num 信息大于一条信息， html = " + htmlText);
            return -1;
        }

        Node numNode = numNodeList.elementAt(0);
        String numText = numNode.getText();
        if(StringUtils.isNotEmpty(numText)) {
            String[] numValueArr = numText.split(" ");
            if(numValueArr != null && numValueArr.length == 4) {
                String numValue = StringUtils.substringBetween(numValueArr[2], "\"", "\"");

                return Integer.valueOf(numValue).intValue();
            }
        }

        return -1;
    }

    private static String[] getSelectOptionIds(String htmlText) throws Exception {
        String[] ids = new String[4];

        Parser parser = new Parser();
        parser.setInputHTML(htmlText);
        // 过滤选项ID
        NodeFilter attributeNodeFilter = new HasAttributeFilter("class", "form-group oplist");
        NodeList nodeList = parser.extractAllNodesThatMatch(attributeNodeFilter);
        // 4个选项
        int nodeSize = nodeList.size();
        if(nodeSize != 4) {
            LogUtil.logError("获取页面上选项ID信息不等于四条！ html = " + htmlText);
            return null;
        }

        for(int i = 0; i < nodeSize; i++) {
            Node node = nodeList.elementAt(i);
            String nodeValue = node.getText();
            if(StringUtils.isNotEmpty(nodeValue)) {
                String[] nodeValueArr = nodeValue.split("id=");
                if(nodeValueArr != null && nodeValueArr.length == 2) {
                    String id = StringUtils.substringBetween(nodeValueArr[1], "\"", "\"");
                    ids[i] = id;
                } else {
                    LogUtil.logError("获取页面上选项ID信息异常！ html = " + htmlText);
                    return null;
                }
            }
        }

        return ids;
    }

    private static JSONObject buildPostData(WordSelectOptionModel model) {
        JSONObject result = new JSONObject();

        result.put("id", model.getId());
        result.put("code", model.getCode());
        result.put("num", model.getNum());
        result.put("stem", model.getStem());
        result.put("analytical", model.getAnalytical());

        JSONArray optionsArr = new JSONArray();
        result.put("options", optionsArr);
        for(SelectOptionModel selectOptionModel : model.getOptions()) {
            JSONObject selectOptionJSON = new JSONObject();
            selectOptionJSON.put("id", selectOptionModel.getId());
            selectOptionJSON.put("content", selectOptionModel.getContent());
            selectOptionJSON.put("questionId", selectOptionModel.getQuestionId());
            selectOptionJSON.put("stemId", selectOptionModel.getStemId());
            selectOptionJSON.put("num", selectOptionModel.getNum());

            optionsArr.put(selectOptionJSON);
        }

        JSONArray correctOptionArr = new JSONArray();
        result.put("correctOptionses", correctOptionArr);
        for(String correctOption : model.getCorrectOptionses()) {
            correctOptionArr.put(correctOption);
        }

        return result;
    }

    private static String postData(String paramStr) throws Exception {
        CloseableHttpClient httpClient = HttpClients.custom().build();
        HttpPost httpPost = new HttpPost("http://www.tutumet.com/education/question/saveQuestionEngVc");
        httpPost.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        httpPost.setHeader("Accept-Encoding", "gzip,deflate");
        httpPost.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setHeader("Cookie", "_ttyk_=1DC5AB8D46800B869DA99077864D1985; _ttyk_session_=h+wD1Qn2aQuSv3OphjUt10ZLRp+gpF9S");
        httpPost.setHeader("Host", "www.tutumet.com");
        httpPost.setHeader("Origin", "http://www.tutumet.com");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.120 Safari/537.36");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

        httpPost.setEntity(new StringEntity(paramStr, CONTENT_TYPE));

        HttpEntity httpEntity = httpClient.execute(httpPost).getEntity();
        if(httpEntity != null) {
            String result = EntityUtils.toString(httpEntity);
            return result;
        }

        return StringUtils.EMPTY;
    }
}
