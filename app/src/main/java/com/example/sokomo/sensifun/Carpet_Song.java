package com.example.sokomo.sensifun;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sokomo on 27/07/16.
 */
public class Carpet_Song extends AppCompatActivity {
    private Sound_Information[][] sound_tab;
    private String mode;
    private Carpet_Receiver Connection;
    private boolean pressed_play;
    private boolean launched;
    private static final int Request_Song = 10;
    private boolean recording;
    private List<Complete_Sound> Save;
    private List<Complete_Sound> Save2;
    private long start_record;
    private relaunched_replay replay;
    private Menu menu_bar;
    private int size_x;
    private int size_y;

    //DECLARATION of storage class Complete Sound

    class Complete_Sound extends Coord_Container implements MediaPlayer.OnCompletionListener{
        private boolean pressed;
        private String mode;
        private long time_launched;

        Complete_Sound(int _x,int _y){
            super(_x,_y);
        }

        Complete_Sound(int _x,int _y,boolean _pressed,String _mode,long time){
            super(_x,_y);
            pressed = _pressed;
            mode = _mode;
            time_launched = time;
        }

        public boolean Compare(Complete_Sound other){
            return  (pressed&&other.pressed) && (mode == other.mode) && (time_launched == other.time_launched);
        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            ((ImageButton) findViewById(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName()))).setImageResource(R.color.inactive_color);
        }
    }


    //DECLARATION OF TWO ASYNC TASK


    //Declaration of the async task to manage the connection with the carpet
    private class Carpet_Receiver extends Communication_Carpet_Async{

        private Socket client_socket;
        private Boolean error_exit;

        Carpet_Receiver(){
            error_exit = false;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            launched = true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String[] frame_split = values[0].split(" ");
            if(values[0] == "Prems"){
                Toast.makeText(getApplicationContext(),R.string.sucess_connection,Toast.LENGTH_SHORT).show();
            }else {
                int x = Integer.parseInt(frame_split[2]);
                int y = Integer.parseInt(frame_split[1]);
                if(x< size_x && y<size_y){
                    if (mode != null) {
                        if (Integer.parseInt(frame_split[0]) == 1) {
                            System.err.println("Split Frame " + frame_split[2] + " " + frame_split[1]);
                            activate_sound(x, y);
                        } else {
                            if (sound_tab[x][y].Sound_Playing()) {
                                sound_tab[x][y].Sound_Stop(((ImageButton) findViewById(getResources().getIdentifier("imageButton" + frame_split[2] + frame_split[1], "id", getPackageName()))));
                            }
                        }
                    }
                }
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (error_exit){
                Toast.makeText(getApplicationContext(),R.string.no_internet,Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(),R.string.finsh_co,Toast.LENGTH_SHORT).show();
            }
            launched = false;
            cancel_all_sound();
            return ;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            cancel_all_sound();
        }

    }


    //Declaration of the asynctask to replay a played sequence
    private class relaunched_replay extends AsyncTask<Void,Complete_Sound,Void>{
        private boolean preced_pressed_play;
        private String preced_mode;
        private boolean finished;
        private List<Complete_Sound> list_to_read;

        relaunched_replay(List<Complete_Sound> list){
            list_to_read = list;
            finished = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            preced_pressed_play = pressed_play;
            preced_mode = mode;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long start = System.nanoTime();
            long current_time;
            int len_Save = list_to_read.size();
            int indice = 0;
            Complete_Sound tmp;
            while (indice < len_Save) {
                current_time = System.nanoTime() - start;

                tmp = list_to_read.get(indice);
                if (current_time >= tmp.time_launched) {
                    System.err.println(current_time + " to goal " + tmp.time_launched);
                    publishProgress(tmp);
                    indice++;
                }
                //System.err.println(tmp.time_launched);
                finished = false;
            }
            while(!finished){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pressed_play = preced_pressed_play;
            mode = preced_mode;
            cancel_all_sound();
            Toast.makeText(getApplicationContext(),"Enregistrement lus ",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Complete_Sound... values) {
            super.onProgressUpdate(values);
            mode = values[0].mode;
            pressed_play = values[0].pressed;
            System.err.println("x: "+values[0].x + " y: "+values[0].y+" mode: "+values[0].mode+" time: "+values[0].time_launched);
            activate_sound(values[0].x, values[0].y);
            finished = true;
        }
    }



    //ACTIVITY CODE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("VALUES", Context.MODE_PRIVATE);
        int shared_x = sharedPref.getInt(getString(R.string.Key_x), -1);
        int shared_y = sharedPref.getInt(getString(R.string.Key_Y), -1);
        System.err.println(shared_x);
        System.err.println(shared_y);
        if(shared_x == -1 && shared_y == -1){
            Toast.makeText(getApplicationContext(),R.string.No_Value,Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        create_sound_by_size(shared_x,shared_y);
    }

    //create a carpet representation with the parameters
    public void create_sound_by_size(int x_sound,int y_sound){
        size_x = x_sound;
        size_y = y_sound;
        setContentView(R.layout.song_layout);
        if (x_sound == 2 && y_sound == 2){
            ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
            stub.setLayoutResource(R.layout.layout_grid_2_2);
            View inflated = stub.inflate();
        }
        if (x_sound == 3 && y_sound == 2){
            ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
            stub.setLayoutResource(R.layout.layout_grid_3_2);
            View inflated = stub.inflate();
        }
        if (x_sound == 3 && y_sound == 3){
            ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
            stub.setLayoutResource(R.layout.layout_grid_3_3);
            View inflated = stub.inflate();
        }
        if (x_sound == 5 && y_sound == 3){
            ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
            stub.setLayoutResource(R.layout.layout_grid_5_3);
            View inflated = stub.inflate();
        }
        sound_tab = new Sound_Information[size_x][size_y];
        for(int x=0;x<size_x;x++)
            for (int y=0;y<size_y;y++){
                ((ImageButton) findViewById(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName()))).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (motionEvent.getAction()){
                            case MotionEvent.ACTION_UP  :
                                on_release(view);
                                return true;
                            case MotionEvent.ACTION_DOWN:
                                on_click_sound(view);
                                return true;
                        }
                        return false;
                    }
                });

                sound_tab[x][y] = new Sound_Information(getApplicationContext(),getResources().getIdentifier("effect"+Integer.toString(x*3 + (y+1)),"raw",getPackageName()));
                sound_tab[x][y].On_Completion(new Complete_Sound(x,y));

            }
        pressed_play = false;
        launched = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Activate the action bar and the return button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect_menu, menu);
        menu_bar = menu;
        return true;
    }



    //Function which launch or pause a sound effect
    public void activate_sound(int x,int y){
        System.err.println("x: "+x+" y: "+y+" mode: "+mode+" pressed:"+pressed_play);
        ImageButton button = ((ImageButton) findViewById(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName())));
        if(sound_tab[x][y] == null) {
            System.err.println("Vide " + Integer.toString(x) + " "+ Integer.toString(y));
            return;
        }
        long time = System.nanoTime()-start_record;
        if(mode == "Play") {
            if(sound_tab[x][y].Sound_Playing()) {
                sound_tab[x][y].Sound_Stop(button);
                if(recording){
                    Save.add(new Complete_Sound(x,y,pressed_play,mode,time));
                }
                return;
            }
            sound_tab[x][y].Sound_Play(button);
            if(recording) {
                Save.add(new Complete_Sound(x, y, pressed_play, mode,time));
            }
        }
        //Pause the current dale
        if(mode == "Pause"){
            sound_tab[x][y].Sound_Pause(button);
            if(recording) {
                Save.add(new Complete_Sound(x, y, pressed_play, mode,time));
            }
        }
        //launch the searchview to select the song to attribuate
        if(mode == "Choose"){
            Intent choose_list = new Intent(this,Song_Selection.class);
            choose_list.putExtra("X1",x);
            choose_list.putExtra("Y1",y);
            try {
                startActivityForResult(choose_list,Request_Song);
            }
            catch (SecurityException e){
                Toast.makeText(getApplicationContext(),"Droit non accepté\nVeuillez les Accepter pour accéder a votre musique",Toast.LENGTH_LONG);
                this.finish();
                e.printStackTrace();
            }
        }

    }

    //Function to get the result of the search view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Request_Song && data != null){
            int x = data.getIntExtra("X",-1);
            int y = data.getIntExtra("Y",-1);
            Toast.makeText(this,"You choose " + data.getStringExtra("DISPLAY_NAME") + "\nPour x :" + Integer.toString(x) + " Y: "+ Integer.toString(y),Toast.LENGTH_SHORT).show();
            sound_tab[x][y] = new Sound_Information(getApplicationContext(),data.getStringExtra("PATH"));
        }else{
            Toast.makeText(this,"No song selected",Toast.LENGTH_SHORT).show();
        }
    }


    public void cancel_all_sound(){
        for (int x=0;x<size_x;x++)
            for (int y=0;y<size_y;y++){
                try {
                    if (sound_tab[x][y].Sound_Playing()) {
                        sound_tab[x][y].Sound_Stop(((ImageButton) findViewById(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName()))));
                        if (recording) {
                            Save.add(new Complete_Sound(x, y, pressed_play, mode, System.nanoTime() - start_record));

                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    finish();
                }
            }
    }

    public void on_release(View v){
        if(pressed_play){
            for (int x=0;x<size_x;x++)
                for (int y=0;y<size_y;y++){
                    if(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName()) == v.getId()) {
                        sound_tab[x][y].Sound_Stop(((ImageButton) findViewById(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName()))));
                        if(recording){
                            Save.add(new Complete_Sound(x,y,pressed_play,mode,System.nanoTime()-start_record));

                        }
                        return;
                    }
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for(int x=0;x<size_x;x++)
            for (int y=0;y<size_y;y++){
                sound_tab[x][y].destroy();
            }
    }





    public void on_click_sound(View v){
        System.err.print("Appuyé sur ");
        switch (v.getId()){
            case R.id.Play_button:
                mode = "Play";
                pressed_play = false;
                System.err.println("Play");
                break;
            case R.id.Pressed_Play_Button:
                mode = "Play";
                pressed_play = true;
                System.err.println("Pressed Play");
                break;
            case R.id.Stop_Button:
                cancel_all_sound();
                System.err.println("Stop");
                break;
            case R.id.Pause_Button:
                mode= "Pause";
                System.err.println("Pause");
                break;
            case R.id.Choose_Song:
                //Activate the mode Choose
                //user must select a case after
                mode = "Choose";
                Toast.makeText(this,R.string.Select_Song,Toast.LENGTH_SHORT).show();
                System.err.println("Choose");
                break;
            case R.id.Rec_Button:
                //Record Mode
                if(!recording){
                    Save = new ArrayList<Complete_Sound>();
                    recording = true;
                    ((Button) findViewById(R.id.Rec_Button)).setText("Stop Record");
                    start_record = System.nanoTime();
                }else{
                    recording = false;
                    ((Button) findViewById(R.id.Rec_Button)).setText("Start Record");
                    System.err.println("Time: " + Long.toString((System.nanoTime()-start_record)/1000000000) + " Second " + Long.toString((System.nanoTime()-start_record)) );
                    System.err.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                    for (Complete_Sound tmp : Save){
                        System.err.println("x : "+Integer.toString(tmp.x)+" y "+Integer.toString(tmp.y)+" mode "+tmp.mode+" pressed "+tmp.pressed+" time "+tmp.time_launched);

                    }
                    System.err.println("END");

                    Save2 = new ArrayList<Complete_Sound>();

                    on_click_sound(findViewById(R.id.Stop_Button));

                    String path = Environment.getExternalStorageDirectory()+"/Sensifun/";
                    File folder = new File(path);
                    if(!folder.exists()){
                        folder.mkdir();
                    }
                    try {
                        //create the print writer of the file
                        PrintWriter file_print_writer = new PrintWriter(path+"aaaa.txt");
                        //Save the number of case initialisate
                        file_print_writer.println(Integer.toString(size_x)+" "+Integer.toString(size_y));
                        for (int x_current = 0;x_current<size_x;x_current++)
                            for (int y_current = 0;y_current<size_y;y_current++) {
                                if(sound_tab[x_current][y_current].get_id() != -1) {
                                    file_print_writer.println(Integer.toString(x_current) + " " + Integer.toString(y_current) + " id:" + sound_tab[x_current][y_current].get_id());
                                }else {
                                    file_print_writer.println(Integer.toString(x_current) + " " + Integer.toString(y_current) + " path:" + sound_tab[x_current][y_current].get_path());
                                }
                            }
                        for (Complete_Sound tmp : Save){
                            file_print_writer.println(Integer.toString(tmp.x)+" "+Integer.toString(tmp.y)+" "+tmp.mode+" "+tmp.pressed+" "+tmp.time_launched);
                        }
                        file_print_writer.flush();
                        file_print_writer.close();
                        Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

/*                    String line = "",path_id = "";
                    String[] reading_buffered_split;
                    int read_x = 0,read_y = 0;
                    String path2 = Environment.getExternalStorageDirectory()+"/Sensifun/";
                    File folder2 = new File(path2);
                    if(!folder2.exists()){
                        folder2.mkdir();
                    }
                    try {

                        //create the buffered reader of the file
                        BufferedReader file_buffer_reader= new BufferedReader(new FileReader(path2+"aaaa.txt"));
                        line = file_buffer_reader.readLine();
                        //load the size
                        read_x = Integer.parseInt(line.split(" ")[0]);
                        read_y = Integer.parseInt(line.split(" ")[1]);
                        create_sound_by_size(read_x,read_y);
                        int size_of_init = read_x * read_y;
                        //load the settings of the carpet
                        for (int current_line=0;current_line<size_of_init;current_line++){
                            line = file_buffer_reader.readLine();
                            reading_buffered_split = line.split(" ");
                            read_x = Integer.parseInt(reading_buffered_split[0]);
                            read_y = Integer.parseInt(reading_buffered_split[1]);
                            if(line.contains("id")){
                                path_id = line.split("id:")[1];
                                System.err.println("id "+path_id);
//                            sound_tab[read_x][read_y] = new Sound_Information(getApplicationContext(),Integer.parseInt(path_id));
                                sound_tab[read_x][read_y] = new Sound_Information(getApplicationContext(),getResources().getIdentifier("effect"+Integer.toString(read_x*3 + (read_y+1)),"raw",getPackageName()));

                            }else{
                                path_id = line.split("path:")[1];
                                sound_tab[read_x][read_y] = new Sound_Information(getApplicationContext(),path_id);
                            }
                            sound_tab[read_x][read_y].On_Completion(new Complete_Sound(read_x,read_y));
                        }
                        while ((line = file_buffer_reader.readLine())!= null){
                            reading_buffered_split = line.split(" ");
                            System.err.println(line);
                            for (String Split_read : reading_buffered_split){
                                System.err.println(Split_read);
                            }
                            Save2.add(new Complete_Sound(Integer.parseInt(reading_buffered_split[0]),Integer.parseInt(reading_buffered_split[1]),Boolean.parseBoolean(reading_buffered_split[3]),reading_buffered_split[2],Long.parseLong(reading_buffered_split[4])));
                        }
                        file_buffer_reader.close();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Erreur survenue lors de la récupération des données",Toast.LENGTH_LONG).show();
                        System.err.println(path_id+" "+Integer.toString(read_x)+" "+Integer.toString(read_y));
                    }*/
                }


                    /*LayoutInflater layoutInflater = LayoutInflater.from(this);
                    final View promptView = layoutInflater.inflate(R.layout.dialog_view, null);
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    //final EditText request_name = new EditText(getApplicationContext());

                    alert.setView(promptView);
                    //Si l'on appuye sur Sauvegarder
                    alert.setPositiveButton("Sauvegarder", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //get the value of the edittext to have the name of the file
                            String YouEditTextValue = ((EditText) promptView.findViewById(R.id.Filename_Text)).getText().toString();
                            if(YouEditTextValue == null){
                                Toast.makeText(getApplicationContext(),"Aucun nom rentrée",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(getApplicationContext(),YouEditTextValue,Toast.LENGTH_SHORT).show();
                            //go to the directory where replays are saved
                            String path = Environment.getExternalStorageDirectory()+"/Sensifun/";
                            File folder = new File(path);
                            if(!folder.exists()){
                                folder.mkdir();
                            }
                            try {
                                //create the print writer of the file
                                PrintWriter file_print_writer = new PrintWriter(path+YouEditTextValue+".txt");
                                //Save the number of case initialisate
                                file_print_writer.println(Integer.toString(sizex)+" "+Integer.toString(sizey));
                                for (int x_current = 0;x_current<size_x;x_current++)
                                    for (int y_current = 0;y_current<size_y;y_current++) {
                                        if(sound_tab[x_current][y_current].get_id() != -1) {
                                            file_print_writer.println(Integer.toString(x_current) + " " + Integer.toString(y_current) + " id:" + sound_tab[x_current][y_current].get_id());
                                        }else {
                                            file_print_writer.println(Integer.toString(x_current) + " " + Integer.toString(y_current) + " path:" + sound_tab[x_current][y_current].get_path());
                                        }
                                    }
                                for (Complete_Sound tmp : Save){
                                    file_print_writer.println("x: "+Integer.toString(tmp.x)+"y: "+Integer.toString(tmp.y)+" mode: "+tmp.mode+" pressed: "+tmp.pressed+" time: "+tmp.time_launched);

                                }
                                file_print_writer.flush();
                                file_print_writer.close();
                                Toast.makeText(getApplicationContext(),"Fin écriture",Toast.LENGTH_LONG).show();
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    //Si l'op appuye sur Annuler
                    alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });

                    alert.show();
                }*/


                break;
            default:
                //Case the click is on the grid
                for(int x=0;x<size_x;x++)
                    for (int y=0;y<size_y;y++){
                        if(getResources().getIdentifier("imageButton" + Integer.toString(x) + Integer.toString(y), "id", getPackageName()) == v.getId()) {
                            activate_sound(x,y);
                            break;
                        }

                    }
        }

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(launched)
                    Connection.cancel(true);
                this.finish();
                return true;
            case R.id.Connect_Button:
                if(!launched) {
                    Connection = new Carpet_Receiver();
                    Connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else
                    Toast.makeText(getApplicationContext(),"Connection Déja lancé",Toast.LENGTH_SHORT);
                System.err.println("Connect");
                return true;
            case R.id.Replay_Mode:
                String line = "",path_id = "";
                String[] reading_buffered_split;
                int read_x = 0,read_y = 0;
                String path = Environment.getExternalStorageDirectory()+"/Sensifun/";
                File folder = new File(path);
                if(!folder.exists()){
                    folder.mkdir();
                }
                try {

                    //create the buffered reader of the file
                    BufferedReader file_buffer_reader= new BufferedReader(new FileReader(path+"aaaa.txt"));
                    line = file_buffer_reader.readLine();
                    //load the size
                    read_x = Integer.parseInt(line.split(" ")[0]);
                    read_y = Integer.parseInt(line.split(" ")[1]);
                    create_sound_by_size(read_x,read_y);
                    int size_of_init = read_x * read_y;
                    //load the settings of the carpet
                    for (int current_line=0;current_line<size_of_init;current_line++){
                        line = file_buffer_reader.readLine();
                        reading_buffered_split = line.split(" ");
                        read_x = Integer.parseInt(reading_buffered_split[0]);
                        read_y = Integer.parseInt(reading_buffered_split[1]);
                        if(line.contains("id")){
                            path_id = line.split("id:")[1];
                            System.err.println("id "+path_id);
//                            sound_tab[read_x][read_y] = new Sound_Information(getApplicationContext(),Integer.parseInt(path_id));
                            sound_tab[read_x][read_y] = new Sound_Information(getApplicationContext(),getResources().getIdentifier("effect"+Integer.toString(read_x*3 + (read_y+1)),"raw",getPackageName()));

                        }else{
                            path_id = line.split("path:")[1];
                            sound_tab[read_x][read_y] = new Sound_Information(getApplicationContext(),path_id);
                        }
                        sound_tab[read_x][read_y].On_Completion(new Complete_Sound(read_x,read_y));
                    }
                    //TODO : Correct the problem read files, the reading list don't d the same thing than the other list
                    //Save2 = new ArrayList<Complete_Sound>();
                    while ((line = file_buffer_reader.readLine())!= null){
                        reading_buffered_split = line.split(" ");
                        System.err.println(line);
                        for (String Split_read : reading_buffered_split){
                            System.err.println(Split_read);
                        }
                        Save2.add(new Complete_Sound(Integer.parseInt(reading_buffered_split[0]),Integer.parseInt(reading_buffered_split[1]),Boolean.parseBoolean(reading_buffered_split[3]),reading_buffered_split[2],Long.parseLong(reading_buffered_split[4])));
                    }
                    file_buffer_reader.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Erreur survenue lors de la récupération des données",Toast.LENGTH_LONG).show();
                    System.err.println(path_id+" "+Integer.toString(read_x)+" "+Integer.toString(read_y));
                    return true;
                }
                System.err.println("Je vais lancer le replay");
                for (Complete_Sound tmp: Save){
                    System.err.println(" x: "+tmp.x +" y: "+ tmp.y + " pressed: "+ tmp.pressed + " time: " + tmp.time_launched +" mode: "+ tmp.mode);
                }
                //Save = new ArrayList<Complete_Sound>(Save2);
                System.err.println("Save 2");
                for (Complete_Sound tmp2: Save2){
                    System.err.println("x: "+tmp2.x +" y: "+ tmp2.y + " pressed: "+ tmp2.pressed + " time: " + tmp2.time_launched +" mode: "+ tmp2.mode);

                }
                for (int ind=0;ind<Save.size();ind++)
                    if(Save.get(ind) == Save2.get(ind))
                        finish();
                    else
                        System.err.println(Save.get(ind));
                //Save: list from previous launched
                //Save2 : list from file reading
                replay =  new relaunched_replay(Save2);
                replay.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}


