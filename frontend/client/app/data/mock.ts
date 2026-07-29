export type Difficulty = "入门" | "中级" | "进阶";
export type BankTone = "blue" | "coral" | "green" | "plum";

export type QuestionBank = {
  id: string;
  eyebrow: string;
  title: string;
  description: string;
  creator: string;
  progress: number;
  questions: number;
  level: Difficulty;
  tone: BankTone;
  category: string;
};

export type Question = {
  id: string;
  bankId: string;
  title: string;
  content: string;
  answer: string;
  difficulty: Difficulty;
  tags: string[];
  views: number;
  likes: number;
  favourites: number;
  vip: boolean;
};

export const banks: QuestionBank[] = [
  {
    id: "frontend",
    eyebrow: "前端工程",
    title: "现代前端核心题库",
    description: "从 JavaScript 运行机制到 React 状态设计，建立稳固的工程基础。",
    creator: "周予安",
    progress: 68,
    questions: 186,
    level: "中级",
    tone: "blue",
    category: "前端",
  },
  {
    id: "algorithm",
    eyebrow: "算法与结构",
    title: "高频算法训练营",
    description: "按模式拆解数组、链表、树与动态规划，适合系统刷题。",
    creator: "陈墨",
    progress: 34,
    questions: 240,
    level: "进阶",
    tone: "coral",
    category: "算法",
  },
  {
    id: "system-design",
    eyebrow: "系统设计",
    title: "可扩展系统设计",
    description: "围绕真实场景理解缓存、消息队列、分布式一致性与容量规划。",
    creator: "沈知行",
    progress: 12,
    questions: 96,
    level: "进阶",
    tone: "green",
    category: "架构",
  },
  {
    id: "java",
    eyebrow: "Java 工程",
    title: "Java 并发与 JVM",
    description: "从线程协作到内存模型，串联高并发服务的关键知识。",
    creator: "顾清川",
    progress: 51,
    questions: 154,
    level: "中级",
    tone: "plum",
    category: "后端",
  },
  {
    id: "database",
    eyebrow: "数据基础",
    title: "数据库原理与实战",
    description: "掌握索引、事务、查询优化与高可用数据架构。",
    creator: "江砚",
    progress: 22,
    questions: 128,
    level: "中级",
    tone: "green",
    category: "数据库",
  },
  {
    id: "network",
    eyebrow: "计算机基础",
    title: "网络与操作系统",
    description: "理解一次请求背后的协议、进程、内存与 I/O。",
    creator: "许星野",
    progress: 0,
    questions: 112,
    level: "入门",
    tone: "blue",
    category: "基础",
  },
  {
    id: "product",
    eyebrow: "产品思维",
    title: "技术人的产品基本功",
    description: "用用户问题、指标与实验方法做更可靠的产品判断。",
    creator: "林晚",
    progress: 8,
    questions: 72,
    level: "入门",
    tone: "coral",
    category: "产品",
  },
  {
    id: "ai",
    eyebrow: "人工智能",
    title: "大模型应用工程",
    description: "覆盖提示设计、RAG、评测与智能体应用的落地方法。",
    creator: "陆深",
    progress: 16,
    questions: 108,
    level: "进阶",
    tone: "plum",
    category: "AI",
  },
];

export const questions: Question[] = [
  {
    id: "q101",
    bankId: "frontend",
    title: "JavaScript 闭包解决了什么问题？",
    content:
      "请解释 JavaScript 中闭包的形成条件、它能保留外部作用域变量的原因，并给出一个适合使用闭包的真实工程场景。",
    answer:
      "闭包是函数与其词法环境的组合。当内部函数在外部函数执行结束后仍被引用，它依然可以访问定义时作用域中的变量。常见场景包括创建私有状态、函数工厂和事件处理器。使用时要避免无意持有大对象，以免延长其生命周期。",
    difficulty: "入门",
    tags: ["JavaScript", "闭包", "作用域"],
    views: 3218,
    likes: 486,
    favourites: 312,
    vip: false,
  },
  {
    id: "q102",
    bankId: "frontend",
    title: "React 状态更新为什么可能是异步的？",
    content:
      "说明 React 对状态更新进行批处理的动机，以及在依赖前一个状态时应该采用的写法。",
    answer:
      "React 会批处理同一轮交互中的更新，以减少重复渲染并保证 UI 一致性。依赖前一个状态时应传入更新函数，例如 setCount(count => count + 1)，避免读取到当前渲染快照中的旧值。",
    difficulty: "中级",
    tags: ["React", "状态管理"],
    views: 2860,
    likes: 392,
    favourites: 244,
    vip: false,
  },
  {
    id: "q103",
    bankId: "frontend",
    title: "浏览器从输入 URL 到页面可交互经历了什么？",
    content:
      "按网络请求、解析、渲染与脚本执行的顺序，概述浏览器完成一次页面加载的关键过程。",
    answer:
      "浏览器先解析 URL、查询 DNS 并建立连接，随后发送 HTTP 请求；收到响应后解析 HTML 构建 DOM、解析 CSS 构建 CSSOM，合成渲染树并完成布局与绘制。JavaScript 的下载和执行会依据脚本属性参与调度，最终在必要资源与任务完成后进入可交互状态。",
    difficulty: "中级",
    tags: ["浏览器", "网络", "渲染"],
    views: 4102,
    likes: 528,
    favourites: 371,
    vip: false,
  },
  {
    id: "q104",
    bankId: "algorithm",
    title: "如何判断链表是否存在环？",
    content:
      "给定一个单链表，请设计 O(n) 时间、O(1) 额外空间的算法判断链表中是否存在环。",
    answer:
      "使用快慢指针。慢指针每次前进一步，快指针每次前进两步；若存在环，两者最终会在环内相遇。若快指针或其 next 为空，则链表无环。",
    difficulty: "入门",
    tags: ["链表", "双指针"],
    views: 3681,
    likes: 441,
    favourites: 289,
    vip: false,
  },
  {
    id: "q105",
    bankId: "algorithm",
    title: "动态规划的状态转移方程如何设计？",
    content:
      "以最长递增子序列为例，说明如何定义状态、寻找转移关系并确定计算顺序。",
    answer:
      "可定义 dp[i] 为以 nums[i] 结尾的最长递增子序列长度。枚举 j < i，若 nums[j] < nums[i]，则 dp[i] = max(dp[i], dp[j] + 1)。因为 i 依赖更小下标，所以按 i 从左到右计算，最终答案是所有 dp[i] 的最大值。",
    difficulty: "中级",
    tags: ["动态规划", "数组"],
    views: 2944,
    likes: 376,
    favourites: 351,
    vip: false,
  },
  {
    id: "q106",
    bankId: "system-design",
    title: "如何设计一个支持千万用户的消息系统？",
    content:
      "请从连接管理、消息投递、顺序性、离线消息和容量规划五个方面，设计一个可水平扩展的即时消息系统。",
    answer:
      "连接层通过网关集群维护长连接并将用户路由写入共享目录；消息经持久化队列削峰并按会话键分区以维持局部顺序；投递服务查询在线路由，在线实时推送、离线写入收件箱。容量规划需分别估算连接数、峰值 QPS、消息体积与保留周期，并通过多地域部署、重试与幂等机制提高可靠性。",
    difficulty: "进阶",
    tags: ["系统设计", "消息队列", "高并发"],
    views: 5210,
    likes: 702,
    favourites: 618,
    vip: true,
  },
  {
    id: "q107",
    bankId: "system-design",
    title: "缓存击穿、穿透和雪崩有什么区别？",
    content:
      "比较三类缓存故障的触发条件、系统表现和常见治理方式。",
    answer:
      "击穿是热点键失效导致请求集中打到数据库，可用互斥更新或逻辑过期；穿透是大量查询不存在的数据，可用布隆过滤器和空值缓存；雪崩是大量键同时失效或缓存整体不可用，可通过随机过期、分级缓存、限流降级与高可用部署缓解。",
    difficulty: "中级",
    tags: ["缓存", "高并发"],
    views: 4560,
    likes: 611,
    favourites: 420,
    vip: false,
  },
  {
    id: "q108",
    bankId: "java",
    title: "volatile 能保证原子性吗？",
    content:
      "解释 Java volatile 关键字提供的可见性和有序性保证，并说明为什么 i++ 仍然不是原子操作。",
    answer:
      "volatile 写入会把值刷新到主内存，读取会获取最新值，并建立相应的 happens-before 关系以限制重排序。但 i++ 包含读取、计算和写回多个步骤，线程之间仍可能交错，因此需要原子类或锁保证复合操作的原子性。",
    difficulty: "中级",
    tags: ["Java", "并发", "JMM"],
    views: 3378,
    likes: 451,
    favourites: 327,
    vip: false,
  },
  {
    id: "q109",
    bankId: "database",
    title: "联合索引为什么要遵循最左前缀？",
    content:
      "结合 B+ 树中联合索引的排序方式，解释最左前缀原则以及常见的索引失效场景。",
    answer:
      "联合索引按索引列从左到右依次排序。只有先确定左侧列，后续列的顺序才可被有效利用；跳过最左列时，树中的记录无法按后续列形成连续区间。范围查询后的列通常也无法继续用于缩小扫描范围。",
    difficulty: "中级",
    tags: ["MySQL", "索引"],
    views: 3890,
    likes: 507,
    favourites: 402,
    vip: false,
  },
  {
    id: "q110",
    bankId: "network",
    title: "TCP 为什么需要三次握手？",
    content:
      "从双方收发能力确认和历史连接干扰两个角度，说明建立 TCP 连接需要三次握手的原因。",
    answer:
      "三次握手让客户端和服务端都能确认自己的发送、接收能力以及对方的发送、接收能力，同时交换并确认初始序列号。第三次确认还能避免延迟到达的旧连接请求让服务端错误建立连接。",
    difficulty: "入门",
    tags: ["TCP", "网络"],
    views: 4421,
    likes: 602,
    favourites: 388,
    vip: false,
  },
  {
    id: "q111",
    bankId: "ai",
    title: "RAG 系统如何评估检索质量？",
    content:
      "为一个企业知识库 RAG 系统设计检索阶段的离线评测集与核心指标。",
    answer:
      "先从真实问题构造查询、相关文档与证据片段的标注集，再用 Recall@K 衡量相关文档召回，用 MRR 或 nDCG 衡量排序质量，同时记录上下文覆盖率和噪声比例。应按问题类型、知识域和时间切片分析，并保留线上无答案问题用于持续扩充评测集。",
    difficulty: "进阶",
    tags: ["RAG", "评测", "大模型"],
    views: 2140,
    likes: 318,
    favourites: 296,
    vip: true,
  },
  {
    id: "q112",
    bankId: "product",
    title: "如何判断一个功能值得开发？",
    content:
      "面对一项用户呼声很高但开发成本不低的功能，请给出一套可验证的决策过程。",
    answer:
      "先明确功能对应的用户问题和目标人群，用访谈、行为数据验证问题频率与严重度；再定义能够反映价值的成功指标，设计成本更低的原型或人工服务进行实验。结合预期收益、覆盖人数、信心与开发成本排序，最后明确继续、调整或停止的判据。",
    difficulty: "入门",
    tags: ["产品", "决策"],
    views: 1660,
    likes: 247,
    favourites: 181,
    vip: false,
  },
];

export const featuredBanks = banks.slice(0, 3);
export const favouriteQuestionIds = ["q101", "q105", "q109"];

export function getBank(bankId: string) {
  return banks.find((bank) => bank.id === bankId);
}

export function getQuestion(questionId: string) {
  return questions.find((question) => question.id === questionId);
}

export function getQuestionsForBank(bankId: string) {
  return questions.filter((question) => question.bankId === bankId);
}

export function formatCount(value: number) {
  return new Intl.NumberFormat("zh-CN").format(value);
}
