package com.cadernonline.view.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.cadernonline.R;
import com.cadernonline.event.UpdateDisciplinesWithNewCourseEvent;
import com.cadernonline.event.UpdateDisciplinesWithOldCourseEvent;
import com.cadernonline.model.Course;
import com.cadernonline.model.Discipline;
import com.cadernonline.util.DbUtil;
import com.cadernonline.view.BaseActivity;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditDisciplineActivity extends BaseActivity {
    private static final String KEY_DISCIPLINE = "discipline";

    @BindView(R.id.toolbar)
    Toolbar vToolbar;
    @BindView(R.id.courses)
    AppCompatSpinner vCourses;
    @BindView(R.id.name)
    AppCompatEditText vName;
    @BindView(R.id.teacher)
    AppCompatEditText vTeacher;
    @BindView(R.id.description)
    AppCompatEditText vDescription;

    private String oldCourseId;
    private Discipline discipline;
    private List<Course> courses;

    public static void start(Context context, Discipline discipline) {
        Intent i = new Intent(context, EditDisciplineActivity.class);
        i.putExtra(KEY_DISCIPLINE, discipline);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_discipline);
        ButterKnife.bind(this);

        setSupportActionBar(vToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        discipline = getIntent().getParcelableExtra(KEY_DISCIPLINE);

        if(discipline == null){
            setTitle(R.string.new_discipline);
            discipline = ParseObject.create(Discipline.class);
        } else {
            setTitle(R.string.edit_discipline);
            vName.setText(discipline.getName());
            vTeacher.setText(discipline.getTeacher());
            vDescription.setText(discipline.getDescription());
            oldCourseId = discipline.getCourse().getObjectId();
        }

        updateCourses();
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
        Course newCourse = getSelectedCourse();
        String name = vName.getText().toString();
        String teacher = vTeacher.getText().toString();
        String description = vDescription.getText().toString();

        discipline.setCourse(newCourse);
        discipline.setName(name);
        discipline.setTeacher(teacher);
        discipline.setDescription(description);
        discipline.setUser(ParseUser.getCurrentUser());
        discipline.saveInBackground(e -> {
            if(e == null){
                if(TextUtils.isEmpty(oldCourseId) || oldCourseId.equals(newCourse.getObjectId())) {
                    EventBus.getDefault().postSticky(new UpdateDisciplinesWithNewCourseEvent(newCourse));
                } else {
                    Course oldCourse = ParseObject.createWithoutData(Course.class, oldCourseId);
                    EventBus.getDefault().postSticky(new UpdateDisciplinesWithOldCourseEvent(oldCourse));
                    EventBus.getDefault().postSticky(new UpdateDisciplinesWithNewCourseEvent(newCourse));
                }
                finish();
            } else {
                showError(e.getMessage());
            }
        });
    }

    private void updateCourses(){
        DbUtil.getCourses((objects, e) -> {
            if(e == null){
                courses = objects;
                List<String> courseNames = new ArrayList<>();
                for(Course course : courses){
                    courseNames.add(course.getName());
                }
                vCourses.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, courseNames));
                if(discipline.getCourse() != null){
                    vCourses.setSelection(getCoursePosition(discipline.getCourse()));
                }
            } else {
                showError(e.getMessage());
            }
        });
    }

    private Course getSelectedCourse(){
        int selectedCourse = vCourses.getSelectedItemPosition();
        return courses.get(selectedCourse);
    }

    private int getCoursePosition(ParseObject course){
        int position = 0;
        for(Course c : courses){
            if(c.getObjectId().equals(course.getObjectId())){
                return position;
            }
            position++;
        }
        return -1;
    }

}