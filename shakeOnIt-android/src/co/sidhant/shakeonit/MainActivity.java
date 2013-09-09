package co.sidhant.shakeonit;

import java.nio.charset.Charset;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication implements RequestHandler, CreateNdefMessageCallback{
	NfcAdapter mNfc;
	static ShakeOnIt myShakeOnIt;
	IntentFilter[] intentFiltersArray;
	PendingIntent pendingIntent;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Read info from intent in case app was started from NFC intent
        mNfc = NfcAdapter.getDefaultAdapter(this);
        if (mNfc == null) {
            Toast.makeText(this, "NFC is not available. Try turning it on.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = false;
        cfg.useCompass = false;
        myShakeOnIt = new ShakeOnIt(this);
        mNfc.setNdefPushMessageCallback(this, this);
        
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
//            processIntent(getIntent());
//        }
        
        //Enable foreground dispatch system to make sure only one instance runs at a time
        pendingIntent = PendingIntent.getActivity(
        	    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("application/vnd.co.sidhant.shakeonit");   
        }
        catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
       intentFiltersArray = new IntentFilter[] {ndef, };
       
       initialize(myShakeOnIt , cfg);
    }
    
    // Native dialog to confirm the user wants to exit on back button press
    @Override
    public void confirm(final ConfirmInterface confirmInterface, final boolean menu) {
        runOnUiThread(new Runnable(){
       @Override
       public void run() {
        if(menu)
        {
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
        else
        {
            new AlertDialog.Builder(MainActivity.this)                                     
            .setTitle("Cancel game?")
//            .setMessage("Quit?")                                           
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmInterface.yes();
                    myShakeOnIt.resetGame();
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
        mNfc.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
        
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }
	
	@Override
	public void onPause() {
	    super.onPause();
	    mNfc.disableForegroundDispatch(this);
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
        myShakeOnIt.recvData(recvdName);
//        Log.v("name received", recvdName);
    }

}