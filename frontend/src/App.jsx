import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Doctors from './pages/Doctors';
import Appointments from './pages/Appointments';
import Departments from './pages/Departments';
import Records from './pages/Records';
import { useAuth } from './context/AuthContext';

function Guard({children, roles}) {
  const {user}=useAuth();
  if(!user) return <Navigate to="/login" replace/>;
  if(roles && !roles.includes(user.role)) return <Navigate to="/" replace/>;
  return children;
}
export default function App(){
 return <Routes>
  <Route path="/login" element={<Login/>}/><Route path="/register" element={<Register/>}/>
  <Route element={<ProtectedRoute><Layout/></ProtectedRoute>}>
   <Route path="/" element={<Dashboard/>}/>
   <Route path="/doctors" element={<Doctors/>}/>
   <Route path="/appointments" element={<Appointments/>}/>
   <Route path="/records" element={<Guard roles={['PATIENT','DOCTOR']}><Records/></Guard>}/>
   <Route path="/departments" element={<Guard roles={['ADMIN']}><Departments/></Guard>}/>
  </Route>
  <Route path="*" element={<Navigate to="/" replace/>}/>
 </Routes>;
}
