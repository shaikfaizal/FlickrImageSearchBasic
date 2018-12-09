package com.faizal.flickrimagesearch.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.faizal.flickrimagesearch.R;
import com.faizal.flickrimagesearch.common.Common;
import com.faizal.flickrimagesearch.listeners.SearchListener;

public class SearchDialog extends DialogFragment {
    SearchListener searchListener;

    public static SearchDialog newInstance(int title) {
        SearchDialog frag = new SearchDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    public void setSearchlistener(SearchListener searchListener) {
        this.searchListener = searchListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity());
        try {

            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            dialog.setContentView(R.layout.dialog_search);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            final EditText newSearchTxt = (EditText) dialog.findViewById(R.id.ed_new_search);
//         OK
            dialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String search_text = newSearchTxt.getText().toString().trim();
                    if (!search_text.matches(Common.SEARCH_PATTERN)) {
                        newSearchTxt.setError("Enter valid text");
                        return;
                    }
                    searchListener.OnSearchComplete(search_text);
                    dismiss();
                }
            });
//        // Close
            dialog.findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        return dialog;
    }
}