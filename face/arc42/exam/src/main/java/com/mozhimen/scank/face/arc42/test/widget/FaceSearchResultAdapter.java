package com.mozhimen.scank.face.arc42.test.widget;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mozhimen.scank.face.arc42.test.R;
import com.mozhimen.scank.face.arc42.test.ui.model.CompareResult;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * 给显示人脸搜索结果的RecyclerView使用的Adapter
 */
public class FaceSearchResultAdapter extends RecyclerView.Adapter<FaceSearchResultAdapter.CompareResultHolder> {
    private List<CompareResult> compareResultList;
    private LayoutInflater inflater;
    private static final String TAG = "FaceSearchResultAdapter";
    
    public FaceSearchResultAdapter(List<CompareResult> compareResultList, Context context) {
        inflater = LayoutInflater.from(context);
        this.compareResultList = compareResultList;
    }

    @NonNull
    @Override
    public CompareResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.recycler_item_search_result, null, false);
        CompareResultHolder compareResultHolder = new CompareResultHolder(itemView);
        compareResultHolder.textView = itemView.findViewById(R.id.tv_item_name);
        compareResultHolder.imageView = itemView.findViewById(R.id.iv_item_head_img);
        return compareResultHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CompareResultHolder holder, int position) {
        if (compareResultList == null) {
            return;
        }
        Glide.with(holder.imageView)
                .load(compareResultList.get(position).getFaceEntity().getImagePath())
                .into(holder.imageView);
        holder.textView.setText(compareResultList.get(position).getFaceEntity().getUserName());
    }

    @Override
    public int getItemCount() {
        return compareResultList == null ? 0 : compareResultList.size();
    }

    static class CompareResultHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView imageView;

        CompareResultHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
