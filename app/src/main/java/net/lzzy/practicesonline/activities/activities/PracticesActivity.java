package net.lzzy.practicesonline.activities.activities;

import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.fragments.PracticesFragment;

/**
 * Created by lzzy_gxy on 2019/4/16.
 * Description:
 */
public class PracticesActivity extends BaseActivity {
    /**
     * 托管activity的布局
     *
     * @return activity的布局
     */
    @Override
    protected int getLayoutRse() {
        return R.layout.activity_ppractices;
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
}
