import { MembershipPurchase } from "./MembershipPurchase";
import {
  getMembershipPlan,
  type MembershipPlanId,
} from "@/app/data/membership";

export default async function MembershipPage({
  searchParams,
}: {
  searchParams: Promise<{ plan?: string }>;
}) {
  const { plan } = await searchParams;
  const initialPlan = (getMembershipPlan(plan)?.id ?? "QUARTER") as MembershipPlanId;

  return <MembershipPurchase initialPlan={initialPlan} />;
}
