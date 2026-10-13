import { useEffect, useMemo, useState } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { DAYS, formatSlots, groupByDoctor, toForm } from '../utils/availability';

const emptyForm = { email: '', specialization: '', qualification: '', experienceYears: '', departmentId: '', consultationHours: '' };

export default function Doctors() {
  const { user } = useAuth();
  const isAdmin = user.role === 'ADMIN';
  const [doctors, setDoctors] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [query, setQuery] = useState('');
  const [departmentFilter, setDepartmentFilter] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [availability, setAvailability] = useState({});   // doctorId -> list of weekly blocks
  const [slots, setSlots] = useState([]);                  // the signed-in doctor's editable blocks

  async function load() {
    try {
      const [d, dep, av] = await Promise.all([api.doctors(), api.departments(), api.availability()]);
      setDoctors(d);
      setDepartments(dep);
      const grouped = groupByDoctor(av);
      setAvailability(grouped);
      const mine = d.find((x) => x.user?.id === user.id);
      if (mine) setSlots((grouped[mine.id] || []).map(toForm));
    } catch (e) { setError(e.message); }
  }
  useEffect(() => { load(); }, []);

  const filtered = useMemo(
    () => doctors.filter((d) =>
      `${d.user?.name || ''} ${d.specialization || ''} ${d.department?.name || ''}`
        .toLowerCase().includes(query.toLowerCase()) &&
      (!departmentFilter || String(d.department?.id) === departmentFilter)
    ),
    [doctors, query, departmentFilter]
  );

  async function addDoctor(e) {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await api.addDoctor({
        email: form.email,
        specialization: form.specialization,
        qualification: form.qualification,
        experienceYears: form.experienceYears === '' ? null : Number(form.experienceYears),
        departmentId: form.departmentId === '' ? null : Number(form.departmentId),
        consultationHours: form.consultationHours
      });
      setSuccess('Doctor added.');
      setForm(emptyForm);
      await load();
    } catch (e) { setError(e.message); }
  }

  async function removeDoctor(id) {
    if (!confirm('Remove this doctor?')) return;
    setError(''); setSuccess('');
    try { await api.deleteDoctor(id); await load(); } catch (e) { setError(e.message); }
  }

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  // A doctor can edit their own weekly hours. The server enforces the same rules (10-minute grid, no overlaps).
  const myDoctor = user.role === 'DOCTOR' ? doctors.find((d) => d.user?.id === user.id) : null;
  const addSlot = () => setSlots([...slots, { day: 'MONDAY', start: '09:00', end: '13:00' }]);
  const changeSlot = (i, key, value) => setSlots(slots.map((s, idx) => (idx === i ? { ...s, [key]: value } : s)));
  const removeSlot = (i) => setSlots(slots.filter((_, idx) => idx !== i));

  async function saveHours(e) {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await api.saveMyAvailability(slots);
      setSuccess('Weekly hours saved.');
      await load();
    } catch (err) { setError(err.message); }
  }

  return (
    <div>
      <div className="page-title"><div><span className="eyebrow">OUR CARE TEAM</span><h1>Find your doctor</h1><p>Explore providers and find the right specialist for your needs.</p></div></div>

      {isAdmin && (
        <form className="booking-card" onSubmit={addDoctor}>
          <div className="booking-heading"><div><span className="eyebrow">ADMINISTRATION</span><h2>Add a doctor</h2></div><span className="booking-icon">♙</span></div>
          <div className="form-grid">
            <label>Registered user's email<input type="email" required value={form.email} onChange={set('email')} placeholder="doctor@example.com" /></label>
            <label>Specialization<input maxLength={100} value={form.specialization} onChange={set('specialization')} placeholder="e.g. Cardiologist" /></label>
            <label>Qualification<input maxLength={150} value={form.qualification} onChange={set('qualification')} placeholder="e.g. MBBS, MD" /></label>
            <label>Years of experience<input type="number" min="0" max="70" value={form.experienceYears} onChange={set('experienceYears')} /></label>
            <label>Department
              <select value={form.departmentId} onChange={set('departmentId')}>
                <option value="">Not assigned</option>
                {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
              </select>
            </label>
            <label>Consultation hours<input maxLength={100} value={form.consultationHours} onChange={set('consultationHours')} placeholder="e.g. Mon–Fri 10:00–14:00" /></label>
          </div>
          <button className="primary">Add doctor +</button>
          <p className="muted tiny">The doctor must register an account first. That account then becomes a doctor account.</p>
        </form>
      )}

      {myDoctor && (
        <form className="booking-card" onSubmit={saveHours}>
          <div className="booking-heading"><div><span className="eyebrow">YOUR SCHEDULE</span><h2>Weekly availability</h2></div><span className="booking-icon">◷</span></div>
          {slots.length === 0 && <p className="muted">No hours set yet, so patients can request any 10-minute start time. Add blocks to limit bookings to your working hours.</p>}
          {slots.map((s, i) => (
            <div className="hours-row" key={i}>
              <label>Day
                <select value={s.day} onChange={(e) => changeSlot(i, 'day', e.target.value)}>
                  {DAYS.map(([key, label]) => <option key={key} value={key}>{label}</option>)}
                </select>
              </label>
              <label>From<input type="time" step="600" required value={s.start} onChange={(e) => changeSlot(i, 'start', e.target.value)} /></label>
              <label>To<input type="time" step="600" required value={s.end} onChange={(e) => changeSlot(i, 'end', e.target.value)} /></label>
              <button type="button" className="danger-text" onClick={() => removeSlot(i)}>Remove</button>
            </div>
          ))}
          <div className="table-actions">
            <button type="button" onClick={addSlot}>Add a block +</button>
            <button className="primary">Save hours</button>
          </div>
          <p className="muted tiny">Appointment starts use 10-minute increments. Weekly working-hour blocks cannot overlap.</p>
        </form>
      )}

      <div className="toolbar">
        <div className="search-box">⌕ <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search by name, specialty, department…" /></div>
        <select aria-label="Filter doctors by department" value={departmentFilter} onChange={(e) => setDepartmentFilter(e.target.value)}>
          <option value="">All departments</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        <span className="muted">{filtered.length} providers</span>
      </div>
      {error && <div className="alert">{error}</div>}
      {success && <div className="success">{success}</div>}

      {filtered.length === 0 ? (
        <div className="empty-state large"><div>♙</div><strong>No doctors found</strong><p>{query ? 'Try another search term.' : 'Doctors added by the administrator will appear here.'}</p></div>
      ) : (
        <div className="doctor-grid">
          {filtered.map((d) => (
            <article className="doctor-card" key={d.id}>
              <div className="doctor-card-top"><div className="doctor-avatar">♙</div><span className="status-label"><span className="status-dot"></span> {d.sample ? 'Sample profile' : 'Provider'}</span></div>
              <h3>Dr. {d.user?.name || 'Doctor'}</h3>
              <p className="specialty">{d.specialization || 'General practice'}</p>
              <div className="doctor-detail"><span>⌂</span>{d.department?.name || 'Department not assigned'}</div>
              <div className="doctor-detail"><span>✧</span>{d.qualification || 'Qualification not listed'}</div>
              <div className="doctor-detail"><span>◷</span>{d.experienceYears ?? '—'} years experience</div>
              {formatSlots(availability[d.id]).length > 0 && (
                <div className="hours-list">{formatSlots(availability[d.id]).map((t) => <span key={t}>{t}</span>)}</div>
              )}
              <div className="doctor-card-footer"><span>Consultation</span><strong>{d.consultationHours || 'By appointment'}</strong></div>
              {isAdmin && <button className="danger-text" onClick={() => removeDoctor(d.id)}>Remove doctor</button>}
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
