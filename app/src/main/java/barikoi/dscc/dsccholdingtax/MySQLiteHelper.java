package barikoi.dscc.dsccholdingtax;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by Sakib on 19-Jan-17.
 * This class is to create a table in the sqlite database
 */

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    // Database Name
    private static final String DATABASE_NAME = "DSCCPlaceDB";
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

    private static MySQLiteHelper instance;

    public MySQLiteHelper(Context context) {
        super(context.getApplicationContext(),  DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static MySQLiteHelper getInstance(Context context){
        if(instance==null){
            instance=new MySQLiteHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_OWN_PLACE_TABLE = "CREATE TABLE "+TABLE_NAME+"(" +
                KEY_HOLDING_NO+" TEXT, "+
                KEY_LON+" TEXT, "+
                KEY_LAT+" TEXT, "+
                KEY_AREA+" TEXT, "+
                KEY_HOLDING_DESC+" TEXT, "+
                KEY_POSTAL+" TEXT, "+
                KEY_ROAD+" TEXT, "+
                KEY_BLOCK+" TEXT, "+
                KEY_SECTOR+" TEXT, "+
                KEY_SECTION+" TEXT,"+ KEY_SUBAREA+" TEXT, "+KEY_WARD+" TEXT, "+KEY_ZONE+" TEXT);";

        // create books table
        db.execSQL(CREATE_OWN_PLACE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);

        Log.d("sqlite","db upgraded to" +newVersion);
        // create fresh books table
        this.onCreate(db);

    }


}
