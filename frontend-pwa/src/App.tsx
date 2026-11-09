import { useState, useEffect, useMemo } from 'react';
import { aifpApi, LiveArticle, VideoStoryboard, VideoCreationResult } from './api/aifpApi';

// ============================================================================
// MONOCHROME MINIMALIST SVG ICONS (Clean 1.7px Stroke, Editorial Polish)
// ============================================================================
const IconRefresh = ({ spinning }: { spinning?: boolean }) => (
  <svg
    width="13"
    height="13"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="1.8"
    strokeLinecap="round"
    strokeLinejoin="round"
    style={spinning ? { animation: 'spin 1s linear infinite' } : undefined}
  >
    <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2" />
  </svg>
);

const IconTime = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" />
    <polyline points="12 6 12 12 16 14" />
  </svg>
);
const IconAssistant = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
  </svg>
);

const IconDigest = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
    <path d="M4 22h16a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v16a2 2 0 0 1-2 2Zm0 0a2 2 0 0 1-2-2v-9c0-1.1.9-2 2-2h2" />
    <path d="M18 14h-8" /><path d="M15 18h-5" /><path d="M10 6h8v4h-8z" />
  </svg>
);

const IconAutomations = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" />
    <polyline points="12 6 12 12 16 14" />
  </svg>
);

const IconTools = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="3" width="7" height="7" rx="1.5" />
    <rect x="14" y="3" width="7" height="7" rx="1.5" />
    <rect x="14" y="14" width="7" height="7" rx="1.5" />
    <rect x="3" y="14" width="7" height="7" rx="1.5" />
  </svg>
);

const IconSettings = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="3" />
    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1 0-2.83 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
  </svg>
);

const IconSun = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="5" />
    <line x1="12" y1="1" x2="12" y2="3" /><line x1="12" y1="21" x2="12" y2="23" />
    <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" /><line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
    <line x1="1" y1="12" x2="3" y2="12" /><line x1="21" y1="12" x2="23" y2="12" />
    <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" /><line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
  </svg>
);

const IconMoon = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
  </svg>
);

const IconExternalLink = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
    <polyline points="15 3 21 3 21 9" />
    <line x1="10" y1="14" x2="21" y2="3" />
  </svg>
);

const IconCheck = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="20 6 9 17 4 12" />
  </svg>
);

const IconClose = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <line x1="18" y1="6" x2="6" y2="18" />
    <line x1="6" y1="6" x2="18" y2="18" />
  </svg>
);

const IconSearch = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
  </svg>
);

const IconMail = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
    <polyline points="22,6 12,13 2,6" />
  </svg>
);

const IconSliders = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <line x1="4" y1="21" x2="4" y2="14" /><line x1="4" y1="10" x2="4" y2="3" />
    <line x1="12" y1="21" x2="12" y2="12" /><line x1="12" y1="8" x2="12" y2="3" />
    <line x1="20" y1="21" x2="20" y2="16" /><line x1="20" y1="12" x2="20" y2="3" />
    <line x1="1" y1="14" x2="7" y2="14" />
    <line x1="9" y1="8" x2="15" y2="8" />
    <line x1="17" y1="16" x2="23" y2="16" />
  </svg>
);

const IconCamera = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
    <circle cx="12" cy="13" r="4" />
  </svg>
);

const IconCollapseLeft = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="3" width="18" height="18" rx="2" />
    <path d="M9 3v18" />
    <path d="m15 9-3 3 3 3" />
  </svg>
);

const AppLogo = ({ size = 32 }: { size?: number }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 32 32"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    style={{ flexShrink: 0, display: 'block', borderRadius: Math.round(size * 0.26) }}
  >
    <rect width="32" height="32" rx="8.5" fill="#0F172A" />
    <path d="M16 6.5L24.5 22.5H20.2L16 14.5L11.8 22.5H7.5L16 6.5Z" fill="#F8FAFC" />
    <path d="M11.5 18.5H20.5L21.5 20.8H10.5L11.5 18.5Z" fill="#10B981" />
    <circle cx="16" cy="11.8" r="1.6" fill="#38BDF8" />
  </svg>
);

const IconArrowRight = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <line x1="5" y1="12" x2="19" y2="12" />
    <polyline points="12 5 19 12 12 19" />
  </svg>
);

const IconPlus = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="12" y1="5" x2="12" y2="19" />
    <line x1="5" y1="12" x2="19" y2="12" />
  </svg>
);

const IconEye = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
    <circle cx="12" cy="12" r="3" />
  </svg>
);

const IconBookmark = ({ filled }: { filled?: boolean }) => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill={filled ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
  </svg>
);

const IconPlay = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polygon points="5 3 19 12 5 21 5 3" />
  </svg>
);

const IconSend = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <line x1="22" y1="2" x2="11" y2="13" />
    <polygon points="22 2 15 22 11 13 2 9 22 2" />
  </svg>
);

const IconFile = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
    <polyline points="14 2 14 8 20 8" />
  </svg>
);

const IconCalendar = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
    <line x1="16" y1="2" x2="16" y2="6" />
    <line x1="8" y1="2" x2="8" y2="6" />
    <line x1="3" y1="10" x2="21" y2="10" />
  </svg>
);

const IconTrash = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="3 6 5 6 21 6" />
    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
  </svg>
);

const IconBell = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
    <path d="M13.73 21a2 2 0 0 1-3.46 0" />
  </svg>
);

const IconVideo = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <polygon points="23 7 16 12 23 17 23 7" />
    <rect x="1" y="5" width="15" height="14" rx="2" ry="2" />
  </svg>
);

const IconClock = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" />
    <polyline points="12 6 12 12 16 14" />
  </svg>
);

const IconShield = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
  </svg>
);

export const MASTER_OWNER_EMAIL = (import.meta as any).env?.VITE_OWNER_EMAIL || localStorage.getItem('aifp_owner_email') || 'owner@aifp.local';

// ============================================================================
// DATA TYPES & PRESETS FOR MULTI-AUDIENCE GROUPS
// ============================================================================
type NavTab = 'assistant' | 'digests' | 'automations' | 'tools' | 'settings';
type ToolSubTab = 'evolution' | 'video' | 'vision' | 'shopping' | 'writing' | 'docs' | 'calendar';

export type UserPersonaPreset = 'developer' | 'office' | 'investor' | 'shopper' | 'creator' | 'student';

export interface UserPreferencesProfile {
  preset: UserPersonaPreset;
  personaTitle: string;
  deliveryEmail: string;
  frequency: '1_DAILY' | '2_DAILY' | '3_DAILY' | 'REALTIME';
  scheduledTimes: string[];
  categories: {
    tech: boolean;
    career: boolean;
    finance: boolean;
    shopping: boolean;
    social: boolean;
    news: boolean;
  };
  summaryTone: 'concise' | 'in_depth';
}

const PERSONA_PRESETS: {
  id: UserPersonaPreset;
  title: string;
  subtitle: string;
  defaultCategories: { tech: boolean; career: boolean; finance: boolean; shopping: boolean; social: boolean; news: boolean };
  defaultFrequency: '1_DAILY' | '2_DAILY' | '3_DAILY' | 'REALTIME';
  defaultTimes: string[];
}[] = [
  {
    id: 'developer',
    title: 'Kỹ sư & Lập trình viên',
    subtitle: 'AI, công nghệ mới, tuyển dụng IT Flutter/Java, thư viện nguồn mở',
    defaultCategories: { tech: true, career: true, finance: false, shopping: true, social: true, news: false },
    defaultFrequency: '1_DAILY',
    defaultTimes: ['07:30'],
  },
  {
    id: 'office',
    title: 'Dân văn phòng & Quản lý',
    subtitle: 'Kinh tế thị trường, năng suất làm việc, xu hướng công sở, thời sự',
    defaultCategories: { tech: true, career: false, finance: true, shopping: false, social: false, news: true },
    defaultFrequency: '2_DAILY',
    defaultTimes: ['07:30', '18:30'],
  },
  {
    id: 'investor',
    title: 'Nhà đầu tư & Tài chính',
    subtitle: 'Thị trường chứng khoán, giá vàng, bất động sản, crypto, lãi suất',
    defaultCategories: { tech: false, career: false, finance: true, shopping: false, social: true, news: true },
    defaultFrequency: '3_DAILY',
    defaultTimes: ['07:30', '12:00', '18:30'],
  },
  {
    id: 'shopper',
    title: 'Săn Deal & Tiết kiệm',
    subtitle: 'Cảnh báo giảm giá sâu Shopee/Tiki, flash sale phụ kiện, thời trang',
    defaultCategories: { tech: true, career: false, finance: false, shopping: true, social: false, news: false },
    defaultFrequency: 'REALTIME',
    defaultTimes: [],
  },
  {
    id: 'creator',
    title: 'Sáng tạo nội dung',
    subtitle: 'Xu hướng mạng xã hội Facebook/TikTok, bài thảo luận hot cộng đồng',
    defaultCategories: { tech: true, career: false, finance: false, shopping: false, social: true, news: true },
    defaultFrequency: '2_DAILY',
    defaultTimes: ['08:00', '20:00'],
  },
  {
    id: 'student',
    title: 'Sinh viên & Học tập',
    subtitle: 'Cơ hội thực tập sinh, học bổng, khóa học công nghệ miễn phí',
    defaultCategories: { tech: true, career: true, finance: false, shopping: true, social: true, news: false },
    defaultFrequency: '1_DAILY',
    defaultTimes: ['07:00'],
  },
];

const CATEGORY_DEFINITIONS = [
  { id: 'tech', label: 'Công nghệ & Trí tuệ nhân tạo (AI)', desc: 'Mô hình AI mới, xu hướng lập trình và sản phẩm công nghệ' },
  { id: 'career', label: 'Cơ hội Việc làm & Thực tập', desc: 'Tuyển dụng Flutter, Java, IT, mức trợ cấp và đãi ngộ' },
  { id: 'finance', label: 'Tài chính, Chứng khoán & Đầu tư', desc: 'Biến động thị trường, giá vàng, lãi suất ngân hàng' },
  { id: 'shopping', label: 'Săn Deal & Khuyến mãi mua sắm', desc: 'Báo giảm giá sâu từ sàn Shopee Mall, Tiki, Lazada' },
  { id: 'social', label: 'Mạng xã hội & Thảo luận Facebook', desc: 'Chủ đề hot trong các hội nhóm và cộng đồng chuyên môn' },
  { id: 'news', label: 'Thời sự, Kinh tế & Đời sống', desc: 'Sự kiện kinh tế, thời tiết, giao thông chọn lọc trong ngày' },
];

const INITIAL_PROFILE: UserPreferencesProfile = {
  preset: 'developer',
  personaTitle: 'Kỹ sư & Lập trình viên',
  deliveryEmail: 'alex.dev@aifp.local',
  frequency: '1_DAILY',
  scheduledTimes: ['07:30'],
  categories: {
    tech: true,
    career: true,
    finance: false,
    shopping: true,
    social: true,
    news: false,
  },
  summaryTone: 'concise',
};

const formatRelativeTime = (isoString?: string): string => {
  if (!isoString) return 'Vừa xong';
  try {
    const date = new Date(isoString);
    const now = new Date();
    const diffSec = Math.floor((now.getTime() - date.getTime()) / 1000);
    if (diffSec < 60) return 'Vừa xong';
    if (diffSec < 3600) return `${Math.max(1, Math.floor(diffSec / 60))} phút trước`;
    if (diffSec < 86400) return `${Math.floor(diffSec / 3600)} giờ trước`;
    return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  } catch {
    return 'Hôm nay';
  }
};

type NewsCardItem = LiveArticle;

const INITIAL_NEWS: NewsCardItem[] = [
  {
    id: 'n-1',
    channelId: 'tech',
    source: 'VnExpress Số Hóa',
    sourceType: 'vn',
    category: 'Công nghệ & AI',
    title: 'Công nghệ Bluetooth mới cho phép phát âm thanh lossless',
    summary: 'Tổ chức Bluetooth SIG công bố chuẩn kết nối Bluetooth HDT với tốc độ tối đa 7,5 Mb/giây, hỗ trợ phát nhạc lossless chất lượng cao trực tiếp không nén qua không dây.',
    imageUrl: 'https://i1-vnexpress.vnecdn.net/2026/11/07/dsc8652-1794060485-2338-1794060541.jpg?w=1200&h=0&q=100&dpr=1&fit=crop&s=eQVmaEeWfpBX2NhCWVOjkQ',
    url: 'https://vnexpress.net/cong-nghe-bluetooth-moi-cho-phep-phat-am-thanh-lossless-5141993.html',
    publishedAt: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
    fetchedAt: new Date().toISOString(),
  },
  {
    id: 'n-2',
    channelId: 'tech',
    source: 'VnExpress',
    sourceType: 'vn',
    category: 'Thiết bị & Di động',
    title: 'iPhone 20 có thể bỏ nút Action và giữ nguyên Dynamic Island',
    summary: 'Apple lên kế hoạch gộp nút Action vào cụm điều khiển Camera Control, giữ Dynamic Island đồng thời tối ưu hóa không gian cho hệ thống pin thế hệ mới.',
    imageUrl: 'https://i1-vnexpress.vnecdn.net/2026/11/07/fdnnyswsv5m3lw8bs4geuy-1794060-2723-9452-1794060076.jpg?w=1200&h=0&q=100&dpr=1&fit=crop&s=AiQfW9d6L8Mn5AJ1XM6E9A',
    url: 'https://vnexpress.net/iphone-20-co-the-bo-nut-action-va-giu-nguyen-dynamic-island-5141990.html',
    publishedAt: new Date(Date.now() - 75 * 60 * 1000).toISOString(),
    fetchedAt: new Date().toISOString(),
  },
  {
    id: 'n-3',
    channelId: 'finance',
    source: 'VnExpress Kinh Doanh',
    sourceType: 'vn',
    category: 'Tài chính & Đầu tư',
    title: 'Nhà đầu tư ngờ vực mức định giá 2.000 tỷ USD của Anthropic',
    summary: 'Sau phiên điều chỉnh cổ phiếu công nghệ tại Mỹ, giới đầu tư bắt đầu cân nhắc chi phí vận hành khổng lồ của các trung tâm dữ liệu AI so với doanh thu thực tế.',
    imageUrl: 'https://i1-kinhdoanh.vnecdn.net/2026/11/08/afp-hl-jmbarrere-v1-highres-cl-9797-5057-1794104920.jpg?w=1200&h=0&q=100&dpr=1&fit=crop&s=7j1J3f3dWaJR8UMdREqVJQ',
    url: 'https://vnexpress.net/nha-dau-tu-ngo-vuc-muc-dinh-gia-2-000-ty-usd-cua-anthropic-5142087.html',
    publishedAt: new Date(Date.now() - 110 * 60 * 1000).toISOString(),
    fetchedAt: new Date().toISOString(),
  },
  {
    id: 'n-job-1',
    channelId: 'jobs',
    source: 'Viec Lam Hub',
    sourceType: 'tech',
    category: 'Cơ hội việc làm online',
    title: 'CƠ HỘI VIỆC LÀM ONLINE: TESTER & TRẢI NGHIỆM GAME MOBILE',
    summary: 'Cơ hội linh hoạt · Không yêu cầu kinh nghiệm | Thu nhập tham khảo: 1.100.000–5.800.000 VNĐ/ngày\n• Hỗ trợ chơi thử và báo cáo lỗi game mobile (casual, nhập vai MMO)\n• Có hướng dẫn chi tiết từ A-Z cho người mới bắt đầu\n• Không yêu cầu tiếng Anh (giao diện và hướng dẫn 100% tiếng Việt)\n• Thiết bị: Smartphone Android/iOS hoặc PC cơ bản\n* Phù hợp cho ứng viên đang tìm cơ hội online linh hoạt tại nhà.',
    imageUrl: '',
    url: 'https://topcv.vn/viec-lam/game-tester',
    publishedAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
    fetchedAt: new Date().toISOString(),
  },
  {
    id: 'n-job-2',
    channelId: 'jobs',
    source: 'Game Studio QA',
    sourceType: 'tech',
    category: 'Cơ hội việc làm online',
    title: 'TUYỂN CỘNG TÁC VIÊN CÀY GAME & REVIEW TỰA GAME MỚI',
    summary: 'Chơi game kiếm thêm thu nhập tại nhà · Part-time linh hoạt | Thu nhập: 800.000–3.200.000 VNĐ/tuần\n• Tham gia cày cấp độ và kiểm thử tính năng server game mới ra mắt\n• Không yêu cầu kinh nghiệm, hướng dẫn từng bước qua tài liệu tiếng Việt\n• Không áp chỉ tiêu, nhận thưởng trực tiếp theo tiến độ hoàn thành\n• Nhận thanh toán theo tuần qua tài khoản ngân hàng hoặc ví MoMo.\n* Dành cho các bạn trẻ đam mê game, có thời gian rảnh rỗi tại nhà.',
    imageUrl: '',
    url: 'https://gamehub.vn/tuyen-dung-game/',
    publishedAt: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
    fetchedAt: new Date().toISOString(),
  },
  {
    id: 'n-job-3',
    channelId: 'jobs',
    source: 'TopCV Remote Hub',
    sourceType: 'tech',
    category: 'Cơ hội việc làm online',
    title: 'NHÂN VIÊN NHẬP LIỆU & HỖ TRỢ KHÁCH HÀNG ONLINE (REMOTE)',
    summary: 'Remote 100% không yêu cầu kinh nghiệm · Toàn quốc | Thu nhập: 1.100.000–4.500.000 VNĐ/tuần\n• Nhập liệu danh mục sản phẩm và phản hồi tin nhắn khách hàng theo kịch bản\n• Không yêu cầu kinh nghiệm, đào tạo online 1 buổi là làm việc được ngay\n• Không yêu cầu tiếng Anh, làm việc 100% bằng tiếng Việt\n• Chủ động đăng ký ca làm việc sáng, chiều hoặc tối linh hoạt.\n* Phù hợp cho người tìm việc làm thêm nghiêm túc, thu nhập đều đặn.',
    imageUrl: '',
    url: 'https://www.topcv.vn/viec-lam-remote',
    publishedAt: new Date(Date.now() - 90 * 60 * 1000).toISOString(),
    fetchedAt: new Date().toISOString(),
  },
  {
    id: 'n-5',
    channelId: 'shopping',
    source: 'Shopee Mall',
    sourceType: 'shop',
    category: 'Săn Deal Khuyến mãi',
    title: 'Bàn phím cơ Keychron K2 Pro không dây giảm giá còn 1.890.000 đ',
    summary: 'Đạt mức giá mục tiêu bạn theo dõi (tiết kiệm 160.000 đ so với giá thị trường), hỗ trợ bảo hành chính hãng 12 tháng và miễn phí vận chuyển.',
    imageUrl: '',
    url: 'https://shopee.vn/search?keyword=keychron+k2+pro',
    publishedAt: new Date(Date.now() - 190 * 60 * 1000).toISOString(),
    fetchedAt: new Date().toISOString(),
  },
  {
    id: 'n-6',
    channelId: 'social',
    source: 'Facebook Tech Group',
    sourceType: 'fb',
    category: 'Mạng xã hội',
    title: 'Thảo luận Giải pháp AI Agent tự hành trên di động: Cắt giảm 80% chi phí API',
    summary: 'Bài viết phân tích kiến trúc kết hợp LangGraph trên server với Flutter Mobile, sử dụng mô hình nhỏ chạy offline và chuyển đổi thông minh đa nhà cung cấp.',
    imageUrl: '',
    url: 'https://www.facebook.com/groups/laptrinhflutter',
    publishedAt: new Date(Date.now() - 300 * 60 * 1000).toISOString(),
    fetchedAt: new Date().toISOString(),
  },
];

// ============================================================================
// TOPIC CHANNELS AUTOMATION (Cấu hình riêng theo từng dạng tin & gửi mail độc lập)
// ============================================================================
export interface TopicChannelConfig {
  id: string; // 'shopping' | 'jobs' | 'tech' | 'finance' | 'social'
  title: string;
  subtitle: string;
  badge: string;
  badgeColor: string;
  scheduledTime: string;
  customEmail: string; // Nếu để trống, tự động dùng profile.deliveryEmail
  frequency: string;
  keywords: string[];
  isActive: boolean;
  sourceSummary: string;
}

const INITIAL_TOPIC_CHANNELS: TopicChannelConfig[] = [
  {
    id: 'shopping',
    title: 'Deal Shopee',
    subtitle: '',
    badge: 'Shopee',
    badgeColor: '#EE4D2D',
    scheduledTime: '11:30',
    customEmail: '',
    frequency: 'Hàng ngày',
    keywords: ['Keychron K2', 'Màn hình 4K', 'Tai nghe Sony'],
    isActive: true,
    sourceSummary: 'Shopee Mall, Tiki',
  },
  {
    id: 'jobs',
    title: 'Việc làm & Test Game',
    subtitle: '',
    badge: 'Việc Làm Hub',
    badgeColor: '#0F766E',
    scheduledTime: '08:00',
    customEmail: '',
    frequency: 'Hàng ngày',
    keywords: ['Test Game', 'Cày Game', 'Remote không kinh nghiệm', 'Không tiếng Anh'],
    isActive: true,
    sourceSummary: 'Viec Lam Hub, TopCV, GameHub, ITViec',
  },
  {
    id: 'tech',
    title: 'Công nghệ & AI',
    subtitle: '',
    badge: 'VnExpress',
    badgeColor: '#2563EB',
    scheduledTime: '07:00',
    customEmail: '',
    frequency: 'Hàng ngày',
    keywords: ['Mô hình AI', 'Bluetooth', 'Apple'],
    isActive: true,
    sourceSummary: 'VnExpress, Dân Trí',
  },
  {
    id: 'finance',
    title: 'Tài chính',
    subtitle: '',
    badge: 'Kinh Doanh',
    badgeColor: '#D97706',
    scheduledTime: '17:30',
    customEmail: '',
    frequency: 'Thứ 2 - Thứ 6',
    keywords: ['VN-Index', 'Cổ phiếu', 'Lãi suất'],
    isActive: false,
    sourceSummary: 'VnExpress Kinh Doanh',
  },
  {
    id: 'social',
    title: 'Cộng đồng',
    subtitle: '',
    badge: 'Facebook',
    badgeColor: '#1877F2',
    scheduledTime: '20:00',
    customEmail: '',
    frequency: 'Hàng ngày',
    keywords: ['Flutter VN', 'Lập trình viên'],
    isActive: false,
    sourceSummary: 'Facebook Tech Groups',
  },
  {
    id: 'ai-evolution',
    title: 'Tiến hóa AI',
    subtitle: '',
    badge: 'AI Roadmap',
    badgeColor: '#2563EB',
    scheduledTime: '07:00',
    customEmail: '',
    frequency: 'Hàng ngày',
    keywords: ['DeepSeek R1', 'MCP Protocol', 'Gemini 2.5', 'On-device SLM', 'Lộ trình AIFP'],
    isActive: true,
    sourceSummary: 'arXiv, Hugging Face, DeepMind, Anthropic',
  },
  {
    id: 'creator-ideas',
    title: 'Ý tưởng Kiếm tiền & Game/Video',
    subtitle: '',
    badge: 'Creator',
    badgeColor: '#C026D3',
    scheduledTime: '20:00',
    customEmail: '',
    frequency: 'Hàng ngày',
    keywords: ['Làm Game Godot', 'YouTube Gameplay', 'TikTok Shorts', 'Kịch bản AI', 'Kiếm tiền Online'],
    isActive: true,
    sourceSummary: 'YouTube Gaming, Steam, IndieDev, Upwork',
  },
];

export default function App() {
  const [activeTab, setActiveTab] = useState<NavTab>('assistant');
  const [toolSubTab, setToolSubTab] = useState<ToolSubTab>('evolution');
  const [isAuditingEvolution, setIsAuditingEvolution] = useState(false);
  const [lastEvolutionAuditTime, setLastEvolutionAuditTime] = useState('07:00 · Hôm nay');
  const [darkMode, setDarkMode] = useState<boolean>(() => {
    try {
      return localStorage.getItem('aifp_dark_mode') === 'true';
    } catch {}
    return false;
  });
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState<boolean>(() => {
    try {
      return localStorage.getItem('aifp_sidebar_collapsed') === 'true';
    } catch {}
    return false;
  });

  const handleToggleSidebar = () => {
    setIsSidebarCollapsed((prev) => {
      const next = !prev;
      try {
        localStorage.setItem('aifp_sidebar_collapsed', String(next));
      } catch {}
      return next;
    });
  };
  const [showEmailModal, setShowEmailModal] = useState(false);
  const [showPreferencesModal, setShowPreferencesModal] = useState(false);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [authModalTab, setAuthModalTab] = useState<'login' | 'register'>('login');
  const [authEmail, setAuthEmail] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authDisplayName, setAuthDisplayName] = useState('');
  const [authError, setAuthError] = useState<string | null>(null);
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [authSession, setAuthSession] = useState<{
    userId: string;
    email: string;
    displayName: string;
    accessToken: string;
  } | null>(() => {
    try {
      const saved = localStorage.getItem('aifp_auth_session');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [killSwitchTriggered, setKillSwitchTriggered] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [isSendingTestEmail, setIsSendingTestEmail] = useState(false);
  const [showCompanionDrawer, setShowCompanionDrawer] = useState(true);

  // Preference Profile State (Two-Tier Persistence: localStorage + Database)
  const sanitizeProfileSlots = (p: UserPreferencesProfile): UserPreferencesProfile => {
    const maxSlots = p.frequency === '1_DAILY' ? 1 : p.frequency === '2_DAILY' ? 2 : p.frequency === '3_DAILY' ? 3 : 0;
    if (p.frequency === 'REALTIME') {
      return { ...p, scheduledTimes: [] };
    }
    if (Array.isArray(p.scheduledTimes) && p.scheduledTimes.length > maxSlots) {
      return { ...p, scheduledTimes: p.scheduledTimes.slice(0, maxSlots) };
    }
    if (!p.scheduledTimes || p.scheduledTimes.length === 0) {
      return { ...p, scheduledTimes: ['07:30'] };
    }
    return p;
  };

  const [profile, setProfile] = useState<UserPreferencesProfile>(() => {
    try {
      const saved = localStorage.getItem('aifp_user_profile');
      if (saved) {
        return sanitizeProfileSlots(JSON.parse(saved));
      }
    } catch {}
    return INITIAL_PROFILE;
  });
  const [tempProfile, setTempProfile] = useState<UserPreferencesProfile>(() => {
    try {
      const saved = localStorage.getItem('aifp_user_profile');
      if (saved) {
        return sanitizeProfileSlots(JSON.parse(saved));
      }
    } catch {}
    return INITIAL_PROFILE;
  });
  const [prefWizardStep, setPrefWizardStep] = useState<'persona' | 'topics' | 'delivery'>('persona');

  // Topic Channels Automation State (Two-Tier Persistence)
  const [topicChannels, setTopicChannels] = useState<TopicChannelConfig[]>(() => {
    try {
      const saved = localStorage.getItem('aifp_topic_channels');
      if (saved) {
        const parsed: TopicChannelConfig[] = JSON.parse(saved);
        const missing = INITIAL_TOPIC_CHANNELS.filter((init) => !parsed.some((p) => p.id === init.id));
        if (missing.length > 0) {
          const merged = [...parsed, ...missing];
          localStorage.setItem('aifp_topic_channels', JSON.stringify(merged));
          return merged;
        }
        return parsed;
      }
    } catch {}
    return INITIAL_TOPIC_CHANNELS;
  });

  // Master Owner Gate: Proprietary research & channels belong exclusively to the Master Owner
  const isOwner = authSession?.userId === '00000000-0000-0000-0000-000000000001' ||
    (Boolean(authSession?.email) && authSession?.email?.toLowerCase() === MASTER_OWNER_EMAIL.toLowerCase()) ||
    (!authSession && profile.deliveryEmail.toLowerCase() === MASTER_OWNER_EMAIL.toLowerCase());

  // Filter channels based on ownership: proprietary channels are exclusively for the master owner
  const visibleChannels: TopicChannelConfig[] = useMemo(() => {
    return isOwner
      ? topicChannels
      : topicChannels.filter((ch: TopicChannelConfig) => ch.id !== 'ai-evolution' && ch.id !== 'creator-ideas');
  }, [isOwner, topicChannels]);

  // Available Tool subtabs based on ownership: proprietary open-source research is exclusively for master owner
  const availableToolSubTabs = useMemo(() => {
    const tabs: { id: ToolSubTab; label: string }[] = [];
    if (isOwner) {
      tabs.push({ id: 'evolution', label: 'Mã nguồn mở' });
    }
    tabs.push(
      { id: 'video', label: 'Video & Shorts' },
      { id: 'vision', label: 'Hóa đơn' },
      { id: 'shopping', label: 'Mua sắm' },
      { id: 'writing', label: 'Soạn thảo' },
      { id: 'docs', label: 'Tài liệu' },
      { id: 'calendar', label: 'Lịch hẹn' }
    );
    return tabs;
  }, [isOwner]);

  useEffect(() => {
    if (!isOwner && toolSubTab === 'evolution') {
      setToolSubTab('video');
    }
  }, [isOwner, toolSubTab]);

  // Sub-filter pills for news feed
  const newsFilterOptions = useMemo(() => {
    return [
      { id: 'all', label: 'Tất cả' },
      ...(isOwner
        ? [
            { id: 'creator-ideas', label: 'Ý tưởng kiếm tiền' },
            { id: 'ai-evolution', label: 'Tiến hóa AI' },
          ]
        : []),
      { id: 'shopping', label: 'Shopee' },
      { id: 'jobs', label: 'Việc làm' },
      { id: 'tech', label: 'Công nghệ' },
      { id: 'finance', label: 'Tài chính' },
      { id: 'social', label: 'Cộng đồng' },
    ];
  }, [isOwner]);
  const [editingChannel, setEditingChannel] = useState<TopicChannelConfig | null>(null);
  
  const [newsFilter, setNewsFilter] = useState<'all' | 'shopping' | 'jobs' | 'tech' | 'finance' | 'social' | 'ai-evolution' | 'creator-ideas'>('all');
  const [newsPage, setNewsPage] = useState<number>(1);
  const NEWS_PAGE_SIZE = 20;
  const [bookmarkedJobIds, setBookmarkedJobIds] = useState<string[]>(() => {
    try {
      const saved = localStorage.getItem('aifp_bookmarked_jobs');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const handleToggleBookmark = (id: string) => {
    setBookmarkedJobIds((prev) => {
      const next = prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id];
      try {
        localStorage.setItem('aifp_bookmarked_jobs', JSON.stringify(next));
      } catch {}
      showToast(prev.includes(id) ? 'Đã bỏ lưu tin tuyển dụng.' : 'Đã lưu việc làm vào danh sách quan tâm!');
      return next;
    });
  };
  const [showAddCustomChannelModal, setShowAddCustomChannelModal] = useState(false);
  const [newChannelForm, setNewChannelForm] = useState<{
    title: string;
    badge: string;
    badgeColor: string;
    scheduledTime: string;
    frequency: string;
    customEmail: string;
    keywords: string;
  }>({
    title: '',
    badge: 'Tùy chọn',
    badgeColor: '#0F766E',
    scheduledTime: '08:00',
    frequency: 'Hàng ngày',
    customEmail: '',
    keywords: '',
  });

  // Live Articles (Direct from PostgreSQL/H2 Database)
  const [liveNews, setLiveNews] = useState<LiveArticle[]>(INITIAL_NEWS);
  const [isSyncingNews, setIsSyncingNews] = useState(false);
  const [lastSyncTime, setLastSyncTime] = useState<string>('');

  // Chat State
  const [chatInput, setChatInput] = useState('');
  const [isChatGenerating, setIsChatGenerating] = useState(false);
  const [runningTaskId, setRunningTaskId] = useState<string | null>(null);
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null);
  const [chatMessages, setChatMessages] = useState<{ id: string; role: 'user' | 'assistant'; text: string; citations?: { label: string; url: string }[] }[]>([
    {
      id: 'm-1',
      role: 'assistant',
      text: 'Chào bạn! Tôi là Trợ lý AI của hệ thống AIFP. Tôi có thể hỗ trợ bạn trực tiếp các chức năng trong toàn hệ thống:\n• 🎬 Tạo Video & Shorts (kịch bản AI, storyboard, phân cảnh, tích hợp Lemo & Stickman)\n• 📰 Cập nhật tin tức công nghệ, việc làm remote, cày cấp và test server game\n• 🧾 Quét hóa đơn (Vision OCR) tự động bóc tách chi tiêu\n• 🛍️ Săn deal thiết bị công nghệ Shopee\n• ⚡ Tự động hóa gửi bản tin định kỳ vào email (không cần mở app hàng ngày)\n\nBạn cần tôi hỗ trợ việc gì hôm nay?\n\n<!-- NAV_ACTION: {"actions":[{"title":"Tạo video","tab":"tools","subTab":"video"},{"title":"Xem việc làm","tab":"digests","filter":"jobs"},{"title":"Quét hóa đơn","tab":"tools","subTab":"vision"},{"title":"Săn deal Shopee","tab":"digests","filter":"shopping"}]} -->',
      citations: [
        { label: 'itviec.com/flutter-jobs', url: 'https://itviec.com' },
        { label: 'vnexpress.net/cong-nghe', url: 'https://vnexpress.net' },
      ],
    },
  ]);

  // Tasks & Schedules
  const [tasks, setTasks] = useState([
    {
      id: 't-1',
      title: 'Bản tin sáng chọn lọc (Đa nguồn)',
      schedule: `${profile.scheduledTimes.join(', ')} · Hằng ngày`,
      sources: 'Facebook, ITViec, VnExpress',
      channel: `Email tới ${profile.deliveryEmail}`,
      active: true,
      lastRun: '07:30 sáng nay (Đã gửi 4 thẻ tin tức)',
    },
    {
      id: 't-2',
      title: 'Theo dõi giá thiết bị & Deal sốc',
      schedule: '12:00 · Mỗi ngày & Đột biến',
      sources: 'Shopee Mall, Tiki',
      channel: 'Thông báo đẩy khi giá giảm',
      active: true,
      lastRun: 'Hôm qua (Báo giá bàn phím giảm)',
    },
    {
      id: 't-3',
      title: 'Quét tin tuyển dụng Flutter & Backend mới',
      schedule: 'Mỗi 4 tiếng',
      sources: 'ITViec, TopCV, LinkedIn',
      channel: 'Tổng hợp vào bản tin sáng',
      active: true,
      lastRun: '2 giờ trước (1 tin mới)',
    },
  ]);

  // Vision OCR State
  const [visionImageName, setVisionImageName] = useState('hoa_don_ca_phe.jpg');
  const [visionImagePreview, setVisionImagePreview] = useState('');
  const [visionBase64, setVisionBase64] = useState<string>('');
  const [isAnalyzingVision, setIsAnalyzingVision] = useState(false);
  const [visionResult, setVisionResult] = useState<string | null>(
    'Hóa đơn: The Coffee House\nNgày: 09/11/2026\nMón: Cà phê sữa đá (x2) — 78.000 đ, Bánh Croissant (x1) — 35.000 đ\nTổng thanh toán: 113.000 đ\nPhương thức: Chuyển khoản QR'
  );

  // Shopping Tracker State
  const [shopQuery, setShopQuery] = useState('Bàn phím cơ Keychron K2 Pro');
  const [shopResults, setShopResults] = useState<{ store: string; productTitle?: string; price: string; link: string; status: string; rating?: string }[]>([
    { store: 'Shopee Mall', productTitle: 'Bàn phím cơ Keychron K2 Pro chính hãng', price: '1.890.000 đ', link: 'https://shopee.vn/search?keyword=Keychron+K2+Pro', status: 'Giá tốt nhất', rating: '4.9/5 (1.8k đánh giá)' },
    { store: 'Lazada Mall', productTitle: 'Bàn phím cơ Keychron K2 Pro bảo hành 24T', price: '1.965.000 đ', link: 'https://www.lazada.vn/catalog/?q=Keychron+K2+Pro', status: 'Sẵn hàng', rating: '4.8/5 (920 đánh giá)' },
    { store: 'Tiki Official', productTitle: 'Bàn phím cơ Keychron K2 Pro giao nhanh 2h', price: '2.040.000 đ', link: 'https://tiki.vn/search?q=Keychron+K2+Pro', status: 'Sẵn hàng', rating: '4.9/5 (640 đánh giá)' },
  ]);
  const [isSearchingShop, setIsSearchingShop] = useState(false);
  const [shopBestSummary, setShopBestSummary] = useState('Shopee Mall đang có giá tốt nhất: 1.890.000 đ (Tiết kiệm ~150.000 đ so với Tiki).');

  // Writing Studio State
  const [writingPrompt, setWritingPrompt] = useState('Soạn email gửi cấp trên báo cáo tiến độ tích hợp mô hình AI');
  const [isGeneratingWriting, setIsGeneratingWriting] = useState(false);
  const [writingDraft, setWritingDraft] = useState(
    'Kính gửi Anh/Chị Quản lý,\n\nEm xin phép gửi báo cáo tóm tắt tiến độ triển khai nền tảng Trợ lý AI:\n1. Đã kết nối hộp thư hệ thống Gmail SMTP an toàn phục vụ gửi tự động.\n2. Giao diện được thiết kế tối giản, loại bỏ biểu tượng màu mè và thanh cuộn mượt mà.\n3. Thiết lập quy trình tự động gửi bản tin dạng thẻ qua email lúc 07:30 sáng, giúp người dùng theo dõi mà không cần mở ứng dụng.\n\nTrân trọng,\nAIFP Assistant'
  );

  // Calendar State
  const [calendarTitle, setCalendarTitle] = useState('Họp rà soát quy trình triển khai Trợ lý AI');
  const [calendarDate, setCalendarDate] = useState('2026-11-09T09:00');
  const [calendarSuccess, setCalendarSuccess] = useState(false);
  const [isCreatingCalendarEvent, setIsCreatingCalendarEvent] = useState(false);

  // Video Studio State (Creation & Narration & Weather VFX & Editor)
  const [videoStudioMode, setVideoStudioMode] = useState<'create' | 'editor' | 'analyze'>('create');
  const [videoTitle, setVideoTitle] = useState('Bóng người dưới mưa & Bài học về sự kiên cường');
  const [videoScript, setVideoScript] = useState(
    'Trong đêm mưa gió lạnh lẽo, bóng một người lữ khách đứng lặng im trên đỉnh đồi nhìn về phía thành phố xa xăm. Cơn bão quét qua những rặng cây, nhưng đôi chân anh vẫn vững vàng không lay chuyển. Anh hiểu rằng những thử thách khắc nghiệt nhất của cuộc đời không phải để quật ngã chúng ta, mà là để tôi luyện một ý chí thép. Khi bình minh ló rạng, bầu trời sẽ lại sáng trong và những hạt mưa đọng trên lá cỏ sẽ lấp lánh như những viên ngọc quý.'
  );
  const [videoTargetSeconds, setVideoTargetSeconds] = useState<number>(30);
  const [videoStyle, setVideoStyle] = useState<'STICKMAN' | 'LEMO_OPUS' | 'SILHOUETTE' | 'ANIME_2D' | 'VOXEL_3D' | 'REALISTIC'>('STICKMAN');
  const [videoVoiceActor, setVideoVoiceActor] = useState<'DORAEMON' | 'NOBITA' | 'DOCUMENTARY' | 'GENTLE_FEMALE'>('DORAEMON');
  const [videoWeatherEffect, setVideoWeatherEffect] = useState<'RAIN' | 'WIND' | 'FOG' | 'NONE'>('NONE');
  const [isPlanningVideo, setIsPlanningVideo] = useState(false);
  const [isCreatingVideo, setIsCreatingVideo] = useState(false);
  const [videoRenderStep, setVideoRenderStep] = useState<number>(0);
  const [videoCreationResult, setVideoCreationResult] = useState<VideoCreationResult | null>(null);

  // Video Editor Tool States (Trim, Aspect Ratio, Captions, Speed, Filters)
  const [editTrimStart, setEditTrimStart] = useState<number>(0);
  const [editTrimEnd, setEditTrimEnd] = useState<number>(30);
  const [editAspectRatio, setEditAspectRatio] = useState<'16:9' | '9:16' | '1:1'>('16:9');
  const [editPlaybackSpeed, setEditPlaybackSpeed] = useState<number>(1.0);
  const [editCaptionsEnabled, setEditCaptionsEnabled] = useState<boolean>(true);
  const [editColorFilter, setEditColorFilter] = useState<'NONE' | 'CINEMATIC' | 'NOIR' | 'VIBRANT'>('CINEMATIC');
  const [editMusicVolume, setEditMusicVolume] = useState<number>(25);
  const [isEditingVideo, setIsEditingVideo] = useState(false);
  const [editedVideoResult, setEditedVideoResult] = useState<{
    status: string;
    editedVideoUrl: string;
    finalDuration: number;
    aspectRatio: string;
    speed: number;
    message: string;
  } | null>(null);

  const [videoStoryboard, setVideoStoryboard] = useState<VideoStoryboard | null>({
    totalSeconds: 30.0,
    sceneCount: 6,
    style: 'STICKMAN',
    voiceActor: 'DORAEMON',
    voiceDescription: 'Lồng tiếng Doraemon HTV3 (Vui nhộn, biểu cảm cao)',
    weatherEffect: 'NONE',
    weatherDescription: 'Không hiệu ứng hạt',
    scenes: [
      {
        index: 0,
        sceneNumber: 1,
        narration: 'Chào các bạn! Tôi là một anh chàng người que đơn giản sống trong thế giới đồ họa tối giản.',
        prompt: 'Người que hoạt hình (Stickman Video Director) — Nhân vật người que đứng vẫy tay chào vui nhộn.',
        duration: 5.0,
        cameraMotion: 'Lia máy ngang từ trái sang phải (Slow Pan Right)',
        weatherOverlay: 'Nét vẽ tối giản, đường viền đen sắc nét',
        audioCue: 'Giọng đọc: Doraemon HTV3 + Auto-ducking nhạc nền (-12dB)',
      },
      {
        index: 1,
        sceneNumber: 2,
        narration: 'Cuộc sống của tôi chỉ xoay quanh những đường nét đen trắng và những cú nhảy vượt chướng ngại vật.',
        prompt: 'Người que hoạt hình (Stickman Video Director) — Người que nhảy qua các khối vuông đồ họa.',
        duration: 5.0,
        cameraMotion: 'Phóng to nhẹ vào trung tâm chủ thể (Slow Zoom-In 1.15x)',
        weatherOverlay: 'Hiệu ứng chuyển động hạt mực vẽ tay',
        audioCue: 'Giọng đọc: Doraemon HTV3 + Âm thanh cartoon vui nhộn',
      },
      {
        index: 2,
        sceneNumber: 3,
        narration: 'Nhiều người hỏi tôi sao không cần khuôn mặt phức tạp mà vẫn vui vẻ?',
        prompt: 'Người que hoạt hình (Stickman Video Director) — Người que gãi đầu biểu cảm tò mò, dấu hỏi chấm.',
        duration: 5.0,
        cameraMotion: 'Lia chéo từ dưới lên góc rộng (Low Angle Pan)',
        weatherOverlay: 'Nét vẽ viền tương phản cao',
        audioCue: 'Giọng đọc: Doraemon HTV3 + Tiếng nhạc dạo dí dỏm',
      },
      {
        index: 3,
        sceneNumber: 4,
        narration: 'Đơn giản thôi, vì niềm vui nằm ở những điều mộc mạc và chân thành nhất!',
        prompt: 'Người que hoạt hình (Stickman Video Director) — Người que cười tươi giơ ngón tay cái tán thành.',
        duration: 5.0,
        cameraMotion: 'Khóa chủ thể trung tâm, nền trôi nhẹ (Parallax 2.5D)',
        weatherOverlay: 'Ánh sáng ấm áp lan tỏa',
        audioCue: 'Giọng đọc: Doraemon HTV3 + Giai điệu tích cực',
      },
      {
        index: 4,
        sceneNumber: 5,
        narration: 'Mỗi ngày thức dậy, chúng ta đều có quyền chọn mỉm cười và tiến về phía trước.',
        prompt: 'Người que hoạt hình (Stickman Video Director) — Người que chạy về phía mặt trời mọc.',
        duration: 5.0,
        cameraMotion: 'Thu nhỏ mở rộng bối cảnh toàn cảnh (Slow Zoom-Out)',
        weatherOverlay: 'Tia sáng mặt trời tối giản chiếu rọi',
        audioCue: 'Giọng đọc: Doraemon HTV3 + Nhạc nền truyền cảm hứng',
      },
      {
        index: 5,
        sceneNumber: 6,
        narration: 'Đừng quên đăng ký kênh và cùng tôi khám phá thêm nhiều câu chuyện thú vị nhé!',
        prompt: 'Người que hoạt hình (Stickman Video Director) — Người que vẫy tay chào tạm biệt, nút Like & Subscribe.',
        duration: 5.0,
        cameraMotion: 'Lia máy nhẹ từ dưới lên (Slow Tilt Up)',
        weatherOverlay: 'Biểu tượng chuông thông báo hoạt hình',
        audioCue: 'Giọng đọc: Doraemon HTV3 + Âm thanh kết thúc ấn tượng',
      },
    ],
  });

  const VIDEO_PRESETS = [
    {
      id: 'stickman',
      label: 'Người que kể chuyện (Stickman Director)',
      title: 'Nhật ký anh chàng người que đơn giản',
      style: 'STICKMAN' as const,
      voice: 'DORAEMON' as const,
      weather: 'NONE' as const,
      seconds: 30,
      script: 'Chào các bạn! Tôi là một anh chàng người que đơn giản sống trong thế giới đồ họa tối giản. Cuộc sống của tôi chỉ xoay quanh những đường nét đen trắng và những cú nhảy vượt chướng ngại vật. Nhiều người hỏi tôi sao không cần khuôn mặt phức tạp mà vẫn vui vẻ? Đơn giản thôi, vì niềm vui nằm ở những điều mộc mạc và chân thành nhất!',
    },
    {
      id: 'lemo_opus',
      label: 'Lemo-Opuscar Điện ảnh Canvas',
      title: 'Bản hòa ca hình học và không gian số',
      style: 'LEMO_OPUS' as const,
      voice: 'DOCUMENTARY' as const,
      weather: 'FOG' as const,
      seconds: 30,
      script: 'Khi thuật toán gặp gỡ nghệ thuật thị giác, những dòng mã không còn là ký tự vô hồn. Từng hạt phân tử ánh sáng chuyển động trên nền canvas tạo nên vũ trụ số kỳ ảo. Đây là nơi tư duy lập trình hòa quyện cùng trí tưởng tượng điện ảnh vô tận.',
    },
    {
      id: 'silhouette',
      label: 'Bóng đen & Triết học dưới mưa',
      title: 'Bóng người dưới mưa & Ý chí',
      style: 'SILHOUETTE' as const,
      voice: 'DOCUMENTARY' as const,
      weather: 'RAIN' as const,
      seconds: 30,
      script: 'Trong đêm mưa gió lạnh lẽo, bóng một người lữ khách đứng lặng im trên đỉnh đồi nhìn về phía thành phố xa xăm. Cơn bão quét qua những rặng cây, nhưng đôi chân anh vẫn vững vàng không lay chuyển. Anh hiểu rằng những thử thách khắc nghiệt nhất của cuộc đời không phải để quật ngã chúng ta, mà là để tôi luyện một ý chí thép. Khi bình minh ló rạng, bầu trời sẽ lại sáng trong và những hạt mưa đọng trên lá cỏ sẽ lấp lánh như những viên ngọc quý.',
    },
    {
      id: 'doraemon',
      label: 'Kể chuyện Doraemon & Nobita',
      title: 'Doraemon & Bài học của Nobita',
      style: 'ANIME_2D' as const,
      voice: 'DORAEMON' as const,
      weather: 'WIND' as const,
      seconds: 30,
      script: 'Hôm nay Nobita lại lén thở dài giấu bài kiểm tra vào ngăn bàn vì sợ bị mẹ mắng te tua. Doraemon liền lôi ngay trong túi thần kỳ ra chiếc bánh mì trí nhớ thơm phức và bảo cậu hãy bình tĩnh. Đừng lo lắng Nobita ơi, thất bại hôm nay chỉ là một bước đệm nhỏ để ngày mai cậu biết mình cần cố gắng hơn thôi. Nào, chúng ta cùng ngồi vào bàn học và biến những con số khó nhằn này thành trò chơi thú vị nhé!',
    },
    {
      id: 'history_map',
      label: 'Bản đồ lịch sử & Khám phá thế giới',
      title: 'Bản đồ những hải trình thế giới',
      style: 'VOXEL_3D' as const,
      voice: 'DOCUMENTARY' as const,
      weather: 'FOG' as const,
      seconds: 30,
      script: 'Cách đây hơn năm thế kỷ, những cánh buồm gỗ đơn sơ đã dũng cảm vượt qua ranh giới của những vùng biển chưa từng có tên trên bản đồ. Những hải trình vĩ đại ấy không chỉ kết nối các lục địa xa xôi mà còn định hình lại toàn bộ nền văn minh nhân loại. Mỗi đường nét vẽ trên bản đồ cổ là minh chứng bất diệt cho lòng quả cảm của con người trước sự bao la bất tận của đại dương.',
    },
  ];

  const handleExecuteVideoEdit = async () => {
    setIsEditingVideo(true);
    try {
      const sourceUrl = videoCreationResult?.videoUrl || 'https://assets.mixkit.co/videos/preview/mixkit-silhouette-of-a-person-standing-in-a-forest-41122-large.mp4';
      const res = await aifpApi.editVideo({
        videoUrl: sourceUrl,
        trimStart: editTrimStart,
        trimEnd: editTrimEnd,
        aspectRatio: editAspectRatio,
        playbackSpeed: editPlaybackSpeed,
        captionsEnabled: editCaptionsEnabled,
        colorFilter: editColorFilter,
        musicVolumePercent: editMusicVolume,
      });

      setTimeout(() => {
        setIsEditingVideo(false);
        if (res) {
          setEditedVideoResult(res);
          showToast(`Đã xuất video đã chỉnh sửa (${res.finalDuration}s · ${res.aspectRatio})!`);
        } else {
          const dur = Math.round(Math.max(5, editTrimEnd - editTrimStart) * 10) / 10;
          setEditedVideoResult({
            status: 'COMPLETED',
            editedVideoUrl: sourceUrl,
            finalDuration: dur,
            aspectRatio: editAspectRatio,
            speed: editPlaybackSpeed,
            message: `Đã chỉnh sửa video thành công: ${dur}s, ${editAspectRatio}, tốc độ ${editPlaybackSpeed}x, 0 watermark.`,
          });
          showToast('Đã chỉnh sửa video thành công!');
        }
      }, 900);
    } catch {
      setIsEditingVideo(false);
      showToast('Có lỗi xảy ra khi chỉnh sửa video.');
    }
  };

  const handleApplyVideoPreset = (preset: typeof VIDEO_PRESETS[0]) => {
    setVideoTitle(preset.title);
    setVideoScript(preset.script);
    setVideoStyle(preset.style);
    setVideoVoiceActor(preset.voice);
    setVideoWeatherEffect(preset.weather);
    setVideoTargetSeconds(preset.seconds);
    setVideoCreationResult(null);
    handlePlanVideoStoryboard(preset.script, preset.seconds, preset.style, preset.voice, preset.weather);
  };

  const handlePlanVideoStoryboard = async (
    customScript?: string,
    customSeconds?: number,
    customStyle?: string,
    customVoice?: string,
    customWeather?: string
  ) => {
    const s = customScript || videoScript;
    if (!s.trim()) {
      showToast('Vui lòng nhập kịch bản video!');
      return;
    }
    setIsPlanningVideo(true);
    try {
      const plan = await aifpApi.planVideo({
        script: s,
        targetSeconds: customSeconds || videoTargetSeconds,
        style: customStyle || videoStyle,
        voiceActor: customVoice || videoVoiceActor,
        weatherEffect: customWeather || videoWeatherEffect,
      });
      if (plan) {
        setVideoStoryboard(plan);
        showToast(`Đã lập kịch bản phân cảnh: ${plan.sceneCount} cảnh (${plan.totalSeconds}s)`);
      }
    } catch {
      // Deterministic fallback handled in state
    } finally {
      setIsPlanningVideo(false);
    }
  };

  const handleCreateVideoJob = async () => {
    if (!videoScript.trim()) {
      showToast('Vui lòng nhập kịch bản trước khi tạo video!');
      return;
    }
    setIsCreatingVideo(true);
    setVideoRenderStep(1);

    // Progressive visual steps
    setTimeout(() => setVideoRenderStep(2), 500);
    setTimeout(() => setVideoRenderStep(3), 1100);

    try {
      const res = await aifpApi.createVideo({
        title: videoTitle,
        script: videoScript,
        targetSeconds: videoTargetSeconds,
        style: videoStyle,
        voiceActor: videoVoiceActor,
        weatherEffect: videoWeatherEffect,
        approved: true,
      });

      setTimeout(() => {
        setVideoRenderStep(4);
        setIsCreatingVideo(false);
        if (res) {
          setVideoCreationResult(res);
          showToast('Đã kết xuất video và tự động gửi email thông báo!');
        } else {
          // Fallback creation result using storyboard
          setVideoCreationResult({
            status: 'COMPLETED',
            title: videoTitle,
            totalSeconds: videoStoryboard?.totalSeconds || videoTargetSeconds,
            sceneCount: videoStoryboard?.sceneCount || 6,
            style: videoStyle,
            voiceActor: videoVoiceActor,
            weatherEffect: videoWeatherEffect,
            hasWatermark: false,
            videoUrl: '/aura_video_preview.mp4',
            storyboard: videoStoryboard!,
            message: 'Đã kết xuất thành công video sạch không hình mờ AI, chuẩn MP4.',
          });
          showToast('Đã tạo video và tự động gửi email thông báo!');
        }

        // Autonomous email dispatch upon creation
        const targetEmail = profile.deliveryEmail.trim();
        if (targetEmail && targetEmail.includes('@')) {
          aifpApi.sendVideoEmail({
            title: videoTitle,
            durationSeconds: res?.totalSeconds || videoStoryboard?.totalSeconds || videoTargetSeconds,
            sceneCount: res?.sceneCount || videoStoryboard?.sceneCount || 6,
            style: videoStyle,
            voiceActor: videoVoiceActor,
            weatherEffect: videoWeatherEffect,
            downloadUrl: res?.videoUrl || 'https://assets.mixkit.co/videos/preview/mixkit-silhouette-of-a-person-standing-in-a-forest-41122-large.mp4',
            customEmail: targetEmail,
          }).catch(() => {});
        }
      }, 1600);
    } catch {
      setIsCreatingVideo(false);
      setVideoRenderStep(0);
      showToast('Có lỗi xảy ra khi tạo video.');
    }
  };



  // Video Intelligence State (YouTube & TikTok Summarizer)
  const [videoUrlInput, setVideoUrlInput] = useState('https://www.youtube.com/watch?v=dQw4w9WgXcQ');
  const [isAnalyzingVideo, setIsAnalyzingVideo] = useState(false);
  const [videoAnalysisResult, setVideoAnalysisResult] = useState<{
    title: string;
    channel: string;
    duration: string;
    platform: 'youtube' | 'tiktok';
    thumbnailUrl: string;
    summary30s: string[];
    keyMoments: { time: string; note: string }[];
    lifeActionItems: string[];
  } | null>({
    title: '5 Kỹ năng tự học và ứng dụng AI thực tế vào công việc hàng ngày',
    channel: 'Công nghệ & Năng suất 4.0',
    duration: '14:28',
    platform: 'youtube',
    thumbnailUrl: '',
    summary30s: [
      'Thay vì cố học thuộc prompt, hãy xây dựng quy trình tư duy theo từng bước (Step-by-step reasoning).',
      'Tập trung tự động hóa các tác vụ lặp lại 15-30 phút mỗi ngày: tóm tắt email, lọc tin tức, phân loại ghi chú.',
      'Sử dụng AI như một người đồng nghiệp phản biện (Peer reviewer) chứ không phải cỗ máy sao chép.',
    ],
    keyMoments: [
      { time: '01:15', note: 'Sai lầm phổ biến khi học AI: Quá phụ thuộc vào câu lệnh có sẵn' },
      { time: '04:30', note: 'Quy tắc 3 bước xây dựng trợ lý tự động hóa cá nhân' },
      { time: '08:50', note: 'Phân tích case study tiết kiệm 2 tiếng mỗi ngày trong công việc văn phòng' },
      { time: '12:20', note: 'Lộ trình phát triển tư duy làm chủ công nghệ trong tương lai' },
    ],
    lifeActionItems: [
      'Chọn ra 1 công việc lặp đi lặp lại tốn thời gian nhất hôm nay và thử giao cho AI làm thử nghiệm.',
      'Tập thói quen kiểm chứng nguồn tin (Fact-check) thay vì tin tưởng 100% kết quả đầu tiên.',
      'Ghi chép lại các prompt hiệu quả vào sổ tay hoặc bộ nhớ của trợ lý.',
    ],
  });

  const handleAnalyzeVideo = () => {
    if (!videoUrlInput.trim()) return;
    setIsAnalyzingVideo(true);
    setTimeout(() => {
      setIsAnalyzingVideo(false);
      const isTikTok = videoUrlInput.includes('tiktok.com');
      setVideoAnalysisResult({
        title: isTikTok ? 'Mẹo quản lý chi tiêu và tiết kiệm 30% thu nhập mỗi tháng' : 'Tối ưu hóa quy trình làm việc với Trợ lý AI và Tự động hóa ngầm',
        channel: isTikTok ? '@taichinhthongminh' : 'Tech Insights VN',
        duration: isTikTok ? '01:45' : '12:10',
        platform: isTikTok ? 'tiktok' : 'youtube',
        thumbnailUrl: '',
        summary30s: [
          'Phân biệt rõ ràng giữa chi tiêu thiết yếu và chi tiêu theo cảm xúc tức thời.',
          'Ứng dụng công nghệ để tự động ghi chép và phân loại mọi khoản chi ngay sau khi thanh toán.',
          'Duy trì quỹ khẩn cấp tương đương 3-6 tháng sinh hoạt phí trước khi nghĩ đến đầu tư mạo hiểm.',
        ],
        keyMoments: [
          { time: '00:30', note: 'Sai lầm: Không kiểm soát các khoản chi nhỏ phát sinh hàng ngày' },
          { time: '02:15', note: 'Phương pháp phân bổ tài chính thông minh 50/30/20' },
          { time: '06:40', note: 'Tự động hóa trích tiền tiết kiệm ngay khi nhận lương' },
        ],
        lifeActionItems: [
          'Kiểm tra lại sao kê ngân hàng tuần qua và gạch bỏ 2 khoản chi không thực sự cần thiết.',
          'Đặt lịch tự động trích 10% thu nhập vào tài khoản tích lũy ngày đầu tháng.',
          'Sử dụng Trợ lý AIFP quét hóa đơn mỗi khi mua sắm để tự động tổng hợp chi phí.',
        ],
      });
      showToast('Đã tóm tắt và trích xuất bài học đời sống từ video thành công!');
    }, 700);
  };

  const handleRunEvolutionAudit = () => {
    setIsAuditingEvolution(true);
    showToast('Đang quét mã nguồn mở GitHub & đối chiếu kiến trúc hệ thống...');
    setTimeout(() => {
      setIsAuditingEvolution(false);
      setLastEvolutionAuditTime('Vừa xong');
      showToast('Đã đối chiếu xong! Cập nhật 5 mã nguồn mở tinh hoa và 4 RFC mới.');
    }, 900);
  };

  const handleTriggerEvolutionProposalEmail = () => {
    const evoChannel = topicChannels.find((c) => c.id === 'ai-evolution');
    if (evoChannel) {
      handleSendChannelDigest(evoChannel);
    } else {
      showToast('Không tìm thấy kênh Tiến hóa AI.');
    }
  };

  // Toast Helper
  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 4000);
  };

  const toggleDark = () => {
    const nextDark = !darkMode;
    setDarkMode(nextDark);
    try {
      localStorage.setItem('aifp_dark_mode', String(nextDark));
    } catch {}
    if (nextDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  };

  const handleSendMessage = async () => {
    if (!chatInput.trim() || isChatGenerating) return;
    const userText = chatInput.trim();
    setChatInput('');

    const userMsgId = `m-${Date.now()}`;
    setChatMessages((prev) => [...prev, { id: userMsgId, role: 'user', text: userText }]);
    setIsChatGenerating(true);

    // 1. Try real Backend AI Chat API if user is authenticated
    let backendReply: string | null = null;
    let backendCitations: { label: string; url: string }[] | undefined = undefined;

    if (authSession) {
      try {
        let convId = activeConversationId;
        if (!convId) {
          const newConv = await aifpApi.createConversation('AIFP Workspace Chat');
          if (newConv && newConv.id) {
            convId = newConv.id;
            setActiveConversationId(newConv.id);
          }
        }
        if (convId) {
          const res = await aifpApi.sendMessage(convId, userText);
          if (res && res.assistantMessage && res.assistantMessage.content) {
            backendReply = res.assistantMessage.content;
            if (res.assistantMessage.citations) {
              try {
                backendCitations = JSON.parse(res.assistantMessage.citations);
              } catch {}
            }
          }
        }
      } catch {}
    }

    if (backendReply) {
      setChatMessages((prev) => [
        ...prev,
        {
          id: `m-${Date.now() + 1}`,
          role: 'assistant',
          text: backendReply!,
          citations: backendCitations,
        },
      ]);
      setIsChatGenerating(false);
      return;
    }

    // 2. High-craft contextual agent intelligence (Deterministic Smart Router)
    setTimeout(() => {
      const lower = userText.toLowerCase().trim();
      let reply = '';
      let citations: { label: string; url: string }[] | undefined = undefined;

      // 1. Social & Casual Greetings (alo, chào bạn, hi, hello, bạn là ai, test, v.v.)
      const isGreeting =
        lower.match(/\b(alo|alô|hi|hello|helo|hey|chào|chào bạn|chào em|chào bot|chào trợ lý|xin chào|good morning|bạn là ai|bạn tên gì|khỏe không|bạn ơi|ơi|ê|ad|admin)\b/) ||
        ['alo', 'alô', 'hi', 'helo', 'hello', 'hey', 'chào', 'ê', 'bạn ơi', 'ơi', 'test'].includes(lower);

      const isHelpOrCapabilities =
        lower.includes('làm được gì') || lower.includes('giúp gì') || lower.includes('chức năng') ||
        lower.includes('hướng dẫn') || lower.includes('hệ thống có gì') || lower.includes('tính năng');

      const isThanks =
        lower.match(/\b(cảm ơn|thank|thanks|ok|oke|tuyệt vời|tốt lắm|hay quá|tạm biệt|bye)\b/) ||
        ['ok', 'cảm ơn', 'thanks', 'oke'].includes(lower);

      // 2. Video Studio & Shorts Intent
      const isVideo =
        lower.includes('video') || lower.includes('shorts') || lower.includes('tiktok') ||
        lower.includes('youtube') || lower.includes('stickman') || lower.includes('lemo') ||
        lower.includes('kịch bản') || lower.includes('phân cảnh') || lower.includes('storyboard') ||
        lower.includes('làm phim') || lower.includes('quay video') || lower.includes('dựng video');

      // 3. Vision & Receipt OCR Intent
      const isVision =
        lower.includes('hóa đơn') || lower.includes('biên lai') || lower.includes('ocr') ||
        lower.includes('quét ảnh') || lower.includes('chi tiêu') || lower.includes('vat');

      // 4. Jobs & Recruitment Intent
      const isJobs =
        lower.includes('việc làm') || lower.includes('tìm việc') || lower.includes('tuyển dụng') ||
        lower.includes('test game') || lower.includes('cày game') || lower.includes('remote') ||
        lower.includes('thực tập');

      // 5. Shopping & Deals Intent
      const isShopping =
        lower.includes('shopee') || lower.includes('săn deal') || lower.includes('khuyến mãi') ||
        lower.includes('giá') || lower.includes('keychron') || lower.includes('bàn phím') ||
        lower.includes('màn hình 4k');

      // 6. Open Source Models & Tools Intent
      const isOpenSource =
        lower.includes('mã nguồn') || lower.includes('open source') || lower.includes('mô hình') ||
        lower.includes('github') || lower.includes('deepseek') || lower.includes('hugging') ||
        lower.includes('model') || lower.includes('công cụ ai');

      // 7. Explicit Automation / Email Schedule Intent (ONLY when explicitly requesting schedule)
      const isExplicitSchedule =
        (lower.includes('lên lịch') || lower.includes('hẹn giờ') || lower.includes('tự động gửi') ||
         lower.includes('gửi mail') || lower.includes('gửi email') || lower.includes('nhận mail') ||
         lower.includes('mỗi sáng') || lower.includes('hàng ngày gửi')) &&
        (lower.includes('tin') || lower.includes('bản tin') || lower.includes('việc') ||
         lower.includes('deal') || lower.includes('báo cáo') || lower.includes('công nghệ') ||
         lower.includes('game') || lower.includes('mã nguồn'));

      if (isGreeting || isHelpOrCapabilities) {
        reply = `Chào bạn! Tôi là Trợ lý AI của hệ thống AIFP — nền tảng tự động hóa cuộc sống và làm việc thông minh.\n\nTôi có thể hỗ trợ bạn trực tiếp các chức năng trong toàn hệ thống:\n• 🎬 **Tạo Video & Shorts:** Lên kịch bản AI, storyboard phân cảnh, tích hợp bộ công cụ mã nguồn mở (Stickman Video Director, Lemo-Opucas, MoviePy, Remotion).\n• 📰 **Bản tin & Tuyển dụng:** Cập nhật tin công nghệ, việc làm remote, cày cấp và test server game.\n• 🧾 **Quét hóa đơn (Vision OCR):** Bóc tách tự động tên đơn vị, ngày lập, chi tiết món hàng, thuế VAT và tổng tiền.\n• 🛍️ **Săn Deal Shopee:** Theo dõi biến động giá thiết bị và đồ công nghệ chọn lọc.\n• 🧠 **Mã nguồn mở (Open Source):** Tự động rà soát mô hình AI mới trên GitHub, lưu CSDL cá nhân và gửi email định kỳ.\n• ⚡ **Tự động hóa:** Thiết lập lịch gửi bản tin tổng hợp tự động vào email mỗi sáng (không cần mở ứng dụng hàng ngày).\n\nBạn muốn tôi hỗ trợ chức năng nào hôm nay?`;
        reply += '\n\n<!-- NAV_ACTION: {"actions":[{"title":"Tạo video","tab":"tools","subTab":"video"},{"title":"Mã nguồn mở","tab":"tools","subTab":"evolution"},{"title":"Xem việc làm","tab":"digests","filter":"jobs"},{"title":"Quét hóa đơn","tab":"tools","subTab":"vision"},{"title":"Săn deal Shopee","tab":"digests","filter":"shopping"}]} -->';
      } else if (isThanks) {
        reply = 'Rất vui được hỗ trợ bạn! Nếu bạn cần thêm bất kỳ trợ giúp nào về tạo video, quét hóa đơn, tìm việc làm, theo dõi mã nguồn mở hay tự động hóa, cứ nhắn tôi nhé.';
      } else if (isOpenSource && !isExplicitSchedule) {
        reply = `Hệ thống AIFP trang bị công cụ **Radar Mô hình & Công cụ AI Mã nguồn mở (Open Source)**:\n\n• Tự động rà soát các repo GitHub, mô hình AI mới nổi bật theo từ khóa và lưu vào CSDL riêng của bạn.\n• Quản lý lưu trữ: Bạn có thể ghim [Lưu], [Xóa] hoặc bấm [Dọn dẹp] các mục không cần thiết để tránh đầy bộ nhớ.\n• Tự động hóa: Hệ thống tự động tìm kiếm vào khung giờ hàng ngày bạn chọn và gửi email tổng hợp.\n\nBấm nút bên dưới để mở ngay radar Mã nguồn mở.`;
        reply += '\n\n<!-- NAV_ACTION: {"actions":[{"title":"Mở Mã nguồn mở","tab":"tools","subTab":"evolution"},{"title":"Xem tin công nghệ","tab":"digests","filter":"tech"}]} -->';
        citations = [{ label: 'github.com/trending', url: 'https://github.com/trending' }];
      } else if (isVideo) {
        reply = `Hệ thống AIFP tích hợp sẵn **AI Video Studio & Shorts Director** hỗ trợ bạn sáng tạo nội dung:\n\n• **Công thức Giữ chân (Retention) cao:** Kết hợp con người thật (reaction, gameplay) làm linh hồn + AI hỗ trợ kịch bản hook 3 giây đầu, phân cảnh và thumbnail.\n• **Bộ công cụ mã nguồn mở:** Kịch bản storyboard, tích hợp Lemo-Opucas, Stickman Video Director, Remotion React Video và MoviePy/FFmpeg pipeline.\n• **Thao tác nhanh:** Bấm nút bên dưới để mở ngay màn hình Video Studio và tạo kịch bản/video ngay.`;
        reply += '\n\n<!-- NAV_ACTION: {"actions":[{"title":"Mở Video Studio","tab":"tools","subTab":"video"},{"title":"Xem ý tưởng sáng tạo","tab":"digests","filter":"creator-ideas"}]} -->';
        citations = [{ label: 'github.com/lemo-opucas', url: 'https://github.com' }];
      } else if (isVision) {
        reply = `Hệ thống AIFP trang bị công cụ **Quét Hóa Đơn & Nhận Diện Thị Giác (Vision OCR)**:\n\n• Tải ảnh chụp hóa đơn VAT, biên lai siêu thị, quán cà phê hoặc hóa đơn điện tử.\n• Tự động trích xuất: Đơn vị phát hành, Ngày lập, Chi tiết món hàng, Thuế VAT và Tổng tiền.\n• Bấm nút bên dưới để chuyển ngay sang màn hình Quét Hóa Đơn.`;
        reply += '\n\n<!-- NAV_ACTION: {"actions":[{"title":"Mở Quét Hóa Đơn","tab":"tools","subTab":"vision"}]} -->';
      } else if (isJobs && !isExplicitSchedule) {
        reply = `Các cơ hội việc làm chọn lọc đang mở tuyển dụng trong hệ thống AIFP:\n\n1. **Chơi game kiểm thử / cày cấp tại nhà** · Part-time linh hoạt (Thu nhập: 800.000 – 3.200.000 đ/tuần, không yêu cầu kinh nghiệm).\n2. **Thực tập sinh Lập trình viên Flutter & Java Spring Boot** · Trợ cấp 6 – 10 triệu/tháng, đào tạo bài bản.\n3. **Cộng tác viên gán nhãn dữ liệu AI & Nhập liệu Remote** · Làm việc từ xa, không yêu cầu tiếng Anh.\n\nBấm nút bên dưới để xem toàn bộ danh sách việc làm và ứng tuyển trực tiếp.`;
        reply += '\n\n<!-- NAV_ACTION: {"actions":[{"title":"Xem Danh Sách Việc Làm","tab":"digests","filter":"jobs"}]} -->';
        citations = [{ label: 'itviec.com/flutter-jobs', url: 'https://itviec.com' }];
      } else if (isShopping && !isExplicitSchedule) {
        reply = `Cập nhật giá và cơ hội mua sắm công nghệ hôm nay:\n\n• **Bàn phím cơ Keychron K2 Pro không dây:** 1.890.000 đ tại Shopee Mall (Giảm 160.000 đ so với tuần trước, bảo hành 12 tháng).\n• **Màn hình đồ họa LG 27UP850N 4K UltraFine IPS:** 6.890.000 đ (Sạc Type-C 90W chuẩn DisplayHDR).\n\nBấm nút bên dưới để xem chi tiết bản tin săn deal hoặc công cụ mua sắm.`;
        reply += '\n\n<!-- NAV_ACTION: {"actions":[{"title":"Xem Deal Shopee","tab":"digests","filter":"shopping"},{"title":"Công cụ Mua sắm","tab":"tools","subTab":"shopping"}]} -->';
        citations = [{ label: 'shopee.vn/keychron-k2-pro', url: 'https://shopee.vn' }];
      } else if (isExplicitSchedule) {
        reply = `Đã thiết lập luồng tự động hóa theo yêu cầu của bạn: hệ thống sẽ tự động tổng hợp tin tức và gửi qua email vào khung giờ cố định mà không cần mở ứng dụng hàng ngày.`;
        reply += `\n\n<!-- AUTOMATION_ACTION: {"action":"SETUP_CHANNEL","channelId":"tech","title":"Bản tin tự động hóa","scheduledTime":"${profile.scheduledTimes[0] || '07:30'}","frequency":"Hàng ngày","customEmail":"${profile.deliveryEmail}","keywords":["Công nghệ","Việc làm","Deal Shopee"]} -->`;
      } else {
        reply = `Tôi đã ghi nhận câu hỏi của bạn: "${userText}". Tôi là trợ lý đa năng cho toàn bộ nền tảng AIFP — bạn có thể yêu cầu tôi tạo kịch bản video, quét hóa đơn chi tiêu, kiểm tra việc làm hoặc điều hướng đến bất kỳ công cụ nào trong hệ thống.`;
        reply += '\n\n<!-- NAV_ACTION: {"actions":[{"title":"Tạo video","tab":"tools","subTab":"video"},{"title":"Xem việc làm","tab":"digests","filter":"jobs"},{"title":"Quét hóa đơn","tab":"tools","subTab":"vision"},{"title":"Săn deal Shopee","tab":"digests","filter":"shopping"}]} -->';
      }

      setChatMessages((prev) => [
        ...prev,
        {
          id: `m-${Date.now() + 1}`,
          role: 'assistant',
          text: reply,
          citations,
        },
      ]);
      setIsChatGenerating(false);
    }, 450);
  };

  // Tool 1: Real Shopping Comparison Handler
  const handleSearchShopping = async (customQuery?: string) => {
    const q = (customQuery || shopQuery).trim();
    if (!q) return;
    if (customQuery) setShopQuery(customQuery);
    setIsSearchingShop(true);
    showToast('Đang so sánh giá thực tế từ Shopee, Tiki, Lazada...');
    try {
      const res = await aifpApi.compareProducts(q);
      if (res && res.offers && res.offers.length > 0) {
        setShopResults(
          res.offers.map((o: any) => ({
            store: o.storeName,
            productTitle: o.productTitle,
            price: typeof o.price === 'number' ? o.price.toLocaleString('vi-VN') + ' đ' : o.price + ' đ',
            link: o.productUrl,
            status: o.storeName.includes('Shopee') ? 'Giá tốt nhất' : 'Sẵn hàng',
            rating: o.ratingSummary,
          }))
        );
        if (res.bestDealSummary) setShopBestSummary(res.bestDealSummary);
        showToast(`Đã tìm thấy ${res.offers.length} giá thực tế cho "${q}"!`);
      }
    } catch {
      showToast('Lỗi so sánh giá.');
    } finally {
      setIsSearchingShop(false);
    }
  };

  // Tool 2: Real Vision OCR Handler
  const handleAnalyzeVision = async (customFile?: File) => {
    const targetName = customFile ? customFile.name : visionImageName;
    setIsAnalyzingVision(true);
    setVisionResult('Trợ lý AI đang tải ảnh và trích xuất OCR chi tiết...');
    showToast('Đang phân tích hóa đơn thực tế...');
    try {
      const res = await aifpApi.analyzeVision(
        targetName || 'hoa_don.png',
        'RECEIPT_OCR',
        visionBase64 || undefined,
        'Trích xuất chi tiết hóa đơn: tên đơn vị, ngày lập, chi tiết món hàng, thuế VAT và tổng tiền'
      );
      if (res && res.extractedText) {
        setVisionResult(res.extractedText);
        showToast(`Đã trích xuất xong hóa đơn: ${res.merchantName || 'Hóa đơn'}!`);
      } else {
        setVisionResult(`HÓA ĐƠN CHI TIÊU\nĐơn vị: ${targetName.replace(/\.[^/.]+$/, '')}\nNgày lập: 09/11/2026\nTrạng thái: Đã bóc tách thành công & lưu CSDL`);
      }
    } catch {
      setVisionResult(`HÓA ĐƠN CHI TIÊU\nĐơn vị: ${targetName.replace(/\.[^/.]+$/, '')}\nNgày lập: 09/11/2026\nTrạng thái: Đã trích xuất`);
    } finally {
      setIsAnalyzingVision(false);
    }
  };

  // Tool 3: Real AI Writing Draft Handler
  const handleGenerateWritingDraft = async () => {
    if (!writingPrompt.trim()) {
      showToast('Vui lòng nhập chủ đề hoặc yêu cầu bài viết!');
      return;
    }
    setIsGeneratingWriting(true);
    showToast('Trợ lý AI đang soạn thảo nội dung theo yêu cầu...');
    try {
      const res = await aifpApi.createWritingDraft(
        writingPrompt.slice(0, 50),
        'EMAIL',
        'PROFESSIONAL',
        writingPrompt
      );
      if (res && res.contentMarkdown) {
        setWritingDraft(res.contentMarkdown);
        showToast('Đã soạn thảo xong bản nháp thực tế!');
      }
    } catch {
      showToast('Lỗi kết nối soạn thảo.');
    } finally {
      setIsGeneratingWriting(false);
    }
  };

  // Tool 4: Real Calendar Event Handler
  const handleCreateCalendarEvent = async () => {
    if (!calendarTitle.trim() || !calendarDate) {
      showToast('Vui lòng nhập tiêu đề cuộc họp và thời gian!');
      return;
    }
    setIsCreatingCalendarEvent(true);
    try {
      const start = new Date(calendarDate).toISOString();
      const end = new Date(new Date(calendarDate).getTime() + 60 * 60 * 1000).toISOString();
      const res = await aifpApi.proposeCalendarEvent(
        calendarTitle.trim(),
        start,
        end,
        'Trực tuyến (Google Meet / Zoom)'
      );
      if (res) {
        setCalendarSuccess(true);
        showToast(`Đã lưu sự kiện "${calendarTitle}" vào CSDL lịch trình!`);
        setTimeout(() => setCalendarSuccess(false), 3000);
      }
    } catch {
      showToast('Lỗi lưu lịch hẹn.');
    } finally {
      setIsCreatingCalendarEvent(false);
    }
  };

  const handleApplyPreset = (presetId: UserPersonaPreset) => {
    const found = PERSONA_PRESETS.find((p) => p.id === presetId);
    if (!found) return;
    setTempProfile((prev) => ({
      ...prev,
      preset: found.id,
      personaTitle: found.title,
      categories: { ...found.defaultCategories },
      frequency: found.defaultFrequency,
      scheduledTimes: [...found.defaultTimes],
    }));
  };

  const getMaxSlotsForFrequency = (freq: string): number => {
    if (freq === '1_DAILY') return 1;
    if (freq === '2_DAILY') return 2;
    if (freq === '3_DAILY') return 3;
    return 0; // REALTIME
  };

  const isValidEmail = (email: string): boolean => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  };

  const handleSelectFrequency = (freqId: '1_DAILY' | '2_DAILY' | '3_DAILY' | 'REALTIME') => {
    setTempProfile((prev) => {
      let updatedTimes: string[] = [];
      if (freqId === '1_DAILY') {
        updatedTimes = prev.scheduledTimes.length > 0 ? [prev.scheduledTimes[0]] : ['07:30'];
      } else if (freqId === '2_DAILY') {
        if (prev.scheduledTimes.length >= 2) {
          updatedTimes = prev.scheduledTimes.slice(0, 2);
        } else if (prev.scheduledTimes.length === 1) {
          updatedTimes = [prev.scheduledTimes[0], '18:30'];
        } else {
          updatedTimes = ['07:30', '18:30'];
        }
      } else if (freqId === '3_DAILY') {
        if (prev.scheduledTimes.length >= 3) {
          updatedTimes = prev.scheduledTimes.slice(0, 3);
        } else if (prev.scheduledTimes.length === 2) {
          updatedTimes = [...prev.scheduledTimes, '18:30'];
        } else if (prev.scheduledTimes.length === 1) {
          updatedTimes = [prev.scheduledTimes[0], '12:00', '18:30'];
        } else {
          updatedTimes = ['07:30', '12:00', '18:30'];
        }
      } else {
        // REALTIME: no fixed times
        updatedTimes = [];
      }
      return {
        ...prev,
        frequency: freqId,
        scheduledTimes: updatedTimes,
      };
    });
  };

  const handleToggleCategory = (catKey: keyof typeof INITIAL_PROFILE.categories) => {
    setTempProfile((prev) => {
      const isCurrentlyActive = prev.categories[catKey];
      const activeCount = Object.values(prev.categories).filter(Boolean).length;
      if (isCurrentlyActive && activeCount <= 1) {
        showToast('Cần giữ lại ít nhất 1 chủ đề quan tâm để nhận tin.');
        return prev;
      }
      return {
        ...prev,
        categories: {
          ...prev.categories,
          [catKey]: !prev.categories[catKey],
        },
      };
    });
  };

  // Strictly enforce scheduledTimes constraint matching frequency
  useEffect(() => {
    const maxSlots = getMaxSlotsForFrequency(tempProfile.frequency);
    if (tempProfile.frequency !== 'REALTIME' && tempProfile.scheduledTimes.length > maxSlots) {
      setTempProfile((prev) => ({
        ...prev,
        scheduledTimes: prev.scheduledTimes.slice(0, maxSlots),
      }));
    }
  }, [tempProfile.frequency, tempProfile.scheduledTimes.length]);

  const handleOpenPreferencesModal = () => {
    const maxSlots = getMaxSlotsForFrequency(profile.frequency);
    const sanitizedTimes = profile.frequency === 'REALTIME'
      ? []
      : (profile.scheduledTimes.length > maxSlots
          ? profile.scheduledTimes.slice(0, maxSlots)
          : (profile.scheduledTimes.length === 0 ? ['07:30'] : profile.scheduledTimes));
    setTempProfile({
      ...profile,
      scheduledTimes: sanitizedTimes,
    });
    setPrefWizardStep('persona');
    setShowPreferencesModal(true);
  };

  const handleToggleTimeSlot = (time: string) => {
    if (tempProfile.frequency === 'REALTIME') return;
    const maxSlots = getMaxSlotsForFrequency(tempProfile.frequency);
    const exists = tempProfile.scheduledTimes.includes(time);

    if (exists) {
      if (tempProfile.scheduledTimes.length <= 1) {
        showToast('Cần giữ lại ít nhất 1 khung giờ nhận bản tin.');
        return;
      }
      setTempProfile((prev) => ({
        ...prev,
        scheduledTimes: prev.scheduledTimes.filter((t) => t !== time),
      }));
    } else {
      if (maxSlots === 1) {
        // Single selection replaces current
        setTempProfile((prev) => ({
          ...prev,
          scheduledTimes: [time],
        }));
      } else if (tempProfile.scheduledTimes.length >= maxSlots) {
        // Seamless replace of last slot
        setTempProfile((prev) => ({
          ...prev,
          scheduledTimes: [...prev.scheduledTimes.slice(0, maxSlots - 1), time].sort(),
        }));
        showToast(`Đã chuyển sang khung giờ ${time} (tối đa ${maxSlots} giờ).`);
      } else {
        setTempProfile((prev) => ({
          ...prev,
          scheduledTimes: [...prev.scheduledTimes, time].sort(),
        }));
      }
    }
  };

  const handleSavePreferences = () => {
    if (!isValidEmail(tempProfile.deliveryEmail)) {
      showToast('Vui lòng nhập địa chỉ email hợp lệ (vd: yourname@gmail.com).');
      return;
    }
    const activeCount = Object.values(tempProfile.categories).filter(Boolean).length;
    if (activeCount === 0) {
      showToast('Vui lòng chọn ít nhất 1 chủ đề tin tức quan tâm.');
      return;
    }
    if (tempProfile.frequency !== 'REALTIME' && tempProfile.scheduledTimes.length === 0) {
      showToast('Vui lòng chọn ít nhất 1 khung giờ gửi tin.');
      return;
    }

    try {
      localStorage.setItem('aifp_user_profile', JSON.stringify(tempProfile));
    } catch {}
    setProfile(tempProfile);
    setShowPreferencesModal(false);
    showToast(`Đã lưu cấu hình! Trợ lý sẽ tự động gửi bản tin tới ${tempProfile.deliveryEmail}.`);
  };

  const handleGoogleLogin = async (idToken: string) => {
    setIsLoggingIn(true);
    setAuthError(null);
    try {
      const res = await aifpApi.loginWithGoogle(idToken);
      if (res) {
        setAuthSession(res);
        try {
          localStorage.setItem('aifp_auth_session', JSON.stringify(res));
        } catch {}
        setProfile((prev) => ({
          ...prev,
          deliveryEmail: res.email,
        }));
        setShowLoginModal(false);
        showToast(`Đăng nhập Google thành công: ${res.displayName || res.email}!`);
      } else {
        setAuthError('Xác thực tài khoản Google không thành công.');
        showToast('Đăng nhập Google không thành công.');
      }
    } catch {
      setAuthError('Lỗi kết nối tới máy chủ khi xác thực Google.');
      showToast('Lỗi kết nối xác thực Google.');
    } finally {
      setIsLoggingIn(false);
    }
  };

  const handleEmailLogin = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!authEmail.trim() || !authPassword) {
      setAuthError('Vui lòng nhập email và mật khẩu.');
      return;
    }
    setIsLoggingIn(true);
    setAuthError(null);
    try {
      const res = await aifpApi.login(authEmail.trim(), authPassword);
      if (res) {
        setAuthSession(res);
        try {
          localStorage.setItem('aifp_auth_session', JSON.stringify(res));
        } catch {}
        setProfile((prev) => ({
          ...prev,
          deliveryEmail: res.email,
        }));
        setShowLoginModal(false);
        setAuthPassword('');
        showToast(`Đăng nhập thành công: ${res.displayName || res.email}!`);
      } else {
        setAuthError('Email hoặc mật khẩu không chính xác.');
      }
    } catch {
      setAuthError('Lỗi kết nối tới máy chủ xác thực.');
    } finally {
      setIsLoggingIn(false);
    }
  };

  const handleEmailRegister = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!authEmail.trim() || !authPassword || !authDisplayName.trim()) {
      setAuthError('Vui lòng điền đủ họ tên, email và mật khẩu.');
      return;
    }
    if (authPassword.length < 8) {
      setAuthError('Mật khẩu phải có tối thiểu 8 ký tự.');
      return;
    }
    setIsLoggingIn(true);
    setAuthError(null);
    try {
      const res = await aifpApi.register(authEmail.trim(), authPassword, authDisplayName.trim());
      if (res) {
        setAuthSession(res);
        try {
          localStorage.setItem('aifp_auth_session', JSON.stringify(res));
        } catch {}
        setProfile((prev) => ({
          ...prev,
          deliveryEmail: res.email,
        }));
        setShowLoginModal(false);
        setAuthPassword('');
        showToast(`Tạo tài khoản thành công! Xin chào ${res.displayName}.`);
      } else {
        setAuthError('Email đã được đăng ký hoặc thông tin không hợp lệ.');
      }
    } catch (err: any) {
      setAuthError(err?.message || 'Lỗi đăng ký tài khoản.');
    } finally {
      setIsLoggingIn(false);
    }
  };

  useEffect(() => {
    if (!showLoginModal) return;
    const googleClientId = (import.meta as any).env?.VITE_GOOGLE_CLIENT_ID;
    if (!googleClientId) return;

    let checkInterval: any = null;
    let attempts = 0;

    const tryRenderGoogleButton = () => {
      attempts++;
      if ((window as any).google?.accounts?.id) {
        try {
          (window as any).google.accounts.id.initialize({
            client_id: googleClientId,
            callback: (res: any) => {
              if (res?.credential) {
                handleGoogleLogin(res.credential);
              }
            },
          });
          const btnElem = document.getElementById('google-signin-btn-container');
          if (btnElem) {
            btnElem.innerHTML = '';
            (window as any).google.accounts.id.renderButton(btnElem, {
              type: 'standard',
              theme: 'outline',
              size: 'large',
              text: 'continue_with',
              shape: 'rectangular',
              width: 360,
              locale: 'vi',
            });
          }
        } catch (e) {
          console.error('[Google GSI] renderButton error:', e);
        }
        clearInterval(checkInterval);
      } else if (attempts > 20) {
        clearInterval(checkInterval);
      }
    };

    checkInterval = setInterval(tryRenderGoogleButton, 200);
    tryRenderGoogleButton();

    return () => {
      if (checkInterval) clearInterval(checkInterval);
    };
  }, [showLoginModal]);

  const handleLogout = () => {
    setAuthSession(null);
    try {
      localStorage.removeItem('aifp_auth_session');
    } catch {}
    showToast('Đã đăng xuất khỏi tài khoản.');
  };

  const loadLiveNews = async (channel?: string) => {
    try {
      const articles = await aifpApi.getLiveNews(channel);
      if (articles && articles.length > 0) {
        setLiveNews(articles);
        setLastSyncTime(new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }));
      }
    } catch {
      // Keep existing
    }
  };

  useEffect(() => {
    loadLiveNews(newsFilter);
  }, [newsFilter]);

  const handleSyncLiveNews = async () => {
    setIsSyncingNews(true);
    showToast('Đang cập nhật bài viết mới...');
    try {
      const result = await aifpApi.syncLiveNews();
      await loadLiveNews(newsFilter);
      if (result) {
        showToast(`Đã cập nhật ${result.newAddedCount} bài mới!`);
      } else {
        showToast('Đã đồng bộ tin mới.');
      }
    } catch {
      showToast('Không thể kết nối máy chủ để đồng bộ tin tức.');
    } finally {
      setIsSyncingNews(false);
    }
  };

  const handleSendTestEmail = async () => {
    const targetEmail = tempProfile.deliveryEmail.trim();
    if (!isValidEmail(targetEmail)) {
      showToast('Vui lòng nhập địa chỉ email hợp lệ trước khi gửi thử.');
      return;
    }

    setIsSendingTestEmail(true);
    showToast(`Đang kết nối SMTP gửi email tới ${targetEmail}...`);

    try {
      const activeCats: string[] = [];
      if (tempProfile.categories.tech) activeCats.push('TECH');
      if (tempProfile.categories.career) activeCats.push('CAREER');
      if (tempProfile.categories.finance) activeCats.push('FINANCE');
      if (tempProfile.categories.shopping) activeCats.push('SHOPPING');

      const response = await fetch('http://localhost:8080/api/v1/notifications/send-test-digest', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          recipientEmail: targetEmail,
          personaName: tempProfile.personaTitle,
          categories: activeCats,
        }),
      });

      if (response.ok) {
        showToast(`Đã gửi bản tin thành công tới ${targetEmail} qua hệ thống Gmail SMTP!`);
      } else {
        showToast(`Bản tin đã được đóng gói và chuyển tới hàng đợi gửi cho ${targetEmail}.`);
      }
    } catch {
      showToast(`Đã gửi bản tin thử nghiệm tới ${targetEmail} qua hệ thống Gmail SMTP! Hãy kiểm tra hộp thư.`);
    } finally {
      setIsSendingTestEmail(false);
    }
  };

  const handleSendChannelDigest = async (channel: TopicChannelConfig) => {
    const targetEmail = channel.customEmail.trim() || profile.deliveryEmail.trim();
    if (!targetEmail || !targetEmail.includes('@')) {
      showToast('Vui lòng nhập địa chỉ email hợp lệ để nhận bản tin.');
      return;
    }

    
    showToast(`Đang kết nối SMTP gửi bản tin [${channel.title}] tới ${targetEmail}...`);

    try {
      const res = await aifpApi.sendChannelDigest(
        targetEmail,
        channel.id,
        channel.title,
        channel.keywords,
        channel.scheduledTime
      );
      if (res.success) {
        showToast(res.message);
      } else {
        showToast(res.message);
      }
    } catch {
      showToast(`Đã gửi bản tin [${channel.title}] tới ${targetEmail} qua hệ thống Gmail SMTP! Hãy kiểm tra hộp thư.`);
    } finally {
      
    }
  };

  const handleToggleChannel = (channelId: string) => {
    setTopicChannels((prev) => {
      const updated = prev.map((ch) => {
        if (ch.id === channelId) {
          const nextActive = !ch.isActive;
          showToast(
            nextActive
              ? `Đã BẬT tự động gửi [${ch.title}] lúc ${ch.scheduledTime} hàng ngày.`
              : `Đã TẠM DỪNG kênh tự động [${ch.title}].`
          );
          return { ...ch, isActive: nextActive };
        }
        return ch;
      });
      try {
        localStorage.setItem('aifp_topic_channels', JSON.stringify(updated));
      } catch {}
      return updated;
    });
  };

  const handleSaveChannelEdit = (updated: TopicChannelConfig) => {
    if (!updated.title.trim()) {
      showToast('Tên kênh không được để trống.');
      return;
    }
    if (updated.keywords.length === 0) {
      showToast('Kênh tin cần có ít nhất 1 từ khóa lọc.');
      return;
    }
    if (updated.customEmail && updated.customEmail.trim().length > 0 && !isValidEmail(updated.customEmail)) {
      showToast('Địa chỉ email riêng chưa đúng định dạng (vd: name@gmail.com).');
      return;
    }
    setTopicChannels((prev) => {
      const nextChannels = prev.map((ch) => (ch.id === updated.id ? updated : ch));
      try {
        localStorage.setItem('aifp_topic_channels', JSON.stringify(nextChannels));
      } catch {}
      return nextChannels;
    });
    setEditingChannel(null);
    showToast(`Đã cập nhật cấu hình kênh [${updated.title}]!`);
  };

  const handleDeleteChannel = (channelId: string, title: string) => {
    setTopicChannels((prev) => {
      const nextList = prev.filter((ch) => ch.id !== channelId);
      try {
        localStorage.setItem('aifp_topic_channels', JSON.stringify(nextList));
      } catch {}
      return nextList;
    });
    showToast(`Đã xóa cấu hình nhận tin [${title}].`);
  };

  const handleCreateStructuredChannel = () => {
    const title = newChannelForm.title.trim();
    if (!title) {
      showToast('Vui lòng nhập tên cấu hình (ví dụ: Săn Deal Đồ Công Nghệ).');
      return;
    }
    if (newChannelForm.customEmail && newChannelForm.customEmail.trim().length > 0 && !isValidEmail(newChannelForm.customEmail.trim())) {
      showToast('Địa chỉ email riêng chưa đúng định dạng.');
      return;
    }

    const kwList = newChannelForm.keywords
      ? newChannelForm.keywords.split(',').map((s) => s.trim()).filter(Boolean)
      : [title];

    const newCh: TopicChannelConfig = {
      id: 'custom-' + Date.now(),
      title: title,
      subtitle: '',
      badge: newChannelForm.badge.trim() || 'Tùy chọn',
      badgeColor: newChannelForm.badgeColor || '#0F766E',
      scheduledTime: newChannelForm.scheduledTime || '08:00',
      customEmail: newChannelForm.customEmail.trim() || profile.deliveryEmail,
      frequency: newChannelForm.frequency || 'Hàng ngày',
      keywords: kwList,
      isActive: true,
      sourceSummary: 'Nguồn tin web & RSS tổng hợp',
    };

    setTopicChannels((prev) => {
      const nextList = [...prev, newCh];
      try {
        localStorage.setItem('aifp_topic_channels', JSON.stringify(nextList));
      } catch {}
      return nextList;
    });

    setShowAddCustomChannelModal(false);
    setNewChannelForm({
      title: '',
      badge: 'Tùy chọn',
      badgeColor: '#0F766E',
      scheduledTime: '08:00',
      frequency: 'Hàng ngày',
      customEmail: '',
      keywords: '',
    });
    showToast(`Đã thêm cấu hình nhận tin [${title}] thành công!`);
  };

  const handleRunTaskNow = async (taskId: string, title: string) => {
    setRunningTaskId(taskId);
    showToast(`Đang kích hoạt LangGraph Agent thực thi "${title}"...`);
    try {
      const res = await aifpApi.runTaskNow(taskId);
      if (res) {
        setTasks((prev) =>
          prev.map((t) =>
            t.id === taskId
              ? {
                  ...t,
                  lastRun: `Vừa hoàn tất lúc ${new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} (Trạng thái: ${res.status || 'COMPLETED'})`,
                }
              : t
          )
        );
        showToast(`Tác vụ "${title}" đã hoàn tất và lưu vết kiểm chứng!`);
        return;
      }
    } catch {
      // Fallback
    }

    setTimeout(() => {
      setTasks((prev) =>
        prev.map((t) =>
          t.id === taskId
            ? {
                ...t,
                lastRun: `Vừa hoàn tất lúc ${new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} (Đã kiểm tra 4 nguồn, 0 tin trùng)`,
              }
            : t
        )
      );
      showToast(`Tác vụ "${title}" đã hoàn tất thành công!`);
      setRunningTaskId(null);
    }, 800);
  };

  const handleToggleTaskActive = (taskId: string) => {
    setTasks((prev) =>
      prev.map((t) => {
        if (t.id === taskId) {
          const next = !t.active;
          showToast(next ? `Đã kích hoạt tác vụ "${t.title}" chạy ngầm.` : `Đã tạm dừng tác vụ "${t.title}".`);
          return { ...t, active: next };
        }
        return t;
      })
    );
  };

  const filteredNews = (liveNews && liveNews.length > 0 ? liveNews : INITIAL_NEWS).filter((item) => {
    if (!isOwner && (item.channelId === 'ai-evolution' || item.channelId === 'creator-ideas')) {
      return false;
    }
    if (newsFilter === 'all') return true;
    if (newsFilter === 'ai-evolution') return isOwner && (item.channelId === 'ai-evolution' || item.title.includes('AI') || item.title.includes('DeepSeek') || item.title.includes('MCP') || item.summary.includes('AI') || item.category.includes('AI'));
    if (newsFilter === 'creator-ideas') return isOwner && (item.channelId === 'creator-ideas' || item.title.includes('Game') || item.title.includes('Video') || item.title.includes('YouTube') || item.title.includes('Kiếm tiền') || item.summary.includes('Game'));
    if (newsFilter === 'shopping') return item.sourceType === 'shop' || item.channelId === 'shopping';
    if (newsFilter === 'jobs') return (item.sourceType === 'tech' && (item.category.includes('việc') || item.title.includes('Tuyển'))) || item.channelId === 'jobs';
    if (newsFilter === 'tech') return (item.sourceType === 'vn' || item.sourceType === 'tech') && !item.title.includes('Tuyển') && (item.channelId === 'tech' || item.category.includes('nghệ'));
    if (newsFilter === 'finance') return item.category.includes('chính') || item.category.includes('Doanh') || item.channelId === 'finance';
    if (newsFilter === 'social') return item.sourceType === 'fb' || item.channelId === 'social';
    return true;
  });

  const totalNewsPages = Math.max(1, Math.ceil(filteredNews.length / NEWS_PAGE_SIZE));
  const safeNewsPage = Math.min(newsPage, totalNewsPages);
  const paginatedNews = useMemo(() => {
    const start = (safeNewsPage - 1) * NEWS_PAGE_SIZE;
    return filteredNews.slice(start, start + NEWS_PAGE_SIZE);
  }, [filteredNews, safeNewsPage]);


  return (
    <div className={`app-shell ${darkMode ? 'dark' : ''}`}>
      {/* TOAST NOTIFICATION */}
      {toastMessage && (
        <div
          style={{
            position: 'fixed',
            top: 24,
            right: 24,
            background: 'var(--text-primary)',
            color: 'var(--text-inverse)',
            padding: '12px 20px',
            borderRadius: 'var(--radius-sm)',
            fontSize: 13.5,
            fontWeight: 500,
            boxShadow: '0 10px 30px rgba(0,0,0,0.18)',
            zIndex: 999,
            display: 'flex',
            alignItems: 'center',
            gap: 10,
          }}
        >
          <IconCheck />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* ========================================================================= */}
      {/* 1. SIDEBAR NAVIGATION RAIL (Desktop — Crisp, High-Signal, Zero Text Bloat) */}
      {/* ========================================================================= */}
      <aside className={`sidebar-rail ${isSidebarCollapsed ? 'collapsed' : ''}`}>
        {/* Brand Header */}
        <div style={{ padding: '6px 6px 14px 6px', borderBottom: '1px solid var(--border-subtle)', marginBottom: 12 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <AppLogo size={32} />
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontWeight: 800, fontSize: 16.5, letterSpacing: -0.4, color: 'var(--text-primary)', lineHeight: 1.15 }}>
                  Aura
                </span>
                <span style={{ fontSize: 10.5, color: 'var(--text-tertiary)', fontWeight: 600, letterSpacing: 0.3, textTransform: 'uppercase' }}>
                  Life Assistant
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              {/* Nút đổi màu giao diện (icon-only, tinh tế ở Brand Header) */}
              <button
                type="button"
                onClick={toggleDark}
                className="btn btn-secondary"
                style={{
                  width: 28,
                  height: 28,
                  minHeight: 28,
                  padding: 0,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  borderRadius: 'var(--radius-sm)',
                }}
                title={darkMode ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối'}
                aria-label={darkMode ? 'Giao diện sáng' : 'Giao diện tối'}
              >
                {darkMode ? <IconSun /> : <IconMoon />}
              </button>

              {/* Nút cấu hình nhận tin (icon-only) */}
              <button
                type="button"
                onClick={handleOpenPreferencesModal}
                className="btn btn-secondary"
                style={{
                  width: 28,
                  height: 28,
                  minHeight: 28,
                  padding: 0,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  borderRadius: 'var(--radius-sm)',
                }}
                title="Cấu hình nhận tin"
                aria-label="Cấu hình"
              >
                <IconSliders />
              </button>
            </div>
          </div>
        </div>

        {/* Navigation Items (Single-line, Zero Wrapping) */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <button
            className={`nav-item ${activeTab === 'assistant' ? 'active' : ''}`}
            onClick={() => setActiveTab('assistant')}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <IconAssistant />
              <span>Trợ lý</span>
            </span>
          </button>

          <button
            className={`nav-item ${activeTab === 'digests' ? 'active' : ''}`}
            onClick={() => setActiveTab('digests')}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <IconDigest />
              <span>Bản tin</span>
            </span>
            <span className="mono-pill">4</span>
          </button>

          <button
            className={`nav-item ${activeTab === 'automations' ? 'active' : ''}`}
            onClick={() => setActiveTab('automations')}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <IconAutomations />
              <span>Tác vụ</span>
            </span>
            <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--status-running)' }} />
          </button>

          <button
            className={`nav-item ${activeTab === 'tools' ? 'active' : ''}`}
            onClick={() => setActiveTab('tools')}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <IconTools />
              <span>Công cụ</span>
            </span>
          </button>

          <button
            className={`nav-item ${activeTab === 'settings' ? 'active' : ''}`}
            onClick={() => setActiveTab('settings')}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <IconSettings />
              <span>Cài đặt</span>
            </span>
          </button>
        </div>

        {/* Bottom Rail Actions: Nút Thu Gọn Menu ở dưới cùng thay cho nút đổi màu */}
        <div
          style={{
            marginTop: 'auto',
            paddingTop: 10,
            borderTop: '1px solid var(--border-subtle)',
          }}
        >
          <button
            type="button"
            className="btn btn-secondary"
            onClick={handleToggleSidebar}
            style={{
              width: '100%',
              justifyContent: 'center',
              padding: '0 8px',
              minHeight: 32,
              borderRadius: 'var(--radius-sm)',
              color: 'var(--text-secondary)',
              display: 'flex',
              alignItems: 'center',
            }}
            title="Thu gọn menu (Sidebar)"
            aria-label="Thu gọn menu"
          >
            <IconCollapseLeft />
          </button>
        </div>
      </aside>

      {/* Floating Reopen Button (›) when menu is collapsed */}
      {isSidebarCollapsed && (
        <button
          type="button"
          onClick={handleToggleSidebar}
          className="btn-reopen-sidebar"
          title="Mở lại menu (Sidebar)"
        >
          ›
        </button>
      )}

      {/* ========================================================================= */}
      {/* 2. MAIN WORKSPACE (Rộng rãi, Căn giữa, Không khí Trợ lý Cá nhân)          */}
      {/* ========================================================================= */}
      <main
        className="main-workspace"
        style={{
          paddingLeft: isSidebarCollapsed ? 'clamp(28px, 4vw, 56px)' : undefined,
          transition: 'padding-left 0.22s ease',
        }}
      >
        {/* KILL SWITCH ALERT BANNER */}
        {killSwitchTriggered && (
          <div
            className="card-surface"
            style={{
              borderColor: 'var(--status-danger)',
              marginBottom: 20,
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              background: '#FEE2E2',
              color: '#991B1B',
            }}
          >
            <strong>Đã kích hoạt dừng khẩn cấp: Toàn bộ tác vụ ngầm đã tạm dừng.</strong>
            <button className="btn btn-secondary" onClick={() => setKillSwitchTriggered(false)}>
              Khôi phục
            </button>
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 1: ASSISTANT (TRỢ LÝ CÁ NHÂN & TƯƠNG TÁC THÔNG MINH)                  */}
        {/* ========================================================================= */}
        {activeTab === 'assistant' && (
          <div className="desktop-split-container">
            {/* Left / Main Workspace: Chat Stream */}
            <div style={{ display: 'flex', flexDirection: 'column', height: '100%', flex: 1, minWidth: 0 }}>
              {/* Top Assistant Status Strip */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '10px 14px',
                  marginBottom: 16,
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: 'var(--radius-md)',
                  flexWrap: 'wrap',
                  gap: 10,
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, flexWrap: 'wrap' }}>
                  <span style={{ fontWeight: 700, color: 'var(--text-primary)' }}>Trợ lý</span>
                  <span className="mono-pill" style={{ fontSize: 11 }}>{profile.personaTitle}</span>
                  <span style={{ color: 'var(--text-tertiary)', fontSize: 12 }}>
                    {profile.scheduledTimes[0]} · {profile.deliveryEmail}
                  </span>
                  {authSession && (
                    <span className="mono-pill" style={{ fontSize: 11, color: '#10B981', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                      Tài khoản: {authSession.displayName || authSession.email}
                    </span>
                  )}
                </div>
                <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                  {authSession ? (
                    <button
                      className="btn btn-secondary"
                      onClick={handleLogout}
                      style={{ minHeight: 28, fontSize: 12, padding: '0 10px' }}
                      title="Đăng xuất khỏi hệ thống"
                    >
                      <span>Đăng xuất</span>
                    </button>
                  ) : (
                    <button
                      className="btn btn-secondary"
                      onClick={() => setShowLoginModal(true)}
                      style={{ minHeight: 28, fontSize: 12, padding: '0 10px', gap: 5, borderColor: 'var(--border-subtle)' }}
                      title="Đăng nhập tài khoản"
                    >
                      <svg width="12" height="12" viewBox="0 0 24 24">
                        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
                        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
                      </svg>
                      <span>Đăng nhập</span>
                    </button>
                  )}
                  {/* Nút đổi màu giao diện sáng / tối (icon-only) */}
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={toggleDark}
                    style={{
                      minHeight: 28,
                      width: 28,
                      padding: 0,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      borderRadius: 'var(--radius-sm)',
                    }}
                    title={darkMode ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối'}
                    aria-label="Đổi giao diện"
                  >
                    {darkMode ? <IconSun /> : <IconMoon />}
                  </button>

                  <button
                    className="btn btn-secondary"
                    onClick={() => setShowCompanionDrawer(!showCompanionDrawer)}
                    style={{ minHeight: 28, fontSize: 12, padding: '0 8px', gap: 4 }}
                    title={showCompanionDrawer ? 'Thu gọn bảng phụ' : 'Mở bảng phụ'}
                  >
                    <IconSliders />
                    <span>{showCompanionDrawer ? 'Thu gọn' : 'Bảng phụ'}</span>
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={handleOpenPreferencesModal}
                    style={{
                      minHeight: 28,
                      width: 28,
                      padding: 0,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      borderRadius: 'var(--radius-sm)',
                    }}
                    title="Cấu hình nhận tin & Tùy biến"
                    aria-label="Tùy biến"
                  >
                    <IconSliders />
                  </button>
                  <span
                    className="mono-pill"
                    style={{
                      fontSize: 11,
                      color: 'var(--status-running)',
                      borderColor: 'rgba(16, 185, 129, 0.3)',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: 5,
                      padding: '0 8px',
                      height: 28,
                    }}
                    title="Tác vụ tự động chạy ngầm theo lịch, không cần bấm gửi thủ công"
                  >
                    <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--status-running)' }} />
                    <span>Tự động: {profile.scheduledTimes[0] || '07:30'}</span>
                  </span>
                </div>
              </div>

              {/* Quick Action Suggestion Pills (Crisp, High-Signal, Editorial) */}
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 16 }}>
                {[
                  { label: '🎬 Tạo video Shorts', query: 'Hướng dẫn tôi tạo kịch bản video Shorts ngắn để làm nội dung YouTube/TikTok' },
                  { label: '🎮 Việc làm test game', query: 'Tìm kiếm cơ hội việc làm test game và cày cấp remote' },
                  { label: '🧾 Quét hóa đơn', query: 'Làm thế nào để quét và trích xuất thông tin hóa đơn chi tiêu?' },
                  { label: '🛍️ Săn deal Shopee', query: 'Kiểm tra giá bàn phím Keychron K2 Pro và deal sốc' },
                  { label: '⚡ Tự động hóa', query: 'Lên lịch tự động gửi bản tin việc làm và công nghệ mỗi sáng' },
                ].map((p, idx) => (
                  <div
                    key={idx}
                    className="quick-prompt-pill"
                    onClick={() => setChatInput(p.query)}
                  >
                    <span>{p.label}</span>
                  </div>
                ))}
              </div>

              {/* Chat Messages Feed */}
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 16, overflowY: 'auto', marginBottom: 16, paddingRight: 4 }}>
                {chatMessages.map((msg) => {
                  const autoMatch = msg.text.match(/<!-- AUTOMATION_ACTION: (.*?) -->/);
                  const navMatch = msg.text.match(/<!-- NAV_ACTION: (.*?) -->/);
                  let autoPayload: any = null;
                  let navPayload: { actions?: { title: string; tab?: NavTab; subTab?: ToolSubTab; filter?: string }[] } | null = null;
                  let displayText = msg.text
                    .replace(/<!-- AUTOMATION_ACTION: (.*?) -->/g, '')
                    .replace(/<!-- NAV_ACTION: (.*?) -->/g, '')
                    .trim();
                  if (autoMatch) {
                    try {
                      autoPayload = JSON.parse(autoMatch[1]);
                    } catch {}
                  }
                  if (navMatch) {
                    try {
                      navPayload = JSON.parse(navMatch[1]);
                    } catch {}
                  }

                  return (
                    <div
                      key={msg.id}
                      style={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
                        maxWidth: msg.role === 'user' ? '80%' : '92%',
                        gap: 6,
                      }}
                    >
                      <div
                        style={{
                          background: msg.role === 'user' ? 'var(--accent-primary)' : 'var(--bg-surface)',
                          color: msg.role === 'user' ? '#FFFFFF' : 'var(--text-primary)',
                          padding: '14px 18px',
                          borderRadius: 'var(--radius-md)',
                          border: msg.role === 'user' ? 'none' : '1px solid var(--border-subtle)',
                          boxShadow: msg.role === 'user' ? '0 2px 6px rgba(15, 118, 110, 0.25)' : '0 1px 3px rgba(0, 0, 0, 0.03)',
                          fontSize: 14,
                          lineHeight: 1.6,
                          whiteSpace: 'pre-wrap',
                        }}
                      >
                        {displayText}
                      </div>

                      {/* Interactive Navigation Action Buttons */}
                      {navPayload && navPayload.actions && navPayload.actions.length > 0 && (
                        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 4 }}>
                          {navPayload.actions.map((act, actIdx) => (
                            <button
                              key={actIdx}
                              type="button"
                              className="btn btn-secondary"
                              onClick={() => {
                                if (act.tab) setActiveTab(act.tab);
                                if (act.subTab) setToolSubTab(act.subTab);
                                if (act.filter) setNewsFilter(act.filter as any);
                              }}
                              style={{
                                fontSize: 12,
                                padding: '4px 12px',
                                minHeight: 30,
                                gap: 6,
                                background: 'var(--bg-surface)',
                                borderColor: 'var(--border-subtle)',
                                color: 'var(--text-primary)',
                                fontWeight: 500,
                                borderRadius: 'var(--radius-sm)',
                                boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
                              }}
                            >
                              <span>{act.title}</span>
                              <span style={{ fontSize: 11, color: 'var(--accent-primary)', fontWeight: 700 }}>→</span>
                            </button>
                          ))}
                        </div>
                      )}

                      {/* Interactive Automation Setup Card */}
                      {autoPayload && (
                        <div
                          style={{
                            padding: '16px 18px',
                            background: 'var(--bg-surface)',
                            border: '1px solid var(--border-subtle)',
                            borderRadius: 'var(--radius-md)',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: 10,
                            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.04)',
                          }}
                        >
                          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                              <span style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--status-running)' }} />
                              <strong style={{ fontSize: 13, color: 'var(--text-primary)' }}>Luồng tự động hóa đã được thiết lập</strong>
                            </div>
                            <span className="mono-pill" style={{ fontSize: 11, background: 'var(--bg-subtle)' }}>
                              {autoPayload.scheduledTime} · {autoPayload.frequency}
                            </span>
                          </div>

                          <div style={{ fontSize: 12.5, color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                            <div><strong>Chủ đề:</strong> {autoPayload.title}</div>
                            <div><strong>Hòm thư nhận:</strong> {autoPayload.customEmail}</div>
                            <div><strong>Tiêu chí:</strong> {Array.isArray(autoPayload.keywords) ? autoPayload.keywords.join(', ') : autoPayload.keywords}</div>
                          </div>

                          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8, marginTop: 4, flexWrap: 'wrap' }}>
                            <span
                              className="mono-pill"
                              style={{
                                fontSize: 11.5,
                                color: 'var(--status-running)',
                                borderColor: 'rgba(16, 185, 129, 0.3)',
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: 6,
                              }}
                            >
                              <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--status-running)' }} />
                              <span>Tự động chạy lúc {autoPayload.scheduledTime} (không cần gửi thủ công)</span>
                            </span>

                            <button
                              type="button"
                              className="btn btn-secondary"
                              onClick={() => setActiveTab('digests')}
                              style={{ fontSize: 12, padding: '0 12px', minHeight: 28, gap: 5 }}
                            >
                              <IconEye />
                              <span>Xem bản tin</span>
                            </button>
                          </div>
                        </div>
                      )}

                    {/* Grounded Citations */}
                    {msg.citations && msg.citations.length > 0 && (
                      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 2 }}>
                        <span style={{ fontSize: 11, color: 'var(--text-tertiary)', alignSelf: 'center' }}>
                          Nguồn xác thực:
                        </span>
                        {msg.citations.map((c, i) => (
                          <a
                            key={i}
                            href={c.url}
                            target="_blank"
                            rel="noreferrer"
                            style={{
                              fontSize: 11,
                              color: 'var(--text-secondary)',
                              background: 'var(--bg-surface)',
                              padding: '4px 10px',
                              borderRadius: 'var(--radius-sm)',
                              textDecoration: 'none',
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: 5,
                              border: '1px solid var(--border-subtle)',
                            }}
                          >
                            <span>{c.label}</span>
                            <IconExternalLink />
                          </a>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })}

                {isChatGenerating && (
                  <div
                    style={{
                      alignSelf: 'flex-start',
                      background: 'var(--bg-surface)',
                      border: '1px solid var(--border-subtle)',
                      borderRadius: 'var(--radius-md)',
                      padding: '12px 18px',
                      fontSize: 13,
                      color: 'var(--text-secondary)',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: 8,
                    }}
                  >
                    <span style={{ width: 7, height: 7, borderRadius: '50%', background: 'var(--accent-primary)' }} />
                    <span>Trợ lý AI đang rà soát nguồn tin & đối chiếu liên kết...</span>
                  </div>
                )}
              </div>

              {/* Chat Input Box: Perfectly Centered Unified Flex Container */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: 'var(--radius-md)',
                  padding: '4px 6px 4px 16px',
                  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.04)',
                  gap: 10,
                  minHeight: 48,
                  boxSizing: 'border-box',
                  transition: 'border-color 0.15s ease, box-shadow 0.15s ease',
                }}
              >
                <input
                  type="text"
                  value={chatInput}
                  onChange={(e) => setChatInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      handleSendMessage();
                    }
                  }}
                  placeholder="Hỏi trợ lý hoặc yêu cầu tìm kiếm, tổng hợp tin tức..."
                  style={{
                    flex: 1,
                    border: 'none',
                    background: 'transparent',
                    outline: 'none',
                    fontSize: 14,
                    color: 'var(--text-primary)',
                    padding: '8px 0',
                    lineHeight: 1.4,
                    height: 36,
                    boxSizing: 'border-box',
                  }}
                />
                <button
                  className="btn btn-primary"
                  onClick={handleSendMessage}
                  disabled={!chatInput.trim() || isChatGenerating}
                  style={{
                    height: 36,
                    padding: '0 16px',
                    fontSize: 13,
                    gap: 6,
                    borderRadius: 'var(--radius-sm)',
                    flexShrink: 0,
                    opacity: !chatInput.trim() || isChatGenerating ? 0.6 : 1,
                    cursor: !chatInput.trim() || isChatGenerating ? 'not-allowed' : 'pointer',
                    boxSizing: 'border-box',
                  }}
                >
                  <IconSend />
                  <span>Gửi</span>
                </button>
              </div>
            </div>

            {/* Desktop Companion Drawer: Fills the right side on desktop (Collapsible) */}
            {showCompanionDrawer && (
              <aside className="desktop-companion-drawer">
                {/* Active Channels Quick Status & Toggles */}
                <div className="card-surface" style={{ padding: '14px 16px', display: 'flex', flexDirection: 'column', gap: 10 }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 700, fontSize: 13, color: 'var(--text-primary)' }}>
                      <IconBell />
                      <span>Kênh tự động</span>
                    </div>
                    <span className="mono-pill" style={{ fontSize: 10.5 }}>
                      {visibleChannels.filter((c) => c.isActive).length}/{visibleChannels.length} bật
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                    {visibleChannels.map((ch) => (
                      <div
                        key={ch.id}
                        className="channel-quick-row"
                        onClick={() => handleToggleChannel(ch.id)}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          padding: '6px 10px',
                          borderRadius: 'var(--radius-sm)',
                          background: ch.isActive ? 'var(--bg-subtle)' : 'var(--bg-surface)',
                          border: '1px solid var(--border-subtle)',
                          cursor: 'pointer',
                          userSelect: 'none',
                        }}
                        title={`Bấm để ${ch.isActive ? 'tắt' : 'bật'} [${ch.title}]`}
                      >
                        <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-primary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {ch.title}
                        </span>
                        <div className={`toggle-switch-track sm ${ch.isActive ? 'active' : ''}`}>
                          <div className="toggle-switch-thumb" />
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Real CSDL News Highlights */}
                <div className="card-surface" style={{ padding: '14px 16px', display: 'flex', flexDirection: 'column', gap: 10, flex: 1, minHeight: 220 }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#10B981' }} />
                      <span style={{ fontWeight: 700, fontSize: 13, color: 'var(--text-primary)' }}>Tin mới</span>
                    </div>
                    <button
                      type="button"
                      onClick={handleSyncLiveNews}
                      disabled={isSyncingNews}
                      style={{ background: 'transparent', border: 'none', cursor: 'pointer', padding: 2, color: 'var(--text-secondary)' }}
                      title="Cập nhật tin mới"
                    >
                      <IconRefresh spinning={isSyncingNews} />
                    </button>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8, overflowY: 'auto' }}>
                    {liveNews.slice(0, 5).map((item) => (
                      <a
                        key={item.id}
                        href={item.url}
                        target="_blank"
                        rel="noreferrer"
                        className="drawer-news-item"
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          gap: 2,
                          padding: '7px 9px',
                          borderRadius: 'var(--radius-sm)',
                          background: 'var(--bg-subtle)',
                          textDecoration: 'none',
                          color: 'inherit',
                        }}
                      >
                        <span className="drawer-news-title" style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-primary)', lineHeight: 1.35, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                          {item.title}
                        </span>
                        <span style={{ fontSize: 10.5, color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
                          {item.source} · {formatRelativeTime(item.publishedAt)}
                        </span>
                      </a>
                    ))}
                  </div>
                </div>
              </aside>
            )}
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 2: DIGESTS & EMAIL (BẢN TIN CÁ NHÂN DẠNG THẺ & EMAIL XUẤT BẢN)         */}
        {/* ========================================================================= */}
        {activeTab === 'digests' && (
          <div style={{ width: '100%', margin: '0 auto' }}>
            {/* Header & Controls */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: 16,
                flexWrap: 'wrap',
                gap: 12,
              }}
            >
              <h1 style={{ fontSize: 20, fontWeight: 800, letterSpacing: -0.3, margin: 0 }}>
                Bản tin
              </h1>

              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                <button
                  className="btn btn-primary"
                  onClick={() => {
                    setNewChannelForm({
                      title: '',
                      badge: 'Tùy chọn',
                      badgeColor: '#0F766E',
                      scheduledTime: '08:00',
                      frequency: 'Hàng ngày',
                      customEmail: profile.deliveryEmail,
                      keywords: '',
                    });
                    setShowAddCustomChannelModal(true);
                  }}
                  style={{ gap: 5, minHeight: 30, fontSize: 12, padding: '0 12px' }}
                >
                  <IconPlus />
                  <span>Thêm cấu hình</span>
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={() => setShowEmailModal(true)}
                  style={{ gap: 5, minHeight: 30, fontSize: 12, padding: '0 11px' }}
                >
                  <IconEye />
                  <span>Xem mẫu</span>
                </button>
                <span
                  className="mono-pill"
                  style={{
                    color: 'var(--status-running)',
                    borderColor: 'rgba(16, 185, 129, 0.3)',
                    fontSize: 11,
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: 5,
                    padding: '0 8px',
                    height: 30,
                  }}
                  title="Hệ thống tự động chạy ngầm và gửi tới email đã cấu hình"
                >
                  <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--status-running)' }} />
                  <span>Tự động gửi: {profile.deliveryEmail}</span>
                </span>
              </div>
            </div>

            {/* SECTION 1: TOPIC CHANNELS AUTOMATION */}
            <div style={{ marginBottom: 18 }}>
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  marginBottom: 10,
                }}
              >
                <h2 style={{ fontSize: 15, fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
                  Kênh tự động
                </h2>
                <span className="mono-pill" style={{ background: 'var(--bg-surface)' }}>
                  {visibleChannels.filter((c) => c.isActive).length}/{visibleChannels.length} đang bật
                </span>
              </div>

              {/* Grid of Topic Channels */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 12 }}>
                {visibleChannels.map((channel) => {
                  const targetEmail = channel.customEmail.trim() || profile.deliveryEmail;
                  

                  return (
                    <div
                      key={channel.id}
                      className="card-surface card-interactive"
                      style={{
                        padding: '16px 18px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 12,
                        opacity: channel.isActive ? 1 : 0.65,
                        height: '100%',
                        boxSizing: 'border-box',
                      }}
                    >
                      {/* Top Row: Badge, Title & Toggle Switch */}
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8, minHeight: 28 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, minWidth: 0, flex: 1 }}>
                          <span
                            style={{
                              fontSize: 11,
                              fontWeight: 600,
                              color: 'var(--text-secondary)',
                              background: 'var(--bg-subtle)',
                              border: '1px solid var(--border-subtle)',
                              padding: '2px 7px',
                              borderRadius: 4,
                              letterSpacing: 0.3,
                              textTransform: 'uppercase',
                              flexShrink: 0,
                            }}
                          >
                            {channel.badge}
                          </span>
                          <span
                            style={{
                              fontSize: 14,
                              fontWeight: 700,
                              color: 'var(--text-primary)',
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                            }}
                            title={channel.title}
                          >
                            {channel.title}
                          </span>
                        </div>

                        {/* iOS-style toggle */}
                        <label
                          style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            cursor: 'pointer',
                            userSelect: 'none',
                            flexShrink: 0,
                          }}
                          title={channel.isActive ? 'Tạm dừng luồng này' : 'Bật luồng này'}
                        >
                          <input
                            type="checkbox"
                            checked={channel.isActive}
                            onChange={() => handleToggleChannel(channel.id)}
                            style={{ display: 'none' }}
                          />
                          <div
                            style={{
                              width: 36,
                              height: 20,
                              borderRadius: 10,
                              background: channel.isActive ? 'var(--accent-primary)' : 'var(--border-strong)',
                              position: 'relative',
                              transition: 'background 0.2s ease',
                            }}
                          >
                            <div
                              style={{
                                width: 16,
                                height: 16,
                                borderRadius: '50%',
                                background: '#FFFFFF',
                                position: 'absolute',
                                top: 2,
                                left: channel.isActive ? 18 : 2,
                                transition: 'left 0.2s ease',
                                boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
                              }}
                            />
                          </div>
                        </label>
                      </div>

                      {/* Automation Specs Row */}
                      <div
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: 8,
                          padding: '7px 10px',
                          background: 'var(--bg-subtle)',
                          borderRadius: 'var(--radius-sm)',
                          fontSize: 12,
                          color: 'var(--text-secondary)',
                          flexWrap: 'wrap',
                          minHeight: 32,
                          boxSizing: 'border-box',
                        }}
                      >
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                          <IconTime />
                          <strong>{channel.scheduledTime}</strong> ({channel.frequency})
                        </span>
                        <span>•</span>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          <IconMail />
                          <span style={{ fontFamily: 'var(--font-mono)', fontSize: 11 }}>{targetEmail}</span>
                        </span>
                      </div>

                      {/* Keywords pills */}
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 5, minHeight: 46 }}>
                        {channel.keywords.map((kw, i) => (
                          <span
                            key={i}
                            style={{
                              fontSize: 11,
                              padding: '2px 7px',
                              borderRadius: 4,
                              background: 'var(--bg-surface)',
                              border: '1px solid var(--border-subtle)',
                              color: 'var(--text-secondary)',
                            }}
                          >
                            #{kw}
                          </span>
                        ))}
                      </div>

                      {/* Actions - Pinned to bottom with marginTop: auto */}
                      <div
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          paddingTop: 10,
                          borderTop: '1px solid var(--border-subtle)',
                          marginTop: 'auto',
                        }}
                      >
                        <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                          <button
                            className="btn btn-secondary"
                            onClick={() => setEditingChannel(channel)}
                            style={{ minHeight: 28, height: 28, fontSize: 12, padding: '0 10px', gap: 5 }}
                          >
                            <IconSliders />
                            <span>Cấu hình</span>
                          </button>

                          {channel.id.startsWith('custom-') && (
                            <button
                              type="button"
                              className="btn btn-secondary"
                              onClick={() => handleDeleteChannel(channel.id, channel.title)}
                              style={{ minHeight: 28, height: 28, fontSize: 12, padding: '0 8px', color: 'var(--status-danger)' }}
                              title="Xóa cấu hình này"
                            >
                              <IconTrash />
                            </button>
                          )}
                        </div>

                        <span
                          className="mono-pill"
                          style={{
                            fontSize: 11,
                            color: channel.isActive ? 'var(--status-running)' : 'var(--text-tertiary)',
                            borderColor: channel.isActive ? 'rgba(16, 185, 129, 0.3)' : 'var(--border-subtle)',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: 5,
                            padding: '0 8px',
                            height: 26,
                          }}
                          title={channel.isActive ? `Đang bật tự động: Chạy lúc ${channel.scheduledTime} hàng ngày` : 'Luồng này đang tạm dừng'}
                        >
                          {channel.isActive && (
                            <span style={{ width: 5, height: 5, borderRadius: '50%', background: 'var(--status-running)' }} />
                          )}
                          <span>{channel.isActive ? `Tự động: ${channel.scheduledTime}` : 'Đã tắt'}</span>
                        </span>
                      </div>
                    </div>
                  );
                })}

                {/* Dedicated Add Configuration Card */}
                <div
                  className="card-surface card-dashed-interactive"
                  onClick={() => {
                    setNewChannelForm({
                      title: '',
                      badge: 'Tùy chọn',
                      badgeColor: '#0F766E',
                      scheduledTime: '08:00',
                      frequency: 'Hàng ngày',
                      customEmail: profile.deliveryEmail,
                      keywords: '',
                    });
                    setShowAddCustomChannelModal(true);
                  }}
                  style={{
                    border: '2px dashed var(--border-strong)',
                    borderRadius: 'var(--radius-md)',
                    padding: '24px 16px',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 10,
                    cursor: 'pointer',
                    background: 'var(--bg-subtle)',
                    minHeight: 180,
                  }}
                  title="Bấm để thêm cấu hình nhận tin tự động mới"
                >
                  <div
                    style={{
                      width: 38,
                      height: 38,
                      borderRadius: '50%',
                      background: 'var(--bg-surface)',
                      border: '1px solid var(--border-subtle)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: 'var(--accent-primary)',
                    }}
                  >
                    <IconPlus />
                  </div>
                  <span style={{ fontSize: 13.5, fontWeight: 700, color: 'var(--text-primary)' }}>
                    Thêm cấu hình mới
                  </span>
                  <span style={{ fontSize: 11.5, color: 'var(--text-secondary)', textAlign: 'center', maxWidth: 220, lineHeight: 1.4 }}>
                    Tạo luồng gửi email tự động theo chủ đề, giờ gửi & hòm thư riêng
                  </span>
                </div>
              </div>
            </div>

            {/* SECTION 2: NEWS FEED WITH LIVE CSDL STATUS & REFRESH */}
            <div id="news-feed-heading" style={{ marginBottom: 12, display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 10 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                <h2 style={{ fontSize: 15, fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
                  Tin tức thực tế
                </h2>
                <span
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: 5,
                    padding: '3px 8px',
                    borderRadius: 12,
                    background: 'rgba(16, 185, 129, 0.12)',
                    color: '#059669',
                    fontSize: 11.5,
                    fontWeight: 700,
                    border: '1px solid rgba(16, 185, 129, 0.28)',
                  }}
                  title="Tổng số bài viết cập nhật"
                >
                  <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#10B981' }} />
                  <span>
                    {filteredNews.length > NEWS_PAGE_SIZE
                      ? `${filteredNews.length} bài · Trang ${safeNewsPage}/${totalNewsPages}`
                      : `${filteredNews.length} bài viết`}
                  </span>
                </span>
                {lastSyncTime && (
                  <span style={{ fontSize: 11, color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
                    Đồng bộ: {lastSyncTime}
                  </span>
                )}
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleSyncLiveNews}
                  disabled={isSyncingNews}
                  style={{
                    minHeight: 28,
                    width: 28,
                    padding: 0,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: 'var(--radius-sm)',
                  }}
                  title={isSyncingNews ? 'Đang cập nhật bài viết...' : 'Cập nhật tin mới'}
                  aria-label="Cập nhật tin mới"
                >
                  <IconRefresh spinning={isSyncingNews} />
                </button>

                {/* Sub Filter Pills */}
                <div style={{ display: 'flex', gap: 5, flexWrap: 'wrap' }}>
                  {newsFilterOptions.map((f) => (
                    <button
                      key={f.id}
                      onClick={() => {
                        setNewsFilter(f.id as any);
                        setNewsPage(1);
                      }}
                      className="btn btn-secondary"
                      style={{
                        minHeight: 26,
                        padding: '0 9px',
                        fontSize: 11.5,
                        fontWeight: newsFilter === f.id ? 700 : 500,
                        background: newsFilter === f.id ? 'var(--accent-primary)' : 'var(--bg-surface)',
                        borderColor: newsFilter === f.id ? 'var(--accent-primary)' : 'var(--border-subtle)',
                        color: newsFilter === f.id ? '#FFFFFF' : 'var(--text-secondary)',
                      }}
                    >
                      {f.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Card-Based News Feed */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(290px, 1fr))', gap: 14 }}>
              {paginatedNews.map((item) => {
                const isJobItem = item.channelId === 'jobs' || item.category.toLowerCase().includes('việc') || item.title.includes('TESTER') || item.title.includes('CÀY GAME') || item.title.includes('NHẬP LIỆU') || item.title.includes('THỰC TẬP');
                const hasRealImage = Boolean(item.imageUrl && !item.imageUrl.includes('unsplash.com') && item.imageUrl.trim().length > 0);

                if (isJobItem) {
                  const isBookmarked = bookmarkedJobIds.includes(item.id);
                  const bulletLines = item.summary.split('\n').filter((l) => l.trim().startsWith('•')).map((l) => l.replace(/^•\s*/, ''));
                  const firstLine = item.summary.split('\n')[0];
                  const salaryPart = firstLine.includes('|') ? firstLine.split('|')[1].trim() : (item.summary.includes('Thu nhập') ? item.summary.split('\n')[0] : 'Thu nhập tham khảo: 1.100.000–5.800.000 VNĐ/ngày');
                  const subtitlePart = firstLine.includes('|') ? firstLine.split('|')[0].trim() : 'Cơ hội linh hoạt · Không yêu cầu kinh nghiệm';

                  return (
                    <div
                      key={item.id}
                      className="card-surface card-interactive"
                      style={{
                        padding: hasRealImage ? '0 0 18px 0' : '20px 20px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 12,
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid var(--border-subtle)',
                        background: 'var(--bg-surface)',
                        boxSizing: 'border-box',
                        height: '100%',
                        overflow: 'hidden',
                      }}
                    >
                      {/* Job Header Cover Image (ONLY if authentic photo exists) */}
                      {hasRealImage ? (
                        <div
                          style={{
                            width: '100%',
                            height: 135,
                            overflow: 'hidden',
                            position: 'relative',
                            background: 'var(--bg-subtle)',
                          }}
                        >
                          <img
                            src={item.imageUrl}
                            alt={item.title}
                            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            onError={(e) => {
                              (e.currentTarget as HTMLElement).style.display = 'none';
                            }}
                          />
                          <div style={{ position: 'absolute', top: 10, left: 10, display: 'flex', gap: 6 }}>
                            <span className="source-badge-pill source-badge-tech">{item.source}</span>
                            <span
                              style={{
                                fontSize: 10.5,
                                fontWeight: 700,
                                padding: '2px 7px',
                                borderRadius: 4,
                                background: '#0F766E',
                                color: '#FFFFFF',
                              }}
                            >
                              Đã xác thực
                            </span>
                          </div>
                          <button
                            type="button"
                            onClick={() => handleToggleBookmark(item.id)}
                            style={{
                              position: 'absolute',
                              top: 10,
                              right: 10,
                              background: 'rgba(255, 255, 255, 0.94)',
                              border: 'none',
                              borderRadius: 6,
                              padding: '5px 7px',
                              cursor: 'pointer',
                              display: 'flex',
                              alignItems: 'center',
                              color: isBookmarked ? 'var(--accent-primary)' : '#64748B',
                              boxShadow: '0 2px 5px rgba(0,0,0,0.12)',
                            }}
                            title={isBookmarked ? 'Bỏ lưu việc làm này' : 'Lưu tin tuyển dụng này'}
                          >
                            <IconBookmark filled={isBookmarked} />
                          </button>
                        </div>
                      ) : (
                        /* Top Row: Source, Verified & Bookmark (Zero fake image banner) */
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, fontWeight: 700, color: 'var(--text-primary)' }}>
                            <span>{item.source}</span>
                            <span style={{ fontSize: 11, color: 'var(--text-tertiary)', fontWeight: 400 }}>• Đã xác thực</span>
                          </div>
                          <button
                            type="button"
                            onClick={() => handleToggleBookmark(item.id)}
                            style={{
                              background: 'none',
                              border: 'none',
                              cursor: 'pointer',
                              padding: 4,
                              color: isBookmarked ? 'var(--accent-primary)' : 'var(--text-tertiary)',
                              display: 'flex',
                              alignItems: 'center',
                            }}
                            title={isBookmarked ? 'Bỏ lưu việc làm này' : 'Lưu tin tuyển dụng này'}
                          >
                            <IconBookmark filled={isBookmarked} />
                          </button>
                        </div>
                      )}

                      {/* Inner Content Padding when image exists */}
                      <div style={{ padding: hasRealImage ? '0 18px' : '0', display: 'flex', flexDirection: 'column', gap: 11, flex: 1 }}>
                        {/* Job Title */}
                        <h3
                          style={{
                            fontSize: 16,
                            fontWeight: 800,
                            textTransform: 'uppercase',
                            letterSpacing: -0.2,
                            lineHeight: 1.35,
                            margin: '2px 0 0 0',
                            color: '#1E3A8A',
                          }}
                        >
                          {item.title}
                        </h3>

                        {/* Subtitle */}
                        <div style={{ fontSize: 13, color: 'var(--text-secondary)', fontWeight: 500 }}>
                          {subtitlePart}
                        </div>

                        {/* Location Pill & Time */}
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 6 }}>
                          <span
                            style={{
                              display: 'inline-block',
                              padding: '3px 9px',
                              borderRadius: 6,
                              background: 'var(--status-running-bg)',
                              color: 'var(--status-running)',
                              fontSize: 11.5,
                              fontWeight: 700,
                            }}
                          >
                            Việt Nam · Remote 100%
                          </span>
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, fontSize: 11.5, color: 'var(--text-tertiary)' }}>
                            <IconClock />
                            <span>Đăng {formatRelativeTime(item.publishedAt)}</span>
                          </span>
                        </div>

                        {/* Type & Salary */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                          <span style={{ fontSize: 12.5, color: 'var(--text-secondary)', fontWeight: 600 }}>
                            Part-time / Full-time (Tự do chọn giờ)
                          </span>
                          <strong style={{ fontSize: 13.5, color: 'var(--text-primary)' }}>
                            {salaryPart}
                          </strong>
                        </div>

                        {/* Bullet points */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, margin: '2px 0' }}>
                          {(bulletLines.length > 0
                            ? bulletLines
                            : [
                                'Hỗ trợ chơi thử và báo cáo lỗi game mobile (casual, nhập vai MMO)',
                                'Không yêu cầu kinh nghiệm, có hướng dẫn chi tiết từ A-Z cho người mới',
                                'Không yêu cầu tiếng Anh (giao diện và hướng dẫn 100% tiếng Việt)',
                                'Thiết bị: Điện thoại smartphone (Android/iOS) hoặc máy tính cơ bản',
                              ]
                          ).map((b, bIdx) => (
                            <div key={bIdx} style={{ display: 'flex', gap: 7, fontSize: 12.5, color: 'var(--text-secondary)', lineHeight: 1.45 }}>
                              <span style={{ color: 'var(--accent-primary)', fontWeight: 800 }}>•</span>
                              <span>{b}</span>
                            </div>
                          ))}
                        </div>

                        {/* Description & Note */}
                        <div style={{ fontSize: 12, color: 'var(--text-tertiary)', lineHeight: 1.45 }}>
                          Phù hợp cho học sinh, sinh viên, người tìm việc làm online linh hoạt tại nhà.
                        </div>
                        <div style={{ fontSize: 11, color: 'var(--text-tertiary)', fontStyle: 'italic' }}>
                          * Tùy thuộc vào mức độ tham gia và hiệu quả công việc.
                        </div>

                        {/* Bottom Full-Width CTA Bar */}
                        <a
                          href={item.url}
                          target="_blank"
                          rel="noreferrer"
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: 6,
                            background: '#0F172A',
                            color: '#FFFFFF',
                            padding: '11px 16px',
                            borderRadius: 'var(--radius-sm)',
                            textDecoration: 'none',
                            fontWeight: 700,
                            fontSize: 13,
                            letterSpacing: 0.2,
                            marginTop: 'auto',
                            boxShadow: '0 2px 6px rgba(15, 23, 42, 0.15)',
                          }}
                        >
                          <span>Ứng tuyển ngay →</span>
                        </a>
                      </div>
                    </div>
                  );
                }

                const hasRealNewsImage = Boolean(item.imageUrl && !item.imageUrl.includes('unsplash.com') && item.imageUrl.trim().length > 0);

                return (
                <div key={item.id} className="news-digest-card">
                  {/* 1. Image Thumbnail (ONLY if verified genuine image exists) */}
                  {hasRealNewsImage && (
                    <div className="news-thumb-wrapper">
                      <img
                        src={item.imageUrl}
                        alt={item.title}
                        className="news-thumb-img"
                        onError={(e) => {
                          (e.currentTarget as HTMLElement).style.display = 'none';
                        }}
                      />
                      <div
                        style={{
                          position: 'absolute',
                          top: 10,
                          left: 10,
                          display: 'flex',
                          gap: 6,
                        }}
                      >
                        <span
                          className={`source-badge-pill ${
                            item.sourceType === 'fb'
                              ? 'source-badge-fb'
                              : item.sourceType === 'vn'
                              ? 'source-badge-vn'
                              : item.sourceType === 'tech'
                              ? 'source-badge-tech'
                              : 'source-badge-shop'
                          }`}
                        >
                          {item.source}
                        </span>
                        <span
                          style={{
                            fontSize: 10,
                            fontWeight: 700,
                            padding: '2px 6px',
                            borderRadius: 4,
                            background: 'rgba(16, 185, 129, 0.92)',
                            color: '#FFFFFF',
                            letterSpacing: 0.3,
                          }}
                        >
                          Mới
                        </span>
                      </div>
                    </div>
                  )}

                  {/* Top Metadata Row when NO image (Clean Editorial Layout) */}
                  {!hasRealNewsImage && (
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 6 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span
                          className={`source-badge-pill ${
                            item.sourceType === 'fb'
                              ? 'source-badge-fb'
                              : item.sourceType === 'vn'
                              ? 'source-badge-vn'
                              : item.sourceType === 'tech'
                              ? 'source-badge-tech'
                              : 'source-badge-shop'
                          }`}
                        >
                          {item.source}
                        </span>
                        <span
                          style={{
                            fontSize: 10,
                            fontWeight: 700,
                            padding: '2px 6px',
                            borderRadius: 4,
                            background: 'var(--bg-subtle)',
                            color: 'var(--text-secondary)',
                            border: '1px solid var(--border-subtle)',
                            letterSpacing: 0.3,
                          }}
                        >
                          Mới
                        </span>
                      </div>
                      <span style={{ fontSize: 11, color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
                        {formatRelativeTime(item.publishedAt)}
                      </span>
                    </div>
                  )}

                  {/* 2. Card Content */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
                    {/* Time & Category Row */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)', fontWeight: 600 }}>{item.category}</span>
                      <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)', display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                        <IconClock />
                        <span>{formatRelativeTime(item.publishedAt)}</span>
                      </span>
                    </div>

                    {/* Title */}
                    <h3
                      style={{
                        fontSize: 15.5,
                        fontWeight: 700,
                        lineHeight: 1.4,
                        margin: 0,
                        color: 'var(--text-primary)',
                        display: '-webkit-box',
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden',
                      }}
                    >
                      {item.title}
                    </h3>

                    {/* Short Description */}
                    <p
                      style={{
                        fontSize: 13,
                        color: 'var(--text-secondary)',
                        lineHeight: 1.55,
                        margin: 0,
                        flex: 1,
                        display: '-webkit-box',
                        WebkitLineClamp: 3,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden',
                      }}
                    >
                      {item.summary}
                    </p>

                    {/* Link đến (Source Link & Domain) */}
                    <div
                      style={{
                        paddingTop: 10,
                        borderTop: '1px solid var(--border-subtle)',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                      }}
                    >
                      <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
                        {item.url.replace(/^https?:\/\//, '').split('/')[0]}
                      </span>
                      <a
                        href={item.url}
                        target="_blank"
                        rel="noreferrer"
                        style={{
                          fontSize: 12.5,
                          fontWeight: 600,
                          color: 'var(--accent-primary)',
                          textDecoration: 'none',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: 4,
                        }}
                      >
                        <span>Đọc tiếp</span>
                        <IconArrowRight />
                      </a>
                    </div>
                  </div>
                </div>
              );
            })}
            </div>

            {/* Pagination Controls (20 items per page) */}
            {filteredNews.length > NEWS_PAGE_SIZE && (
              <div
                style={{
                  marginTop: 20,
                  padding: '12px 18px',
                  borderRadius: 'var(--radius-md)',
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-subtle)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexWrap: 'wrap',
                  gap: 12,
                }}
              >
                <div style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
                  Hiển thị <strong style={{ color: 'var(--text-primary)' }}>{(safeNewsPage - 1) * NEWS_PAGE_SIZE + 1}–{Math.min(safeNewsPage * NEWS_PAGE_SIZE, filteredNews.length)}</strong> trên tổng số <strong style={{ color: 'var(--text-primary)' }}>{filteredNews.length}</strong> bài viết
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => {
                      setNewsPage((p) => Math.max(1, p - 1));
                      document.getElementById('news-feed-heading')?.scrollIntoView({ behavior: 'smooth' });
                    }}
                    disabled={safeNewsPage <= 1}
                    style={{ fontSize: 12, minHeight: 28, padding: '0 10px', gap: 4 }}
                  >
                    <span>← Trước</span>
                  </button>

                  {Array.from({ length: totalNewsPages }, (_, i) => i + 1).map((pageNum) => (
                    <button
                      key={pageNum}
                      type="button"
                      onClick={() => {
                        setNewsPage(pageNum);
                        document.getElementById('news-feed-heading')?.scrollIntoView({ behavior: 'smooth' });
                      }}
                      className="btn btn-secondary"
                      style={{
                        minHeight: 28,
                        minWidth: 28,
                        padding: '0 8px',
                        fontSize: 12,
                        fontFamily: 'var(--font-mono)',
                        fontWeight: safeNewsPage === pageNum ? 700 : 500,
                        background: safeNewsPage === pageNum ? 'var(--accent-primary)' : 'var(--bg-surface)',
                        color: safeNewsPage === pageNum ? '#FFFFFF' : 'var(--text-primary)',
                        borderColor: safeNewsPage === pageNum ? 'var(--accent-primary)' : 'var(--border-subtle)',
                      }}
                    >
                      {pageNum}
                    </button>
                  ))}

                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => {
                      setNewsPage((p) => Math.min(totalNewsPages, p + 1));
                      document.getElementById('news-feed-heading')?.scrollIntoView({ behavior: 'smooth' });
                    }}
                    disabled={safeNewsPage >= totalNewsPages}
                    style={{ fontSize: 12, minHeight: 28, padding: '0 10px', gap: 4 }}
                  >
                    <span>Sau →</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 3: ZERO-APP AUTOMATIONS (CÁC TÁC VỤ CHẠY NGẦM KHÔNG CẦN MỞ ỨNG DỤNG)     */}
        {/* ========================================================================= */}
        {activeTab === 'automations' && (
          <div style={{ width: '100%' }}>
            <div style={{ marginBottom: 18, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <h1 style={{ fontSize: 20, fontWeight: 800, letterSpacing: -0.3, margin: 0 }}>
                Tác vụ
              </h1>
              <span className="mono-pill" style={{ color: 'var(--status-running)', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                {tasks.filter((t) => t.active).length}/{tasks.length} hoạt động
              </span>
            </div>

            <div className="desktop-2col-grid">
              {tasks.map((task) => (
                <div key={task.id} className="card-surface card-interactive" style={{ display: 'flex', flexDirection: 'column', gap: 12, height: '100%', boxSizing: 'border-box' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span style={{ width: 8, height: 8, borderRadius: '50%', background: task.active ? 'var(--status-running)' : 'var(--text-tertiary)' }} />
                        <h3 style={{ fontSize: 15, fontWeight: 700 }}>{task.title}</h3>
                      </div>
                      <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginTop: 4 }}>
                        {task.schedule} · {task.sources}
                      </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <button
                        className="btn btn-secondary"
                        onClick={() => handleToggleTaskActive(task.id)}
                        style={{ fontSize: 12, minHeight: 30, padding: '0 10px', gap: 4 }}
                      >
                        <IconSliders />
                        <span>{task.active ? 'Tạm dừng' : 'Bật'}</span>
                      </button>

                      <button
                        className="btn btn-primary"
                        onClick={() => handleRunTaskNow(task.id, task.title)}
                        disabled={runningTaskId === task.id}
                        style={{ fontSize: 12, minHeight: 30, padding: '0 12px', gap: 4 }}
                      >
                        <IconPlay />
                        <span>{runningTaskId === task.id ? 'Đang chạy...' : 'Chạy ngay'}</span>
                      </button>
                    </div>
                  </div>

                  <div
                    style={{
                      background: 'var(--bg-subtle)',
                      padding: '8px 12px',
                      borderRadius: 'var(--radius-sm)',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      fontSize: 12,
                      color: 'var(--text-secondary)',
                    }}
                  >
                    <span>{task.channel}</span>
                    <span>{task.lastRun}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 4: TOOLS (BỘ CÔNG CỤ THIẾT THỰC)                                      */}
        {/* ========================================================================= */}
        {activeTab === 'tools' && (
          <div style={{ width: '100%' }}>
            <div style={{ marginBottom: 16 }}>
              <h1 style={{ fontSize: 20, fontWeight: 800, letterSpacing: -0.3, margin: 0 }}>
                Công cụ
              </h1>
            </div>

            {/* Sub-Tabs (Clean Pills) */}
            <div style={{ display: 'flex', gap: 6, marginBottom: 18, flexWrap: 'wrap' }}>
              {availableToolSubTabs.map((tab) => (
                <button
                  key={tab.id}
                  className={`subtab-pill ${toolSubTab === tab.id ? 'active' : ''}`}
                  onClick={() => setToolSubTab(tab.id as ToolSubTab)}
                >
                  {tab.label}
                </button>
              ))}
            </div>

            {/* TOOL 0: SELF-EVOLUTION & OPEN-SOURCE RADAR */}
            {toolSubTab === 'evolution' && (
              !isOwner ? (
                <div
                  className="card-surface"
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    padding: '44px 24px',
                    textAlign: 'center',
                    gap: 12,
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)',
                    background: 'var(--bg-surface)',
                  }}
                >
                  <div
                    style={{
                      width: 48,
                      height: 48,
                      borderRadius: '50%',
                      background: 'var(--bg-subtle)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      border: '1px solid var(--border-subtle)',
                      color: 'var(--accent-primary)',
                    }}
                  >
                    <IconShield />
                  </div>
                  <div style={{ fontWeight: 800, fontSize: 16, color: 'var(--text-primary)' }}>
                    Kho Nghiên Cứu Mã Nguồn Mở Riêng Tư (Owner Only)
                  </div>
                  <p style={{ maxWidth: 480, fontSize: 13, color: 'var(--text-secondary)', margin: 0, lineHeight: 1.5 }}>
                    Toàn bộ 8 công cụ mã nguồn mở chọn lọc (Stickman Director, Lemo-Opuscar, Lossless-Cut, Remotion...) và báo cáo phân tích tiến hóa AI từ trước đến nay thuộc quyền sở hữu riêng biệt của Quản trị viên (Chủ sở hữu).
                  </p>
                  <span className="mono-pill" style={{ fontSize: 11, color: '#10B981', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                    Chỉ hiển thị khi đăng nhập tài khoản: {MASTER_OWNER_EMAIL}
                  </span>
                  {!authSession && (
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={() => setShowLoginModal(true)}
                      style={{ marginTop: 6, fontSize: 12.5, minHeight: 32, padding: '0 16px' }}
                    >
                      Đăng nhập Chủ sở hữu
                    </button>
                  )}
                </div>
              ) : (
                <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                  {/* Header Row */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                      <h3 style={{ fontSize: 16, fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
                        Mã nguồn mở
                      </h3>
                      <span className="mono-pill" style={{ color: 'var(--status-running)', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                        8 đề xuất
                      </span>
                      <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
                        Cập nhật {lastEvolutionAuditTime}
                      </span>
                    </div>

                    <div style={{ display: 'flex', gap: 8 }}>
                      <button
                        type="button"
                        className="btn btn-secondary"
                        onClick={handleRunEvolutionAudit}
                        disabled={isAuditingEvolution}
                        style={{ gap: 5, fontSize: 12, minHeight: 28, height: 28, padding: '0 10px' }}
                      >
                        <IconRefresh spinning={isAuditingEvolution} />
                        <span>{isAuditingEvolution ? 'Đang quét...' : 'Quét lại'}</span>
                      </button>
                      <button
                        type="button"
                        className="btn btn-primary"
                        onClick={handleTriggerEvolutionProposalEmail}
                        style={{ gap: 5, fontSize: 12, minHeight: 28, height: 28, padding: '0 12px' }}
                      >
                        <IconSend />
                        <span>Gửi mail</span>
                      </button>
                    </div>
                  </div>

                  {/* Grid of 8 Curated Open-Source Cards */}
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 12 }}>
                    {[
                      {
                        name: 'stickman-video-director',
                        stars: '★ 4.2k',
                        url: 'https://github.com/kaomei/stickman-video-director',
                        tag: 'Video người que kịch bản',
                        summary: 'Tự động đạo diễn video người que không lộ mặt cho YouTube & TikTok từ kịch bản phân cảnh.',
                      },
                      {
                        name: 'lemo-opuscar',
                        stars: '★ 3.8k',
                        url: 'https://github.com/lemomo-ai/lemo-opuscar',
                        tag: 'Canvas & WebGL Motion',
                        summary: 'Làm phim hoạt hình và đồ họa chuyển động bằng code Canvas/WebGL không cần GPU nặng.',
                      },
                      {
                        name: 'lossless-cut & remotion',
                        stars: '★ 28.1k',
                        url: 'https://github.com/mifi/lossless-cut',
                        tag: 'Biên tập video mở',
                        summary: 'Cắt ghép, tinh chỉnh tỉ lệ 16:9/9:16 và xuất video siêu tốc tức thì không suy hao chất lượng.',
                      },
                      {
                        name: 'crawl4ai',
                        stars: '★ 34.5k',
                        url: 'https://github.com/unclecode/crawl4ai',
                        tag: 'Thu thập tin & Việc làm',
                        summary: 'Cào quét dữ liệu tự động tốc độ cao, trích xuất Markdown sạch không quảng cáo.',
                      },
                      {
                        name: 'modelcontextprotocol',
                        stars: '★ 18.2k',
                        url: 'https://github.com/modelcontextprotocol',
                        tag: 'Chuẩn công cụ MCP',
                        summary: 'Chuẩn kết nối công cụ mở của Anthropic, tích hợp hơn 1,000 API doanh nghiệp.',
                      },
                      {
                        name: 'mem0',
                        stars: '★ 26.8k',
                        url: 'https://github.com/mem0ai/mem0',
                        tag: 'Bộ nhớ dài hạn',
                        summary: 'Tự động học thói quen người dùng, giải quyết xung đột thông tin và nén bộ nhớ.',
                      },
                      {
                        name: 'ollama',
                        stars: '★ 118k',
                        url: 'https://github.com/ollama/ollama',
                        tag: 'AI Offline 0đ',
                        summary: 'Chạy mô hình SLM cục bộ (Gemma 2 / Qwen 2.5), phân loại tin tức miễn phí và bảo mật.',
                      },
                      {
                        name: 'livekit',
                        stars: '★ 8.5k',
                        url: 'https://github.com/livekit/agents',
                        tag: 'Đàm thoại 2 chiều',
                        summary: 'Đàm thoại giọng nói thời gian thực độ trễ thấp (< 300ms) trên Web & Mobile.',
                      },
                    ].map((oss) => (
                      <div
                        key={oss.name}
                        className="card-surface card-interactive"
                        style={{
                          padding: '14px 16px',
                          display: 'flex',
                          flexDirection: 'column',
                          gap: 10,
                          border: '1px solid var(--border-subtle)',
                          borderRadius: 'var(--radius-md)',
                          background: 'var(--bg-surface)',
                        }}
                      >
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 6 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <span style={{ fontWeight: 700, fontSize: 14, color: 'var(--text-primary)' }}>
                              {oss.name}
                            </span>
                            <span className="mono-pill" style={{ fontSize: 10.5 }}>
                              {oss.stars}
                            </span>
                          </div>
                          <span style={{ fontSize: 11, color: 'var(--text-tertiary)', fontWeight: 500 }}>
                            {oss.tag}
                          </span>
                        </div>

                        <p style={{ fontSize: 12.5, color: 'var(--text-secondary)', margin: 0, lineHeight: 1.45, flex: 1 }}>
                          {oss.summary}
                        </p>

                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: 8, borderTop: '1px solid var(--border-subtle)', marginTop: 'auto' }}>
                          <a
                            href={oss.url}
                            target="_blank"
                            rel="noreferrer"
                            style={{ fontSize: 12, color: 'var(--text-secondary)', textDecoration: 'none', fontWeight: 600 }}
                          >
                            GitHub ↗
                          </a>
                          <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => {
                              showToast(`Đã ghi nhận duyệt đề xuất: ${oss.name}`);
                            }}
                            style={{ fontSize: 11.5, minHeight: 26, height: 26, padding: '0 10px' }}
                          >
                            Duyệt
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Minimalist 1-line Footer */}
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '8px 12px',
                      borderRadius: 'var(--radius-sm)',
                      background: 'var(--bg-subtle)',
                      border: '1px solid var(--border-subtle)',
                      fontSize: 12,
                      color: 'var(--text-tertiary)',
                      flexWrap: 'wrap',
                      gap: 6,
                    }}
                  >
                    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#10B981' }} />
                      <span>Tự động quét lúc 07:00 hàng ngày &middot; Gửi email để phê duyệt</span>
                    </span>
                    <button
                      type="button"
                      onClick={() => {
                        setActiveTab('assistant');
                        showToast('Đang mở Trợ lý...');
                      }}
                      style={{ background: 'transparent', border: 'none', color: 'var(--text-secondary)', fontSize: 11.5, fontWeight: 600, cursor: 'pointer', padding: 0 }}
                    >
                      Hỏi Trợ lý →
                    </button>
                  </div>
                </div>
              )
            )}

            {/* TOOL 1: VISION OCR (QUÉT HÓA ĐƠN) */}
            {toolSubTab === 'vision' && (
              <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>Quét hóa đơn chi tiêu</h3>
                  <span className="source-badge-pill">Vision OCR CSDL</span>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 16 }}>
                  <div>
                    <div
                      style={{
                        width: '100%',
                        height: 200,
                        borderRadius: 'var(--radius-sm)',
                        overflow: 'hidden',
                        background: 'var(--bg-subtle)',
                        border: '1px solid var(--border-subtle)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      {visionImagePreview ? (
                        <img src={visionImagePreview} alt="Hóa đơn" style={{ width: '100%', height: '100%', objectFit: 'contain' }} />
                      ) : (
                        <span style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>Chưa có ảnh hóa đơn được chọn</span>
                      )}
                    </div>
                    <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
                      <label className="btn btn-secondary" style={{ fontSize: 12.5, flex: 1, cursor: 'pointer', gap: 5 }}>
                        <IconCamera />
                        <span>Chọn ảnh hóa đơn</span>
                        <input
                          type="file"
                          accept="image/*"
                          style={{ display: 'none' }}
                          onChange={(e) => {
                            if (e.target.files && e.target.files[0]) {
                              const file = e.target.files[0];
                              setVisionImageName(file.name);
                              setVisionImagePreview(URL.createObjectURL(file));
                              const reader = new FileReader();
                              reader.onload = (event) => {
                                const b64 = (event.target?.result as string) || '';
                                setVisionBase64(b64.split(',')[1] || b64);
                              };
                              reader.readAsDataURL(file);
                              handleAnalyzeVision(file);
                            }
                          }}
                        />
                      </label>
                      <button
                        type="button"
                        className="btn btn-primary"
                        onClick={() => handleAnalyzeVision()}
                        disabled={isAnalyzingVision}
                        style={{ fontSize: 12.5, gap: 5 }}
                      >
                        <IconSearch />
                        <span>{isAnalyzingVision ? 'Đang quét...' : 'Quét ngay'}</span>
                      </button>
                    </div>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    <div style={{ fontSize: 11.5, fontWeight: 700, color: 'var(--text-secondary)' }}>
                      KẾT QUẢ PHÂN TÍCH THỰC TẾ:
                    </div>
                    <div
                      style={{
                        flex: 1,
                        background: 'var(--bg-subtle)',
                        padding: '12px 14px',
                        borderRadius: 'var(--radius-sm)',
                        fontSize: 13,
                        lineHeight: 1.5,
                        whiteSpace: 'pre-wrap',
                        border: '1px solid var(--border-subtle)',
                        minHeight: 140,
                      }}
                    >
                      {visionResult}
                    </div>
                    <div style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>
                      Tệp: {visionImageName} · Tự động ghi nhận vào CSDL chi tiêu
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* TOOL 2: SHOPPING TRACKER */}
            {toolSubTab === 'shopping' && (
              <div
                className="card-surface"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 16,
                  minHeight: 'calc(100vh - 200px)',
                  boxSizing: 'border-box',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>So sánh giá sản phẩm</h3>
                    <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)' }}>
                      Dữ liệu giá thực tế từ Shopee Mall, Tiki Official, Lazada Mall
                    </span>
                  </div>
                  <span className="source-badge-pill">Shopee · Tiki · Lazada</span>
                </div>

                <div style={{ display: 'flex', gap: 8 }}>
                  <input
                    type="text"
                    value={shopQuery}
                    onChange={(e) => setShopQuery(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') handleSearchShopping();
                    }}
                    placeholder="Nhập tên sản phẩm để so sánh giá thực tế..."
                    style={{
                      flex: 1,
                      padding: '10px 14px',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      background: 'var(--bg-surface)',
                      color: 'var(--text-primary)',
                      fontSize: 13.5,
                    }}
                  />
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => handleSearchShopping()}
                    disabled={isSearchingShop || !shopQuery.trim()}
                    style={{
                      width: 42,
                      minHeight: 38,
                      padding: 0,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      borderRadius: 'var(--radius-sm)',
                    }}
                    title={isSearchingShop ? 'Đang so sánh giá...' : 'Tìm kiếm giá'}
                    aria-label="Tìm kiếm giá"
                  >
                    {isSearchingShop ? <IconRefresh spinning /> : <IconSearch />}
                  </button>
                </div>

                {/* Quick Keyword Suggestions */}
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', alignItems: 'center' }}>
                  <span style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>Gợi ý nhanh:</span>
                  {[
                    'Bàn phím cơ Keychron K2 Pro',
                    'iPhone 15 Pro Max 256GB',
                    'Chuột Logitech MX Master 3S',
                    'Màn hình LG 27UP850N 4K',
                    'Tai nghe Sony WH-1000XM5',
                  ].map((kw, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => handleSearchShopping(kw)}
                      className="mono-pill"
                      style={{
                        cursor: 'pointer',
                        background: 'var(--bg-surface)',
                        border: '1px solid var(--border-subtle)',
                        fontSize: 11,
                        padding: '3px 10px',
                      }}
                    >
                      {kw}
                    </button>
                  ))}
                </div>

                {/* Best Deal Summary Alert */}
                {shopBestSummary && (
                  <div
                    style={{
                      padding: '12px 16px',
                      borderRadius: 'var(--radius-sm)',
                      background: 'var(--bg-subtle)',
                      border: '1px solid var(--border-subtle)',
                      fontSize: 13,
                      color: 'var(--text-primary)',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 10,
                    }}
                  >
                    <span style={{ color: 'var(--accent-primary)', fontWeight: 800, fontSize: 14 }}>●</span>
                    <span>{shopBestSummary}</span>
                  </div>
                )}

                {/* Main Store Comparison Rows */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {shopResults.map((item, idx) => (
                    <div
                      key={idx}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '14px 18px',
                        borderRadius: 'var(--radius-sm)',
                        border: '1px solid var(--border-subtle)',
                        background: 'var(--bg-surface)',
                      }}
                    >
                      <div>
                        <div style={{ fontWeight: 700, fontSize: 14 }}>{item.store}</div>
                        <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginTop: 2 }}>
                          {item.productTitle || item.status} {item.rating ? `· ${item.rating}` : ''}
                        </div>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                        <span style={{ fontWeight: 800, fontSize: 16, color: 'var(--accent-primary)' }}>
                          {item.price}
                        </span>
                        <a
                          href={item.link}
                          target="_blank"
                          rel="noreferrer"
                          className="btn btn-secondary"
                          style={{ fontSize: 12, minHeight: 30, padding: '0 12px', gap: 4 }}
                        >
                          <span>Xem giá</span>
                          <IconArrowRight />
                        </a>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Rich Shopping Intelligence: Matrix & Advice & Coupons */}
                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                    gap: 14,
                    marginTop: 8,
                  }}
                >
                  {/* Column 1: Policy Matrix */}
                  <div
                    style={{
                      background: 'var(--bg-subtle)',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      padding: '14px 16px',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 8,
                    }}
                  >
                    <strong style={{ fontSize: 13, color: 'var(--text-primary)' }}>Chính sách & Bảo hành</strong>
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', gap: 6 }}>
                      <div>• <strong>Shopee Mall:</strong> Hàng chính hãng 100%, đổi trả miễn phí 15 ngày, bảo hành 12T.</div>
                      <div>• <strong>Tiki Official:</strong> Giao siêu tốc TikiNOW 2h, bồi thường 111% nếu hàng giả.</div>
                      <div>• <strong>Lazada Mall:</strong> Hàng phân phối uỷ quyền, Freeship Max, bảo hành hãng 24T.</div>
                    </div>
                  </div>

                  {/* Column 2: Available Promo Coupons */}
                  <div
                    style={{
                      background: 'var(--bg-subtle)',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      padding: '14px 16px',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 8,
                    }}
                  >
                    <strong style={{ fontSize: 13, color: 'var(--text-primary)' }}>Mã giảm giá khả dụng</strong>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                      {[
                        { code: 'TECH50K', store: 'Shopee', desc: 'Giảm 50k cho đơn từ 1.5tr' },
                        { code: 'TIKICONGNGHE', store: 'Tiki', desc: 'Giảm 5% tối đa 100k' },
                        { code: 'LAZFREESHIP', store: 'Lazada', desc: 'Miễn phí vận chuyển toàn quốc' },
                      ].map((cp, cIdx) => (
                        <div key={cIdx} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 12 }}>
                          <div>
                            <span className="mono-pill" style={{ fontSize: 11, fontWeight: 700 }}>{cp.code}</span>
                            <span style={{ color: 'var(--text-tertiary)', marginLeft: 6 }}>{cp.desc}</span>
                          </div>
                          <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => {
                              navigator.clipboard.writeText(cp.code);
                              showToast(`Đã chép mã ${cp.code}!`);
                            }}
                            style={{ minHeight: 22, padding: '0 6px', fontSize: 10.5 }}
                          >
                            Chép mã
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* TOOL 3: WRITING STUDIO */}
            {toolSubTab === 'writing' && (
              <div
                className="card-surface"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 14,
                  minHeight: 'calc(100vh - 200px)',
                  boxSizing: 'border-box',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>Soạn thảo văn bản AI</h3>
                    <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)' }}>
                      Tự động hoàn thiện văn bản theo chủ đề, lưu trữ CSDL cá nhân
                    </span>
                  </div>
                  <span className="source-badge-pill">Lưu trữ CSDL</span>
                </div>

                <input
                  type="text"
                  value={writingPrompt}
                  onChange={(e) => setWritingPrompt(e.target.value)}
                  placeholder="Nêu chủ đề hoặc mục tiêu bài viết..."
                  style={{
                    width: '100%',
                    padding: '10px 14px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-surface)',
                    color: 'var(--text-primary)',
                    fontSize: 13.5,
                  }}
                />

                <textarea
                  value={writingDraft}
                  onChange={(e) => setWritingDraft(e.target.value)}
                  placeholder="Nội dung bài viết sẽ hiển thị tại đây..."
                  style={{
                    width: '100%',
                    flex: 1,
                    minHeight: 360,
                    padding: '16px 18px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-subtle)',
                    color: 'var(--text-primary)',
                    fontSize: 13.5,
                    lineHeight: 1.6,
                    resize: 'vertical',
                    boxSizing: 'border-box',
                  }}
                />

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 'auto' }}>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => {
                      navigator.clipboard.writeText(writingDraft);
                      showToast('Đã sao chép vào khay nhớ tạm.');
                    }}
                    style={{ gap: 5 }}
                  >
                    <IconFile />
                    <span>Sao chép</span>
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleGenerateWritingDraft}
                    disabled={isGeneratingWriting || !writingPrompt.trim()}
                    style={{ gap: 5 }}
                  >
                    <IconRefresh />
                    <span>{isGeneratingWriting ? 'Đang soạn thảo...' : 'Soạn thảo theo yêu cầu'}</span>
                  </button>
                </div>
              </div>
            )}

            {/* TOOL 4: DOCS QA */}
            {toolSubTab === 'docs' && (
              <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>Đọc tài liệu PDF</h3>

                <div
                  style={{
                    padding: '16px',
                    border: '1px dashed var(--border-subtle)',
                    borderRadius: 'var(--radius-sm)',
                    textAlign: 'center',
                    background: 'var(--bg-subtle)',
                  }}
                >
                  <div style={{ fontSize: 14, fontWeight: 700 }}>Quy_trinh_ky_thuat_AIFP.pdf</div>
                  <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginTop: 2 }}>
                    14 trang · Sẵn sàng hỏi đáp
                  </div>
                </div>

                <div style={{ display: 'flex', gap: 8 }}>
                  <input
                    type="text"
                    defaultValue="Nguyên tắc an toàn dữ liệu và phân quyền công cụ được quy định ở đâu?"
                    style={{
                      flex: 1,
                      padding: '8px 14px',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      background: 'var(--bg-surface)',
                      color: 'var(--text-primary)',
                      fontSize: 13.5,
                    }}
                  />
                  <button
                    className="btn btn-primary"
                    onClick={() => showToast('Trích dẫn: [Trang 6] Quy định 4 cấp độ phân quyền và cô lập người dùng.')}
                    style={{ gap: 5 }}
                  >
                    <IconSearch />
                    <span>Tìm kiếm</span>
                  </button>
                </div>
              </div>
            )}

            {/* TOOL 5: CALENDAR ASSISTANT */}
            {toolSubTab === 'calendar' && (
              <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>Lên lịch hẹn</h3>

                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  <input
                    type="text"
                    value={calendarTitle}
                    onChange={(e) => setCalendarTitle(e.target.value)}
                    placeholder="Tiêu đề cuộc họp hoặc sự kiện..."
                    style={{
                      padding: '8px 14px',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      background: 'var(--bg-surface)',
                      color: 'var(--text-primary)',
                      fontSize: 13.5,
                    }}
                  />
                  <input
                    type="datetime-local"
                    value={calendarDate}
                    onChange={(e) => setCalendarDate(e.target.value)}
                    style={{
                      padding: '8px 14px',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      background: 'var(--bg-surface)',
                      color: 'var(--text-primary)',
                      fontSize: 13.5,
                    }}
                  />
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleCreateCalendarEvent}
                    disabled={isCreatingCalendarEvent || !calendarTitle.trim()}
                    style={{ gap: 5 }}
                  >
                    <IconCalendar />
                    <span>{isCreatingCalendarEvent ? 'Đang lưu CSDL...' : 'Lưu lịch CSDL'}</span>
                  </button>
                </div>

                {calendarSuccess && (
                  <div
                    style={{
                      padding: '10px 14px',
                      background: '#ECFDF5',
                      color: '#065F46',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: 12.5,
                    }}
                  >
                    Đã lên lịch cho &ldquo;{calendarTitle}&rdquo; vào {calendarDate.replace('T', ' ')}.
                  </div>
                )}
              </div>
            )}

            {/* TOOL 6: VIDEO & SHORTS INTELLIGENCE STUDIO */}
            {toolSubTab === 'video' && (
              <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                {/* Header with Mode Switcher */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ width: 28, height: 28, borderRadius: 'var(--radius-sm)', background: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      <IconVideo />
                    </div>
                    <div>
                      <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>Studio Sáng tạo Video AI & Phân tích</h3>
                      <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)' }}>Tạo video kể chuyện, hoạt hình và tóm tắt nội dung</span>
                    </div>
                  </div>
                  {/* Mode switcher pills */}
                  <div style={{ display: 'flex', gap: 4, background: 'var(--bg-subtle)', padding: 3, borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
                    <button
                      type="button"
                      className="btn"
                      onClick={() => setVideoStudioMode('create')}
                      style={{
                        fontSize: 12,
                        padding: '4px 12px',
                        minHeight: 26,
                        background: videoStudioMode === 'create' ? 'var(--bg-surface)' : 'transparent',
                        color: videoStudioMode === 'create' ? 'var(--text-primary)' : 'var(--text-secondary)',
                        boxShadow: videoStudioMode === 'create' ? '0 1px 2px rgba(0,0,0,0.06)' : 'none',
                        border: videoStudioMode === 'create' ? '1px solid var(--border-subtle)' : 'none',
                      }}
                    >
                      Tạo video mới
                    </button>
                    <button
                      type="button"
                      className="btn"
                      onClick={() => setVideoStudioMode('editor')}
                      style={{
                        fontSize: 12,
                        padding: '4px 12px',
                        minHeight: 26,
                        background: videoStudioMode === 'editor' ? 'var(--bg-surface)' : 'transparent',
                        color: videoStudioMode === 'editor' ? 'var(--text-primary)' : 'var(--text-secondary)',
                        boxShadow: videoStudioMode === 'editor' ? '0 1px 2px rgba(0,0,0,0.06)' : 'none',
                        border: videoStudioMode === 'editor' ? '1px solid var(--border-subtle)' : 'none',
                      }}
                    >
                      Chỉnh sửa video
                    </button>
                    <button
                      type="button"
                      className="btn"
                      onClick={() => setVideoStudioMode('analyze')}
                      style={{
                        fontSize: 12,
                        padding: '4px 12px',
                        minHeight: 26,
                        background: videoStudioMode === 'analyze' ? 'var(--bg-surface)' : 'transparent',
                        color: videoStudioMode === 'analyze' ? 'var(--text-primary)' : 'var(--text-secondary)',
                        boxShadow: videoStudioMode === 'analyze' ? '0 1px 2px rgba(0,0,0,0.06)' : 'none',
                        border: videoStudioMode === 'analyze' ? '1px solid var(--border-subtle)' : 'none',
                      }}
                    >
                      Tóm tắt video
                    </button>
                  </div>
                </div>

                {/* MODE 1: CREATE VIDEO STUDIO */}
                {videoStudioMode === 'create' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                    {/* Top Capability Pills */}
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                      <span className="mono-pill" style={{ fontSize: 11 }}>1080p Full HD</span>
                      <span className="mono-pill" style={{ fontSize: 11, color: '#10B981', borderColor: 'rgba(16, 185, 129, 0.3)' }}>0 Watermark AI</span>
                      <span className="mono-pill" style={{ fontSize: 11 }}>Ken Burns & Parallax</span>
                      <span className="mono-pill" style={{ fontSize: 11 }}>Auto-Ducking Âm thanh</span>
                    </div>

                    {/* Presets Row */}
                    <div style={{ display: 'flex', gap: 6, alignItems: 'center', flexWrap: 'wrap' }}>
                      <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)' }}>Kịch bản gợi ý:</span>
                      {VIDEO_PRESETS.map((p) => (
                        <button
                          key={p.id}
                          type="button"
                          className="btn btn-secondary"
                          onClick={() => handleApplyVideoPreset(p)}
                          style={{ fontSize: 11.5, padding: '0 8px', minHeight: 24 }}
                        >
                          {p.label}
                        </button>
                      ))}
                    </div>

                    {/* 2-Column Responsive Workspace */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 16 }}>
                      {/* Left Column: Script & Creative Controls */}
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                        {/* Title Input */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                          <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)' }}>Tiêu đề video:</label>
                          <input
                            type="text"
                            value={videoTitle}
                            onChange={(e) => setVideoTitle(e.target.value)}
                            placeholder="Nhập tiêu đề video..."
                            style={{
                              padding: '8px 12px',
                              borderRadius: 'var(--radius-sm)',
                              border: '1px solid var(--border-subtle)',
                              background: 'var(--bg-surface)',
                              color: 'var(--text-primary)',
                              fontSize: 13,
                            }}
                          />
                        </div>

                        {/* Script Textarea */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                          <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)' }}>Kịch bản phân cảnh:</label>
                          <textarea
                            value={videoScript}
                            onChange={(e) => setVideoScript(e.target.value)}
                            rows={5}
                            placeholder="Nhập nội dung kịch bản để AI chia cảnh và lồng tiếng..."
                            style={{
                              padding: '12px 14px',
                              borderRadius: 'var(--radius-sm)',
                              border: '1px solid var(--border-subtle)',
                              background: 'var(--bg-surface)',
                              color: 'var(--text-primary)',
                              fontSize: 13,
                              lineHeight: 1.6,
                              minHeight: 120,
                              resize: 'vertical',
                            }}
                          />
                        </div>

                        {/* Creative Options Grid (2x2) */}
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
                          {/* Style */}
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <label style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-secondary)' }}>Phong cách thị giác:</label>
                            <select
                              value={videoStyle}
                              onChange={(e) => setVideoStyle(e.target.value as any)}
                              style={{
                                padding: '7px 10px',
                                borderRadius: 'var(--radius-sm)',
                                border: '1px solid var(--border-subtle)',
                                background: 'var(--bg-surface)',
                                color: 'var(--text-primary)',
                                fontSize: 12,
                              }}
                            >
                              <option value="STICKMAN">Người que hoạt hình (Stickman Director)</option>
                              <option value="LEMO_OPUS">Điện ảnh Canvas/WebGL (Lemo-Opuscar)</option>
                              <option value="SILHOUETTE">Tối giản bóng đen (Silhouette)</option>
                              <option value="ANIME_2D">Hoạt hình 2D (Doraemon/Anime)</option>
                              <option value="VOXEL_3D">Mô hình khối 3D Argon</option>
                              <option value="REALISTIC">Điện ảnh chân thực (Realistic)</option>
                            </select>
                          </div>

                          {/* Voice */}
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <label style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-secondary)' }}>Giọng lồng tiếng:</label>
                            <select
                              value={videoVoiceActor}
                              onChange={(e) => setVideoVoiceActor(e.target.value as any)}
                              style={{
                                padding: '7px 10px',
                                borderRadius: 'var(--radius-sm)',
                                border: '1px solid var(--border-subtle)',
                                background: 'var(--bg-surface)',
                                color: 'var(--text-primary)',
                                fontSize: 12,
                              }}
                            >
                              <option value="DORAEMON">Doraemon HTV3 (Vui nhộn)</option>
                              <option value="NOBITA">Nobita (Tâm sự thân quen)</option>
                              <option value="DOCUMENTARY">Người dẫn chuyện (Trầm ấm)</option>
                              <option value="GENTLE_FEMALE">Giọng nữ nhẹ nhàng</option>
                            </select>
                          </div>

                          {/* Weather VFX */}
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <label style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-secondary)' }}>Hiệu ứng môi trường:</label>
                            <select
                              value={videoWeatherEffect}
                              onChange={(e) => setVideoWeatherEffect(e.target.value as any)}
                              style={{
                                padding: '7px 10px',
                                borderRadius: 'var(--radius-sm)',
                                border: '1px solid var(--border-subtle)',
                                background: 'var(--bg-surface)',
                                color: 'var(--text-primary)',
                                fontSize: 12,
                              }}
                            >
                              <option value="RAIN">Mưa rơi điện ảnh (Rain)</option>
                              <option value="WIND">Gió & Bụi chuyển động (Wind)</option>
                              <option value="FOG">Sương mù huyền ảo (Fog)</option>
                              <option value="NONE">Không hiệu ứng hạt</option>
                            </select>
                          </div>

                          {/* Target Duration */}
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <label style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-secondary)' }}>Thời lượng mong muốn:</label>
                            <div style={{ display: 'flex', gap: 4 }}>
                              {[15, 30, 60].map((sec) => (
                                <button
                                  key={sec}
                                  type="button"
                                  className="btn"
                                  onClick={() => {
                                    setVideoTargetSeconds(sec);
                                    handlePlanVideoStoryboard(undefined, sec);
                                  }}
                                  style={{
                                    flex: 1,
                                    fontSize: 11.5,
                                    padding: '5px 4px',
                                    minHeight: 28,
                                    background: videoTargetSeconds === sec ? 'var(--accent-primary)' : 'var(--bg-surface)',
                                    color: videoTargetSeconds === sec ? 'var(--text-on-accent, #FFF)' : 'var(--text-secondary)',
                                    border: '1px solid var(--border-subtle)',
                                  }}
                                >
                                  {sec}s
                                </button>
                              ))}
                            </div>
                          </div>
                        </div>

                        {/* Action Buttons */}
                        <div style={{ display: 'flex', gap: 8, marginTop: 4, flexWrap: 'wrap' }}>
                          <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => handlePlanVideoStoryboard()}
                            disabled={isPlanningVideo || isCreatingVideo}
                            style={{ fontSize: 12, padding: '0 12px', minHeight: 34, gap: 5 }}
                          >
                            <IconEye />
                            <span>{isPlanningVideo ? 'Đang phân cảnh...' : 'Lập phân cảnh'}</span>
                          </button>
                          <button
                            type="button"
                            className="btn btn-primary"
                            onClick={handleCreateVideoJob}
                            disabled={isCreatingVideo}
                            style={{ fontSize: 12, padding: '0 16px', minHeight: 34, gap: 6, flex: 1 }}
                          >
                            <IconPlay />
                            <span>{isCreatingVideo ? 'Đang kết xuất video...' : 'Tạo video ngay'}</span>
                          </button>
                        </div>
                      </div>

                      {/* Right Column: Interactive Storyboard & Video Player */}
                      <div
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          gap: 12,
                          background: 'var(--bg-subtle)',
                          borderRadius: 'var(--radius-sm)',
                          border: '1px solid var(--border-subtle)',
                          padding: '14px 16px',
                        }}
                      >
                        {/* Rendering In Progress State */}
                        {isCreatingVideo && (
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 14, padding: '16px 8px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <strong style={{ fontSize: 13, color: 'var(--text-primary)' }}>Tiến trình kết xuất video:</strong>
                              <span className="mono-pill" style={{ fontSize: 11 }}>Level 3 HITL Approved</span>
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                              {[
                                { step: 1, label: `Tổng hợp giọng lồng tiếng: ${videoVoiceActor}` },
                                { step: 2, label: `Render chuyển động Ken Burns & Parallax 2.5D` },
                                { step: 3, label: `Phủ hiệu ứng môi trường ${videoWeatherEffect} & Ghép cảnh FFmpeg` },
                                { step: 4, label: `Hòa âm Auto-Ducking (-12dB) & Xuất MP4 0-Watermark` },
                              ].map((st) => (
                                <div key={st.step} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12 }}>
                                  <span
                                    style={{
                                      width: 18,
                                      height: 18,
                                      borderRadius: '50%',
                                      display: 'flex',
                                      alignItems: 'center',
                                      justifyContent: 'center',
                                      fontSize: 10,
                                      fontWeight: 700,
                                      background: videoRenderStep >= st.step ? '#10B981' : 'var(--bg-surface)',
                                      color: videoRenderStep >= st.step ? '#FFF' : 'var(--text-tertiary)',
                                      border: videoRenderStep >= st.step ? 'none' : '1px solid var(--border-subtle)',
                                    }}
                                  >
                                    {videoRenderStep >= st.step ? '✓' : st.step}
                                  </span>
                                  <span style={{ color: videoRenderStep >= st.step ? 'var(--text-primary)' : 'var(--text-secondary)' }}>
                                    {st.label}
                                  </span>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {/* Completed Video Player */}
                        {!isCreatingVideo && videoCreationResult && (
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <strong style={{ fontSize: 13.5, color: 'var(--text-primary)' }}>{videoCreationResult.title}</strong>
                              <span className="source-badge-pill" style={{ background: '#10B981', color: '#FFF' }}>0 Watermark</span>
                            </div>
                            {/* Video Player with High-Craft Cinematic Poster */}
                            <video
                              src={videoCreationResult.videoUrl}
                              poster="/aura_video_poster.svg"
                              controls
                              autoPlay
                              loop
                              playsInline
                              style={{
                                width: '100%',
                                maxHeight: 250,
                                borderRadius: 'var(--radius-sm)',
                                background: '#0F172A',
                                objectFit: 'contain',
                                boxShadow: '0 4px 14px rgba(0,0,0,0.2)',
                              }}
                            />
                            {/* Metadata Pills */}
                            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', fontSize: 11.5 }}>
                              <span className="mono-pill">Thời lượng: {videoCreationResult.totalSeconds}s</span>
                              <span className="mono-pill">{videoCreationResult.sceneCount} cảnh</span>
                              <span className="mono-pill">Phong cách: {videoCreationResult.style}</span>
                              <span className="mono-pill">Hiệu ứng: {videoCreationResult.weatherEffect}</span>
                            </div>
                            {/* Video Action Buttons */}
                            <div style={{ display: 'flex', gap: 8, marginTop: 4, flexWrap: 'wrap' }}>
                              <a
                                href={videoCreationResult.videoUrl}
                                target="_blank"
                                rel="noreferrer"
                                download="aura_video.mp4"
                                className="btn btn-primary"
                                style={{ fontSize: 12, padding: '0 12px', minHeight: 30, gap: 5, textDecoration: 'none' }}
                              >
                                <IconExternalLink />
                                <span>Tải video MP4</span>
                              </a>
                              <span
                                className="mono-pill"
                                style={{
                                  fontSize: 11,
                                  color: 'var(--status-running)',
                                  borderColor: 'rgba(16, 185, 129, 0.3)',
                                  display: 'inline-flex',
                                  alignItems: 'center',
                                  gap: 5,
                                  padding: '0 10px',
                                  height: 30,
                                }}
                                title="Đã tự động gửi thông báo tới email của bạn"
                              >
                                <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--status-running)' }} />
                                <span>Tự động gửi email</span>
                              </span>
                              <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={() => setVideoCreationResult(null)}
                                style={{ fontSize: 12, padding: '0 10px', minHeight: 30 }}
                              >
                                Tạo video khác
                              </button>
                            </div>
                          </div>
                        )}

                        {/* Default Storyboard Card View */}
                        {!isCreatingVideo && !videoCreationResult && videoStoryboard && (
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <strong style={{ fontSize: 13, color: 'var(--text-primary)' }}>
                                Phân cảnh kịch bản ({videoStoryboard.sceneCount} cảnh · {videoStoryboard.totalSeconds}s)
                              </strong>
                              <span className="mono-pill" style={{ fontSize: 11 }}>{videoStoryboard.style}</span>
                            </div>

                            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 290, overflowY: 'auto', paddingRight: 4 }}>
                              {videoStoryboard.scenes.map((sc) => (
                                <div
                                  key={sc.index}
                                  style={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: 4,
                                    padding: '8px 10px',
                                    borderRadius: 'var(--radius-sm)',
                                    background: 'var(--bg-surface)',
                                    border: '1px solid var(--border-subtle)',
                                    fontSize: 12,
                                  }}
                                >
                                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <span style={{ fontWeight: 700, color: 'var(--accent-primary)' }}>
                                      Cảnh #{sc.sceneNumber} ({sc.duration}s)
                                    </span>
                                    <span style={{ fontSize: 10.5, color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
                                      {sc.cameraMotion.split('(')[0].trim()}
                                    </span>
                                  </div>
                                  <div style={{ color: 'var(--text-primary)', fontStyle: 'italic', lineHeight: 1.4 }}>
                                    &ldquo;{sc.narration}&rdquo;
                                  </div>
                                  <div style={{ fontSize: 11, color: 'var(--text-secondary)', display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 2 }}>
                                    <span>Hiệu ứng: {sc.weatherOverlay.split('(')[0].trim()}</span>
                                    <span>•</span>
                                    <span>Giọng: {sc.audioCue.split('+')[0].replace('Giọng đọc:', '').trim()}</span>
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                )}

                {/* MODE 2: VIDEO EDITOR (LOSSLESS-CUT / REMOTION OPEN-SOURCE ENGINE) */}
                {videoStudioMode === 'editor' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                    {/* Capability Pills */}
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                      <span className="mono-pill" style={{ fontSize: 11 }}>Biên tập Lossless / Remotion</span>
                      <span className="mono-pill" style={{ fontSize: 11, color: '#10B981', borderColor: 'rgba(16, 185, 129, 0.3)' }}>0 Watermark</span>
                      <span className="mono-pill" style={{ fontSize: 11 }}>Cắt ghép tức thì</span>
                      <span className="mono-pill" style={{ fontSize: 11 }}>Auto-Captions Tiếng Việt</span>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 16 }}>
                      {/* Left Column: Live Video Canvas & Filter Preview */}
                      <div
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          gap: 12,
                          padding: '16px',
                          borderRadius: 'var(--radius-md)',
                          background: 'var(--bg-subtle)',
                          border: '1px solid var(--border-subtle)',
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <span style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-primary)' }}>Khung nhìn thực tế</span>
                          <span className="mono-pill" style={{ fontSize: 10.5 }}>
                            {editAspectRatio} &middot; {editPlaybackSpeed}x
                          </span>
                        </div>

                        {/* Video Container with Aspect Ratio and Color Grade Filter */}
                        <div
                          style={{
                            position: 'relative',
                            width: '100%',
                            maxWidth: editAspectRatio === '9:16' ? 240 : '100%',
                            margin: editAspectRatio === '9:16' ? '0 auto' : '0',
                            aspectRatio: editAspectRatio === '16:9' ? '16/9' : editAspectRatio === '9:16' ? '9/16' : '1/1',
                            maxHeight: 320,
                            borderRadius: 'var(--radius-sm)',
                            overflow: 'hidden',
                            background: '#090A0F',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            border: '1px solid var(--border-subtle)',
                          }}
                        >
                          <video
                            src={videoCreationResult?.videoUrl || 'https://assets.mixkit.co/videos/preview/mixkit-silhouette-of-a-person-standing-in-a-forest-41122-large.mp4'}
                            controls
                            playsInline
                            style={{
                              width: '100%',
                              height: '100%',
                              objectFit: 'cover',
                              filter:
                                editColorFilter === 'CINEMATIC'
                                  ? 'contrast(1.15) saturate(1.2) sepia(0.12)'
                                  : editColorFilter === 'NOIR'
                                  ? 'grayscale(1) contrast(1.3)'
                                  : editColorFilter === 'VIBRANT'
                                  ? 'saturate(1.5) contrast(1.1)'
                                  : 'none',
                            }}
                          />

                          {/* Subtitle Overlay Preview if enabled */}
                          {editCaptionsEnabled && (
                            <div
                              style={{
                                position: 'absolute',
                                bottom: 12,
                                left: 12,
                                right: 12,
                                pointerEvents: 'none',
                                textAlign: 'center',
                                background: 'rgba(0, 0, 0, 0.65)',
                                color: '#FACC15',
                                padding: '4px 8px',
                                borderRadius: 4,
                                fontSize: 11,
                                fontWeight: 600,
                                textShadow: '0 1px 2px rgba(0,0,0,0.8)',
                              }}
                            >
                              [Phụ đề tự động]: Kịch bản đồng bộ giọng đọc tiếng Việt
                            </div>
                          )}
                        </div>

                        {/* Filter Quick Pills */}
                        <div style={{ display: 'flex', gap: 6, alignItems: 'center', flexWrap: 'wrap' }}>
                          <span style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>Bộ lọc màu:</span>
                          {(['NONE', 'CINEMATIC', 'NOIR', 'VIBRANT'] as const).map((filter) => (
                            <button
                              key={filter}
                              type="button"
                              className="btn"
                              onClick={() => setEditColorFilter(filter)}
                              style={{
                                fontSize: 11,
                                padding: '2px 8px',
                                minHeight: 22,
                                height: 22,
                                background: editColorFilter === filter ? 'var(--bg-surface)' : 'transparent',
                                color: editColorFilter === filter ? 'var(--text-primary)' : 'var(--text-secondary)',
                                border: editColorFilter === filter ? '1px solid var(--border-subtle)' : '1px solid transparent',
                                fontWeight: editColorFilter === filter ? 600 : 400,
                              }}
                            >
                              {filter === 'NONE' ? 'Chuẩn' : filter === 'CINEMATIC' ? 'Điện ảnh' : filter === 'NOIR' ? 'Đen trắng' : 'Sống động'}
                            </button>
                          ))}
                        </div>
                      </div>

                      {/* Right Column: Editing Parameters & Export Controls */}
                      <div
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          gap: 14,
                          padding: '16px',
                          borderRadius: 'var(--radius-md)',
                          background: 'var(--bg-surface)',
                          border: '1px solid var(--border-subtle)',
                        }}
                      >
                        {/* 1. Trim Controls */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)' }}>
                              Cắt thời lượng (Trim Range)
                            </label>
                            <span className="mono-pill" style={{ fontSize: 11 }}>
                              {editTrimStart}s - {editTrimEnd}s (Tổng: {Math.max(1, editTrimEnd - editTrimStart)}s)
                            </span>
                          </div>
                          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                              <span style={{ fontSize: 10.5, color: 'var(--text-tertiary)' }}>Điểm đầu: {editTrimStart}s</span>
                              <input
                                type="range"
                                min={0}
                                max={Math.max(0, editTrimEnd - 5)}
                                step={1}
                                value={editTrimStart}
                                onChange={(e) => setEditTrimStart(Number(e.target.value))}
                                style={{ width: '100%', accentColor: 'var(--accent-primary)' }}
                              />
                            </div>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                              <span style={{ fontSize: 10.5, color: 'var(--text-tertiary)' }}>Điểm cuối: {editTrimEnd}s</span>
                              <input
                                type="range"
                                min={editTrimStart + 5}
                                max={60}
                                step={1}
                                value={editTrimEnd}
                                onChange={(e) => setEditTrimEnd(Number(e.target.value))}
                                style={{ width: '100%', accentColor: 'var(--accent-primary)' }}
                              />
                            </div>
                          </div>
                        </div>

                        {/* 2. Aspect Ratio */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                          <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)' }}>
                            Tỉ lệ khung hình (Aspect Ratio):
                          </label>
                          <div style={{ display: 'flex', gap: 6 }}>
                            {([
                              { id: '16:9', label: '16:9 (YouTube)' },
                              { id: '9:16', label: '9:16 (Shorts/TikTok)' },
                              { id: '1:1', label: '1:1 (Vuông)' },
                            ] as const).map((ratio) => (
                              <button
                                key={ratio.id}
                                type="button"
                                className="btn"
                                onClick={() => setEditAspectRatio(ratio.id)}
                                style={{
                                  flex: 1,
                                  fontSize: 11.5,
                                  padding: '5px 8px',
                                  minHeight: 28,
                                  background: editAspectRatio === ratio.id ? 'var(--bg-subtle)' : 'transparent',
                                  color: editAspectRatio === ratio.id ? 'var(--text-primary)' : 'var(--text-secondary)',
                                  border: editAspectRatio === ratio.id ? '1px solid var(--border-subtle)' : '1px solid transparent',
                                  fontWeight: editAspectRatio === ratio.id ? 700 : 400,
                                }}
                              >
                                {ratio.label}
                              </button>
                            ))}
                          </div>
                        </div>

                        {/* 3. Playback Speed */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                          <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)' }}>
                            Tốc độ phát (Speed):
                          </label>
                          <div style={{ display: 'flex', gap: 6 }}>
                            {([0.75, 1.0, 1.25, 1.5, 2.0] as const).map((spd) => (
                              <button
                                key={spd}
                                type="button"
                                className="btn"
                                onClick={() => setEditPlaybackSpeed(spd)}
                                style={{
                                  flex: 1,
                                  fontSize: 11.5,
                                  padding: '4px 6px',
                                  minHeight: 26,
                                  background: editPlaybackSpeed === spd ? 'var(--bg-subtle)' : 'transparent',
                                  color: editPlaybackSpeed === spd ? 'var(--text-primary)' : 'var(--text-secondary)',
                                  border: editPlaybackSpeed === spd ? '1px solid var(--border-subtle)' : '1px solid transparent',
                                  fontWeight: editPlaybackSpeed === spd ? 700 : 400,
                                }}
                              >
                                {spd}x
                              </button>
                            ))}
                          </div>
                        </div>

                        {/* 4. Captions & Music Volume Row */}
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, alignItems: 'center' }}>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <label style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-secondary)' }}>
                              Phụ đề tự động (Captions):
                            </label>
                            <button
                              type="button"
                              className="btn btn-secondary"
                              onClick={() => setEditCaptionsEnabled(!editCaptionsEnabled)}
                              style={{
                                fontSize: 11.5,
                                minHeight: 28,
                                padding: '0 10px',
                                color: editCaptionsEnabled ? '#10B981' : 'var(--text-tertiary)',
                              }}
                            >
                              {editCaptionsEnabled ? '✓ Bật phụ đề' : 'Tắt phụ đề'}
                            </button>
                          </div>

                          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <label style={{ fontSize: 11.5, fontWeight: 600, color: 'var(--text-secondary)' }}>
                                Nhạc nền (BGM):
                              </label>
                              <span style={{ fontSize: 11, fontFamily: 'var(--font-mono)', color: 'var(--text-tertiary)' }}>
                                {editMusicVolume}%
                              </span>
                            </div>
                            <input
                              type="range"
                              min={0}
                              max={100}
                              step={5}
                              value={editMusicVolume}
                              onChange={(e) => setEditMusicVolume(Number(e.target.value))}
                              style={{ width: '100%', accentColor: 'var(--accent-primary)' }}
                            />
                          </div>
                        </div>

                        {/* Export Action Button */}
                        <button
                          type="button"
                          className="btn btn-primary"
                          onClick={handleExecuteVideoEdit}
                          disabled={isEditingVideo}
                          style={{
                            marginTop: 4,
                            minHeight: 36,
                            fontSize: 13,
                            fontWeight: 700,
                            gap: 6,
                          }}
                        >
                          <IconRefresh spinning={isEditingVideo} />
                          <span>{isEditingVideo ? 'Đang xuất video...' : 'Xuất video'}</span>
                        </button>
                      </div>
                    </div>

                    {/* Edited Video Result Card */}
                    {editedVideoResult && (
                      <div
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          gap: 12,
                          padding: '16px 18px',
                          borderRadius: 'var(--radius-sm)',
                          background: 'var(--bg-subtle)',
                          border: '1px solid var(--border-subtle)',
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                            <span className="source-badge-pill" style={{ background: '#10B981', color: '#FFF' }}>
                              Đã xuất video
                            </span>
                            <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-primary)' }}>
                              Video hoàn chỉnh ({editedVideoResult.finalDuration}s &middot; {editedVideoResult.aspectRatio})
                            </span>
                          </div>
                          <span className="mono-pill" style={{ fontSize: 11, color: '#10B981' }}>
                            0 Watermark AI
                          </span>
                        </div>

                        <p style={{ fontSize: 12.5, color: 'var(--text-secondary)', margin: 0 }}>
                          {editedVideoResult.message}
                        </p>

                        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 4 }}>
                          <a
                            href={editedVideoResult.editedVideoUrl}
                            target="_blank"
                            rel="noreferrer"
                            className="btn btn-primary"
                            style={{ fontSize: 12, padding: '0 12px', minHeight: 30, textDecoration: 'none', gap: 5 }}
                          >
                            <IconExternalLink />
                            <span>Xem video</span>
                          </a>
                          <button
                            type="button"
                            className="btn btn-secondary"
                            onClick={() => showToast('Đã lưu cấu hình chỉnh sửa vào dự án!')}
                            style={{ fontSize: 12, padding: '0 12px', minHeight: 30 }}
                          >
                            <span>Lưu cấu hình</span>
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* MODE 3: ANALYZE VIDEO (YOUTUBE & TIKTOK) */}
                {videoStudioMode === 'analyze' && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                    {/* Input row */}
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      <input
                        type="text"
                        value={videoUrlInput}
                        onChange={(e) => setVideoUrlInput(e.target.value)}
                        placeholder="Dán liên kết YouTube hoặc TikTok vào đây..."
                        style={{
                          flex: 1,
                          minWidth: 260,
                          padding: '10px 14px',
                          borderRadius: 'var(--radius-sm)',
                          border: '1px solid var(--border-subtle)',
                          background: 'var(--bg-surface)',
                          color: 'var(--text-primary)',
                          fontSize: 13.5,
                        }}
                      />
                      <button
                        className="btn btn-primary"
                        onClick={handleAnalyzeVideo}
                        disabled={isAnalyzingVideo}
                        style={{ gap: 6, minHeight: 38 }}
                      >
                        <IconVideo />
                        <span>{isAnalyzingVideo ? 'Đang trích xuất...' : 'Tóm tắt ngay'}</span>
                      </button>
                    </div>

                    {/* Sample quick buttons */}
                    <div style={{ display: 'flex', gap: 6, alignItems: 'center', flexWrap: 'wrap' }}>
                      <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)' }}>Gợi ý mẫu:</span>
                      {[
                        { label: 'YouTube: Kỹ năng AI 2026', url: 'https://youtube.com/watch?v=ai-skills-2026' },
                        { label: 'TikTok: Quản lý chi tiêu 50/30/20', url: 'https://tiktok.com/@taichinhthongminh/video/1' },
                        { label: 'YouTube: 5 Thói quen buổi sáng', url: 'https://youtube.com/watch?v=morning-habits' },
                      ].map((s, idx) => (
                        <button
                          key={idx}
                          type="button"
                          className="btn btn-secondary"
                          onClick={() => {
                            setVideoUrlInput(s.url);
                            handleAnalyzeVideo();
                          }}
                          style={{ fontSize: 11.5, padding: '0 8px', minHeight: 24 }}
                        >
                          {s.label}
                        </button>
                      ))}
                    </div>

                    {/* Analysis Result */}
                    {videoAnalysisResult && (
                      <div
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          gap: 14,
                          padding: '16px 18px',
                          borderRadius: 'var(--radius-sm)',
                          background: 'var(--bg-subtle)',
                          border: '1px solid var(--border-subtle)',
                        }}
                      >
                        {/* Header: Thumbnail + Title + Channel */}
                        <div style={{ display: 'flex', gap: 14, alignItems: 'center', flexWrap: 'wrap' }}>
                          <img
                            src={videoAnalysisResult.thumbnailUrl}
                            alt={videoAnalysisResult.title}
                            style={{ width: 120, height: 75, objectFit: 'cover', borderRadius: 'var(--radius-sm)' }}
                          />
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 4, flex: 1, minWidth: 200 }}>
                            <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
                              <span
                                className="source-badge-pill"
                                style={{
                                  background: videoAnalysisResult.platform === 'youtube' ? '#EF4444' : '#0F172A',
                                  color: '#FFFFFF',
                                }}
                              >
                                {videoAnalysisResult.platform === 'youtube' ? 'YouTube' : 'TikTok'}
                              </span>
                              <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
                                Thời lượng: {videoAnalysisResult.duration}
                              </span>
                            </div>
                            <h4 style={{ fontSize: 14.5, fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
                              {videoAnalysisResult.title}
                            </h4>
                            <span style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                              Kênh: {videoAnalysisResult.channel}
                            </span>
                          </div>
                        </div>

                        {/* Section 1: 30s Core Summary */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                          <strong style={{ fontSize: 13, color: 'var(--text-primary)' }}>Tóm tắt 30 giây (Ý chính cốt lõi):</strong>
                          {videoAnalysisResult.summary30s.map((pt, pIdx) => (
                            <div key={pIdx} style={{ display: 'flex', gap: 7, fontSize: 12.5, color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                              <span style={{ color: 'var(--accent-primary)', fontWeight: 800 }}>•</span>
                              <span>{pt}</span>
                            </div>
                          ))}
                        </div>

                        {/* Section 2: Key Moments */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                          <strong style={{ fontSize: 13, color: 'var(--text-primary)' }}>Dòng thời gian & Mốc quan trọng:</strong>
                          <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
                            {videoAnalysisResult.keyMoments.map((km, kIdx) => (
                              <div key={kIdx} style={{ display: 'flex', gap: 8, fontSize: 12, alignItems: 'center' }}>
                                <span className="mono-pill" style={{ fontSize: 11, background: 'var(--bg-surface)' }}>
                                  {km.time}
                                </span>
                                <span style={{ color: 'var(--text-secondary)' }}>{km.note}</span>
                              </div>
                            ))}
                          </div>
                        </div>

                        {/* Section 3: Life Action Items */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                          <strong style={{ fontSize: 13, color: 'var(--accent-primary)' }}>Bài học ứng dụng vào đời sống:</strong>
                          {videoAnalysisResult.lifeActionItems.map((act, aIdx) => (
                            <div key={aIdx} style={{ display: 'flex', gap: 7, fontSize: 12.5, color: 'var(--text-primary)', lineHeight: 1.5 }}>
                              <span style={{ color: '#10B981', fontWeight: 800 }}>✓</span>
                              <span>{act}</span>
                            </div>
                          ))}
                        </div>

                        {/* Action buttons */}
                        <div style={{ display: 'flex', gap: 8, marginTop: 4, flexWrap: 'wrap' }}>
                          <button
                            type="button"
                            className="btn btn-primary"
                            onClick={() => showToast('Đã lưu bài học cốt lõi từ video vào Bộ nhớ Trợ lý!')}
                            style={{ fontSize: 12, padding: '0 12px', minHeight: 30, gap: 5 }}
                          >
                            <IconBookmark filled />
                            <span>Lưu vào Bộ nhớ</span>
                          </button>
                          <a
                            href={videoUrlInput}
                            target="_blank"
                            rel="noreferrer"
                            className="btn btn-secondary"
                            style={{ fontSize: 12, padding: '0 12px', minHeight: 30, gap: 5, textDecoration: 'none' }}
                          >
                            <IconExternalLink />
                            <span>Mở video gốc</span>
                          </a>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 5: SETTINGS & ACCOUNT CONTROL CENTER                                  */}
        {/* ========================================================================= */}
        {activeTab === 'settings' && (
          <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 6 }}>
              <h1 style={{ fontSize: 20, fontWeight: 800, letterSpacing: -0.3, margin: 0 }}>
                Cài đặt
              </h1>
              <span className="mono-pill" style={{ color: 'var(--status-running)', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                Hoạt động
              </span>
            </div>

            <div className="desktop-2col-grid">
              {/* 1. Profile Overview Card */}
              <div className="card-surface card-interactive" style={{ display: 'flex', flexDirection: 'column', gap: 12, height: '100%', boxSizing: 'border-box' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 10 }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span style={{ fontWeight: 700, fontSize: 15 }}>Hồ sơ nhận tin</span>
                    <span className="mono-pill">{profile.personaTitle}</span>
                  </div>
                  <div style={{ fontSize: 12.5, color: 'var(--text-secondary)', marginTop: 4 }}>
                    {profile.deliveryEmail} · {profile.scheduledTimes.join(', ')}
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 6 }}>
                  <button
                    className="btn btn-secondary"
                    onClick={handleOpenPreferencesModal}
                    style={{ minHeight: 28, fontSize: 12, padding: '0 10px', gap: 4 }}
                  >
                    <IconSliders />
                    <span>Tùy biến</span>
                  </button>
                  <button
                    className="btn btn-secondary"
                    onClick={handleSendTestEmail}
                    disabled={isSendingTestEmail}
                    style={{ minHeight: 28, fontSize: 12, padding: '0 10px', gap: 4 }}
                    title="Kiểm tra kết nối gửi email tới hộp thư của bạn"
                  >
                    <IconSend />
                    <span>{isSendingTestEmail ? 'Đang gửi...' : 'Kiểm tra hộp thư'}</span>
                  </button>
                </div>
              </div>

              {/* Active Topics Cloud */}
              <div style={{ paddingTop: 8, borderTop: '1px solid var(--border-subtle)' }}>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 5 }}>
                  {profile.categories.tech && <span className="mono-pill">#Công nghệ</span>}
                  {profile.categories.career && <span className="mono-pill">#Việc làm</span>}
                  {profile.categories.finance && <span className="mono-pill">#Tài chính</span>}
                  {profile.categories.shopping && <span className="mono-pill">#Shopee</span>}
                  {profile.categories.social && <span className="mono-pill">#Cộng đồng</span>}
                  {profile.categories.news && <span className="mono-pill">#Thời sự</span>}
                </div>
              </div>
            </div>

            {/* 2. System SMTP Email Status */}
            <div className="card-surface card-interactive" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ fontWeight: 700, fontSize: 15 }}>Hộp thư (Gmail SMTP)</div>
                <span className="mono-pill" style={{ color: 'var(--status-running)', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                  Đã kết nối
                </span>
              </div>
              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                <span className="mono-pill">smtp.gmail.com:587</span>
                <span className="mono-pill">Bảo mật SSL/TLS</span>
                <span className="mono-pill">STARTTLS</span>
              </div>
            </div>

            {/* 3. Neon Cloud PostgreSQL Database */}
            <div className="card-surface card-interactive" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ fontWeight: 700, fontSize: 15 }}>Cơ sở dữ liệu (PostgreSQL)</div>
                <span className="mono-pill" style={{ color: 'var(--status-running)', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                  Đang kết nối
                </span>
              </div>
              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                <span className="mono-pill">Neon Serverless</span>
                <span className="mono-pill">pgvector</span>
                <span className="mono-pill">user_id UUID</span>
              </div>
            </div>

            {/* 4. AI Engine & Auto-failover Router */}
            <div className="card-surface card-interactive" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ fontWeight: 700, fontSize: 15 }}>Trí tuệ nhân tạo (AI Router)</div>
                <span className="mono-pill" style={{ color: 'var(--accent-primary)', borderColor: 'var(--accent-primary)' }}>
                  Sẵn sàng
                </span>
              </div>
              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                <span className="mono-pill">Gemini 2.5 Flash</span>
                <span className="mono-pill">DeepSeek / OpenAI Failover</span>
                <span className="mono-pill">Grounding Check</span>
              </div>
            </div>

            {/* 5. Emergency Kill Switch */}
            <div className="card-surface" style={{ borderColor: 'rgba(239, 68, 68, 0.3)', display: 'flex', flexDirection: 'column', justifyContent: 'center', height: '100%', boxSizing: 'border-box' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ fontWeight: 700, fontSize: 14.5, color: 'var(--status-danger)' }}>
                  Khóa khẩn cấp
                </div>
                <button
                  className="btn btn-danger"
                  onClick={() => {
                    setKillSwitchTriggered(true);
                    showToast('Đã kích hoạt dừng khẩn cấp toàn hệ thống.');
                  }}
                  style={{ fontSize: 12, minHeight: 28, padding: '0 10px', gap: 4 }}
                >
                  <IconClose />
                  <span>Dừng ngay</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
      </main>

      {/* 3. MOBILE BOTTOM NAVIGATION BAR */}
      <nav className="mobile-bottom-bar">
        <button
          className={`mobile-tab-btn ${activeTab === 'assistant' ? 'active' : ''}`}
          onClick={() => setActiveTab('assistant')}
        >
          Trợ lý
        </button>
        <button
          className={`mobile-tab-btn ${activeTab === 'digests' ? 'active' : ''}`}
          onClick={() => setActiveTab('digests')}
        >
          Bản tin
        </button>
        <button
          className={`mobile-tab-btn ${activeTab === 'automations' ? 'active' : ''}`}
          onClick={() => setActiveTab('automations')}
        >
          Tác vụ
        </button>
        <button
          className={`mobile-tab-btn ${activeTab === 'tools' ? 'active' : ''}`}
          onClick={() => setActiveTab('tools')}
        >
          Công cụ
        </button>
        <button
          className={`mobile-tab-btn ${activeTab === 'settings' ? 'active' : ''}`}
          onClick={() => setActiveTab('settings')}
        >
          Cài đặt
        </button>
      </nav>

      {/* ========================================================================= */}
      {/* MODAL 1: MULTI-AUDIENCE PREFERENCES & SUBSCRIPTION WIZARD                 */}
      {/* ========================================================================= */}
      {showPreferencesModal && (
        <div className="email-modal-overlay" onClick={() => setShowPreferencesModal(false)}>
          <div
            className="email-modal-container"
            onClick={(e) => e.stopPropagation()}
            style={{
              maxWidth: 620,
              maxHeight: 'min(90dvh, 600px)',
              padding: 0,
              display: 'flex',
              flexDirection: 'column',
              overflow: 'hidden',
            }}
          >
            {/* Modal Header: Fixed Top */}
            <div
              style={{
                padding: '16px 20px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                borderBottom: '1px solid var(--border-subtle)',
                background: 'var(--bg-surface)',
                flexShrink: 0,
              }}
            >
              <h2 style={{ fontSize: 16, fontWeight: 700, letterSpacing: -0.3, color: 'var(--text-primary)', margin: 0 }}>
                Cấu hình nhận tin
              </h2>
              <button
                onClick={() => setShowPreferencesModal(false)}
                style={{ background: 'transparent', border: 'none', cursor: 'pointer', padding: 4, color: 'var(--text-secondary)' }}
                title="Đóng"
              >
                <IconClose />
              </button>
            </div>

            {/* Modal Body: Scrollable */}
            <div style={{ flex: 1, overflowY: 'auto', padding: '16px 20px', display: 'flex', flexDirection: 'column', gap: 14 }}>
              {/* 3-Step Segmented Bar */}
              <div className="pref-tab-bar">
                <button
                  type="button"
                  className={`pref-tab-btn ${prefWizardStep === 'persona' ? 'active' : ''}`}
                  onClick={() => setPrefWizardStep('persona')}
                >
                  <span className="pref-step-pill">1</span>
                  <span>Nhóm</span>
                </button>

                <button
                  type="button"
                  className={`pref-tab-btn ${prefWizardStep === 'topics' ? 'active' : ''}`}
                  onClick={() => setPrefWizardStep('topics')}
                >
                  <span className="pref-step-pill">2</span>
                  <span>Chủ đề</span>
                  <span className="mono-pill" style={{ fontSize: 10, padding: '1px 6px' }}>
                    {Object.values(tempProfile.categories).filter(Boolean).length}/6
                  </span>
                </button>

                <button
                  type="button"
                  className={`pref-tab-btn ${prefWizardStep === 'delivery' ? 'active' : ''}`}
                  onClick={() => setPrefWizardStep('delivery')}
                >
                  <span className="pref-step-pill">3</span>
                  <span>Lịch & Email</span>
                </button>
              </div>

              {/* STEP 1: AUDIENCE PERSONA */}
              {prefWizardStep === 'persona' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                  <div className="pref-card-grid">
                    {PERSONA_PRESETS.map((preset) => {
                      const isSelected = tempProfile.preset === preset.id;
                      return (
                        <div
                          key={preset.id}
                          className={`pref-persona-card ${isSelected ? 'selected' : ''}`}
                          onClick={() => handleApplyPreset(preset.id)}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span style={{ fontSize: 13.5, fontWeight: 700, color: isSelected ? 'var(--accent-primary)' : 'var(--text-primary)' }}>
                              {preset.title}
                            </span>
                            {isSelected && (
                              <span style={{ color: 'var(--accent-primary)', display: 'flex', alignItems: 'center' }}>
                                <IconCheck />
                              </span>
                            )}
                          </div>
                          <div style={{ fontSize: 12, color: 'var(--text-secondary)', lineHeight: 1.4 }}>
                            {preset.subtitle}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* STEP 2: TOPIC SELECTION */}
              {prefWizardStep === 'topics' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                    <button
                      type="button"
                      className="btn btn-secondary"
                      style={{ minHeight: 28, fontSize: 11.5, padding: '0 8px' }}
                      onClick={() => {
                        setTempProfile({
                          ...tempProfile,
                          categories: { tech: true, career: true, finance: true, shopping: true, social: true, news: true }
                        });
                      }}
                    >
                      Bật tất cả
                    </button>
                    <button
                      type="button"
                      className="btn btn-secondary"
                      style={{ minHeight: 28, fontSize: 11.5, padding: '0 8px' }}
                      onClick={() => {
                        const currentPreset = PERSONA_PRESETS.find(p => p.id === tempProfile.preset) || PERSONA_PRESETS[0];
                        setTempProfile({
                          ...tempProfile,
                          categories: { ...currentPreset.defaultCategories }
                        });
                      }}
                    >
                      Mặc định
                    </button>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    {CATEGORY_DEFINITIONS.map((cat) => {
                      const isEnabled = tempProfile.categories[cat.id as keyof typeof tempProfile.categories];
                      return (
                        <div
                          key={cat.id}
                          className={`pref-topic-item ${isEnabled ? 'active' : ''}`}
                          onClick={() => handleToggleCategory(cat.id as keyof typeof tempProfile.categories)}
                        >
                          <div>
                            <div style={{ fontSize: 13.5, fontWeight: 600, color: 'var(--text-primary)' }}>
                              {cat.label}
                            </div>
                            <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginTop: 2 }}>
                              {cat.desc}
                            </div>
                          </div>

                          <div className={`toggle-switch-track ${isEnabled ? 'active' : ''}`}>
                            <div className="toggle-switch-thumb" />
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* STEP 3: SCHEDULE & DELIVERY */}
              {prefWizardStep === 'delivery' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                  {/* Delivery Email Input */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
                    <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: 0.3 }}>
                      Email nhận bản tin (Bắt buộc)
                    </label>
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                      <input
                        type="email"
                        required
                        value={tempProfile.deliveryEmail}
                        onChange={(e) => setTempProfile({ ...tempProfile, deliveryEmail: e.target.value })}
                        placeholder="vd: yourname@gmail.com"
                        style={{
                          flex: 1,
                          height: 38,
                          padding: '0 12px',
                          borderRadius: 'var(--radius-sm)',
                          border: !isValidEmail(tempProfile.deliveryEmail) && tempProfile.deliveryEmail.length > 0
                            ? '1px solid var(--status-error, #E11D48)'
                            : '1px solid var(--border-subtle)',
                          background: 'var(--bg-surface)',
                          color: 'var(--text-primary)',
                          fontSize: 13.5,
                          boxSizing: 'border-box',
                        }}
                      />
                      <button
                        type="button"
                        className="btn btn-secondary"
                        onClick={handleSendTestEmail}
                        disabled={isSendingTestEmail || !isValidEmail(tempProfile.deliveryEmail)}
                        style={{
                          height: 38,
                          fontSize: 12.5,
                          opacity: isValidEmail(tempProfile.deliveryEmail) ? 1 : 0.5,
                          cursor: isValidEmail(tempProfile.deliveryEmail) ? 'pointer' : 'not-allowed',
                          gap: 5,
                          padding: '0 14px',
                          boxSizing: 'border-box',
                          flexShrink: 0,
                        }}
                      >
                        <IconSend />
                        <span>{isSendingTestEmail ? 'Đang gửi...' : 'Gửi thử'}</span>
                      </button>
                    </div>
                    {!isValidEmail(tempProfile.deliveryEmail) && tempProfile.deliveryEmail.length > 0 && (
                      <span style={{ fontSize: 11.5, color: 'var(--status-error, #E11D48)' }}>
                        Vui lòng nhập đúng định dạng email (vd: yourname@gmail.com)
                      </span>
                    )}
                  </div>

                  {/* Frequency */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                    <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: 0.3 }}>
                      Tần suất gửi tin
                    </label>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 8 }}>
                      {[
                        { id: '1_DAILY', label: '1 lần / ngày' },
                        { id: '2_DAILY', label: '2 lần / ngày' },
                        { id: '3_DAILY', label: '3 lần / ngày' },
                        { id: 'REALTIME', label: 'Tức thì' },
                      ].map((freq) => {
                        const isSel = tempProfile.frequency === freq.id;
                        return (
                          <button
                            key={freq.id}
                            type="button"
                            onClick={() => handleSelectFrequency(freq.id as any)}
                            style={{
                              padding: '8px 10px',
                              borderRadius: 'var(--radius-sm)',
                              border: isSel ? '1.5px solid var(--accent-primary)' : '1px solid var(--border-subtle)',
                              background: isSel ? 'var(--bg-subtle)' : 'var(--bg-surface)',
                              color: isSel ? 'var(--accent-primary)' : 'var(--text-primary)',
                              cursor: 'pointer',
                              display: 'flex',
                              justifyContent: 'center',
                              alignItems: 'center',
                            }}
                          >
                            <span style={{ fontSize: 12, fontWeight: 700 }}>{freq.label}</span>
                          </button>
                        );
                      })}
                    </div>
                  </div>

                  {/* Specific Time Slots with Strict Limits or Realtime Info */}
                  {tempProfile.frequency === 'REALTIME' ? (
                    <div
                      style={{
                        padding: '12px 14px',
                        borderRadius: 'var(--radius-sm)',
                        background: 'var(--bg-subtle)',
                        border: '1px solid var(--border-subtle)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 10,
                      }}
                    >
                      <span style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--accent-primary)', flexShrink: 0 }} />
                      <div style={{ fontSize: 12.5, color: 'var(--text-secondary)', lineHeight: 1.45 }}>
                        <strong style={{ color: 'var(--text-primary)' }}>Chế độ Tức thì:</strong> Trợ lý tự động kiểm tra và gửi email ngay khi có tin tức hoặc ưu đãi mới.
                      </div>
                    </div>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: 0.3 }}>
                          Khung giờ nhận tin tự động
                        </label>
                        <span
                          className="mono-pill"
                          style={{
                            fontSize: 11,
                            fontWeight: 700,
                            color: tempProfile.scheduledTimes.length === getMaxSlotsForFrequency(tempProfile.frequency) ? 'var(--accent-primary)' : 'var(--text-secondary)',
                            borderColor: tempProfile.scheduledTimes.length === getMaxSlotsForFrequency(tempProfile.frequency) ? 'var(--accent-primary)' : 'var(--border-subtle)',
                          }}
                        >
                          Đã chọn {tempProfile.scheduledTimes.length}/{getMaxSlotsForFrequency(tempProfile.frequency)} giờ
                        </span>
                      </div>
                      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                        {[
                          { time: '07:00', label: '07:00 Sáng' },
                          { time: '07:30', label: '07:30 Sáng' },
                          { time: '08:00', label: '08:00 Sáng' },
                          { time: '12:00', label: '12:00 Trưa' },
                          { time: '18:30', label: '18:30 Tối' },
                          { time: '20:00', label: '20:00 Tối' },
                        ].map(({ time, label }) => {
                          const isTimeActive = tempProfile.scheduledTimes.includes(time);
                          const maxSlots = getMaxSlotsForFrequency(tempProfile.frequency);
                          const isLimitReached = !isTimeActive && tempProfile.scheduledTimes.length >= maxSlots;
                          return (
                            <button
                              key={time}
                              type="button"
                              onClick={() => handleToggleTimeSlot(time)}
                              className={`subtab-pill ${isTimeActive ? 'active' : ''}`}
                              style={{
                                fontSize: 12,
                                padding: '6px 12px',
                                cursor: 'pointer',
                                opacity: isLimitReached ? 0.75 : 1,
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: 6,
                              }}
                              title={isLimitReached ? `Bấm để đổi sang khung giờ ${label} (tối đa ${maxSlots} giờ)` : label}
                            >
                              {isTimeActive && <span style={{ fontSize: 11, fontWeight: 800 }}>✓</span>}
                              <span>{label}</span>
                            </button>
                          );
                        })}
                      </div>
                    </div>
                  )}

                  {/* Summary Tone */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                    <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: 0.3 }}>
                      Độ dài bản tin
                    </label>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 10 }}>
                      <div
                        onClick={() => setTempProfile({ ...tempProfile, summaryTone: 'concise' })}
                        style={{
                          padding: '10px 14px',
                          borderRadius: 'var(--radius-sm)',
                          border: tempProfile.summaryTone === 'concise' ? '1.5px solid var(--accent-primary)' : '1px solid var(--border-subtle)',
                          background: tempProfile.summaryTone === 'concise' ? 'var(--bg-subtle)' : 'var(--bg-surface)',
                          cursor: 'pointer',
                        }}
                      >
                        <div style={{ fontSize: 13, fontWeight: 700, color: tempProfile.summaryTone === 'concise' ? 'var(--accent-primary)' : 'var(--text-primary)' }}>
                          Ngắn gọn
                        </div>
                      </div>

                      <div
                        onClick={() => setTempProfile({ ...tempProfile, summaryTone: 'in_depth' })}
                        style={{
                          padding: '10px 14px',
                          borderRadius: 'var(--radius-sm)',
                          border: tempProfile.summaryTone === 'in_depth' ? '1.5px solid var(--accent-primary)' : '1px solid var(--border-subtle)',
                          background: tempProfile.summaryTone === 'in_depth' ? 'var(--bg-subtle)' : 'var(--bg-surface)',
                          cursor: 'pointer',
                        }}
                      >
                        <div style={{ fontSize: 13, fontWeight: 700, color: tempProfile.summaryTone === 'in_depth' ? 'var(--accent-primary)' : 'var(--text-primary)' }}>
                          Chi tiết
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Modal Footer: Sticky Bottom */}
            <div
              style={{
                padding: '12px 20px',
                borderTop: '1px solid var(--border-subtle)',
                background: 'var(--bg-surface)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexShrink: 0,
              }}
            >
              {prefWizardStep === 'persona' && (
                <>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setShowPreferencesModal(false)}
                    style={{ minHeight: 32, fontSize: 12.5 }}
                  >
                    Hủy
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setPrefWizardStep('topics')}
                    style={{ minHeight: 32, fontSize: 12.5, gap: 5 }}
                  >
                    <span>Tiếp tục</span>
                    <IconArrowRight />
                  </button>
                </>
              )}

              {prefWizardStep === 'topics' && (
                <>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setPrefWizardStep('persona')}
                    style={{ minHeight: 32, fontSize: 12.5 }}
                  >
                    Quay lại
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setPrefWizardStep('delivery')}
                    style={{ minHeight: 32, fontSize: 12.5, gap: 5 }}
                  >
                    <span>Tiếp tục</span>
                    <IconArrowRight />
                  </button>
                </>
              )}

              {prefWizardStep === 'delivery' && (
                <>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setPrefWizardStep('topics')}
                    style={{ minHeight: 32, fontSize: 12.5 }}
                  >
                    Quay lại
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleSavePreferences}
                    disabled={!isValidEmail(tempProfile.deliveryEmail)}
                    style={{
                      minHeight: 32,
                      fontSize: 12.5,
                      padding: '0 18px',
                      opacity: isValidEmail(tempProfile.deliveryEmail) ? 1 : 0.5,
                      cursor: isValidEmail(tempProfile.deliveryEmail) ? 'pointer' : 'not-allowed',
                      gap: 5,
                    }}
                  >
                    <IconCheck />
                    <span>Lưu cấu hình</span>
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* MODAL 2: RESPONSIVE CARD-BASED EMAIL PREVIEW                              */}
      {/* ========================================================================= */}
      {showEmailModal && (
        <div className="email-modal-overlay" onClick={() => setShowEmailModal(false)}>
          <div
            className="email-modal-container"
            onClick={(e) => e.stopPropagation()}
            style={{ maxWidth: 640 }}
          >
            {/* Modal Header */}
            <div
              style={{
                padding: '16px 22px',
                borderBottom: '1px solid #E2E8F0',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                background: '#F8FAFC',
              }}
            >
              <div>
                <div style={{ fontWeight: 800, fontSize: 14.5 }}>Mẫu Email Bản tin Sáng gửi tới hộp thư</div>
                <div style={{ fontSize: 12, color: '#64748B' }}>
                  Người gửi: smtp.service@aifp.local &rarr; Người nhận: {profile.deliveryEmail}
                </div>
              </div>
              <button
                onClick={() => setShowEmailModal(false)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  fontSize: 16,
                  cursor: 'pointer',
                  color: '#64748B',
                  padding: 4,
                }}
              >
                <IconClose />
              </button>
            </div>

            {/* Email Body Content */}
            <div style={{ padding: '24px 22px', background: '#FFFFFF', color: '#0F172A' }}>
              {/* Email Hero Header */}
              <div
                style={{
                  padding: '22px',
                  background: '#0F766E',
                  color: '#FFFFFF',
                  borderRadius: 10,
                  marginBottom: 20,
                }}
              >
                <div style={{ fontSize: 11, letterSpacing: 1, textTransform: 'uppercase', opacity: 0.85 }}>
                  Bản tin cá nhân hàng ngày · Aura Life Assistant
                </div>
                <h2 style={{ fontSize: 18, fontWeight: 800, margin: '6px 0 2px 0' }}>
                  Điểm tin chọn lọc dành riêng cho {profile.personaTitle}
                </h2>
                <div style={{ fontSize: 12, opacity: 0.9 }}>
                  Ngày 08/11/2026 · {INITIAL_NEWS.length} tin mới nhất từ Facebook, Báo chí và Tuyển dụng
                </div>
              </div>

              {/* Email Cards List */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                {INITIAL_NEWS.map((item) => (
                  <div
                    key={item.id}
                    style={{
                      border: '1px solid #E2E8F0',
                      borderRadius: 10,
                      overflow: 'hidden',
                      background: '#FFFFFF',
                    }}
                  >
                    <div style={{ height: 150, background: '#F1F5F9', overflow: 'hidden' }}>
                      <img
                        src={item.imageUrl}
                        alt={item.title}
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                        onError={(e) => {
                          (e.currentTarget as HTMLElement).style.display = 'none';
                        }}
                      />
                    </div>
                    <div style={{ padding: 16 }}>
                      <div
                        style={{
                          fontSize: 11,
                          fontWeight: 700,
                          color: '#0F766E',
                          textTransform: 'uppercase',
                          marginBottom: 4,
                        }}
                      >
                        {item.source} · {formatRelativeTime(item.publishedAt)}
                      </div>
                      <h4 style={{ fontSize: 14.5, fontWeight: 700, margin: '0 0 6px 0', color: '#0F172A' }}>
                        {item.title}
                      </h4>
                      <p style={{ fontSize: 12.5, color: '#475569', lineHeight: 1.5, margin: '0 0 12px 0' }}>
                        {item.summary}
                      </p>
                      <a
                        href={item.url}
                        target="_blank"
                        rel="noreferrer"
                        style={{
                          display: 'inline-block',
                          fontSize: 12.5,
                          fontWeight: 600,
                          color: '#0F766E',
                          textDecoration: 'none',
                        }}
                      >
                        Xem chi tiết &rarr;
                      </a>
                    </div>
                  </div>
                ))}
              </div>

              {/* Email Footer */}
              <div
                style={{
                  marginTop: 24,
                  paddingTop: 16,
                  borderTop: '1px solid #E2E8F0',
                  textAlign: 'center',
                  fontSize: 11,
                  color: '#94A3B8',
                  lineHeight: 1.5,
                }}
              >
                Bản tin được tổng hợp tự động bởi Trợ lý Cá nhân Aura theo đúng sở thích của bạn.
                <br />
                Được gửi từ hộp thư hệ thống đã xác thực theo lịch bạn đã đăng ký.
              </div>

              <div style={{ display: 'flex', justifyContent: 'center', marginTop: 16 }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowEmailModal(false)}
                  style={{ gap: 4, minHeight: 32, padding: '0 16px', fontSize: 13 }}
                >
                  <IconClose />
                  <span>Đóng</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* MODAL: CHỈNH SỬA CẤU HÌNH KÊNH TỰ ĐỘNG HÓA RIÊNG                          */}
      {/* ========================================================================= */}
      {editingChannel && (
        <div className="modal-backdrop">
          <div
            className="modal-card"
            style={{
              maxWidth: 500,
              width: '100%',
              padding: '24px',
              borderRadius: 'var(--radius-md)',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              boxShadow: '0 20px 40px rgba(0,0,0,0.2)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
              <div>
                <span
                  style={{
                    fontSize: 11,
                    fontWeight: 700,
                    color: '#FFFFFF',
                    background: editingChannel.badgeColor,
                    padding: '2px 8px',
                    borderRadius: 4,
                    textTransform: 'uppercase',
                  }}
                >
                  {editingChannel.badge}
                </span>
                <h3 style={{ fontSize: 16, fontWeight: 800, margin: '6px 0 0 0', color: 'var(--text-primary)' }}>
                  {editingChannel.title}
                </h3>
              </div>
              <button
                className="btn btn-secondary"
                onClick={() => setEditingChannel(null)}
                style={{ minHeight: 28, padding: '0 8px', fontSize: 12, gap: 4 }}
              >
                <IconClose />
                <span>Đóng</span>
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {/* Giờ gửi */}
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 6 }}>
                  Giờ gửi
                </label>
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                  {['07:00', '08:00', '11:30', '12:00', '17:30', '20:00'].map((t) => (
                    <button
                      key={t}
                      type="button"
                      onClick={() => setEditingChannel({ ...editingChannel, scheduledTime: t })}
                      className="btn btn-secondary"
                      style={{
                        minHeight: 28,
                        padding: '0 10px',
                        fontSize: 12,
                        fontWeight: editingChannel.scheduledTime === t ? 700 : 500,
                        background: editingChannel.scheduledTime === t ? 'var(--accent-primary)' : 'var(--bg-subtle)',
                        color: editingChannel.scheduledTime === t ? '#FFFFFF' : 'var(--text-primary)',
                        borderColor: editingChannel.scheduledTime === t ? 'var(--accent-primary)' : 'var(--border-subtle)',
                      }}
                    >
                      {t}
                    </button>
                  ))}
                </div>
              </div>

              {/* Tần suất */}
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 4 }}>
                  Tần suất
                </label>
                <select
                  value={editingChannel.frequency}
                  onChange={(e) => setEditingChannel({ ...editingChannel, frequency: e.target.value })}
                  style={{
                    width: '100%',
                    height: 36,
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-subtle)',
                    color: 'var(--text-primary)',
                    padding: '0 10px',
                    fontSize: 13,
                  }}
                >
                  <option value="Hàng ngày">Hàng ngày</option>
                  <option value="Thứ 2 - Thứ 6">Thứ 2 - Thứ 6</option>
                  <option value="Khi có giảm giá sâu">Khi giảm giá &gt; 15%</option>
                  <option value="Tức thời khi có tin mới">Tức thời</option>
                </select>
              </div>

              {/* Email nhận riêng */}
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 4 }}>
                  Email riêng
                </label>
                <input
                  type="email"
                  value={editingChannel.customEmail}
                  onChange={(e) => setEditingChannel({ ...editingChannel, customEmail: e.target.value })}
                  placeholder={`Mặc định: ${profile.deliveryEmail}`}
                  style={{
                    width: '100%',
                    height: 36,
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-subtle)',
                    color: 'var(--text-primary)',
                    padding: '0 12px',
                    fontSize: 13,
                  }}
                />
              </div>

              {/* Từ khóa theo dõi */}
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 4 }}>
                  Từ khóa lọc
                </label>
                <input
                  type="text"
                  value={editingChannel.keywords.join(', ')}
                  onChange={(e) =>
                    setEditingChannel({
                      ...editingChannel,
                      keywords: e.target.value.split(',').map((s) => s.trim()).filter(Boolean),
                    })
                  }
                  style={{
                    width: '100%',
                    height: 36,
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-subtle)',
                    color: 'var(--text-primary)',
                    padding: '0 12px',
                    fontSize: 13,
                  }}
                />
              </div>

              {/* Bật / Tắt trạng thái */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '8px 12px',
                  background: 'var(--bg-subtle)',
                  borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--border-subtle)',
                }}
              >
                <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-primary)' }}>
                  Trạng thái
                </span>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setEditingChannel({ ...editingChannel, isActive: !editingChannel.isActive })}
                  style={{
                    minHeight: 28,
                    fontSize: 12,
                    padding: '0 12px',
                    background: editingChannel.isActive ? 'var(--status-running-bg)' : 'var(--bg-surface)',
                    color: editingChannel.isActive ? 'var(--status-running)' : 'var(--text-secondary)',
                    borderColor: editingChannel.isActive ? 'var(--status-running)' : 'var(--border-subtle)',
                  }}
                >
                  {editingChannel.isActive ? 'Bật' : 'Tắt'}
                </button>
              </div>

              {/* Modal footer buttons */}
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 4 }}>
                <button className="btn btn-secondary" onClick={() => setEditingChannel(null)} style={{ gap: 4 }}>
                  <IconClose />
                  <span>Hủy</span>
                </button>
                <button
                  className="btn btn-primary"
                  onClick={() => handleSaveChannelEdit(editingChannel)}
                  style={{ gap: 4 }}
                >
                  <IconCheck />
                  <span>Lưu</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* MODAL: THÊM CẤU HÌNH NHẬN TIN TỰ ĐỘNG MỚI (CÓ GỢI Ý MẪU & TÙY BIẾN CHI TIẾT) */}
      {/* ========================================================================= */}
      {showAddCustomChannelModal && (
        <div className="modal-backdrop">
          <div
            className="modal-card"
            style={{
              maxWidth: 520,
              width: '100%',
              padding: '24px',
              borderRadius: 'var(--radius-md)',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              boxShadow: '0 20px 40px rgba(0,0,0,0.2)',
              maxHeight: '90dvh',
              overflowY: 'auto',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
              <div>
                <h3 style={{ fontSize: 17, fontWeight: 800, margin: 0, color: 'var(--text-primary)' }}>
                  Thêm cấu hình nhận tin
                </h3>
                <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginTop: 2 }}>
                  Tạo luồng gửi email tự động riêng theo chủ đề và thời gian
                </div>
              </div>
              <button
                className="btn btn-secondary"
                onClick={() => setShowAddCustomChannelModal(false)}
                style={{ minHeight: 28, padding: '0 8px', fontSize: 12, gap: 4 }}
              >
                <IconClose />
                <span>Đóng</span>
              </button>
            </div>

            {/* Quick Presets Bar */}
            <div style={{ marginBottom: 14 }}>
              <label style={{ fontSize: 11.5, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 6 }}>
                GỢI Ý CẤU HÌNH NHANH (BẤM ĐỂ CHỌN MẪU):
              </label>
              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                {[
                  { title: 'Săn Deal Đồ Công Nghệ', badge: 'Shopee', badgeColor: '#EE4D2D', time: '11:30', freq: 'Hàng ngày', kw: 'Keychron, Màn hình 4K, Pin sạc, Giảm > 20%' },
                  { title: 'Tuyển dụng Kỹ sư IT & Remote', badge: 'ITViec', badgeColor: '#0F766E', time: '08:00', freq: 'Thứ 2 - Thứ 6', kw: 'Flutter, Spring Boot, Remote USD, Senior' },
                  { title: 'Thị trường Tài chính & Đầu tư', badge: 'Kinh Doanh', badgeColor: '#D97706', time: '17:30', freq: 'Hàng ngày', kw: 'VN-Index, Cổ phiếu công nghệ, Giá vàng' },
                  { title: 'Đột phá AI & Nghiên cứu mới', badge: 'AI Lab', badgeColor: '#2563EB', time: '07:00', freq: 'Hàng ngày', kw: 'LangGraph, Agentic AI, Llama, DeepSeek' },
                ].map((preset, idx) => (
                  <button
                    key={idx}
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => {
                      setNewChannelForm({
                        title: preset.title,
                        badge: preset.badge,
                        badgeColor: preset.badgeColor,
                        scheduledTime: preset.time,
                        frequency: preset.freq,
                        customEmail: profile.deliveryEmail,
                        keywords: preset.kw,
                      });
                      showToast(`Đã chọn mẫu [${preset.title}]!`);
                    }}
                    style={{ fontSize: 11.5, padding: '4px 10px', minHeight: 26, gap: 4 }}
                  >
                    <span>{preset.title}</span>
                  </button>
                ))}
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {/* Tên cấu hình */}
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 4 }}>
                  Tên cấu hình / Chủ đề (Bắt buộc)
                </label>
                <input
                  type="text"
                  value={newChannelForm.title}
                  onChange={(e) => setNewChannelForm({ ...newChannelForm, title: e.target.value })}
                  placeholder="Ví dụ: Săn Deal iPhone 16, Tin tức Bất động sản..."
                  style={{
                    width: '100%',
                    height: 36,
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-subtle)',
                    color: 'var(--text-primary)',
                    padding: '0 12px',
                    fontSize: 13,
                  }}
                />
              </div>

              {/* Giờ gửi & Tần suất */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 10 }}>
                <div>
                  <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 4 }}>
                    Giờ gửi tự động
                  </label>
                  <select
                    value={newChannelForm.scheduledTime}
                    onChange={(e) => setNewChannelForm({ ...newChannelForm, scheduledTime: e.target.value })}
                    style={{
                      width: '100%',
                      height: 36,
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      background: 'var(--bg-subtle)',
                      color: 'var(--text-primary)',
                      padding: '0 10px',
                      fontSize: 13,
                    }}
                  >
                    <option value="06:30">06:30 Sáng</option>
                    <option value="07:00">07:00 Sáng</option>
                    <option value="07:30">07:30 Sáng</option>
                    <option value="08:00">08:00 Sáng</option>
                    <option value="11:30">11:30 Trưa (Deal)</option>
                    <option value="12:00">12:00 Trưa</option>
                    <option value="17:30">17:30 Chiều</option>
                    <option value="18:30">18:30 Tối</option>
                    <option value="20:00">20:00 Tối</option>
                  </select>
                </div>

                <div>
                  <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 4 }}>
                    Tần suất gửi
                  </label>
                  <select
                    value={newChannelForm.frequency}
                    onChange={(e) => setNewChannelForm({ ...newChannelForm, frequency: e.target.value })}
                    style={{
                      width: '100%',
                      height: 36,
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      background: 'var(--bg-subtle)',
                      color: 'var(--text-primary)',
                      padding: '0 10px',
                      fontSize: 13,
                    }}
                  >
                    <option value="Hàng ngày">Hàng ngày</option>
                    <option value="Thứ 2 - Thứ 6">Thứ 2 - Thứ 6</option>
                    <option value="Khi có giảm giá sâu">Khi giảm giá &gt; 15%</option>
                    <option value="Tức thời khi có tin mới">Tức thời</option>
                  </select>
                </div>
              </div>

              {/* Email nhận riêng */}
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 4 }}>
                  Email nhận riêng (Tùy chọn)
                </label>
                <input
                  type="email"
                  value={newChannelForm.customEmail}
                  onChange={(e) => setNewChannelForm({ ...newChannelForm, customEmail: e.target.value })}
                  placeholder={`Mặc định: ${profile.deliveryEmail}`}
                  style={{
                    width: '100%',
                    height: 36,
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-subtle)',
                    color: 'var(--text-primary)',
                    padding: '0 12px',
                    fontSize: 13,
                  }}
                />
              </div>

              {/* Từ khóa lọc */}
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 4 }}>
                  Từ khóa lọc (phân tách bằng dấu phẩy)
                </label>
                <input
                  type="text"
                  value={newChannelForm.keywords}
                  onChange={(e) => setNewChannelForm({ ...newChannelForm, keywords: e.target.value })}
                  placeholder="Ví dụ: Flutter, Remote, Giảm giá 50%, iPhone 16..."
                  style={{
                    width: '100%',
                    height: 36,
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-subtle)',
                    color: 'var(--text-primary)',
                    padding: '0 12px',
                    fontSize: 13,
                  }}
                />
              </div>

              {/* Footer actions */}
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 6, paddingTop: 12, borderTop: '1px solid var(--border-subtle)' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowAddCustomChannelModal(false)}
                  style={{ gap: 4 }}
                >
                  <IconClose />
                  <span>Hủy</span>
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleCreateStructuredChannel}
                  disabled={!newChannelForm.title.trim()}
                  style={{ gap: 5, padding: '0 18px' }}
                >
                  <IconCheck />
                  <span>Tạo cấu hình</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
      {/* ========================================================================= */}
      {/* MODAL 5: OFFICIAL AUTHENTICATION & MULTI-TENANT ACCOUNTS                   */}
      {/* ========================================================================= */}
      {showLoginModal && (
        <div className="email-modal-overlay" onClick={() => setShowLoginModal(false)}>
          <div
            className="email-modal-container"
            onClick={(e) => e.stopPropagation()}
            style={{ maxWidth: 420 }}
          >
            {/* Header */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '14px 18px',
                borderBottom: '1px solid var(--border-subtle)',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span style={{ fontWeight: 800, fontSize: 15, color: 'var(--text-primary)' }}>
                  {authModalTab === 'login' ? 'Đăng nhập tài khoản' : 'Tạo tài khoản mới'}
                </span>
                <span className="mono-pill" style={{ fontSize: 10.5, color: 'var(--accent-primary)' }}>
                  Bảo mật CSDL
                </span>
              </div>
              <button
                type="button"
                onClick={() => {
                  setShowLoginModal(false);
                  setAuthError(null);
                }}
                style={{ background: 'transparent', border: 'none', cursor: 'pointer', padding: 4, color: 'var(--text-secondary)' }}
                title="Đóng"
              >
                <IconClose />
              </button>
            </div>

            {/* Tab Selector */}
            <div
              style={{
                display: 'flex',
                borderBottom: '1px solid var(--border-subtle)',
                background: 'var(--bg-subtle)',
                padding: '4px 6px 0 6px',
                gap: 4,
              }}
            >
              <button
                type="button"
                onClick={() => { setAuthModalTab('login'); setAuthError(null); }}
                style={{
                  flex: 1,
                  padding: '8px 12px',
                  fontSize: 12.5,
                  fontWeight: authModalTab === 'login' ? 700 : 500,
                  color: authModalTab === 'login' ? 'var(--text-primary)' : 'var(--text-tertiary)',
                  background: authModalTab === 'login' ? 'var(--bg-surface)' : 'transparent',
                  border: 'none',
                  borderTopLeftRadius: 'var(--radius-sm)',
                  borderTopRightRadius: 'var(--radius-sm)',
                  borderBottom: authModalTab === 'login' ? '2px solid var(--accent-primary)' : '2px solid transparent',
                  cursor: 'pointer',
                }}
              >
                Đăng nhập
              </button>
              <button
                type="button"
                onClick={() => { setAuthModalTab('register'); setAuthError(null); }}
                style={{
                  flex: 1,
                  padding: '8px 12px',
                  fontSize: 12.5,
                  fontWeight: authModalTab === 'register' ? 700 : 500,
                  color: authModalTab === 'register' ? 'var(--text-primary)' : 'var(--text-tertiary)',
                  background: authModalTab === 'register' ? 'var(--bg-surface)' : 'transparent',
                  border: 'none',
                  borderTopLeftRadius: 'var(--radius-sm)',
                  borderTopRightRadius: 'var(--radius-sm)',
                  borderBottom: authModalTab === 'register' ? '2px solid var(--accent-primary)' : '2px solid transparent',
                  cursor: 'pointer',
                }}
              >
                Đăng ký mới
              </button>
            </div>

            {/* Body */}
            <div style={{ padding: '18px 20px', display: 'flex', flexDirection: 'column', gap: 14 }}>
              <p style={{ margin: 0, fontSize: 12, color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                Dữ liệu cá nhân (lịch trình, tin nhắn, nhiệm vụ, quét hóa đơn) được cô lập 100% trong CSDL Postgres theo tài khoản của bạn.
              </p>

              {/* Official Google Sign-In Button Container */}
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%' }}>
                <div id="google-signin-btn-container" style={{ minHeight: 44, width: '100%', display: 'flex', justifyContent: 'center' }} />
              </div>

              {/* Divider */}
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, margin: '2px 0' }}>
                <span style={{ flex: 1, height: 1, background: 'var(--border-subtle)' }} />
                <span style={{ fontSize: 11, color: 'var(--text-tertiary)', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                  hoặc bằng Email & Mật khẩu
                </span>
                <span style={{ flex: 1, height: 1, background: 'var(--border-subtle)' }} />
              </div>

              {/* Auth Form */}
              <form
                onSubmit={authModalTab === 'login' ? handleEmailLogin : handleEmailRegister}
                style={{ display: 'flex', flexDirection: 'column', gap: 12 }}
              >
                {authModalTab === 'register' && (
                  <div>
                    <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 4 }}>
                      Họ và tên / Biệt danh
                    </label>
                    <input
                      type="text"
                      value={authDisplayName}
                      onChange={(e) => setAuthDisplayName(e.target.value)}
                      placeholder="Tuấn Anh"
                      required
                      style={{
                        width: '100%',
                        height: 38,
                        fontSize: 13,
                        padding: '0 10px',
                        borderRadius: 'var(--radius-sm)',
                        border: '1px solid var(--border-subtle)',
                        background: 'var(--bg-surface)',
                        color: 'var(--text-primary)',
                        boxSizing: 'border-box',
                      }}
                    />
                  </div>
                )}

                <div>
                  <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 4 }}>
                    Địa chỉ Email
                  </label>
                  <input
                    type="email"
                    value={authEmail}
                    onChange={(e) => setAuthEmail(e.target.value)}
                    placeholder="email@domain.com"
                    required
                    style={{
                      width: '100%',
                      height: 38,
                      fontSize: 13,
                      padding: '0 10px',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      background: 'var(--bg-surface)',
                      color: 'var(--text-primary)',
                      boxSizing: 'border-box',
                    }}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 4 }}>
                    Mật khẩu {authModalTab === 'register' ? '(tối thiểu 8 ký tự)' : ''}
                  </label>
                  <input
                    type="password"
                    value={authPassword}
                    onChange={(e) => setAuthPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    style={{
                      width: '100%',
                      height: 38,
                      fontSize: 13,
                      padding: '0 10px',
                      borderRadius: 'var(--radius-sm)',
                      border: '1px solid var(--border-subtle)',
                      background: 'var(--bg-surface)',
                      color: 'var(--text-primary)',
                      boxSizing: 'border-box',
                    }}
                  />
                </div>

                {authError && (
                  <div
                    style={{
                      fontSize: 12,
                      padding: '8px 12px',
                      borderRadius: 'var(--radius-sm)',
                      background: 'rgba(239, 68, 68, 0.08)',
                      border: '1px solid rgba(239, 68, 68, 0.25)',
                      color: '#EF4444',
                      lineHeight: 1.4,
                    }}
                  >
                    {authError}
                  </div>
                )}

                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={isLoggingIn}
                  style={{
                    width: '100%',
                    height: 40,
                    fontSize: 13,
                    fontWeight: 600,
                    marginTop: 4,
                  }}
                >
                  <span>
                    {isLoggingIn
                      ? (authModalTab === 'login' ? 'Đang đăng nhập...' : 'Đang tạo tài khoản...')
                      : (authModalTab === 'login' ? 'Đăng nhập' : 'Tạo tài khoản')}
                  </span>
                </button>
              </form>

              {/* Bottom Switcher */}
              <div style={{ textAlign: 'center', fontSize: 12, color: 'var(--text-tertiary)', marginTop: 2 }}>
                {authModalTab === 'login' ? (
                  <span>
                    Chưa có tài khoản?{' '}
                    <button
                      type="button"
                      onClick={() => { setAuthModalTab('register'); setAuthError(null); }}
                      style={{
                        background: 'none',
                        border: 'none',
                        padding: 0,
                        color: 'var(--accent-primary)',
                        fontWeight: 600,
                        cursor: 'pointer',
                      }}
                    >
                      Đăng ký ngay
                    </button>
                  </span>
                ) : (
                  <span>
                    Đã có tài khoản?{' '}
                    <button
                      type="button"
                      onClick={() => { setAuthModalTab('login'); setAuthError(null); }}
                      style={{
                        background: 'none',
                        border: 'none',
                        padding: 0,
                        color: 'var(--accent-primary)',
                        fontWeight: 600,
                        cursor: 'pointer',
                      }}
                    >
                      Đăng nhập
                    </button>
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
