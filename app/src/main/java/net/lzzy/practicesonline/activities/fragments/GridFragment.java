package net.lzzy.practicesonline.activities.fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.models.view.QuestionResult;
import net.lzzy.sqllib.GenericAdapter;
import net.lzzy.sqllib.ViewHolder;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lzzy_gxy
 * @date 2019/5/13
 * Description:
 */
public class GridFragment extends BaseFragment {
    private GridView gv;
    private TextView tvView;
    public static final String ARGS_RESULT = "result";
    List<QuestionResult> results;
    private GenericAdapter<QuestionResult> adapter;
    private OnGridSkipListener listener;


    /** 静态方法传参数 **/
    public static GridFragment newInstance(List<QuestionResult> results){
        GridFragment fragment=new GridFragment();
        Bundle args=new Bundle();
        args.putParcelableArrayList(ARGS_RESULT, (ArrayList<? extends Parcelable>) results);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null){
            results = getArguments().getParcelableArrayList(ARGS_RESULT);
        }
    }


    @Override
    protected void populate() {
        gv = find(R.id.fragment_grid_gv_circle);
        tvView = find(R.id.fragment_grid_circle_tv_view);
        //region  显示
        adapter = new GenericAdapter<QuestionResult>(getContext(),R.layout.fragment_grid_itme,results) {
            @Override
            public void populate(ViewHolder viewHolder, QuestionResult questionResult) {
                TextView tvLabel=viewHolder.getView(R.id.fragment_grid_tv_circleItem);

                viewHolder.setTextView(R.id.fragment_grid_tv_circleItem,getPosition(questionResult)+1+"");
                if (questionResult.isRight()){
                    tvLabel.setBackgroundResource(R.drawable.grid_circle);
                }else {
                    tvLabel.setBackgroundResource(R.drawable.grid_circle_no);
                }


            }

            @Override
            public boolean persistInsert(QuestionResult questionResult) {
                return false;
            }

            @Override
            public boolean persistDelete(QuestionResult questionResult) {
                return false;
            }

        };
        gv.setAdapter(adapter);
        //endregion

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onGridSkip(adapter.getPosition(results.get(position)));
            }
        });

      //  tvView.setOnClickListener(v -> listener.gotoChart());

    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_grid;
    }

    @Override
    public void search(String kw) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener= (OnGridSkipListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+"必须实现OnGridSkipListener接口");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener=null;
    }

    public interface OnGridSkipListener {
        /**
         * 跳转返回Question视图查看题目
         * @param position
         */
        void onGridSkip(int position);

        /**
         * 跳转到ChartFragment
         */
       // void gotoChart();
    }

}
