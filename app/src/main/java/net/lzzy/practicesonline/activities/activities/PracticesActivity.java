package net.lzzy.practicesonline.activities.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.fragments.PracticesFragment;
import net.lzzy.practicesonline.activities.models.PracticeFactory;

import net.lzzy.practicesonline.activities.network.DetectWebService;
import net.lzzy.practicesonline.activities.utils.AppUtils;
import net.lzzy.practicesonline.activities.utils.ViewUtils;

/**
 * Created by lzzy_gxy on 2019/4/16.
 * Description:
 */
public class PracticesActivity extends BaseActivity implements PracticesFragment.OnPracticesSelectedListener {

    public static final String PRACTICES_ID = "practicesId";
    public static final String API_ID = "apiId";
    public static final String EXTRA_LOCAL_COUNT = "localCount";
    private ServiceConnection connection;
    private boolean refresh=false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        if (getIntent()!=null){

            refresh=getIntent().getBooleanExtra(DetectWebService.EXTRA_REFRESH,false);
        }

        /**④Activity中创建ServiceConnection**/
        connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DetectWebService.DetectWebBinder binder= (DetectWebService.DetectWebBinder) service;
                binder.detect();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        //读取本地数据，传到DetectWebService,进行对比
        int localCount = PracticeFactory.getInstance().get().size();
        Intent intent=new Intent(this,DetectWebService.class);
        intent.putExtra(EXTRA_LOCAL_COUNT,localCount);

        /**⑤Activity中启动Service(bindService/startService)**/
        bindService(intent,connection,BIND_AUTO_CREATE);

    }

    /** 跳转后自动下拉刷新更新联系 **/
    @Override
    protected void onResume() {
        super.onResume();
        if (refresh){
            ((PracticesFragment)getFragment()).startRefresh();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("退出应用吗？")
                .setPositiveButton("退出",(dialog, which) -> AppUtils.exit())
                .show();
    }

    /**⑤销毁时结束Service**/
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }





    private void initView() {
        SearchView search = findViewById(R.id.practices_sv_search);
        search.setQueryHint("关键字搜索");
        search.setOnQueryTextListener(new ViewUtils.AbstractQueryHandler() {

            @Override
            public void handleQuery(String kw) {
                ((PracticesFragment) getFragment()).search(kw);
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
    public void onPracticesSelected(String practicesId, int apiId) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(PRACTICES_ID, practicesId);
        intent.putExtra(API_ID, apiId);
        startActivity(intent);
    }
}
