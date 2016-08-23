package com.example.sokomo.sensifun;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by sokomo on 11/08/16.
 */
public class Simon_Button_Information implements MediaPlayer.OnCompletionListener{

    private boolean pressed;
    private ImageButton button;
    private Sound_Information sound;
    private int color_active;
    private int color_inactive;
    private boolean push_go;
    private int go_id_drawable;
    private ImageButton button_play;
    //Attribute to lock a dale to avoid to have multiple activation
    private boolean locked_dale;

    public Simon_Button_Information(ImageButton _button, Context Application, int id, int active, int inactive){
        pressed = false;
        button = _button;
        sound = new Sound_Information(Application,id);
        sound.On_Completion(this);
        color_active = active;
        color_inactive = inactive;

    }

    public Simon_Button_Information(ImageButton _button, Context Application, int id, int active, int inactive, int id_go, ImageButton change_button){
        pressed = false;
        button = _button;
        sound = new Sound_Information(Application,id);
        sound.On_Completion(this);
        color_active = active;
        locked_dale = false;
        color_inactive = inactive;
        go_id_drawable = id_go;
        button_play = change_button;

    }

    public void set_lock(){ locked_dale =true;}

    public void unlock(){ locked_dale = false;}

    public boolean locked(){ return  locked_dale;}

    public void set_push_go(){
        push_go = true;
    }

    public boolean get_pressed(){
        return pressed;
    }

    public ImageButton get_button(){
        return button;
    }

    public Sound_Information get_sound(){
        return sound;
    }

    public void change_button_state(){
        //if(pressed){
           // pressed= false;
            //button.setBackgroundResource(color_active);
        button.setBackgroundResource(color_active);
        sound.Sound_Play();
            return;
        //}
        //pressed = true;
        //button.setImageResource(color_inactive);

    }


    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //mediaPlayer.release();
        button.setBackgroundResource(color_inactive);
        if(push_go){
            button_play.setImageResource(go_id_drawable);
            push_go = false;
        }
    }
}
