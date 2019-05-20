package net.lzzy.practicesonline.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import net.lzzy.practicesonline.R;

import net.lzzy.practicesonline.activities.models.Question;
import net.lzzy.practicesonline.activities.models.UserCookies;
import net.lzzy.practicesonline.activities.models.view.QuestionResult;
import net.lzzy.practicesonline.activities.models.view.WrongType;
import net.lzzy.practicesonline.activities.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lzzy_gxy on 2019/5/13.
 * Description:
 */
public class CharFragment extends BaseFragment {
    public static final String ARGS_RESULT = "result";
    private static final float MIN_DISTANCE = 100;
    List<QuestionResult> results;
    private CharFragment.OnCharSkipListener listener;
    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private Chart[] charts;
    private float touchX1;
    private int chartIndex = 0;
    private View[] dots;
    private List<Integer> counts = new ArrayList<>();

    /**
     * 静态方法传参数
     **/
    public static CharFragment newInstance(List<QuestionResult> results) {
        CharFragment fragment = new CharFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_RESULT, (ArrayList<? extends Parcelable>) results);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            results = getArguments().getParcelableArrayList(ARGS_RESULT);
        }
    }

    @Override
    protected void populate() {
//        Button button = find(R.id.fragment_char_circle_btn_view);
//        button.setOnClickListener(v ->
        //               listener.gotoGrid());
        // region 柱形图 前一个
        //      BarChartView barChart =  find(R.id.fragment_chart);
        int right = 0, miss = 0, extra = 0, wrong = 0;
        String[] HORIZONTAL_AXIS = {WrongType.RIGHT_OPTINS.toString(), WrongType.MISS_OPTIONS.toString(),
                WrongType.EXTRA_OPTIONS.toString(), WrongType.WRONG_OPTIONS.toString()};

//        for (int i = 0; i < WrongType.values().length; i++) {
//            HORIZONTAL_AXIS[i] = WrongType.getInstance(i).toString();
//        }
        for (QuestionResult questionResult : results) {
            switch (questionResult.getType()) {
                case RIGHT_OPTINS:
                    right++;
                    break;
                case MISS_OPTIONS:
                    miss++;
                    break;
                case EXTRA_OPTIONS:
                    extra++;
                    break;
                case WRONG_OPTIONS:
                    wrong++;
                    break;
                default:
                    break;
            }
        }
        float[] DATA = {right, miss, extra, wrong};
        //      barChart.setHorizontalAxis(HORIZONTAL_AXIS);
//        barChart.setDataList(DATA, 5);
        //endregion


        initCharts();
        // 饼图
        PieChart();
        //折线图
        lineChart();
        //柱形图
        barChart();
        View dot1 = find(R.id.fragment_chart_dot1);
        View dot2 = find(R.id.fragment_chart_dot2);
        View dot3 = find(R.id.fragment_chart_dot3);
        dots = new View[]{dot1, dot2, dot3};
        find(R.id.fragment_chart_container).setOnTouchListener(new ViewUtils.AbstractTouchHandler() {
            @Override
            public boolean handleTouch(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchX1 = event.getX();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float touchX2 = event.getX();
                    if (Math.abs(touchX2 - touchX1) > MIN_DISTANCE) {
                        if (touchX2 < touchX1) {
                            if (chartIndex < charts.length - 1) {
                                chartIndex++;
                            } else {
                                chartIndex = 0;
                            }
                        } else {
                            if (chartIndex > 0) {
                                chartIndex--;
                            } else {
                                chartIndex = charts.length - 1;
                            }
                        }
                        switchChart();
                    }
                }
                return true;
            }

            private void switchChart() {
                for (int i = 0; i < charts.length; i++) {
                    if (chartIndex == i) {
                        charts[i].setVisibility(View.VISIBLE);
                        dots[i].setBackgroundResource(R.drawable.dot_fill_style);
                    } else {
                        charts[i].setVisibility(View.GONE);
                        dots[i].setBackgroundResource(R.drawable.dot_style);
                    }
                }
            }
        });
    }

    private void initCharts() {
        pieChart = find(R.id.fragment_chart_pie);
        barChart = find(R.id.fragment_chart_bar);
        lineChart = find(R.id.fragment_chart_line);
        charts = new Chart[]{pieChart, barChart, lineChart};
        // int i = 0;
        for (Chart chart : charts) {
            chart.setTouchEnabled(false);
            chart.setVisibility(View.GONE);
            Description desc = new Description();
            //    desc.setText(titles[i]);
            chart.setDescription(desc);
            chart.setNoDataText("数据获取中.....");
            chart.setExtraOffsets(5, 10, 5, 5);
        }
    }

    private void PieChart() {

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        // pieChart.setExtraOffsets(5, 5, 5, 5);

        ArrayList<PieEntry> entries = new ArrayList<>();
        int Count = 0;
        for (QuestionResult qr : results) {
            if (qr.isRight()) {
                Count++;
            }
        }
        int wrongCount = results.size() - Count;

        entries.add(new PieEntry(Count, "正确"));
        entries.add(new PieEntry(wrongCount, "错误"));

        List<Integer> colors = new ArrayList<>();

        colors.add(Color.parseColor("#4DB34D"));
        colors.add(Color.parseColor("#EE1169"));

        PieDataSet pieDataSet = new PieDataSet(entries, "");
        PieData pieData = new PieData(pieDataSet);
        pieDataSet.setColors(colors);
        String descriptionStr = "正确及错误比例(单位%)";
        Description description = new Description();
        description.setText(descriptionStr);
        pieChart.setDescription(description);
        pieChart.setDrawHoleEnabled(false);
        //  pieChart.setTouchEnabled(false);
        pieChart.setData(pieData);

    }

    private void lineChart() {

        for (QuestionResult questionResult : results) {
            int count = UserCookies.getInstance().getReadCount(questionResult.getQuestionId().toString());
            counts.add(count);
        }

        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setNoDataText(".....");
        lineChart.setNoDataTextColor(Color.GRAY);
        lineChart.setBorderColor(Color.BLUE);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);

        lineChart.setExtraRightOffset(25f);
        lineChart.setExtraBottomOffset(10f);
        lineChart.setExtraTopOffset(10f);

        int i = 1;
        List<Entry> entries = new ArrayList<>();
        for (int coo : counts) {
            entries.add(new Entry(i++, coo));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, null);

        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setColor(Color.parseColor("#FC863E"));
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#31FF5A00"), Color.parseColor("#00FA5544")}));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setDrawValues(true);
        LineData data = new LineData(lineDataSet);
        lineChart.setData(data);
    }

    private void barChart() {
        ArrayList entries = new ArrayList<>();//将数据源添加到图标

        String[] HORIZONTAL_AXIS = {WrongType.RIGHT_OPTINS.toString(), WrongType.MISS_OPTIONS.toString(),
                WrongType.EXTRA_OPTIONS.toString(), WrongType.WRONG_OPTIONS.toString()};
        int right = 0, miss = 0, extra = 0, wrong = 0;

        for (QuestionResult questionResult : results) {
            switch (questionResult.getType()) {
                case RIGHT_OPTINS:
                    right++;
                    break;
                case MISS_OPTIONS:
                    miss++;
                    break;
                case EXTRA_OPTIONS:
                    extra++;
                    break;
                case WRONG_OPTIONS:
                    wrong++;
                    break;
                default:
                    break;
            }
        }
        float[] DATA = {right, miss, extra, wrong};
        for (int i = 0; i < WrongType.values().length; i++) {
           // HORIZONTAL_AXIS[i] = WrongType.getInstance(i).toString();
            entries.add(new BarEntry(i, DATA[i]));
        }
        BarDataSet barDataSet = new BarDataSet(entries, null);

        barDataSet.setValueTextSize(9f); //设置数值字体大小
        // barDataSet.setFormLineWidth(1f);  //线条宽度
        barDataSet.setFormSize(15.f);  ///图例窗体的大小
        barChart.getXAxis().setGranularity(1f); //间隔大小
        barChart.getXAxis().setDrawGridLines(false);

        barDataSet.setColors(Color.GREEN ,Color.YELLOW,Color.BLUE,Color.RED);  //   //摄住柱状图颜色
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        ArrayList dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
        BarData data = new BarData(dataSets);
        // barChart.getXAxis().(HORIZONTAL_AXIS);//设置X轴的刻度数
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(HORIZONTAL_AXIS));
        barChart.setData(data);

    }


    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_chant;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnCharSkipListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "必须实现OnGridSkipListener接口");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    @Override
    public void search(String kw) {

    }

    public interface OnCharSkipListener {
        /**
         * 跳转到GridFragment
         */
        //   void gotoGrid();
    }
}
