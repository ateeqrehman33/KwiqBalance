package com.kwiqbalance.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by Ankit Chouhan on 29-12-2017.
 */

public class SmsReceiver extends BroadcastReceiver {
    private String TAG = SmsReceiver.class.getSimpleName();
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the data (SMS data) bound to intent
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();

            //String str = "";
            String timeStamp="";
            if (bundle != null) {
                // Retrieve the SMS Messages received
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                SmsMessage[] msgs = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();

                // For every SMS message received
                for (int i = 0; i < msgs.length; i++) {
                    // Convert Object array
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(msgs[i].getOriginatingAddress()).append(" : ");
                    sb.append(msgs[i].getMessageBody()).append("\n");
                    timeStamp = String.valueOf(msgs[i].getTimestampMillis());
                    // Sender's phone number
                    //str += "SMS from " + msgs[i].getOriginatingAddress() + " : ";
                    // Fetch the text message
                    //str += msgs[i].getMessageBody();
                   // str += "\n";
                }
                String message = sb.toString();
                // Display the entire SMS Message
                //Log.d(TAG, message);
                Intent i = new Intent("mycustombroadcast");
                i.putExtra("data", message);
                i.putExtra("timestamp",timeStamp);
                context.sendBroadcast(i);
            }
        }
    }
}