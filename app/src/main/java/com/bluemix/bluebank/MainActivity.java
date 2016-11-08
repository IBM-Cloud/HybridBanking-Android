package com.bluemix.bluebank;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ////////////////////////////////////////////////
    //FILL THESE OUT WITH YOUR OWN VALUES!

    // TODO: Replace <APP_GUID> and <CLIENT_SECRET> with a valid App GUID and Client Secret from your Bluemix Push dashboard -> Configure -> Mobile Options
    private String BluemixMobileBackendApplication_CLIENT_SECRET = "xxxxxxxxxxxx";
    private String BluemixMobileBackendApplication_App_GUID = "xxxxx-xxxx-xxxx-xxxx-xxxxx";


    // Application which will receive the feedback submitted from this Android application.
    //If running in a hybrid environment where feedback is stored on-premise, the Secure Gateway API would go here.
    //For this demo application, we are going to talk directly to the feedback manager application.
    //TODO: Specify your own Feedback Manager application
    private String FeedbackApplicationRoute = "http://feedbackmanager.mybluemix.net/";

    //Your name
    private String YourName = "Ram Vennam";
    ////////////////////////////////////////////////


    private static final String TAG = MainActivity.class.getSimpleName();
    static DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    static Date date = new Date();

    private MFPPush push;
    private MFPPushNotificationListener notificationListener;

    EditText mEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize core SDK with IBM Bluemix application Region, TODO: Update region if not using Bluemix US SOUTH
        BMSClient.getInstance().initialize(this, BMSClient.REGION_US_SOUTH);

        // Grabs push client sdk instance
        push = MFPPush.getInstance();
        // Initialize Push client
        // You can find your App Guid and Client Secret by navigating to the Configure section of your Push dashboard, click Mobile Options (Upper Right Hand Corner)

        push.initialize(this, BluemixMobileBackendApplication_App_GUID, BluemixMobileBackendApplication_CLIENT_SECRET);

        // A notification listener is needed to handle any incoming push notifications within the Android application
        // This might be immediate or take several minutes.
        notificationListener = new MFPPushNotificationListener() {
            @Override
            public void onReceive (final MFPSimplePushNotification message) {
                // TODO: process the message, add your logic here
                android.util.Log.i(TAG, "Received a Push Notification: " + message.toString());
                runOnUiThread(new Runnable() {
                    public void run() {
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("Push Notification Received")
                                .setMessage(message.getAlert())
                                .show();
                    }
                });
            }
        };

        // Attempt to register Android device with Bluemix push instance.
        push.registerDevice(new MFPPushResponseListener<String>() {
            // Response listener handles success and fail callback from Bluemix
            @Override
            public void onSuccess(String response) {
                android.util.Log.i(TAG, "Successfully registered for push notifications");
                push.listen(notificationListener);
            }

            @Override
            public void onFailure (MFPPushException ex) {

                String errLog = "Error registering for push notifications: ";
                String errMessage = ex.getErrorMessage();
                int statusCode = ex.getStatusCode();

                // Create error log based on response code and error message
                if (statusCode == 401){
                    errLog += "Cannot authenticate successfully with Bluemix Push instance, ensure your CLIENT SECRET is correct.";
                } else if(statusCode == 404 && errMessage.contains("Push GCM Configuration")){
                    errLog += "Push GCM Configuration does not exist, ensure you have configured GCM Push credentials on your Bluemix Push dashboard correctly.";
                } else if(statusCode == 404){
                    errLog += "Cannot find Bluemix Push instance, ensure your APPLICATION ID is correct";
                } else if(statusCode >= 500){
                    errLog += "Bluemix and/or your Push instance seem to be having problems, please try again later.";
                } else if (statusCode == 0){
                    errLog += "Request to Bluemix push instance timed out, ensure your device is connected to the internet.";
                }

                android.util.Log.e(TAG, errLog);

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
    public void onResume() {
        super.onResume();
        // Enable the Push sdk to listen for Push notifications using the predefined notification listener
        if (push != null) {
            android.util.Log.i(TAG, "Listening for notifications");
            push.listen(notificationListener);
        }

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

        //Sends the feedback to the Feedback Manager application, which will use watson to  process, translate, analyze and display all feedback in a dashboard
        public void callAPI(String feedback) throws Exception {
            String feedbackAPIURL = Uri.parse(FeedbackApplicationRoute).buildUpon().appendPath("submitFeedback").toString();
            Log.d(TAG, "Submitting feedback to " + feedbackAPIURL);
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
