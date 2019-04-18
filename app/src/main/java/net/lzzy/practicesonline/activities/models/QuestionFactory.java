package net.lzzy.practicesonline.activities.models;

import android.text.TextUtils;

import net.lzzy.practicesonline.activities.constants.DbConstants;
import net.lzzy.practicesonline.activities.utils.AppUtils;
import net.lzzy.sqllib.SqlRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzzy_gxy on 2019/4/17.
 * Description:
 */
public class QuestionFactory {
    private static final QuestionFactory OUR_INSTACE = new QuestionFactory();
    private static SqlRepository<Question> repository;
    private SqlRepository<Option> optionRepository;

    private QuestionFactory() {
        repository = new SqlRepository<>(AppUtils.getContext(), Question.class, DbConstants.packager);
    }


    public static QuestionFactory getInstance() {
        return OUR_INSTACE;
    }

    public void insert(Question question) {
        //todo:1
        String q = repository.getInsertString(question);
        List<String> sqlActions = new ArrayList<>();
        for (Option option : question.getOptions()) {
            sqlActions.add(optionRepository.getInsertString(option));
        }
        sqlActions.add(q);
        repository.exeSqls(sqlActions);

    }

    public List<Question> getByPractice(String practiceId) {
        try {
            List<Question> questions = repository.getByKeyword(practiceId,
                    new String[]{Question.COL_PRACTICE_ID}, true);
            //需每个都要获取Question对象
            for (Question question : questions) {
                completeQuestion(question);
            }
            return questions;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
        //todo:2
    }

    List<String> getDeleteString(Question question) {
        List<String> sqlActions = new ArrayList<>();
        sqlActions.add(repository.getDeleteString(question));
        for (Option option : question.getOptions()) {
            sqlActions.add(optionRepository.getDeleteString(option));
        }
        String f = FavoriteFactory.getInstance().getDeleteString(question.getId().toString());
        if (!TextUtils.isEmpty(f)) {
            sqlActions.add(f);
        }
        return sqlActions;
        //todo:3
    }

    /**
     *  配置好option（选项） 题目与选项要完整
     * @param question
     */
    private void completeQuestion(Question question) throws InstantiationException, IllegalAccessException {
        List<Option> options = optionRepository.getByKeyword(question.getId().toString(),
                new String[]{Option.COL_QUESTION_ID}, true);
        question.setOptions(options);
        question.setDbType(question.getDbType());
        //todo:4
    }

    public Question getById(String questionId) {
        //todo:5
        try {
            Question question = repository.getById(questionId);
            completeQuestion(question);
            return question;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

}