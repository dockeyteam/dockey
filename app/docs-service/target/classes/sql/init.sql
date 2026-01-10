-- Initial data for documents table
INSERT INTO documents (title, content, user_id, created_at, status) VALUES ('Welcome Document', 'Welcome to Dockey! This is your first document.', 1, CURRENT_TIMESTAMP, 'PUBLISHED');
INSERT INTO documents (title, content, user_id, created_at, status) VALUES ('Getting Started Guide', 'Here are some tips to get started with the platform...', 1, CURRENT_TIMESTAMP, 'PUBLISHED');
INSERT INTO documents (title, content, user_id, created_at, status) VALUES ('Draft Document', 'This is a work in progress...', 1, CURRENT_TIMESTAMP, 'DRAFT');
