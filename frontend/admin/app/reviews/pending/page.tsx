import type { Metadata } from "next";
import { ReviewQueue } from "../../components/ReviewQueue";

export const metadata: Metadata = {
  title: "Review queue",
  description: "Review pending question submissions inline.",
};

export default function PendingReviewsPage() {
  return <ReviewQueue />;
}
