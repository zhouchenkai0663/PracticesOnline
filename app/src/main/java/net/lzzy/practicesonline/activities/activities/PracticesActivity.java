package net.lzzy.practicesonline.activities.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.fragments.PracticesFragment;
import net.lzzy.practicesonline.activities.utils.ViewUtils;

/**
 * Created by lzzy_gxy on 2019/4/16.
 * Description:
 */
public class PracticesActivity extends BaseActivity implements PracticesFragment.OnPracticesSelectedListener {

    public static final String PRACTICES_ID = "practicesId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchView search = findViewById(R.id.practices_sv_search);
        search.setQueryHint("关键字搜索");
        search.setOnQueryTextListener(new ViewUtils.AbstractQueryHandler() {

            @Override
            public void handleQuery(String kw) {
                ((PracticesFragment)getFragment()).search(kw);
            }
        });
        SearchView.SearchAutoComplete auto = search.findViewById(R.id.search_src_text);
        auto.setHighlightColor(Color.WHITE);
        auto.setTextColor(Color.WHITE);
        ImageView icon = search.findViewById(R.id.search_button);
        ImageView icX = search.findViewById(R.id.search_close_btn);
        ImageView icG = search.findViewById(R.id.search_go_btn);
        icX.setColorFilter(Color.WHITE);
        icon.setColorFilter(Color.WHITE);
        icG.setColorFilter(Color.WHITE);
    }

    /**
     * 托管activity的布局
     *
     * @return activity的布局
     */
    @Override
    protected int getLayoutRse() {
        return R.layout.activity_practices;
    }

    /**
     * fragment容器的ID
     *
     * @return 容器的ID
     */
    @Override
    protected int getContainerId() {
        return R.id.activity_practices_container;
    }

    /**
     * 生成Fragment对象
     *
     * @return Fragment对象
     */
    @Override
    protected Fragment createFragment() {
        return new PracticesFragment();
    }

    @Override
    public void onPracticesSelected(String practicesId) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(PRACTICES_ID, practicesId);
        startActivity(intent);
    }
}
