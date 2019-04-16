package net.lzzy.practicesonline.activities.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import net.lzzy.practicesonline.activities.utils.AppUtils;

/**
 * Created by lzzy_gxy on 2019/4/11.
 * Description:
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutRse());
        AppUtils.addActivity(this);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(getContainerId());
        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction().add(getContainerId(), fragment).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.setRunning(getLocalClassName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppUtils.setStoppped(getLocalClassName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppUtils.removeActivity(this);
    }

    /**
     * 托管activity的布局
     *
     * @return activity的布局
     */
    protected abstract int getLayoutRse();

    /**
     * fragment容器的ID
     *
     * @return 容器的ID
     */
    protected abstract int getContainerId();

    /**
     * 生成Fragment对象
     *
     * @return Fragment对象
     */
    protected abstract Fragment createFragment();

}
