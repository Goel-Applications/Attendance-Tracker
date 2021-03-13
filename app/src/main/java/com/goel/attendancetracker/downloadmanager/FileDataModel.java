package com.goel.attendancetracker.downloadmanager;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.goel.attendancetracker.classes.ClassesModel;
import com.google.android.gms.common.util.ArrayUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

public abstract class FileDataModel {


    public static final String[] monthsArray = {"January", "February", "March", "April", "May", "June",
                                                "July", "August", "September", "October", "November", "December"};
    public static final String TITLE = "Attendance Tracker";
    private static final int TITLE_SIZE = 54;
    private static final int TITLE_NAME_SIZE = 34;
    private static final int TEXT_SIZE_SMALL = 22;
    private static final int PRESENT = 0;
    private static final int ABSENT = 1;
    public static Typeface[] fonts;
    public static Bitmap logo, table;
    private final static int safeColor = Color.rgb(4, 170, 81);
    private final static int lowColor = Color.rgb(245, 102, 0);
    private final static int dangerColor = Color.rgb(255, 7, 58);

    public void createFirstPage(ClassDownloadManager manager){
        Paint paint = manager.getPaint();
        Canvas canvas = manager.getCanvas();
        int attendance = manager.getModel().getClassAttendancePercentage();
        int target = manager.getModel().getRequiredAttendance();
        int color;
        if (attendance >= target)
            color = safeColor;
        else if (attendance > target*0.75f)
            color = lowColor;
        else
            color = dangerColor;

        // SET IMAGE
        canvas.drawBitmap(logo, 10, 15, paint);

        // SET TITLE
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(TITLE_SIZE);
        paint.setTypeface(Typeface.create(fonts[2], Typeface.NORMAL));
        canvas.drawText(TITLE, 135, 76, paint);

        //SET TAG LINE
        paint.setTextSize(TEXT_SIZE_SMALL);
        paint.setTypeface(Typeface.create(fonts[3], Typeface.NORMAL));
        canvas.drawText("Track Your Attendance With Ease", 135, 108, paint);
        paint.setStrokeWidth(4);
        canvas.drawLine(10, 130, manager.getPageInfo().getPageWidth()-10, 130, paint);

        // SET ORGANISATION NAME
        paint.setTextSize(36);
        paint.setUnderlineText(true);
        paint.setTypeface(Typeface.create(fonts[4], Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(manager.getOrganisationName().toUpperCase(), (float) manager.getPageInfo().getPageWidth()/2, 185, paint);
        paint.setUnderlineText(false);

        //SET NAME AND TARGET
        paint.setTextSize(TITLE_NAME_SIZE);
        paint.setTypeface(Typeface.create(fonts[3], Typeface.BOLD));
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Subject: ", 150, 240, paint);
        canvas.drawText("Target: ", 150, 290, paint);
        canvas.drawText("Attendance: ", 200, 360, paint);
        paint.setTypeface(Typeface.create(fonts[3], Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(manager.getModel().getClassName().toUpperCase(), 170, 240, paint);
        paint.setTypeface(Typeface.create(fonts[2], Typeface.BOLD));
        canvas.drawText(target + "%", 170, 290, paint);

        // SET ATTENDANCE
        int total = manager.getModel().getPresentCount() + manager.getModel().getAbsentCount();
        String status = manager.getModel().getPresentCount() + "/" + total;
        paint.setTypeface(Typeface.create(fonts[2], Typeface.BOLD));
        canvas.drawText(status, 220, 360, paint);
        paint.setColor(color);
        paint.setTextSize(96);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(attendance + "%", (float) manager.getPageInfo().getPageWidth()/2, 470, paint);
    }

    public void writeAttendance(ClassDownloadManager manager){
        int[] years = getSortedYears(manager);
        HashMap<Integer, int[]> months = getSortedMonths(manager, years);

        for (int year : years){
            for(int month : Objects.requireNonNull(months.get(year))){
                String monthName = monthsArray[month];
                manager.addPage();
                manager.getCanvas().drawBitmap(table, 20, 190, manager.getPaint());
                int[] presentArray = getMonthData(manager.getModel(), year, month, PRESENT);
                int[] absentArray = getMonthData(manager.getModel(), year, month, ABSENT);
                setMonthTitle(manager.getCanvas(), manager.getPaint(), year, monthName, sumOf(presentArray), sumOf(absentArray));
                writeAttendanceArray(manager.getCanvas(), manager.getPaint(), presentArray, absentArray);
                manager.getDocument().finishPage(manager.getPage());
            }
        }
    }

    private void setMonthTitle(Canvas canvas, Paint paint, int year, String month, int presents, int absents){
        int total = presents + absents;
        int percentage = (presents * 100)/total;
        paint.setTypeface(Typeface.create(fonts[3], Typeface.BOLD));
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(0, 0, 0));
        canvas.drawText(month + ", " + year, 40, 50, paint);
        paint.setTextSize(36);
        canvas.drawText("This Month: ", 120, 125, paint);
        paint.setTypeface(Typeface.create(fonts[2], Typeface.BOLD));
        canvas.drawText("(" + percentage + "%)", 385, 125, paint);
        paint.setTypeface(Typeface.create(fonts[2], Typeface.NORMAL));
        canvas.drawText(presents + "/" + total, 330, 125, paint);
    }

    private void writeAttendanceArray(Canvas canvas, Paint paint, int[] presentArray, int[] absentArray){
        final int xPresent = 185;
        final int xAbsent = 303;
        final int yCommon = 307;
        final int X_OFFSET = 360;
        final int Y_OFFSET = 58;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(46);

        int x, y;
        // SET PRESENTS
        x = xPresent;
        y = yCommon;
        paint.setColor(safeColor);

        for (int i = 0; i < presentArray.length; i++) {
            if (i == 15) {
                x = xPresent + X_OFFSET;
                y = yCommon;
            }
            canvas.drawText(String.valueOf(presentArray[i]), x, y, paint);
            y += Y_OFFSET;
        }

        //SET ABSENTS
        x = xAbsent;
        y = yCommon;
        paint.setColor(dangerColor);

        for (int i = 0; i < absentArray.length; i++) {
            if (i == 15) {
                x = xAbsent + X_OFFSET;
                y = yCommon;
            }
            canvas.drawText(String.valueOf(absentArray[i]), x, y, paint);
            y += Y_OFFSET;
        }
    }

    private int[] getMonthData(ClassesModel model, int year, int month, int index){
        int[] data  = new int[31];
        Arrays.fill(data, 0);

        String query = getFormattedValue(year) + getFormattedValue(month);

        JSONObject history;
        try {
            history = new JSONObject(model.getClassHistory());
        } catch (JSONException e) {
            return data;
        }

        try {
            Iterator<String> keys = history.keys();
            while (keys.hasNext()){
                String key = keys.next();
                if (key.substring(0, 6).equals(query)){
                    int date = Integer.parseInt(key.substring(6));
                    JSONArray dateData = (JSONArray) history.get(key);
                    data[date-1] = dateData.getInt(index);
                }
            }
        }catch (JSONException e) {
            return data;
        }

        return data;
    }

    private HashMap<Integer, int[]> getSortedMonths(ClassDownloadManager manager, int[] years){
        HashMap<Integer, int[]> months = new HashMap<>();
        JSONObject history;
        try {
            history = new JSONObject(manager.getModel().getClassHistory());
        } catch (JSONException e) {
            return months;
        }
        for (int year : years) {
            HashSet<Integer> tempMonths = new HashSet<>();
            Iterator<String> keys = history.keys();
            while (keys.hasNext()){
                String  key = keys.next();
                if (Integer.parseInt(key.substring(0, 4)) == year){
                    tempMonths.add(Integer.valueOf(key.substring(4, 6)));
                }
            }
            int[] yearData = ArrayUtils.toPrimitiveArray(tempMonths);
            sort(yearData);
            months.put(year, yearData);
        }
        return months;
    }

    private int[] getSortedYears(ClassDownloadManager manager){
        HashSet<Integer> tempSet = new HashSet<>();
        JSONObject history;
        try {
            history = new JSONObject(manager.getModel().getClassHistory());
        } catch (JSONException e) {
            return new int[]{};
        }
        Iterator<String> keys = history.keys();
        while (keys.hasNext()){
            String year = keys.next().substring(0, 4);
            tempSet.add(Integer.valueOf(year));
        }

        int[] years = ArrayUtils.toPrimitiveArray(tempSet);
        sort(years);
        return years;
    }

    private void sort(int[] array){
        for (int i = 0; i < array.length-1; i++) {
            if (array[i] > array[i+1]){
                int temp = array[i];
                array[i] = array[i+1];
                array[i+1] = temp;
            }
        }
    }

    private int sumOf(int[] array){
        int sum = 0;
        for (int item : array)
            sum += item;
        return sum;
    }

    private String getFormattedValue(int value){
        return (value >= 10) ? String.valueOf(value) : "0" + value;
    }
}
