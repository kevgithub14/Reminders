package com.anrlabs.reminders;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.anrlabs.locationreminder.GeoFenceMain;


/**
 * Created by Archie on 12/8/2014.
 */
public class NewReminder extends Activity {
    Fragment timeFragment = new TimeFragment();
    Fragment mapFragment = new LocationFragment();

    private Double longitude;
    private Double latitude;
    private Long radius;
    private String location_name;
    private static String time, date;
    protected ContentValues dataFiller;
    protected EditText titleCarrier, memoCarrier;
    private int tabSelected=0;
    private PendingIntent pendingIntent;
    private AlarmManager manager;
    private Intent alarmIntent;


    /////////////////////////////////// added by michael //////////////////////////////////////////
    private long rowID;
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void setRadius(Long radius) {
        this.radius = radius;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public void setLocationName(String location) {
        this.location_name = location;
    }

    public static void setTime(String sTime)
    {
        time = sTime;
    }

    public static void setDate(String sDate)
    {
        date = sDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_new);

        titleCarrier = (EditText) findViewById(R.id.titleBox);
        memoCarrier = (EditText) findViewById(R.id.memoBox);

    }


    //method to handle fragment selected: time or location (default time)
    public void selectFrag(View fragSelected)
    // public void selectFrag(View v)
    {
        if (fragSelected.getId() == R.id.buttonLocationFrag) {
            tabSelected = 1;
            if (getFragmentManager().findFragmentByTag("map") != null) {
                if (getFragmentManager().findFragmentByTag("map").isAdded())
                {
                    getFragmentManager().beginTransaction().hide(timeFragment).commit();
                    getFragmentManager().beginTransaction().show(mapFragment).commit();
                }
            } else{
                if (getFragmentManager().findFragmentByTag("time") != null
                        && getFragmentManager().findFragmentByTag("time").isAdded())
                {
                 getFragmentManager().beginTransaction().hide(timeFragment).commit();
                }
                getFragmentManager().beginTransaction().add(R.id.main_frag, mapFragment,"map").commit();
            }

         } else
        if (fragSelected.getId() == R.id.buttonTimeFrag) {
            tabSelected = 2;
            if (getFragmentManager().findFragmentByTag("time") != null ) {
                if (getFragmentManager().findFragmentByTag("time").isAdded())
                {
                    getFragmentManager().beginTransaction().hide(mapFragment).commit();
                    getFragmentManager().beginTransaction().show(timeFragment).commit();
                }
            } else{
                if (getFragmentManager().findFragmentByTag("map")!=null
                        && getFragmentManager().findFragmentByTag("map").isAdded()) {
                    getFragmentManager().beginTransaction().hide(mapFragment).commit();
                }
                getFragmentManager().beginTransaction().add(R.id.main_frag, timeFragment,"time").commit();
            }
       }
    }

    //saving data
    public void saveData(View v)
    {
        if(checkForData()) {
            //for timer alarm
            if (tabSelected == 2) {
                settingAlarm();
            }

            savingData();
            super.onBackPressed();
        }
        else
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(
                    NewReminder.this);

            alert.setTitle("No Data");
            alert.setMessage("Please Fill in To-Do and select a Time/Locations");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }

            });
            alert.show();
        }
    }

    //validating entry before save
    public boolean checkForData()
    {
        if(titleCarrier.getText().toString().trim().equals("")) {
            return false;
        }
        else if(location_name==null && date==null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    //populating DataBase
    public void savingData()
    {
        titleCarrier = (EditText) findViewById(R.id.titleBox);
        memoCarrier = (EditText) findViewById(R.id.memoBox);

        dataFiller = new ContentValues();
        dataFiller.put(DatabaseHelper.TITLE, titleCarrier.getText().toString());
        dataFiller.put(DatabaseHelper.MESSAGE, memoCarrier.getText().toString());

        switch (tabSelected)
        {
            case 1:
                dataFiller.put(DatabaseHelper.XCOORDS,latitude.toString());
                dataFiller.put(DatabaseHelper.YCOORDS,longitude.toString());
                dataFiller.put(DatabaseHelper.RADIUS,radius.toString());
                dataFiller.put(DatabaseHelper.LOCATION_NAME,location_name);

                Long id = DatabaseHelper.getInstance(this).addData(dataFiller);

                GeoFenceMain gm = new GeoFenceMain();
                gm.addGeoFence(getApplicationContext(),id.toString(),latitude,longitude,radius);
                break;
            case 2:
                dataFiller.put(DatabaseHelper.TIME, time);
                dataFiller.put(DatabaseHelper.DATE, date);

                rowID = DatabaseHelper.getInstance(this).addData(dataFiller);
                break;
        }




    }

    public void settingAlarm()
    {
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        // Retrieve a PendingIntent that will perform a broadcast
        alarmIntent = new Intent("android.intent.action.RUN");
        alarmIntent.putExtra("idNumber", rowID);
        pendingIntent = PendingIntent.getBroadcast(this, (int)rowID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, TimeFragment.timeAlarmMillis(), 0, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }



}
