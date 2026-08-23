-- Default admin, created because only an admin can create other users (bootstrap problem).
-- Password: ChangeMe123! — change this immediately after the first login in a real deployment.
INSERT INTO users (email, password_hash, role, active, created_at)
VALUES ('admin@trackflow.dev',
        '$2b$10$3.T9f0lDqGaTOh9syDctT.TuBguBK02nrHFwj5gPWwZhn2pCLWvvS',
        'ADMIN',
        true,
        now());