import type { Metadata } from "next";
import { BankQuestions } from "../../../components/BankQuestions";

export const metadata: Metadata = {
  title: "Bank contents",
  description: "Assign approved questions to a Prunus Mume question bank.",
};

export default async function BankQuestionsPage({
  params,
}: {
  params: Promise<{ bankId: string }>;
}) {
  const { bankId } = await params;
  return <BankQuestions bankId={bankId} />;
}
