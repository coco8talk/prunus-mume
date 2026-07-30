import type { Metadata } from "next";
import { ReviewHistory } from "../../components/ReviewHistory";

export const metadata: Metadata = {
  title: "Review history",
  description: "Search and inspect the question review audit trail.",
};

export default function ReviewHistoryPage() {
  return <ReviewHistory />;
}
