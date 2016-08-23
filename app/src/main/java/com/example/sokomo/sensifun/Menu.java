package com.example.sokomo.sensifun;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by sokomo on 25/07/16.
 */
public class Menu extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        TextView writer = ((TextView) findViewById(R.id.menu_text));
        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/OpenSans-Bold.ttf");
        writer.setTypeface(font);
        /*SpannableString s = new SpannableString("Sensifun");
        s.setSpan(new TypefaceSpan("Dosis-Medium.otf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/
        //getSupportActionBar().setTitle(s);
        ((Button) findViewById(R.id.New_game_button)).setTypeface(font);
        ((Button) findViewById(R.id.Simon_Button)).setTypeface(font);
        ((Button) findViewById(R.id.Settings_button)).setTypeface(font);
        ((Button) findViewById(R.id.Exit_button)).setTypeface(font);

    }

    //On click method for the menu button
    public void Test_Button(View button_pressed){
        Intent lauched_activity;
        switch (button_pressed.getId()){
            case R.id.New_game_button :
                lauched_activity = new Intent(this,Reaction_Activity.class);
                this.startActivity(lauched_activity);
                break;
            /*case R.id.Song_launch:
                lauched_activity = new Intent(this,Carpet_Song.class);
                this.startActivity(lauched_activity);
                break;*/
            case R.id.Simon_Button:
                lauched_activity = new Intent(this,Simon_Activity.class);
                this.startActivity(lauched_activity);
                break;
            case R.id.Settings_button:
                lauched_activity = new Intent(this,Settings.class);
                this.startActivity(lauched_activity);
                break;
            case R.id.Exit_button:
                this.finish();
        }
    }
}
