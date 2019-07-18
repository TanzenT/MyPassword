package com.smileapps.mypassword.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.smileapps.mypassword.R;
import com.smileapps.mypassword.app.AES;
import com.smileapps.mypassword.app.MyApplication;
import com.smileapps.mypassword.model.Password;
import com.smileapps.mypassword.model.PasswordGroup;
import com.smileapps.mypassword.model.SettingKey;

/**
 * 데이터베이스
 */
public class PasswordDatabase extends SQLiteOpenHelper {
    private static final int version = 4;
    private Context context;

    /** AES암호화키 */
    private String currentPasswd;

    public PasswordDatabase(Context context) {
        super(context, "password", null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createPasswordTable(db);
        createGroupTable(db);
    }

    private void createPasswordTable(SQLiteDatabase db) {
        String sql = "create table password(id integer primary key autoincrement, create_date integer, title text, "
                + "user_name text, password text, is_top integer default 0, note text, group_name text default '"
                + getDefaultGroupName() + "')";
        db.execSQL(sql);
    }

    private String getDefaultGroupName() {
        return context.getString(R.string.password_group_default_name);
    }

    private void createGroupTable(SQLiteDatabase db) {
        String sql;
        sql = "create table password_group(name text primary key)";
        db.execSQL(sql);

        sql = "insert into password_group(name) values('" + getDefaultGroupName() + "')";
        db.execSQL(sql);
        getMyApplication().putSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME, getDefaultGroupName());
    }

    private MyApplication getMyApplication() {
        return (MyApplication) context.getApplicationContext();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String sql = "alter table password add is_top integer default 0";
            db.execSQL(sql);
        }

        if (oldVersion < 3) {
            String sql = "alter table password add group_name text default '" + getDefaultGroupName() + "'";
            db.execSQL(sql);
            createGroupTable(db);
        }

        if (oldVersion < 4) {
            upgradeFor4(db);
        }
    }

    private void upgradeFor4(SQLiteDatabase db) {
        // 데이터베이스에 있는 전체 암호 불러오기
        List<Password> passwords = new ArrayList<Password>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select id, password from password", null);
            while (cursor.moveToNext()) {
                Password password = new Password();
                password.setId(cursor.getInt(cursor.getColumnIndex("id")));
                password.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                passwords.add(password);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        // 암호화 후 데이터 저장
        for (Password password : passwords) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("password", encrypt(password.getPassword()));
            db.update("password", contentValues, "id = ?", new String[]{password.getId() + ""});
        }
    }

    public long insertPassword(Password password) {
        long id = -1;
        try {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("create_date", password.getCreateDate());
            contentValues.put("title", password.getTitle());
            contentValues.put("user_name", password.getUserName());
            contentValues.put("password", encrypt(password.getPassword()));
            contentValues.put("note", password.getNote());
            contentValues.put("is_top", password.isTop() ? 1 : 0);
            contentValues.put("group_name", password.getGroupName());
            id = sqLiteDatabase.insert("password", null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public int updatePassword(Password password) {
        int result = 0;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            if (password.getCreateDate() != 0)
                contentValues.put("create_date", password.getCreateDate());
            if (password.getTitle() != null)
                contentValues.put("title", password.getTitle());
            if (password.getUserName() != null)
                contentValues.put("user_name", password.getUserName());
            if (password.getPassword() != null)
                contentValues.put("password", encrypt(password.getPassword()));
            if (password.getNote() != null)
                contentValues.put("note", password.getNote());
            contentValues.put("is_top", password.isTop() ? 1 : 0);

            if (password.getGroupName() != null)
                contentValues.put("group_name", password.getGroupName());

            result = sqLiteDatabase.update("password", contentValues, "id = ?",
                    new String[]{String.valueOf(password.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public Password getPassword(int id) {
        Password password = null;

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query("password", null, "id = ?", new String[]{String.valueOf(id)}, null, null,
                    null);

            if (cursor.moveToNext()) {
                password = mapPassword(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return password;
    }

    private Password mapPassword(Cursor cursor) {
        Password password = new Password();
        password.setId(cursor.getInt(cursor.getColumnIndex("id")));
        password.setCreateDate(cursor.getLong(cursor.getColumnIndex("create_date")));
        password.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        password.setUserName(cursor.getString(cursor.getColumnIndex("user_name")));
        password.setPassword(decrypt(cursor.getString(cursor.getColumnIndex("password"))));
        password.setNote(cursor.getString(cursor.getColumnIndex("note")));
        password.setTop(cursor.getInt(cursor.getColumnIndex("is_top")) == 1 ? true : false);
        password.setGroupName(cursor.getString(cursor.getColumnIndex("group_name")));
        return password;
    }

    public List<Password> getAllPassword() {
        List<Password> passwords = new ArrayList<Password>();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query("password", null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                Password password = null;
                password = mapPassword(cursor);
                passwords.add(password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return passwords;
    }

    public int deletePasssword(int id) {
        int result = -1;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        result = sqLiteDatabase.delete("password", "id = ?", new String[]{String.valueOf(id)});
        return result;
    }

    public List<Password> getAllPasswordByGroupName(String groupName) {
        List<Password> passwords = new ArrayList<Password>();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query("password", null, "group_name = ?", new String[]{groupName}, null, null,
                    null);
            while (cursor.moveToNext()) {
                Password password = null;
                password = mapPassword(cursor);
                passwords.add(password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return passwords;
    }

    public void addPasswordGroup(PasswordGroup passwordGroup) {
        try {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", passwordGroup.getGroupName());
            sqLiteDatabase.insert("password_group", null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<PasswordGroup> getAllPasswordGroup() {
        List<PasswordGroup> passwordGroups = new ArrayList<PasswordGroup>();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.query("password_group", null, null, null, null, null, null);

            while (cursor.moveToNext()) {
                PasswordGroup passwordGroup = new PasswordGroup();
                passwordGroup.setGroupName(cursor.getString(cursor.getColumnIndex("name")));
                passwordGroups.add(passwordGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return passwordGroups;
    }

    public void updatePasswdGroupName(String oldGroupName, String newGroupName) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor rawQuery = null;
        try {
            rawQuery = sqLiteDatabase.rawQuery("select count(name) from password_group where name = ?",
                    new String[]{newGroupName});
            if (rawQuery != null && rawQuery.moveToNext() && rawQuery.getInt(0) == 1) {
                sqLiteDatabase.delete("password_group", "name = ?", new String[]{oldGroupName});
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", newGroupName);
                sqLiteDatabase.update("password_group", contentValues, "name = ?", new String[]{oldGroupName});
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put("group_name", newGroupName);
            sqLiteDatabase.update("password", contentValues, "group_name = ?", new String[]{oldGroupName});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rawQuery != null)
                rawQuery.close();
        }
    }

    public int deletePasswordGroup(String passwordGroupName) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        int count;
        count = sqLiteDatabase.delete("password_group", "name = ?", new String[]{passwordGroupName});
        if (count > 0) {
            sqLiteDatabase.delete("password", "group_name = ?", new String[]{passwordGroupName});
        }
        return count;
    }

    public String getCurrentPasswd() {
        return currentPasswd;
    }

    public void setCurrentPasswd(String currentPasswd) {
        this.currentPasswd = currentPasswd;
    }

    /** 암호화 */
    public String encrypt(String key) {
        String result = "";
        try {
            result = AES.encrypt(key, currentPasswd);
        } catch (Exception e) {
            e.printStackTrace();
            result = key;
        }
        return result;
    }

    /** 복호화 */
    public String decrypt(String data) {
        return decrypt(data, currentPasswd);
    }

    public String decrypt(String data, String passwd) {
        String result = "";
        try {
            result = AES.decrypt(data, passwd);
        } catch (Exception e) {
            e.printStackTrace();
            result = data;
        }
        return result;
    }

    public void encodePasswd(String newPasswd) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        List<Password> passwords = new ArrayList<Password>();

        // 모든 계정 데이터베이스 로드
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery("select id, password from password", null);
            while (cursor.moveToNext()) {
                Password password = new Password();
                password.setId(cursor.getInt(cursor.getColumnIndex("id")));
                password.setPassword(decrypt(cursor.getString(cursor.getColumnIndex("password"))));
                passwords.add(password);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }

        currentPasswd = newPasswd;
        for (Password password : passwords) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("password", encrypt(password.getPassword()));
            sqLiteDatabase.update("password", contentValues, "id = ?", new String[]{password.getId() + ""});
        }
    }
}
