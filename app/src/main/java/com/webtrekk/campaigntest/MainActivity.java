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

package com.webtrekk.campaigntest;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.ArraySet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.webtrekk.webtrekksdk.Webtrekk;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private EditText mApplicationPackage;
    private EditText mTrackingID;
    private EditText mMediaCodeParameter;
    private EditText mMediaCodeValue;
    private Button mTestButton;
    private View mTrackIdExclaimer;
    private boolean mIsShouldStarted;

    private static final String APPLICATION_PACKAGE_SETTING = "APPLICATION_PACKAGE_SETTING";
    private static final String TRACKING_ID_SETTING = "TRACKING_ID_SETTING";
    private static final String MEDIA_PARAMETER_SETTING = "MEDIA_PARAMETER_SETTING";
    private static final String MEDIA_CODE_VALUE = "MEDIA_CODE_VALUE";

    private static final String APPLICATION_PACKAGE_SETTING_LIST = "APPLICATION_PACKAGE_SETTING_LIST";
    private static final String TRACKING_ID_SETTING_LIST = "TRACKING_ID_SETTING_LIST";
    private static final String MEDIA_PARAMETER_SETTING_LIST = "MEDIA_PARAMETER_SETTING_LIST";
    private static final String MEDIA_CODE_VALUE_LIST = "MEDIA_CODE_VALUE_LIST";
    private static final String LIST_SEPARATOR = "&";

    private static final int mEditsIDs[] = {R.id.application_id, R.id.tracking_id, R.id.media_code_par, R.id.media_code_val};
    private static final String mSettingKeys[] = {APPLICATION_PACKAGE_SETTING, TRACKING_ID_SETTING, MEDIA_PARAMETER_SETTING, MEDIA_CODE_VALUE};
    private static final String mSettingAutoListKeys[] = {APPLICATION_PACKAGE_SETTING_LIST, TRACKING_ID_SETTING_LIST, MEDIA_PARAMETER_SETTING_LIST, MEDIA_CODE_VALUE_LIST};

    private static final int ITEMS_COUNT = 4;

    private static final String ALLERT_DIALOG_TAG_NAME = "ALLERT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Webtrekk.getInstance().initWebtrekk(getApplication());

        mApplicationPackage = (EditText)findViewById(R.id.application_id);
        mTrackingID = (EditText)findViewById(R.id.tracking_id);
        mMediaCodeParameter = (EditText)findViewById(R.id.media_code_par);
        mMediaCodeValue = (EditText)findViewById(R.id.media_code_val);
        mTestButton = (Button)findViewById(R.id.test_button);
        mTrackIdExclaimer = findViewById(R.id.track_id_exclaimer);

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
                if (validateTrackingID() && isTrackIDErrorEnable()){
                    enableTrackIDError(false, true);
                }
            }
        };


        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !validateTrackingID() && v == mTrackingID)
                    enableTrackIDError(true, false);
                else if (hasFocus &&  (v != mTrackingID || (v == mTrackingID && validateTrackingID())))
                    v.getBackground().setColorFilter(getWTColor(R.color.wt_grey_line), PorterDuff.Mode.SRC_IN);

                // do action tracking here
            }
        };

        mApplicationPackage.addTextChangedListener(textWatcher);
        mTrackingID.addTextChangedListener(textWatcher);
        mMediaCodeParameter.addTextChangedListener(textWatcher);
        mMediaCodeValue.addTextChangedListener(textWatcher);

        mApplicationPackage.setOnFocusChangeListener(focusChangeListener);
        mTrackingID.setOnFocusChangeListener(focusChangeListener);
        mMediaCodeParameter.setOnFocusChangeListener(focusChangeListener);
        mMediaCodeValue.setOnFocusChangeListener(focusChangeListener);

        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTest();
            }
        });

        validateInput();
        enableTrackIDError(!validateTrackingID() && mTrackingID.getText().length() > 0, false);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(null);
        applyAdapters();

        View helpMenu = myToolbar.findViewById(R.id.help_menu_button);

        helpMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            }
        });


        mMediaCodeValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && validateInput()){
                    runTest();
                    return true;
                } else
                    return false;
            }
        });
    }

    private boolean validateInput(){
        boolean returnValue = mApplicationPackage.getText().length() > 0 && mMediaCodeParameter.getText().length() > 0 &&
                mMediaCodeValue.getText().length() > 0 && validateTrackingID();

        mTestButton.setEnabled(returnValue);

        return returnValue;
    }

    private boolean validateTrackingID(){
        return mTrackingID.getText().length() == 15;
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

                                         if (!isProcessed)
                                            processError(error);

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

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        hideAllertDialog();

        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = AlertDialogFragment.newInstance(error);
        newFragment.show(ft, ALLERT_DIALOG_TAG_NAME);

        Webtrekk.getInstance().trackException("Error Message", error.length() > 255 ? error.substring(0, 254) : error);
    }

    private void hideAllertDialog(){
        DialogFragment prev = (DialogFragment)getFragmentManager().findFragmentByTag(ALLERT_DIALOG_TAG_NAME);

        if (prev != null) {
            prev.dismiss();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        mIsShouldStarted = false;
        findViewById(R.id.help_menu_button).setVisibility(View.VISIBLE);
        findViewById(R.id.back_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStop(){
        super.onStop();

        saveToSetting();
        if (mIsShouldStarted)
            hideAllertDialog();
        mIsShouldStarted = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void readFromSetting(){

        SharedPreferences pref = getPreferences(MODE_PRIVATE);

        for (int i = 0; i < ITEMS_COUNT; i++) {
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

        for (int i = 0; i < ITEMS_COUNT; i++) {
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
                saveToAutoList();
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

            // do action tracking here
    }

    private void saveToAutoList(){

        for (int i = 0; i < ITEMS_COUNT; i++){
            saveItemToAutoList(mEditsIDs[i], mSettingAutoListKeys[i]);
        }
    }

    private void saveItemToAutoList(int resID, String settingKey){
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = pref.edit();

        AutoCompleteTextView editText = (AutoCompleteTextView)findViewById(resID);

        if (editText == null){
            Log.e(getLocalClassName(), "can't modify auto list");
            return;
        }

        LinkedList<String> originList = getRecommendationList(settingKey, pref);

        if (originList == null){
            originList = new LinkedList<String>();
        }

        String strToAdd = editText.getText().toString();

        if (strToAdd != null & !strToAdd.isEmpty() && !originList.contains(strToAdd)){
            originList.push(strToAdd);
            prefEdit.putString(settingKey, TextUtils.join(LIST_SEPARATOR, originList)).apply();

            ArrayAdapter<String> adapter = (ArrayAdapter<String>)editText.getAdapter();
            adapter.insert(strToAdd, adapter.getCount());
        }

    }

    private List<String> loadAutoList(int index){

        LinkedList<String> list = getRecommendationList(mSettingAutoListKeys[index], null);

        return list == null ? null : list;
    }

    private void applyAdapters(){
        for (int i = 0; i < ITEMS_COUNT; i++){
            AutoCompleteTextView autoComplete = (AutoCompleteTextView)findViewById(mEditsIDs[i]);

            if (autoComplete == null)
                continue;

            List<String> suggestions = loadAutoList(i);

            if (suggestions == null)
                suggestions = new ArrayList<String>();

            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>(suggestions));

            autoComplete.setAdapter(adapter);
        }
    }

    private LinkedList<String> getRecommendationList(String key, SharedPreferences pref){

        SharedPreferences prefLocal = pref == null ? getPreferences(MODE_PRIVATE) : pref;
        String str = prefLocal.getString(key, null);

        if (str == null || str.isEmpty()){
            return null;
        }else {
            return new LinkedList<String>(Arrays.asList(str.split(LIST_SEPARATOR)));
        }
    }

    private void enableTrackIDError(boolean enable, boolean isFocus){
        mTrackIdExclaimer.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);

        if (enable){
            mTrackingID.getBackground().setColorFilter(getWTColor(R.color.wt_red), PorterDuff.Mode.SRC_IN);
        } else{
            if (isFocus)
                mTrackingID.getBackground().setColorFilter(getWTColor(R.color.wt_grey_line), PorterDuff.Mode.SRC_IN);
            else
                mTrackingID.getBackground().clearColorFilter();
        }
    }

    private int getWTColor(int resID){
        return getResources().getColor(resID);
    }

    private boolean isTrackIDErrorEnable(){
        return mTrackIdExclaimer.getVisibility() == View.VISIBLE;
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

                if (parValue.length == 2) {
                    mMap.put(parValue[0], parValue[1]);
                }
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
