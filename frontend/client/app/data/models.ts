export type Difficulty = "入门" | "中级" | "进阶";
export type BankTone = "blue" | "coral" | "green" | "plum";

export type QuestionBank = {
  id: string;
  eyebrow: string;
  title: string;
  description: string;
  creator: string;
  creatorId?: string;
  progress: number;
  questions: number;
  level: Difficulty;
  tone: BankTone;
  category: string;
  picture?: string;
};

export type Question = {
  id: string;
  bankId?: string;
  title: string;
  content: string;
  answer?: string;
  difficulty: Difficulty;
  tags: string[];
  views: number;
  likes: number;
  favourites: number;
  vip: boolean;
};

export function formatCount(value: number) {
  return new Intl.NumberFormat("zh-CN").format(value);
}
