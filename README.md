# Hybrid Banking Demo

https://www.youtube.com/watch?v=7OLkIGoBpi0

This repository helps you set up a demo application to showcase some the capabilities. It is not a sample application. This demo application uses Bluemix mobile push and Watson services, but the data is mock and all pieces runs entirely on Bluemix Public for demo purposes. In a real hybrid environemnt, the data would reside in an on-prem database, the Feedback Manager application would run in Bluemix Local and you can leverage technologies like Secure Gateway and API management to handle secure integration. This is highlighted in the video above.

![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/Architecture.png)

### Android Application Setup
This portion will set up the Android application the Banking customer will use to review their loyalty points and submit feedback.

1. Download https://github.com/IBM-Bluemix/HybridBanking-Android/archive/master.zip
2. Extract it.
3. Launch Android Studio
4. Import existing project -> Point to extracted folder
5. Give it a minute to set everything up. Click **Install build tools and sync project** if you see it.

### Bluemix Mobile Backend

Next, you will need to setup a Bluemix Mobile Backend application which will handle sending push notifications to the Android application. 

1. Log into Bluemix
2. **CREATE APP** -> **Mobile** -> \<PickABackendName\> -> **Finish**
3. Click on **Dashboard** -> select your app -> **IBM Push Notifications** -> **Set up Push**
4. Open ANOTHER tab: https://console.developers.google.com/
5. **Select a Project** -> **Create a new project**
6. Copy the **Project Number** from the top of the page, and Paste this under **Sender ID** in your Bluemix **Push Dashboard** Tab.
7. Go back to the Google Developers Tab and click **APIs & auth -> APIs**
8. **Cloud Messaging for Android -> Enable API**
9. **Credentials -> Add Credentials -> API Key -> Server Key -> Create**. 
10. Copy this key and and Paste this under **API Key** in your Bluemix **Push Dashbaord**

### Connect Android Application to your Bluemix Mobile backend

1. Click on **Project** in the top left corner and drill down to: `BlueBank/app/src/main/java/com/bluemix/bluebank/MainActivity.java`
2. Around Line 50, fill in these two fields with your own values and **Save**
```
private String BluemixMobileBackendApplication_ROUTE = "http://backendURLxxxxxxx.mybluemix.net";
private String BluemixMobileBackendApplication_App_GUID= "ef5xxxx-xxxx-xxxx-xxxx-xxxxxxxx";
```
   You can get both of these by clicking on **Mobile Options** in your Bluemix Mobile Backend application dashboard.

### Feedback Manager application with Watson
Next, you will need to deploy a node.js application which the Bank would use internally to manage all the feedback. In a hybrid scenario, you would run this application in Bluemix Local.

1. Go here: https://github.com/IBM-Bluemix/HybridBanking-FeedbackManager
2. If you have **git** installed, you know what to do. If not, click on **Download Zip** and extract it.
3. Open **server.js** using your favorite editor and update these two lines below. You can find these values by clicking **Show Credentials** under IBM Push Notifications in your Bluemix Mobile Backend dashboard. This will allow this application to talk to the Bluemix mobile push service.
```
var IBMPushNotifications_url = "http://imfpush.ng.bluemix.net/imfpush/v1/apps/....";
var IBMPushNotifications_appSecret  = "48xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
```
Push this application to Bluemix. 
```
cd <location of your app>
cf login -a https://api.ng.bluemix.net
cf push <pickAUniqueAppName>
```
Bind the **Language Translation** and **Tone Analyzer** (in the Bluemix Labs catalog at the bottom of the regular catalog) services to this application. 

### Link Android Application to the Feedback Manager app
The Android application for this demo will submit the feedback directly to the node.js application we just created. In a real world scenario, the app would write to a database leveraging the Secure Gateway service and the Feedback Manager application would read from that database. You could use API Management to expose that database as a Bluemix custom service. To keep the demo simple, the Android application will communicate directly to the Feedback Manager application.

1. Open Android Studio again and drill down to: 
`BlueBank/app/src/main/java/com/bluemix/bluebank/MainActivity.java`
2. Around Line 55, update this variable with the route of the Feedback Manager application you created above.
```    
private String FeedbackApplicationRoute = "http://<FEEDBACKMANAGER>.mybluemix.net/";
```
**Save** and click the **Play** button at the top to launch your app in the emulator!


### Demo
1. Use the  Android application to submit feedback in English or Spanish.
2. In your browser, visit your Feedback Manager application to see the new feedback and see the Watson analysis.
3. Click on Award, type a message and click Send. The Android emulator should get a push notification!
