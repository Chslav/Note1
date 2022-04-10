package com.example.note1;

import static com.example.note1.CollectionWidget.updateNote1Widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class EditNoteActivity extends AppCompatActivity {

    private ShareActionProvider shareActionProvider;
    public static final String EXTRA_NOTE_ID = "noteId";
    public static final String EXTRA_NOTE_PATH = "fileNotePath";
    private String undoText;
    private EditText editText;
    private String textNew;
    private String noteTextOld;
    private String fileNotePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar (toolbar);

        //заполнение EditText.
        fileNotePath = (String) getIntent().getExtras().get(EXTRA_NOTE_PATH);
        if (fileNotePath != null) {
            try {
                noteTextOld = readFromFile (fileNotePath);
            } catch (IOException e) {
                Toast.makeText (this, "1. " + e.toString (), Toast.LENGTH_LONG).show ();
            }
        }
        editText = findViewById(R.id.editText);
        editText.setText (noteTextOld);
    }

    private String readFromFile(String filePath)  throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = new FileInputStream (filePath);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                         stringBuilder.append (line);
                     }
                 }
        return stringBuilder.toString ();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        //Получить ссылку на провайдера действия передачи информации и присвоить
        // ее приватной переменной. Затем вызвать метод setShareActionIntent()
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        //Здесь задается текст по умолчанию, который должен передаваться
        //провайдером действия передачи информации.
        setShareActionIntent(editText.getText ().toString ());
        return super.onCreateOptionsMenu(menu);
    }

    // метод setShareActionIntent(), который создает интент и передает
    // его провайдеру действия передачи информации при помощи его метода setShareIntent().
    private void setShareActionIntent(String text) {
        Intent intent = new Intent (Intent.ACTION_SEND);
        intent.setType ("text/plain");
        intent.putExtra (Intent.EXTRA_TEXT, text);
        shareActionProvider.setShareIntent (intent);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())/*Получение идентификатора действия.*/ {
            case R.id.delete_note:
            {
                undoText = editText.getText ().toString ();
                editText.setText ("");
                try {
                    File file = new File (fileNotePath);
                    if (file.exists()) file.delete();
                    updateNote1Widget (this);
                }  catch (Exception e) {
                    Toast.makeText(this, e.toString (), Toast.LENGTH_LONG).show();
                }
                CharSequence text = getResources().getString (R.string.deleted);
                int duration = Snackbar.LENGTH_LONG;
                Snackbar snackbar = Snackbar.make (findViewById(R.id.constraint), text, duration);
                snackbar.setAction(getResources().getString (R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editText.setText (undoText);
                        if (!undoText.isEmpty ()) {
                            writeToFile (undoText);
                        }
                    }
                });
                snackbar.show();
                updateNote1Widget (this);
            }
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Узнаем volumeRootPath
    public String myGetExternalStorageDir() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return getPrimaryStorageVolumeForAndroid11AndAbove();
        else
            return getPrimaryStorageVolumeBeforeAndroid11();
    }
    @TargetApi(Build.VERSION_CODES.R)
    private String getPrimaryStorageVolumeForAndroid11AndAbove() {
        StorageManager myStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        StorageVolume mySV = myStorageManager.getPrimaryStorageVolume();
        return mySV.getDirectory().getPath();
    }
    private String getPrimaryStorageVolumeBeforeAndroid11() {
        String volumeRootPath = "";
        @SuppressLint({"NewApi", "LocalSuppress"}) StorageManager myStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        @SuppressLint({"NewApi", "LocalSuppress"}) StorageVolume mySV = myStorageManager.getPrimaryStorageVolume();
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

    // создание имени файла .txt и запись в него text заметки
    private void writeToFile (String text) {
        try {
            if (fileNotePath != null) {
                File file = new File (fileNotePath);
                if (file.exists ()) file.delete ();
            }
        }  catch (Exception e) {
            Toast.makeText(this, e.toString (), Toast.LENGTH_LONG).show();
        }
        String fileName;
        if (text.trim ().length () < 20) {
            fileName = text.trim ();
        } else {
            fileName = text.trim ().substring (0, 20) + "...";
        }
        try {
            File external = new File (myGetExternalStorageDir () + "/Note1");
            if (!external.isDirectory ()) external.mkdir ();
            File file = new File (external.getPath () + File.separator + fileName + ".txt");
            if (!file.isFile ()) file.createNewFile ();
            FileOutputStream fos = new FileOutputStream (file, false);
            byte[] buffer = text.getBytes (StandardCharsets.UTF_8);
            fos.write (buffer, 0, buffer.length);
            fos.close ();

            //showToast (getResources().getString (R.string.file_saved));
            updateNote1Widget (this);
        } catch (Exception e) {
            Toast.makeText (this, e.toString (), Toast.LENGTH_LONG).show ();
        }
    }

    public void showToast(String toast) {
        runOnUiThread(() -> Toast.makeText(this, toast, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStop() {
        super.onStop ();
        textNew = editText.getText ().toString ();
        if (!textNew.equals (noteTextOld) & !textNew.isEmpty ()) writeToFile (textNew);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed ();
        textNew = editText.getText ().toString ();
        if (!textNew.equals (noteTextOld) & !textNew.isEmpty ()) writeToFile (textNew);
    }
}