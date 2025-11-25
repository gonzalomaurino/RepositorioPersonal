PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    telefono TEXT,
    email TEXT UNIQUE
);

CREATE TABLE IF NOT EXISTS cancha (
    id_cancha INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    tipo_superficie TEXT,
    iluminacion INTEGER DEFAULT 0,
    precio_hora REAL NOT NULL,
    servicios TEXT
);

CREATE TABLE IF NOT EXISTS horario (
    id_horario INTEGER PRIMARY KEY AUTOINCREMENT,
    hora_inicio TEXT NOT NULL,
    hora_fin TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS reserva (
    id_reserva INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT NOT NULL,
    id_cliente INTEGER NOT NULL,
    id_cancha INTEGER NOT NULL,
    id_horario INTEGER NOT NULL,
    monto REAL,
    estado TEXT DEFAULT 'pendiente',
    FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
    FOREIGN KEY (id_cancha) REFERENCES cancha (id_cancha),
    FOREIGN KEY (id_horario) REFERENCES horario (id_horario),
    UNIQUE (fecha, id_cancha, id_horario)
);

CREATE TABLE IF NOT EXISTS pago (
    id_pago INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_pago TEXT NOT NULL,
    monto REAL NOT NULL,
    metodo TEXT,
    id_reserva INTEGER NOT NULL,
    FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS torneo (
    id_torneo INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    fecha_inicio TEXT,
    fecha_fin TEXT,
    categoria TEXT
);

CREATE TABLE IF NOT EXISTS torneo_reserva (
    id_torneo INTEGER NOT NULL,
    id_reserva INTEGER NOT NULL,
    PRIMARY KEY (id_torneo, id_reserva),
    FOREIGN KEY (id_torneo) REFERENCES torneo (id_torneo) ON DELETE CASCADE,
    FOREIGN KEY (id_reserva) REFERENCES reserva (id_reserva) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS servicio (
    id_servicio INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE,
    costo REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS cancha_servicio (
    id_cancha INTEGER NOT NULL,
    id_servicio INTEGER NOT NULL,
    PRIMARY KEY (id_cancha, id_servicio),
    FOREIGN KEY (id_cancha) REFERENCES cancha (id_cancha) ON DELETE CASCADE,
    FOREIGN KEY (id_servicio) REFERENCES servicio (id_servicio) ON DELETE CASCADE
);
