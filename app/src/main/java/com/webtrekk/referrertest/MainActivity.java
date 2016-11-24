/*The MIT License (MIT)
*
*Copyright (c) 2016 Webtrekk GmbH
*
*Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the
*"Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish,
*distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject
*to the following conditions:
*
*The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
*
*THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
*MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
*CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
*SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
* Created by vartbaronov on 22.11.16.
*/

package com.webtrekk.referrertest;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {

    private EditText mApplicationPackage;
    private EditText mTrackingID;
    private EditText mMediaCodeParameter;
    private EditText mMediaCodeValue;
    private Button mTestButton;
    private boolean mIsShouldStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApplicationPackage = (EditText)findViewById(R.id.application_id);
        mTrackingID = (EditText)findViewById(R.id.tracking_id);
        mMediaCodeParameter = (EditText)findViewById(R.id.media_code_par);
        mMediaCodeValue = (EditText)findViewById(R.id.media_code_val);
        mTestButton = (Button)findViewById(R.id.test_button);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInput();
            }
        };


        mApplicationPackage.addTextChangedListener(textWatcher);
        mTrackingID.addTextChangedListener(textWatcher);
        mMediaCodeParameter.addTextChangedListener(textWatcher);
        mMediaCodeValue.addTextChangedListener(textWatcher);

        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTest();
            }
        });

        Button helpButton = (Button)findViewById(R.id.help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            }
        });

        validateInput();
    }

    private boolean validateInput(){
        boolean returnValue = mApplicationPackage.getText().length() > 0 && mMediaCodeParameter.getText().length() > 0 &&
                mMediaCodeValue.getText().length() > 0 && mTrackingID.getText().length() == 15;

        mTestButton.setEnabled(returnValue);

        return returnValue;
    }

    private void runTest(){
        final String mediaValue = mMediaCodeParameter.getText() + "%3D" +mMediaCodeValue.getText();
        final String installURL ="http://appinstall.webtrekk.net/appinstall/v1/redirect?mc="+mediaValue+"&trackid="+mTrackingID.getText()+"&as1=market%3A//details%3Fid%3Dcom.Webtrekk.SDKTest";

        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.setVisibility(View.VISIBLE);


        webView.setWebViewClient(new WebViewClient() {

                                     @Override
                                     public boolean shouldOverrideUrlLoading(WebView view, String url) {

                                         //
                                         String error = null;
                                         boolean isProcessed = false;
                                         URLParsel parcel = new URLParsel();

                                         parcel.parseURL(url);

                                         String referrerValue = parcel.getValue("referrer");

                                         if (referrerValue != null) {

                                             String clickID = parcel.getValue("referrer").split("%3D")[1];
                                             if (clickID != null) {
                                                 processClickID(clickID);
                                                 isProcessed = true;
                                             } else {
                                                 error = "Incorrect referrer:"+ referrerValue;
                                             }
                                         } else{
                                             error = "Lack of referrer in url:"+ url;
                                         }

                                         if (!isProcessed){
                                             processError(error);
                                         }
                                         return true;
                                     }
                                 }
        );

        webView.loadUrl(installURL);
    }

    private void processError(String error){
        if (error == null){
            error = "Undefined error";
        }

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(error);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onStart(){
        super.onStart();
        mIsShouldStarted = false;
        findViewById(R.id.final_help_text).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStop(){
        super.onStop();
        mIsShouldStarted = false;
    }

    private void processClickID(String clickID){
        final String referrer = "wt_clickid="+clickID;
        final String key = "referrer";
        final String packageName = mApplicationPackage.getText().toString();

        // check if package installed

        PackageManager pm = getPackageManager();
        boolean isInstalled = false;

        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            isInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            isInstalled = false;
        }

        if (isInstalled) {
            // send referrer intent
            Intent intent = new Intent();
            intent.setAction("com.android.vending.INSTALL_REFERRER");
            intent.putExtra(key, referrer);
            intent.setClassName(packageName, "com.webtrekk.webtrekksdk.ReferrerReceiver");
            sendBroadcast(intent);

            //launc this app

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                startActivity(launchIntent);
                mIsShouldStarted = true;

                // show error message if nothing happened
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsShouldStarted){
                            View view = MainActivity.this.findViewById(R.id.final_help_text);

                            if (view != null){
                                view.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }, 2000);
            } else {
                isInstalled = false;
            }

        }
            if (!isInstalled){
                processError("Package " + packageName +" isn't insalled on this device. Please change package name.");
            }
    }

    private static class URLParsel
    {
        final private Map<String, String> mMap = new HashMap<String, String>();
        public static String URLKEY = "MAIN_URL_KEY";

        public boolean parseURL(String url)
        {
            Pattern pattern = Pattern.compile("([^?&]+)");
            Matcher matcher = pattern.matcher(url);

            matcher.find();
            mMap.put(URLKEY, matcher.group());
            while (matcher.find())
            {
                final String parValue[] = matcher.group().split("=");

                if (parValue.length == 2)
                    mMap.put(parValue[0], parValue[1]);
                else
                {
                    return false;
                }
            }

            return true;
        }

        public String getValue(String key)
        {
            return  mMap.get(key);
        }
    }

}
