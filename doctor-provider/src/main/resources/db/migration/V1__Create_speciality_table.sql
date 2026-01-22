CREATE TYPE speciality_type AS ENUM (
    'allgemeinmedizin',
    'inneremedizin',
    'kardiologe',
    'dermatologe',
    'orthopäde',
    'neurologe',
    'psychiater',
    'gynäkologe',
    'pädiater',
    'urologe',
    'augenarzt',
    'hno',
    'radiologe',
    'anästhesist',
    'zahnarzt'
);

CREATE TABLE speciality (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name speciality_type NOT NULL UNIQUE
);
