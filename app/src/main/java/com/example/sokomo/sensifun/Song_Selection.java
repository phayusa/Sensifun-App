package com.example.sokomo.sensifun;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.SearchView;
import android.widget.SearchView;
import android.text.TextUtils;
import android.view.*;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by sokomo on 28/07/16.
 */

import android.app.ListActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Song_Selection extends AppCompatActivity implements SearchView.OnQueryTextListener {



    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int Request_Song = 10;
    private HashMap<String,String> Path_Song;
    private int x;
    private int y;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    public List<String> get_sound_information(){
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
            finish();
        }

        List<String> Sound_title = new ArrayList<>();
        Cursor cursor;
        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE ,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        cursor = getApplicationContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        //List<String> songs = new ArrayList<String>();
        while(cursor.moveToNext()) {
          /*  songs.add(cursor.getString(0) + "||"
                    + cursor.getString(1) + "||"
                    + cursor.getString(2) + "||"
                    + cursor.getString(3) + "||"
                    + cursor.getString(4) + "||"
                    + cursor.getString(5));
            //Sound_title += cursor.getString(4) + "-|-";*/
            Path_Song.put(cursor.getString(4),cursor.getString(3));
            Sound_title.add(cursor.getString(4));
        }
        return Sound_title;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.song_select, menu);
        return super.onCreateOptionsMenu(menu);
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getMenuInflater().inflate(R.menu.song_select, menu);



        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.Search_Item).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText.toString());
        /*if (TextUtils.isEmpty(newText)) {
            listView.clearTextFilter();
        }
        else {
            listView.setFilterText(newText.toString());
        }*/
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 340:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    get_sound_information();
                } else {
                    // Permission Denied
                    Toast.makeText(getApplicationContext(), "You need to allow access to External Storage to access to your songs", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void insertDummyContactWrapper() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessageOKCancel("You need to allow access to External Storage to access to your songs",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                        340);
                            }
                        });
                return;
            }
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    340);
            return;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.list_view);
        Intent t = getIntent();
        x = t.getIntExtra("X1",-1);
        y = t.getIntExtra("Y1",-1);
        Path_Song = new HashMap<>();
        //get_sound_information();
        listView = (ListView) findViewById(R.id.List_Song);
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    340);
            return;
        }
        get_sound_information();
        adapter = new ArrayAdapter<String>(this,
                R.layout.listview_content,android.R.id.text1,get_sound_information());
        if(listView == null){
            this.finish();
        }
        listView.setAdapter(adapter);
        listView.setBackgroundColor(getColor(R.color.Bottom_App));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) adapterView.getAdapter().getItem(i);
                //Toast.makeText(this, item + " selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("DISPLAY_NAME",item);
                intent.putExtra("X",x);
                intent.putExtra("Y",y);
                intent.putExtra("PATH",Path_Song.get(item));
                setResult(Request_Song,intent);
                finish();
            }
        });
        listView.setTextFilterEnabled(true);
    }

}