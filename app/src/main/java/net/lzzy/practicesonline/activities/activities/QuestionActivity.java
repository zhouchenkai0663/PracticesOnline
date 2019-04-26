package net.lzzy.practicesonline.activities.activities;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;

import static net.lzzy.practicesonline.activities.activities.PracticesActivity.PRACTICES_ID;

public class QuestionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        String PracticesId=getIntent().getStringExtra(PRACTICES_ID);


    }

    /**
     * 托管activity的布局
     *
     * @return activity的布局
     */
    @Override
    protected int getLayoutRse() {
        return R.layout.activity_question;
    }

    /**
     * fragment容器的ID
     *
     * @return 容器的ID
     */
    @Override
    protected int getContainerId() {
        return R.id.activity_question_container;
    }

    /**
     * 生成Fragment对象
     *
     * @return Fragment对象
     */
    @Override
    protected Fragment createFragment() {
        return null;
    }
}
