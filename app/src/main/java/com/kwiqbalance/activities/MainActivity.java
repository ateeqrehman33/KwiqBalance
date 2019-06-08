package com.kwiqbalance.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.kwiqbalance.utils.AppUtils;
import com.kwiqbalance.models.OperatorModel;
import com.kwiqbalance.R;
import com.kwiqbalance.receivers.SmsReceiver;
import com.kwiqbalance.models.BalanceModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private Button checkBalanceFirstBtn;
    private TextView lastCheckTv, dataTv, messageTv;
    private List<OperatorModel> mOperatorList;
    private RadioGroup radioGroup;
    private RadioButton sim1, sim2;
    private PendingIntent sentPendingIntent;
    private PendingIntent deliveredPendingIntent;
    private ArrayList<BalanceModel> balanceArrayList;
    private SmsReceiver smsReceiver;
    private BroadcastReceiver smsSendingReceiver, smsDeliveredReceiver;


    private FloatingActionMenu materialDesignFAM;
    private FloatingActionButton share, rate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    /**
     * Method to initialize views.
     */
    private void initViews() {
        mAdView = findViewById(R.id.adview);
        checkBalanceFirstBtn = findViewById(R.id.btn_check_balance_first);
        lastCheckTv = findViewById(R.id.tv_last_check);
        dataTv = findViewById(R.id.tv_data_balance);
        messageTv = findViewById(R.id.tv_message);
        radioGroup = findViewById(R.id.radio_group);

        materialDesignFAM = findViewById(R.id.material_design_android_floating_action_menu);
        share = findViewById(R.id.share);
        rate = findViewById(R.id.rate);
        sim1 = findViewById(R.id.rb_sim1);
        sim2 = findViewById(R.id.rb_sim2);
        mOperatorList = new ArrayList<>();
        balanceArrayList = AppUtils.getBalanceData(this);
        setAd();
        grantPermission();

        share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = "Link to Play store.";
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "Sharing Using"));
            }
        });
        rate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
    }

    /**
     * Method to get network info.
     */
    private void getNetworkInfo() {
        checkBalanceFirstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                if ((sim1.isShown() || sim2.isShown()) && radioGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getApplicationContext(), "Please select network to check balance", Toast.LENGTH_SHORT).show();
                } else if (mOperatorList.size() > 0) {
                    registerReceivers();
                } else {
                    Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT).show();
                }
            }

        });


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
            if (subscriptionManager != null) {
                List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                if (activeSubscriptionInfoList != null) {
                    for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                        if (!subscriptionInfo.getCarrierName().toString().contains("No service")) {
                            mOperatorList.add(new OperatorModel(subscriptionInfo.getDisplayName().toString(), subscriptionInfo.getSubscriptionId()));
                        }
                        //int mcc = subscriptionInfo.getMcc();
                        // int mnc = subscriptionInfo.getMnc();
                    }
                }
            }
        }
        if (mOperatorList.size() == 1) {
            sim1.setVisibility(View.VISIBLE);
            sim2.setVisibility(View.GONE);
            sim1.setText(mOperatorList.get(0).getDisplayName());
        } else if (mOperatorList.size() == 2) {
            sim1.setVisibility(View.VISIBLE);
            sim2.setVisibility(View.VISIBLE);
            sim1.setText(mOperatorList.get(0).getDisplayName());
            sim2.setText(mOperatorList.get(1).getDisplayName());
        } else {
            sim1.setVisibility(View.GONE);
            sim2.setVisibility(View.GONE);
        }

        smsReceiver = new SmsReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                String msg = null;
                String timestamp = null;
                if (bundle != null) {
                    msg = bundle.getString("data");
                    timestamp = bundle.getString("timestamp");
                }
                if (msg != null) {
                    dataTv.setText("");
                    if (msg.contains("Jio") || msg.contains("VFCARE")) {
                        messageTv.setText(msg);
                        if (msg.contains("Data")) {
                            dataTv.setText(msg.substring(msg.indexOf("Data") + 6, msg.indexOf("Voice")));
                        }
                        if (timestamp != null) {
                            lastCheckTv.setText(AppUtils.getDate(Long.parseLong(timestamp)));
                        }
                    }
                }

            }
        };
        registerReceiver(smsReceiver, new IntentFilter("mycustombroadcast"));
    }


    /**
     * registering message sending events.
     */
    private void registerReceivers() {
        String SMS_SENT = "SMS_SENT";
        String SMS_DELIVERED = "SMS_DELIVERED";
        sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

        // For when the SMS has been sent
        smsSendingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(smsSendingReceiver, new IntentFilter(SMS_SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(SMS_DELIVERED));

        if (sim1.isChecked()) {
            sendingSms(sim1.getText().toString());

        } else if (sim2.isChecked()) {
            sendingSms(sim2.getText().toString());
        }
    }

    /**
     * sending sms  or callinng ussd.
     */



    public void sendingSms(String simname) {
        //build ussd code based on

        //Idea India
        if (simname.contains("Idea") || simname.contains("IDEA") || simname.contains("idea")) {

            String code = "*191*1" + Uri.encode("#");
            //contact network provider

            startActivity(new Intent("android.intent.action.CALL", Uri.parse(code)));
        }


    }

/*/
    private void sendingSms(String text) {
        for (OperatorModel data : mOperatorList) {
            if (text.equalsIgnoreCase(data.getDisplayName())) {
                for (BalanceModel balanceModel : balanceArrayList) {
                    if (data.getDisplayName().equalsIgnoreCase(balanceModel.getCarrierName())) {
                        startActivity(new Intent("android.intent.action.CALL", Uri.parse(balanceModel.getNumber())));


                        break;
                    }
                }
                break;
            }
        }
    }
/*/
    /**
     * Method to set ads.
     */
    private void setAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitialAd_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
            @Override
            public void onAdClosed() {
            }

            @Override
            public void onAdFailedToLoad(int i) {
            }
        });
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    /**
     * Method to grant location permission.
     */
    private void grantPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS}, 10);
        } else {
            getNetworkInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_DENIED && grantResults[1] != PackageManager.PERMISSION_DENIED) {
                    getNetworkInfo();
                } else {
                    grantPermission();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
        if (smsSendingReceiver != null) {
            unregisterReceiver(smsSendingReceiver);
        }
        if (smsDeliveredReceiver != null) {
            unregisterReceiver(smsDeliveredReceiver);
        }
        super.onDestroy();
    }
}
