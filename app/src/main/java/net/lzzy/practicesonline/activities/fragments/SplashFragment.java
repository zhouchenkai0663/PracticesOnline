package net.lzzy.practicesonline.activities.fragments;

import android.content.Context;
import android.view.View;

import net.lzzy.practicesonline.R;

import java.util.Calendar;

/**
 * Created by lzzy_gxy on 2019/4/10.
 * Description:
 */
public class SplashFragment extends BaseFragment {
    private OnSplashFinishedListener listener;

    private int[] imgs = new int[]{R.drawable.splash1, R.drawable.splash2, R.drawable.splash3};


    @Override
    protected void populate() {
        View wall = find(R.id.fragment_splash_wall);
        int pos = Calendar.getInstance().get(Calendar.SECOND) % 3;
        wall.setBackgroundResource(imgs[pos]);
        wall.setOnClickListener(v -> listener.cancelCount());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_splash;
    }

    @Override
    public void search(String kw) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnSplashFinishedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "必须实现OnSplashFinishedListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnSplashFinishedListener {
        void cancelCount();
    }
}
