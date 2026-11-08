---
name: flutter-mobile-companion
description: >-
  Guides the development and verification of the Flutter Native Mobile App (mobile-flutter)
  for iOS and Android. Ensures 100% design token parity with frontend-pwa, zero hardcoded API
  secrets, secure JWT storage, and clean native navigation. Activate this skill when working on
  mobile-flutter/ Dart code.
---

# Flutter Mobile Companion Skill

Activate this skill whenever creating or modifying Dart/Flutter code inside `mobile-flutter/`.

## 1. Visual & Token Parity with Web PWA (`AifpColors`)

The Flutter app MUST use the exact same Anti-AI-Slop editorial color palette as `frontend-pwa/src/styles/tokens.css`:

- `bgCanvas`: `Color(0xFFF8F7F4)` (Warm Alabaster)
- `bgSurface`: `Color(0xFFFFFFFF)` (Pure Card Surface)
- `bgSubtle`: `Color(0xFFF1EFEA)` (Subtle Pill / Indicator)
- `textPrimary`: `Color(0xFF141413)` (Deep Charcoal Ink)
- `textSecondary`: `Color(0xFF575653)` (Slate Warm Gray)
- `borderSubtle`: `Color(0xFFE6E4DD)` (`1px` crisp border, `10px` radius)
- `accentPrimary`: `Color(0xFF1F4E3D)` (Forest Emerald)
- `statusRunning`: `Color(0xFF0D9488)`
- `statusApproval`: `Color(0xFFD97706)` (Level 3 HITL Approval)
- `statusDanger`: `Color(0xFFDC2626)` (Emergency Kill Switch)

## 2. Mobile Security Rules

1. **Zero Embedded Secrets**: Never embed Gemini, OpenAI, or SMTP keys inside Dart code or mobile assets. All AI calls go through Spring Boot (`backend-core`).
2. **Button Microcopy**: Maximum 1–3 words per button (`Duyệt gửi`, `Tạm dừng`, `Dừng khẩn cấp`).
3. **Verification Gate**: Always run `flutter pub get` and `flutter analyze` inside `mobile-flutter/` and confirm `No issues found!` before completing a task.
