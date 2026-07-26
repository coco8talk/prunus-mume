package com.coco8talk.pm.api.question.service;

import java.util.List;

/**
 * 题目删除时的同步清理端口（依赖倒置）。question 模块删除题目时回调所有实现，
 * 让下游模块（如 interaction）清理与该题目相关的数据，而无需 question 依赖它们。
 * 这是同步直调端口，不是领域事件。
 */
public interface QuestionDeletionCleaner {
    void onQuestionDeleted(Long questionId);
    void onQuestionsDeleted(List<Long> questionIds);
}
