CREATE TABLE doctor_speciality (
    doctor_id UUID NOT NULL,
    speciality_id UUID NOT NULL,
    PRIMARY KEY (doctor_id, speciality_id),

    CONSTRAINT fk_doctor_speciality_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor(id) ON DELETE CASCADE,

    CONSTRAINT fk_doctor_speciality_speciality FOREIGN KEY (speciality_id)
        REFERENCES speciality(id) ON DELETE CASCADE
);