-- ============================================================================
-- V2__seed_agents_and_tools.sql
-- Seeds default AI Agents and the 4-Level Permission Tool Catalog
-- ============================================================================

INSERT INTO ai_agents (id, code, name, description, default_model_tier, system_prompt_template, allowed_tool_codes_json, is_active)
VALUES
  ('11111111-1111-1111-1111-111111111101', 'RESEARCH_AGENT', 'Trợ lý Nghiên cứu & Theo dõi', 'Tìm kiếm đa nguồn qua SearXNG/RSS, lọc trùng lặp và kiểm chứng nguồn.', 'FAST', 'You are AIFP Research Agent. Always cite verified URLs from tool outputs.', '["searxng_search","rss_fetch","webpage_extract","email_send_self","notification_send"]', TRUE),
  ('11111111-1111-1111-1111-111111111102', 'LIFE_ADMIN_AGENT', 'Trợ lý Quản lý Công việc & Cuộc sống', 'Quản lý lịch, bộ nhớ cá nhân, email và nhắc việc.', 'FAST', 'You are AIFP Life Admin Agent. Respect 4-level tool permissions.', '["memory_candidate_save","notification_send","email_send_self","email_send_external","calendar_event_write"]', TRUE),
  ('11111111-1111-1111-1111-111111111103', 'DOC_ANALYST_AGENT', 'Trợ lý Tài liệu & Dịch thuật', 'Đọc tài liệu PDF/DOCX và dịch thuật chuẩn ngữ cảnh.', 'FAST', 'You are AIFP Document Analyst Agent.', '["doc_parse","translate_text","memory_candidate_save"]', TRUE);

INSERT INTO ai_tools (id, code, name, category, permission_level, input_schema_json, timeout_seconds, rate_limit_per_hour, is_enabled_globally, version)
VALUES
  ('22222222-2222-2222-2222-222222222201', 'searxng_search', 'Tìm kiếm Web (SearXNG)', 'RESEARCH', 1, '{"type":"object","required":["query"]}', 15, 120, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222202', 'rss_fetch', 'Đọc nguồn RSS/Atom', 'RESEARCH', 1, '{"type":"object","required":["feed_url"]}', 15, 120, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222203', 'webpage_extract', 'Bóc tách nội dung bài viết (Trafilatura)', 'RESEARCH', 1, '{"type":"object","required":["url"]}', 15, 120, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222204', 'doc_parse', 'Đọc tài liệu cá nhân (PyMuPDF4LLM)', 'DOCUMENT', 1, '{"type":"object","required":["file_id"]}', 20, 60, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222205', 'translate_text', 'Dịch thuật đa ngôn ngữ', 'TRANSLATION', 1, '{"type":"object","required":["text","target_lang"]}', 15, 120, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222206', 'memory_candidate_save', 'Lưu đề xuất bộ nhớ cá nhân', 'MEMORY', 2, '{"type":"object","required":["category","content"]}', 10, 60, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222207', 'notification_send', 'Gửi thông báo Web Push / In-App', 'NOTIFICATION', 2, '{"type":"object","required":["title","body"]}', 10, 120, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222208', 'email_send_self', 'Gửi báo cáo về Email cá nhân đã xác minh', 'EMAIL', 2, '{"type":"object","required":["subject","body_markdown"]}', 15, 30, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222209', 'email_send_external', 'Gửi Email tới người nhận bên ngoài', 'EMAIL', 3, '{"type":"object","required":["to_email","subject","body"]}', 15, 20, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222210', 'calendar_event_write', 'Tạo hoặc sửa sự kiện Lịch', 'CALENDAR', 3, '{"type":"object","required":["title","start_time"]}', 15, 30, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222211', 'task_bulk_delete', 'Xóa hàng loạt nhiệm vụ/dữ liệu', 'SYSTEM', 3, '{"type":"object","required":["task_ids"]}', 10, 10, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222212', 'shell_exec', 'Thực thi lệnh hệ điều hành (Cấm mặc định)', 'SYSTEM', 4, '{"type":"object","required":["command"]}', 5, 0, FALSE, '1.0.0');

