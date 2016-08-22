package com.example.sokomo.sensifun;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by sokomo on 17/08/16.
 */
public class Score_Activity extends AppCompatActivity {
    private static int Request_Score = 152;
    public String type;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setElevation(0);
        return true;
    }

    public void the_result(boolean play_again){
        Intent intent = new Intent();
        intent.putExtra(getString(R.string.Play_Again),play_again);
        if(type == "Simon")
            setResult(Request_Score,intent);
        else
            setResult(getResources().getInteger(R.integer.Request_Score_Reaction),intent);
        finish();

    }

    public void Activity_Score_On_Click(View v){
        switch (v.getId()){
            case R.id.button_play_again:
                the_result(true);
                break;
            case R.id.button_back_menu:
                the_result(false);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent t = getIntent();
        int level,record,time,score;
        SharedPreferences sharedPref;
        ViewStub stub;
        View inflated;
        String game;
        setContentView(R.layout.layout_simon);
        TextView writer = ((TextView) findViewById(R.id.writer_simon));
        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/OpenSans-Bold.ttf");
        writer.setTypeface(font);
        Typeface font2 = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/OpenSans-Regular.ttf");

        game = t.getStringExtra(getString(R.string.GAME_KEY));
        stub = (ViewStub) findViewById(R.id.stub_simon);
        switch (game){
            case "Simon":
                type = t.getStringExtra(getString(R.string.MEMORY_TYPE));
                if(type == "")
                    finish();
                level = t.getIntExtra(getString(getResources().getIdentifier("SCORE_"+type,"string",getPackageName())),-1);
                if(level == -1)
                    finish();
                sharedPref = getApplicationContext().getSharedPreferences("SIMON_SCORE", Context.MODE_PRIVATE);
                record = sharedPref.getInt(getString(getResources().getIdentifier("HIGH_SCORE_"+type,"string",getPackageName())), 0);
                stub.setLayoutResource(R.layout.score_bottom);
                inflated = stub.inflate();
                ((TextView) findViewById(R.id.Record_item)).setTypeface(font2);
                ((TextView) findViewById(R.id.Score_item)).setTypeface(font2);
                ((TextView) findViewById(R.id.Record_item)).setText(Integer.toString(record));
                ((TextView) findViewById(R.id.Score_item)).setText(Integer.toString(level));
                if(record < level){
                    getApplicationContext().getSharedPreferences("SIMON_SCORE", Context.MODE_PRIVATE).edit().putInt(getString(getResources().getIdentifier("HIGH_SCORE_"+type,"string",getPackageName())),level).commit();
                }
                writer.setText(getString(R.string.GAME_OVER));
                break;
            case "Reaction":
                time = t.getIntExtra(getString(R.string.KEY_TIME),-1);
                if(time == -1)
                    finish();
                score = t.getIntExtra(getString(R.string.SCORE_KEY),-1);
                if(score == -1)
                    finish();
                sharedPref = getApplicationContext().getSharedPreferences("REACTION_SCORE", Context.MODE_PRIVATE);
                record = sharedPref.getInt(getString(R.string.RECORD_KEY), 0);
                stub.setLayoutResource(R.layout.score_reaction);
                inflated = stub.inflate();
                ((TextView) findViewById(R.id.Record_item)).setTypeface(font2);
                ((TextView) findViewById(R.id.Score_item)).setTypeface(font2);
                ((TextView) findViewById(R.id.Time_item)).setTypeface(font2);

                ((TextView) findViewById(R.id.Time_item)).setText(Integer.toString(time));
                ((TextView) findViewById(R.id.Record_item)).setText(Integer.toString(record));
                ((TextView) findViewById(R.id.Score_item)).setText(Integer.toString(score));
                if(record < score){
                    getApplicationContext().getSharedPreferences("REACTION_SCORE", Context.MODE_PRIVATE).edit().putInt(getString(R.string.RECORD_KEY),score).commit();
                }
                writer.setText(getString(R.string.GAME_OVER));
                break;
            default:
                finish();
        }
        ((Button) findViewById(R.id.button_play_again)).setTypeface(font2);
        ((Button) findViewById(R.id.button_back_menu)).setTypeface(font2);
    }

}
