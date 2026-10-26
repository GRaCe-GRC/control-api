CREATE TABLE control_groups (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_group_id uuid REFERENCES control_groups (id) ON DELETE RESTRICT,
    group_ref varchar(255) NOT NULL,
    title varchar(255) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(255) NOT NULL,
    CONSTRAINT control_groups_group_ref_key UNIQUE (group_ref)
);

CREATE INDEX control_groups_parent_group_id_idx ON control_groups (parent_group_id);
