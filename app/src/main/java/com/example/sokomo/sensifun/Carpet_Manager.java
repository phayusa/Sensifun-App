package com.example.sokomo.sensifun;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by sokomo on 22/07/16.
 */


//class who managed the interaction with the carpet
public class Carpet_Manager {

    protected int nb_x;
    protected int nb_y;
    private int [][] carpet_state_posit;
    private int [][] carpet_state_negat;
    private ImageButton [][] button_carpet;



    public Carpet_Manager(){
        return;
    }

    //add the size oF the carpet
    public Carpet_Manager(int nbx , int nby){
        nb_x = nbx;
        nb_y = nby;
        carpet_state_negat = new int[nbx][nby];
        carpet_state_posit = new int[nbx][nby];
    }

    //add the button paramter to manage inside the class the change of color
    public Carpet_Manager(int nbx , int nby, ImageButton[][] buttons_app){
        nb_x = nbx;
        nb_y = nby;
        carpet_state_negat = new int[nbx][nby];
        carpet_state_posit = new int[nbx][nby];
        button_carpet = buttons_app;
    }

    public int get_borderx(){
        return nb_x;
    }


    public int get_bordery(){
        return nb_y;
    }


    //return if a button is active or not
    //to overide if don't need to switch off the dale or change the native color
    public boolean Button_Active(int x, int y){
          return (carpet_state_posit[x][y] - carpet_state_negat[x][y])>0;
    }

    //desactivate a case color
    //to overide if don't need to switch off the dale or change the native color
    public void desactivate_color(int x,int y){
        button_carpet[x][y].setImageResource(R.color.inactive_color);
    }

    //activate a case color
    public void activate_color(int x,int y){
        button_carpet[x][y].setImageResource(R.color.press_color);
    }

    //active a dale by presence of a people
    public void present_color(int x,int y){
        button_carpet[x][y].setImageResource(R.color.Present_Carpet);
    }

    public void set_to_zero(int x,int y){
        carpet_state_negat[x][y]=0;
        carpet_state_posit[x][y]=0;
    }

    //check the color and changed his color if needed
    //to overide if don't need to switch off the dale or change the native color
    protected void check_button_color(int x,int y){
        if(Button_Active(x,y)){
            present_color(x,y);
            //button_carpet[x][y].setImageResource(R.color.Present_Carpet);
        }else{
            desactivate_color(x,y);
            //       button_carpet[x][y].setImageResource(R.color.inactive_color);
        }
    }


    //add points to the counter of button and changed if button activate change
    public void Change_Button_State(Context Application, int x, int y, boolean value){
        System.err.println("x: "+x+" y: "+y+" value_posti: "+carpet_state_posit[x][y]+" negat: "+carpet_state_negat[x][y] + " pressed "+value);
        //if it is pressed
        if(value)
            carpet_state_posit[x][y]++;
        else
            carpet_state_negat[x][y]++;

        check_button_color(x,y);

    }



}
