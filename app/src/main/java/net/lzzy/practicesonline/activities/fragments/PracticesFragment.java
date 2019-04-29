package net.lzzy.practicesonline.activities.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.models.Practice;
import net.lzzy.practicesonline.activities.models.PracticeFactory;
import net.lzzy.practicesonline.activities.models.Question;
import net.lzzy.practicesonline.activities.models.view.UserCookies;
import net.lzzy.practicesonline.activities.network.DetectWebService;
import net.lzzy.practicesonline.activities.network.PracticeService;
import net.lzzy.practicesonline.activities.network.QuestionService;
import net.lzzy.practicesonline.activities.utils.AbstractStaticHandler;
import net.lzzy.practicesonline.activities.utils.AppUtils;
import net.lzzy.practicesonline.activities.utils.DateTimeUtils;
import net.lzzy.practicesonline.activities.utils.ViewUtils;
import net.lzzy.sqllib.GenericAdapter;
import net.lzzy.sqllib.ViewHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import java.util.Objects;
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
    public static final int WHAT_QUESTION_DONE = 2;
    public static final int WHAT_QUESTION_EXCEPTION = 3;
    private ListView lv;
    private SwipeRefreshLayout swipe;
    private TextView tvHint;
    private TextView tvTime;
    private List<Practice> practices;
    private GenericAdapter<Practice> adapter;
    private PracticeFactory factory = PracticeFactory.getInstance();
    private ThreadPoolExecutor executor = AppUtils.getExectuor();
    private boolean isDelete = false;
    private float touchX1;
    public static final float MIN_DISTANCE = 100;
    private OnPracticesSelectedListener listener;

    //region 线程池处理消息 下载章节and下载题目

    private DownloadHandler handler = new DownloadHandler(this);

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
                case WHAT_QUESTION_DONE:
                    UUID practiceId = practicesFragment.factory.getPracticeId(msg.arg1);
                    practicesFragment.saveQuestion(msg.obj.toString(), practiceId);
                    ViewUtils.dismissPrgress();
                    break;
                case WHAT_QUESTION_EXCEPTION:
                    ViewUtils.dismissPrgress();
                    Toast.makeText(practicesFragment.getContext(), "下载失败请重试\n" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private void saveQuestion(String json, UUID practiceId) {
        try {
            List<Question> questions = QuestionService.getQuestions(json, practiceId);
            factory.saveQuestions(questions, practiceId);
            for (Practice practice : practices) {
                if (practice.getId().equals(practiceId)) {
                    practice.setDownloaded(true);
                }
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "下载失败请重试" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //endregion

    //region AsyncTask执行简单后台线程任务 下载章节

    /**
     * 参数1  执行异步任务需要的参数，如章节id、图书isbn
     * 参数2  进度条数据更新的单位类型  没有则填Void
     * 参数3  异步任务返回数据的类型
     */
    static class PracticeDownLoader extends AsyncTask<Void, Void, String> {

        WeakReference<PracticesFragment> fragment;

        PracticeDownLoader(PracticesFragment fragment) {
            this.fragment = new WeakReference<>(fragment);
        }

        /**
         * 异步任务执行前的初始化及准备工作，如显示进度条
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PracticesFragment fragment = this.fragment.get();
            fragment.tvTime.setVisibility(View.VISIBLE);
            fragment.tvHint.setVisibility(View.VISIBLE);
        }

        /**
         * 该方法就是异步任务要执行的方法
         **/
        @Override
        protected String doInBackground(Void... voids) {
            try {
                return PracticeService.getPracticeFromServer();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        /**
         * 异步任务完成后会返回数据给该方法，在该方法内处理数据刷新UI
         **/
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
                fragment.handlerPracticeException(e.getMessage());
            }
        }
    }
    //endregion

    //region AsyncTask执行简单后台线程任务 下载章节题目

    static class QuestionDownLoader extends AsyncTask<Integer, Void, String> {

        WeakReference<PracticesFragment> fragment;
        Practice practice;

        QuestionDownLoader(PracticesFragment fragment, Practice practice) {
            this.fragment = new WeakReference<>(fragment);
            this.practice = practice;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ViewUtils.showProgress(fragment.get().getContext(), "开始下载题目");
        }

        @Override
        protected String doInBackground(Integer... integers) {

            try {
                return QuestionService.getQuestionsOfPracticeFromServer(practice.getApiId());
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            fragment.get().saveQuestion(s, practice.getId());
            ViewUtils.dismissPrgress();
        }

    }

    //endregion

    /**
     * 抛出异常
     **/
    private void handlerPracticeException(String message) {
        finishRefresh();
        Snackbar.make(lv, "同步失败\n" + message, Snackbar.LENGTH_LONG)
                .setAction("重试", v -> {
                    swipe.setRefreshing(true);
                    refreshListener.onRefresh();
                }).show();
    }

    public void startRefresh() {
        swipe.setRefreshing(true);
        refreshListener.onRefresh();


    }

    private void finishRefresh() {
        swipe.setRefreshing(false);
        tvTime.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);
        NotificationManager manager = (NotificationManager) Objects.requireNonNull(getContext())
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(DetectWebService.NOTIFICATION_DETECT_ID);
        }
    }


    @Override
    protected void populate() {
        initViews();
        loadPractices();
        initSwipe();
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener = this::downloadPracticesAsync;


    /**
     * 线程池处理消息  下载章节
     **/
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

    /**
     * AsyncTask执行简单后台线程任务  下载章节
     **/
    private void downloadPracticesAsync() {
        new PracticeDownLoader(this).execute();
    }

    private void initSwipe() {
        swipe.setOnRefreshListener(refreshListener);

        //region 解决列表视图往下滑动与下拉刷新动作的冲突
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
        //endregion

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

                //region 侧滑删除
                Button btnDel = viewHolder.getView(R.id.practices_items_btn_del);
                btnDel.setVisibility(View.GONE);
                btnDel.setOnClickListener(v -> new android.app.AlertDialog.Builder(getActivity())
                        .setTitle("删除确认")
                        .setMessage("要删除该章节吗？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isDelete = false;
                                adapter.remove(practice);
                            }
                        }).show());

                int visible = isDelete ? View.VISIBLE : View.GONE;
                btnDel.setVisibility(visible);
                viewHolder.getConvertView().setOnTouchListener(new ViewUtils.AbstractTouchHandler() {
                    @Override
                    public boolean handleTouch(MotionEvent event) {
                        slideToDelete(event, btnDel, practice);
                        return true;
                    }
                });

                //endregion

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

    /**
     * 触摸判断
     **/
    private void slideToDelete(MotionEvent event, Button btn, Practice practice) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                float touchX2 = event.getX();
                if (touchX1 - touchX2 > MIN_DISTANCE) {
                    if (!isDelete) {
                        btn.setVisibility(View.VISIBLE);
                        isDelete = true;
                    }
                } else {
                    if (btn.isShown()) {
                        btn.setVisibility(View.GONE);
                        isDelete = false;
                    } else {
                        performItemClick(practice);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void performItemClick(Practice practice) {
        if (practice.isDownloaded() && listener != null) {
            listener.onPracticesSelected(practice.getId().toString(), practice.getApiId());
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage("下载该章节题目吗？")
                    .setPositiveButton("下载", ((dialog, which) -> downloadQuestionAsync(practice)))
                    .setNegativeButton("取消", null)
                    .show();
        }
    }


    /**
     * AsyncTask执行简单后台线程任务  下载题目
     **/
    private void downloadQuestionAsync(Practice practice) {
        new QuestionDownLoader(this, practice).execute();
    }

    /**
     * 线程池处理消息  下载题目
     **/
    private void downloadQuestion(int apiId) {
        ViewUtils.showProgress(getContext(), "开始下载题目...");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = QuestionService.getQuestionsOfPracticeFromServer(apiId);
                    Message mag = handler.obtainMessage(WHAT_QUESTION_DONE, json);
                    mag.arg1 = apiId;
                    handler.sendMessage(mag);
                } catch (IOException e) {
                    handler.sendMessage(handler.obtainMessage(WHAT_QUESTION_EXCEPTION, e.getMessage()));
                }
            }
        });
    }

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
        swipe.setColorSchemeColors((Color.parseColor("#CD853F")), Color.parseColor("#CD853F"));
        /** 判断触摸是否需要取消删除模式 **/
        find(R.id.fragment_practices_lv).setOnTouchListener(new ViewUtils.AbstractTouchHandler() {
            @Override
            public boolean handleTouch(MotionEvent event) {
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

    @Override
    public void search(String kw) {
        practices.clear();
        if (kw.isEmpty()) {
            practices.addAll(factory.get());
        } else {
            practices.addAll(factory.searchPractices(kw));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnPracticesSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "必须实现OnPracticesSelectedListener接口");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        handler.removeCallbacksAndMessages(null);
    }

    public interface OnPracticesSelectedListener {
        /**
         * 点击章节跳转题目
         *
         * @param practiceId
         */
        void onPracticesSelected(String practiceId, int apiId);
    }
}
