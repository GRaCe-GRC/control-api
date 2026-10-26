CREATE TABLE controls (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id uuid REFERENCES control_groups (id) ON DELETE RESTRICT,
    parent_control_id uuid REFERENCES controls (id) ON DELETE RESTRICT,
    control_ref varchar(255) NOT NULL,
    title varchar(255) NOT NULL,
    statement text,
    created_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(255) NOT NULL,
    CONSTRAINT controls_control_ref_key UNIQUE (control_ref)
);

CREATE INDEX controls_group_id_idx ON controls (group_id);

CREATE INDEX controls_parent_control_id_idx ON controls (parent_control_id);
