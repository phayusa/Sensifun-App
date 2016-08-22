package com.example.sokomo.sensifun;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by sokomo on 25/07/16.
 */
public class Settings extends AppCompatActivity {


    private int choicex;
    private int choicey;
    private int time;

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.common_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Typeface font2 = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/OpenSans-Regular.ttf");
        Typeface font1 = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/OpenSans-Bold.ttf");
        ((Button) findViewById(R.id.validate_button_settings)).setTypeface(font1);
        ((TextView) findViewById(R.id.Countdown_Settings)).setTypeface(font2);
        ((TextView) findViewById(R.id.Tile_Number_Settings)).setTypeface(font2);
        ((TextView) findViewById(R.id.Entry_Time)).setTypeface(font2);
        SharedPreferences sharedpref = getApplicationContext().getSharedPreferences("VALUES", Context.MODE_PRIVATE);
        choicex = sharedpref.getInt(getString(R.string.Key_x),-1);
        choicey = sharedpref.getInt(getString(R.string.Key_Y),-1);
        if(choicex!= -1 && choicey != -1){
            ((RadioButton) findViewById(getResources().getIdentifier("size" + Integer.toString(choicex * 10 + choicey), "id", getPackageName()))).setChecked(true);
        }
        time = sharedpref.getInt(getString(R.string.Key_Time_Set),20);
        ((TextView) findViewById(R.id.Entry_Time)).setText(Integer.toString(time));
    }

    public void on_click(View button_pressed){

        switch (button_pressed.getId()){
            case R.id.size22:
                choicex = 2;
                choicey = 2;
                ((RadioButton) button_pressed).setChecked(true);
                break;
            case R.id.size32:
                choicex = 3;
                choicey = 2;
                ((RadioButton) button_pressed).setChecked(true);
                break;
            case R.id.size33:
                choicex = 3;
                choicey = 3;
                ((RadioButton) button_pressed).setChecked(true);
                break;
            case R.id.size53:
                choicex = 5;
                choicey = 3;
                ((RadioButton) button_pressed).setChecked(true);
                break;
            case R.id.validate_button_settings:
                if(Integer.parseInt(((TextView) findViewById(R.id.Entry_Time)).getText().toString()) <= 20) {
                    Toast.makeText(getApplicationContext(),"Countdown must be superior to 20",Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences.Editor editor= getApplicationContext().getSharedPreferences("VALUES", Context.MODE_PRIVATE).edit();
                editor.putInt(getString(R.string.Key_x), choicex);
                editor.putInt(getString(R.string.Key_Y), choicey);
                editor.putInt(getString(R.string.Key_Time_Set),Integer.parseInt(((TextView) findViewById(R.id.Entry_Time)).getText().toString()));
                editor.commit();
                Toast.makeText(getApplicationContext(),"Settings Save",Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }
}
