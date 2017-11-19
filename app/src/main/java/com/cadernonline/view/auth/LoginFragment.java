package com.cadernonline.view.auth;

import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cadernonline.R;
import com.cadernonline.event.FacebookLoginEvent;
import com.cadernonline.event.LoginEvent;
import com.cadernonline.view.BaseFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginFragment extends BaseFragment {

    @BindView(R.id.email)
    AppCompatEditText vEmail;
    @BindView(R.id.password)
    AppCompatEditText vPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vRoot = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, vRoot);
        return vRoot;
    }

    @OnClick(R.id.login)
    public void login(){
        String email = vEmail.getText().toString();
        String password = vPassword.getText().toString();
        EventBus.getDefault().post(new LoginEvent(email, password));
    }

    @OnClick(R.id.login_facebook)
    public void facebookLogin(){
        EventBus.getDefault().post(new FacebookLoginEvent());
    }

}