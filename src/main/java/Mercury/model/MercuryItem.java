package main.java.Mercury.model;

import java.util.Date;

/**
 * Created by Данил on 04.04.2019.
 */
public class MercuryItem {
    long id;
    Date date;
    String nameOfProduct;
    Double volume;
    String productionDate;
    String companySender;
    String url;
    Company company;

    @Override
    public String toString() {
        return "=====================\n" +
                "id : " + id + "\n" +
                "date : " + date + "\n" +
                "nameOfProduct : " + nameOfProduct + "\n" +
                "volume : " + volume + "\n" +
                "productionDate : " + productionDate + "\n" +
                "companySender : " + companySender + "\n" +
                "url : " + url;
    }
}
