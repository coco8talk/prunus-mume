package com.coco8talk.pm.api.question.service;

import java.util.List;

/**
 * 题目删除时的同步清理端口（依赖倒置）。question 模块删除题目时回调所有实现，
 * 让下游模块（如 interaction）清理与该题目相关的数据，而无需 question 依赖它们。
 * 这是同步直调端口，不是领域事件。
 *
 * TODO: 当前跨服务场景下失效。pm-interaction 的实现类
 * (InteractionDeletionCleaner) 所在包未被 pm-question 的 scanBasePackages
 * 扫描到，QuestionServiceImpl 注入的 List<QuestionDeletionCleaner> 运行时为空，
 * 删除题目不会真正触发点赞/收藏记录清理。需要评估 Feign 直调 vs MQ 事件两种方案重新实现。
 */
public interface QuestionDeletionCleaner {
    void onQuestionDeleted(Long questionId);
    void onQuestionsDeleted(List<Long> questionIds);
}
