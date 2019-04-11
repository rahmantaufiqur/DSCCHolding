package barikoi.dscc.dsccholdingtax;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.common.BlockingServiceConnection;

import java.util.ArrayList;

/**
 * This adapter is used to add a place in db and any kind of query regarding place db
 * This checks if place is saved or not
 * The place is own or not
 * THe db is open or not
 * Created by Sakib on 06-Feb-17.
 */

public class PlaceDbAdapter {

    private static final String TABLE_NAME="Place";
    private static final String KEY_HOLDING_NO="holdingNo";
    private static final String KEY_ROAD="road";
    private static final String KEY_BLOCK="block";
    private static final String KEY_SECTOR="sector";
    private static final String KEY_SECTION="section";
    private static final String KEY_SUBAREA="subarea";
    private static final String KEY_AREA="area";
    private static final String KEY_POSTAL="postCode";
    private static final String KEY_WARD="ward";
    private static final String KEY_ZONE="zone";
    private static final String KEY_HOLDING_DESC="holdingDesc";
    private static final String KEY_LON="lon";
    private static final String KEY_LAT="lat";

    public static final String VALUE_TYPE_PUBLIC="public";
    private static PlaceDbAdapter instance;
    private static SQLiteDatabase db;
    private static MySQLiteHelper dbHelper;

    public PlaceDbAdapter(Context context){

        dbHelper=MySQLiteHelper.getInstance(context.getApplicationContext());
    }

    public static synchronized PlaceDbAdapter getInstance(Context context){
        if(instance==null){
            instance=new PlaceDbAdapter(context.getApplicationContext());
        }
        return instance;
    }

    public synchronized boolean isOpen(){
        return db.isOpen();
    }
    public synchronized void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public synchronized void close() {
        dbHelper.close();
    }

    public synchronized Boolean addPlace(String holdingNo, String holdingDesc, String lon, String lat, String road, String block, String sector, String section, String subarea, String area, String postalcode, String ward, String zone){

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_HOLDING_NO, holdingNo);
        values.put(KEY_LON, lon);
        values.put(KEY_LAT, lat);
        values.put(KEY_AREA,area);
        values.put(KEY_HOLDING_DESC,holdingDesc);
        values.put(KEY_POSTAL,postalcode);
        values.put(KEY_ROAD,road);
        values.put(KEY_BLOCK,block);
        values.put(KEY_SECTOR, sector);
        values.put(KEY_SECTION,section);
        values.put(KEY_SUBAREA,subarea);
        values.put(KEY_WARD,ward);
        values.put(KEY_ZONE,zone);

        // 3. insert
        if(db.insert(TABLE_NAME, null, values)!=-1){
            return true;
        } // key/value -> keys = column names/ values = column values
        else{
            return false;
        }
    }
    public synchronized void addPlace(Place p, String type){
        try {
            // 2. create ContentValues to add key "column"/value
            ContentValues values = new ContentValues();
            values.put(KEY_HOLDING_NO, p.getHoldingNo());
            values.put(KEY_LON, p.getLon());
            values.put(KEY_LAT, p.getLat());
            values.put(KEY_AREA,p.getArea());
            values.put(KEY_HOLDING_DESC,p.getHoldingDesc());
            values.put(KEY_POSTAL,p.getPostalcode());
            values.put(KEY_ROAD,p.getRoad());
            values.put(KEY_BLOCK,p.getBlock());
            values.put(KEY_SECTOR, p.getSector());
            values.put(KEY_SECTION,p.getSection());
            values.put(KEY_SUBAREA,p.getSubarea());
            values.put(KEY_WARD,p.getWard());
            values.put(KEY_ZONE,p.getZone());


            // 3. insert
            // key/value -> keys = column names/ values = column values
            db.insert(TABLE_NAME, null, values);
            // 4. close
        }catch (Exception e){
            //Crashlytics.log(e.getMessage());
        }

    }

    public synchronized void addPlaces(ArrayList<Place> places, String type){
        String holdingNo, holdingDesc,lon,lat,road,block,sector,section,subarea,area,postalcode,ward,zone;
        Place inputplace;
        int i = 0;
        try {
            for (; i < places.size(); i++) {
                inputplace = places.get(i);
                holdingNo = inputplace.getHoldingNo();
                lon = inputplace.getLon();
                lat = inputplace.getLat();
                holdingDesc = inputplace.getHoldingDesc();
                area = inputplace.getArea();
                subarea = inputplace.getSubarea();
                road = inputplace.getRoad();
                postalcode = inputplace.getPostalcode();
                ward = inputplace.getWard();
                zone = inputplace.getZone();
                block=inputplace.getBlock();
                sector=inputplace.getSector();
                section=inputplace.getSection();
                addPlace(holdingNo,holdingDesc,lon,lat,road,block,sector,section,subarea,area,postalcode,ward, zone);
            }
        }catch (Exception e){
            //Crashlytics.log(e.getMessage());
        }
    }
    public synchronized ArrayList<Place> getSavedPlaces(){
        String[] projection={
                KEY_HOLDING_NO,
                KEY_LON,
                KEY_LAT,
                KEY_AREA,
                KEY_HOLDING_DESC,
                KEY_ROAD,
                KEY_POSTAL,
                KEY_BLOCK,
                KEY_SECTOR,
                KEY_SECTION,
                KEY_SUBAREA,
                KEY_WARD,
                KEY_ZONE
        };
        Place newplace;
        ArrayList<Place> places = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, projection, null, null, null, null, null, null);
        //Cursor cursor=db.rawQuery(selectquery,null);
        if (cursor.moveToFirst()) {
            do {
                newplace=new Place(cursor.getString(cursor.getColumnIndex(KEY_HOLDING_NO)),cursor.getString(cursor.getColumnIndex(KEY_LON)),cursor.getString(cursor.getColumnIndex(KEY_LAT)),cursor.getString(cursor.getColumnIndex(KEY_AREA)),cursor.getString(cursor.getColumnIndex(KEY_HOLDING_DESC)),cursor.getString(cursor.getColumnIndex(KEY_ROAD)),cursor.getString(cursor.getColumnIndex(KEY_POSTAL)),cursor.getString(cursor.getColumnIndex(KEY_BLOCK)),cursor.getString(cursor.getColumnIndex(KEY_SECTOR)),cursor.getString(cursor.getColumnIndex(KEY_SECTION)),cursor.getString(cursor.getColumnIndex(KEY_SUBAREA)),cursor.getString(cursor.getColumnIndex(KEY_WARD)),cursor.getString(cursor.getColumnIndex(KEY_ZONE)));
                places.add(newplace);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return places;

    }
    public void deletePlace(String holdingNo) {

        db.delete(TABLE_NAME, KEY_HOLDING_NO + " = ?",
                new String[] { holdingNo });

    }
    /*
    public synchronized Place getPlace(String code){
        String[] projection={
                KEY_HOLDING_NO,
                KEY_LON,
                KEY_LAT,
                KEY_AREA,
                KEY_HOLDING_DESC,
                KEY_ROAD,
                KEY_POSTAL,
                KEY_BLOCK,
                KEY_SECTOR,
                KEY_SECTION,
                KEY_SUBAREA,
                KEY_WARD,
                KEY_ZONE
        };
        Place newplace;
        //String selectquery="SELECT * FROM "+TABLE_NAME+" WHERE "+KEY_TYPE+ " = "+;
        Cursor cursor = db.query(TABLE_NAME, projection, KEY_CODE + "=?",
                new String[] { code }, null, null, null, null);
        //Cursor cursor=db.rawQuery(selectquery,null);
        if (cursor != null)
            cursor.moveToFirst();
        newplace=new Place(cursor.getString(cursor.getColumnIndex(KEY_HOLDING_NO)),cursor.getString(cursor.getColumnIndex(KEY_LON)),cursor.getString(cursor.getColumnIndex(KEY_LAT)),cursor.getString(cursor.getColumnIndex(KEY_AREA)),cursor.getString(cursor.getColumnIndex(KEY_HOLDING_DESC)),cursor.getString(cursor.getColumnIndex(KEY_ROAD)),cursor.getString(cursor.getColumnIndex(KEY_POSTAL)),cursor.getString(cursor.getColumnIndex(KEY_BLOCK)),cursor.getString(cursor.getColumnIndex(KEY_SECTOR)),cursor.getString(cursor.getColumnIndex(KEY_SECTION)),cursor.getString(cursor.getColumnIndex(KEY_SUBAREA)),cursor.getString(cursor.getColumnIndex(KEY_WARD)),cursor.getString(cursor.getColumnIndex(KEY_ZONE)));cursor.close();
        return newplace;

    }
    public synchronized void updatePlace(String address, String lat, String lon, String code, String area, String city, String postal){
        ContentValues values = new ContentValues();
        values.put(KEY_ADDRESS, address);
        values.put(KEY_LON, lat);
        values.put(KEY_LAT, lon);
        values.put(KEY_AREA,area);
        values.put(KEY_CITY,city);
        values.put(KEY_POSTAL,postal);

        db.update(TABLE_NAME, values, KEY_CODE + " = ?",
                new String[]{code});
    }

    public void deleteAllPlaces(){
        dbHelper.onUpgrade(db,1,1);
    }

    public void deletePlace(String code) {

        db.delete(TABLE_NAME, KEY_CODE + " = ?",
                new String[] { code });

    }*/

}
