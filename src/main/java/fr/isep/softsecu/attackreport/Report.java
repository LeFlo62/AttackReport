package fr.isep.softsecu.attackreport;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Report {

    private String address;
    private StringBuffer report;

    public Report(String address) {
        this.address = address;
        this.report = new StringBuffer();
        append("Report for " + address);
    }

    public void append(String str) {
        report.append(str);
        report.append(System.lineSeparator());
    }

    public void addSection(String title) {
        report.append(System.lineSeparator());
        report.append("========================================");
        report.append(System.lineSeparator());
        report.append(title);
        report.append(System.lineSeparator());
        report.append("========================================");
        report.append(System.lineSeparator());
    }

    public void print() {
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File file = new File(address.split(":")[0] + "_report-" + date + ".txt");
        try{
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(report.toString());
            fw.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
