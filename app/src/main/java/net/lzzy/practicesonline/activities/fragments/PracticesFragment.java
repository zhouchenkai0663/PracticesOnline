package net.lzzy.practicesonline.activities.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.models.Practice;
import net.lzzy.practicesonline.activities.models.PracticeFactory;

import net.lzzy.practicesonline.activities.models.Question;
import net.lzzy.practicesonline.activities.models.QuestionFactory;
import net.lzzy.practicesonline.activities.models.view.UserCookies;
import net.lzzy.practicesonline.activities.network.PracticeService;
import net.lzzy.practicesonline.activities.network.QuestionService;
import net.lzzy.practicesonline.activities.utils.AbstractStaticHandler;
import net.lzzy.practicesonline.activities.utils.AppUtils;
import net.lzzy.practicesonline.activities.utils.DateTimeUtils;
import net.lzzy.sqllib.GenericAdapter;
import net.lzzy.sqllib.ViewHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lzzy_gxy
 * @date 2019/4/16
 * Description:
 */
public class PracticesFragment extends BaseFragment {

    public static final int WHAT_PRACTICE_DONE = 0;
    public static final int WHAT_EXCEPTION = 1;
    private ListView lv;
    private SwipeRefreshLayout swipe;
    private TextView tvHint;
    private TextView tvTime;
    private List<Practice> practices;
    private GenericAdapter<Practice> adapter;
    private PracticeFactory factory = PracticeFactory.getInstance();
    private ThreadPoolExecutor executor = AppUtils.getExectuor();
    private boolean isDelete;
    private float touchX1;
    private float touchX2;
    private final float MIN_DElETE = 100;
    private OnPracticesSelectedListener listener;

    private DownloadHandler handler = new DownloadHandler(this);

    //自定义的
    private static class DownloadHandler extends AbstractStaticHandler<PracticesFragment> {

        public DownloadHandler(PracticesFragment context) {
            super(context);
        }

        @Override
        public void handleMessage(Message msg, PracticesFragment practicesFragment) {
            switch (msg.what) {
                case WHAT_PRACTICE_DONE:

                    practicesFragment.tvTime.setText(DateTimeUtils.DATE_TIME_FORMAT.format(new Date()));
                    UserCookies.getInstance().updateLastRefereencesTime();
                    try {
                        List<Practice> practices = PracticeService.getPratices(msg.obj.toString());
                        for (Practice practice : practices) {
                            practicesFragment.adapter.add(practice);
                        }
                        Toast.makeText(practicesFragment.getContext(), "同步完成", Toast.LENGTH_SHORT).show();
                        practicesFragment.finishRefresh();
                    } catch (Exception e) {
                        e.printStackTrace();
                        practicesFragment.handlerPracticeException(e.getMessage());
                    }
                    break;
                case WHAT_EXCEPTION:
                    practicesFragment.handlerPracticeException(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    }

    //使用封装的AsyncTask
    static class PracticeDownloader extends AsyncTask<Void, Void, String> {
        WeakReference<PracticesFragment> fragment;

        PracticeDownloader(PracticesFragment fragment) {
            this.fragment = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PracticesFragment fragment = this.fragment.get();
            fragment.tvTime.setVisibility(View.GONE);
            fragment.tvHint.setVisibility(View.GONE);
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param voids The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return PracticeService.getPracticeFromServer();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            PracticesFragment fragment = this.fragment.get();
            fragment.tvTime.setText(DateTimeUtils.DATE_TIME_FORMAT.format(new Date()));
            UserCookies.getInstance().updateLastRefereencesTime();
            try {
                List<Practice> practices = PracticeService.getPratices(s);
                for (Practice practice : practices) {
                    fragment.adapter.add(practice);
                }
                Toast.makeText(fragment.getContext(), "同步完成", Toast.LENGTH_SHORT).show();
                fragment.finishRefresh();
            } catch (Exception e) {
                e.printStackTrace();
                fragment.handlerPracticeException(s);
            }
        }
    }


    //同步失败重试
    private void handlerPracticeException(String message) {
        finishRefresh();
        Snackbar.make(lv, "同步失败\n" + message, Snackbar.LENGTH_LONG)
                .setAction("重试", v -> {
                    swipe.setRefreshing(true);
                    refreshListener.onRefresh();
                }).show();
    }

    private void finishRefresh() {
        swipe.setRefreshing(false);
        tvTime.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);
    }

    @Override
    protected void populate() {
        initViews();
        loadPractices();
        initSwipe();
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener = this::downloadPracticesAsync;

    //自定义的 下载的放法
    private void downloadPractices() {
        tvTime.setVisibility(View.VISIBLE);
        tvHint.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                String json = PracticeService.getPracticeFromServer();
                handler.sendMessage(handler.obtainMessage(WHAT_PRACTICE_DONE, json));
            } catch (IOException e) {
                e.printStackTrace();
                handler.sendMessage(handler.obtainMessage(WHAT_EXCEPTION, e.getMessage()));
            }
        });
    }

    private void downloadPracticesAsync() {
        new PracticeDownloader(this).execute();
    }

    private void initSwipe() {
        swipe.setOnRefreshListener(refreshListener);

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //判断列表是否需要下拉刷新
                boolean isTop = view.getChildCount() == 0 || view.getChildAt(0).getTop() >= 0;
                swipe.setEnabled(isTop);
            }
        });

    }

    private void loadPractices() {
        practices = factory.get();
        //列表排序
        Collections.sort(practices, new Comparator<Practice>() {
            @Override
            public int compare(Practice o1, Practice o2) {
                return o2.getDownloadDate().compareTo(o1.getDownloadDate());
            }
        });
        //列表显示
        adapter = new GenericAdapter<Practice>(getActivity(), R.layout.practices_item, practices) {
            @Override
            public void populate(ViewHolder viewHolder, Practice practice) {
                viewHolder.setTextView(R.id.practices_items_name, practice.getName());
                TextView tvOutlines = viewHolder.getView(R.id.practices_items_btn_outlines);
                if (practice.isDownloaded()) {
                    tvOutlines.setVisibility(View.VISIBLE);
                    tvOutlines.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                            .setMessage(practice.getOutlines())
                            .show());
                } else {
                    tvOutlines.setVisibility(View.GONE);
                }
                Button btnDel = viewHolder.getView(R.id.practices_items_btn_del);
                btnDel.setVisibility(View.GONE);
                //侧滑删除
                btnDel.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                        .setTitle("删除确认")
                        .setMessage("确定删除吗？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", (dialog, which) -> {
                            isDelete = false;
                            adapter.remove(practice);
                        })
                        .show()
                );
                int visible = isDelete ? View.VISIBLE : View.GONE;
                btnDel.setVisibility(visible);
                viewHolder.getConvertView().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        slideToDelete(event, btnDel,practice);
                        return true;
                    }
                });
            }


            @Override
            public boolean persistInsert(Practice practice) {
                return factory.addPracticce(practice);
            }

            @Override
            public boolean persistDelete(Practice practice) {
                return factory.deletePracticeAndRelated(practice);
            }
        };
        lv.setAdapter(adapter);

    }

    //侧滑的XY轴距离
    private void slideToDelete(MotionEvent event, Button button,Practice  practice) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                touchX2 = event.getX();
                if (touchX1 - touchX2 > MIN_DElETE) {
                    if (!isDelete) {
                        button.setVisibility(View.VISIBLE);
                        isDelete = true;
                    }
                } else {
                    if (button.isShown()) {
                        button.setVisibility(View.GONE);
                        isDelete = false;
                    }else {
                        prefromItemClick(practice);
                    }
                }
                break;
            default:
                break;
        }
    }

    //题目
    class QuestionDownloader extends AsyncTask<Void, Void, String> {

        WeakReference<PracticesFragment> fragment;
        Practice practice;
        QuestionDownloader(PracticesFragment fragment, Practice practice) {
            this.fragment = new WeakReference<>(fragment);
            this.practice = (practice);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param voids The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(Void... voids) {
            int apiId = practice.getApiId();
            try {
                return QuestionService.getQuestionsOfPracticeFromServer(apiId);
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                List<Question> questions = QuestionService.getQuestions(s,practice.getId());
                QuestionFactory factory=QuestionFactory.getInstance();
                for (Question question : questions) {
                        factory.insert(question);
                }
                practice.setDownloaded(true);
                Toast.makeText(getContext(), "下载完成", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void prefromItemClick(Practice practice) {
        if (practice.isDownloaded()) {

            //todo 1:跳转
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage("确定下载吗？")
                    .setPositiveButton("确定", (dialog, which) -> downloadQuestions(practice))
                    .setNegativeButton("取消", null).show();

        }
    }

    private void downloadQuestions(Practice practice) {
        new QuestionDownloader(this,practice);
    }

    public interface OnPracticesSelectedListener {
        void onPracticesSelected(String practicesId);
    }

    //初始化视图组件
    private void initViews() {
        lv = find(R.id.fragment_practices_lv);
        TextView tvNone = find(R.id.fragment_practices_tv_none);
        lv.setEmptyView(tvNone);
        swipe = find(R.id.fragment_practices_swipe);
        tvHint = find(R.id.fragment_practices_tv_hint);
        tvTime = find(R.id.fragment_practices_tv_time);
        tvTime.setText(UserCookies.getInstance().getLastRefrshTime());
        tvHint.setVisibility(View.GONE);
        tvTime.setVisibility(View.GONE);
        find(R.id.fragment_practices_lv).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isDelete = false;
                adapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_practices;
    }

    //搜索
    @Override
    public void search(String kw) {
        practices.clear();
        if (TextUtils.isEmpty(kw)) {
            practices.addAll(factory.get());
        } else {
            practices.addAll(factory.searchPractices(kw));
        }
        adapter.notifyDataSetChanged();
    }

}
