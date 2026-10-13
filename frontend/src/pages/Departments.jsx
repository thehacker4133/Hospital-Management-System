import { useEffect, useState } from 'react';
import { api } from '../services/api';

export default function Departments() {
  const [items, setItems] = useState([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');

  async function load() {
    try { setItems(await api.departments()); } catch (e) { setError(e.message); }
  }
  useEffect(() => { load(); }, []);

  async function create(e) {
    e.preventDefault();
    setError('');
    try {
      await api.addDepartment({ name, description });
      setName(''); setDescription('');
      await load();
    } catch (e) { setError(e.message); }
  }

  async function remove(id) {
    if (!confirm('Delete this department?')) return;
    setError('');
    try { await api.deleteDepartment(id); await load(); } catch (e) { setError(e.message); }
  }

  return (
    <div>
      <div className="page-title"><div><span className="eyebrow">ADMINISTRATION</span><h1>Departments</h1><p>Organize specialties and clinical departments.</p></div></div>
      {error && <div className="alert">{error}</div>}
      <form className="booking-card compact-form" onSubmit={create}>
        <h2>Add a department</h2>
        <div className="form-grid">
          <label>Department name<input required maxLength={100} value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Cardiology" /></label>
          <label>Description<input maxLength={255} value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Short description" /></label>
        </div>
        <button className="primary">Add department +</button>
      </form>
      <div className="department-grid">
        {items.map((d) => (
          <div className="department-card" key={d.id}>
            <span className="department-icon">✚</span>
            <h3>{d.name}</h3>
            <p>{d.description || 'Clinical department'}</p>
            <button className="danger-text" onClick={() => remove(d.id)}>Delete</button>
          </div>
        ))}
      </div>
      {items.length === 0 && <div className="empty-state"><strong>No departments yet</strong><p>Add departments to organize doctors.</p></div>}
    </div>
  );
}
