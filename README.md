# Hybrid Banking Demo - Interaction of Customer on Mobile Device with Banking Service Center

This project shows how a customer on a mobile device like an Android phone could interact with support staff in a banking service center. The customer is able to check the account status and to submit a feedback message to the customer support via a Customer Loyality App (see architecture below). The bank's staff sees the incoming messages with enhanced context data in an internal Feedback Manager application. If the message was submitted in a language other than English, the message is translated and presented together with the original notice. The service personnel can respond to the customer and issue reward points. The response is directly pushed back to the customer's mobile device.

The Customer Loyality App connects to backend services hosted on [Bluemix Public](http://www.bluemix.net). The internal Feedback Manager could reside on a Bluemix Dedicated or Bluemix Local. Both application use integration services like the Secure Gateway or API Management to securely access resources in the bank's data center.

Watch a walkthrough of an earlier version of this demo by following this link:
https://www.youtube.com/watch?v=7OLkIGoBpi0

![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/Architecture.png)

## Overview
The Hybrid Banking Demo implements the core features of the scenario described above. It showcases some Watson APIs and demonstrates mobile integration capabilities. Right now the data is mock and is stored in a server-side file. In a real hybrid environemnt, the data would reside in an on-premise database system, the Feedback Manager application would run in Bluemix Local, and services like the Secure Gateway and API Management would be used to handle the secure integration of enterprise data. This is highlighted in the video above. The installation of this demo requires several steps which are described below, followed by instructions on how to use the Hybrid Banking Demo.


## Installation
As described above the project consists of a mobile application for an Android device ("Android Application") and the Web application used by the support stuff ("Feedback Manager"). Hence, the installation of this demo requires setting up both applications, provisioning and configurating the required services in Bluemix, and finally connecting the two applications together with the proper credentials. In the following, we are going to start with seting up the Android Application.

### Android Application Setup
This portion will set up the Android application the Banking customer will use to review their loyalty points and submit feedback.

1. Download https://github.com/IBM-Bluemix/HybridBanking-Android/archive/master.zip
2. Extract the zip file into directory in your file system.
3. Launch Android Studio. If you haven't installed it yet, go [here to learn more about Android Studio](http://developer.android.com/develop/index.html).
4. In the Android Studio starter screen choose "Open an existing Android Studio project". This will open a dialog to select the location of the existing project.
5. Navigate to the directory where you extracted the zip file and choose that directory. Android Studio will now load and organize the project.
6. Give it a minute or two for the import of the project to finish. If you see **Install build tools and sync project**, click it.

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
3. Open **server.js** using your favorite editor and update these two lines below. You can find these values by clicking **Show Credentials** under IBM Push Notifications in your Bluemix Mobile Backend dashboard. This will allow this application to talk to the Bluemix mobile push service. Look in the "credentials" block:
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
1. Use the  Android application to submit feedback in English, Spanish, or French.
2. In your browser, visit your Feedback Manager application to see the new feedback and see the Watson analysis.
3. Click on Award, type a message and click Send.
4. The Android emulator should get a push notification! Watch the upper left corner for the notification icon to appear.
