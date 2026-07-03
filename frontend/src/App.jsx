import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import { LayoutDashboard, Users, UserCog, CalendarDays, CreditCard, Stethoscope, LogOut, Plane, ShieldCheck, Menu, X } from 'lucide-react';
import PatientsPage from './pages/PatientsPage';
import DoctorsPage from './pages/DoctorsPage';
import AppointmentsPage from './pages/AppointmentsPage';
import InsuranceProvidersPage from './pages/InsuranceProvidersPage';
import MedicalServicesPage from './pages/MedicalServicesPage';
import PaymentsPage from './pages/PaymentsPage'; 
import AuthPage from './pages/AuthPage';
import { authService } from './services/api';
import DoctorLeavesPage from './pages/DoctorLeavesPage';
import ErrorPage from './pages/ErrorPage';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('med_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const userRole =  user?.role ?? null;
  const isPatient = userRole === 'ROLE_PATIENT' || userRole === 'PATIENT';
  const isDoctor = userRole === 'ROLE_DOCTOR' || user?.role === 'DOCTOR';

  const handleLogout = async () => {
    try {
      await authService.logout();
    } catch (err) {
      console.error("Eroare la logout pe server, curățăm local oricum", err);
    } finally {
      localStorage.removeItem('med_user');
      setUser(null);
      setIsMenuOpen(false);
    }
  };

  const closeMenu = () => setIsMenuOpen(false);
  
return (
    <Router>
      {!user ? (
        <Routes>
          <Route path="/login" element={<AuthPage onLoginSuccess={(userData) => setUser(userData)} />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      ) : (
        <div className="min-h-screen bg-gray-100 flex justify-center p-0 md:p-6">
          <div className="w-full max-w-5xl bg-white shadow-2xl flex flex-col min-h-screen md:min-h-0 md:rounded-2xl overflow-hidden">
            
            <header className="bg-indigo-700 text-white shadow-lg sticky top-0 z-50">
              <div className="px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                  
                  <div className="flex items-center gap-4">
                    <button 
                      onClick={() => setIsMenuOpen(!isMenuOpen)}
                      className="inline-flex lg:hidden p-2 rounded-lg hover:bg-indigo-600 focus:outline-none transition-colors"
                      title="Meniu"
                    >
                      {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
                    </button>

                    <Link to="/" onClick={closeMenu} className="text-xl sm:text-2xl font-bold flex items-center gap-2 whitespace-nowrap">
                      <LayoutDashboard size={24} />
                      MedManager
                    </Link>
                  </div>
                  
                  <nav className="hidden lg:flex items-center gap-1 xl:gap-2">
                    <Link to="/patients" className="flex items-center gap-1.5 py-2 px-2.5 rounded-lg hover:bg-indigo-600 transition-colors text-sm font-medium whitespace-nowrap">
                      <Users size={16} />
                      {isPatient ? 'Profilul Meu' : 'Pacienți'}
                    </Link>
                    <Link to="/doctors" className="flex items-center gap-1.5 py-2 px-2.5 rounded-lg hover:bg-indigo-600 transition-colors text-sm font-medium whitespace-nowrap">
                      <UserCog size={16} />
                      Doctori
                    </Link>
                    <Link to="/appointments" className="flex items-center gap-1.5 py-2 px-2.5 rounded-lg hover:bg-indigo-600 transition-colors text-sm font-medium whitespace-nowrap">
                      <CalendarDays size={16} />
                      Programări
                    </Link>
                    <Link to="/services" className="flex items-center gap-1.5 py-2 px-2.5 rounded-lg hover:bg-indigo-600 transition-colors text-sm font-medium whitespace-nowrap">
                      <Stethoscope size={16} />
                      Servicii
                    </Link>
                    <Link to="/payments" className="flex items-center gap-1.5 py-2 px-2.5 rounded-lg hover:bg-indigo-600 transition-colors text-sm font-medium whitespace-nowrap">
                      <CreditCard size={16} />
                      Plăți
                    </Link>
                    {!isPatient && (
                      <Link to="/insurance" className="flex items-center gap-1.5 py-2 px-2.5 rounded-lg hover:bg-indigo-600 transition-colors text-sm font-medium whitespace-nowrap">
                        <ShieldCheck size={16} />
                        Asigurări
                      </Link>
                    )}
                    {user?.role === 'DOCTOR' && (
                      <Link to="/leaves" className="flex items-center gap-1.5 py-2 px-2.5 rounded-lg hover:bg-indigo-600 transition-colors text-sm font-medium whitespace-nowrap">
                        <Plane size={16} />
                        Concedii
                      </Link>
                    )}
                  </nav>

                  <div className="flex items-center gap-4">
                    <div className="hidden sm:flex flex-col text-right">
                      <span className="font-semibold text-sm leading-tight">{user.username}</span>
                      <span className="text-xs text-indigo-200 capitalize">{user.role?.toLowerCase() || 'Utilizator'}</span>
                    </div>
                    <button 
                      onClick={handleLogout}
                      className="flex items-center gap-2 bg-indigo-800 hover:bg-red-600 p-2 rounded-lg transition-colors text-sm"
                      title="Deconectare"
                    >
                      <LogOut size={18} />
                      <span className="hidden sm:inline">Ieșire</span>
                    </button>
                  </div>
                </div>
              </div>

              {isMenuOpen && (
                <div className="lg:hidden border-t border-indigo-600 bg-indigo-800 px-4 py-3 space-y-1 shadow-inner animate-in fade-in slide-in-from-top-5 duration-200">
                  <div className="sm:hidden px-3 py-2 border-b border-indigo-700 mb-2">
                    <p className="text-sm font-bold">{user.username}</p>
                    <p className="text-xs text-indigo-300 capitalize">{user.role?.toLowerCase() || 'Utilizator'}</p>
                  </div>

                  <Link to="/patients" onClick={closeMenu} className="flex items-center gap-3 py-2.5 px-3 rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium">
                    <Users size={18} />
                    {isPatient ? 'Profilul Meu' : 'Pacienți'}
                  </Link>
                  <Link to="/doctors" onClick={closeMenu} className="flex items-center gap-3 py-2.5 px-3 rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium">
                    <UserCog size={18} />
                    Doctori
                  </Link>
                  <Link to="/appointments" onClick={closeMenu} className="flex items-center gap-3 py-2.5 px-3 rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium">
                    <CalendarDays size={18} />
                    Programări
                  </Link>
                  <Link to="/services" onClick={closeMenu} className="flex items-center gap-3 py-2.5 px-3 rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium">
                    <Stethoscope size={18} />
                    Servicii Medicale
                  </Link>
                  <Link to="/payments" onClick={closeMenu} className="flex items-center gap-3 py-2.5 px-3 rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium">
                    <CreditCard size={18} />
                    Plăți
                  </Link>
                  {!isPatient && (
                    <Link to="/insurance" onClick={closeMenu} className="flex items-center gap-3 py-2.5 px-3 rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium">
                      <ShieldCheck size={18} />
                      Asigurări
                    </Link>
                  )}
                  {user?.role === 'DOCTOR' && (
                    <Link to="/leaves" onClick={closeMenu} className="flex items-center gap-3 py-2.5 px-3 rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium">
                      <Plane size={18} />
                      Concedii
                    </Link>
                  )}
                </div>
              )}
            </header>

            <main className="flex-1 p-4 md:p-8 overflow-y-auto">
              <Routes>
                <Route path="/login" element={<Navigate to="/" replace />} />

                <Route path="/" element={
                  <ProtectedRoute>
                    <div className="text-center mt-20">
                      <h2 className="text-4xl font-bold text-gray-800">Bine ai venit la MedManager</h2>
                      <p className="text-gray-600 mt-4 max-w-2xl mx-auto text-lg">
                        Sistem integrat pentru gestionarea clinicilor medicale. 
                        Administrează pacienții, programează consultații și analizează datele în timp real.
                      </p>
                      <div className="mt-10 grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-4xl mx-auto">
                        <Link to="/patients" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow border-b-4 border-indigo-500">
                          <Users className="mx-auto text-indigo-600 mb-4" size={40} />
                          <h3 className="font-bold text-lg text-gray-800">{isPatient ? 'Profilul Meu' : 'Registru Pacienți'}</h3>
                          <p className="text-sm text-gray-500 mt-2">Gestiune baze de date pacienți și istoric</p>
                        </Link>
                        <Link to="/services" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-all border-b-4 border-indigo-500">
                          <Stethoscope className="mx-auto text-indigo-600 mb-4" size={40} />
                          <h3 className="font-bold text-lg text-gray-800">Servicii Clinică</h3>
                          <p className="text-sm text-gray-500 mt-2">Lista specializărilor și a tarifelor standard</p>
                        </Link>
                        <Link to="/appointments" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow border-b-4 border-indigo-500">
                          <CalendarDays className="mx-auto text-indigo-600 mb-4" size={40} />
                          <h3 className="font-bold text-lg text-gray-800">Programări</h3>
                          <p className="text-sm text-gray-500 mt-2">Programări consultații și verificări sloturi</p>
                        </Link>
                        {isPatient ? (
                          <Link to="/payments" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-all border-b-4 border-indigo-500">
                            <CreditCard className="mx-auto text-indigo-600 mb-4" size={40} />
                            <h3 className="font-bold text-lg text-gray-800">Istoric Plăți</h3>
                            <p className="text-sm text-gray-500 mt-2">Evidența tranzacțiilor financiare</p>
                          </Link>
                        ) : (
                          <Link to="/insurance" className="p-6 bg-white rounded-2xl shadow-md hover:shadow-xl transition-all border-b-4 border-indigo-500">
                            <ShieldCheck className="mx-auto text-indigo-600 mb-4" size={40} />
                            <h3 className="font-bold text-lg text-gray-800">Asigurări</h3>
                            <p className="text-sm text-gray-500 mt-2">Nomenclator furnizori de asigurări</p>
                          </Link>
                        )}
                      </div>
                    </div>
                  </ProtectedRoute>
                } />

                <Route path="/patients" element={<ProtectedRoute><PatientsPage /></ProtectedRoute>} />
                <Route path="/doctors" element={<ProtectedRoute><DoctorsPage /></ProtectedRoute>} />
                <Route path="/appointments" element={<ProtectedRoute><AppointmentsPage /></ProtectedRoute>} />
                <Route path="/services" element={<ProtectedRoute><MedicalServicesPage /></ProtectedRoute>} />
                <Route path="/insurance" element={<ProtectedRoute><InsuranceProvidersPage /></ProtectedRoute>} />
                <Route path="/payments" element={<ProtectedRoute><PaymentsPage /></ProtectedRoute>} />
                
                <Route path="/insurance" element={
                  <ProtectedRoute>
                    {!isPatient ? <InsuranceProvidersPage /> : <Navigate to="/" replace />}
                  </ProtectedRoute>
                } />
                
                <Route path="/leaves" element={
                  <ProtectedRoute>
                    {isDoctor ? <DoctorLeavesPage /> : <Navigate to="/" replace />}
                  </ProtectedRoute>
                } />

                <Route path="/500" element={<ErrorPage type={500} />} />
                <Route path="*" element={<ErrorPage type={404} />} />
              </Routes>
            </main>
          </div>
        </div>
      )}
    </Router>
  );
}

export default App;