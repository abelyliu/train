package site;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        String startLocation = "阜阳";
        String endLocation = "杭州";
        String date = "2019-04-28";
        Map<String, String> codeMap = getCodeMap();
//        getTicket(date, codeMap.get(startLocation), codeMap.get(endLocation));

    }

    private void getTrainNo(String trainNo, String startLocation, String endLocation, String date) throws IOException {
        Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/czxx/queryByTrainNo?train_no=" + trainNo + "&from_station_telecode=" + startLocation + "&to_station_telecode=" + endLocation + "&depart_date=" + date).execute();
        String body = execute.body();
        Map<String,String> map = new HashMap<>();
        JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("data");
        for (int i = 0; i < jsonArray.size() - 1; i++) {
            if (i == jsonArray.size() - 2) {

            }
            JSONObject cur = (JSONObject) jsonArray.get(i);
            JSONObject next = (JSONObject) jsonArray.get(i + 1);
        }
    }

    private static void getTicket(String date, String startLocation, String endLocation) throws IOException {
        Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=" + date + "&leftTicketDTO.from_station=" + startLocation + "&leftTicketDTO.to_station=" + endLocation + "&purpose_codes=ADULT").ignoreContentType(true).execute();
        JSONObject jsonObject = JSONObject.parseObject(execute.body());
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("result");
        Map<String,String> map = new HashMap<>();
        for (Object o : jsonArray) {
            String str = (String) o;
            String[] split = str.split("\\|");
            if (StringUtils.isNotBlank(split[28]) && !"无".equals(split[28])) {
                System.out.println(split[3]);
            }
        }
    }

    private static Map<String, String> getCodeMap() throws IOException {
        Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/resources/js/framework/station_name.js").execute();
        String body = execute.body();
        int i = body.indexOf("'");
        String content = body.substring(i + 1, body.length() - 2);
        System.out.println(content);
        Map<String, String> codeMap = new HashMap<>();
        String[] split = content.split("\\|");
        for (int i1 = 0; i1 < split.length; i1++) {
            int i2 = i1 % 5;
            if (i2 == 1) {
//                City city = new City();
//                city.setCode(split[i1+1]);
//                city.setName(split[i1]);
                codeMap.put(split[i1], split[i1 + 1]);
                i1 = i1 + 3;
            }
        }
        return codeMap;
    }

//    private static Ticket
}
