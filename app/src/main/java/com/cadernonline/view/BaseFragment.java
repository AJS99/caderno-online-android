package com.cadernonline.view;

import android.support.v4.app.Fragment;
import android.widget.Toast;

public class BaseFragment extends Fragment {

    protected void showError(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}