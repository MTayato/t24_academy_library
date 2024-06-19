package jp.co.metateam.library.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.Date;

@Getter
@Setter

public class RentableNumDto {
    public Object dayOfExpected;

    public List<String>stockIdList;{
        this.stockIdList = new ArrayList<>();
    }
    public Date linkDate;
}
