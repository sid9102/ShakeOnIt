package co.sidhant.shakeonit;

import java.nio.charset.Charset;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication implements RequestHandler, CreateNdefMessageCallback{
	NfcAdapter mNfc;
	static ShakeOnIt myShakeOnIt;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Read info from intent in case app was started from NFC intent
        String startData = null;
        mNfc = NfcAdapter.getDefaultAdapter(this);
        if (mNfc == null) {
            Toast.makeText(this, "NFC is not available. Try turning it on.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = false;
        cfg.useCompass = false;
        myShakeOnIt = new ShakeOnIt(this, startData);
        mNfc.setNdefPushMessageCallback(this, this);
        
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
        
        initialize(myShakeOnIt , cfg);
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

	@Override
	public NdefMessage createNdefMessage(NfcEvent arg0) {
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA ,
			    "application/vnd.co.sidhant.shakeonit".getBytes(Charset.forName("US-ASCII")),
			    new byte[0], myShakeOnIt.transmitData().getBytes(Charset.forName("US-ASCII")));
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] {
                		mimeRecord, NdefRecord.createApplicationRecord("co.sidhant.shakeonit")
                });
        return msg;
	}
	
	@Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }
	
	@Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }


    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        // only one message sent during the beam
    	Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        String recvdName = new String(msg.getRecords()[0].getPayload());
        myShakeOnIt = new ShakeOnIt(this, recvdName);
        Log.v("name received", recvdName);
    }

}