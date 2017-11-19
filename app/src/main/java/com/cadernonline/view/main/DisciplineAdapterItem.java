package com.cadernonline.view.main;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cadernonline.R;
import com.cadernonline.model.Discipline;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DisciplineAdapterItem extends AbstractItem<DisciplineAdapterItem, DisciplineAdapterItem.ViewHolder> {
    public final Discipline discipline;

    public DisciplineAdapterItem(Discipline discipline){
        this.discipline = discipline;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.discipline;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.list_item_discipline;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.name.setText(discipline.getName());
        holder.description.setText(discipline.getDescription());
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.name.setText(null);
        holder.description.setText(null);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        AppCompatTextView name;
        @BindView(R.id.description)
        AppCompatTextView description;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}