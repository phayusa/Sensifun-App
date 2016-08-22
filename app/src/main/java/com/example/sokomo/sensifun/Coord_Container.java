package com.example.sokomo.sensifun;

/**
 * Created by sokomo on 11/08/16.
 */
public class Coord_Container {

    int x,y;
    boolean last;

    public Coord_Container(){
        x = 0;
        y = 0;
    }

    public Coord_Container(int _x,int _y){
        x= _x;
        y= _y;
    }

    public Coord_Container(int _x,int _y,boolean _last){
        x= _x;
        y= _y;
        last = _last;
    }
}
