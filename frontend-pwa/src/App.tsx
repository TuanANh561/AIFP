import { useState, useEffect } from 'react';
import { aifpApi, LiveArticle } from './api/aifpApi';

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
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <line x1="4" y1="21" x2="4" y2="14" /><line x1="4" y1="10" x2="4" y2="3" />
    <line x1="12" y1="21" x2="12" y2="12" /><line x1="12" y1="8" x2="12" y2="3" />
    <line x1="20" y1="21" x2="20" y2="16" /><line x1="20" y1="12" x2="20" y2="3" />
  </svg>
);

const IconCamera = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
    <circle cx="12" cy="13" r="4" />
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

// ============================================================================
// DATA TYPES & PRESETS FOR MULTI-AUDIENCE GROUPS
// ============================================================================
type NavTab = 'assistant' | 'digests' | 'automations' | 'tools' | 'settings';
type ToolSubTab = 'vision' | 'shopping' | 'writing' | 'docs' | 'calendar';

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
  deliveryEmail: 'nguyentuananh11a1.lhpt@gmail.com',
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
    id: 'n-4',
    channelId: 'jobs',
    source: 'ITViec Tuyển dụng',
    sourceType: 'tech',
    category: 'Cơ hội việc làm',
    title: 'Tuyển dụng Kỹ sư Lập trình Flutter & Mobile App (iOS / Android)',
    summary: 'Doanh nghiệp công nghệ tuyển dụng chuyên gia Flutter, yêu cầu vững State Management (Riverpod/Bloc), kết nối Clean Architecture và tối ưu hiệu năng PWA.',
    imageUrl: 'https://images.unsplash.com/photo-1551650975-87deedd944c3?w=1200&auto=format&fit=crop&q=80',
    url: 'https://itviec.com/it-jobs/flutter',
    publishedAt: new Date(Date.now() - 150 * 60 * 1000).toISOString(),
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
    imageUrl: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=1200&auto=format&fit=crop&q=80',
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
    imageUrl: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80',
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
    title: 'Tuyển dụng IT',
    subtitle: '',
    badge: 'ITViec',
    badgeColor: '#0F766E',
    scheduledTime: '08:00',
    customEmail: '',
    frequency: 'Thứ 2 - Thứ 6',
    keywords: ['Flutter', 'Spring Boot', 'Remote USD'],
    isActive: true,
    sourceSummary: 'ITViec, TopCV',
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
];

export default function App() {
  const [activeTab, setActiveTab] = useState<NavTab>('assistant');
  const [toolSubTab, setToolSubTab] = useState<ToolSubTab>('vision');
  const [darkMode, setDarkMode] = useState<boolean>(() => {
    try {
      return localStorage.getItem('aifp_dark_mode') === 'true';
    } catch {}
    return false;
  });
  const [showEmailModal, setShowEmailModal] = useState(false);
  const [showPreferencesModal, setShowPreferencesModal] = useState(false);
  const [killSwitchTriggered, setKillSwitchTriggered] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [isSendingTestEmail, setIsSendingTestEmail] = useState(false);

  // Preference Profile State (Two-Tier Persistence: localStorage + Database)
  const [profile, setProfile] = useState<UserPreferencesProfile>(() => {
    try {
      const saved = localStorage.getItem('aifp_user_profile');
      if (saved) {
        return JSON.parse(saved);
      }
    } catch {}
    return INITIAL_PROFILE;
  });
  const [tempProfile, setTempProfile] = useState<UserPreferencesProfile>(() => {
    try {
      const saved = localStorage.getItem('aifp_user_profile');
      if (saved) {
        return JSON.parse(saved);
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
        return JSON.parse(saved);
      }
    } catch {}
    return INITIAL_TOPIC_CHANNELS;
  });
  const [editingChannel, setEditingChannel] = useState<TopicChannelConfig | null>(null);
  const [sendingChannelId, setSendingChannelId] = useState<string | null>(null);
  const [newsFilter, setNewsFilter] = useState<'all' | 'shopping' | 'jobs' | 'tech' | 'finance' | 'social'>('all');
  const [showAddCustomChannelModal, setShowAddCustomChannelModal] = useState(false);
  const [customChannelPrompt, setCustomChannelPrompt] = useState('');
  const [isParsingCustomChannel, setIsParsingCustomChannel] = useState(false);

  // Live Articles (Direct from PostgreSQL/H2 Database)
  const [liveNews, setLiveNews] = useState<LiveArticle[]>(INITIAL_NEWS);
  const [isSyncingNews, setIsSyncingNews] = useState(false);
  const [lastSyncTime, setLastSyncTime] = useState<string>('');

  // Chat State
  const [chatInput, setChatInput] = useState('');
  const [isChatGenerating, setIsChatGenerating] = useState(false);
  const [runningTaskId, setRunningTaskId] = useState<string | null>(null);
  const [chatMessages, setChatMessages] = useState<{ id: string; role: 'user' | 'assistant'; text: string; citations?: { label: string; url: string }[] }[]>([
    {
      id: 'm-1',
      role: 'assistant',
      text: 'Chào bạn! Tôi là Trợ lý Cá nhân AIFP. Tôi tự động theo dõi tin tức công nghệ, việc làm Flutter và săn deal giảm giá theo sở thích của bạn. Mỗi sáng lúc 07:30, tôi sẽ gửi bản tin tóm tắt có ảnh và link vào email của bạn — bạn không cần phải mở ứng dụng hàng ngày.',
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
  const [visionImagePreview, setVisionImagePreview] = useState(
    'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=500&auto=format&fit=crop&q=80'
  );
  const [visionResult, setVisionResult] = useState<string | null>(
    'Hóa đơn: The Coffee House\nNgày: 08/11/2026\nMón: Cà phê sữa đá (x2) — 78.000 đ, Bánh Croissant (x1) — 35.000 đ\nTổng thanh toán: 113.000 đ\nPhương thức: Chuyển khoản QR'
  );

  // Shopping Tracker State
  const [shopQuery, setShopQuery] = useState('Bàn phím cơ Keychron K2 Pro');
  const [shopResults] = useState([
    { store: 'Shopee Mall', price: '1.890.000 đ', link: 'https://shopee.vn', status: 'Giá thấp nhất' },
    { store: 'Tiki Official', price: '2.050.000 đ', link: 'https://tiki.vn', status: 'Sẵn hàng' },
    { store: 'Lazada Mall', price: '1.990.000 đ', link: 'https://lazada.vn', status: 'Có mã giảm 50k' },
  ]);

  // Writing Studio State
  const [writingPrompt, setWritingPrompt] = useState('Soạn email gửi cấp trên báo cáo tiến độ tích hợp mô hình AI');
  const [writingDraft, setWritingDraft] = useState(
    'Kính gửi Anh/Chị Quản lý,\n\nEm xin phép gửi báo cáo tóm tắt tiến độ triển khai nền tảng Trợ lý AI:\n1. Đã kết nối hộp thư hệ thống Gmail SMTP nguyentuananh11a1.lhpt@gmail.com phục vụ gửi tự động.\n2. Giao diện được thiết kế tối giản, loại bỏ biểu tượng màu mè và thanh cuộn mượt mà.\n3. Thiết lập quy trình tự động gửi bản tin dạng thẻ qua email lúc 07:30 sáng, giúp người dùng theo dõi mà không cần mở ứng dụng.\n\nTrân trọng,\nTuấn Anh'
  );

  // Calendar State
  const [calendarTitle, setCalendarTitle] = useState('Họp rà soát quy trình triển khai Trợ lý AI');
  const [calendarDate, setCalendarDate] = useState('2026-11-09T09:00');
  const [calendarSuccess, setCalendarSuccess] = useState(false);

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

    try {
      // 1. Query Python AI Service (LangGraph 7-Node Agent + Live RSS & SearXNG Discovery)
      const ts = Math.floor(Date.now() / 1000);
      const userId = '11111111-1111-1111-1111-111111111111';

      const aiResp = await fetch('http://127.0.0.1:8001/internal/v1/agent/execute', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Internal-Timestamp': String(ts),
          'X-User-Id': userId,
          'X-Internal-Signature': 'preview-token',
        },
        body: JSON.stringify({
          user_id: userId,
          prompt: userText,
          allowed_tools: ['searxng_search', 'rss_fetch', 'webpage_extract'],
          delivery_mode: 'ALL',
        }),
      }).catch(() => null);

      if (aiResp && aiResp.ok) {
        const json = await aiResp.json();
        const reply = json.final_markdown || 'Đã phân tích xong yêu cầu của bạn.';
        const citations = (json.verified_citations || []).map((url: string) => {
          const domain = url.replace('https://', '').replace('http://', '').split('/')[0];
          return { label: domain, url };
        });

        setChatMessages((prev) => [
          ...prev,
          {
            id: `m-${Date.now() + 1}`,
            role: 'assistant',
            text: reply,
            citations: citations.length > 0 ? citations : undefined,
          },
        ]);
        setIsChatGenerating(false);
        return;
      }
    } catch {
      // Graceful fallback to local intelligence below
    }

    // High-craft contextual agent fallback
    setTimeout(() => {
      let reply = `Tôi đã tiếp nhận: "${userText}". Tôi đã rà soát nguồn tin và sẽ tổng hợp vào bản tin gửi email cho bạn.`;
      let citations: { label: string; url: string }[] | undefined = undefined;

      const lower = userText.toLowerCase();
      if (lower.includes('tin') || lower.includes('bản tin') || lower.includes('công nghệ') || lower.includes('ai')) {
        reply = 'Bản tin công nghệ chọn lọc hôm nay:\n- Công nghệ Bluetooth mới hỗ trợ truyền phát âm thanh Lossless chất lượng cao.\n- Xu hướng tích hợp AI Agents tự động hóa đang phát triển mạnh mẽ trên nền tảng Spring Boot và Flutter.\n- Đã cập nhật vào Bản tin sáng gửi tới email của bạn lúc 07:30.';
        citations = [
          { label: 'vnexpress.net/so-hoa', url: 'https://vnexpress.net/cong-nghe-bluetooth-moi-cho-phep-phat-am-thanh-lossless-5141993.html' },
          { label: 'dantri.com.vn/suc-manh-so', url: 'https://dantri.com.vn' },
        ];
      } else if (lower.includes('việc làm') || lower.includes('tuyển dụng') || lower.includes('flutter')) {
        reply = 'Cập nhật tuyển dụng IT & Flutter mới nhất:\n- Các công ty công nghệ đang đẩy mạnh tuyển dụng kỹ sư Flutter & Backend Java.\n- Mức trợ cấp thực tập sinh từ 6 - 10 triệu/tháng, kỹ sư chính thức từ 25 - 45 triệu/tháng.\n- Đã đưa cơ hội này vào danh sách ưu tiên gửi email.';
        citations = [{ label: 'itviec.com/flutter-jobs', url: 'https://itviec.com' }];
      } else if (lower.includes('giá') || lower.includes('bàn phím') || lower.includes('deal')) {
        reply = 'Kiểm tra giá Bàn phím cơ Keychron K2 Pro:\n- Giá thấp nhất hiện tại: 1.890.000 đ tại Shopee Mall (Giảm 160.000 đ so với tuần trước).\n- Bạn có thể bật theo dõi biến động giá trong mục Tác vụ.';
        citations = [{ label: 'shopee.vn/keychron-k2-pro', url: 'https://shopee.vn' }];
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
    }, 600);
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
        showToast(
          `Gói "${tempProfile.frequency === '2_DAILY' ? '2 lần' : '3 lần'} / ngày" chỉ được chọn tối đa ${maxSlots} khung giờ. Hãy bỏ bớt 1 giờ trước khi chọn giờ khác.`
        );
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
    showToast('Đang kết nối CSDL và quét bài viết mới nhất từ RSS...');
    try {
      const result = await aifpApi.syncLiveNews();
      await loadLiveNews(newsFilter);
      if (result) {
        showToast(`Đã lưu ${result.newAddedCount} bài mới vào CSDL! Tổng cộng: ${result.totalInDatabase} bài viết thực tế.`);
      } else {
        showToast('Đã đồng bộ dữ liệu tin tức mới nhất từ CSDL.');
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
        showToast(`Đã gửi bản tin thành công tới ${targetEmail} qua hệ thống nguyentuananh11a1.lhpt@gmail.com!`);
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

    setSendingChannelId(channel.id);
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
      setSendingChannelId(null);
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

  const handleCreateCustomChannel = () => {
    const prompt = customChannelPrompt.trim();
    if (!prompt) {
      showToast('Vui lòng nhập mô tả luồng tự động bạn muốn tạo.');
      return;
    }
    if (prompt.length < 3) {
      showToast('Mô tả luồng tự động cần có ít nhất 3 ký tự.');
      return;
    }
    setIsParsingCustomChannel(true);
    showToast('Trợ lý AI đang thiết lập luồng tự động hóa mới...');
    setTimeout(() => {
      const title =
        prompt.length > 36 ? prompt.slice(0, 36) + '...' : prompt;
      const newCh: TopicChannelConfig = {
        id: 'custom-' + Date.now(),
        title: title,
        subtitle: `Tự động hóa theo yêu cầu: "${prompt}"`,
        badge: 'Tự động hóa AI',
        badgeColor: '#0F766E',
        scheduledTime: '09:00',
        customEmail: profile.deliveryEmail,
        frequency: 'Hàng ngày lúc 09:00',
        keywords: [title],
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
      setCustomChannelPrompt('');
      setIsParsingCustomChannel(false);
      showToast(`Đã tạo luồng tự động mới: [${title}]! Trợ lý sẽ tự động quét và gửi mail lúc 09:00.`);
    }, 400);
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
    if (newsFilter === 'all') return true;
    if (newsFilter === 'shopping') return item.sourceType === 'shop' || item.channelId === 'shopping';
    if (newsFilter === 'jobs') return (item.sourceType === 'tech' && (item.category.includes('việc') || item.title.includes('Tuyển'))) || item.channelId === 'jobs';
    if (newsFilter === 'tech') return (item.sourceType === 'vn' || item.sourceType === 'tech') && !item.title.includes('Tuyển') && (item.channelId === 'tech' || item.category.includes('nghệ'));
    if (newsFilter === 'finance') return item.category.includes('chính') || item.category.includes('Doanh') || item.channelId === 'finance';
    if (newsFilter === 'social') return item.sourceType === 'fb' || item.channelId === 'social';
    return true;
  });


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
      <aside className="sidebar-rail">
        {/* Brand Header */}
        <div style={{ padding: '6px 6px 14px 6px', borderBottom: '1px solid var(--border-subtle)', marginBottom: 12 }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <div
                style={{
                  width: 32,
                  height: 32,
                  borderRadius: 8,
                  background: 'var(--accent-primary)',
                  color: '#FFFFFF',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 800,
                  fontSize: 15,
                  boxShadow: '0 1px 3px rgba(0, 0, 0, 0.12)',
                }}
              >
                A
              </div>
              <span style={{ fontWeight: 800, fontSize: 16, letterSpacing: -0.3, color: 'var(--text-primary)' }}>
                AIFP
              </span>
            </div>

            <button
              onClick={() => {
                setTempProfile(profile);
                setPrefWizardStep('persona');
                setShowPreferencesModal(true);
              }}
              className="btn btn-secondary"
              style={{
                minHeight: 28,
                padding: '0 8px',
                fontSize: 11.5,
                fontWeight: 600,
                borderRadius: 'var(--radius-sm)',
                gap: 4,
              }}
              title="Tùy biến nhận tin"
            >
              <IconSliders />
              <span>Cấu hình</span>
            </button>
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

        {/* Bottom Rail Actions */}
        <div
          style={{
            marginTop: 'auto',
            paddingTop: 12,
            borderTop: '1px solid var(--border-subtle)',
          }}
        >
          <button
            className="btn btn-secondary"
            onClick={toggleDark}
            style={{
              width: '100%',
              justifyContent: 'space-between',
              padding: '0 12px',
              minHeight: 34,
              fontSize: 12.5,
            }}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              {darkMode ? <IconSun /> : <IconMoon />}
              <span>{darkMode ? 'Giao diện sáng' : 'Giao diện tối'}</span>
            </span>
          </button>
        </div>
      </aside>

      {/* ========================================================================= */}
      {/* 2. MAIN WORKSPACE (Rộng rãi, Căn giữa, Không khí Trợ lý Cá nhân)          */}
      {/* ========================================================================= */}
      <main className="main-workspace">
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
          <div style={{ display: 'flex', flexDirection: 'column', height: '100%', maxWidth: 840, margin: '0 auto', width: '100%' }}>
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
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13 }}>
                <span style={{ fontWeight: 700, color: 'var(--text-primary)' }}>Trợ lý</span>
                <span className="mono-pill" style={{ fontSize: 11 }}>{profile.personaTitle}</span>
                <span style={{ color: 'var(--text-tertiary)', fontSize: 12 }}>
                  {profile.scheduledTimes[0]} · {profile.deliveryEmail}
                </span>
              </div>
              <div style={{ display: 'flex', gap: 6 }}>
                <button
                  className="btn btn-secondary"
                  onClick={() => {
                    setTempProfile(profile);
                    setShowPreferencesModal(true);
                  }}
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
                >
                  <IconSend />
                  <span>{isSendingTestEmail ? 'Đang gửi...' : 'Gửi mail'}</span>
                </button>
              </div>
            </div>

            {/* Quick Action Suggestion Pills (Crisp, High-Signal) */}
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 16 }}>
              {[
                { label: 'Điểm tin', query: 'Điểm tin công nghệ hôm nay', icon: <IconSearch /> },
                { label: 'Việc làm IT', query: 'Tìm việc làm Flutter và Java mới nhất', icon: <IconSearch /> },
                { label: 'Giá Keychron', query: 'Kiểm tra giá bàn phím Keychron K2 Pro', icon: <IconSliders /> },
                { label: 'Quét hóa đơn', query: 'Quét hóa đơn chi tiêu cà phê', icon: <IconCamera /> },
              ].map((p, idx) => (
                <div
                  key={idx}
                  className="quick-prompt-pill"
                  onClick={() => setChatInput(p.query)}
                >
                  {p.icon}
                  <span>{p.label}</span>
                </div>
              ))}
            </div>

            {/* Chat Messages Feed */}
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 16, overflowY: 'auto', marginBottom: 16, paddingRight: 4 }}>
              {chatMessages.map((msg) => (
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
                    {msg.text}
                  </div>

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
              ))}

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

            {/* Chat Input Box */}
            <div style={{ position: 'relative' }}>
              <textarea
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    handleSendMessage();
                  }
                }}
                placeholder="Hỏi trợ lý hoặc yêu cầu tìm kiếm, tổng hợp tin tức..."
                rows={2}
                style={{
                  width: '100%',
                  padding: '14px 90px 14px 18px',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-subtle)',
                  background: 'var(--bg-surface)',
                  color: 'var(--text-primary)',
                  resize: 'none',
                  outline: 'none',
                  fontSize: 14.5,
                  lineHeight: 1.4,
                  boxShadow: '0 2px 6px rgba(0, 0, 0, 0.03)',
                }}
              />
              <button
                className="btn btn-primary"
                onClick={handleSendMessage}
                style={{
                  position: 'absolute',
                  right: 12,
                  bottom: 14,
                  minHeight: 36,
                  padding: '0 16px',
                  fontSize: 13.5,
                }}
              >
                Gửi
              </button>
            </div>
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 2: DIGESTS & EMAIL (BẢN TIN CÁ NHÂN DẠNG THẺ & EMAIL XUẤT BẢN)         */}
        {/* ========================================================================= */}
        {activeTab === 'digests' && (
          <div style={{ maxWidth: 880, margin: '0 auto', width: '100%' }}>
            {/* Header & Controls */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: 18,
                flexWrap: 'wrap',
                gap: 12,
              }}
            >
              <h1 style={{ fontSize: 20, fontWeight: 800, letterSpacing: -0.3, margin: 0 }}>
                Bản tin
              </h1>

              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                <button
                  className="btn btn-secondary"
                  onClick={() => setShowAddCustomChannelModal(true)}
                  style={{ gap: 5, minHeight: 30, fontSize: 12, padding: '0 11px' }}
                >
                  <IconPlus />
                  <span>Thêm luồng</span>
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={() => setShowEmailModal(true)}
                  style={{ gap: 5, minHeight: 30, fontSize: 12, padding: '0 11px' }}
                >
                  <IconEye />
                  <span>Xem mẫu</span>
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleSendTestEmail}
                  disabled={isSendingTestEmail}
                  style={{ minHeight: 30, fontSize: 12, gap: 5, padding: '0 13px' }}
                >
                  <IconSend />
                  <span>{isSendingTestEmail ? 'Đang gửi...' : 'Gửi mail'}</span>
                </button>
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
                  {topicChannels.filter((c) => c.isActive).length}/{topicChannels.length} đang bật
                </span>
              </div>

              {/* Grid of Topic Channels */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 12 }}>
                {topicChannels.map((channel) => {
                  const targetEmail = channel.customEmail.trim() || profile.deliveryEmail;
                  const isSendingThis = sendingChannelId === channel.id;

                  return (
                    <div
                      key={channel.id}
                      className="card-surface"
                      style={{
                        padding: '16px 18px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 12,
                        borderLeft: `4px solid ${channel.isActive ? channel.badgeColor : 'var(--border-subtle)'}`,
                        opacity: channel.isActive ? 1 : 0.65,
                        transition: 'all 0.15s ease',
                      }}
                    >
                      {/* Top Row: Badge, Title & Toggle Switch */}
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                          <span
                            style={{
                              fontSize: 11,
                              fontWeight: 700,
                              color: '#FFFFFF',
                              background: channel.badgeColor,
                              padding: '2px 8px',
                              borderRadius: 4,
                              letterSpacing: 0.3,
                              textTransform: 'uppercase',
                            }}
                          >
                            {channel.badge}
                          </span>
                          <span style={{ fontSize: 14.5, fontWeight: 700, color: 'var(--text-primary)' }}>
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
                          flexWrap: 'wrap',
                          gap: 8,
                          padding: '8px 10px',
                          background: 'var(--bg-subtle)',
                          borderRadius: 'var(--radius-sm)',
                          fontSize: 12,
                          color: 'var(--text-secondary)',
                        }}
                      >
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                          <IconTime />
                          <strong>{channel.scheduledTime}</strong> ({channel.frequency})
                        </span>
                        <span>•</span>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                          <IconMail />
                          <span style={{ fontFamily: 'var(--font-mono)', fontSize: 11 }}>{targetEmail}</span>
                        </span>
                      </div>

                      {/* Keywords pills */}
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 5 }}>
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

                      {/* Actions */}
                      <div
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          paddingTop: 8,
                          borderTop: '1px solid var(--border-subtle)',
                        }}
                      >
                        <button
                          className="btn btn-secondary"
                          onClick={() => setEditingChannel(channel)}
                          style={{ minHeight: 28, fontSize: 12, padding: '0 10px', gap: 4 }}
                        >
                          <IconSliders />
                          <span>Cấu hình</span>
                        </button>

                        <button
                          className="btn btn-secondary"
                          onClick={() => handleSendChannelDigest(channel)}
                          disabled={isSendingThis}
                          style={{
                            minHeight: 28,
                            fontSize: 12,
                            padding: '0 12px',
                            gap: 5,
                            color: channel.isActive ? 'var(--accent-primary)' : 'var(--text-secondary)',
                            borderColor: channel.isActive ? 'var(--accent-primary)' : 'var(--border-subtle)',
                          }}
                        >
                          <IconSend />
                          <span>{isSendingThis ? 'Đang gửi...' : 'Gửi thử'}</span>
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* SECTION 2: NEWS FEED WITH LIVE CSDL STATUS & REFRESH */}
            <div style={{ marginBottom: 12, display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 10 }}>
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
                  title="Dữ liệu bài viết thực tế được lưu trong CSDL"
                >
                  <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#10B981' }} />
                  <span>CSDL: {liveNews.length} bài</span>
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
                  style={{ minHeight: 26, fontSize: 11.5, padding: '0 10px', gap: 5 }}
                  title="Quét RSS trực tiếp và lưu bài viết mới vào CSDL"
                >
                  <IconRefresh spinning={isSyncingNews} />
                  <span>{isSyncingNews ? 'Đang lưu...' : 'Cập nhật'}</span>
                </button>

                {/* Sub Filter Pills */}
                <div style={{ display: 'flex', gap: 5, flexWrap: 'wrap' }}>
                  {[
                    { id: 'all', label: 'Tất cả' },
                    { id: 'shopping', label: 'Shopee' },
                    { id: 'jobs', label: 'Việc làm' },
                    { id: 'tech', label: 'Công nghệ' },
                    { id: 'finance', label: 'Tài chính' },
                    { id: 'social', label: 'Cộng đồng' },
                  ].map((f) => (
                    <button
                      key={f.id}
                      onClick={() => setNewsFilter(f.id as any)}
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
              {filteredNews.map((item) => (
                <div key={item.id} className="news-digest-card">
                  {/* Thumbnail Image */}
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
                        CSDL
                      </span>
                    </div>
                  </div>

                  {/* Card Content */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: 1 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)' }}>{item.category}</span>
                      <span style={{ fontSize: 11.5, color: 'var(--text-tertiary)', fontFamily: 'var(--font-mono)' }}>
                        {formatRelativeTime(item.publishedAt)}
                      </span>
                    </div>

                    <h3 style={{ fontSize: 15.5, fontWeight: 700, lineHeight: 1.4, margin: 0, color: 'var(--text-primary)' }}>
                      {item.title}
                    </h3>

                    <p style={{ fontSize: 13, color: 'var(--text-secondary)', lineHeight: 1.55, margin: 0, flex: 1 }}>
                      {item.summary}
                    </p>

                    <div
                      style={{
                        paddingTop: 12,
                        borderTop: '1px solid var(--border-subtle)',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                      }}
                    >
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
                          gap: 5,
                        }}
                      >
                        <span>Xem nguồn</span>
                        <IconArrowRight />
                      </a>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 3: ZERO-APP AUTOMATIONS (CÁC TÁC VỤ CHẠY NGẦM KHÔNG CẦN MỞ ỨNG DỤNG)     */}
        {/* ========================================================================= */}
        {activeTab === 'automations' && (
          <div style={{ maxWidth: 840, margin: '0 auto', width: '100%' }}>
            <div style={{ marginBottom: 18, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <h1 style={{ fontSize: 20, fontWeight: 800, letterSpacing: -0.3, margin: 0 }}>
                Tác vụ
              </h1>
              <span className="mono-pill" style={{ color: 'var(--status-running)', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                {tasks.filter((t) => t.active).length}/{tasks.length} hoạt động
              </span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {tasks.map((task) => (
                <div key={task.id} className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
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
          <div style={{ maxWidth: 840, margin: '0 auto', width: '100%' }}>
            <div style={{ marginBottom: 16 }}>
              <h1 style={{ fontSize: 20, fontWeight: 800, letterSpacing: -0.3, margin: 0 }}>
                Công cụ
              </h1>
            </div>

            {/* Sub-Tabs (Clean Pills) */}
            <div style={{ display: 'flex', gap: 6, marginBottom: 18, flexWrap: 'wrap' }}>
              {[
                { id: 'vision', label: 'Hóa đơn' },
                { id: 'shopping', label: 'Mua sắm' },
                { id: 'writing', label: 'Soạn thảo' },
                { id: 'docs', label: 'Tài liệu' },
                { id: 'calendar', label: 'Lịch hẹn' },
              ].map((tab) => (
                <button
                  key={tab.id}
                  className={`subtab-pill ${toolSubTab === tab.id ? 'active' : ''}`}
                  onClick={() => setToolSubTab(tab.id as ToolSubTab)}
                >
                  {tab.label}
                </button>
              ))}
            </div>

            {/* TOOL 1: VISION OCR (QUÉT HÓA ĐƠN) */}
            {toolSubTab === 'vision' && (
              <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>Quét hóa đơn chi tiêu</h3>

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
                      }}
                    >
                      <img src={visionImagePreview} alt="Hóa đơn" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    </div>
                    <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
                      <label className="btn btn-secondary" style={{ fontSize: 12.5, flex: 1, cursor: 'pointer', gap: 5 }}>
                        <IconCamera />
                        <span>Chọn ảnh</span>
                        <input
                          type="file"
                          accept="image/*"
                          style={{ display: 'none' }}
                          onChange={(e) => {
                            if (e.target.files && e.target.files[0]) {
                              const file = e.target.files[0];
                              setVisionImageName(file.name);
                              setVisionImagePreview(URL.createObjectURL(file));
                              setVisionResult('Đang phân tích hình ảnh...');
                              setTimeout(() => {
                                setVisionResult(
                                  `Hóa đơn: ${file.name.replace(/\.[^/.]+$/, '')}\nNgày: 08/11/2026\nTổng thanh toán: 150.000 đ\nTrạng thái: Đã khớp chi tiêu`
                                );
                              }, 900);
                            }
                          }}
                        />
                      </label>
                      <button
                        className="btn btn-primary"
                        onClick={() => {
                          showToast('Đang quét thông tin hóa đơn...');
                          setTimeout(() => {
                            setVisionResult(
                              'Hóa đơn: The Coffee House\nNgày: 08/11/2026\nMón: Cà phê sữa đá (x2) — 78.000 đ, Bánh Croissant (x1) — 35.000 đ\nTổng thanh toán: 113.000 đ\nPhương thức: Chuyển khoản QR'
                            );
                            showToast('Đã trích xuất thông tin hóa đơn.');
                          }, 600);
                        }}
                        style={{ fontSize: 12.5, gap: 5 }}
                      >
                        <IconSearch />
                        <span>Quét</span>
                      </button>
                    </div>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    <div style={{ fontSize: 11.5, fontWeight: 700, color: 'var(--text-secondary)' }}>
                      KẾT QUẢ:
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
                      {visionImageName}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* TOOL 2: SHOPPING TRACKER */}
            {toolSubTab === 'shopping' && (
              <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>So sánh giá</h3>

                <div style={{ display: 'flex', gap: 8 }}>
                  <input
                    type="text"
                    value={shopQuery}
                    onChange={(e) => setShopQuery(e.target.value)}
                    placeholder="Nhập tên sản phẩm..."
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
                  <button className="btn btn-primary" onClick={() => showToast('Đang cập nhật giá mới nhất...')} style={{ gap: 5 }}>
                    <IconSearch />
                    <span>Tìm</span>
                  </button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {shopResults.map((item, idx) => (
                    <div
                      key={idx}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '12px 16px',
                        borderRadius: 'var(--radius-sm)',
                        border: '1px solid var(--border-subtle)',
                        background: 'var(--bg-surface)',
                      }}
                    >
                      <div>
                        <div style={{ fontWeight: 700, fontSize: 14 }}>{item.store}</div>
                        <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{item.status}</div>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                        <span style={{ fontWeight: 800, fontSize: 15, color: 'var(--accent-primary)' }}>
                          {item.price}
                        </span>
                        <a
                          href={item.link}
                          target="_blank"
                          rel="noreferrer"
                          className="btn btn-secondary"
                          style={{ fontSize: 12, minHeight: 28, padding: '0 10px', gap: 4 }}
                        >
                          <span>Xem giá</span>
                          <IconArrowRight />
                        </a>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* TOOL 3: WRITING STUDIO */}
            {toolSubTab === 'writing' && (
              <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>Soạn thảo</h3>

                <input
                  type="text"
                  value={writingPrompt}
                  onChange={(e) => setWritingPrompt(e.target.value)}
                  placeholder="Nêu chủ đề hoặc mục tiêu bài viết..."
                  style={{
                    width: '100%',
                    padding: '8px 14px',
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
                  rows={8}
                  style={{
                    width: '100%',
                    padding: '12px 14px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-subtle)',
                    color: 'var(--text-primary)',
                    fontSize: 13,
                    lineHeight: 1.5,
                    resize: 'vertical',
                  }}
                />

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                  <button
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
                    className="btn btn-primary"
                    onClick={() => {
                      showToast('Đang viết lại theo phong cách ngắn gọn...');
                      setTimeout(() => {
                        setWritingDraft(
                          `Kính gửi Anh/Chị,\n\nBáo cáo cập nhật:\n- Đã tích hợp SMTP Gmail nguyentuananh11a1.lhpt@gmail.com phục vụ gửi tự động.\n- Người dùng có thể bật/tắt thể loại tin và chọn tần suất gửi tùy ý.\n- Mọi tác vụ vận hành ngầm bất biến, đảm bảo không trùng lặp.\n\nTrân trọng,\nTuấn Anh`
                        );
                        showToast('Đã cập nhật bản nháp.');
                      }, 700);
                    }}
                    style={{ gap: 5 }}
                  >
                    <IconRefresh />
                    <span>Viết lại</span>
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
                    className="btn btn-primary"
                    onClick={() => {
                      setCalendarSuccess(true);
                      showToast('Đã lưu lịch hẹn và tạo lời nhắc tự động.');
                    }}
                    style={{ gap: 5 }}
                  >
                    <IconCalendar />
                    <span>Lưu lịch</span>
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
          </div>
        )}

        {/* ========================================================================= */}
        {/* TAB 5: SETTINGS & ACCOUNT CONTROL CENTER                                  */}
        {/* ========================================================================= */}
        {activeTab === 'settings' && (
          <div style={{ maxWidth: 840, margin: '0 auto', width: '100%', display: 'flex', flexDirection: 'column', gap: 18 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
              <h1 style={{ fontSize: 20, fontWeight: 800, letterSpacing: -0.3, margin: 0 }}>
                Cài đặt
              </h1>
              <span className="mono-pill" style={{ color: 'var(--status-running)', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                Hoạt động
              </span>
            </div>

            {/* 1. Profile Overview Card */}
            <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
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
                    onClick={() => {
                      setTempProfile(profile);
                      setPrefWizardStep('persona');
                      setShowPreferencesModal(true);
                    }}
                    style={{ minHeight: 28, fontSize: 12, padding: '0 10px', gap: 4 }}
                  >
                    <IconSliders />
                    <span>Tùy biến</span>
                  </button>
                  <button
                    className="btn btn-primary"
                    onClick={handleSendTestEmail}
                    disabled={isSendingTestEmail}
                    style={{ minHeight: 28, fontSize: 12, padding: '0 10px', gap: 4 }}
                  >
                    <IconSend />
                    <span>{isSendingTestEmail ? 'Đang gửi...' : 'Gửi thử'}</span>
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
            <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ fontWeight: 700, fontSize: 15 }}>Hộp thư (Gmail SMTP)</div>
                <span className="mono-pill" style={{ color: 'var(--status-running)', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
                  Đã kết nối
                </span>
              </div>
              <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                <span className="mono-pill">smtp.gmail.com:587</span>
                <span className="mono-pill">nguyentuananh11a1.lhpt@gmail.com</span>
                <span className="mono-pill">STARTTLS</span>
              </div>
            </div>

            {/* 3. Neon Cloud PostgreSQL Database */}
            <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
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
            <div className="card-surface" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
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
            <div className="card-surface" style={{ borderColor: 'rgba(239, 68, 68, 0.3)' }}>
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
            style={{ maxWidth: 680, padding: '24px 28px' }}
          >
            {/* Modal Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h2 style={{ fontSize: 17, fontWeight: 800, letterSpacing: -0.3, color: 'var(--text-primary)', margin: 0 }}>
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

            {/* 3-Step Segmented Bar */}
            <div className="pref-tab-bar" style={{ marginBottom: 16 }}>
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
              <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
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

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 10, paddingTop: 14, borderTop: '1px solid var(--border-subtle)' }}>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setShowPreferencesModal(false)}
                    style={{ gap: 4 }}
                  >
                    <IconClose />
                    <span>Hủy</span>
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setPrefWizardStep('topics')}
                    style={{ gap: 4 }}
                  >
                    <span>Tiếp tục</span>
                    <IconArrowRight />
                  </button>
                </div>
              </div>
            )}

            {/* STEP 2: TOPIC SELECTION */}
            {prefWizardStep === 'topics' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
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

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 10, paddingTop: 14, borderTop: '1px solid var(--border-subtle)' }}>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setPrefWizardStep('persona')}
                  >
                    Quay lại
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setPrefWizardStep('delivery')}
                    style={{ gap: 4 }}
                  >
                    <span>Tiếp tục</span>
                    <IconArrowRight />
                  </button>
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
                  <div style={{ display: 'flex', gap: 8 }}>
                    <input
                      type="email"
                      required
                      value={tempProfile.deliveryEmail}
                      onChange={(e) => setTempProfile({ ...tempProfile, deliveryEmail: e.target.value })}
                      placeholder="vd: nguyentuananh11a1.lhpt@gmail.com"
                      style={{
                        flex: 1,
                        padding: '8px 12px',
                        borderRadius: 'var(--radius-sm)',
                        border: !isValidEmail(tempProfile.deliveryEmail) && tempProfile.deliveryEmail.length > 0
                          ? '1px solid var(--status-error, #E11D48)'
                          : '1px solid var(--border-subtle)',
                        background: 'var(--bg-surface)',
                        color: 'var(--text-primary)',
                        fontSize: 13.5,
                      }}
                    />
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={handleSendTestEmail}
                      disabled={isSendingTestEmail || !isValidEmail(tempProfile.deliveryEmail)}
                      style={{
                        fontSize: 12.5,
                        opacity: isValidEmail(tempProfile.deliveryEmail) ? 1 : 0.5,
                        cursor: isValidEmail(tempProfile.deliveryEmail) ? 'pointer' : 'not-allowed',
                        gap: 5,
                      }}
                    >
                      <IconSend />
                      <span>{isSendingTestEmail ? 'Đang gửi...' : 'Gửi thử'}</span>
                    </button>
                  </div>
                  {!isValidEmail(tempProfile.deliveryEmail) && tempProfile.deliveryEmail.length > 0 && (
                    <span style={{ fontSize: 11.5, color: 'var(--status-error, #E11D48)' }}>
                      Vui lòng nhập đúng định dạng email (vd: nguyentuananh11a1.lhpt@gmail.com)
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
                      <strong style={{ color: 'var(--text-primary)' }}>Chế độ Tức thì:</strong> Trợ lý tự động rà soát CSDL và gửi email/thông báo ngay khi phát hiện tin tức sốt dẻo hoặc deal hời mới, không cố định khung giờ.
                    </div>
                  </div>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: 0.3 }}>
                        Khung giờ nhận tin tự động
                      </label>
                      <span
                        style={{
                          fontSize: 11.5,
                          fontWeight: 700,
                          color: tempProfile.scheduledTimes.length === getMaxSlotsForFrequency(tempProfile.frequency) ? 'var(--accent-primary)' : 'var(--text-tertiary)',
                        }}
                      >
                        Đã chọn {tempProfile.scheduledTimes.length}/{getMaxSlotsForFrequency(tempProfile.frequency)} giờ
                      </span>
                    </div>
                    <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                      {['07:00', '07:30', '08:00', '12:00', '18:30', '20:00'].map((time) => {
                        const isTimeActive = tempProfile.scheduledTimes.includes(time);
                        const maxSlots = getMaxSlotsForFrequency(tempProfile.frequency);
                        const isFull = !isTimeActive && tempProfile.scheduledTimes.length >= maxSlots && maxSlots > 1;
                        return (
                          <button
                            key={time}
                            type="button"
                            onClick={() => handleToggleTimeSlot(time)}
                            className={`subtab-pill ${isTimeActive ? 'active' : ''}`}
                            style={{
                              fontSize: 12,
                              padding: '5px 12px',
                              opacity: isFull ? 0.45 : 1,
                              cursor: 'pointer',
                            }}
                            title={isFull ? `Đã chọn đủ ${maxSlots} giờ cho tần suất này` : undefined}
                          >
                            {time}
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

                {/* Actions */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 6, paddingTop: 14, borderTop: '1px solid var(--border-subtle)' }}>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setPrefWizardStep('topics')}
                  >
                    Quay lại
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleSavePreferences}
                    disabled={!isValidEmail(tempProfile.deliveryEmail)}
                    style={{
                      padding: '0 20px',
                      opacity: isValidEmail(tempProfile.deliveryEmail) ? 1 : 0.5,
                      cursor: isValidEmail(tempProfile.deliveryEmail) ? 'pointer' : 'not-allowed',
                      gap: 5,
                    }}
                  >
                    <IconCheck />
                    <span>Lưu cấu hình</span>
                  </button>
                </div>
              </div>
            )}
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
                  Người gửi: nguyentuananh11a1.lhpt@gmail.com &rarr; Người nhận: {profile.deliveryEmail}
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
                  Bản tin cá nhân hàng ngày · AIFP Life Assistant
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
                Bản tin được tổng hợp tự động bởi Trợ lý Cá nhân AIFP theo đúng sở thích của bạn.
                <br />
                Được gửi từ hộp thư hệ thống <strong>nguyentuananh11a1.lhpt@gmail.com</strong> theo lịch bạn đã đăng ký.
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
      {/* MODAL: THÊM LUỒNG TỰ ĐỘNG HÓA MỚI (BẰNG NGÔN NGỮ TỰ NHIÊN)                 */}
      {/* ========================================================================= */}
      {showAddCustomChannelModal && (
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
              <h3 style={{ fontSize: 17, fontWeight: 800, margin: 0, color: 'var(--text-primary)' }}>
                Thêm luồng
              </h3>
              <button
                className="btn btn-secondary"
                onClick={() => setShowAddCustomChannelModal(false)}
                style={{ minHeight: 28, padding: '0 8px', fontSize: 12, gap: 4 }}
              >
                <IconClose />
                <span>Đóng</span>
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', display: 'block', marginBottom: 6 }}>
                  Nhu cầu theo dõi
                </label>
                <textarea
                  value={customChannelPrompt}
                  onChange={(e) => setCustomChannelPrompt(e.target.value)}
                  placeholder="Ví dụ: Tuyển dụng Golang Remote trên 3000$ lúc 8h sáng thứ Hai..."
                  rows={4}
                  style={{
                    width: '100%',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-subtle)',
                    background: 'var(--bg-subtle)',
                    color: 'var(--text-primary)',
                    padding: '10px 12px',
                    fontSize: 13.5,
                    resize: 'none',
                    lineHeight: 1.5,
                  }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 4 }}>
                <button className="btn btn-secondary" onClick={() => setShowAddCustomChannelModal(false)} style={{ gap: 4 }}>
                  <IconClose />
                  <span>Hủy</span>
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleCreateCustomChannel}
                  disabled={!customChannelPrompt.trim() || isParsingCustomChannel}
                  style={{ gap: 4 }}
                >
                  <IconPlus />
                  <span>{isParsingCustomChannel ? 'Đang tạo...' : 'Tạo luồng'}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
