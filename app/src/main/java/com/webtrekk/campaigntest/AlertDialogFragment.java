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
* Created by vartbaronov on 09.12.16.
*/

package com.webtrekk.campaigntest;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AlertDialogFragment extends DialogFragment {

    private static String TEXT_KEY = "TEXT";

    public static AlertDialogFragment newInstance(String text){
        AlertDialogFragment f = new AlertDialogFragment();

        Bundle args = new Bundle();

        args.putString(TEXT_KEY, text);

        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.allert_dialog, container, false);

        View btOK = v.findViewById(R.id.error_ok_button);
        TextView text = (TextView) v.findViewById(R.id.error_text);

        text.setText(getArguments().getString(TEXT_KEY, "no text"));

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }
}
