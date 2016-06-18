create table "IMAGE"
(
    "Id" INTEGER not null primary key,
    "Real" Double,
    "Imaginary" Double,
    "EdgeLength" Double,
    "IterationLimit" INTEGER,
    "NumPixels" INTEGER,
    "ColorMapId" INTEGER,
    "ColorMapOffset" INTEGER,
    "RotationAngle" INTEGER,
    "Note" VARCHAR(80),
    "Thumbnail" BLOB
);