package com.example.sokomo.sensifun;

import android.widget.ImageButton;

/**
 * Created by sokomo on 17/08/16.
 */
public class Button_Game {
    private int color_active;
    private int color_inactive;
    private int color_present;
    private ImageButton button;
    private boolean pressed;

    Button_Game(ImageButton buttons,int active,int inactive,int present){
        color_inactive = inactive;
        color_active = active;
        color_present = present;
        button = buttons;
        pressed = false;
    }

    public ImageButton getButton(){
        return button;
    }

    public void desactivate_button(){
        button.setBackgroundResource(color_inactive);
    }

    public void activate_button(){
        button.setBackgroundResource(color_active);
    }

    public void present_button(){
        button.setBackgroundResource(color_present);
    }
}
