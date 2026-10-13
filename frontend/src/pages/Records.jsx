import { useEffect, useState } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';

const emptyRecord = { appointmentId: '', symptoms: '', diagnosis: '', notes: '' };
const emptyRx = { appointmentId: '', medicine: '', dosage: '', frequency: '', duration: '', instructions: '' };

export default function Records() {
  const { user } = useAuth();
  const isDoctor = user.role === 'DOCTOR';
  const [records, setRecords] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [visits, setVisits] = useState([]);   // doctor only: appointments they can write notes for
  const [recordForm, setRecordForm] = useState(emptyRecord);
  const [rxForm, setRxForm] = useState(emptyRx);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  async function load() {
    try {
      const [r, p, a] = await Promise.all([api.records(), api.prescriptions(), isDoctor ? api.appointments() : Promise.resolve([])]);
      setRecords(r);
      setPrescriptions(p);
      setVisits(a.filter((x) => x.status === 'CONFIRMED' || x.status === 'COMPLETED'));
    } catch (e) { setError(e.message); }
  }
  useEffect(() => { load(); }, []);

  const visitLabel = (a) => `${a.patient?.name || 'Patient'} — ${a.appointmentDate} ${a.appointmentTime}`;
  const patientOf = (appointmentId) => visits.find((v) => String(v.id) === String(appointmentId))?.patient?.id;

  async function submitRecord(e) {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await api.addRecord({
        patientId: patientOf(recordForm.appointmentId),
        appointmentId: Number(recordForm.appointmentId),
        symptoms: recordForm.symptoms,
        diagnosis: recordForm.diagnosis,
        notes: recordForm.notes
      });
      setSuccess('Medical record saved.');
      setRecordForm(emptyRecord);
      await load();
    } catch (e) { setError(e.message); }
  }

  async function submitRx(e) {
    e.preventDefault();
    setError(''); setSuccess('');
    try {
      await api.addPrescription({
        patientId: patientOf(rxForm.appointmentId),
        appointmentId: Number(rxForm.appointmentId),
        medicine: rxForm.medicine,
        dosage: rxForm.dosage,
        frequency: rxForm.frequency,
        duration: rxForm.duration,
        instructions: rxForm.instructions
      });
      setSuccess('Prescription saved.');
      setRxForm(emptyRx);
      await load();
    } catch (e) { setError(e.message); }
  }

  const setR = (key) => (e) => setRecordForm({ ...recordForm, [key]: e.target.value });
  const setP = (key) => (e) => setRxForm({ ...rxForm, [key]: e.target.value });
  const visitSelect = (value, onChange) => (
    <select required value={value} onChange={onChange}>
      <option value="">Select a confirmed or completed visit</option>
      {visits.map((a) => <option key={a.id} value={a.id}>{visitLabel(a)}</option>)}
    </select>
  );

  return (
    <div>
      <div className="page-title"><div><span className="eyebrow">CLINICAL</span><h1>{isDoctor ? 'Records & prescriptions' : 'My health records'}</h1><p>{isDoctor ? 'Write notes and prescriptions for your patients.' : 'Your diagnoses and prescriptions from your visits.'}</p></div></div>
      {error && <div className="alert">{error}</div>}
      {success && <div className="success">{success}</div>}

      {isDoctor && (
        <>
          <form className="booking-card" onSubmit={submitRecord}>
            <div className="booking-heading"><div><span className="eyebrow">NEW NOTE</span><h2>Add a medical record</h2></div><span className="booking-icon">✚</span></div>
            <div className="form-grid">
              <label className="span-2">Visit{visitSelect(recordForm.appointmentId, setR('appointmentId'))}</label>
              <label>Symptoms<input maxLength={255} value={recordForm.symptoms} onChange={setR('symptoms')} /></label>
              <label>Diagnosis<input required maxLength={255} value={recordForm.diagnosis} onChange={setR('diagnosis')} /></label>
              <label className="span-2">Notes<textarea rows="3" maxLength={4000} value={recordForm.notes} onChange={setR('notes')} /></label>
            </div>
            <button className="primary">Save record →</button>
          </form>

          <form className="booking-card" onSubmit={submitRx}>
            <div className="booking-heading"><div><span className="eyebrow">NEW PRESCRIPTION</span><h2>Prescribe medicine</h2></div><span className="booking-icon">℞</span></div>
            <div className="form-grid">
              <label className="span-2">Visit{visitSelect(rxForm.appointmentId, setP('appointmentId'))}</label>
              <label>Medicine<input required maxLength={200} value={rxForm.medicine} onChange={setP('medicine')} /></label>
              <label>Dosage<input maxLength={100} value={rxForm.dosage} onChange={setP('dosage')} placeholder="e.g. 500 mg" /></label>
              <label>Frequency<input maxLength={100} value={rxForm.frequency} onChange={setP('frequency')} placeholder="e.g. Twice a day" /></label>
              <label>Duration<input maxLength={100} value={rxForm.duration} onChange={setP('duration')} placeholder="e.g. 5 days" /></label>
              <label className="span-2">Instructions<textarea rows="2" maxLength={2000} value={rxForm.instructions} onChange={setP('instructions')} /></label>
            </div>
            <button className="primary">Save prescription →</button>
          </form>
        </>
      )}

      <div className="section-heading"><div><span className="eyebrow">HISTORY</span><h2>Medical records</h2></div><span className="muted">{records.length} total</span></div>
      <div className="table-card">
        {records.length === 0 ? (
          <div className="empty-state"><div>✚</div><strong>No records yet</strong><p>Records appear here after a doctor adds them.</p></div>
        ) : (
          <div className="table-scroll"><table>
            <thead><tr><th>Date</th><th>{isDoctor ? 'Patient' : 'Doctor'}</th><th>Symptoms</th><th>Diagnosis</th><th>Notes</th></tr></thead>
            <tbody>{records.map((r) => (
              <tr key={r.id}>
                <td>{r.recordDate}</td>
                <td>{isDoctor ? r.patient?.name : `Dr. ${r.doctor?.user?.name || ''}`}</td>
                <td>{r.symptoms || '—'}</td><td>{r.diagnosis || '—'}</td><td>{r.notes || '—'}</td>
              </tr>
            ))}</tbody>
          </table></div>
        )}
      </div>

      <div className="section-heading"><div><span className="eyebrow">MEDICATION</span><h2>Prescriptions</h2></div><span className="muted">{prescriptions.length} total</span></div>
      <div className="table-card">
        {prescriptions.length === 0 ? (
          <div className="empty-state"><div>℞</div><strong>No prescriptions yet</strong><p>Prescriptions appear here after a doctor adds them.</p></div>
        ) : (
          <div className="table-scroll"><table>
            <thead><tr><th>Date</th><th>{isDoctor ? 'Patient' : 'Doctor'}</th><th>Medicine</th><th>Dosage</th><th>Frequency</th><th>Duration</th><th>Instructions</th></tr></thead>
            <tbody>{prescriptions.map((p) => (
              <tr key={p.id}>
                <td>{String(p.createdAt || '').slice(0, 10)}</td>
                <td>{isDoctor ? p.patient?.name : `Dr. ${p.doctor?.user?.name || ''}`}</td>
                <td>{p.medicine}</td><td>{p.dosage || '—'}</td><td>{p.frequency || '—'}</td><td>{p.duration || '—'}</td><td>{p.instructions || '—'}</td>
              </tr>
            ))}</tbody>
          </table></div>
        )}
      </div>
    </div>
  );
}
