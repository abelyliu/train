package site.pojo;

import lombok.Data;

import java.sql.Time;

@Data
public class TrainInfo {
    private String trainNo;
    private String startLocation;
    private String endLocation;
}
