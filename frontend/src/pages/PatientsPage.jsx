import { useState, useEffect, useCallback } from 'react';
import { patientService } from '../services/api';
import { User,  ShieldAlert, PlusCircle, Edit2, Trash2, ArrowUpDown } from 'lucide-react';

export default function PatientsPage() {
  const [patients, setPatients] = useState([]);
  
  const [page, setPage] = useState(0);
  const [size] = useState(5);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState('name');
  const [sortDir, setSortDir] = useState('asc');

  const [showModal, setShowModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedId, setSelectedId] = useState(null);

  const [name, setName] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [sex, setSex] = useState(true);
  const [subscription, setSubscription] = useState(false);

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchPatients = useCallback(async () => {
    try {
      const params = {
        page: page,
        size: size,
        sort: `${sortBy},${sortDir}`
      };
      const response = await patientService.getPaged(params);
      
      setPatients(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (err) {
      setError('Eroare la preluarea listei de pacienți de pe server.');
    }
  }, [page, size, sortBy, sortDir]);

  useEffect(() => {
    fetchPatients();
  }, [fetchPatients]);

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDir('asc');
    }
    setPage(0);
  };

  const openCreateModal = () => {
    setIsEditMode(false);
    setSelectedId(null);
    setName('');
    setBirthDate('');
    setSex(true);
    setSubscription(false);
    setError('');
    setShowModal(true);
  };

  const openEditModal = (patient) => {
    setIsEditMode(true);
    setSelectedId(patient.id);
    setName(patient.name);
    setBirthDate(patient.age ? patient.age.substring(0, 10) : '');
    setSex(patient.sex ?? true);
    setSubscription(patient.subscription ?? false);
    setError('');
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (name.trim().length < 3) {
      setError('Numele complet trebuie să conțină cel puțin 3 caractere.');
      return;
    }
    if (!birthDate) {
      setError('Data nașterii este obligatorie.');
      return;
    }

    const payload = {
      name: name.trim(),
      birthDate: birthDate, 
      sex: sex,
      subscription: subscription,
      insuranceProviderId: null 
    };

    try {
      if (isEditMode) {
        await patientService.update(selectedId, payload);
        setSuccess('Datele pacientului au fost actualizate.');
      } else {
        await patientService.create(payload);
        setSuccess('Pacientul a fost înregistrat cu succes.');
      }
      setShowModal(false);
      fetchPatients();
    } catch (err) {
      setError(err.response?.data?.message || 'A apărut o eroare la salvarea datelor.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Sigur doriți să ștergeți acest pacient? Această acțiune poate anula și programările asociate.')) return;
    setError('');
    setSuccess('');

    try {
      await patientService.delete(id);
      setSuccess('Pacientul a fost eliminat din sistem.');
      fetchPatients();
    } catch (err) {
      setError(err.response?.data?.message || 'Nu s-a putut șterge pacientul (posibil să aibă constrângeri de cheie străină).');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">Registru Pacienți</h2>
          <p className="text-sm text-gray-500">Administrare pacienți, abonamente și istoric medical</p>
        </div>
        
        <button 
          onClick={openCreateModal}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors"
        >
          <PlusCircle size={18} />
          Adaugă Pacient
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border-l-4 border-red-500 text-red-700 text-sm rounded-xl flex items-center gap-2">
          <ShieldAlert size={20} className="shrink-0" />
          <span>{error}</span>
        </div>
      )}
      {success && (
        <div className="p-4 bg-green-50 border-l-4 border-green-500 text-green-700 text-sm rounded-xl">
          {success}
        </div>
      )}

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 border-b border-gray-100 text-xs font-bold text-gray-500 uppercase tracking-wider">
              <th className="py-4 px-6">ID</th>
              <th className="py-4 px-6 cursor-pointer hover:bg-gray-100" onClick={() => handleSort('name')}>
                <div className="flex items-center gap-1">Nume Complet <ArrowUpDown size={14} /></div>
              </th>
              <th className="py-4 px-6">Data Nașterii</th>
              <th className="py-4 px-6">Gen</th>
              <th className="py-4 px-6">Status Abonament</th>
              <th className="py-4 px-6 text-center">Acțiuni</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 text-sm text-gray-700">
            {patients.length > 0 ? (
              patients.map((patient) => (
                <tr key={patient.id} className="hover:bg-gray-50/50 transition-colors">
                  <td className="py-4 px-6 font-semibold text-gray-400">#{patient.id}</td>
                  <td className="py-4 px-6 font-medium text-gray-900">
                    <div className="flex items-center gap-2">
                      <User size={16} className="text-indigo-500" />
                      {patient.name}
                    </div>
                  </td>
                  <td className="py-4 px-6 text-gray-500">
                    {patient.age ? new Date(patient.age).toLocaleDateString('ro-RO') : 'Nespecificată'}
                  </td>
                  <td className="py-4 px-6">
                    <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${patient.sex ? 'bg-blue-50 text-blue-700' : 'bg-pink-50 text-pink-700'}`}>
                      {patient.sex ? 'Masculin' : 'Feminin'}
                    </span>
                  </td>
                  <td className="py-4 px-6">
                    <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${patient.subscription ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-600'}`}>
                      {patient.subscription ? 'Activ' : 'Fără Abonament'}
                    </span>
                  </td>
                  <td className="py-4 px-6">
                    <div className="flex justify-center items-center gap-3">
                      <button 
                        onClick={() => openEditModal(patient)}
                        className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Editează profilul"
                      >
                        <Edit2 size={16} />
                      </button>
                      <button 
                        onClick={() => handleDelete(patient.id)}
                        className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="Șterge pacientul"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="6" className="py-8 text-center text-gray-400">
                  Nu s-au găsit pacienți înregistrați.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {totalPages > 1 && (
          <div className="bg-gray-50 px-6 py-4 border-t border-gray-100 flex items-center justify-between text-sm">
            <span className="text-gray-500">
              Pagina <strong className="text-gray-700">{page + 1}</strong> din <strong className="text-gray-700">{totalPages}</strong>
            </span>
            <div className="flex gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
                className="px-3 py-1 bg-white border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-50 transition-colors font-medium"
              >
                Anterior
              </button>
              <button
                disabled={page === totalPages - 1}
                onClick={() => setPage(page + 1)}
                className="px-3 py-1 bg-white border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-50 transition-colors font-medium"
              >
                Următor
              </button>
            </div>
          </div>
        )}
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-150">
            <h3 className="text-lg font-bold text-gray-900 mb-4">
              {isEditMode ? 'Actualizare Date Pacient' : 'Fișă Nouă Pacient'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Nume Complet</label>
                <input 
                  type="text"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  placeholder="Numele și prenumele pacientului"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Data Nașterii</label>
                <input 
                  type="date"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                  value={birthDate}
                  onChange={(e) => setBirthDate(e.target.value)}
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Gen</label>
                  <select 
                    className="w-full bg-white px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                    value={sex}
                    onChange={(e) => setSex(e.target.value === 'true')}
                  >
                    <option value="true">Masculin</option>
                    <option value="false">Feminin</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-gray-700 uppercase mb-1">Abonament Clinică</label>
                  <select 
                    className="w-full bg-white px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none text-sm"
                    value={subscription}
                    onChange={(e) => setSubscription(e.target.value === 'true')}
                  >
                    <option value="false">Inactiv (Standard)</option>
                    <option value="true">Activ (Premium)</option>
                  </select>
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button 
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border rounded-xl hover:bg-gray-50 text-gray-700 text-sm font-medium transition-colors"
                >
                  Anulare
                </button>
                <button 
                  type="submit"
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-bold transition-colors"
                >
                  {isEditMode ? 'Salvează Modificările' : 'Adaugă Pacient'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}