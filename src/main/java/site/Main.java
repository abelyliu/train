package site;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Main {

    public static BiMap<String, String> codeMap = HashBiMap.create();

    public static void main(String[] args) throws IOException {
        String startLocation = "阜阳";
        String endLocation = "杭州";
        String date = "2019-10-06";
        getCodeMap();
        getTicket(date, startLocation, endLocation);
//        Map<String, String> headerMap = getHeaderMap();
//        System.out.println(headerMap);

    }

    private static void getTrainNo(String trainNo, String ls, String le, String date) throws IOException {
        String startLocationCode = codeMap.get(ls);
        String endLocationCode = codeMap.get(le);
//                                                      https://kyfw.12306.cn/otn/czxx/queryByTrainNo?train_no=5e000K143820&from_station_telecode=HZH&to_station_telecode=FYH&depart_date=2019-09-19
        Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/czxx/queryByTrainNo?train_no=" + trainNo + "&from_station_telecode=" + startLocationCode + "&to_station_telecode=" + endLocationCode + "&depart_date=" + date)
                                           .headers(getHeaderMap()).ignoreContentType(true).execute();
        String body = execute.body();
        JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("data");
        Integer start = null;
        Integer end = null;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject cur = (JSONObject) jsonArray.get(i);
            String stationName = cur.getString("station_name");
            if (stationName.contains(ls)) {
                start = cur.getInteger("station_no") - 1;
            } else if (stationName.contains(le)) {
                end = cur.getInteger("station_no") - 1;
            }
        }
        List<String> startLocations = new ArrayList<>();
        List<String> endLocations = new ArrayList<>();
        for (int i = start; i >= 0; i--) {
            JSONObject cur = (JSONObject) jsonArray.get(i);
            String stationName = cur.getString("station_name");
            startLocations.add(codeMap.get(stationName));
        }

        for (int i = end; i < jsonArray.size(); i++) {
            JSONObject cur = (JSONObject) jsonArray.get(i);
            String stationName = cur.getString("station_name");
            endLocations.add(codeMap.get(stationName));
        }

        for (String s : startLocations) {
            for (String e : endLocations) {
                getTicket2(date, s, e, trainNo);
            }
        }
    }

    private static void getTicket(String date, String ls, String le) throws IOException {
        String startLocationCode = codeMap.get(ls);
        String endLocationCode = codeMap.get(le);
        Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/leftTicket/queryA?leftTicketDTO.train_date=" + date + "&leftTicketDTO.from_station=" + startLocationCode + "&leftTicketDTO.to_station=" + endLocationCode + "&purpose_codes=ADULT").ignoreContentType(true)
                                          .headers(getHeaderMap()).execute();
        JSONObject jsonObject = JSONObject.parseObject(execute.body());
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("result");
        Map<String, String> map = new HashMap<>();
        for (Object o : jsonArray) {
//            String str = (String) o;
//            String[] split = str.split("\\|");
//            if (StringUtils.isNotBlank(split[28]) && !"无".equals(split[28])) {
//                System.out.println(split[3]+":"+date+":"+ls+":"+le);
//                break;
//            }
        }
        //todo change
        String s = jsonArray.getString(jsonArray.size()-9);
        String[] split = s.split("\\|");
        getTrainNo(split[2], ls, le, date);

    }

    private static void getTicket2(String date, String startLocation, String endLocation, String trainNo) throws IOException {
        Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/leftTicket/queryA?leftTicketDTO.train_date=" + date + "&leftTicketDTO.from_station=" + startLocation + "&leftTicketDTO.to_station=" + endLocation + "&purpose_codes=ADULT")
                                           .headers(getHeaderMap()).ignoreContentType(true).execute();
        JSONObject jsonObject = JSONObject.parseObject(execute.body());
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("result");
        Map<String, String> map = new HashMap<>();
        for (Object o : jsonArray) {
            String str = (String) o;
            String[] split = str.split("\\|");
            if (StringUtils.isNotBlank(split[28]) && !"无".equals(split[28]) && Objects.equals(trainNo, split[2])) {
                System.out.println("卧铺");
                System.out.println(split[3] + ":" + date + ":" + codeMap.inverse().get(startLocation) + ":" + codeMap.inverse().get(endLocation));
                break;
            }

            if (StringUtils.isNotBlank(split[29]) && !"无".equals(split[29]) && Objects.equals(trainNo, split[2])) {
                System.out.println("硬座");
                System.out.println(split[3] + ":" + date + ":" + codeMap.inverse().get(startLocation) + ":" + codeMap.inverse().get(endLocation));
                break;
            }
        }
    }

    private static Map<String, String> getCodeMap() throws IOException {
        boolean exists = Paths.get("1.txt").toFile().exists();
        String content;
        if (!exists) {
            Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/resources/js/framework/station_name.js").execute();
            String body = execute.body();
            int i = body.indexOf("'");
            content = body.substring(i + 1, body.length() - 2);
            System.out.println(content);
            Files.write(Paths.get("1.txt"), body.getBytes("utf-8"));
        } else {
            content = Files.readString(Paths.get("1.txt"));
        }
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

    private static Map<String,String> getHeaderMap() {
        String s = "Host: kyfw.12306.cn\n" +
                "Connection: keep-alive\n" +
                "Cache-Control: no-cache\n" +
                "Accept: */*\n" +
                "X-Requested-With: XMLHttpRequest\n" +
                "If-Modified-Since: 0\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36\n" +
                "Sec-Fetch-Mode: cors\n" +
                "Sec-Fetch-Site: same-origin\n" +
                "Referer: https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc&fs=%E6%9D%AD%E5%B7%9E,HZH&ts=%E9%98%9C%E9%98%B3,FYH&date=2019-09-19&flag=N,N,Y\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,th;q=0.7,id;q=0.6,zh-TW;q=0.5\n" +
                "Cookie: JSESSIONID=49E4B79A6AEECBF623C84B13AE36DF08; _jc_save_wfdc_flag=dc; RAIL_EXPIRATION=1569141643996; RAIL_DEVICEID=LL9NUsGAyPyqaiktu_Cg-TM9PSmjj8YUYqrC_sFkZ2Ab4Qrw1aZ2a7k6Aq7V7FbZXHVhSe75ae0FwoZhUaE9TDlfq1snfb4oLGPRHLcFIAlVqXcfr3_eOJnDKBlKE54VwyyNOcqOIfslBZarEykkawlzrKjXbpPW; _jc_save_toStation=%u961C%u9633%2CFYH; BIGipServerpool_passport=183304714.50215.0000; route=6f50b51faa11b987e576cdb301e545c4; BIGipServerotn=535822858.64545.0000; _jc_save_fromStation=%u676D%u5DDE%2CHZH; _jc_save_fromDate=2019-09-19; _jc_save_toDate=2019-09-19";
        String[] split = s.split("\n");
        Map<String,String> map = new HashMap<>();
        for (String s1 : split) {
            String[] split1 = s1.split(": ");
            map.put(split1[0],split1[1]);
        }
        return map;
    }

//    private static Ticket
}
