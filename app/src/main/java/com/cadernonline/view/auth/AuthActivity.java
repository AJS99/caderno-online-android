package com.cadernonline.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.cadernonline.R;
import com.cadernonline.event.AfterLoginEvent;
import com.cadernonline.event.FacebookLoginEvent;
import com.cadernonline.event.LoginEvent;
import com.cadernonline.event.RegisterEvent;
import com.cadernonline.view.BaseActivity;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.Profile;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar vToolbar;
    @BindView(R.id.tabs)
    TabLayout vTabLayout;
    @BindView(R.id.container)
    ViewPager vPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        setSupportActionBar(vToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        vTabLayout.setupWithViewPager(vPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                break;
        }
        return true;
    }

    @Subscribe
    public void onLoginEvent(LoginEvent event) {
        ParseUser.logInInBackground(event.email, event.password, (user, e) -> {
            if(e == null) {
                EventBus.getDefault().postSticky(new AfterLoginEvent());
                finish();
            } else {
                showError(e.getMessage());
            }
        });
    }

    @Subscribe
    public void onFacebookLoginEvent(FacebookLoginEvent event) {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, Collections.singleton("email"), (user, e) -> {
            if(e == null) {
                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), (object, response) -> {
                    try {
                        updateFacebookInfo(object);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
                Bundle permissions = new Bundle();
                permissions.putString("fields", "id,email");
                request.setParameters(permissions);
                request.executeAsync();
            } else {
                showError(e.getMessage());
            }
        });
    }

    @Subscribe
    public void onRegisterEvent(final RegisterEvent event) {
        ParseUser user = new ParseUser();
        user.setEmail(event.email);
        user.setUsername(event.email);
        user.setPassword(event.password);
        user.put("name", event.name);
        user.signUpInBackground(e -> {
            if(e == null) {
                onLoginEvent(new LoginEvent(event.email, event.password));
            } else {
                showError(e.getMessage());
            }
        });
    }

    private void updateFacebookInfo(JSONObject graphResponse){
        String name = Profile.getCurrentProfile().getName();
        String pictureUrl = Profile.getCurrentProfile().getProfilePictureUri(300, 300).toString();
        String email = null;
        try {
            if(graphResponse != null && graphResponse.has("email")){
                email = graphResponse.getString("email");
            }
        } catch (Exception ignored){ }

        ParseUser user = ParseUser.getCurrentUser();
        if(email != null) {
            user.setEmail(email);
            user.setUsername(email);
        }
        user.put("name", name);
        user.put("profileImageUrl", pictureUrl);
        user.saveInBackground(e -> {
            if(e == null){
                EventBus.getDefault().postSticky(new AfterLoginEvent());
                finish();
            } else {
                showError(e.getMessage());
            }
        });
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return new LoginFragment();
                case 1: return new RegisterFragment();
                default: return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0: return getString(R.string.login);
                case 1: return getString(R.string.register);
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}