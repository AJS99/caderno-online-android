package com.cadernonline.view.main;

import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cadernonline.R;
import com.cadernonline.event.UpdateCoursesEvent;
import com.cadernonline.model.Course;
import com.cadernonline.view.BaseActivity;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditCourseActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar vToolbar;
    @BindView(R.id.name)
    AppCompatEditText vName;
    @BindView(R.id.coordinator)
    AppCompatEditText vCoordinator;
    @BindView(R.id.description)
    AppCompatEditText vDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);
        ButterKnife.bind(this);

        setSupportActionBar(vToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                break;
            case R.id.action_save:
                save();
                break;
        }
        return true;
    }

    private void save(){
        String name = vName.getText().toString();
        String coordinator = vCoordinator.getText().toString();
        String description = vDescription.getText().toString();

        Course course = ParseObject.create(Course.class);
        course.setName(name);
        course.setCoordinator(coordinator);
        course.setDescription(description);
        course.setUser(ParseUser.getCurrentUser());
        course.saveInBackground(e -> {
            if(e == null){
                EventBus.getDefault().postSticky(new UpdateCoursesEvent());
                finish();
            } else {
                showError(e.getMessage());
            }
        });
    }

}