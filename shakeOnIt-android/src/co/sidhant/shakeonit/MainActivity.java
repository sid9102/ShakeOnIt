package co.sidhant.shakeonit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication implements RequestHandler{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = false;
        cfg.useCompass = false;

        initialize(new ShakeOnIt(this), cfg);
    }
    
    // Native dialog to confirm the user wants to exit on back button press
    @Override
    public void confirm(final ConfirmInterface confirmInterface) {
        runOnUiThread(new Runnable(){
       @Override
       public void run() {
        new AlertDialog.Builder(MainActivity.this)                                     
                .setTitle("Quit?")
//                .setMessage("Quit?")                                            
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmInterface.yes();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
               .create().show();        
       }        
    });
    }
}