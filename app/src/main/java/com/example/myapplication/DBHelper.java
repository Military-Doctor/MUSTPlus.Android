package com.example.myapplication;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.example.myapplication.models.ModelCourse;
import com.example.myapplication.models.ModelResponseLogin;
import com.example.myapplication.utils.APIs;
import com.example.myapplication.utils.Tools;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "test_db";//数据库名字
    private static final int DB_VERSION = 1;   // 数据库版本
    private static String TABLE_NAME_API_PERSISTENCE = "apis";// 表名
    private static String TABLE_NAME_API_CONFIG = "configs";// 表名
    private static String TABLE_NAME_API_COURSE = "courses";// 表名
    private static String TABLE_NAME_API_TEACHER = "teachers";// 表名
    private static String TABLE_NAME_API_STUDENT = "students";// 表名
    private final int API_PERSISTENCE_EXPIRES_DAY = 30; // API_PERSISTENCE记录 30天后 失效
    private final int COURSE_EXPIRES_DAY = 30; // COURSE记录 30天后 失效
    private final int TEACHER_EXPIRES_DAY = 30; // TEACHER记录 30天后 失效
    private final int STUDENT_EXPIRES_DAY = 7; // STUDENT记录 7天后 失效

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void removeRecord(APIs api) {
        updatePersistence(api, "", 0);
    }

    private long updatePersistence(APIs api, String value, long time) {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        values.put("id", api.i());
        values.put("api", api.v());
        values.put("value", value);
        values.put("time", time);

        long ret = db.insertWithOnConflict(TABLE_NAME_API_PERSISTENCE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return ret;
    }

    public long updatePersistence(APIs api, String value) {
        return updatePersistence(api, value, Tools.getUTCTimestamp());
    }

    // 从数据库中获取登录记录
    // 失败返回 null
    @Nullable
    public ModelResponseLogin getLoginRecord() {
        ModelResponseLogin model = JSON.parseObject(getAPIRecord(APIs.AUTH_LOGIN), ModelResponseLogin.class);
        return model;
    }

    // 从数据库中获取API记录
    // 失败返回 空字符串
    public String getAPIRecord(APIs api) {
        String result, time;
        String querySQL
                = "SELECT value, time" +
                " FROM " + TABLE_NAME_API_PERSISTENCE +
                " WHERE id='" + api.i() + "'" +
                " AND api='" + api.v() + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(querySQL, null);

        if (cursor.moveToFirst()) {
            result = cursor.getString(0);
            time = cursor.getString(1);
            cursor.close();
            db.close();
            if (Tools.getUTCTimestamp() - Integer.valueOf(time) > 60 * 60 * 24 * API_PERSISTENCE_EXPIRES_DAY)
                result = "";
            return result;
        } else {
            cursor.close();
            db.close();
            return "";
        }
    }


    // 设置 course 记录
    // 直接把后端返回的 json 放进来
    public void setCourseRecord(final String course_code, final String course_class, final String json_value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("course_code", course_code);
        values.put("course_class", course_class);
        values.put("value", json_value);
        values.put("time", Tools.getUTCTimestamp());
        db.insertWithOnConflict(TABLE_NAME_API_COURSE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // 读取 course 记录
    @Nullable
    public ModelCourse getCourseRecord(final String course_code, final String course_class) {
        ModelCourse course;
        String result, time;
        String querySQL
                = "SELECT value, time" +
                " FROM " + TABLE_NAME_API_COURSE +
                " WHERE course_code='" + course_code + "'" +
                " AND course_class='" + course_class + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(querySQL, null);

        if (cursor.getCount() > 1) { // 找到重复，删除重复
            String delete
                    = "DELETE FROM " + TABLE_NAME_API_COURSE +
                    " WHERE course_code='" + course_code + "'" +
                    " AND course_class='" + course_class + "'";
            db.rawQuery(delete, null).close();
            db.close();
            return null;
        } else if (cursor.getCount() == 1) { // 恰好找到，检测时间
            result = cursor.getString(0);
            time = cursor.getString(1);
            cursor.close();
            db.close();
            if (Tools.getUTCTimestamp() - Integer.valueOf(time) > 60 * 60 * 24 * COURSE_EXPIRES_DAY) {
                return null;
            }
            return JSON.parseObject(result, ModelCourse.class);
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql_apis = "CREATE TABLE " + TABLE_NAME_API_PERSISTENCE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "api TEXT," +
                "value TEXT," +
                "time INTEGER); ";

        String sql_config = "CREATE TABLE " + TABLE_NAME_API_CONFIG + " (" +
                "config TEXT PRIMARY KEY," +
                "value TEXT);";

        String sql_courses = "CREATE TABLE " + TABLE_NAME_API_COURSE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "course_code TEXT," +
                "course_class TEXT," +
                "value TEXT," +
                "time INTEGER);";

        String sql_teachers = "CREATE TABLE " + TABLE_NAME_API_TEACHER + " (" +
                "name_zh TEXT PRIMARY KEY," +
                "value TEXT," +
                "time INTEGER);";

        String sql_students = "CREATE TABLE " + TABLE_NAME_API_STUDENT + " (" +
                "student_id TEXT PRIMARY KEY," +
                "value TEXT," +
                "time INTEGER);";

        db.execSQL(sql_apis);
        db.execSQL(sql_config);
        db.execSQL(sql_courses);
        db.execSQL(sql_teachers);
        db.execSQL(sql_students);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
