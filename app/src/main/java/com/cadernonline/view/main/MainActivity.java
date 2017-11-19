package com.cadernonline.view.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.cadernonline.R;
import com.cadernonline.event.AfterLoginEvent;
import com.cadernonline.event.UpdateCoursesEvent;
import com.cadernonline.model.Course;
import com.cadernonline.util.AuthUtil;
import com.cadernonline.util.DbUtil;
import com.cadernonline.view.BaseActivity;
import com.cadernonline.view.auth.AuthActivity;
import com.github.clans.fab.FloatingActionMenu;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    private static final int REQUEST_PERMISSIONS = 0;

    @BindView(R.id.toolbar)
    Toolbar vToolbar;
    @BindView(R.id.tabs)
    TabLayout vTabLayout;
    @BindView(R.id.refresh)
    SwipeRefreshLayout vRefresh;
    @BindView(R.id.container)
    ViewPager vPager;
    @BindView(R.id.menu)
    FloatingActionMenu vMenu;

    private List<Course> courses;
    private PagerAdapter adapter;
    private MenuItem authMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(vToolbar);

        courses = new ArrayList<>();
        adapter = new PagerAdapter(getSupportFragmentManager());
        vPager.setOffscreenPageLimit(10);
        vPager.setOnTouchListener((v, event) -> {
            vRefresh.setEnabled(false);
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    vRefresh.setEnabled(true);
                    break;
            }
            return false;
        });
        vTabLayout.setupWithViewPager(vPager);
        vRefresh.setOnRefreshListener(this::updateCourses);

        updateCourses();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQUEST_PERMISSIONS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.RECORD_AUDIO }, REQUEST_PERMISSIONS);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        authMenuItem = menu.findItem(R.id.action_auth);
        updateUI();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_auth:
                if(AuthUtil.isLoggedIn()){
                    logOut();
                } else {
                    startActivity(new Intent(this, AuthActivity.class));
                }
                break;
        }
        return true;
    }

    @Subscribe(sticky = true)
    public void onAfterLoginEvent(AfterLoginEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        updateUI();
        updateCourses();
    }

    @Subscribe(sticky = true)
    public void onUpdateCoursesEvent(UpdateCoursesEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        updateCourses();
    }

    @OnClick(R.id.add_course)
    public void addCourse(){
        vMenu.close(true);
        startActivity(new Intent(this, EditCourseActivity.class));
    }

    @OnClick(R.id.add_discipline)
    public void addDiscipline(){
        vMenu.close(true);
        EditDisciplineActivity.start(this, null);
    }

    private void logOut(){
        ParseUser.logOutInBackground(e -> {
            updateUI();
            courses.clear();
            adapter.notifyDataSetChanged();
            vPager.setAdapter(null);
        });
    }

    private void updateCourses(){
        vRefresh.setRefreshing(true);
        DbUtil.getCourses((objects, e) -> {
            if(e == null){
                courses.clear();
                courses.addAll(objects);
                adapter.notifyDataSetChanged();
                vPager.setAdapter(adapter);
            } else {
                showError(e.getMessage());
            }
            vRefresh.setRefreshing(false);
        });
    }

    private void updateUI(){
        if(vMenu != null && authMenuItem != null){
            if(AuthUtil.isLoggedIn()){
                vMenu.setVisibility(View.VISIBLE);
                authMenuItem.setTitle(R.string.logout);
            } else {
                vMenu.setVisibility(View.GONE);
                authMenuItem.setTitle(R.string.login);
            }
        }
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return CourseFragment.newInstance(courses.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return courses.get(position).getName();
        }

        @Override
        public int getCount() {
            return courses.size();
        }
    }

}