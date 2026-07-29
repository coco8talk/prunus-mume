import type { Metadata } from "next";
import { QuestionBankManagement } from "../components/QuestionBankManagement";

export const metadata: Metadata = {
  title: "Question banks",
  description: "Create, organize, and maintain Prunus Mume question banks.",
};

export default function QuestionBanksPage() {
  return <QuestionBankManagement />;
}
