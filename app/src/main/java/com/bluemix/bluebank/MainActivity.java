package com.bluemix.bluebank;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    ////////////////////////////////////////////////
    //FILL THESE OUT WITH YOUR OWN VALUES!

    //Bluemix Mobile backend which will send push notifications
    private String BluemixMobileBackendApplication_ROUTE = "http://YOURBACKENDROUTE.mybluemix.net";
    private String BluemixMobileBackendApplication_App_GUID= "xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxx";

    //Application which will receive the feedback submitted from this Android application.
    //If running in a hybrid environment where feedback is stored on-premise, the Secure Gateway API would go here.
    //For this demo application, we are going to talk directly to the feedback manager application.
    private String FeedbackApplicationRoute = "http://FEEDBACKMANAGER_APP.mybluemix.net/";

    //Your name
    private String YourName = "Ram Vennam";
    ////////////////////////////////////////////////


    private static final String TAG = MainActivity.class.getSimpleName();
    static DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    static Date date = new Date();

    private MFPPush push = null;

    //private static final String consumerID = "BlueBank_" + dateFormat.format(date);
    private static final String consumerID = "BlueBankConsumer1";
    private static final String deviceAlias = "TargetDevice";
    EditText mEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            //Initialize the Core SDK
            BMSClient.getInstance().initialize(getApplicationContext(), BluemixMobileBackendApplication_ROUTE, BluemixMobileBackendApplication_App_GUID);
        } catch (MalformedURLException e) {
            System.out.println("ERROR : Initialize the Core SDK");
            e.printStackTrace();
        }

        //Initialize client Push SDK for Java
        MFPPush.getInstance().initialize(getApplicationContext());
        push = MFPPush.getInstance();


        push.register(new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String deviceId) {
                System.out.println("Registration successful");
            }

            @Override
            public void onFailure(MFPPushException ex) {
                ex.printStackTrace();
            }
        });
        final Activity activity = this;


        final Button button = (Button) findViewById(R.id.button_submit);
        mEdit = (EditText) findViewById(R.id.EditText_feedback);
        mEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    button.callOnClick();
                    handled = true;
                }
                return handled;
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Thank you!")
                        .setMessage("We received your feedback, and will get back to you shortly.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    new CallAPITask().execute(mEdit.getText().toString());
                                    finish();
                                    startActivity(getIntent());
                                } catch (Exception e) {
                                    Log.d(TAG, e.toString());
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The


        // action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Uses AsyncTask to create a task away from the main UI thread.
    private class CallAPITask extends AsyncTask<String, Void, String> {

        ListView list;

        @Override
        protected String doInBackground(String... params) {

            // params comes from the execute() call: params[0] is the url.
            try {
                callAPI(params[0]);
                return "Done!";
            } catch (Exception e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPost " + result);


        }

        public void callAPI(String feedback) throws Exception {
            String feedbackAPIURL = Uri.parse(FeedbackApplicationRoute).buildUpon().appendPath("submitFeedback").toString();
            URL url = new URL(feedbackAPIURL);
            JSONObject payloadjson = new JSONObject();
            payloadjson.put("user", YourName);
            payloadjson.put("feedback", feedback);
            Log.d(TAG, "feedback " + feedback);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(payloadjson.toString().getBytes());
            os.flush();

            StringBuilder sb = new StringBuilder();
            int HttpResult = conn.getResponseCode();
            Log.d(TAG, String.valueOf(HttpResult));
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    Log.d(TAG, line + "\n");
                }

                br.close();

                Log.d(TAG, "" + sb.toString());

            } else {
                Log.d(TAG, conn.getResponseMessage());
            }


        }
    }
}
