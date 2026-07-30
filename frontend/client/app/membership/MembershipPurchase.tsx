"use client";

import Link from "next/link";
import { useState } from "react";
import { useAuth } from "@/app/components/AuthProvider";
import { VipBadge } from "@/app/components/VipBadge";
import { apiUrl, errorMessage, membershipLevelId } from "@/app/lib/api";
import { membershipService } from "@/app/lib/services";
import {
  membershipPlans,
  type MembershipPlanId,
} from "@/app/data/membership";

type PurchaseState = "idle" | "signed-out" | "submitting" | "failed";

export function MembershipPurchase({
  initialPlan,
}: {
  initialPlan: MembershipPlanId;
}) {
  const { status: authStatus } = useAuth();
  const [selectedPlanId, setSelectedPlanId] = useState(initialPlan);
  const [purchaseState, setPurchaseState] = useState<PurchaseState>("idle");
  const [message, setMessage] = useState("");

  const selectedPlan =
    membershipPlans.find((plan) => plan.id === selectedPlanId) ?? membershipPlans[2];

  function selectPlan(planId: MembershipPlanId) {
    setSelectedPlanId(planId);
    setPurchaseState("idle");
    setMessage("");
  }

  async function createOrder() {
    if (authStatus !== "authenticated") {
      setPurchaseState("signed-out");
      return;
    }

    const levelId = membershipLevelId();
    if (!levelId) {
      setMessage("会员等级尚未配置，请设置 MEMBERSHIP_LEVEL_ID 后重试。");
      setPurchaseState("failed");
      return;
    }

    setPurchaseState("submitting");
    setMessage("");
    try {
      const order = await membershipService.submit(
        levelId,
        selectedPlan.id,
        selectedPlan.durationValue,
      );
      window.location.assign(apiUrl(`/membership-orders/${encodeURIComponent(order.outTradeNo)}/pay-page`));
    } catch (error) {
      setMessage(errorMessage(error));
      setPurchaseState("failed");
    }
  }

  return (
    <main className="page-shell app-shell membership-page" data-od-id="membership-page">
      <section className="membership-hero" data-od-id="membership-introduction">
        <div>
          <VipBadge />
          <p className="section-kicker">梅问会员</p>
          <h1>更完整的题解，更清晰的练习路径。</h1>
          <p>
            解锁会员题完整解析、进阶题库与结构化学习路径。按你的学习周期选择，
            所有方案均为一次性购买，不会自动续费。
          </p>
        </div>
        <dl className="membership-hero-facts" aria-label="会员服务说明">
          <div><dt>会员题解析</dt><dd>完整开放</dd></div>
          <div><dt>支付方式</dt><dd>支付宝</dd></div>
          <div><dt>续费方式</dt><dd>到期后自选</dd></div>
        </dl>
      </section>

      <section className="membership-benefit-section" aria-labelledby="benefit-heading">
        <div className="membership-section-heading">
          <div>
            <p className="section-kicker">会员权益</p>
            <h2 id="benefit-heading">专注于练习真正需要的内容</h2>
          </div>
          <p>公开题始终可以免费练习，会员在此基础上提供更完整的进阶支持。</p>
        </div>
        <div className="benefit-comparison" role="table" aria-label="普通用户与梅问会员权益对比">
          <div className="comparison-row comparison-head" role="row">
            <span role="columnheader">权益</span>
            <span role="columnheader">普通用户</span>
            <span role="columnheader">梅问会员</span>
          </div>
          {[
            ["公开题练习", "完整使用", "完整使用"],
            ["会员题完整解析", "预览题目", "完整开放"],
            ["进阶会员题库", "—", "完整开放"],
            ["结构化学习路径", "基础进度", "进阶路径"],
          ].map(([benefit, free, member]) => (
            <div className="comparison-row" role="row" key={benefit}>
              <strong role="cell">{benefit}</strong>
              <span role="cell">{free}</span>
              <span className="member-value" role="cell">{member}</span>
            </div>
          ))}
        </div>
      </section>

      <section className="membership-purchase-section" aria-labelledby="plan-heading">
        <div className="membership-section-heading">
          <div>
            <p className="section-kicker">选择方案</p>
            <h2 id="plan-heading">选择适合你的学习周期</h2>
          </div>
          <p>价格均为人民币含税价；创建订单后将前往支付宝完成付款。</p>
        </div>

        <div className="membership-checkout-layout">
          <div>
            <fieldset className="plan-selector" data-od-id="membership-plan-selector">
              <legend className="sr-only">会员时长</legend>
              {membershipPlans.map((plan) => (
                <label
                  className={`plan-option ${selectedPlan.id === plan.id ? "selected" : ""}`}
                  key={plan.id}
                  data-od-id={`membership-plan-${plan.id.toLowerCase()}`}
                >
                  <input
                    type="radio"
                    name="membership-plan"
                    value={plan.id}
                    checked={selectedPlan.id === plan.id}
                    onChange={() => selectPlan(plan.id)}
                  />
                  <span className="plan-radio" aria-hidden="true" />
                  <span className="plan-copy">
                    <span>
                      <strong>{plan.name}</strong>
                      {plan.recommended && <em>推荐</em>}
                    </span>
                    <small>{plan.description}</small>
                  </span>
                  <span className="plan-price">
                    <b>¥{plan.price}</b>
                    <small>{plan.duration}</small>
                  </span>
                </label>
              ))}
            </fieldset>

          </div>

          <aside className="order-summary" aria-labelledby="summary-heading" data-od-id="membership-order-summary">
            <div className="order-summary-heading">
              <p className="section-kicker">订单摘要</p>
              <h2 id="summary-heading">{selectedPlan.name}</h2>
              <span>{selectedPlan.duration}</span>
            </div>
            <dl>
              <div><dt>会员方案</dt><dd>{selectedPlan.name}</dd></div>
              <div><dt>服务时长</dt><dd>{selectedPlan.duration}</dd></div>
              <div><dt>支付方式</dt><dd><span className="alipay-mark">支</span> 支付宝</dd></div>
              <div className="order-total"><dt>应付金额</dt><dd><b>¥{selectedPlan.price}</b><small>CNY</small></dd></div>
            </dl>

            <button
              className="purchase-button"
              type="button"
              onClick={createOrder}
              disabled={purchaseState === "submitting"}
              data-od-id="create-membership-order"
            >
              {purchaseState === "submitting"
                ? "正在创建订单…"
                : authStatus === "authenticated"
                    ? `确认支付 ¥${selectedPlan.price}`
                    : "登录后继续购买"}
            </button>

            <div className="purchase-feedback" aria-live="polite">
              {purchaseState === "signed-out" && (
                <div className="purchase-message guidance" data-od-id="purchase-signed-out">
                  <strong>请先登录梅问账号</strong>
                  <p>登录后会返回会员页，你可以继续完成当前选择。</p>
                  <Link href="/login?next=%2Fmembership">前往登录 <span aria-hidden="true">→</span></Link>
                </div>
              )}
              {purchaseState === "submitting" && (
                <div className="purchase-message pending" role="status" data-od-id="purchase-submitting">
                  <span className="status-spinner" aria-hidden="true" />
                  <div><strong>正在创建订单</strong><p>请稍候，不要重复提交。</p></div>
                </div>
              )}
              {purchaseState === "failed" && (
                <div className="purchase-message error" role="alert" data-od-id="purchase-failed">
                  <strong>订单创建失败</strong>
                  <p>{message}</p>
                  <button type="button" onClick={createOrder}>重新创建</button>
                </div>
              )}
            </div>

            <ul className="service-notes">
              <li>创建订单后会整页前往支付宝，支付页面不会在站内加载。</li>
              <li>付款完成后，会员有效期会从支付成功时间开始计算。</li>
              <li>本服务不会自动续费，到期后可按需重新选择。</li>
            </ul>
          </aside>
        </div>
      </section>
    </main>
  );
}
