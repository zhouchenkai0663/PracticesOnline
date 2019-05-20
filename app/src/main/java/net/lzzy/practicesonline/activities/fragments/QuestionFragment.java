package net.lzzy.practicesonline.activities.fragments;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.models.FavoriteFactory;
import net.lzzy.practicesonline.activities.models.Option;
import net.lzzy.practicesonline.activities.models.Question;
import net.lzzy.practicesonline.activities.models.QuestionFactory;
import net.lzzy.practicesonline.activities.models.view.QuestionType;
import net.lzzy.practicesonline.activities.models.UserCookies;

import java.util.List;

/**
 * Created by lzzy_gxy on 2019/4/26.
 * Description:
 */
public class QuestionFragment extends BaseFragment {

    public static final String ARG_QUESTION_ID = "argQuestionId";
    public static final String ARG_POS = "argPos";
    public static final String ARG_IS_COMMITTED = "argIsCommitted";
    private Question question;
    private int pos;
    private boolean isCommitted;
    private boolean isMulti = false;
    private TextView tvType;
    private ImageButton imgFavorite;
    private TextView tvContent;
    private RadioGroup container;

    public static QuestionFragment newInstance(String questionId, int pos, boolean isCommitted) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUESTION_ID, questionId);
        args.putInt(ARG_POS, pos);
        args.putBoolean(ARG_IS_COMMITTED, isCommitted);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pos = getArguments().getInt(ARG_POS);
            isCommitted = getArguments().getBoolean(ARG_IS_COMMITTED);
            question = QuestionFactory.getInstance().getById(getArguments().getString(ARG_QUESTION_ID));
        }
    }

    @Override
    protected void populate() {
        initViews();
        //题目及收藏
        displayQuestion();
        //选项
        generateOptions();
    }

    private void generateOptions() {
        List<Option> options = question.getOptions();
        for (Option option : options) {
            CompoundButton btn = isMulti ? new CheckBox(getContext()) : new RadioButton(getContext());
            String content = option.getLabel() + " . " + option.getContent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btn.setButtonTintList(ColorStateList.valueOf(Color.GRAY));
            }
            btn.setText(content);
            btn.setEnabled(!isCommitted);
            btn.setOnCheckedChangeListener((buttonView, isChecked) ->
                    UserCookies.getInstance().changeOptionState(option, isChecked, isMulti));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // btn.setTextAppearance(R.style.Text2);
            }
            container.addView(btn);
            //添加点击监听，选中了就要记录到选项文件SharedPreferences，取消选中则从文件中把选项去掉
            boolean shouldCheck = UserCookies.getInstance().isOptionSelected(option);
            //勾选，到文件中是否存在选项的id，存在则勾选
            if (isMulti) {
                //  多选 checkbox btn.setChecked(true);
                btn.setChecked(shouldCheck);
            } else if (shouldCheck) {
                // 单选 radiobutton container.check(btn.getId());
                container.check(btn.getId());
            }
            /** 正确答案选项改变成绿色 **/
            if (isCommitted && option.isAnswer()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    btn.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                } else {
                    btn.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        }
    }


    private void displayQuestion() {
        isMulti = question.getType() == QuestionType.MULTI_CHOICE;
        int label = pos + 1;
        String qType = label + "." + question.getType().toString();
        //题目
        String content = question.getContent();
        tvContent.setText(content);
        tvType.setText(qType);
        int starId = FavoriteFactory.getInstance().isQuestionStarred(question.getId().toString()) ?
                android.R.drawable.star_on : android.R.drawable.star_off;
        imgFavorite.setImageResource(starId);
        //收藏
        imgFavorite.setOnClickListener(v -> switchStar());
    }

    private void switchStar() {
        boolean collect = FavoriteFactory.getInstance().isQuestionStarred(question.getId().toString());
        if (collect) {
            FavoriteFactory.getInstance().canceIStarQuestion(question.getId());
            imgFavorite.setImageResource(android.R.drawable.star_off);
        } else {
            FavoriteFactory.getInstance().starQuestion(question.getId());
            imgFavorite.setImageResource(android.R.drawable.star_on);
        }
    }

    private void initViews() {
        tvType = find(R.id.fragment_question_tv_question_type);
        imgFavorite = find(R.id.fragment_question_img_favorite);
        tvContent = find(R.id.fragment_question_tv_content);
        container = find(R.id.fragment_question_option_container);
        if (isCommitted) {
            container.setOnClickListener(v ->
                    new AlertDialog.Builder(getContext())
                    .setMessage(question.getAnalysis()).show()
            );
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_question;
    }

    @Override
    public void search(String kw) {

    }

}
