-- Baseline RBAC data: the four-tier role model (Reader, Author, Editor, Admin)
-- per the Software Architecture Document, Section 10, and a starter set of
-- atomic permissions per the Database Design Specification's Permissions entity.

INSERT INTO roles (name, description) VALUES
    ('READER', 'Default role for every registered user; can read, bookmark, comment, and take quizzes.'),
    ('AUTHOR', 'Can create and edit their own articles; submits for editorial review.'),
    ('EDITOR', 'Can review, publish, and manage any article; moderates comments.'),
    ('ADMIN', 'Full platform administration: users, roles, reference data, analytics.');

INSERT INTO permissions (code, description) VALUES
    ('article.create', 'Create a new draft article'),
    ('article.edit.own', 'Edit an article the user authored'),
    ('article.edit.any', 'Edit any article regardless of author'),
    ('article.publish', 'Transition an article to published status'),
    ('article.delete', 'Soft-delete an article'),
    ('comment.moderate', 'Hide, flag, or restore comments and replies'),
    ('user.manage', 'Change user roles and account status'),
    ('reference.manage', 'Manage Government Schemes, Plant Diseases, and Machinery reference data'),
    ('analytics.view', 'View platform-wide analytics dashboards');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'AUTHOR' AND p.code IN ('article.create', 'article.edit.own');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'EDITOR' AND p.code IN
    ('article.create', 'article.edit.own', 'article.edit.any', 'article.publish', 'article.delete', 'comment.moderate');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';
