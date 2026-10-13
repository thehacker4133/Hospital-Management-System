const API = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
export const STORAGE_KEY = 'hospitalUser';

export function getStoredUser() {
  try { return JSON.parse(sessionStorage.getItem(STORAGE_KEY) || 'null'); } catch { return null; }
}

async function request(path, options = {}) {
  const token = getStoredUser()?.token;
  let response;
  try {
    response = await fetch(`${API}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers || {})
      }
    });
  } catch {
    throw new Error('Cannot reach the server. Check that the backend is running.');
  }
  const data = response.status === 204 ? null : await response.json().catch(() => null);
  // An expired or invalid token: tell the app to sign the user out (login/register errors are handled normally).
  if (response.status === 401 && !path.startsWith('/auth/')) window.dispatchEvent(new Event('auth:expired'));
  if (!response.ok) throw new Error(data?.message || `Request failed (${response.status})`);
  return data;
}

const json = (method, body) => ({ method, body: JSON.stringify(body) });

export const api = {
  login: (body) => request('/auth/login', json('POST', body)),
  register: (body) => request('/auth/register', json('POST', body)),

  doctors: () => request('/doctors'),
  addDoctor: (body) => request('/doctors', json('POST', body)),
  deleteDoctor: (id) => request(`/doctors/${id}`, { method: 'DELETE' }),
  availability: () => request('/doctors/availability'),
  saveMyAvailability: (slots) => request('/doctors/me/availability', json('PUT', { slots })),

  departments: () => request('/departments'),
  addDepartment: (body) => request('/departments', json('POST', body)),
  deleteDepartment: (id) => request(`/departments/${id}`, { method: 'DELETE' }),

  // The server scopes this list by role (admin: all, doctor: own, patient: own).
  appointments: () => request('/appointments'),
  bookAppointment: (body) => request('/appointments', json('POST', body)),
  updateAppointmentStatus: (id, status) => request(`/appointments/${id}/status`, json('PATCH', { status })),

  records: () => request('/medical-records'),
  addRecord: (body) => request('/medical-records', json('POST', body)),
  prescriptions: () => request('/prescriptions'),
  addPrescription: (body) => request('/prescriptions', json('POST', body))
};
