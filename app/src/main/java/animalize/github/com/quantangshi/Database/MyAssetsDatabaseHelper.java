package animalize.github.com.quantangshi.Database;

import android.content.Context;

public class MyAssetsDatabaseHelper extends com.readystatesoftware.sqliteasset.SQLiteAssetHelper {

    // 更新全唐诗数据库时，仅需递增此变量，就可以在首次运行APP时更新数据
    public static final int DATABASE_VERSION = 36;
    private static final String DATABASE_NAME = "tangshi.db";
    private static String mPath;

    private MyAssetsDatabaseHelper(Context context) {
        super(context,
                DATABASE_NAME,
                context.getFilesDir().getAbsolutePath(),
                null,
                DATABASE_VERSION);
        setForcedUpgrade();
    }

    public static String getDBPath(Context context) {
        if (mPath == null) {
            MyAssetsDatabaseHelper db = new MyAssetsDatabaseHelper(context.getApplicationContext());
            db.getReadableDatabase();
            db.close();

            mPath = context.getFilesDir().getAbsolutePath() +
                    "/" +
                    DATABASE_NAME;
        }
        return mPath;
    }

}