package com.juniper.jconference.util;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.juniper.jconference.R;

/**
 * Created by koteswara on 8/11/17.
 */

public class ViewDialog {
    public void showDialog(Activity activity, String msg){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.customdialog);

        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText(msg);

       /* Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();*/

    }
}
