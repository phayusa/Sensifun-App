package com.example.sokomo.sensifun;

import android.os.AsyncTask;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by sokomo on 19/08/16.
 */
public class Communication_Carpet_Async extends AsyncTask<Void,String,Void> {

    private Socket client_socket;

    protected boolean error_exit;

    private MenuItem icon_connection;

    private boolean error_internet;

    protected  TextView writer;

    protected  BufferedReader recv_from_server;

    Communication_Carpet_Async(){
        error_exit = true;
        error_exit = true;
    }

    //constructor where nbx,nby is the max value possible
    Communication_Carpet_Async(TextView status_writer,MenuItem icon,boolean is_online) {
        error_exit = false;
        icon_connection = icon;
        error_internet = is_online;
        writer = status_writer;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (error_internet) {
            writer.setText(R.string.no_internet);
        } else {
            if(!error_exit) {
                writer.setText(R.string.finsh_game);
            }
            else{
                writer.setText(R.string.finsh_co);
            }

        }
        icon_connection.setIcon(R.drawable.wifi_off);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if (error_internet) {
                error_exit = true;
                this.cancel(true);
            }
            client_socket = new Socket(InetAddress.getByName("192.168.0.17"), 3333);
            recv_from_server = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            BufferedWriter send_to_server = new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream()));
            String frame;
            send_to_server.write("realtime\n");
            send_to_server.flush();
            publishProgress("Prems");
            while (client_socket.isConnected() && !this.isCancelled()) {
                frame = recv_from_server.readLine();
                if (frame == null) {
                    System.err.println("Erreur inactivité");
                    error_exit = true;
                    this.cancel(true);
                    return null;
                }
                System.err.println("frame: " +frame);
                publishProgress(frame);
                do_current_co();
            }
            client_socket.close();
        } catch (Exception e) {
            error_exit = true;
            System.err.println("Error connection with client impossible");
            e.printStackTrace();
        }
        return null;
    }


    //To OVERIDE for do more things
    public void do_current_co(){

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if(values[0] == "Prems"){
            icon_connection.setIcon(R.drawable.wifi_on);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (error_exit) {
            if(error_internet) {
                writer.setText(R.string.no_internet);
            }else {
                writer.setText(R.string.no_connection);
            }
        }
        icon_connection.setIcon(R.drawable.wifi_off);
    }
}
