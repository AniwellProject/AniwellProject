"use client";

import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { z } from "zod";
import { Scanner } from "@yudiel/react-qr-scanner";
import type { IDetectedBarcode } from "@yudiel/react-qr-scanner";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  AreaChart,
  Area,
} from "recharts";

/* =========================
   1) Schemas / Types
========================= */
const PetDaySchema = z.object({
  date: z.string(),
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
  intakeSummary: z
    .object({ totalWaterMl: z.number(), totalFoodG: z.number() })
    .optional(),
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
      z.object({ label: z.string(), value: z.union([z.number(), z.string()]) })
    )
    .optional(),
  sections: z
    .array(z.object({ heading: z.string(), content: z.string() }))
    .optional(),
  petReport: PetReportSchema.optional(),
});
export type Report = z.infer<typeof ReportSchema>;

/* =========================
   2) Mock (fallback)
========================= */
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
      { label: "에러 수(24h)", value: 1 },
    ],
    sections: [
      {
        heading: "요약",
        content:
          "시스템 전반 상태는 양호합니다. 보안 패치가 1건 미적용으로 감지되었으며, 적용을 권장합니다.",
      },
      {
        heading: "세부 권장 사항",
        content:
          "방화벽 규칙 점검 및 Node.js LTS 업그레이드를 권장합니다. DB 연결 재시도 로직에 지수 백오프 적용을 고려하세요.",
      },
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
        { date: "2025-08-12", bowel: 1, waterMl: 520, foodG: 100 },
      ],
      diagnoses: [
        { name: "양호", count: 4 },
        { name: "소화불량", count: 2 },
        { name: "설사", count: 1 },
      ],
      intakeSummary: { totalWaterMl: 2900, totalFoodG: 910 },
    },
  },
  "DEMO-456": {
    code: "DEMO-456",
    title: "Weekly Wellness Report",
    createdAt: new Date().toISOString(),
    owner: "Aniwell QA",
    summary:
      "활동량 증가에 따라 수분 섭취가 늘었습니다. 배변 패턴은 안정적입니다.",
    metrics: [
      { label: "산책 횟수(주)", value: 9 },
      { label: "평균 컨디션", value: "좋음" },
    ],
    sections: [
      {
        heading: "관찰 포인트",
        content:
          "더운 날씨에 따라 식수량 증가. 저녁 사료 급여량은 10% 감량 권장.",
      },
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
        { date: "2025-08-19", bowel: 1, waterMl: 540, foodG: 160 },
      ],
      diagnoses: [
        { name: "양호", count: 5 },
        { name: "변비 의심", count: 1 },
        { name: "설사", count: 1 },
      ],
      intakeSummary: { totalWaterMl: 3580, totalFoodG: 1110 },
    },
  },
};

/* =========================
   3) API helpers (8080)
========================= */
const API_BASE =
  (import.meta as any)?.env?.NEXT_PUBLIC_API_BASE ||
  (process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080/api");

async function fetchReportByCode(code: string): Promise<Report> {
  const key = code.trim().toUpperCase();
  // 서버 우선
  try {
    const res = await fetch(`${API_BASE}/reports/${encodeURIComponent(key)}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const json = await res.json();
    return ReportSchema.parse(json);
  } catch {
    // 서버 실패 시 MOCK fallback
    const mock = MOCK_REPORTS[key];
    if (mock) return mock;
    throw new Error(
      "해당 코드의 레포트를 찾을 수 없습니다. (예: DEMO-123 / DEMO-456)"
    );
  }
}

async function fetchAllReports(): Promise<Report[]> {
  // 서버 우선
  try {
    const res = await fetch(`${API_BASE}/reports`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const json = await res.json();
    const arr = z.array(ReportSchema).parse(json);
    // 아무것도 없으면 목으로 채워서 UX 보장
    if (!arr.length) return Object.values(MOCK_REPORTS);
    return arr;
  } catch {
    // 서버 실패 → 데모용
    return Object.values(MOCK_REPORTS);
  }
}

/* =========================
   4) UI helpers
========================= */
function classNames(...xs: Array<string | false | undefined | null>) {
  return xs.filter(Boolean).join(" ");
}
function PrettyDate({ iso }: { iso: string }) {
  const d = new Date(iso);
  const text = isNaN(d.getTime()) ? iso : d.toLocaleString();
  return <span>{text}</span>;
}
function buildShareUrl(code: string) {
  try {
    const { origin, pathname } = window.location;
    return `${origin}${pathname}?code=${encodeURIComponent(code)}`;
  } catch {
    return code;
  }
}
function codeFromMaybeUrl(input: string) {
  const trimmed = input.trim();
  try {
    const u = new URL(trimmed);
    const seg = u.pathname.split("/").filter(Boolean).pop();
    return seg || trimmed;
  } catch {
    return trimmed;
  }
}

/* =========================
   5) Cards / Charts
========================= */
function ReportCard({ report }: { report: Report }) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white shadow-sm overflow-hidden">
      <div className="px-6 py-5 border-b border-gray-100 bg-gradient-to-r from-gray-50 to-white">
        <div className="flex items-center justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold leading-tight">
              {report.title}
            </h2>
            <p className="text-sm text-gray-500 mt-1">
              코드 <span className="font-mono">{report.code}</span> • 생성일{" "}
              <PrettyDate iso={report.createdAt} />
            </p>
          </div>
          <div className="flex gap-2">
            <button
              className="rounded-xl px-3 py-2 text-sm font-medium border border-gray-300 hover:bg-gray-50"
              onClick={() =>
                navigator.clipboard.writeText(buildShareUrl(report.code))
              }
            >
              링크 복사
            </button>
            <button
              className="rounded-xl px-3 py-2 text-sm font-medium border border-gray-300 hover:bg-gray-50"
              onClick={() => window.print()}
            >
              인쇄/저장
            </button>
          </div>
        </div>
      </div>

      {report.summary && (
        <div className="px-6 pt-5">
          <div className="text-base leading-relaxed">{report.summary}</div>
        </div>
      )}

      {report.metrics?.length ? (
        <div className="px-6 pb-1 pt-5 grid grid-cols-1 sm:grid-cols-3 gap-4">
          {report.metrics.map((m, i) => (
            <div
              key={i}
              className="rounded-xl border border-gray-100 p-4 bg-gray-50"
            >
              <div className="text-xs text-gray-500">{m.label}</div>
              <div className="mt-1 text-2xl font-bold">{m.value}</div>
            </div>
          ))}
        </div>
      ) : null}

      {report.sections?.length ? (
        <div className="px-6 py-5 space-y-6">
          {report.sections.map((s, i) => (
            <section key={i}>
              <h3 className="text-lg font-semibold mb-1">{s.heading}</h3>
              <p className="text-gray-700 leading-relaxed whitespace-pre-wrap">
                {s.content}
              </p>
            </section>
          ))}
        </div>
      ) : null}
    </div>
  );
}

function SectionCard({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
}) {
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
  const totals = useMemo(
    () =>
      data.daily.reduce(
        (acc, d) => ({
          bowel: acc.bowel + d.bowel,
          waterMl: acc.waterMl + d.waterMl,
          foodG: acc.foodG + d.foodG,
        }),
        { bowel: 0, waterMl: 0, foodG: 0 }
      ),
    [data.daily]
  );
  const shortDate = (s: string) => (s.length >= 10 ? s.slice(5) : s);

  return (
    <div className="space-y-6">
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

      {/* 1열 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <SectionCard title="날짜별 배변(횟수)">
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={data.daily}
                margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
              >
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
              <BarChart
                data={data.diagnoses}
                margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
              >
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

      {/* 2열 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <SectionCard title="날짜별 식수(ml)">
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={data.daily}
                margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tickFormatter={shortDate} />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="waterMl"
                  name="식수(ml)"
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </SectionCard>

        <SectionCard title="날짜별 식음(g)">
          <div className="h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart
                data={data.daily}
                margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
              >
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

/* =========================
   6) Main (One-Page, Vertical)
========================= */
export default function OnePageReportList() {
  const [allReports, setAllReports] = useState<Report[]>([]);
  const [loadingList, setLoadingList] = useState(true);
  const [errorList, setErrorList] = useState<string | null>(null);

  const [code, setCode] = useState("");
  const [querying, setQuerying] = useState(false);
  const [manualResult, setManualResult] = useState<Report | null>(null);
  const [manualError, setManualError] = useState<string | null>(null);

  // 초기: 전체 목록 로드
  useEffect(() => {
    (async () => {
      setLoadingList(true);
      setErrorList(null);
      try {
        const arr = await fetchAllReports();
        setAllReports(arr);
      } catch (e: any) {
        setErrorList(e?.message || "전체 레포트를 불러오지 못했습니다.");
      } finally {
        setLoadingList(false);
      }
    })();
  }, []);

  const handleResolve = useCallback(async (raw: string) => {
    const normalized = codeFromMaybeUrl(raw);
    if (!/\S/.test(normalized)) return;

    setManualError(null);
    setManualResult(null);
    setQuerying(true);
    try {
      const rep = await fetchReportByCode(normalized);
      setManualResult(rep);
      // 화면 상단으로 스크롤(조회 결과 바로 보이도록)
      requestAnimationFrame(() =>
        window.scrollTo({ top: 0, behavior: "smooth" })
      );
    } catch (e: any) {
      setManualError(e?.message ?? "조회 실패");
    } finally {
      setQuerying(false);
    }
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-b from-[#eef2ff] to-white">
      <div className="mx-auto max-w-5xl px-6 py-8">
        {/* 헤더 */}
        <header className="mb-6">
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">
            QR 코드 레포트(세로 스크롤 단일 페이지)
          </h1>
          <p className="text-gray-600 mt-1">
            8080 서버에서 받아 전체 레포트를 한 페이지에서 확인합니다. 필요 시
            코드로 단건 조회하세요.
          </p>
        </header>

        {/* 코드 입력(단건 조회) */}
        <div className="rounded-2xl border border-gray-200 bg-white p-5 mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            코드 또는 URL
          </label>
          <div className="flex gap-2">
            <input
              className="flex-1 rounded-xl border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-gray-400 font-mono"
              placeholder="예: DEMO-123 또는 https://your.app/r/DEMO-123"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") handleResolve(code);
              }}
            />
            <button
              className="rounded-xl px-4 py-2 bg-gray-900 text-white text-sm disabled:opacity-60"
              onClick={() => handleResolve(code)}
              disabled={querying}
              type="button"
            >
              조회
            </button>
          </div>
          {manualError && (
            <div className="mt-3 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-800">
              {manualError}
            </div>
          )}
        </div>

        {/* (접힘) QR 스캔 보조 */}
        <details className="mb-6 rounded-2xl border border-gray-200 bg-white p-5">
          <summary className="cursor-pointer text-sm font-semibold">
            QR 스캔 열기(보조)
          </summary>
          <div className="mt-4 rounded-xl overflow-hidden border border-gray-200 bg-black/90">
            <Scanner
              constraints={{ facingMode: "environment" }}
              onScan={(codes: IDetectedBarcode[]) => {
                const text = codes?.[0]?.rawValue;
                if (text) handleResolve(text);
              }}
              onError={() => {}}
              styles={{
                container: { width: "100%" },
                video: { objectFit: "cover" },
              }}
            />
          </div>
          <p className="text-xs text-gray-500 mt-2">
            모바일 권장. PC는 웹캠 필요.
          </p>
        </details>

        {/* 단건 조회가 있으면 최상단에 표시 */}
        {manualResult && (
          <div className="mb-10">
            <ReportCard report={manualResult} />
            {manualResult.petReport && (
              <div className="mt-6">
                <PetHealthReport data={manualResult.petReport} />
              </div>
            )}
          </div>
        )}

        {/* 전체 레포트 리스트 (세로 스크롤) */}
        <section>
          <h2 className="text-xl font-semibold mb-3">전체 레포트</h2>

          {loadingList && (
            <div className="rounded-2xl border border-gray-200 bg-white p-6 text-gray-600">
              불러오는 중…
            </div>
          )}
          {errorList && (
            <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-800 text-sm">
              {errorList}
            </div>
          )}

          {!loadingList && !errorList && (
            <div className="space-y-10">
              {allReports.map((r) => (
                <div key={r.code}>
                  <ReportCard report={r} />
                  {r.petReport && (
                    <div className="mt-6">
                      <PetHealthReport data={r.petReport} />
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </section>

        {/* 하단 여백 */}
        <div className="h-12" />
      </div>
    </div>
  );
}
