import type { Difficulty } from "@/app/data/mock";

export type ContributionStatus = "pending" | "approved" | "rejected";

export type Contribution = {
  id: string;
  title: string;
  content: string;
  answer: string;
  tags: string[];
  difficulty: Difficulty;
  recommendVip: boolean;
  status: ContributionStatus;
  submittedAt: string;
  reviewComment?: string;
};

export type EditableProfile = {
  displayName: string;
  bio: string;
  phoneNumber: string;
  email: string;
  grade: string;
  workExperience: string;
  expertiseDirection: string;
};

export const initialContributions: Contribution[] = [
  {
    id: "c-1042",
    title: "React Server Components 如何划分客户端边界？",
    content: "请说明在实际项目中判断组件应当运行在服务端还是客户端的关键依据。",
    answer: "默认保留在服务端；仅在需要状态、事件处理器或浏览器 API 时建立客户端边界，并尽量缩小边界范围。",
    tags: ["React", "RSC", "架构"],
    difficulty: "中级",
    recommendVip: false,
    status: "pending",
    submittedAt: "2026-07-26",
  },
  {
    id: "c-1036",
    title: "如何为异步任务设计可靠的幂等键？",
    content: "结合支付回调或消息消费场景，说明幂等键的生成、存储与过期策略。",
    answer: "幂等键应由业务唯一事实生成，并与处理结果原子存储；重复请求直接复用首次结果，过期时间至少覆盖重试窗口。",
    tags: ["分布式", "幂等", "消息队列"],
    difficulty: "进阶",
    recommendVip: true,
    status: "approved",
    submittedAt: "2026-07-18",
  },
  {
    id: "c-1029",
    title: "CSS 中 BFC 的全部触发方式有哪些？",
    content: "列出所有 BFC 触发方式并解释其布局影响。",
    answer: "通过 overflow、display、position 等属性可建立新的块级格式化上下文。",
    tags: ["CSS", "布局"],
    difficulty: "入门",
    recommendVip: false,
    status: "rejected",
    submittedAt: "2026-07-08",
    reviewComment: "题目范围过宽，建议聚焦“BFC 如何解决外边距折叠”，并补充一个可运行的示例。",
  },
];

export const initialProfile: EditableProfile = {
  displayName: "林晚",
  bio: "前端工程师，也在认真学习系统设计。相信好的问题比标准答案更接近理解。",
  phoneNumber: "138 **** 6721",
  email: "linwan@example.com",
  grade: "本科",
  workExperience: "5 年前端开发经验，参与过内容平台与协作工具的产品研发。",
  expertiseDirection: "React、TypeScript、前端工程化",
};

export const publicProfiles = {
  "lin-wan": {
    displayName: "林晚",
    role: "认证贡献者",
    bio: "前端工程师，也在认真学习系统设计。相信好的问题比标准答案更接近理解。",
    joinDate: "2023 年 9 月",
    expertise: ["React", "TypeScript", "前端工程化"],
    banks: [
      { id: "product", title: "技术人的产品基本功", questions: 72 },
      { id: "frontend", title: "现代前端核心题库", questions: 186 },
    ],
    approvedContributions: 28,
  },
  "zhou-yuan": {
    displayName: "周予安",
    role: "题库创作者",
    bio: "关注前端基础、浏览器原理与可靠的软件设计。",
    joinDate: "2022 年 11 月",
    expertise: ["JavaScript", "浏览器", "Web 性能"],
    banks: [{ id: "frontend", title: "现代前端核心题库", questions: 186 }],
    approvedContributions: 46,
  },
} as const;
