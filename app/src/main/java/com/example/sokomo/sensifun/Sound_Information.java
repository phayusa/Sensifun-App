package com.example.sokomo.sensifun;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by sokomo on 02/08/16.
 */

//Class wich contains the different possiblity that a sound can do
public class Sound_Information {
    private int id;
    public MediaPlayer sound;
    private String path;
    private boolean ready;
    private Context appli_context;

    Sound_Information(Context context_application, int Ressorce_Id) {
        sound = MediaPlayer.create(context_application, Ressorce_Id);
        id = Ressorce_Id;
        path = null;
        ready = false;
        appli_context = context_application;
        ready = true;

    }

    Sound_Information(Context context_application,String Path_Song) {
        try {
            sound = new MediaPlayer();
            Uri t = Uri.parse(Path_Song);
            sound.setDataSource(context_application, t);
            path = Path_Song;
            id = -1;
            appli_context = context_application;
            sound.prepare();
            sound.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    ready = true;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int get_id(){
        return id;
    }

    public String get_path(){
        return path;
    }

    public void On_Completion(Carpet_Song.Complete_Sound Sound){
        sound.setOnCompletionListener(Sound);
        return;
    }

    public void On_Completion(MediaPlayer.OnCompletionListener t){
        sound.setOnCompletionListener(t);
        return;
    }

    public void Sound_Stop(ImageButton button_to_stop){
        sound.pause();
        sound.seekTo(0);
        button_to_stop.setImageResource(R.color.inactive_color);
    }

    public void Sound_Pause(ImageButton button_to_pause){
        sound.pause();
        button_to_pause.setImageResource(R.color.Pause_color);
    }

    public boolean Sound_Playing(){
        return sound.isPlaying();
    }

    public void Sound_Play(ImageButton button_to_activate){
        try {
            //sound.prepare();
            if(!ready){
                Toast.makeText(appli_context,"Son non prêt",Toast.LENGTH_SHORT).show();
                return;
            }
            sound.start();
            button_to_activate.setImageResource(R.color.press_color);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void Sound_Play(){
        try {
            sound.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void destroy(){
        sound.release();
    }
}