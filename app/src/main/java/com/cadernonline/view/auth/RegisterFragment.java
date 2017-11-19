package com.cadernonline.view.auth;

import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cadernonline.R;
import com.cadernonline.event.FacebookLoginEvent;
import com.cadernonline.event.RegisterEvent;
import com.cadernonline.view.BaseFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterFragment extends BaseFragment {

    @BindView(R.id.name)
    AppCompatEditText vName;
    @BindView(R.id.email)
    AppCompatEditText vEmail;
    @BindView(R.id.password)
    AppCompatEditText vPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vRoot = inflater.inflate(R.layout.fragment_register, container, false);
        ButterKnife.bind(this, vRoot);
        return vRoot;
    }

    @OnClick(R.id.register)
    public void register(){
        String name = vName.getText().toString();
        String email = vEmail.getText().toString();
        String password = vPassword.getText().toString();
        EventBus.getDefault().post(new RegisterEvent(name, email, password));
    }

    @OnClick(R.id.register_facebook)
    public void facebookRegister(){
        EventBus.getDefault().post(new FacebookLoginEvent());
    }

}