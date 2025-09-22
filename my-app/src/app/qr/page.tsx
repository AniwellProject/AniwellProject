'use client';

import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
// This example uses the camera to scan a QR code and then fetches a report by code.
// Dependencies to install:
//   npm i @yudiel/react-qr-scanner zod
// TailwindCSS is used for styling.

import { Scanner } from "@yudiel/react-qr-scanner";
import type { IDetectedBarcode } from "@yudiel/react-qr-scanner";
import { z } from "zod";
// Charts
import { ResponsiveContainer, LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, AreaChart, Area } from "recharts";

// Pet report specific schemas
const PetDaySchema = z.object({
  date: z.string(), // YYYY-MM-DD
  bowel: z.number().nonnegative(),
  waterMl: z.number().nonnegative(),
  foodG: z.number().nonnegative(),
});
const DiagnosisItemSchema = z.object({
  name: z.string(),
  count: z.number().nonnegative(),
});
const PetReportSchema = z.object({
  period: z.object({ start: z.string(), end: z.string() }),
  daily: z.array(PetDaySchema),
  diagnoses: z.array(DiagnosisItemSchema),
  intakeSummary: z.object({ totalWaterMl: z.number(), totalFoodG: z.number() }).optional(),
});
export type PetReportData = z.infer<typeof PetReportSchema>;

const ReportSchema = z.object({
  code: z.string(),
  title: z.string(),
  createdAt: z.string(),
  owner: z.string().optional(),
  summary: z.string().optional(),
  metrics: z
    .array(
      z.object({
        label: z.string(),
        value: z.union([z.number(), z.string()]),
      })
    )
    .optional(),
  sections: z
    .array(
      z.object({
        heading: z.string(),
        content: z.string(),
      })
    )
    .optional(),
  petReport: PetReportSchema.optional(),
});
export type Report = z.infer<typeof ReportSchema>;

// ---- Test fixtures (mock data for quick testing)
const MOCK_REPORTS: Record<string, Report> = {
  "DEMO-123": {
    code: "DEMO-123",
    title: "Health Check Report",
    createdAt: new Date().toISOString(),
    owner: "Aniwell QA",
    summary: "자동 점검 결과 양호. 일부 권장 설정이 감지되었습니다.",
    metrics: [
      { label: "CPU 사용률(평균)", value: "27%" },
      { label: "메모리 사용률", value: "61%" },
      { label: "에러 수(24h)", value: 1 }
    ],
    sections: [
      { heading: "요약", content: "시스템 전반 상태는 양호합니다. 보안 패치가 1건 미적용으로 감지되었으며, 적용을 권장합니다." },
      { heading: "세부 권장 사항", content: "방화벽 규칙 점검 및 Node.js LTS 업그레이드를 권장합니다. DB 연결 재시도 로직에 지수 백오프 적용을 고려하세요." }
    ],
    petReport: {
      period: { start: "2025-08-06", end: "2025-08-12" },
      daily: [
        { date: "2025-08-06", bowel: 1, waterMl: 300, foodG: 120 },
        { date: "2025-08-07", bowel: 2, waterMl: 420, foodG: 140 },
        { date: "2025-08-08", bowel: 1, waterMl: 380, foodG: 130 },
        { date: "2025-08-09", bowel: 0, waterMl: 350, foodG: 110 },
        { date: "2025-08-10", bowel: 1, waterMl: 410, foodG: 150 },
        { date: "2025-08-11", bowel: 2, waterMl: 520, foodG: 160 },
        { date: "2025-08-12", bowel: 1, waterMl: 520, foodG: 100 }
      ],
      diagnoses: [
        { name: "양호", count: 4 },
        { name: "소화불량", count: 2 },
        { name: "설사", count: 1 }
      ],
      intakeSummary: { totalWaterMl: 2900, totalFoodG: 910 }
    }
  },
  "DEMO-456": {
    code: "DEMO-456",
    title: "Weekly Wellness Report",
    createdAt: new Date().toISOString(),
    owner: "Aniwell QA",
    summary: "활동량 증가에 따라 수분 섭취가 늘었습니다. 배변 패턴은 안정적입니다.",
    metrics: [
      { label: "산책 횟수(주)", value: 9 },
      { label: "평균 컨디션", value: "좋음" }
    ],
    sections: [
      { heading: "관찰 포인트", content: "더운 날씨에 따라 식수량 증가. 저녁 사료 급여량은 10% 감량 권장." }
    ],
    petReport: {
      period: { start: "2025-08-13", end: "2025-08-19" },
      daily: [
        { date: "2025-08-13", bowel: 1, waterMl: 450, foodG: 150 },
        { date: "2025-08-14", bowel: 1, waterMl: 480, foodG: 155 },
        { date: "2025-08-15", bowel: 2, waterMl: 520, foodG: 160 },
        { date: "2025-08-16", bowel: 1, waterMl: 500, foodG: 150 },
        { date: "2025-08-17", bowel: 1, waterMl: 530, foodG: 165 },
        { date: "2025-08-18", bowel: 2, waterMl: 560, foodG: 170 },
        { date: "2025-08-19", bowel: 1, waterMl: 540, foodG: 160 }
      ],
      diagnoses: [
        { name: "양호", count: 5 },
        { name: "변비 의심", count: 1 },
        { name: "설사", count: 1 }
      ],
      intakeSummary: { totalWaterMl: 3580, totalFoodG: 1110 }
    }
  }
};

// ---- Utility: mock fetch (replace with your API)
const API_BASE = (import.meta as any)?.env?.VITE_API_BASE || "";
async function fetchReportByCode(code: string): Promise<Report> {
  // 1) Mock lookup first
  await new Promise((r) => setTimeout(r, 400));
  const key = code.trim().toUpperCase();
  const mock = MOCK_REPORTS[key];
  if (mock) return mock;

  // 2) Real API fallback (if configured)
  if (API_BASE) {
    const res = await fetch(`${API_BASE}/reports/${encodeURIComponent(code)}`);
    if (!res.ok) throw new Error(`Report not found (${res.status})`);
    const json = await res.json();
    const parsed = ReportSchema.parse(json);
    return parsed;
  }

  throw new Error("해당 코드의 레포트를 찾을 수 없습니다. (DEMO-123 또는 DEMO-456로 테스트해보세요)");
}

// ---- Small helpers
function classNames(...xs: Array<string | false | undefined | null>) {
  return xs.filter(Boolean).join(" ");
}

function PrettyDate({ iso }: { iso: string }) {
  const d = new Date(iso);
  const text = isNaN(d.getTime()) ? iso : d.toLocaleString();
  return <span>{text}</span>;
}

// ---- Report card
function ReportCard({ report }: { report: Report }) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white shadow-sm overflow-hidden">
      <div className="px-6 py-5 border-b border-gray-100 bg-gradient-to-r from-gray-50 to-white">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold leading-tight">{report.title}</h2>
            <p className="text-sm text-gray-500 mt-1">
              코드 <span className="font-mono">{report.code}</span> • 생성일 <PrettyDate iso={report.createdAt} />
            </p>
          </div>
          <button
            className="rounded-xl px-4 py-2 text-sm font-medium border border-gray-300 hover:bg-gray-50"
            onClick={() => window.print()}
          >
            인쇄/저장
          </button>
        </div>
      </div>

      {report.summary && (
        <div className="px-6 pt-5">
          <div className="text-base leading-relaxed">{report.summary}</div>
        </div>
      )}

      {report.metrics && report.metrics.length > 0 && (
        <div className="px-6 pb-1 pt-5 grid grid-cols-1 sm:grid-cols-3 gap-4">
          {report.metrics.map((m, i) => (
            <div key={i} className="rounded-xl border border-gray-100 p-4 bg-gray-50">
              <div className="text-xs text-gray-500">{m.label}</div>
              <div className="mt-1 text-2xl font-bold">{m.value}</div>
            </div>
          ))}
        </div>
      )}

      {report.sections && report.sections.length > 0 && (
        <div className="px-6 py-5 space-y-6">
          {report.sections.map((s, i) => (
            <section key={i}>
              <h3 className="text-lg font-semibold mb-1">{s.heading}</h3>
              <p className="text-gray-700 leading-relaxed whitespace-pre-wrap">{s.content}</p>
            </section>
          ))}
        </div>
      )}
    </div>
  );
}

// ---- Pet Health Report (date-wise, diagnosis-wise, intake-wise)
function SectionCard({ title, subtitle, children }: { title: string; subtitle?: string; children: React.ReactNode }) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
      <div className="mb-3">
        <h3 className="text-lg font-semibold leading-tight">{title}</h3>
        {subtitle && <p className="text-sm text-gray-500 mt-0.5">{subtitle}</p>}
      </div>
      {children}
    </div>
  );
}

function PetHealthReport({ data }: { data: PetReportData }) {
  const totals = React.useMemo(
    () =>
      data.daily.reduce(
        (acc, d) => ({ bowel: acc.bowel + d.bowel, waterMl: acc.waterMl + d.waterMl, foodG: acc.foodG + d.foodG }),
        { bowel: 0, waterMl: 0, foodG: 0 }
      ),
    [data.daily]
  );
  const shortDate = (s: string) => (s.length >= 10 ? s.slice(5) : s);

  return (
    <div className="space-y-6">
      {/* 요약 */}
      <div className="rounded-2xl border border-gray-200 bg-gradient-to-r from-gray-50 to-white p-5">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold">건강 리포트</h2>
            <p className="text-sm text-gray-500 mt-0.5">
              기간: {data.period.start} ~ {data.period.end}
            </p>
          </div>
          <div className="grid grid-cols-3 gap-3 min-w-[280px]">
            <div className="rounded-xl border border-gray-100 bg-white p-3 text-center">
              <div className="text-xs text-gray-500">총 배변</div>
              <div className="text-xl font-bold">{totals.bowel}</div>
            </div>
            <div className="rounded-xl border border-gray-100 bg-white p-3 text-center">
              <div className="text-xs text-gray-500">총 식수(ml)</div>
              <div className="text-xl font-bold">{totals.waterMl}</div>
            </div>
            <div className="rounded-xl border border-gray-100 bg-white p-3 text-center">
              <div className="text-xs text-gray-500">총 식음(g)</div>
              <div className="text-xl font-bold">{totals.foodG}</div>
            </div>
          </div>
        </div>
      </div>

      {/* 1열: 날짜별 배변 / 진단별 분포 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <SectionCard title="날짜별 배변(횟수)">
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.daily} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tickFormatter={shortDate} />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Legend />
                <Bar dataKey="bowel" name="배변(횟수)" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </SectionCard>

        <SectionCard title="진단별 분포">
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.diagnoses} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Legend />
                <Bar dataKey="count" name="건수" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </SectionCard>
      </div>

      {/* 2열: 식수 / 식음 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <SectionCard title="날짜별 식수(ml)">
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data.daily} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tickFormatter={shortDate} />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="waterMl" name="식수(ml)" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </SectionCard>

        <SectionCard title="날짜별 식음(g)">
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={data.daily} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tickFormatter={shortDate} />
                <YAxis />
                <Tooltip />
                <Legend />
                <Area type="monotone" dataKey="foodG" name="식음(g)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

// ---- Main Page
export default function QRReportPage() {
  const [tab, setTab] = useState<"scan" | "manual">("scan");
  const [code, setCode] = useState("");
  const [report, setReport] = useState<Report | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const lastCodeRef = useRef<string | null>(null);
  const isFetchingRef = useRef(false);

  const isValidCode = useCallback((raw: string) => {
    // Accept anything non-empty; add stricter validation if needed
    return /\S/.test(raw);
  }, []);

  const handleResolve = useCallback(async (raw: string) => {
    if (!raw) return;
    const normalized = raw.trim();
    if (!isValidCode(normalized)) return;

    // Prevent duplicate rapid fetches for the same code
    if (lastCodeRef.current === normalized || isFetchingRef.current) return;

    setError(null);
    setLoading(true);
    isFetchingRef.current = true;
    try {
      const rep = await fetchReportByCode(normalized);
      setReport(rep);
      lastCodeRef.current = normalized;
      setCode(normalized);
      setTab("manual"); // move to manual tab to avoid camera focus issues
    } catch (e: any) {
      setReport(null);
      setError(e?.message || "조회 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
      isFetchingRef.current = false;
    }
  }, [isValidCode]);

  

  const pasteFromClipboard = async () => {
    try {
      const text = await navigator.clipboard.readText();
      if (text) setCode(text.trim());
    } catch (e) {
      // ignore
    }
  };

  const disabled = loading;

  return (
    <div className="min-h-screen bg-gradient-to-b from-[#eef2ff] to-white">
      <div className="mx-auto max-w-5xl px-6 py-8">
        <header className="mb-6">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">QR 코드 레포트 뷰어</h1>
          <p className="text-gray-600 mt-1">QR을 스캔하거나 코드(토큰)를 입력해 레포트를 확인하세요. (테스트 코드: <span className="font-mono">DEMO-123</span>)</p>
        </header>

        {/* Tabs */}
        <div className="inline-flex rounded-xl border border-gray-200 bg-white p-1 mb-6 shadow-sm">
          {(
            [
              { key: "scan", label: "QR 스캔" },
              { key: "manual", label: "코드 입력" },
            ] as const
          ).map((t) => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={
                classNames(
                  "px-4 py-2 text-sm rounded-lg transition",
                  tab === t.key ? "bg-gray-900 text-white" : "hover:bg-gray-50"
                )
              }
            >
              {t.label}
            </button>
          ))}
        </div>

        {/* Content */}
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6 items-start">
          <div className="lg:col-span-3">
            {tab === "scan" ? (
              <div className="rounded-2xl overflow-hidden border border-gray-200 bg-black/90">
                {/* Camera scanner */}
                <Scanner
                  constraints={{ facingMode: "environment" }}
                  onScan={(codes: IDetectedBarcode[]) => {
                    const text = codes?.[0]?.rawValue;
                    if (text) handleResolve(text);
                  }}
                  onError={() => {}}
                  styles={{ container: { width: "100%" }, video: { objectFit: "cover" } }}
                />
              </div>
            ) : (
              <div className="rounded-2xl border border-gray-200 bg-white p-5">
                <label className="block text-sm font-medium text-gray-700 mb-2">코드 또는 URL</label>
                <div className="flex gap-2">
                  <input
                    className="flex-1 rounded-xl border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-gray-400 font-mono"
                    placeholder="예: DEMO-123 또는 https://your.app/r/DEMO-123"
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") handleResolve(codeFromMaybeUrl(code));
                    }}
                  />
                  <button
                    className="rounded-xl px-3 py-2 border border-gray-300 hover:bg-gray-50 text-sm"
                    onClick={pasteFromClipboard}
                    type="button"
                  >
                    붙여넣기
                  </button>
                  <button
                    className="rounded-xl px-4 py-2 bg-gray-900 text-white text-sm disabled:opacity-60"
                    onClick={() => handleResolve(codeFromMaybeUrl(code))}
                    disabled={disabled}
                    type="button"
                  >
                    조회
                  </button>
                </div>
                <p className="text-xs text-gray-500 mt-2">URL 전체를 붙여넣어도 자동으로 코드만 추출해 조회합니다.</p>
              </div>
            )}

            {error && (
              <div className="mt-4 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
                {error}
              </div>
            )}
          </div>

          <aside className="lg:col-span-2">
            <div className="rounded-2xl border border-gray-200 bg-white p-5 sticky top-6">
              <h3 className="text-base font-semibold">사용 팁</h3>
              <ul className="list-disc pl-5 mt-2 space-y-1 text-sm text-gray-700">
                <li>카메라 권한을 허용해야 스캔이 가능합니다.</li>
                <li>빛 반사/초점을 맞추고, 코드가 프레임 중앙에 오도록 해보세요.</li>
                <li>모바일에서 더 안정적입니다. PC는 웹캠이 필요합니다.</li>
                <li>테스트 코드는 <span className="font-mono">DEMO-123</span>입니다.</li>
              </ul>
              <div className="mt-4 text-xs text-gray-500">API_BASE: {API_BASE || "(demo mode)"}</div>
            </div>
          </aside>
        </div>

        {/* Report */}
        <div className="mt-8">
          {loading && (
            <div className="rounded-2xl border border-gray-200 bg-white p-6 text-gray-600">
              조회 중입니다…
            </div>
          )}
          {!loading && report && (
            <>
              <ReportCard report={report} />
              {report.petReport && (
                <div className="mt-6">
                  <PetHealthReport data={report.petReport} />
                </div>
              )}
            </>
          )}
          {!loading && !report && !error && (
            <div className="rounded-2xl border border-dashed border-gray-300 bg-white p-8 text-center text-gray-500">
              스캔 또는 코드를 입력하면 레포트가 여기에 표시됩니다.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// ---- Helpers
function codeFromMaybeUrl(input: string) {
  const trimmed = input.trim();
  // If input is a URL like https://your.app/r/DEMO-123, take the last path segment
  try {
    const u = new URL(trimmed);
    const seg = u.pathname.split("/").filter(Boolean).pop();
    return seg || trimmed;
  } catch {
    return trimmed;
  }
}
