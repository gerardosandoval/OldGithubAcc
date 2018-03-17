package com.bignerdranch.android.geoquiz;

/**
 * Created by Gerardo on 02/09/2015.
 */
public class Question {

    private int mTextResId;
    private boolean mAnswerTrue;

    public Question(int TextResId, boolean answerTrue){
        mAnswerTrue = answerTrue;
        mTextResId = TextResId;
    }

    public boolean isAnswerTrue() {
        return mAnswerTrue;
    }

    public void setAnswerTrue(boolean answerTrue) {
        mAnswerTrue = answerTrue;
    }

    public int getTextResId() {
        return mTextResId;
    }

    public void setTextResId(int textResId) {
        mTextResId = textResId;
    }
}
