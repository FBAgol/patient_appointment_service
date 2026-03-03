CREATE TYPE speciality_type AS ENUM (
    'Allgemeinmedizin',
    'InnereMedizin',
    'Kardiologie',
    'Dermatologie',
    'Orthopädie',
    'Neurologie',
    'Psychiatrie',
    'Gynäkologie',
    'Pädiatrie',
    'Urologie',
    'Augenheilkunde',
    'HNO',
    'Radiologie',
    'Anästhesiologie',
    'Zahnmedizin'
);

CREATE TABLE speciality (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name speciality_type NOT NULL UNIQUE
);
