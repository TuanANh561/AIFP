import 'package:flutter_test/flutter_test.dart';
import 'package:aifp_mobile/main.dart';

void main() {
  testWidgets('AIFP Mobile Companion navigates 5 tabs and triggers Emergency Kill Switch', (WidgetTester tester) async {
    await tester.pumpWidget(const AifpMobileApp());

    // Verify Tasks tab is visible initially
    expect(find.text('Tuyển dụng Flutter Intern TP.HCM'), findsOneWidget);

    // Navigate to Inbox tab (tab index 3) and approve Level 3 HITL request
    await tester.tap(find.text('Hộp thư'));
    await tester.pumpAndSettle();

    expect(find.text('Duyệt gửi'), findsOneWidget);
    await tester.tap(find.text('Duyệt gửi'));
    await tester.pumpAndSettle();
    expect(find.textContaining('APPROVED'), findsOneWidget);

    // Navigate to Security tab (tab index 4) and trigger Emergency Kill Switch
    await tester.tap(find.text('Bảo mật'));
    await tester.pumpAndSettle();

    expect(find.text('Dừng khẩn cấp'), findsOneWidget);
    await tester.tap(find.text('Dừng khẩn cấp'));
    await tester.pumpAndSettle();

    // Return to Tasks tab and confirm Kill Switch banner is displayed
    await tester.tap(find.text('Nhiệm vụ'));
    await tester.pumpAndSettle();

    expect(find.textContaining('Đã kích hoạt Dừng khẩn cấp'), findsOneWidget);
  });
}

