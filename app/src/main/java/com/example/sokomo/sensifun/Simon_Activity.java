package com.example.sokomo.sensifun;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by sokomo on 11/08/16.
 */
//Class of the activity of Simon Game
public class Simon_Activity extends AppCompatActivity {

    private MenuItem tmp;
    private Simon_Game game_instance;
    private Client carpet_async_task;
    private boolean launched_client ;
    private static int Request_Score = 152;


    private class Client extends Communication_Carpet_Async {

        //constructor where nbx,nby is the max value possible
        Client(TextView writer_text,MenuItem icon,boolean connetion) {
            super(writer_text,icon,connetion);
        }

        public void Pressed_button(String[] frame) {
            if (Integer.parseInt(frame[1]) >= game_instance.get_bordery() || Integer.parseInt(frame[2]) >= game_instance.get_borderx() || game_instance.in_play_sequence())
                return;
            try {

                ImageButton button = (ImageButton) findViewById(getResources().getIdentifier("imageButton" + frame[2] + frame[1], "id", getPackageName()));
                if(frame[0].equals("0")){
                    game_instance.Change_Button_State(getApplicationContext(),Integer.parseInt(frame[2]), Integer.parseInt(frame[1]),false);
                }
                if (frame[0].equals("1") ) {
                    game_instance.Change_Button_State(getApplicationContext(),Integer.parseInt(frame[2]), Integer.parseInt(frame[1]), true);
                }
                if(game_instance.loosed()) {
                    onloose();
                    game_instance.reset();
                }
            } catch (Exception e) {
               System.err.println("Erreur sur " + frame[1] + " " + frame[2]);
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(values[0] == "Prems"){
                writer.setText("Press PLAY to begin the Game");
            }else {
                Pressed_button(values[0].split(" "));
            }
        }




    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_simon);
        ViewStub stub = (ViewStub) findViewById(R.id.stub_simon);
        stub.setLayoutResource(R.layout.simon_grid);
        View inflated = stub.inflate();
        Button_Simon_Information[][] buttons = new Button_Simon_Information[2][2];
        launched_client = false;
        int counter = 1;

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                if(counter == 3)
                    counter++;
                ImageButton button = (ImageButton) findViewById(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName()));
                button.setImageResource(getResources().getIdentifier("circle"+counter,"drawable",getPackageName()));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        on_click_button(view);
                    }
                });
                buttons[x][y] = new Button_Simon_Information(button,getApplicationContext(),getResources().getIdentifier("simon_sound"+x+y,"raw",getPackageName()),getResources().getIdentifier("inside_circle"+counter,"drawable",getPackageName()),R.color.Bottom_App,R.drawable.go_button, ((ImageButton) findViewById(R.id.Lauch_sequence)));
                counter++;
            }
        }
        TextView writer = ((TextView) findViewById(R.id.writer_simon));
        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/OpenSans-Bold.ttf");
        writer.setTypeface(font);
        game_instance = new Simon_Game(2,2,buttons,writer, ((ImageButton) findViewById(R.id.Lauch_sequence)),R.drawable.eye_button,R.drawable.go_button);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ((ImageButton) findViewById(R.id.Lauch_sequence)).setImageResource(R.drawable.play_button);
        game_instance.on_resume();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //to delete the shadow of the action bar
        getSupportActionBar().setElevation(0);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect_menu, menu);
        return true;
    }


    public void onloose(){
        Intent intent;
        intent = new Intent(this,Score_Activity.class);
        intent.putExtra(getString(R.string.GAME_KEY),"Simon");
        intent.putExtra(getString(R.string.MEMORY_TYPE),game_instance.get_Type());
        intent.putExtra(getString(getResources().getIdentifier("SCORE_"+game_instance.get_Type(),"string",getPackageName())),game_instance.get_level());
        this.startActivityForResult(intent,Request_Score);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean result;
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Request_Score && data != null){
            result = data.getBooleanExtra(getString(R.string.Play_Again),false);
            if(!result){
                this.finish();
            }
        }
    }

    public void on_click_button(View v){
        game_instance.on_click_simon_button(v);
        if(game_instance.loosed()){
            onloose();
            game_instance.reset();
            return;

        }
    }

    public void launch_first(View v){
        if(!game_instance.get_played_sequence()) {
            game_instance.launch_sequence();
        }else{
            Toast.makeText(getApplicationContext(),"You already press the button start",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(launched_client) {
            carpet_async_task.cancel(true);
            tmp.setIcon(R.drawable.wifi_off);
            launched_client = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        game_instance.on_pause();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.Connect_Button:
                if(!launched_client) {
                    carpet_async_task = new Client(((TextView) findViewById(R.id.writer_simon)),item,!isOnline());
                    carpet_async_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    tmp = item;
                }else{
                    carpet_async_task.cancel(true);
                    tmp.setIcon(R.drawable.wifi_off);
                }
                launched_client = !launched_client;
                System.err.println(launched_client);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}