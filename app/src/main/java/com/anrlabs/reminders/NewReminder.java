package com.anrlabs.reminders;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.anrlabs.locationreminder.GeoFenceMain;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;


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
    protected DatabaseHelper dataCarrier;
    protected ContentValues dataFiller;
    protected EditText titleCarrier, memoCarrier;
    private int tabSelected=0;
    private PendingIntent pendingIntent;
    private AlarmManager manager;
    private Intent alarmIntent;
    Context ctx = this;


    /////////////////////////////////// added by michael //////////////////////////////////////////
    private long rowID;
    int year,day,month;
    //SQLiteCursor cursor;
    Cursor cursor;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_new);

        titleCarrier = (EditText) findViewById(R.id.titleBox);
        memoCarrier = (EditText) findViewById(R.id.memoBox);



//        fillDisplay(rowID);
    }

    public void fillDisplay(long id)
    {
        if(id >0) {
            //calling specific row
            Cursor constantsCursor = DatabaseHelper.getInstance(ctx).editReminders(id);

            titleCarrier.setText(constantsCursor.getString(constantsCursor.getColumnIndex(DatabaseHelper.TITLE)));
            memoCarrier.setText(constantsCursor.getString(constantsCursor.getColumnIndex(DatabaseHelper.MESSAGE)));

            constantsCursor.close();
        }
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
        savingData();
        settingAlarm();

        super.onBackPressed();
    }

    //populating DataBase
    public void savingData()
    {
        titleCarrier = (EditText) findViewById(R.id.titleBox);
        memoCarrier = (EditText) findViewById(R.id.memoBox);

        dataFiller = new ContentValues();
        dataFiller.put(DatabaseHelper.TITLE, titleCarrier.getText().toString());
        dataFiller.put(DatabaseHelper.MESSAGE, memoCarrier.getText().toString());
        //dataFiller.put(DatabaseHelper.TIME, TimeFragment.passTime());
        //dataFiller.put(DatabaseHelper.DATE, TimeFragment.passDate());
        dataFiller.put(DatabaseHelper.XCOORDS,latitude.toString());
        dataFiller.put(DatabaseHelper.YCOORDS,longitude.toString());
        dataFiller.put(DatabaseHelper.RADIUS,radius.toString());
        dataFiller.put(DatabaseHelper.RADIUS,radius.toString());
        dataFiller.put(DatabaseHelper.LOCATION_NAME,location_name);

        switch (tabSelected)
        {
            case 1:
                dataFiller.put(DatabaseHelper.XCOORDS,latitude.toString());
                dataFiller.put(DatabaseHelper.YCOORDS,longitude.toString());
                dataFiller.put(DatabaseHelper.RADIUS,radius.toString());
                dataFiller.put(DatabaseHelper.RADIUS,radius.toString());
                dataFiller.put(DatabaseHelper.LOCATION_NAME,location_name);

        GeoFenceMain gm = new GeoFenceMain();
        gm.addGeoFence(getApplicationContext(),id.toString(),latitude,longitude,1000);
                Long id = DatabaseHelper.getInstance(this).addData(dataFiller);

                GeoFenceMain gm = new GeoFenceMain();
                gm.addGeoFence(this,id.toString(),latitude,longitude,1000);
                break;
            case 2:
                dataFiller.put(DatabaseHelper.TIME, TimeFragment.passTime());
                dataFiller.put(DatabaseHelper.DATE, TimeFragment.passDate());

                rowID = DatabaseHelper.getInstance(this).addData(dataFiller);
                break;
        }




    }

    public void settingAlarm()
    {
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        // Retrieve a PendingIntent that will perform a broadcast
        alarmIntent = new Intent(this, AlarmHandler.class);
        alarmIntent.putExtra("idNumber", rowID);
        pendingIntent = PendingIntent.getBroadcast(this, (int)rowID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, TimeFragment.timeAlarmMillis(), 0, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }


    ////////////////////////////// Code by Michael ///////////////////////////////////
    /*public void populateReminder(){

        //Toast.makeText(this, "Editing mode!", Toast.LENGTH_LONG).show();

        long dataBaseID = getIntent().getLongExtra("idNumber", rowID);
        cursor = DatabaseHelper.getInstance(this).loadRemiderDetails(16);
        //getRowData(dataBaseID);

        EditText editMemo = (EditText)findViewById(R.id.memoBox);
        editMemo.setText(cursor.getString(2));

        EditText editTitle = (EditText) findViewById(R.id.titleBox);
        editTitle.setText(cursor.getString(1));

        DatePicker editDate = (DatePicker)findViewById(R.id.dateRemember);
        //editDate.init(year, month, day, null);
        //editDate.updateDate(2014, 12, 10);


        TimePicker editTIme = (TimePicker)findViewById(R.id.timeRemeber);
        //editTIme.setCurrentHour(1);
          //editTIme.setCurrentMinute(25);

       }

       public void getRowData(long id){

           /*SQLiteDatabase db = dataCarrier.getReadableDatabase();



           //Integer.parseInt(cursor.getString(0));



         /* dataCarrier = new DatabaseHelper(this, DatabaseHelper.TABLE, null, 1);

           cursor = (SQLiteCursor) dataCarrier.getReadableDatabase().rawQuery("SELECT " + DatabaseHelper.ID + ", "
                   + DatabaseHelper.TITLE + ", " + DatabaseHelper.MESSAGE + ", "
                   + DatabaseHelper.DATE + ", " + DatabaseHelper.TIME + ", "
                   + DatabaseHelper.XCOORDS + ", " + DatabaseHelper.YCOORDS + ", " + DatabaseHelper.RADIUS +
                   " FROM " + DatabaseHelper.TABLE + " ORDER BY " + DatabaseHelper.DATE, null);
           if (cursor != null) {

               cursor.moveToFirst();
           }*/
       }
    ////////////////////////////////////////////////////////////////////////////////////////////

}
