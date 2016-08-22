package com.example.sokomo.sensifun;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by sokomo on 12/08/16.
 */

//Class to Manage the Simon Game
public class Simon_Game extends Carpet_Manager{


    private int level;
    private List<Coord_Container> sequence_random_play;
    private long start;
    private int current_indice;
    private boolean played_sequence;
    private TextView writer;
    private Button_Simon_Information[][] buttons;
    private boolean random;
    private int lastx;
    private int lasty;
    private int lastx2;
    private int lasty2;
    private boolean in_play;
    private Sequence_Launcher current;
    private boolean loose;
    private ImageButton change_button;
    private int id_look_drawable;
    private int id_go_drawable;

    Simon_Game(int carpet_size_x,int carpet_size_y,Button_Simon_Information[][] buttons_Simon,TextView text_util,ImageButton middle,int id_look,int id_go){
        super(carpet_size_x,carpet_size_y);
        random = true;
        sequence_random_play = new ArrayList<Coord_Container>();
        level = 1;
        buttons = new Button_Simon_Information[nb_x][nb_y];
        buttons = buttons_Simon;
        in_play = false;
        writer = text_util;
        lasty = -1;
        lastx = -1;
        lastx2 = -1;
        lasty2 = -1;
        loose = false;
        change_button = middle;
        id_look_drawable = id_look;
        id_go_drawable = id_go;
    }


    //Class to launch the sequence to see the dale to activate
    private class Sequence_Launcher extends AsyncTask<Void,Coord_Container,Void> {

        private long time_to_launch;

        public Sequence_Launcher(){
            time_to_launch = 0;
        }

        public Sequence_Launcher(long time_to_execute){

            time_to_launch =time_to_execute;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            in_play = true;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            in_play = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            start = System.nanoTime();
            while(System.nanoTime() - start < time_to_launch){

            }
            publishProgress(new Coord_Container(-1,-1));
            int size_sequence = sequence_random_play.size();
            Coord_Container tmp;
            start = System.nanoTime();
            int indice = 0;
            //boolean first = true;
            while (indice < size_sequence){
                tmp = sequence_random_play.get(indice);
                if(indice == size_sequence -1){
                    tmp.last = true;
                }
                /*if(first){
                    publishProgress(tmp);
                    first = false;
                }*/
                if(System.nanoTime()-start >= 1000000000){
                    publishProgress(tmp);
                    indice++;
                    start = System.nanoTime();
                    //first = true;
                }
            }
            publishProgress(new Coord_Container(-2,-2));
            return null;
        }

        @Override
        protected void onProgressUpdate(Coord_Container... values) {
            super.onProgressUpdate(values);
            if(values[0].x == -2 && values[0].y == -2) {
                writer.setText("Your turn");

                //change_button.setImageResource(id_go_drawable);
            }
            else
            if(values[0].x == -1 && values[0].y == -1) {
                writer.setText("Look");
                change_button.setImageResource(id_look_drawable);
            }
            else {
                if(values[0].last){
                    buttons[values[0].x][values[0].y].set_push_go();
                }
                    buttons[values[0].x][values[0].y].change_button_state();
            }
        }


    }


    //function with the same body for launch sequence functions
    public void common_lauch_sequence(){
        if(!random) {
            if (sequence_random_play.size() != level) {
                generated_random_sequence();
            }
        }
        else{
            sequence_random_play = new ArrayList<Coord_Container>();
            int tmpx = lastx;
            int tmpy = lasty;
            int tmp2x = lastx2;
            int tmp2y = lasty2;
            for (int i=0;i<level;i++) {
                generated_random_sequence_block_same();
                System.err.println(i);
            }
            lastx = tmpx;
            lasty = tmpy;
            lastx2 = tmp2x;
            lasty2 = tmp2y;
        }
        played_sequence = true;
        current_indice = 0;
    }


    public boolean get_played_sequence(){
        return played_sequence;
    }

    public void launch_sequence(){
        common_lauch_sequence();
        current = new Sequence_Launcher();
        current.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void launch_sequence(boolean random_choice){
        random = random_choice;
        common_lauch_sequence();
        current = new Sequence_Launcher();
        current.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void launch_sequence(long time_to_execute){
        common_lauch_sequence();
        current = new Sequence_Launcher(time_to_execute);
        current.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void generated_random_sequence(){
        sequence_random_play.add(new Coord_Container(getRandomNumberInRange(0,1),getRandomNumberInRange(0,1)));
    }


    public void generated_random_sequence_block_same(){
        int x,y;
        do {
            x = getRandomNumberInRange(0, 1);
            y = getRandomNumberInRange(0, 1);
        } while ((x == lastx && y == lasty) || (x == lastx2 && y == lasty2));
        sequence_random_play.add(new Coord_Container(x,y));
        lastx2 = lastx;
        lasty2 = lasty;
        lastx = x;
        lasty = y;
    }

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public void button_pressed(int x,int y){
        Coord_Container tmp_pos;
        /*if(buttons[x][y].locked())
            return;*/
        if((lastx == x && lasty == y )|| (lasty2 == y && lastx2 == x))
            return;
//        buttons[lastx][lasty].unlock();
        buttons[x][y].change_button_state();
        //lock the button
        if(random) {
            lastx2 = lastx;
            lasty2 = lasty;
            lastx = x;
            lasty = y;
            //buttons[x][y].set_lock();
        }
        if(played_sequence) {
            tmp_pos = sequence_random_play.get(current_indice);
            if(tmp_pos.x == x && tmp_pos.y == y){
                current_indice++;
            }else{
                writer.setText("Perdu\nMeilleur Score: "+level);
                loose = true;
                played_sequence = false;


            }
            if(current_indice >= sequence_random_play.size()){
                level++;
                played_sequence = false;
                writer.setText("SCORE : "+level);
                launch_sequence(1000000000);

            }
        }
    }

    @Override
    protected void check_button_color(int x, int y) {
        //super.check_button_color(x, y);
        System.err.println("x: "+x+" y: "+y+" lastx: "+lastx+" lasty: "+lasty);
        if(Button_Active(x,y)) {
            button_pressed(x,y);
        }
        /*
        //no thrust in inactivate -> slow
        if(!Button_Active(x,y) && buttons[x][y].locked())
            buttons[x][y].unlock();*/
        /*if(!Button_Active(x,y) && lastx!= x && lasty != y){
            buttons[x][y].unlock();
            writer.setText("Passe pas "+ x +" "+y);
            set_to_zero(x,y);
        }
        System.err.println("Passe pas "+x+" "+y);*/
    }

    public void on_click_simon_button(View v){
        if(in_play)
            return;
        for (int x = 0; x < nb_x; x++) {
            for (int y = 0; y < nb_y; y++) {
                if(v.getId() == buttons[x][y].get_button().getId()) {
                    button_pressed(x,y);
                }
            }
        }
    }

    public boolean loosed(){
        return loose;
    }


    public void reset(){
        loose = false;
        level = 1;
        /*for (int x=0;x<nb_x;x++)
            for (int y=0;y<nb_y;y++)
                set_to_zero(x,y);*/
        writer.setText("Press PLAY to begin the Game");
    }

    public String get_Type(){
        if(random)
            return "SIMON";
        return "MEMORY";
    }

    public int get_level(){
        return level;
    }

    public boolean in_play_sequence(){
        return in_play;
    }

    public void on_pause(){
        if(in_play)
            current.cancel(true);
        else
            in_play = true;
    }

    public void on_resume(){
        if(in_play)
            in_play = false;
    }
}
