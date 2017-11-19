package com.cadernonline.view.discipline;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cadernonline.R;
import com.cadernonline.model.Annotation;
import com.cadernonline.util.DateUtil;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnnotationAdapterItem extends AbstractItem<AnnotationAdapterItem, AnnotationAdapterItem.ViewHolder> {
    public final Annotation annotation;

    public AnnotationAdapterItem(Annotation annotation){
        this.annotation = annotation;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.annotation;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.list_item_annotation;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.subject.setText(annotation.getSubject());
        holder.date.setText(DateUtil.getPrettyDate(annotation.getCreatedAt()));
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.subject.setText(null);
        holder.date.setText(null);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.subject)
        AppCompatTextView subject;
        @BindView(R.id.date)
        AppCompatTextView date;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}