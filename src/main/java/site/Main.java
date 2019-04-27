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
import java.time.format.FormatStyle;
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
        String date = "2019-05-04";
        getCodeMap();
        getTicket(date, startLocation, endLocation);

    }

    private static void getTrainNo(String trainNo, String ls, String le, String date) throws IOException {
        String startLocationCode = codeMap.get(ls);
        String endLocationCode = codeMap.get(le);

        Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/czxx/queryByTrainNo?train_no=" + trainNo + "&from_station_telecode=" + startLocationCode + "&to_station_telecode=" + endLocationCode + "&depart_date=" + date).ignoreContentType(true).execute();
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
        Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=" + date + "&leftTicketDTO.from_station=" + startLocationCode + "&leftTicketDTO.to_station=" + endLocationCode + "&purpose_codes=ADULT").ignoreContentType(true).execute();
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
        String s = jsonArray.getString(jsonArray.size() - 9);
        String[] split = s.split("\\|");
        getTrainNo(split[2], ls, le, date);

    }

    private static void getTicket2(String date, String startLocation, String endLocation, String trainNo) throws IOException {
        Connection.Response execute = Jsoup.connect("https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=" + date + "&leftTicketDTO.from_station=" + startLocation + "&leftTicketDTO.to_station=" + endLocation + "&purpose_codes=ADULT").ignoreContentType(true).execute();
        JSONObject jsonObject = JSONObject.parseObject(execute.body());
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("result");
        Map<String, String> map = new HashMap<>();
        for (Object o : jsonArray) {
            String str = (String) o;
            String[] split = str.split("\\|");
            if (StringUtils.isNotBlank(split[28]) && !"无".equals(split[28]) && Objects.equals(trainNo, split[2])) {
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

//    private static Ticket
}
