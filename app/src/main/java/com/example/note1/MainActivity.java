package com.example.note1;

import static com.example.note1.CollectionWidget.updateNote1Widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private String dirNotes;
    private String[][] notes;
    private NoteCardAdapter adapter;
    private RecyclerView noteRecycler;
    /* notes[0] - text for cardView (150 char)
     * notes[1] - filepath
     * notes[2] - date for last change
     * notes[3] - flag for delete */
    private static final int PERMISSION_STORAGE = 101;
    private int column = 2;
    private boolean flag = true;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        dirNotes = myGetExternalStorageDir () + "/Note1";

        if (PermissionUtils.hasPermissions (MainActivity.this)) {
            searchTXT (dirNotes);
            textToRecycleView ();
        } else {
            PermissionUtils.requestPermissions (MainActivity.this, PERMISSION_STORAGE);
        }

        SearchView simpleSearchView = (SearchView) findViewById(R.id.search_view);
        simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            //заполнение отфильтрованными данными RecycleView при добавлении нового символа в поисковой строке
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals ("")) textToRecycleView ();
                    else {
                    adapter = new NoteCardAdapter (getFilter (newText));
                    noteRecycler.setAdapter (adapter);
                    StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager (column,
                            StaggeredGridLayoutManager.VERTICAL);
                    noteRecycler.setLayoutManager (staggeredGridLayoutManager);
                }
                return false;
            }
        });

        //переключение RecycleView с двух столбцов на один и обратно
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // меняем изображение на кнопке
                if (flag) {
                    imageButton.setImageResource (R.drawable.for_card_2column_24dp);
                    column = 1;
                    textToRecycleView ();
                } else {
                    // возвращаем первую картинку
                    imageButton.setImageResource(R.drawable.for_card_1column_24dp);
                    column = 2;
                    textToRecycleView ();
                }
                flag = !flag;
            }
        });

        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);

        FloatingActionButton floatingActionButton = findViewById (R.id.floatingActionButton);
        floatingActionButton.setOnClickListener (new View.OnClickListener () {
            public void onClick(View v) {
                Intent intent = new Intent (MainActivity.this, com.example.note1.EditNoteActivity.class);
                intent.putExtra (EditNoteActivity.EXTRA_NOTE_ID, "");
                startActivity(intent);
            }
        });
    }

    //создание отфильтрованного массива по поисковой строке
    protected String[][] getFilter (CharSequence constraint) {
        int[] tempItem = new int[notes[0].length];
        int item=0;
        if (constraint == null || constraint.length () == 0) return notes;
        else {
            String filterPattern = constraint.toString ().toLowerCase ().trim ();
            for (int i=0; i<notes[0].length; i++) {
                if (notes[0][i].toLowerCase ().contains (filterPattern)) {
                    tempItem[item] = i;
                    item++;
                }
            }
            String[][] filteredNotes = new String[4][item];
            for (int i=0; i<item; i++) {
                    filteredNotes[0][i] = notes[0][tempItem[i]];
                    filteredNotes[1][i] = notes[1][tempItem[i]];
                    filteredNotes[2][i] = notes[2][tempItem[i]];
                    filteredNotes[3][i] = notes[3][tempItem[i]];
            }
            return filteredNotes;
        }
    }

    // заполнение RecycleView данными из массива notes
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void textToRecycleView() {
        noteRecycler = findViewById (R.id.note_recycler);
        adapter = new NoteCardAdapter (notes);
        noteRecycler.setAdapter (adapter);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager (column,
                StaggeredGridLayoutManager.VERTICAL);
        noteRecycler.setLayoutManager (staggeredGridLayoutManager);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater ().inflate (R.menu.menu_main, menu);
        return super.onCreateOptionsMenu (menu);
    }

    //Метод вызывается при выборе действия на панели приложения.
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) { //Получение идентификатора действия.
            //удалить отмеченные файлы (из массива deleteFlag)
            case R.id.delete_note: {
                for (String deletePathNote : NoteCardAdapter.getDeleteFlag()) {
                    try {
                        File file = new File (deletePathNote);
                        if (file.exists ()) file.delete ();
                    } catch (Exception e) {
                        Toast.makeText (this, "1. " + e.toString (), Toast.LENGTH_LONG).show ();
                    }
                }
                searchTXT (dirNotes);
                textToRecycleView ();
                updateNote1Widget (this);
            }
            case R.id.setting: {
            //пока ничего не настраиваем
            }
            return true;
            default:
                return super.onOptionsItemSelected (item);
        }
    }

    //запрос разрешения на доступ к памяти у пользователя
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (PermissionUtils.hasPermissions(this)) {
                    searchTXT (dirNotes);
                    textToRecycleView ();
                    Toast.makeText(this, getResources().getString (R.string.permission_granted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getResources().getString (R.string.permission_not_granted), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                searchTXT (dirNotes);
                textToRecycleView ();
                Toast.makeText(this, getResources().getString (R.string.permission_granted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString (R.string.permission_not_granted), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //поиск файлов .txt в папке Note1 и заполнение массива notes данными из этих файлов
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void searchTXT(String dir) {
        try {
            File f = new File(dir);
            if (!f.isDirectory ()) f.mkdir ();
            FileFilter filter = new FileFilter() {

                public boolean accept(File f) {
                    return f.getName().endsWith(".txt");
                }
            };

            File[] files = f.listFiles(filter);
            assert files != null;
            //сортировка по последнему обновлению
            boolean flag = true;// устанавливаем наш флаг в true для первого прохода по массиву
            File temp1;// вспомогательная переменная
            while (flag) {
                flag = false;    // устанавливаем флаг в false в ожидании возможного свопа (замены местами)
                for (int j = 0; j < files.length - 1; j++) {
                    if (files[j].lastModified() < files[j+1].lastModified()) {
                        // меняем элементы местами
                        temp1 = files[j];
                        files[j] = files[j + 1];
                        files[j + 1] = temp1;
                        flag = true;  // true означает, что замена местами была проведена
                    }
                }
            }
            notes = new String[4][files.length];
            for (int i = 0; i < files.length; i++) {
                notes[0][i] = readFromFile(files[i]);
                notes[1][i] = files[i].toString ();

                //узнаем file date lastModified()
                long lastmodified = files[i].lastModified();
                Date date = new Date();
                date.setTime(lastmodified);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat postFormater = new SimpleDateFormat("Изменено:dd-MM-yyyy ' ' HH:mm:ss");
                String dateInt = postFormater.format(date);

                notes[2][i] = dateInt;
                notes[3][i] = "while";// deleteFlag
            }
        }
        catch (Exception e) {
            Toast.makeText(this, e.toString (), Toast.LENGTH_LONG).show();
        }
    }

    private String readFromFile(File filePath) {
        String textToCardView = "";

        try {
            InputStream inputStream = new FileInputStream (filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            int size = inputStream.available();
            if (size >= 200) size = 200;
            char[] buffer = new char[size];
            inputStreamReader.read (buffer);

            inputStream.close();
            textToCardView = new String(buffer);
        }catch (Exception e) {
            Toast.makeText(this, e.toString (), Toast.LENGTH_LONG).show();
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
        StorageManager myStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        StorageVolume mySV = myStorageManager.getPrimaryStorageVolume();
        return mySV.getDirectory().getPath();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getPrimaryStorageVolumeBeforeAndroid11() {
        String volumeRootPath = "";
        StorageManager myStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        StorageVolume mySV = myStorageManager.getPrimaryStorageVolume();
        Class<?> storageVolumeClazz;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            volumeRootPath = (String) getPath.invoke(mySV);
        } catch (Exception e) {
            Toast.makeText(this, e.toString (), Toast.LENGTH_LONG).show();
        }
        return volumeRootPath;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        if (PermissionUtils.hasPermissions(MainActivity.this)) {
            searchTXT (dirNotes);
            textToRecycleView ();
        }
        super.onStart ();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed ();
        finishAffinity ();//очистить стек
    }
}