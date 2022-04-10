package com.example.note1;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Date;

public class DataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private String dirNotes;
    private String[][] notes;
    private String filePath;
    int appWidgetId;

    public DataProvider(Context context, Intent intent) {

        mContext = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        initData();
    }

    @SuppressLint("NewApi")
    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return notes[0].length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews view = new RemoteViews(mContext.getPackageName(),
                R.layout.item);
        view.setTextViewText(R.id.item_Text, notes[0][position]);
        filePath = notes[1][position];

        // Нажатие на элемент из списка
        Intent clickIntent = new Intent();
        clickIntent.putExtra(EditNoteActivity.EXTRA_NOTE_PATH, filePath);
        view.setOnClickFillInIntent(R.id.item_Text, clickIntent);

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initData() {
        //проверка доступа к памяти
        if (PermissionUtils.hasPermissions (mContext)) {
            dirNotes = myGetExternalStorageDir () + "/Note1";
            searchTXT (dirNotes);
        } else {
            notes = new String[3][1];
            for (int i = 0; i < 1; i++) {
                notes[0][i] = mContext.getResources().getString (R.string.no_memory_access);
                notes[1][i] = " ";
                notes[2][i] = " ";
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void searchTXT(String dir) {
        try {
            File f = new File(dir);
            if (!f.isDirectory ()) f.mkdir ();
            FileFilter filter = new FileFilter() {

                public boolean accept(File f) {
                    return f.getName().endsWith("txt");
                }
            };

            File[] files = f.listFiles(filter);
            //сортировка по последнему обновлению
            boolean flag = true;// устанавливаем наш флаг в true для первого прохода по массиву
            File temp1;// вспомогательная переменная
            while (flag) {
                flag = false;    // устанавливаем флаг в false в ожидании возможного свопа (замены местами)
                for (int j = 0; j < (files != null ? files.length : 0) - 1; j++) {
                    if (files[j].lastModified() < files[j+1].lastModified()) {
                        // меняем элементы местами
                        temp1 = files[j];
                        files[j] = files[j + 1];
                        files[j + 1] = temp1;
                        flag = true;  // true означает, что замена местами была проведена
                    }
                }
            }
            notes = new String[3][files.length];
            for (int i = 0; i < files.length; i++) {
                notes[0][i] = readFromFile(files[i]);//содержание записки в CardView
                notes[1][i] = files[i].toString ();//путь к файлу .txt

                //узнаем file date lastModified()
                long lastmodified = files[i].lastModified();
                Date date = new Date();
                date.setTime(lastmodified);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat postFormater = new SimpleDateFormat("Изменено:dd-MM-yyyy ' ' HH:mm:ss");
                String dateInt = postFormater.format(date);

                notes[2][i] = dateInt;//дата последнего обновления файла .txt
            }
        }
        catch (Exception e){
            Toast.makeText (mContext, e.toString (), Toast.LENGTH_LONG).show();
        }
    }

    private String readFromFile(File filePath) {
        String textToCardView = "";

        try {
            InputStream inputStream = new FileInputStream (filePath.toString ());
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            int size = inputStream.available();
            if (size >= 200) size = 200;
            char[] buffer = new char[size];
            inputStreamReader.read(buffer);

            inputStream.close();
            textToCardView = new String(buffer);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return textToCardView;
    }

    //Узнаем volumeRootPath
    @SuppressLint("NewApi")
    public String myGetExternalStorageDir() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return getPrimaryStorageVolumeForAndroid11AndAbove();
        else
            return getPrimaryStorageVolumeBeforeAndroid11();
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getPrimaryStorageVolumeForAndroid11AndAbove() {
        StorageManager myStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume mySV = myStorageManager.getPrimaryStorageVolume();
        return mySV.getDirectory().getPath();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getPrimaryStorageVolumeBeforeAndroid11() {
        String volumeRootPath = "";
        StorageManager myStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume mySV = myStorageManager.getPrimaryStorageVolume();
        Class<?> storageVolumeClazz;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            volumeRootPath = (String) getPath.invoke(mySV);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return volumeRootPath;
    }
}
