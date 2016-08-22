package com.example.sokomo.sensifun;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by sokomo on 12/08/16.
 */
public class Reaction_Activity extends AppCompatActivity {


    private int size_x,size_y;
    private Reaction_Game game_instance;
    private MenuItem tmp_icon;
    private Client connect_carpet;
    private boolean launched_client;
    private TextView writer;
    private long max_time;

    private ProgressBar progressBar;
    private Handler handler = new Handler();
    private Timer_Bar bat_manager;

    private class Client extends Communication_Carpet_Async {

        Client(TextView status,MenuItem icon,boolean online){
            super(status,icon,online);
        }

        public void Pressed_button(String[] frame) {
            if (Integer.parseInt(frame[1]) >= game_instance.get_bordery() || Integer.parseInt(frame[2]) >= game_instance.get_borderx())
                return;
            try {

                ImageButton button = (ImageButton) findViewById(getResources().getIdentifier("imageButton" + frame[2] + frame[1], "id", getPackageName()));
                if(frame[0].equals("0")){
                    game_instance.Change_Button_State(getApplicationContext(),Integer.parseInt(frame[2]), Integer.parseInt(frame[1]),false);
                }
                if (frame[0].equals("1") ) {
                    game_instance.Change_Button_State(getApplicationContext(),Integer.parseInt(frame[2]), Integer.parseInt(frame[1]), true);
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
                game_instance.activate_random();
                bat_manager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }else {
                Pressed_button(values[0].split(" "));
            }

        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            int maxx = game_instance.get_borderx();
            int maxy = game_instance.get_bordery();
            for (int x = 0; x < maxx; x++) {
                for (int y = 0; y < maxy; y++) {
                    game_instance.desactivate_color(x,y);
                    game_instance.set_to_zero(x,y);
                }
            }
        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int tmp_id_active,tmp_id_present,type,counter;
        int maxy;
        type = -1;
        counter=1;
        setContentView(R.layout.layout_simon);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("VALUES", Context.MODE_PRIVATE);
        size_x = sharedPref.getInt(getString(R.string.Key_x), -1);
        size_y = sharedPref.getInt(getString(R.string.Key_Y), -1);
        max_time = sharedPref.getInt(getString(R.string.Key_Time_Set),-1);
        max_time *= 1000000000;
        if(size_x == -1 && size_y == -1){
            Toast.makeText(getApplicationContext(),R.string.No_Value,Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        if (size_x == 2 && size_y == 2){
            type = 0;
            ViewStub stub = (ViewStub) findViewById(R.id.stub_simon);
            stub.setLayoutResource(R.layout.circle_grid_2_2);
            View inflated = stub.inflate();
        }
        if (size_x == 3 && size_y == 2){
            type = 1;
            ViewStub stub = (ViewStub) findViewById(R.id.stub_simon);
            stub.setLayoutResource(R.layout.circle_grid_3_2);
            View inflated = stub.inflate();
        }
        if (size_x == 3 && size_y == 3){
            type =2;
            ViewStub stub = (ViewStub) findViewById(R.id.stub_simon);
            stub.setLayoutResource(R.layout.circle_grid_3_3);
            View inflated = stub.inflate();
        }
        if (size_x == 5 && size_y == 3){
            type = 3;
            ViewStub stub = (ViewStub) findViewById(R.id.stub_simon);
            stub.setLayoutResource(R.layout.circle_grid_5_3);
            View inflated = stub.inflate();
        }
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Button_Game[][] tmp_button = new Button_Game[size_x][size_y];
        writer = ((TextView) findViewById(R.id.writer_simon));
        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/OpenSans-Bold.ttf");
        writer.setTypeface(font);
        maxy = size_y;
        for (int x=0;x<size_x;x++) {
            if(type == 1 && x == 1){
                maxy = 1;
            }
            for (int y = 0; y < maxy; y++) {
                switch (type) {
                    case 0:
                        if (counter == 3)
                            counter++;
                        break;
                    case 2:
                    case 3:
                        if (counter == 6)
                            counter = 1;
                        break;
                }
                tmp_id_active = getResources().getIdentifier("inside_circle" + counter, "drawable", getPackageName());
                tmp_id_present = getResources().getIdentifier("present_circle" + counter, "drawable", getPackageName());
                counter++;


                tmp_button[x][y] = new Button_Game((ImageButton) findViewById(getResources().getIdentifier("imageButton" + x + y, "id", getPackageName())), tmp_id_active, R.color.Bottom_App, tmp_id_present);
            }
        }
        game_instance = new Reaction_Game(size_x,size_y,max_time,tmp_button);

        progressBar = (ProgressBar) findViewById(R.id.timer_progress);
        progressBar.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setMax(game_instance.get_max_in_second());
        progressBar.setProgress(game_instance.get_max_in_second());
        // Start long running operation in a background thread
        /*bat_manager = new Thread(new Runnable() {
            public void run() {
                while (!game_instance.is_end_game()) {
                    // Update the progress bar and display the
                    //current value in the text view
                    if(game_instance.left_time() == -30)
                        continue;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(game_instance.left_time());
                        }
                    });
                    try {
                        // Sleep for 10 milliseconds.
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                onloose();
                return;
            }
        });*/
        bat_manager = new Timer_Bar();

    }


    private class Timer_Bar extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            while (!game_instance.is_end_game()) {
                // Update the progress bar and display the
                //current value in the text view
                if(game_instance.left_time() == -30)
                    continue;
                handler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(game_instance.left_time());
                    }
                });
                try {
                    // Sleep for 10 milliseconds.
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            onloose();
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MenuInflater inflater = getMenuInflater();
        getSupportActionBar().setElevation(0);
        inflater.inflate(R.menu.connect_menu, menu);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(launched_client) {
            connect_carpet.cancel(true);
            tmp_icon.setIcon(R.drawable.wifi_off);
            launched_client = false;
        }
    }

    public void onloose(){
        Intent intent;
        intent = new Intent(this,Score_Activity.class);
        intent.putExtra(getString(R.string.GAME_KEY),"Reaction");
        intent.putExtra(getString(R.string.KEY_TIME),game_instance.get_max_in_second());
        intent.putExtra(getString(R.string.SCORE_KEY),game_instance.get_score());
        this.startActivityForResult(intent,getResources().getInteger(R.integer.Request_Score_Reaction));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean result;
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == getResources().getInteger(R.integer.Request_Score_Reaction) && data != null){
            result = data.getBooleanExtra(getString(R.string.Play_Again),false);
            if(!result){
                this.finish();
            }
            if(bat_manager != null) {
                bat_manager.cancel(true);
            }
            bat_manager = new Timer_Bar();
            bat_manager.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            game_instance.reset_game();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.Connect_Button:
                if(!launched_client){
                    connect_carpet = new Client(writer,item,!isOnline());
                    connect_carpet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else{
                    connect_carpet.cancel(true);
                    item.setIcon(R.drawable.wifi_off);
                }
                tmp_icon = item;
                launched_client = false;
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
