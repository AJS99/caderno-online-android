package com.cadernonline.view.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cadernonline.R;
import com.cadernonline.event.FilterDisciplinesEvent;
import com.cadernonline.event.UpdateDisciplinesWithNewCourseEvent;
import com.cadernonline.event.UpdateDisciplinesWithOldCourseEvent;
import com.cadernonline.model.Course;
import com.cadernonline.model.Discipline;
import com.cadernonline.util.DbUtil;
import com.cadernonline.util.StringUtil;
import com.cadernonline.view.BaseFragment;
import com.cadernonline.view.discipline.DisciplineActivity;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CourseFragment extends BaseFragment {
    private static final String KEY_COURSE = "course";

    @BindView(R.id.list)
    RecyclerView vDisciplines;

    private Course course;
    private FastItemAdapter<DisciplineAdapterItem> adapter;

    public static CourseFragment newInstance(Course course) {
        CourseFragment fragment = new CourseFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_COURSE, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        course = getArguments().getParcelable(KEY_COURSE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vRoot = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, vRoot);

        adapter = new FastItemAdapter<>();
        adapter.setHasStableIds(true);
        adapter.withOnClickListener((v, adapter1, item, position) -> {
            DisciplineActivity.start(getContext(), course, item.discipline);
            return true;
        });
        adapter.withOnLongClickListener((v, adapter1, item, position) -> {
            showOptionsDialog(item.discipline);
            return true;
        });
        adapter.getItemFilter().withFilterPredicate((item, constraint) -> {
            String query = StringUtil.getSlug(constraint.toString());
            String name = StringUtil.getSlug(item.discipline.getName());
            return name.startsWith(query);
        });

        vDisciplines.setHasFixedSize(true);
        vDisciplines.setLayoutManager(new LinearLayoutManager(getContext()));
        vDisciplines.setAdapter(adapter);

        updateDisciplines();
        return vRoot;
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

    @Subscribe(sticky = true)
    public void onUpdateDisciplinesEvent(UpdateDisciplinesWithOldCourseEvent event) {
        if(course != null && course.getObjectId().equals(event.course.getObjectId())) {
            EventBus.getDefault().removeStickyEvent(event);
            updateDisciplines();
        }
    }

    @Subscribe(sticky = true)
    public void onUpdateDisciplinesEvent(UpdateDisciplinesWithNewCourseEvent event) {
        if(course != null && course.getObjectId().equals(event.course.getObjectId())) {
            EventBus.getDefault().removeStickyEvent(event);
            updateDisciplines();
        }
    }

    @Subscribe(sticky = true)
    public void onFilterDisciplinesEvent(FilterDisciplinesEvent event) {
        if(adapter != null) {
            adapter.filter(event.query);
        }
    }

    public void updateDisciplines(){
        DbUtil.getDisciplines(course, (objects, e) -> {
            if(e == null){
                adapter.clear();
                for(Discipline discipline : objects){
                    adapter.add(new DisciplineAdapterItem(discipline));
                }
                adapter.notifyDataSetChanged();
            } else {
                showError(e.getMessage());
            }
        });
    }

    private void editDiscipline(Discipline discipline){
        EditDisciplineActivity.start(getContext(), discipline);
    }

    private void deleteDiscipline(Discipline discipline){
        DbUtil.delete(discipline);
        updateDisciplines();
    }

    private void showOptionsDialog(Discipline discipline){
        new AlertDialog.Builder(getContext())
                .setItems(R.array.options, (dialog, which) -> {
                    switch (which){
                        case 0: editDiscipline(discipline); break;
                        case 1: deleteDiscipline(discipline); break;
                    }
                })
                .create()
                .show();
    }
}