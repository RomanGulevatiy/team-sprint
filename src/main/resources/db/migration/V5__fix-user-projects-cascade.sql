ALTER TABLE user_projects
    DROP CONSTRAINT fk_usepro_on_project;

ALTER TABLE user_projects
    ADD CONSTRAINT fk_usepro_on_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
            ON DELETE CASCADE;