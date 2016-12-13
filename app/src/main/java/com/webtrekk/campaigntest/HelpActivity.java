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
* Created by vartbaronov on 24.11.16.
*/

package com.webtrekk.campaigntest;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;


public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);

        findViewById(R.id.main_layout).setVisibility(View.GONE);

        WebView vebView = (WebView)findViewById(R.id.web_view);

        vebView.setVisibility(View.VISIBLE);
        vebView.loadData(getString(R.string.html_help), "text/html", "UTF-8");

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        findViewById(R.id.help_menu_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.back_button).setVisibility(View.VISIBLE);
    }

}
