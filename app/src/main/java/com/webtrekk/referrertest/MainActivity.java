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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private EditText mApplicationPackage;
    private EditText mTrackingID;
    private EditText mMediaCodeParameter;
    private EditText mMediaCodeValue;
    private Button mTestButton;
    private boolean mIsShouldStarted;
    AlertDialog mAlertDialog;

    private static final String APPLICATION_PACKAGE_SETTING = "APPLICATION_PACKAGE_SETTING";
    private static final String TRACKING_ID_SETTING = "TRACKING_ID_SETTING";
    private static final String MEDIA_PARAMETER_SETTING = "MEDIA_PARAMETER_SETTING";
    private static final String MEDIA_CODE_VALUE = "MEDIA_CODE_VALUE";

    private static final int mEditsIDs[] = {R.id.application_id, R.id.tracking_id, R.id.media_code_par, R.id.media_code_val};
    private static final String mSettingKeys[] = {APPLICATION_PACKAGE_SETTING, TRACKING_ID_SETTING, MEDIA_PARAMETER_SETTING, MEDIA_CODE_VALUE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApplicationPackage = (EditText)findViewById(R.id.application_id);
        mTrackingID = (EditText)findViewById(R.id.tracking_id);
        mMediaCodeParameter = (EditText)findViewById(R.id.media_code_par);
        mMediaCodeValue = (EditText)findViewById(R.id.media_code_val);
        mTestButton = (Button)findViewById(R.id.test_button);

        readFromSetting();

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

        validateInput();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(null);
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
                                                 error = getString(R.string.error_incorrect_referer, referrerValue);
                                             }
                                         } else{
                                             error = getString(R.string.error_lack_of_referer, url);
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

        if (mAlertDialog == null)
            mAlertDialog = new AlertDialog.Builder(MainActivity.this).create();

        if (mAlertDialog.isShowing())
            mAlertDialog.dismiss();

        mAlertDialog.setTitle("Error");
        mAlertDialog.setMessage(error);
        mAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        mAlertDialog.show();
    }

    @Override
    public void onStart(){
        super.onStart();
        mIsShouldStarted = false;
    }

    @Override
    public void onStop(){
        super.onStop();

        saveToSetting();
        if (mIsShouldStarted && mAlertDialog != null && mAlertDialog.isShowing())
            mAlertDialog.dismiss();
        mIsShouldStarted = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            startActivity(new Intent(MainActivity.this, HelpActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void readFromSetting(){

        SharedPreferences pref = getPreferences(MODE_PRIVATE);

        for (int i = 0; i < mEditsIDs.length; i++) {
            EditText editCtrl = (EditText)findViewById(mEditsIDs[i]);
            if (editCtrl != null){
                String text = pref.getString(mSettingKeys[i], null);
                if (text != null && !text.isEmpty()){
                    editCtrl.setText(text);
                }
            }
        }
    }

    private void saveToSetting()
    {
        SharedPreferences.Editor prefEdit = getPreferences(MODE_PRIVATE).edit();

        for (int i = 0; i < mEditsIDs.length; i++) {
            EditText editCtrl = (EditText)findViewById(mEditsIDs[i]);
            if (editCtrl != null){
                String text = editCtrl.getText().toString();
                if (text != null){
                    prefEdit.putString(mSettingKeys[i], text);
                } else {
                    prefEdit.remove(mSettingKeys[i]);
                }
            }
        }
        prefEdit.apply();
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

            //launch this app
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                startActivity(launchIntent);
                mIsShouldStarted = true;

                // show error message if nothing happened
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsShouldStarted){
                            processError(getString(R.string.error_no_launch));
                        }
                    }
                }, 2000);
            } else {
                isInstalled = false;
            }

        }
            if (!isInstalled){
                processError(getString(R.string.error_incorrect_package, packageName));
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
