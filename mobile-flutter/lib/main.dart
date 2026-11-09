import 'package:flutter/material.dart';

void main() {
  runApp(const AifpMobileApp());
}

/// Compile-time & Runtime Mobile API Configuration (Zero embedded secrets)
/// Override at build time via: flutter build apk --dart-define=API_BASE_URL=https://your-domain.com
class AifpMobileConfig {
  static const String defaultApiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://api.aifp.app',
  );
}

/// Unified Anti-AI-Slop Design Tokens matching frontend-pwa/src/styles/tokens.css
class AifpColors {
  static const Color bgCanvas = Color(0xFFF8F7F4);
  static const Color bgSurface = Color(0xFFFFFFFF);
  static const Color bgSubtle = Color(0xFFF1EFEA);
  static const Color textPrimary = Color(0xFF141413);
  static const Color textSecondary = Color(0xFF575653);
  static const Color borderSubtle = Color(0xFFE6E4DD);
  static const Color accentPrimary = Color(0xFF1F4E3D);
  static const Color statusRunning = Color(0xFF0D9488);
  static const Color statusApproval = Color(0xFFD97706);
  static const Color statusDanger = Color(0xFFDC2626);
}

class AifpMobileApp extends StatelessWidget {
  const AifpMobileApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'AIFP Mobile',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        scaffoldBackgroundColor: AifpColors.bgCanvas,
        colorScheme: ColorScheme.fromSeed(
          seedColor: AifpColors.accentPrimary,
          primary: AifpColors.accentPrimary,
          surface: AifpColors.bgSurface,
        ),
        useMaterial3: true,
      ),
      home: const AifpMobileShell(),
    );
  }
}

class AifpMobileShell extends StatefulWidget {
  const AifpMobileShell({super.key});

  @override
  State<AifpMobileShell> createState() => _AifpMobileShellState();
}

class _AifpMobileShellState extends State<AifpMobileShell> {
  int _selectedIndex = 0;
  bool _killSwitchActive = false;
  String _approvalStatus = 'PENDING';
  String _selectedPersona = 'JOB_SEEKER';
  String _activeServerEndpoint = AifpMobileConfig.defaultApiBaseUrl;

  @override
  Widget build(BuildContext context) {
    final pages = <Widget>[
      _buildTasksAndOverviewTab(),
      _buildChatTab(),
      _buildDocsAndTranslationTab(),
      _buildInboxApprovalTab(),
      _buildSecuritySettingsTab(),
    ];

    return Scaffold(
      appBar: AppBar(
        backgroundColor: AifpColors.bgSurface,
        elevation: 0,
        title: const Text(
          'AIFP',
          style: TextStyle(
            color: AifpColors.textPrimary,
            fontWeight: FontWeight.w700,
            fontSize: 18,
          ),
        ),
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(1),
          child: Container(color: AifpColors.borderSubtle, height: 1),
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: pages[_selectedIndex],
        ),
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        backgroundColor: AifpColors.bgSurface,
        indicatorColor: AifpColors.bgSubtle,
        onDestinationSelected: (idx) => setState(() => _selectedIndex = idx),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.check_circle_outline), label: 'Nhiệm vụ'),
          NavigationDestination(icon: Icon(Icons.chat_bubble_outline), label: 'Trợ lý'),
          NavigationDestination(icon: Icon(Icons.description_outlined), label: 'Tài liệu'),
          NavigationDestination(icon: Icon(Icons.inbox_outlined), label: 'Hộp thư'),
          NavigationDestination(icon: Icon(Icons.shield_outlined), label: 'Bảo mật'),
        ],
      ),
    );
  }

  Widget _buildTasksAndOverviewTab() {
    const personas = [
      ('JOB_SEEKER', 'Tìm việc'),
      ('DEVELOPER', 'Lập trình viên'),
      ('STUDENT', 'Sinh viên'),
      ('OFFICE_WORKER', 'Văn phòng'),
      ('BUSINESS', 'Kinh doanh'),
    ];
    return ListView(
      children: [
        if (_killSwitchActive)
          Container(
            padding: const EdgeInsets.all(12),
            margin: const EdgeInsets.only(bottom: 12),
            decoration: BoxDecoration(
              color: AifpColors.bgSurface,
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: AifpColors.statusDanger),
            ),
            child: const Text(
              'Đã kích hoạt Dừng khẩn cấp: Mọi tác vụ nền đang tạm khóa.',
              style: TextStyle(color: AifpColors.statusDanger, fontWeight: FontWeight.w600),
            ),
          ),
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            children: personas.map((p) {
              final active = _selectedPersona == p.$1;
              return Padding(
                padding: const EdgeInsets.only(right: 8, bottom: 12),
                child: ChoiceChip(
                  label: Text(p.$2),
                  selected: active,
                  onSelected: (_) => setState(() => _selectedPersona = p.$1),
                ),
              );
            }).toList(),
          ),
        ),
        _taskCard('Tuyển dụng Flutter Intern TP.HCM', '08:00 · Hằng ngày · DELTA_ONLY', !_killSwitchActive),
        const SizedBox(height: 10),
        _taskCard('Bản tin bảo mật Spring Boot & OWASP', '07:30 · Thứ Hai · DELTA_ONLY', !_killSwitchActive),
      ],
    );
  }

  Widget _taskCard(String title, String subtitle, bool running) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AifpColors.bgSurface,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AifpColors.borderSubtle),
      ),
      child: Row(
        children: [
          Icon(
            Icons.circle,
            size: 10,
            color: running ? AifpColors.statusRunning : AifpColors.textSecondary,
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 15)),
                const SizedBox(height: 4),
                Text(subtitle, style: const TextStyle(color: AifpColors.textSecondary, fontSize: 12)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildChatTab() {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AifpColors.bgSurface,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AifpColors.borderSubtle),
      ),
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text('AIFP Research Agent · gemini-2.5-flash',
              style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
          SizedBox(height: 8),
          Text('Đã tổng hợp 2 tin tuyển dụng Flutter Intern mới sáng nay với đầy đủ nguồn xác thực.'),
        ],
      ),
    );
  }

  Widget _buildDocsAndTranslationTab() {
    return ListView(
      children: [
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AifpColors.bgSurface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AifpColors.borderSubtle),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Tài liệu & Dịch thuật · PyMuPDF4LLM',
                  style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
              SizedBox(height: 8),
              Text('Kien_Truc_AIFP.pdf: Chính sách phê duyệt HITL Level 3 được xác thực tại [Trang 2].'),
            ],
          ),
        ),
        const SizedBox(height: 10),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AifpColors.bgSurface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AifpColors.borderSubtle),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Trợ lý Lịch & Soạn thảo AI (P1)',
                  style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
              SizedBox(height: 8),
              Text('Phỏng vấn Kỹ sư AI Backend · 09:00 Ngày mai (CONFIRMED · Level 3 HITL)'),
            ],
          ),
        ),
        const SizedBox(height: 10),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AifpColors.bgSurface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AifpColors.borderSubtle),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Trợ lý Mua sắm & So sánh giá (M13)',
                  style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
              SizedBox(height: 8),
              Text('Bàn phím cơ Keychron K2 Pro · 1.890.000 đ (Shopee Mall · Đang theo dõi)'),
            ],
          ),
        ),
        const SizedBox(height: 10),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AifpColors.bgSurface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AifpColors.borderSubtle),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('AI Vision & Quét hóa đơn (M14)',
                      style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
                  Text('Level 2',
                      style: TextStyle(fontSize: 11, fontFamily: 'monospace', color: AifpColors.accentPrimary)),
                ],
              ),
              SizedBox(height: 8),
              Text('The Coffee House · 80.000 đ (MoMo)',
                  style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
              SizedBox(height: 4),
              Text('1. Cà phê sữa đá (L) - 45.000đ\n2. Bánh mì chà bông - 35.000đ',
                  style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
            ],
          ),
        ),
        const SizedBox(height: 10),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AifpColors.bgSurface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AifpColors.borderSubtle),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Thử đồ ảo AI (M15 - Try-On)',
                      style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
                  Text('Tự hủy 24h',
                      style: TextStyle(fontSize: 11, fontFamily: 'monospace', color: AifpColors.textSecondary)),
                ],
              ),
              SizedBox(height: 8),
              Text('Áo sơ mi linen cổ trụ · Phối đồ hoàn tất',
                  style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
            ],
          ),
        ),
        const SizedBox(height: 10),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AifpColors.bgSurface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AifpColors.borderSubtle),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('PC Companion (M16)',
                      style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
                  Text('CONNECTED',
                      style: TextStyle(fontSize: 11, fontFamily: 'monospace', color: AifpColors.statusRunning)),
                ],
              ),
              SizedBox(height: 8),
              Text('Workstation Desktop · Thư mục Sandbox an toàn',
                  style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
            ],
          ),
        ),
        const SizedBox(height: 10),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AifpColors.bgSurface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AifpColors.borderSubtle),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Video Studio & Mã nguồn mở (M17)',
                      style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
                  Text('0 WATERMARK',
                      style: TextStyle(fontSize: 11, fontFamily: 'monospace', color: AifpColors.statusRunning)),
                ],
              ),
              SizedBox(height: 8),
              Text('Stickman Director & Lemo-Opuscar WebGL',
                  style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
              SizedBox(height: 4),
              Text('Biên tập Lossless-Cut & Remotion · Tỉ lệ 16:9 / 9:16 Shorts · Phụ đề tự động',
                  style: TextStyle(fontSize: 12, color: AifpColors.textSecondary)),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildInboxApprovalTab() {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AifpColors.bgSurface,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AifpColors.statusApproval),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            'Level 3 · Cần duyệt (email_send_external) · $_approvalStatus',
            style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: AifpColors.statusApproval),
          ),
          const SizedBox(height: 6),
          const Text('Gửi báo cáo tổng hợp tuần tới hr-partner@company.vn'),
          if (_approvalStatus == 'PENDING') ...[
            const SizedBox(height: 12),
            Row(
              children: [
                ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AifpColors.accentPrimary,
                    foregroundColor: Colors.white,
                  ),
                  onPressed: () => setState(() => _approvalStatus = 'APPROVED'),
                  child: const Text('Duyệt gửi'),
                ),
                const SizedBox(width: 8),
                OutlinedButton(
                  onPressed: () => setState(() => _approvalStatus = 'REJECTED'),
                  child: const Text('Từ chối'),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSecuritySettingsTab() {
    return ListView(
      children: [
        Container(
          padding: const EdgeInsets.all(14),
          margin: const EdgeInsets.only(bottom: 12),
          decoration: BoxDecoration(
            color: AifpColors.bgSurface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AifpColors.borderSubtle),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Máy chủ API (Mobile & Web Deploy)',
                style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
              ),
              const SizedBox(height: 6),
              Text(
                'Endpoint: $_activeServerEndpoint',
                style: const TextStyle(fontSize: 12, color: AifpColors.textSecondary),
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                children: [
                  OutlinedButton(
                    onPressed: () => setState(() => _activeServerEndpoint = 'https://api.aifp.app'),
                    child: const Text('Cloud HTTPS'),
                  ),
                  OutlinedButton(
                    onPressed: () => setState(() => _activeServerEndpoint = 'http://10.0.2.2:8080'),
                    child: const Text('Android Dev'),
                  ),
                ],
              ),
            ],
          ),
        ),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AifpColors.bgSurface,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AifpColors.statusDanger),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Expanded(
                child: Text(
                  'Nút dừng khẩn cấp AI (Kill Switch)',
                  style: TextStyle(fontWeight: FontWeight.w600, color: AifpColors.statusDanger),
                ),
              ),
              ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AifpColors.statusDanger,
                  foregroundColor: Colors.white,
                ),
                onPressed: () => setState(() => _killSwitchActive = true),
                child: const Text('Dừng khẩn cấp'),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

