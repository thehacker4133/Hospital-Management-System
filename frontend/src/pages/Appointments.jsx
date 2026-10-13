import { useEffect, useState } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { formatSlots, groupByDoctor } from '../utils/availability';

// Local calendar date (toISOString() is UTC and can show "yesterday" for users east of UTC, e.g. India).
function todayLocal() {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

export default function Appointments() {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [availability, setAvailability] = useState({});
  const [form, setForm] = useState({ doctorId: '', appointmentDate: '', appointmentTime: '09:00', reason: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const isPatient = user.role === 'PATIENT';

  async function load() {
    try {
      const [a, d, av] = await Promise.all([api.appointments(), api.doctors(), api.availability()]);
      setAppointments(a);
      setDoctors(d);
      setAvailability(groupByDoctor(av));
    } catch (e) { setError(e.message); }
  }
  useEffect(() => { load(); }, [user]);

  async function book(e) {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await api.bookAppointment({ ...form, doctorId: Number(form.doctorId) });
      setSuccess('Appointment request created. It is awaiting confirmation.');
      setForm({ ...form, reason: '' });
      await load();
    } catch (e) { setError(e.message); }
  }

  async function changeStatus(id, status) {
    setError(''); setSuccess('');
    try { await api.updateAppointmentStatus(id, status); await load(); }
    catch (e) { setError(e.message); }
  }

  const chosenHours = formatSlots(availability[form.doctorId]);
  const canCancel = (a) => isPatient && (a.status === 'PENDING' || a.status === 'CONFIRMED');

  return (
    <div>
      <div className="page-title"><div><span className="eyebrow">SCHEDULE & VISITS</span><h1>Appointments</h1><p>Book a visit or review appointment requests.</p></div></div>
      {error && <div className="alert">{error}</div>}
      {success && <div className="success">{success}</div>}

      {isPatient && (
        <form className="booking-card" onSubmit={book}>
          <div className="booking-heading"><div><span className="eyebrow">NEW REQUEST</span><h2>Book an appointment</h2></div><span className="booking-icon">▦</span></div>
          <div className="form-grid">
            <label>Choose doctor
              <select required value={form.doctorId} onChange={(e) => setForm({ ...form, doctorId: e.target.value })}>
                <option value="">Select a doctor</option>
                {doctors.map((d) => <option key={d.id} value={d.id}>Dr. {d.user?.name} — {d.specialization || 'General'}</option>)}
              </select>
            </label>
            <label>Date<input type="date" min={todayLocal()} required value={form.appointmentDate} onChange={(e) => setForm({ ...form, appointmentDate: e.target.value })} /></label>
            <label>Time<input type="time" step="600" required value={form.appointmentTime} onChange={(e) => setForm({ ...form, appointmentTime: e.target.value })} /></label>
            <label>Reason for visit<input maxLength={255} value={form.reason} onChange={(e) => setForm({ ...form, reason: e.target.value })} placeholder="Optional note" /></label>
          </div>
          {form.doctorId && (
            <p className="muted tiny hours-hint">
              {chosenHours.length > 0 ? `This doctor sees patients: ${chosenHours.join(' · ')}.` : 'This doctor has not published weekly hours, so 10-minute start times can be requested.'}
            </p>
          )}
          <button className="primary">Request appointment →</button>
          <p className="muted tiny">Start times are 10 minutes apart. Booked times cannot be requested again; if a time is taken, the available ranges will be shown. Your request stays pending until the clinic confirms it.</p>
        </form>
      )}

      <div className="section-heading"><div><span className="eyebrow">YOUR SCHEDULE</span><h2>Appointment list</h2></div><span className="muted">{appointments.length} total</span></div>
      <div className="table-card">
        {appointments.length === 0 ? (
          <div className="empty-state"><div>▦</div><strong>No appointments yet</strong><p>New appointments will appear here.</p></div>
        ) : (
          <div className="table-scroll">
            <table>
              <thead><tr><th>Patient</th><th>Doctor</th><th>Date & time</th><th>Reason</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {appointments.map((a) => (
                  <tr key={a.id}>
                    <td>{a.patient?.name || 'Patient'}</td>
                    <td>Dr. {a.doctor?.user?.name || 'Provider'}</td>
                    <td>{a.appointmentDate}<small className="cell-sub">{a.appointmentTime}</small></td>
                    <td>{a.reason || '—'}</td>
                    <td><span className={`badge ${String(a.status).toLowerCase()}`}>{a.status}</span></td>
                    <td>
                      <div className="table-actions">
                        {!isPatient && a.status === 'PENDING' && <>
                          <button onClick={() => changeStatus(a.id, 'CONFIRMED')}>Confirm</button>
                          <button className="danger-text" onClick={() => changeStatus(a.id, 'REJECTED')}>Reject</button>
                        </>}
                        {!isPatient && a.status === 'CONFIRMED' && <button onClick={() => changeStatus(a.id, 'COMPLETED')}>Complete</button>}
                        {canCancel(a) && <button className="danger-text" onClick={() => changeStatus(a.id, 'CANCELLED')}>Cancel</button>}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
