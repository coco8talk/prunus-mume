import Link from "next/link";

export default function PaymentResultPage() {
  return (
    <main className="page-shell app-shell payment-result-page" data-od-id="membership-payment-result">
      <section className="payment-result-card processing" aria-labelledby="payment-result-heading">
        <div className="payment-result-status">
          <span className="result-mark" aria-hidden="true">支</span>
          <p className="section-kicker">支付结果</p>
          <h1 id="payment-result-heading">请以支付宝返回页面为准</h1>
          <p>
            真实支付结果由后端的支付宝返回地址直接展示；梅问不会通过站内请求获取或模拟支付页面。
          </p>
        </div>
        <div className="payment-result-actions">
          <Link className="result-primary" href="/me/profile">查看个人资料</Link>
          <Link className="result-secondary" href="/membership">返回会员页</Link>
        </div>
      </section>
    </main>
  );
}
