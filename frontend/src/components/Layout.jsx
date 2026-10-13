import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
export default function Layout() {
  const { user, logout } = useAuth(); const navigate = useNavigate();
  const role = user?.role;
  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><span className="brand-mark">✚</span><span>CarePoint<small>HOSPITAL SYSTEM</small></span></div>
      <div className="user-chip"><div className="avatar">{user?.name?.slice(0,1)?.toUpperCase()}</div><div><strong>{user?.name}</strong><small>{role}</small></div></div>
      <nav>
        <NavLink to="/">Overview</NavLink>
        <NavLink to="/doctors">Doctors</NavLink>
        <NavLink to="/appointments">Appointments</NavLink>
        {(role === 'PATIENT' || role === 'DOCTOR') && <NavLink to="/records">Records</NavLink>}
        {role === 'ADMIN' && <NavLink to="/departments">Departments</NavLink>}
      </nav>
      <button className="logout" onClick={() => {logout();navigate('/login');}}>↪ Sign out</button>
    </aside>
    <main className="main-area"><header className="topbar"><div><span className="eyebrow">HEALTHCARE MANAGEMENT</span><h2>Welcome back, {user?.name?.split(' ')[0]}</h2></div><div className="top-right"><span className="status-dot"></span> System online <span className="avatar small">{user?.name?.slice(0,1)?.toUpperCase()}</span></div></header><section className="page-content"><Outlet /></section></main>
  </div>;
}
