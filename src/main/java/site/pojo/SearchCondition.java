package site.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class SearchCondition {
    private String startLocation;
    private String endLocation;
    private String date;
    private String trainNo;
}
