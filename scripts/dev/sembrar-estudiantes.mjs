import { readFileSync } from 'node:fs';

const API_BASE = process.env.API ?? 'http://localhost:8080/api/v1';

const API = API_BASE;
const LOG = process.env.BACKEND_LOG ?? 'backend-dev.log';
const CLAVE = 'Clave-de-prueba-1';

async function llamar(metodo, ruta, cuerpo, token) {
  const r = await fetch(API + ruta, {
    method: metodo,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: cuerpo ? JSON.stringify(cuerpo) : undefined,
  });
  const texto = await r.text();
  return { estado: r.status, datos: texto ? JSON.parse(texto) : null };
}

function tokenDeVerificacion(correo) {
  const lineas = readFileSync(LOG, 'utf8').split('\n').filter((l) => l.includes(correo) && l.includes('confirm?token='));
  const ultima = lineas[lineas.length - 1] ?? '';
  return ultima.slice(ultima.indexOf('token=') + 6).trim();
}

const personas = [
  { n: 'Camila', a: 'Rojas', carrera: 'Ingeniería de Sistemas', ciclo: 7, sede: 'Lima Centro', g: 'FEMALE', nac: '2003-05-10', gustos: ['Café', 'Vóley', 'Videojuegos', 'Música indie'], hab: ['Programación', 'Diseño gráfico'], d: 'Me encanta el café, el vóley y programar hasta tarde. Busco gente para estudiar y salir.' },
  { n: 'Diego', a: 'Paredes', carrera: 'Ingeniería de Sistemas', ciclo: 6, sede: 'Lima Centro', g: 'MALE', nac: '2002-09-21', gustos: ['Videojuegos', 'Anime', 'Fútbol', 'Café'], hab: ['Programación'], d: 'Gamer y hincha. Armo grupos de estudio los jueves.' },
  { n: 'Valeria', a: 'Quispe', carrera: 'Psicología', ciclo: 4, sede: 'Lima Norte', g: 'FEMALE', nac: '2004-01-15', gustos: ['Lectura', 'Cine', 'Voluntariado', 'Mascotas'], hab: ['Redacción', 'Oratoria'], d: 'Voluntaria los fines de semana. Me gustan las charlas largas.' },
  { n: 'Mateo', a: 'Flores', carrera: 'Administración', ciclo: 8, sede: 'Lima Centro', g: 'MALE', nac: '2001-11-30', gustos: ['Emprendimiento', 'Gimnasio', 'Running', 'Café'], hab: ['Liderazgo', 'Excel'], d: 'Entreno por las mañanas y emprendo por las tardes.' },
];

let intereses = null;
let habilidades = null;
const idDe = (lista, nombre) => lista.find((x) => x.name === nombre)?.id;

for (const p of personas) {
  const correo = `${p.n.toLowerCase()}.${p.a.toLowerCase()}@utp.edu.pe`;
  let r = await llamar('POST', '/auth/register', { universityId: 1, email: correo, password: CLAVE, firstName: p.n, lastName: p.a });
  console.log('registro', correo, r.estado);
  await new Promise((x) => setTimeout(x, 700));
  const token = tokenDeVerificacion(correo);
  const c = await fetch(`${API}/email-verifications/confirm?token=${token}`);
  console.log('verificación', c.status);
  r = await llamar('POST', '/auth/login', { email: correo, password: CLAVE });
  const sesion = r.datos;
  if (intereses === null) {
    intereses = (await llamar('GET', '/interests', null, sesion.accessToken)).datos;
    habilidades = (await llamar('GET', '/skills', null, sesion.accessToken)).datos;
  }
  r = await llamar('PUT', '/students/me/profile', {
    career: p.carrera, cycle: p.ciclo, description: p.d, visibility: 'PUBLIC', gender: p.g, birthDate: p.nac, campus: p.sede,
    interestIds: p.gustos.map((x) => idDe(intereses, x)).filter(Boolean), skillIds: p.hab.map((x) => idDe(habilidades, x)).filter(Boolean),
  }, sesion.accessToken);
  console.log('perfil', p.n, r.estado);
}
