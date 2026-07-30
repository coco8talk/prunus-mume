export type MembershipPlanId = "MONTH" | "QUARTER" | "YEAR" | "TRIAL";

export type MembershipPlan = {
  id: MembershipPlanId;
  name: string;
  duration: string;
  durationValue: number;
  price: number;
  description: string;
  recommended?: boolean;
};

export const membershipPlans: MembershipPlan[] = [
  {
    id: "TRIAL",
    name: "体验会员",
    duration: "7 天",
    durationValue: 7,
    price: 1,
    description: "适合先体验完整题解与会员题库",
  },
  {
    id: "MONTH",
    name: "月度会员",
    duration: "1 个月",
    durationValue: 1,
    price: 29,
    description: "适合围绕近期目标集中练习",
  },
  {
    id: "QUARTER",
    name: "季度会员",
    duration: "3 个月",
    durationValue: 3,
    price: 79,
    description: "适合完成一段系统学习计划",
    recommended: true,
  },
  {
    id: "YEAR",
    name: "年度会员",
    duration: "12 个月",
    durationValue: 12,
    price: 269,
    description: "适合长期积累与持续复盘",
  },
];

export function getMembershipPlan(value: string | undefined) {
  return membershipPlans.find((plan) => plan.id === value);
}
