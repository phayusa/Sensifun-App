package com.example.sokomo.sensifun;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuView;
import android.view.*;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;



public class Game_Activity extends AppCompatActivity {

    static boolean connect;

    private MenuItem tmp;

    //Async task to manage the connection with the carpet
    private Client launched_client;

    private int size_x;
    private int size_y;

    private class Client extends AsyncTask<Void, String, Void> {

        private TextView writer;

        private Socket client_socket;
        //class which manage the game state
        private Game_Jump current_game;

        //activate a dale every X second
        private Timer generate_random_dale;

        private Timer end_game;

        private long start_time;

        private boolean error_exit;


        //constructor where nbx,nby is the max value possible
        Client(View t, int nbx, int nby) {
            writer = (TextView) t;
            ImageButton[][] tmp_buttons = new ImageButton[nbx][nby];
            for (int x=0;x<nbx;x++)
                for(int y=0;y<nby;y++){
                    tmp_buttons[x][y] = ((ImageButton) findViewById(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName())));
                }
            current_game = new Game_Jump(nbx, nby,tmp_buttons);
            start_time = System.nanoTime();
            generate_random_dale = new Timer();
            end_game = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    //System.err.println("Passed Random");
                    try {
                        if(current_game.One_dale())
                            current_game.Cancel_Activate_Dale();
                        publishProgress(current_game.Activate_Random());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Error no created game",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            };
            TimerTask tmp = new TimerTask() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Fin du jeu",Toast.LENGTH_SHORT);
                    generate_random_dale.cancel();
                    generate_random_dale.purge();
                }
            };
            generate_random_dale.schedule(tt, 5000, 2500);
            end_game.schedule(tmp,1000000);
            error_exit = false;
        }


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isOnline()) {
                writer.setText(R.string.no_internet);
            } else {
                if(!error_exit) {
                    writer.setText(R.string.finsh_game);
                }
                else{
                    writer.setText(R.string.finsh_co);
                }

            }
            /*generate_random_dale.cancel();
            generate_random_dale.purge();*/
            tmp.setIcon(android.R.drawable.button_onoff_indicator_off);
        }

        public void Pressed_button(String[] frame) {
            if (Integer.parseInt(frame[1]) >= current_game.get_bordery() || Integer.parseInt(frame[2]) >= current_game.get_borderx())
                return;
            try {

                ImageButton button = (ImageButton) findViewById(getResources().getIdentifier("imageButton" + frame[2] + frame[1], "id", getPackageName()));
                if(frame[0].equals("0")){
                    current_game.Change_Button_State(getApplicationContext(),Integer.parseInt(frame[2]), Integer.parseInt(frame[1]),false);
                }
                if (frame[0].equals("1") ) {
                    current_game.Change_Button_State(getApplicationContext(),Integer.parseInt(frame[2]), Integer.parseInt(frame[1]), true);
                }
                if (frame[0].equals("2")) {
                    writer.setText("Saute sur la case Rouge");
                    button.setImageResource(R.color.press_color);
                }
                writer.setText("Score: "+current_game.getScore()+"Step Score: "+current_game.getStepScore());
            } catch (Exception e) {
                System.err.println("Erreur sur " + frame[1] + " " + frame[2]);
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[0] == "Prems"){
                tmp.setIcon(android.R.drawable.button_onoff_indicator_on);
                writer.setText("Connection Réussie ");
            }else {
                Pressed_button(values[0].split(" "));
            }
        }



        @Override
        protected void onCancelled() {
            int maxx = current_game.get_borderx();
            int maxy = current_game.get_bordery();
            /*generate_random_dale.cancel();
            generate_random_dale.purge();*/
            for (int x = 0; x < maxx; x++) {
                for (int y = 0; y < maxy; y++) {
                    ImageButton button = (ImageButton) findViewById(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName()));
                    button.setImageResource(R.color.inactive_color);
                }
            }
            if (error_exit) {
                if(!isOnline()) {
                    writer.setText("Non connecté a internet");
                }else {
                    writer.setText("Connection Perdue");
                }
            } else {
                writer.setText("Jeu Fermé");
            }
            tmp.setIcon(android.R.drawable.button_onoff_indicator_off);
            connect = false;
        }

        public boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

        @Override
        protected Void doInBackground(Void... b) {
            try {
                if (!isOnline()) {
                    error_exit = true;
                    this.cancel(true);
                }
                client_socket = new Socket(InetAddress.getByName("192.168.0.17"), 3333);
               // client_socket.shutdownOutput();
                //client_socket.setSoTimeout(30000);
                //DataOutputStream send_from_server = new DataOutputStream(client_socket.getOutputStream());
                BufferedReader recv_from_server = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
                BufferedWriter send_to_server = new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream()));
                String frame;
                send_to_server.write("realtime\n");
                send_to_server.flush();
                publishProgress("Prems");

                while (client_socket.isConnected() && !this.isCancelled()) {
                    /*if (System.nanoTime()-start_time<=current_game.Time_Before_New_State()){
                        publishProgress(current_game.Activate_Random());
                        start_time = System.nanoTime();
                    }*/
                    frame = recv_from_server.readLine();
                    if (frame == null) {
                        System.err.println("Erreur inactivité");
                        client_socket.close();
                        error_exit = true;
                        return null;
                    }
                    System.err.println("frame: " +frame);
                    publishProgress(frame);
                }
                client_socket.close();
            } catch (Exception e) {
                error_exit = true;
                System.err.println("Error connection with client impossible");
                e.printStackTrace();
            }
            /*generate_random_dale.cancel();
            generate_random_dale.purge();*/
            return null;
        }

    }

    public static float convRateDpToPx(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi / 160f;
    }

    private void Create_Grid(){
        GridLayout grid = ((GridLayout) findViewById(R.id.gridLayout));
        grid.removeAllViews();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        int maxx = sharedPref.getInt(getString(R.string.Key_x), -1);
        int maxy = sharedPref.getInt(getString(R.string.Key_Y), -1);

        int weightx = 1/maxx;
        int weighty = 1/maxy;

        for (int x=0;x<maxx;x++)
            for (int y=0;y<maxy;y++){
                ImageButton tmp_button = new ImageButton(this);
                tmp_button.setImageResource(R.color.inactive_color);
                GridLayout.LayoutParams button_param =new GridLayout.LayoutParams();
                button_param.columnSpec = GridLayout.spec(y);
                button_param.rowSpec = GridLayout.spec(x);
                button_param.height = GridLayout.LayoutParams.MATCH_PARENT;
                button_param.width = GridLayout.LayoutParams.MATCH_PARENT;
                button_param.rightMargin = 5;
                button_param.topMargin = 5;
                grid.addView(tmp_button,button_param);
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("VALUES", Context.MODE_PRIVATE);
        size_x = sharedPref.getInt(getString(R.string.Key_x), -1);
        size_y = sharedPref.getInt(getString(R.string.Key_Y), -1);
        if(size_x == -1 && size_y == -1){
            Toast.makeText(getApplicationContext(),R.string.No_Value,Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        if (size_x == 2 && size_y == 2){
            ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
            stub.setLayoutResource(R.layout.layout_grid_2_2);
            View inflated = stub.inflate();
        }
        if (size_x == 3 && size_y == 2){
            ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
            stub.setLayoutResource(R.layout.layout_grid_3_2);
            View inflated = stub.inflate();
        }
        if (size_x == 3 && size_y == 3){
            ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
            stub.setLayoutResource(R.layout.layout_grid_3_3);
            View inflated = stub.inflate();
        }
        if (size_x == 5 && size_y == 3){
            ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
            stub.setLayoutResource(R.layout.layout_grid_5_3);
            View inflated = stub.inflate();
        }
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //Create_Grid();
        connect = false;

    }



    @Override
    protected void onDestroy() {
        if(launched_client != null) {
            launched_client.cancel(true);
        }
        super.onDestroy();
    }

    public void Test_Button(View view_pressed) {
        ImageButton pressed = (ImageButton) view_pressed;
        pressed.setImageResource(R.color.press_color);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.Connect_Button:
                if (!connect) {
                    connect = true;
                    launched_client = new Client(findViewById(R.id.Status_Text), size_x, size_y);
                    launched_client.execute();
                    tmp=item;
                    return true;
                }
                item.setIcon(android.R.drawable.button_onoff_indicator_off);
                launched_client.cancel(true);
                connect = false;
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
