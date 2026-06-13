import { useState, useEffect } from 'react';
import { authService, medicalServiceService } from '../services/api';
import { ShieldAlert, User, Lock, FileText,  CheckCircle } from 'lucide-react';

export default function AuthPage({ onLoginSuccess }) {
    const [isLogin, setIsLogin] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [fullName, setFullName] = useState('');
    const [role, setRole] = useState('PATIENT');
    const [age, setAge] = useState('');
    const [sex, setSex] = useState(true);
    const [medicalServiceId, setMedicalServiceId] = useState('');
    const [medicalServices, setMedicalServices] = useState([]);
    const [office, setOffice] = useState('');

    useEffect(() => {
    const fetchServices = async () => {
        try {
        const response = await medicalServiceService.getAll();
        setMedicalServices(response.data);
        
        if (response.data && response.data.length > 0) {
            setMedicalServiceId(response.data[0].id);
        }
        } catch (err) {
        console.error("Nu s-a putut încărca serviciile medicale:", err);
        }
    };

    if (!isLogin && role === 'DOCTOR') {
        fetchServices();
    }
    }, [isLogin, role]);

    const validateForm = () => {
        if (username.trim().length < 3) {
            setError('Username-ul trebuie să aibă cel puțin 3 caractere.');
            return false;
        }
        if (password.length < 4) {
            setError('Parola trebuie să aibă cel puțin 4 caractere.');
            return false;
        }
        if (!isLogin) {
            if (!fullName.trim()) {
                setError('Numele complet este obligatoriu la înregistrare.');
                return false;
            }
            if (role === 'PATIENT' && !age) {
                setError('Data nașterii este obligatorie pentru pacienți.');
                return false;
            }
            if (role === 'PATIENT') {
                const birthDate = new Date(age);
                const today = new Date();
                let years = today.getFullYear() - birthDate.getFullYear();
                if (today.getMonth() < birthDate.getMonth() || (today.getMonth() === birthDate.getMonth() && today.getDate() < birthDate.getDate())) {
                years--;
                }
                if (years < 18) {
                setError('Pacienții trebuie să fie majori (minim 18 ani).');
                return false;
                }
            }
        }
        return true;
    };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!validateForm()) return;

    try {
        if (isLogin) {
            const loginPayload = {
                username: username,
                password: password
            };

            const response = await authService.login(loginPayload);

            const rolesArray = response.data.roles || [];

            const rawRole = rolesArray.length > 0 ? rolesArray[0] : 'PATIENT';
            const cleanRole = typeof rawRole === 'object' ? rawRole.authority : rawRole;

            const userData = {
            username: response.data.username,
            role: cleanRole.replace('ROLE_', ''),
            profileId: response.data.profileId,
            token: true
            };

            localStorage.setItem('med_user', JSON.stringify(userData));

            if (typeof onLoginSuccess === 'function') {
            onLoginSuccess(userData);
            } else if (typeof setUser === 'function') {
            setUser(userData);
            }

            navigate('/');
        } else {
            const registerPayload = {
            username,
            password,
            role,
            fullName,
            age: role === 'PATIENT' ? age : null,
            sex: role === 'PATIENT' ? sex : null,
            medicalServiceId: role === 'DOCTOR' ? parseInt(medicalServiceId) : null,
            office: role === 'DOCTOR' ? office : null
            };

            await authService.register(registerPayload);
            setSuccess('Cont creat cu succes! Acum te poți autentifica.');
            setIsLogin(true);
            setPassword('');
        }
    } catch (err) {
        if (err.response && err.response.data) {
            setError(err.response.data.error || err.response.data.message || 'Date de autentificare invalide.');
        } else {
            setError('Nu s-a putut stabili conexiunea cu serverul medical.');
        }
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-tr from-indigo-800 to-indigo-600 flex items-center justify-center p-4">
      <div className="bg-white w-full max-w-md rounded-2xl shadow-2xl p-8 border border-indigo-100">
        <div className="text-center mb-6">
          <h2 className="text-3xl font-bold text-gray-800">MedManager</h2>
          <p className="text-gray-500 mt-2 text-sm">
            {isLogin ? 'Autentifică-te în portalul medical' : 'Creează un cont nou în sistem'}
          </p>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border-l-4 border-red-500 text-red-700 text-sm rounded flex items-center gap-2">
            <ShieldAlert size={18} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}
        {success && (
          <div className="mb-4 p-3 bg-green-50 border-l-4 border-green-500 text-green-700 text-sm rounded flex items-center gap-2">
            <CheckCircle size={18} className="shrink-0" />
            <span>{success}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Username</label>
            <div className="relative">
              <User className="absolute left-3 top-3 text-gray-400" size={18} />
              <input 
                type="text"
                className="w-full pl-10 pr-4 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                placeholder="ex: ioan_popescu"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Parolă</label>
            <div className="relative">
              <Lock className="absolute left-3 top-3 text-gray-400" size={18} />
              <input 
                type="password"
                className="w-full pl-10 pr-4 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          {!isLogin && (
            <>
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Nume Complet</label>
                <div className="relative">
                  <FileText className="absolute left-3 top-3 text-gray-400" size={18} />
                  <input 
                    type="text"
                    className="w-full pl-10 pr-4 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    placeholder="ex: Dr. Popescu Ioan sau Vasile Maria"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Rol Utilizator</label>
                <select 
                  className="w-full bg-white px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                >
                  <option value="PATIENT">Pacient</option>
                  <option value="DOCTOR">Medic / Doctor</option>
                </select>
              </div>

              {role === 'PATIENT' ? (
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Data Nașterii</label>
                    <input 
                      type="date"
                      className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                      value={age}
                      onChange={(e) => setAge(e.target.value)}
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Sex</label>
                    <select 
                      className="w-full bg-white px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                      value={sex}
                      onChange={(e) => setSex(e.target.value === 'true')}
                    >
                      <option value="true">Masculin</option>
                      <option value="false">Feminin</option>
                    </select>
                  </div>
                </div>
              ) : (
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Cabinet</label>
                    <input 
                      type="text"
                      className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                      placeholder="Cabinet 204"
                      value={office}
                      onChange={(e) => setOffice(e.target.value)}
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Serviciu Medical</label>
                    <select 
                      className="w-full bg-white px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                      value={medicalServiceId}
                      onChange={(e) => setMedicalServiceId(e.target.value)}
                      required
                    >
                      {medicalServices.length === 0 ? (
                        <option value="">Se încarcă serviciile...</option>
                      ) : (
                        medicalServices.map((service) => (
                          <option key={service.id} value={service.id}>
                            {service.specialization} ({service.name})
                          </option>
                        ))
                      )}
                    </select>
                  </div>
                </div>
              )}
            </>
          )}

          <button 
            type="submit"
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded-xl transition-colors mt-2"
          >
            {isLogin ? 'Autentificare' : 'Înregistrare'}
          </button>
        </form>

        <div className="text-center mt-6 pt-4 border-t border-gray-100">
          <button 
            type="button"
            className="text-indigo-600 hover:underline text-sm font-medium"
            onClick={() => {
              setIsLogin(!isLogin);
              setError('');
            }}
          >
            {isLogin ? 'Nu ai cont? Înregistrează-te' : 'Ai deja cont? Conectează-te'}
          </button>
        </div>
      </div>
    </div>
  );
}