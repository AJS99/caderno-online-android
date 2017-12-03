package com.cadernonline.view.discipline;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.cadernonline.R;
import com.cadernonline.event.UpdateAnnotationsEvent;
import com.cadernonline.model.Annotation;
import com.cadernonline.model.Course;
import com.cadernonline.model.Discipline;
import com.cadernonline.util.DbUtil;
import com.cadernonline.util.StringUtil;
import com.cadernonline.view.BaseActivity;
import com.cadernonline.view.annotation.AnnotationActivity;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.wordpress.android.util.ActivityUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DisciplineActivity extends BaseActivity {
    private static final String KEY_COURSE = "course";
    private static final String KEY_DISCIPLINE = "discipline";

    @BindView(R.id.toolbar)
    Toolbar vToolbar;
    @BindView(R.id.refresh)
    SwipeRefreshLayout vRefresh;
    @BindView(R.id.list)
    RecyclerView vAnnotations;

    private Course course;
    private Discipline discipline;
    private FastItemAdapter<AnnotationAdapterItem> adapter;

    public static void start(Context context, Course course, Discipline discipline) {
        Intent i = new Intent(context, DisciplineActivity.class);
        i.putExtra(KEY_COURSE, course);
        i.putExtra(KEY_DISCIPLINE, discipline);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discipline);
        ButterKnife.bind(this);

        course = getIntent().getParcelableExtra(KEY_COURSE);
        discipline = getIntent().getParcelableExtra(KEY_DISCIPLINE);

        setSupportActionBar(vToolbar);
        getSupportActionBar().setTitle(discipline.getName());
        getSupportActionBar().setSubtitle(course.getName());
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new FastItemAdapter<>();
        adapter.setHasStableIds(false);
        adapter.withOnClickListener((v, adapter1, item, position) -> {
            AnnotationActivity.start(this, discipline, item.annotation);
            return true;
        });
        adapter.withOnLongClickListener((v, adapter1, item, position) -> {
            showOptionsDialog(item.annotation);
            return true;
        });
        adapter.getItemFilter().withFilterPredicate((item, constraint) -> {
            String query = StringUtil.getSlug(constraint.toString());
            String subject = StringUtil.getSlug(item.annotation.getSubject());
            return subject.startsWith(query);
        });

        vAnnotations.setHasFixedSize(true);
        vAnnotations.setLayoutManager(new LinearLayoutManager(this));
        vAnnotations.setAdapter(adapter);

        vRefresh.setOnRefreshListener(this::updateAnnotations);

        updateAnnotations();
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
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                ActivityUtils.hideKeyboard(DisciplineActivity.this);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
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

    @Subscribe(sticky = true)
    public void onUpdateAnnotationsEvent(UpdateAnnotationsEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        updateAnnotations();
    }

    @OnClick(R.id.add_annotation)
    public void addAnnotation(){
        AnnotationActivity.start(this, discipline, null);
    }

    public void updateAnnotations(){
        vRefresh.setRefreshing(true);
        DbUtil.getAnnotations(discipline, (objects, e) -> {
            if(e == null){
                adapter.clear();
                for(Annotation annotation : objects){
                    adapter.add(new AnnotationAdapterItem(annotation));
                }
                adapter.notifyDataSetChanged();
            } else {
                showError(e.getMessage());
            }
            vRefresh.setRefreshing(false);
        });
    }

    private void editAnnotation(Annotation annotation){
        final EditText vSubject = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_title)
                .setView(vSubject)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String subject = vSubject.getText().toString();
                    if(!TextUtils.isEmpty(subject)) {
                        annotation.setSubject(subject);
                        annotation.saveInBackground();
                        updateAnnotations();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void deleteAnnotation(Annotation annotation){
        DbUtil.delete(annotation);
        updateAnnotations();
    }

    private void showOptionsDialog(Annotation annotation){
        new AlertDialog.Builder(this)
                .setItems(R.array.options, (dialog, which) -> {
                    switch (which){
                        case 0: editAnnotation(annotation); break;
                        case 1: deleteAnnotation(annotation); break;
                    }
                })
                .create()
                .show();
    }
}