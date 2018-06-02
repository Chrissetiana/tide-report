package com.chrissetiana.tidereport;

public class Tsunami {

    public final String title;
    public final long time;
    public final int alert;

    public Tsunami(String earthquakeTitle, long time, int tsunamiAlert) {
        this.title = earthquakeTitle;
        this.time = time;
        this.alert = tsunamiAlert;
    }
}
