package com.example.sokomo.sensifun;

import java.util.Random;

/**
 * Created by sokomo on 12/08/16.
 */
public class Reaction_Game extends Carpet_Manager {

    private long start;
    private long finish_time;
    private int score;
    private int goal_x;
    private int goal_y;
    private int last_x;
    private int last_y;
    private boolean first;
    private Button_Game[][] buttons;
    private boolean end_game;

    Reaction_Game(int sizex, int sizey, long max_time, Button_Game[][] button_tab){
        super(sizex,sizey);
        finish_time = max_time;
        score = 0;
        goal_x = -1;
        goal_y = -1;
        last_x = -1;
        last_y = -1;
        first = true;
        buttons = button_tab;
        //start = System.nanoTime();
    }

    public void set_end_game(){
        end_game = true;

    }

    public boolean is_end_game(){
        return end_game;
    }


    public int get_max_in_second(){
        return ((int) (finish_time / 1000000000));
    }

    public int left_time(){
        if(!first) {
            if((get_max_in_second() - current_time())<=0)
                set_end_game();
            return get_max_in_second() - current_time();
        }
        else
            return -30;
    }

    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public boolean get_first(){
        return first;
    }

    public int current_time() {
        return ((int)((System.nanoTime() - start) / 1000000000));
    }

    public int get_score(){return score/get_max_in_second();}

    @Override
    protected void check_button_color(int x, int y) {
        super.check_button_color(x, y);
        last_x = x;
        last_y = y;
        if(Button_Active(x,y) && x == goal_x && y == goal_y && !end_game){
            if(first){
                start = System.nanoTime();
                first = false;
            }
            score++;
            activate_random();
        }
    }

    @Override
    public void desactivate_color(int x, int y) {
        //super.desactivate_color(x, y);
        buttons[x][y].desactivate_button();
    }

    @Override
    public void activate_color(int x, int y) {
        //super.activate_color(x, y);
        buttons[x][y].activate_button();
    }

    @Override
    public void present_color(int x, int y) {
        //super.present_color(x, y);
        buttons[x][y].present_button();
    }

    public void reset_game(){
        first = true;
        end_game = false;
        score = 0;
    }

    public void activate_random(){
        int x,y;
        do {
            x = getRandomNumberInRange(0,nb_x-1);
            y = getRandomNumberInRange(0,nb_y-1);
        }while (x == last_x && y == last_y);
        activate_color(x,y);
        goal_x = x;
        goal_y = y;
        score +=1;
    }


}
