package jp.co.metateam.library.model;

import java.util.List;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

/**
 * カレンダーマスタDTO
 */
@Getter
@Setter
public class CalendarDto {

    private String title;

    private int stockCount;

    public List<RentableNumDto> rentableNum;{
        this.rentableNum = new ArrayList<>();
    }

}
