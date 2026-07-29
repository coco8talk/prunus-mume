import type { Metadata } from "next";
import { QuestionManagement } from "../components/QuestionManagement";

export const metadata: Metadata = {
  title: "Questions",
  description: "Search, create, edit, and remove questions.",
};

export default function QuestionsPage() {
  return <QuestionManagement />;
}
