package com.example.sokomo.sensifun;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by sokomo on 10/08/16.
 */
public class Game_Jump extends Carpet_Manager{
    private long score;
    private long step_score;
    private boolean dale_activate;
    private long nb_clear;
    private int difficulty;
    private int goal_x;
    private int goal_y;
    private boolean[][] activate;

    Game_Jump(int nbx , int nby,ImageButton[][] button_app){
        super(nbx,nby,button_app);
        difficulty = 1;
        nb_clear = 0;
        score = 0;
        step_score = 100;
        dale_activate = false;
        activate = new boolean[nbx][nby];
    }

    public long Time_Before_New_State(){
        return 1000000 * difficulty;
    }


    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public String Activate_Random() {
        dale_activate = true;
        int x,y;
        nb_clear ++;
        if(nb_clear >= 10){
            nb_clear = 0;
            difficulty++;
        }
        String res = "";
        do {
            x = (getRandomNumberInRange(0, get_borderx()-1));
            y = (getRandomNumberInRange(0,get_bordery()-1));
            //System.err.println("x: "+x+" y: "+y+" carpet: "+carpet_state[x][y] + " active: " );
        }while (Button_Active(x,y) && x>=get_borderx() && y>=get_bordery());
        //carpet_state[x][y]=-10;
        goal_x = x;
        goal_y = y;
        res  += "2";
        res  += " ";
        res  += Integer.toString(y);
        res  += " ";
        res  += Integer.toString(x);
        res  += "";
        System.err.println("res       "+res);
        return res;
    }

    public long getScore(){return score;}
    public long getStepScore(){return step_score;}

    public boolean One_dale(){
        return dale_activate;
    }

    public void Cancel_Activate_Dale(){
        dale_activate = false;
        step_score = 100;
        desactivate_color(goal_x,goal_y);
        activate[goal_y][goal_y] = false;
        goal_x = -1;
        goal_y = -1;
    }




    @Override
    public void Change_Button_State(Context Application,int x, int y, boolean value){
        //call the carpet manger method
        super.Change_Button_State(Application,x,y,value);
        //writer.setText("after x: "+x+" y: "+y+" value : "+);

        //if a dale is active
        if(dale_activate && value ){
            //if is not the good dale then
            if((x != goal_x || y != goal_y) && Button_Active(x,y)){
                //step score decrease
                step_score -= 1 * difficulty;
            }
            //if it is the dale then
            if(x == goal_x && y == goal_y){
                //we add the score and go again
                score += step_score;
                step_score = 100;
                //carpet_state[x][y]=0;
                dale_activate = false;
            }
        }

        if(Button_Active(x,y)){
            activate[x][y] = true;
        }else{
            activate[x][y] = false;
        }

        if(score == 0){
            dale_activate = false;
            Toast.makeText(Application,"Dale désactiver",Toast.LENGTH_SHORT);
        }
    }

}
