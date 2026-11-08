---
name: human-ui-craft
description: >-
  Enforces a bespoke, human-crafted, high-signal UI/UX design system for React + TypeScript PWA
  and iPhone Safari. Eliminates generic "AI-generated" visual tropes, enforces strict microcopy
  limits (1-3 words per button, zero redundant text bloat), unified design tokens, and native-grade
  iOS PWA ergonomics. Activate this skill whenever creating or modifying frontend components, CSS,
  layouts, or user flows.
---

# Human UI Craft — Anti-AI-Slop & iOS PWA Design Skill

Activate this skill whenever designing, building, or reviewing React components, Tailwind styles, layouts, or PWA configurations.

## 1. Anti-AI-Slop Visual Checklist (Chống Giao Diện "Màu Mè AI")

Before committing any UI component, verify that NONE of these generic AI tropes exist:

- **NO Neon Gradients**: Never use purple-to-pink-to-cyan gradients (`from-purple-500 via-pink-500 to-cyan-500`), glowing neon borders, or rainbow text.
- **NO Sparkle Abuse**: Do not place ✨ (`Sparkles`) icons on every button, input, or heading.
- **NO Text Bloat**:
  - Never add explanatory paragraphs under page titles (e.g., delete *"Quản lý mọi tác vụ tự động thông minh của bạn tại đây"*).
  - Never write verbose button labels (e.g., use `Tạo tác vụ` instead of `Bấm vào đây để tạo tác vụ AI mới`).
  - Never stack more than 3 data points inside a compact list item or card header.
- **NO Inconsistent Radius/Borders**: Never mix arbitrary `rounded-3xl`, `rounded-sm`, and heavy drop shadows on the same screen.

## 2. Unified Design Token System (Hệ Quy Chiếu Thiết Kế Đồng Nhất)

Use a calm, tactile editorial palette inspired by **Linear, Things 3, and Notion Calendar**:

### Color Palette (CSS Variables)
- **Light Mode (Default Warm Paper & Ink)**:
  - `--bg-canvas`: `#F8F7F4` (Warm alabaster background)
  - `--bg-surface`: `#FFFFFF` (Primary card/panel surface)
  - `--bg-subtle`: `#F1EFEA` (Hover state, secondary pills, sidebar)
  - `--text-primary`: `#141413` (Deep charcoal ink)
  - `--text-secondary`: `#575653` (Muted slate-warm gray)
  - `--text-tertiary`: `#8C8A84` (Timestamps, metadata)
  - `--border-subtle`: `#E6E4DD` (`1px solid` crisp divider)
  - `--accent-primary`: `#1F4E3D` (Deep forest emerald — calm, trustworthy) or `#18181B` (High-contrast obsidian)
  - `--status-running`: `#0D9488` (Teal)
  - `--status-approval`: `#D97706` (Amber — requires human action)
  - `--status-error`: `#DC2626` (Crimson)
- **Dark Mode (Obsidian Carbon)**:
  - `--bg-canvas`: `#0C0D0E`
  - `--bg-surface`: `#141618`
  - `--bg-subtle`: `#1C1F22`
  - `--text-primary`: `#F3F4F6`
  - `--text-secondary`: `#9CA3AF`
  - `--border-subtle`: `#26292E`

### Typography & Spacing Scale
- **Sans Font**: `'Plus Jakarta Sans'` or `'Inter'`, system-ui fallback.
- **Mono Font**: `'JetBrains Mono'` (used strictly for cron schedules, timestamps, token counts, and audit IDs).
- **Radius**: `--radius-sm: 6px` (buttons, badges, inputs), `--radius-md: 10px` (cards, dropdowns), `--radius-lg: 14px` (modals, bottom sheets).
- **Microcopy Rules**:
  - Button label: **1–3 words max** (`Tạo mới`, `Chạy thử`, `Tạm dừng`, `Duyệt gửi`, `Xóa`).
  - Empty state: **1 short sentence + 1 primary button**.

## 3. Core Layout & Navigation Architecture

- **Desktop (`>= 1024px`)**:
  - Left slim navigation rail (`220px` fixed width) with 6 core items: `Tổng quan`, `Trợ lý`, `Nhiệm vụ`, `Khám phá`, `Hộp thư`, `Cài đặt` + 1 prominent `+ Nhiệm vụ mới` button at top (`Cmd/Ctrl + K` shortcut).
  - Main workspace (`flex-1`) with optional right context drawer (`340px`, collapsible) for Task Config / Sources / Approval preview.
- **iPhone Safari & Mobile PWA (`< 768px`)**:
  - Use `min-h-[100dvh]` and `h-[100dvh]` (NEVER `100vh`).
  - Bottom Navigation Bar (`56px` + `env(safe-area-inset-bottom)`) with 4 primary destinations (`Trợ lý`, `Nhiệm vụ`, `Hộp thư`, `Cài đặt`) + center/floating quick task trigger.
  - All `<input>` and `<textarea>` elements MUST have `text-base` (`16px`) minimum on mobile so iOS Safari does not zoom in on focus.
  - Provide an inline, dismissible **"Cài đặt lên Màn hình chính"** banner only when `isIOS && !isStandalone` to guide Web Push activation on iOS 16.4+.

