# Hybrid Banking Demo - Interaction of Customer on Mobile Device with Banking Service Center

This project shows how a customer on a mobile device like an Android phone could interact with support staff in a banking service center. The customer is able to check the account status and to submit a feedback message to the customer support via a Customer Loyality App (see architecture below). The bank's staff sees the incoming messages with enhanced context data in an internal Feedback Manager application. If the message was submitted in a language other than English, the message is translated and presented together with the original notice. The service personnel can respond to the customer and issue reward points. The response is directly pushed back to the customer's mobile device.

The Customer Loyality App connects to backend services hosted on [Bluemix Public](http://www.bluemix.net). The internal Feedback Manager could reside on a Bluemix Dedicated or Bluemix Local. Both application use integration services like the Secure Gateway or API Management to securely access resources in the bank's data center.

Watch a walkthrough of an earlier version of this demo by following this link:
https://www.youtube.com/watch?v=7OLkIGoBpi0

![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/Architecture.png)

## Overview
The Hybrid Banking Demo implements the core features of the scenario described above. It showcases some Watson APIs and demonstrates mobile integration capabilities. Right now the data is mock and is stored in a server-side file. In a real hybrid environemnt, the data would reside in an on-premise database system, the Feedback Manager application would run in Bluemix Local, and services like the Secure Gateway and API Management would be used to handle the secure integration of enterprise data. This is highlighted in the video above. The installation of this demo requires several steps which are described below, followed by instructions on how to use the Hybrid Banking Demo.


## Installation
As described above the project consists of a mobile application for an Android device ("Android Application") and the Web application used by the support stuff ("Feedback Manager"). Hence, the installation of this demo requires setting up both applications, provisioning and configurating the required services in Bluemix, and finally connecting the two applications together with the proper credentials. In the following, we are going to start with seting up the Android Application.

### A) Android Application Setup
In this portion of the demo installation we are going to set up the Android application, the app the banking customer will use to review loyalty points and submit feedback.

1. Download https://github.com/IBM-Bluemix/HybridBanking-Android/archive/master.zip
2. Extract the zip file into directory in your file system.
3. Launch Android Studio. If you haven't installed it yet, go [here to learn more about Android Studio](http://developer.android.com/develop/index.html).
4. In the Android Studio starter screen choose "Open an existing Android Studio project". This will open a dialog to select the location of the existing project.
5. Navigate to the directory where you extracted the zip file and choose that directory. Android Studio will now load and organize the project.
6. Give it a minute or two for the import of the project to finish. If you see **Install build tools and sync project**, click it.

Leave the project in Android Studio open. We are going to come back to it in a later step when we are adapting URLs and service credentials.

### B) Bluemix Mobile Backend

Next on our list is to create the Mobile Backend application on Bluemix. The backend handles sending push notifications to the Android application. 

1. Log into [Bluemix](http://www.bluemix.net) and go to the Dashboard.
2. At the top are tiles for Cloud Foundry Apps, Containers, Services, and more. In the tile for Cloud Foundry Apps click on **CREATE APP** to create a new application.
3. In the upcoming dialog choose **MOBILE**.
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/MobileApp.png)
4. Now type in a name for the application and then click **FINISH**. The app and its services are now provisioned.
5. Either by waiting few moments or anytime later by clicking on **Dashboard** and selecting your app, an overview of your app and services can be brought up. Click on **IBM Push Notifications** as shown.
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/MobileAppOverview.png)
6. In the new screen just click **Setup Push**. This brings up a form with several fields. Here we have to configure the Google Cloud Messaging service.
7. To obtain the needed credentials for the Google Cloud Messaging service, we need to open the following URL in another browser tab or window: https://console.developers.google.com/ It is the [Google Developers Console](https://console.developers.google.com/)
8. In the console on the upper right click on a shown existing project (if available) and select **Create a project**
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/GoogleDevelopersConsole.png)
9. In the dialog type in a name for the project and then click **Create**.
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/GoogleNewProject.png)
10. Thereafter you should see the Developers Console again with some tiles shown. On the upper left is a tile with your project name and the assigned project number shown. Copy that **Project Number** and paste this under **Sender ID** into your Bluemix **Push Dashboard** form in the other tab.
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/GoogleProject.png)
11. Go back to the Google Developers Console and click on the tile **Use Google APIs**. An overview of available APIs opens and you will find a set of **Mobile APIs**.
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/GoogleMobileAPIs.png)
12. Now select on **Cloud Messaging for Android** and in the new dialog click on **Enable API**. A new message should appear with the recommendation to create credentials. Go with it.
13. In the new dialog click on **Add Credentials** and choose **API Key**. 
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/GoogleAddCredentials.png)
14. We are almost done by picking **Server Key**. This brings up another form. Here the name for the new Server API Key needs to be specified. You can choose the name. Leave the optional field for the IP addresses empty and click **Create**.
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/GoogleServerAPIKey.png)
15. A notice with the new API key should come up. Copy this key and and paste this as **API Key** into your Bluemix **Push Dashboard** in the other browser tab. Once done click **Save** to finish the configuration.

### C) Connect the Android Application to your Bluemix Mobile backend

1. In the Android Studio click on **Project** in the top left corner and drill down, i.e., navigate the directory and file tree down to the following file and open it in the editor: `BlueBank/app/src/main/java/com/bluemix/bluebank/MainActivity.java`
2. Locate the following lines of code in that file, they are around lines 45 to 50:
 
 ```
 private String BluemixMobileBackendApplication_ROUTE = "http://backendURLxxxxxxx.mybluemix.net";
 private String BluemixMobileBackendApplication_App_GUID= "ef5xxxx-xxxx-xxxx-xxxx-xxxxxxxx";
 ```
3. Replace the value for ROUTE and App_GUID with your own values. Both can be obtained by clicking on **Mobile Options** in your Bluemix Mobile Backend application dashboard as shown.
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/MobileOptions.png)

### D) Feedback Manager application with Watson
Next, you will need to deploy a node.js application. The bank would use this app internally to manage all the feedback ("Feeback Manager"). In a hybrid scenario this application would run on Bluemix Local.

1. The source code for the Feedback Manager is located here: (https://github.com/IBM-Bluemix/HybridBanking-FeedbackManager)
2. If you have **git** installed you could simply clone the repository. If you don't have `git`, click on **Download Zip** and extract the downloaded zip archive to a directory on your computer.
3. Open **server.js** using your favorite editor and update these two lines below. You can find these values by clicking **Show Credentials** under IBM Push Notifications in your Bluemix Mobile Backend dashboard. This will allow this application to talk to the Bluemix mobile push service. Look in the "credentials" block:

  ```
  var IBMPushNotifications_url = "http://imfpush.ng.bluemix.net/imfpush/v1/apps/....";
  var IBMPushNotifications_appSecret  = "48xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
  ```
![alt tag](https://raw.githubusercontent.com/IBM-Bluemix/HybridBanking-Android/master/images/MobilePushCredentials.png)

Push this application to Bluemix. 
```
cd <location of your app>
cf login -a https://api.ng.bluemix.net
cf push <pickAUniqueAppName>
```
Bind the [**Language Translation**](https://console.ng.bluemix.net/catalog/services/language-translation/) and [**Tone Analyzer**](https://console.ng.bluemix.net/catalog/services/tone-analyzer) services (in the Bluemix Labs catalog at the bottom of the regular catalog) to this application. 

### E) Link Android Application to the Feedback Manager app
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
